/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.io.IOException;
import java.net.URI;
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
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.ResponseMessage;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public final class Connector {

	public static final class ConnectorBuilder {

		private CuratorConfiguration curatorConfiguration;
		private DeliveryChannel connectorChannel;
		private MessageIdentifierFactory factory;
		private UUID agentIdentifier;
		private ConversionContext context;
		private String queueName;

		private ConnectorBuilder() {
			this.context=ConversionContext.newInstance();
		}

		public ConnectorBuilder withBase(final URI base) {
			this.context=this.context.withBase(base);
			return this;
		}

		public ConnectorBuilder withBase(final String base) {
			return withBase(URI.create(base));
		}

		public ConnectorBuilder withNamespacePrefix(final String namespace, final String prefix) {
			this.context=this.context.withNamespacePrefix(namespace, prefix);
			return this;
		}

		public ConnectorBuilder withCuratorConfiguration(final CuratorConfiguration configuration) {
			this.curatorConfiguration = configuration;
			return this;
		}

		public ConnectorBuilder withMessageIdentifierFactory(final MessageIdentifierFactory factory) {
			this.factory = factory;
			return this;
		}

		public ConnectorBuilder withAgentIdentifier(final UUID identifier) {
			this.agentIdentifier = identifier;
			return this;
		}

		public ConnectorBuilder withAgentIdentifier(final String identifier) {
			UUID agentId=null;
			if(identifier!=null) {
				agentId=UUID.fromString(identifier);
			}
			return withAgentIdentifier(agentId);
		}

		public ConnectorBuilder withConnectorChannel(final DeliveryChannel connectorChannel) {
			this.connectorChannel = connectorChannel;
			return this;
		}

		public ConnectorBuilder withQueueName(final String queueName) {
			this.queueName = queueName;
			return this;
		}

		public Connector build() {
			final ConnectorConfiguration conf =
				new ConnectorConfiguration().
					withCuratorConfiguration(curatorConfiguration()).
					withQueueName(this.queueName).
					withConnectorChannel(connectorChannel()).
					withAgent(agent());
			return
				new Connector(
					conf,
					this.context,
					this.factory!=null?this.factory:new DefaultMessageIdentifierFactory());
		}

		private DeliveryChannel connectorChannel() {
			return
				this.connectorChannel!=null?
					this.connectorChannel:
					ProtocolFactory.
						newDeliveryChannel().
							withRoutingKey("connector").
							build();
		}

		private Agent agent() {
			UUID agentId=this.agentIdentifier;
			if(agentId==null) {
				agentId=UUID.randomUUID();
			}
			return
				ProtocolFactory.
					newAgent().
						withAgentId(agentId).
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
		public void handlePayload(final String payload) {
			LOGGER.trace("Received message in connector's curator response queue: {}",payload);
			final AcceptedMessage acceptedMessage=HandlerUtil.parsePayload(payload,AcceptedMessage.class);
			if(acceptedMessage!=null) {
				processAcknowledgement(acceptedMessage);
				return;
			}
			final FailureMessage failureMessage=HandlerUtil.parsePayload(payload,FailureMessage.class);
			if(failureMessage!=null) {
				processAcknowledgement(failureMessage);
				return;
			}
			LOGGER.error("Could not understand request:\n{}",payload);
		}

	}

	private final class ConnectorResponseListener implements MessageHandler {

		@Override
		public void handlePayload(final String payload) {
			LOGGER.trace("Received message in connector's response queue: {}",payload);
			final EnrichmentResponseMessage response=HandlerUtil.parsePayload(payload, EnrichmentResponseMessage.class);
			if(response!=null) {
				processEnrichmentResponse(response);
				return;
			}
			LOGGER.error("Could not understand request:\n{}",payload);
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

	private Connector(final ConnectorConfiguration configuration, final ConversionContext context, final MessageIdentifierFactory factory) {
		this.configuration = configuration;
		this.factory = factory;
		this.curatorController=new ClientCuratorController(configuration.curatorConfiguration(),"connector-curator",context);
		this.connectorController=new ClientConnectorController(configuration.queueName(),configuration.connectorChannel(),context,this.curatorController);
		final ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.connected=false;
		this.pendingAcknowledgements=Maps.newConcurrentMap();
		this.activeRequests=Maps.newConcurrentMap();
	}

	private void processAcknowledgement(final ResponseMessage response) {
		final ConnectorFuture future=this.pendingAcknowledgements.get(response.responseTo());
		if(future==null) {
			LOGGER.debug("Could not process curator acknowledgement {}: unknown enrichment request {}",response,response.responseTo());
		} else {
			LOGGER.trace("Acknowledging enrichment request {} to {}...",response.responseTo(),future);
			this.pendingAcknowledgements.remove(future.messageId(),future);
			if(response instanceof FailureMessage) {
				this.activeRequests.remove(future.messageId());
			}
			try {
				future.complete(response);
			} catch (final InterruptedException e) {
				LOGGER.warn("Could not complete request {} with curator response {}: {}",future.messageId(),response,e.getMessage());
			}
		}
	}

	private void processEnrichmentResponse(final EnrichmentResponseMessage response) {
		final EnrichmentResultHandler handler=this.activeRequests.get(response.responseTo());
		if(handler==null) {
			LOGGER.debug("Discarded enrichment response {}: unknown enrichment request {}",response,response.responseTo());
		} else {
			LOGGER.trace("Handling processing of response {} for request {} to handler {}...",response.messageId(),response.responseTo(),handler);
			final EnrichmentResult result = ProtocolUtil.toEnrichmentResult(response);
			handler.onResult(result);
		}
	}

	private ConnectorFuture addRequest(final EnrichmentRequestMessage message, final EnrichmentResultHandler handler) {
		final ConnectorFuture future= new LoggedConnectorFuture(new DefaultConnectorFuture(this,message));
		this.pendingAcknowledgements.put(future.messageId(),future);
		this.activeRequests.put(future.messageId(), handler);
		return future;
	}

	private void clearRequests() {
		for(final Entry<UUID,ConnectorFuture> entry:this.pendingAcknowledgements.entrySet()) {
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
		} catch (final IOException e) {
			LOGGER.warn("Could not send disconnect to curator",e);
			throw new ConnectorException("Could not send disconnect to curator",e);
		}
	}

	private void addCuratorResponseHandler(final MessageHandler handler) throws ConnectorException {
		try {
			this.curatorController.registerMessageHandler(handler);
		} catch (final Exception e) {
			throw new ConnectorException("Could not setup the curator response message handler",e);
		}
	}

	private void addConnectorResponseHandler(final MessageHandler handler) throws ConnectorException {
		try {
			this.connectorController.handleMessage(handler);
		} catch (final Exception e) {
			throw new ConnectorException("Could not setup the connector response message handler",e);
		}
	}

	private void connectToCurator() throws ConnectorException {
		this.curatorController.connect(this.configuration.agent());
		try {
			connectController();
		} catch (final Exception e) {
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
		} catch (final Exception e) {
			this.connectorController.disconnect();
			throw e;
		}
	}

	private void verifyConnection() {
		Preconditions.checkState(this.connected,"Not connected");
	}

	public void connect() throws ConnectorException {
		this.write.lock();
		try {
			Preconditions.checkState(!this.connected,"Already connected");
			try {
				LOGGER.info("-->> CONNECTING <<--");
				connectToCurator();
				this.connected=true;
				LOGGER.info(this.configuration.toString());
				LOGGER.info("-->> CONNECTED <<--");
			} catch (final Exception e) {
				LOGGER.error("-->> CONNECTION FAILED <<--",e);
				throw e;
			}
		} finally {
			this.write.unlock();
		}
	}

	public Future<Enrichment> requestEnrichment(final EnrichmentRequest request, final EnrichmentResultHandler handler) throws IOException {
		this.read.lock();
		try {
			verifyConnection();
			LOGGER.debug("Requesting {}",request);
			final EnrichmentRequestMessage message=
				ProtocolUtil.
					toRequestBuilder(request).
						withMessageId(this.factory.nextIdentifier()).
						withSubmittedOn(new Date()).
						withSubmittedBy(this.configuration.agent()).
						withReplyTo(this.configuration.connectorChannel()).
						build();
			final ConnectorFuture future = addRequest(message,handler);
			this.curatorController.publishRequest(message);
			future.start();
			LOGGER.debug("Enrichment requested: {}",future);
			return future;
		} finally {
			this.read.unlock();
		}
	}

	public void cancelEnrichment(final Enrichment enrichment) {
		this.read.lock();
		try {
			verifyConnection();
			LOGGER.debug("Cancelling enrichment {}...",enrichment);
			if(enrichment.cancel()) {
				this.activeRequests.remove(enrichment.messageId());
				LOGGER.debug("Enrichment {} cancelled.",enrichment);
			}
		} finally {
			this.read.unlock();
		}
	}

	public void disconnect() throws ConnectorException {
		this.write.lock();
		try {
			verifyConnection();
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

	void abortRequest(final ConnectorFuture future) {
		LOGGER.debug("Aborting enrichment request {}...",future.messageId());
		this.pendingAcknowledgements.remove(future.messageId());
		this.activeRequests.remove(future.messageId());
	}

	public static ConnectorBuilder builder() {
		return new ConnectorBuilder();
	}

}
