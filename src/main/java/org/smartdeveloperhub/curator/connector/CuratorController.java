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
import java.util.Map;

import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.collect.ImmutableMap;
import com.rabbitmq.client.Channel;

abstract class CuratorController {

	private static final String EXCHANGE_TYPE = "topic";

	private final CuratorConfiguration configuration;
	private final BrokerController brokerController;

	CuratorController(final CuratorConfiguration configuration, final String name, final ConversionContext context) {
		this.configuration=configuration;
		this.brokerController=new BrokerController(configuration.broker(),name,context);
	}

	final void registerMessageHandler(final MessageHandler handler, final String queueName) throws IOException {
		final Channel channel = this.brokerController.channel();
		channel.basicConsume(
			queueName,
			true,
			new MessageHandlerConsumer(channel, handler)
		);
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
		configureBroker(agent);
	}

	final void disconnect() {
		this.brokerController.disconnect();
	}

	private DeliveryChannel replyTo(final String routingKey) {
		return
			ProtocolFactory.
				newDeliveryChannel().
					withExchangeName(this.curatorConfiguration().exchangeName()).
					withRoutingKey(routingKey).
					build();
	}

	private void configureBroker(final Agent agent) throws ControllerException {
		prepareExchange(this.brokerController.channel(), this.configuration.exchangeName());
		prepareQueue(agent);
	}

	protected abstract void prepareQueue(Agent agent) throws ControllerException;

	final void prepareQueue(final String queueName, final String routingKey) throws ControllerException {
		prepareQueue(this.brokerController.channel(), this.configuration.exchangeName(), queueName, routingKey);
	}

	private void prepareExchange(final Channel channel, final String exchangeName) throws ControllerException {
		try {
			channel.exchangeDeclare(exchangeName,EXCHANGE_TYPE,true,true,null);
		} catch (final IOException e) {
			throw new ControllerException("Could not create curator exchange named '"+exchangeName+"'",e);
		}
	}

	private void prepareQueue(final Channel channel, final String exchangeName, final String queueName, final String routingKey) throws ControllerException {
		try {
			final Map<String, Object> args=
				ImmutableMap.
					<String, Object>builder().
						put("x-expires",1000).
						build();
			channel.queueDeclare(queueName,true,false,true,args);
			this.brokerController.register(CleanerFactory.queueDelete(queueName));
		} catch (final IOException e) {
			throw new ControllerException("Could not create curator queue named '"+queueName+"'",e);
		}
		try {
			channel.queueBind(queueName,exchangeName,routingKey);
			this.brokerController.register(CleanerFactory.queueUnbind(exchangeName,queueName,routingKey));
		} catch (final IOException e) {
			throw new ControllerException("Could not bind curator queue '"+queueName+"' to exchange '"+exchangeName+"' using routing key '"+routingKey+"'",e);
		}
	}

}