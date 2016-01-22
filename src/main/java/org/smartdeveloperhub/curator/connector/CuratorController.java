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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.io.IOException;

import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Message;

abstract class CuratorController {

	private final CuratorConfiguration configuration;
	private final BrokerController brokerController;

	CuratorController(final CuratorConfiguration configuration, final String name, final ConversionContext context) {
		this.configuration=configuration;
		this.brokerController=new BrokerController(configuration.broker(),name,context);
	}

	final void registerMessageHandler(final MessageHandler handler) throws IOException {
		this.brokerController.registerConsumer(handler,this.configuration.queueName());
	}

	final void publishMessage(final Message message, final String routingKey) throws IOException {
		this.brokerController.publishMessage(replyTo(routingKey), message);
	}

	final void publishMessage(final String message, final String routingKey) throws IOException {
		this.brokerController.publishMessage(replyTo(routingKey), message);
	}

	final BrokerController brokerController() {
		return this.brokerController;
	}

	final CuratorConfiguration curatorConfiguration() {
		return this.configuration;
	}

	final void connect(final Agent agent) throws ControllerException {
		this.brokerController.connect();
		try {
			configureBroker(agent);
		} catch (final ControllerException e) {
			this.brokerController.disconnect();
			throw e;
		}
	}

	final void disconnect() {
		this.brokerController.disconnect();
	}

	protected abstract String routingKey(CuratorConfiguration configuration2, Agent agent) throws ControllerException;

	private DeliveryChannel replyTo(final String routingKey) {
		return
			ProtocolFactory.
				newDeliveryChannel().
					withExchangeName(this.curatorConfiguration().exchangeName()).
					withRoutingKey(routingKey).
					build();
	}

	private void configureBroker(final Agent agent) throws ControllerException {
		this.brokerController.declareExchange(this.configuration.exchangeName());
		final String routingKey = routingKey(this.configuration,agent);
		this.brokerController.prepareQueue(this.configuration.exchangeName(), this.configuration.queueName(), routingKey);
	}

}