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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.Channel;

final class ConnectorController {

	private static final Logger LOGGER=LoggerFactory.getLogger(ConnectorController.class);

	private final CuratorController curatorController;
	private final BrokerController connectorController;
	private final DeliveryChannel connectorConfiguration;
	private DeliveryChannel effectiveConfiguration;

	ConnectorController(DeliveryChannel connectorConfiguration, CuratorController curatorController) {
		this.connectorConfiguration = connectorConfiguration;
		this.curatorController = curatorController;
		if(usesDifferentBrokers()) {
			this.connectorController=new BrokerController(this.connectorConfiguration.broker(),"connector-custom");
		} else {
			this.connectorController=this.curatorController.brokerController();
		}
	}

	void connect() throws ControllerException {
		this.connectorController.connect();
		Channel channel = this.connectorController.channel();
		String exchangeName = declareConnectorExchange(channel);
		String queueName = declareConnectorQueue(channel);
		String routingKey = bindConnectorQueue(channel, exchangeName, queueName);
		this.effectiveConfiguration=
			ProtocolFactory.
				newDeliveryChannel().
					withBroker(this.connectorController.broker()).
					withExchangeName(exchangeName).
					withQueueName(queueName).
					withRoutingKey(routingKey).
					build();
	}

	DeliveryChannel effectiveConfiguration() {
		return this.effectiveConfiguration;
	}

	void publishMessage(Message message) throws IOException {
		final String routingKey = this.effectiveConfiguration.routingKey();
		LOGGER.debug("Publishing message {} to routing key {}...",message,routingKey);
		try {
			this.connectorController.
				channel().
					basicPublish(
						this.effectiveConfiguration.exchangeName(),
						routingKey,
						null,
						MessageUtil.
							newInstance().
								toString(message).
									getBytes());
		} catch (IOException e) {
			LOGGER.warn("Could not publish message {} to routing key {}: {}",message,routingKey,e.getMessage());
			throw e;
		} catch (MessageConversionException e) {
			LOGGER.warn("Could not publish message {} to routing key {}: {}",message,routingKey,e.getMessage());
			throw new IOException("Could not serialize message",e);
		}
	}

	void handleMessage(MessageHandler handler) throws IOException {
		Channel channel = this.connectorController.channel();
		channel.basicConsume(
			this.connectorConfiguration.queueName(),
			true,
			new MessageHandlerConsumer(channel, handler)
		);
	}

	void disconnect() {
		this.connectorController.disconnect();
	}

	private boolean usesDifferentBrokers() {
		final Broker connectorBroker = this.connectorConfiguration.broker();
		return
			connectorBroker!=null &&
			!connectorBroker.equals(curatorConfiguration().broker());
	}

	private CuratorConfiguration curatorConfiguration() {
		return this.curatorController.curatorConfiguration();
	}

	private boolean connectorUsesSameQueueAsCurator(String connectorQueueName) {
		return
			curatorConfiguration().requestQueueName().equals(connectorQueueName) ||
			curatorConfiguration().responseQueueName().equals(connectorQueueName);
	}

	private String bindConnectorQueue(Channel channel, String exchangeName, String queueName) throws ControllerException {
		String routingKey=firstNonNull(this.connectorConfiguration.routingKey(), "");
		try {
			channel.queueBind(queueName,exchangeName,routingKey);
		} catch (IOException e) {
			throw new ControllerException("Could not bind connector queue '"+queueName+"' using routing key '"+routingKey+"' to exchange '"+exchangeName+"'",e);
		}
		return routingKey;
	}

	private String declareConnectorQueue(Channel channel) throws ControllerException {
		String queueName = this.connectorConfiguration.queueName();
		if(!usesDifferentBrokers() || !connectorUsesSameQueueAsCurator(queueName)) {
			if(queueName!=null) {
				try {
					channel.queueDeclare(queueName,true,false,false,null);
				} catch (IOException e) {
					throw new ControllerException("Could not declare connector queue named '"+queueName+"'",e);
				}
			} else {
				try {
					queueName=channel.queueDeclare().getQueue();
				} catch (IOException e) {
					throw new ControllerException("Could not declare anonymous connector queue",e);
				}
			}
		}
		return queueName;
	}

	private String declareConnectorExchange(Channel channel) throws ControllerException {
		String exchangeName=firstNonNull(this.connectorConfiguration.exchangeName(), curatorConfiguration().exchangeName());
		if(!usesDifferentBrokers() || !exchangeName.equals(curatorConfiguration().exchangeName())) {
			try {
				channel.exchangeDeclare(exchangeName,"direct");
			} catch (IOException e) {
				throw new ControllerException("Could not declare connector exchange named '"+exchangeName+"'",e);
			}
		}
		return exchangeName;
	}

	private String firstNonNull(String providedValue, String defaultValue) {
		return providedValue!=null?providedValue:defaultValue;
	}

}