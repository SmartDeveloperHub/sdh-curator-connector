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

import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

abstract class ConnectorController {

	private final CuratorController curatorController;
	private final BrokerController brokerController;
	private final DeliveryChannel connectorConfiguration;
	private final boolean requiresCreation;
	private DeliveryChannel effectiveConfiguration;

	ConnectorController(final DeliveryChannel connectorConfiguration, final ConversionContext context, final CuratorController curatorController, final boolean requiresCreation) {
		this.connectorConfiguration = connectorConfiguration;
		this.curatorController = curatorController;
		this.requiresCreation = requiresCreation;
		if(usesDifferentBrokers()) {
			this.brokerController=new BrokerController(this.connectorConfiguration.broker(),"connector-custom",context);
		} else {
			this.brokerController=this.curatorController.brokerController();
		}
	}

	final void connect() throws ControllerException {
		this.brokerController.connect();
		if(!this.requiresCreation) {
			return;
		}
		final String exchangeName = declareConnectorExchange();
		final String queueName = declareConnectorQueue();
		final String routingKey = bindConnectorQueue(exchangeName, queueName);
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

	private String bindConnectorQueue(final String exchangeName, final String queueName) throws ControllerException {
		final String routingKey=firstNonNull(this.connectorConfiguration.routingKey(), "");
		this.brokerController.bindQueue(exchangeName, queueName, routingKey);
		return routingKey;
	}

	private String declareConnectorQueue() throws ControllerException {
		String queueName = this.connectorConfiguration.queueName();
		if(usesDifferentBrokers() || !connectorUsesSameQueueAsCurator(queueName)) {
			queueName=brokerController().declareQueue(queueName);
		}
		return queueName;
	}

	private String declareConnectorExchange() throws ControllerException {
		final String exchangeName=firstNonNull(this.connectorConfiguration.exchangeName(), curatorConfiguration().exchangeName());
		if(usesDifferentBrokers() || !exchangeName.equals(curatorConfiguration().exchangeName())) {
			brokerController().declareExchange(exchangeName);
		}
		return exchangeName;
	}

	private String firstNonNull(final String providedValue, final String defaultValue) {
		return providedValue!=null?providedValue:defaultValue;
	}

}