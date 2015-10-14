/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.1.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.1.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public final class Connector {

	public static final class ConnectorBuilder {

		private CuratorConfiguration curatorConfiguration;
		private DeliveryChannel connectorChannel;
		private Agent agent;
		private MessageIdentifierFactory factory;

		private ConnectorBuilder() {
		}

		public ConnectorBuilder withCuratorConfiguration(CuratorConfiguration configuration) {
			this.curatorConfiguration = configuration;
			return this;
		}

		public ConnectorBuilder withMessageIdentifierFactory(MessageIdentifierFactory factory) {
			this.factory = factory;
			return this;
		}

		public ConnectorBuilder withAgentIdentifier(UUID identifier) {
			this.agent = ProtocolFactory.newAgent().withAgentId(identifier).build();
			return this;
		}

		public ConnectorBuilder withAgentIdentifier(String identifier) {
			return withAgentIdentifier(UUID.fromString(identifier));
		}

		public ConnectorBuilder withConnectorChannel(DeliveryChannel connectorChannel) {
			this.connectorChannel = connectorChannel;
			return this;
		}

		public Connector build() {
			return
				new Connector(
					curatorConfiguration(),
					agent(),
					connectorChannel(),
					this.factory!=null?this.factory:new DefaultMessageIdentifierFactory());
		}

		private DeliveryChannel connectorChannel() {
			return
				this.connectorChannel!=null?
					this.connectorChannel:
					ProtocolFactory.
						newDeliveryChannel().
							build();
		}

		private Agent agent() {
			return
				this.agent!=null?
					this.agent:
					ProtocolFactory.
						newAgent().
							withAgentId(UUID.randomUUID()).
							build();
		}

		private CuratorConfiguration curatorConfiguration() {
			return
				this.curatorConfiguration!=null?
					this.curatorConfiguration:
					CuratorConfiguration.newInstance();
		}

	}

	private final class CuratorResponseListener implements MessageHandler {

		@Override
		public void handlePayload(String payload) {
			LOGGER.trace("Received message in connector's curator response queue: {}",payload);
			try {
				AcceptedMessage response =
					MessageUtil.
						newInstance().
							fromString(payload, AcceptedMessage.class);
				processAcceptance(response);
			} catch (MessageConversionException e) {
				LOGGER.warn("Could not process curator response:\n{}\n. Full stacktrace follows",payload,e);
			}
		}

	}

	private final class ConnectorResponseListener implements MessageHandler {

		@Override
		public void handlePayload(String payload) {
			LOGGER.trace("Received message in connector's response queue: {}",payload);
			try {
				EnrichmentResponseMessage response =
					MessageUtil.
						newInstance().
							fromString(payload, EnrichmentResponseMessage.class);
				processEnrichmentResponse(response);
			} catch (MessageConversionException e) {
				LOGGER.warn("Could not process message:\n{}\n. Full stacktrace follows",e);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(Connector.class);

	private final ClientCuratorController curatorController;
	private final ClientConnectorController connectorController;

	private final Lock read;
	private final Lock write;

	private final ConcurrentMap<UUID,ConnectorFuture> pendingAcknowledgements;
	private final ConcurrentMap<UUID,EnrichmentResultHandler> activeRequests;

	private final ConnectorConfiguration configuration;
	private final MessageIdentifierFactory factory;

	private boolean connected;

	private Connector(CuratorConfiguration curatorConfiguration, Agent agent, DeliveryChannel connectorChannel, MessageIdentifierFactory factory) {
		this.factory = factory;
		this.configuration =
			new ConnectorConfiguration().
				withCuratorConfiguration(curatorConfiguration).
				withConnectorChannel(connectorChannel).
				withAgent(agent);
		this.curatorController = new ClientCuratorController(curatorConfiguration,"connector-curator");
		this.connectorController=new ClientConnectorController(connectorChannel,this.curatorController);
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.connected=false;
		this.pendingAcknowledgements=Maps.newConcurrentMap();
		this.activeRequests=Maps.newConcurrentMap();
	}

	private void processAcceptance(AcceptedMessage response) {
		ConnectorFuture future=this.pendingAcknowledgements.get(response.responseTo());
		if(future==null) {
			LOGGER.warn("Could not process curator response {}: unknown curator request {}",response,response.responseTo());
		} else {
			this.pendingAcknowledgements.remove(future.messageId(),future);
			try {
				future.complete(response);
			} catch (InterruptedException e) {
				LOGGER.warn("Could not complete request {} with curator response {}: {}",future.messageId(),response,e.getMessage());
			}
		}
	}

	private void processEnrichmentResponse(EnrichmentResponseMessage response) {
		EnrichmentResultHandler handler=this.activeRequests.get(response.responseTo());
		if(handler!=null) {
			LOGGER.trace("Handling processing of response {} for request {} to handler {}...",response.messageId(),response.responseTo(),handler);
			final EnrichmentResult result = ProtocolUtil.toEnrichmentResult(response);
			handler.onResult(result);
		} else {
			LOGGER.debug("Discarded response {}.",response);
		}
	}

	private ConnectorFuture addRequest(EnrichmentRequestMessage message, EnrichmentResultHandler handler) {
		ConnectorFuture future= new LoggedConnectorFuture(new DefaultConnectorFuture(this,message));
		this.pendingAcknowledgements.put(future.messageId(),future);
		this.activeRequests.put(future.messageId(), handler);
		return future;
	}

	private void clearRequests() {
		for(Entry<UUID,ConnectorFuture> entry:this.pendingAcknowledgements.entrySet()) {
			entry.getValue().cancel(true);
		}
		this.pendingAcknowledgements.clear();
		this.activeRequests.clear();
	}

	private void publishDisconnectMessage() throws ConnectorException  {
		try {
			this.curatorController.
				publishRequest(
					ProtocolFactory.
						newDisconnectMessage().
							withMessageId(this.factory.nextIdentifier()).
							withSubmittedOn(new Date()).
							withSubmittedBy(this.configuration.agent()).
							build());
		} catch (IOException e) {
			LOGGER.warn("Could not send disconnect to curator",e);
			throw new ConnectorException("Could not send disconnect to curator",e);
		}
	}

	private void addCuratorResponseHandler(MessageHandler handler) throws ConnectorException {
		try {
			this.curatorController.handleResponses(handler);
		} catch (Exception e) {
			throw new ConnectorException("Could not setup the curator response message handler",e);
		}
	}

	private void addConnectorResponseHandler(MessageHandler handler) throws ConnectorException {
		try {
			this.connectorController.handleMessage(handler);
		} catch (Exception e) {
			throw new ConnectorException("Could not setup the connector response message handler",e);
		}
	}

	private void connectToCurator() throws ConnectorException {
		this.curatorController.connect();
		try {
			connectController();
		} catch (ConnectorException e) {
			this.curatorController.disconnect();
			throw e;
		}
	}

	private void connectController() throws ConnectorException {
		this.connectorController.connect();
		try {
			this.configuration.withConnectorChannel(this.connectorController.effectiveConfiguration());
			addCuratorResponseHandler(new CuratorResponseListener());
			addConnectorResponseHandler(new ConnectorResponseListener());
		} catch (Exception e) {
			this.connectorController.disconnect();
			throw e;
		}
	}

	public void connect() throws ConnectorException {
		this.write.lock();
		try {
			Preconditions.checkState(!this.connected,"Already connected");
			LOGGER.info("-->> CONNECTING <<--");
			connectToCurator();
			this.connected=true;
			LOGGER.info(this.configuration.toString());
			LOGGER.info("-->> CONNECTED <<--");
		} catch (Exception e) {
			LOGGER.error("-->> CONNECTION FAILED <<--",e);
			throw e;
		} finally {
			this.write.unlock();
		}
	}

	public Future<Acknowledge> requestEnrichment(EnrichmentSpecification specification, EnrichmentResultHandler handler) throws IOException {
		this.read.lock();
		try {
			Preconditions.checkState(this.connected,"Not connected");
			EnrichmentRequestMessage message=
				ProtocolUtil.
					toRequestBuilder(specification).
						withMessageId(this.factory.nextIdentifier()).
						withSubmittedOn(new Date()).
						withSubmittedBy(this.configuration.agent()).
						withReplyTo(this.configuration.connectorChannel()).
						build();
			ConnectorFuture future = addRequest(message,handler);
			this.curatorController.publishRequest(message);
			future.start();
			return future;
		} finally {
			this.read.unlock();
		}
	}

	public void disconnect() throws ConnectorException {
		this.write.lock();
		try {
			Preconditions.checkState(this.connected,"Not connected");
			LOGGER.info("-->> DISCONNECTING <<--");
			LOGGER.info(this.configuration.toString());
			try {
				publishDisconnectMessage();
			} finally {
				clearRequests();
				this.connectorController.disconnect();
				this.curatorController.disconnect();
				this.connected=false;
				LOGGER.info("-->> DISCONNECTED <<--");
			}
		} finally {
			this.write.unlock();
		}
	}

	void cancelRequest(ConnectorFuture future) {
		this.pendingAcknowledgements.remove(future.messageId(),future);
		this.activeRequests.remove(future.messageId());
	}

	public static ConnectorBuilder builder() {
		return new ConnectorBuilder();
	}

}
