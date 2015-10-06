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
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.rabbitmq.client.Channel;

public final class Connector {

	public static final class ConnectorBuilder {

		private CuratorConfiguration curatorConfiguration;
		private DeliveryChannel connectorChannel;
		private Agent agent;

		public ConnectorBuilder withCuratorConfiguration(CuratorConfiguration configuration) {
			this.curatorConfiguration = configuration;
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
					connectorChannel());
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
				Accepted response =
					MessageUtil.
						newInstance().
							fromString(payload, Accepted.class);
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
				EnrichmentResponse response =
					MessageUtil.
						newInstance().
							fromString(payload, EnrichmentResponse.class);
				LOGGER.info("Should process EnrichmentResponse {}",response);
			} catch (MessageConversionException e) {
				LOGGER.warn("Could not process EnrichmentResponse:\n{}\n. Full stacktrace follows",e);
			}
		}

	}

	private final class NullMessageHandler implements MessageHandler {

		@Override
		public void handlePayload(String payload) {
			// NO IMPLEMENTATION REQUIRED YET
			// AS THIS TYPE IS TO BE REPLACED
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(Connector.class);

	private static final String NL = System.lineSeparator();

	private final CuratorConfiguration curatorConfiguration;
	private final Agent agent;
	private final CuratorController curatorController;
	private final BrokerController connectorController;

	private final Lock read;
	private final Lock write;

	private final ConcurrentMap<UUID,ConnectorFuture> pendingAcknowledgements;
	private final ConcurrentMap<UUID,MessageHandler> activeRequests;

	private DeliveryChannel connectorConfiguration;

	private boolean connected;

	private Connector(CuratorConfiguration configuration, Agent agent, DeliveryChannel connectorChannel) {
		this.curatorConfiguration = configuration;
		this.agent = agent;
		this.connectorConfiguration = connectorChannel;
		this.curatorController = new CuratorController(this.curatorConfiguration,"connector-curator");
		if(usesDifferentBrokers()) {
			this.connectorController=new BrokerController(this.connectorConfiguration.broker(),"connector-custom");
		} else {
			this.connectorController = this.curatorController.brokerController();
		}
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.connected=false;
		this.pendingAcknowledgements=Maps.newConcurrentMap();
		this.activeRequests=Maps.newConcurrentMap();
	}

	void processAcceptance(Accepted response) {
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

	void cancelRequest(ConnectorFuture future) {
		this.pendingAcknowledgements.remove(future.messageId(),future);
		this.activeRequests.remove(future.messageId());
	}

	private boolean usesDifferentBrokers() {
		final Broker connectorBroker = this.connectorConfiguration.broker();
		return
			connectorBroker!=null &&
			!connectorBroker.equals(this.curatorConfiguration.broker());
	}

	private boolean connectorUsesSameQueueAsCurator(String connectorQueueName) {
		return
			this.curatorConfiguration.requestQueueName().equals(connectorQueueName) ||
			this.curatorConfiguration.responseQueueName().equals(connectorQueueName);
	}

	private DeliveryChannel configureConnectorQueue() throws ConnectorException {
		this.connectorController.connect();
		Channel channel = this.connectorController.channel();
		String exchangeName = declareConnectorExchange(channel);
		String queueName = declareConnectorQueue(channel);
		String routingKey = bindConnectorQueue(channel, exchangeName, queueName);
		return
			ProtocolFactory.
				newDeliveryChannel().
					withBroker(this.connectorController.broker()).
					withExchangeName(exchangeName).
					withQueueName(queueName).
					withRoutingKey(routingKey).
					build();
	}

	private String bindConnectorQueue(Channel channel, String exchangeName, String queueName) throws ConnectorException {
		String routingKey=firstNonNull(this.connectorConfiguration.routingKey(), "");
		try {
			channel.queueBind(queueName,exchangeName,routingKey);
		} catch (IOException e) {
			throw new ConnectorException("Could not bind connector queue '"+queueName+"' using routing key '"+routingKey+"' to exchange '"+exchangeName+"'",e);
		}
		return routingKey;
	}

	private String declareConnectorQueue(Channel channel) throws ConnectorException {
		String queueName = this.connectorConfiguration.queueName();
		if(!usesDifferentBrokers() || !connectorUsesSameQueueAsCurator(queueName)) {
			if(queueName!=null) {
				try {
					channel.queueDeclare(queueName,true,false,false,null);
				} catch (IOException e) {
					throw new ConnectorException("Could not declare connector queue named '"+queueName+"'",e);
				}
			} else {
				try {
					queueName=channel.queueDeclare().getQueue();
				} catch (IOException e) {
					throw new ConnectorException("Could not declare anonymous connector queue",e);
				}
			}
		}
		return queueName;
	}

	private String declareConnectorExchange(Channel channel) throws ConnectorException {
		String exchangeName=firstNonNull(this.connectorConfiguration.exchangeName(), this.curatorConfiguration.exchangeName());
		if(!usesDifferentBrokers() || !exchangeName.equals(this.curatorConfiguration.exchangeName())) {
			try {
				channel.exchangeDeclare(exchangeName,"direct");
			} catch (IOException e) {
				throw new ConnectorException("Could not declare connector exchange named '"+exchangeName+"'",e);
			}
		}
		return exchangeName;
	}

	private String firstNonNull(String providedValue, String defaultValue) {
		return providedValue!=null?providedValue:defaultValue;
	}

	private void logConnectionDetails() {
		StringBuilder builder=new StringBuilder();
		builder.append("-- Connector details:").append(NL);
		builder.append("   + Curator configuration:").append(NL);
		appendBrokerDetails(builder, this.curatorConfiguration.broker());
		appendExchangeName(builder, this.curatorConfiguration.exchangeName());
		builder.append("     - Request queue name.: ").append(this.curatorConfiguration.requestQueueName()).append(NL);
		appendResponseQueueDetails(builder, this.curatorConfiguration.responseQueueName());
		builder.append("   + Connector configuration:").append(NL);
		appendBrokerDetails(builder, this.connectorConfiguration.broker());
		appendExchangeName(builder, this.connectorConfiguration.exchangeName());
		appendResponseQueueDetails(builder, this.connectorConfiguration.queueName());
		builder.append("     - Routing key........: ").append(this.connectorConfiguration.routingKey());
		LOGGER.info(builder.toString());
	}

	private void appendResponseQueueDetails(StringBuilder builder, String responseQueueName) {
		builder.append("     - Response queue name: ").append(responseQueueName).append(NL);
	}

	private void appendExchangeName(StringBuilder builder, String exchangeName) {
		builder.append("     - Exchange name......: ").append(exchangeName).append(NL);
	}

	private void appendBrokerDetails(StringBuilder builder, Broker broker) {
		builder.append("     - Broker").append(NL);
		builder.append("       + Host.............: ").append(broker.host()).append(NL);
		builder.append("       + Port.............: ").append(broker.port()).append(NL);
		builder.append("       + Virtual host.....: ").append(broker.virtualHost()).append(NL);
	}

	private ConnectorFuture addRequest(EnrichmentRequest message, MessageHandler handler) {
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
						newDisconnect().
							withMessageId(UUID.randomUUID()).
							withSubmittedOn(new Date()).
							withSubmittedBy(this.agent).
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
			Channel channel = this.connectorController.channel();
			channel.basicConsume(
				this.connectorConfiguration.queueName(),
				true,
				new MessageHandlerConsumer(channel, handler)
			);
		} catch (IOException e) {
			throw new ConnectorException("Could not setup the connector response message handler",e);
		}
	}

	public void connect() throws ConnectorException {
		this.write.lock();
		try {
			Preconditions.checkState(!this.connected,"Already connected");
			LOGGER.info("-->> CONNECTING <<--");
			this.curatorController.connect();
			this.connectorConfiguration=configureConnectorQueue();
			addCuratorResponseHandler(new CuratorResponseListener());
			addConnectorResponseHandler(new ConnectorResponseListener());
			this.connected=true;
			logConnectionDetails();
			LOGGER.info("-->> CONNECTED <<--");
		} catch (ConnectorException e) {
			LOGGER.error("-->> CONNECTION FAILED <<--",e);
			throw e;
		} finally {
			this.write.unlock();
		}
	}

	public Future<Acknowledge> requestEnrichment(URI targetResource) throws IOException {
		this.read.lock();
		try {
			Preconditions.checkState(this.connected,"Not connected");
			EnrichmentRequest message=
				ProtocolFactory.
					newEnrichmentRequest().
						withMessageId(UUID.randomUUID()).
						withSubmittedOn(new Date()).
						withSubmittedBy(this.agent).
						withReplyTo(this.connectorConfiguration).
						withTargetResource(targetResource).
						build();
			ConnectorFuture future = addRequest(message, new NullMessageHandler());
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
			logConnectionDetails();
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

	public static ConnectorBuilder builder() {
		return new ConnectorBuilder();
	}

}
