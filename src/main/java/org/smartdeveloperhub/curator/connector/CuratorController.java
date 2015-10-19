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
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.Channel;

abstract class CuratorController {

	private static final Logger LOGGER=LoggerFactory.getLogger(CuratorController.class);

	private final CuratorConfiguration configuration;
	private final BrokerController brokerController;
	private final ConversionContext context;

	CuratorController(final CuratorConfiguration configuration, final String name, final ConversionContext context) {
		this.configuration=configuration;
		this.context = context;
		this.brokerController=new BrokerController(configuration.broker(),name);
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
		LOGGER.debug("Publishing message {} to routing key {}...",message,routingKey);
		try {
			publishMessage(
				MessageUtil.
					newInstance().
						withConversionContext(this.context).
						toString(message),
				routingKey);
		} catch (final MessageConversionException e) {
			LOGGER.error("Could not publish message {} to routing key {}: {}",message,routingKey,e.getMessage());
			throw new IOException("Could not serialize message",e);
		}
	}

	final void publishMessage(final String message, final String routingKey) throws IOException {
		LOGGER.trace("Publishing message {} to routing key {}...",message,routingKey);
		try {
			this.brokerController.
				channel().
					basicPublish(
						this.curatorConfiguration().exchangeName(),
						routingKey,
						null,
						message.getBytes());
		} catch (final Exception e) {
			LOGGER.debug("Could not publish message {} to routing key {}: {}",message,routingKey,e.getMessage());
			throw e;
		}
	}

	final BrokerController brokerController() {
		return this.brokerController;
	}

	final CuratorConfiguration curatorConfiguration() {
		return this.configuration;
	}

	final void connect() throws ControllerException {
		this.brokerController.connect();
		configureBroker();
	}

	final void disconnect() {
		this.brokerController.disconnect();
	}

	private void configureBroker() throws ControllerException {
		final Channel channel = this.brokerController.channel();
		prepareExchange(channel, this.configuration.exchangeName());
		prepareQueue(channel, this.configuration.exchangeName(), this.configuration.requestQueueName(), this.configuration.requestRoutingKey());
		prepareQueue(channel, this.configuration.exchangeName(), this.configuration.responseQueueName(), this.configuration.responseRoutingKey());
	}

	private void prepareExchange(final Channel channel, final String exchangeName) throws ControllerException {
		try {
			channel.exchangeDeclare(exchangeName,"direct");
		} catch (final IOException e) {
			throw new ControllerException("Could not create curator exchange named '"+exchangeName+"'",e);
		}
	}

	private void prepareQueue(final Channel channel, final String exchangeName, final String queueName, final String routingKey) throws ControllerException {
		try {
			channel.queueDeclare(queueName,true,false,false,null);
		} catch (final IOException e) {
			throw new ControllerException("Could not create curator queue named '"+queueName+"'",e);
		}
		try {
			channel.queueBind(queueName,exchangeName,routingKey);
		} catch (final IOException e) {
			throw new ControllerException("Could not bind curator queue '"+queueName+"' to exchange '"+exchangeName+"' using routing key '"+routingKey+"'",e);
		}
	}

}