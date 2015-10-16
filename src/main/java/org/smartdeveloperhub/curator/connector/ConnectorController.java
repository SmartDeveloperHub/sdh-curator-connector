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

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.rabbitmq.client.Channel;

abstract class ConnectorController {

	private final CuratorController curatorController;
	private final BrokerController brokerController;
	private final DeliveryChannel connectorConfiguration;
	private DeliveryChannel effectiveConfiguration;

	ConnectorController(final DeliveryChannel connectorConfiguration, final CuratorController curatorController) {
		this.connectorConfiguration = connectorConfiguration;
		this.curatorController = curatorController;
		if(usesDifferentBrokers()) {
			this.brokerController=new BrokerController(this.connectorConfiguration.broker(),"connector-custom");
		} else {
			this.brokerController=this.curatorController.brokerController();
		}
	}

	final void connect() throws ControllerException {
		this.brokerController.connect();
		final Channel channel = this.brokerController.channel();
		final String exchangeName = declareConnectorExchange(channel);
		final String queueName = declareConnectorQueue(channel);
		final String routingKey = bindConnectorQueue(channel, exchangeName, queueName);
		this.effectiveConfiguration=
			ProtocolFactory.
				newDeliveryChannel().
					withBroker(this.brokerController.broker()).
					withExchangeName(exchangeName).
					withQueueName(queueName).
					withRoutingKey(routingKey).
					build();
	}

	final DeliveryChannel effectiveConfiguration() {
		return this.effectiveConfiguration;
	}

	final BrokerController brokerController() {
		return this.brokerController;
	}

	final void disconnect() {
		this.brokerController.disconnect();
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

	private boolean connectorUsesSameQueueAsCurator(final String connectorQueueName) {
		return
			curatorConfiguration().requestQueueName().equals(connectorQueueName) ||
			curatorConfiguration().responseQueueName().equals(connectorQueueName);
	}

	private String bindConnectorQueue(final Channel channel, final String exchangeName, final String queueName) throws ControllerException {
		final String routingKey=firstNonNull(this.connectorConfiguration.routingKey(), "");
		try {
			channel.queueBind(queueName,exchangeName,routingKey);
			return routingKey;
		} catch (final IOException e) {
			throw new ControllerException("Could not bind connector queue '"+queueName+"' using routing key '"+routingKey+"' to exchange '"+exchangeName+"'",e);
		}
	}

	private String declareConnectorQueue(final Channel channel) throws ControllerException {
		String queueName = this.connectorConfiguration.queueName();
		if(usesDifferentBrokers() || !connectorUsesSameQueueAsCurator(queueName)) {
			if(queueName!=null) {
				try {
					channel.queueDeclare(queueName,true,false,false,null);
				} catch (final IOException e) {
					throw new ControllerException("Could not declare connector queue named '"+queueName+"'",e);
				}
			} else {
				try {
					queueName=channel.queueDeclare().getQueue();
				} catch (final IOException e) {
					throw new ControllerException("Could not declare anonymous connector queue",e);
				}
			}
		}
		return queueName;
	}

	private String declareConnectorExchange(final Channel channel) throws ControllerException {
		final String exchangeName=firstNonNull(this.connectorConfiguration.exchangeName(), curatorConfiguration().exchangeName());
		if(usesDifferentBrokers() || !exchangeName.equals(curatorConfiguration().exchangeName())) {
			try {
				channel.exchangeDeclare(exchangeName,"direct");
			} catch (final IOException e) {
				throw new ControllerException("Could not declare connector exchange named '"+exchangeName+"'",e);
			}
		}
		return exchangeName;
	}

	private String firstNonNull(final String providedValue, final String defaultValue) {
		return providedValue!=null?providedValue:defaultValue;
	}

}