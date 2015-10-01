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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;

import com.google.common.base.Preconditions;
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

	private final class CuratorResponseHandler implements Runnable {

		private final class ResponseMessageHandler implements MessageHandler {

			@Override
			public void handlePayload(String payload) {
				System.out.println("Received response: "+payload);
			}

			@Override
			public void handleCancel() {
				System.out.println("Closing response handler...");
			}

		}

		@Override
		public void run() {
			try {
				Connector.this.curatorController.handleResponses(new ResponseMessageHandler());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private final CuratorConfiguration curatorConfiguration;
	private final Agent agent;
	private final DeliveryChannel connectorConfiguration;

	private final CuratorController curatorController;
	private final BrokerController connectorController;

	private final Lock read;
	private final Lock write;
	private boolean connected;
	private ExecutorService executor;

	private Connector(CuratorConfiguration configuration, Agent agent, DeliveryChannel connectorChannel) {
		this.curatorConfiguration = configuration;
		this.agent = agent;
		this.connectorConfiguration = connectorChannel;
		this.curatorController = new CuratorController(this.curatorConfiguration);
		if(usesDifferentBrokers()) {
			this.connectorController=new BrokerController(this.connectorConfiguration.broker());
		} else {
			this.connectorController = this.curatorController.brokerController();
		}
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.connected=false;
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

	private DeliveryChannel prepareConnectorQueue() throws IOException, TimeoutException {
		this.connectorController.connect();

		Channel channel = this.connectorController.channel();

		String exchangeName=firstNonNull(this.connectorConfiguration.exchangeName(), this.curatorConfiguration.exchangeName());
		if(!usesDifferentBrokers() || !exchangeName.equals(this.curatorConfiguration.exchangeName())) {
			channel.exchangeDeclare(exchangeName,"direct");
		}

		String queueName = this.connectorConfiguration.queueName();
		if(!usesDifferentBrokers() || !connectorUsesSameQueueAsCurator(queueName)) {
			if(queueName==null) {
				channel.queueDeclare(this.connectorConfiguration.queueName(),true,false,false,null);
			} else {
				queueName=channel.queueDeclare().getQueue();
			}
		}

		String routingKey=firstNonNull(this.connectorConfiguration.routingKey(), "");
		channel.queueBind(queueName,exchangeName,routingKey);

		return
			ProtocolFactory.
				newDeliveryChannel().
					withBroker(this.connectorController.broker()).
					withExchangeName(exchangeName).
					withQueueName(queueName).
					withRoutingKey(routingKey).
					build();
	}

	private String firstNonNull(String providedValue, String defaultValue) {
		return providedValue!=null?providedValue:defaultValue;
	}

	private void shutdownExecutorQuietly() {
		this.executor.shutdown();
		while(!this.executor.isTerminated()) {
			try {
				this.executor.awaitTermination(100,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void connect() throws IOException, TimeoutException {
		this.write.lock();
		try {
			Preconditions.checkState(!this.connected,"Already connected");
			this.executor = Executors.newFixedThreadPool(2);
			try {
				this.curatorController.connect();
				prepareConnectorQueue();
				this.executor.submit(new CuratorResponseHandler());
				this.connected=true;
			} catch (IOException | TimeoutException e) {
				shutdownExecutorQuietly();
				throw e;
			}
		} finally {
			this.write.unlock();
		}
	}

	public void requestEnrichment(URI targetResource) throws IOException {
		this.read.lock();
		try {
			Preconditions.checkState(this.connected,"Not connected");
			EnrichmentRequest message = ProtocolFactory.
				newEnrichmentRequest().
					withMessageId(UUID.randomUUID()).
					withSubmittedOn(new Date()).
					withSubmittedBy(this.agent).
					withReplyTo(this.connectorConfiguration).
					withTargetResource(targetResource).
					build();
			this.curatorController.publish(message);
		} finally {
			this.read.unlock();
		}
	}

	public void disconnect() {
		this.write.lock();
		try {
			Preconditions.checkState(this.connected,"Not connected");
			this.connectorController.disconnect();
			this.curatorController.disconnect();
			shutdownExecutorQuietly();
			this.connected=false;
		} finally {
			this.write.unlock();
		}
	}

	public static ConnectorBuilder builder() {
		return new ConnectorBuilder();
	}

}
