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
import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.Channel;

final class CuratorController {

	private static final String CURATOR_REQUEST_ROUTING_KEY = "curator.request";
	private static final String CURATOR_RESPONSE_ROUTING_KEY = "curator.response";

	private static final Logger LOGGER=LoggerFactory.getLogger(CuratorController.class);

	private final CuratorConfiguration configuration;
	private final BrokerController brokerController;

	CuratorController(CuratorConfiguration configuration, String name) {
		this.configuration=configuration;
		this.brokerController=new BrokerController(configuration.broker(),name);
	}

	BrokerController brokerController() {
		return this.brokerController;
	}

	CuratorConfiguration curatorConfiguration() {
		return this.configuration;
	}

	void connect() throws ControllerException {
		this.brokerController.connect();
		configureBroker();
	}

	void publishRequest(Message message) throws IOException {
		publishMessage(message, CURATOR_REQUEST_ROUTING_KEY);
	}

	void publishResponse(Message message) throws IOException {
		publishMessage(message, CURATOR_RESPONSE_ROUTING_KEY);
	}

	void handleResponses(final MessageHandler handler) throws IOException {
		Channel channel = this.brokerController.channel();
		channel.basicConsume(
			this.curatorConfiguration().responseQueueName(),
			true,
			new MessageHandlerConsumer(channel, handler)
		);
	}

	void handleRequests(final MessageHandler handler) throws IOException {
		Channel channel = this.brokerController.channel();
		channel.basicConsume(
			this.curatorConfiguration().requestQueueName(),
			true,
			new MessageHandlerConsumer(channel, handler)
		);
	}

	void disconnect() {
		this.brokerController.disconnect();
	}

	private void configureBroker() throws ControllerException {
		Channel channel = this.brokerController.channel();
		prepareExchange(channel, this.curatorConfiguration().exchangeName());
		prepareQueue(channel, this.curatorConfiguration().exchangeName(), this.curatorConfiguration().requestQueueName(), CURATOR_REQUEST_ROUTING_KEY);
		prepareQueue(channel, this.curatorConfiguration().exchangeName(), this.curatorConfiguration().responseQueueName(), CURATOR_RESPONSE_ROUTING_KEY);
	}

	private void prepareExchange(Channel channel, String exchangeName) throws ControllerException {
		try {
			channel.exchangeDeclare(exchangeName,"direct");
		} catch (IOException e) {
			throw new ControllerException("Could not create curator exchange named '"+exchangeName+"'",e);
		}
	}

	private void prepareQueue(Channel channel, String exchangeName, String queueName, String routingKey) throws ControllerException {
		try {
			channel.queueDeclare(queueName,true,false,false,null);
		} catch (IOException e) {
			throw new ControllerException("Could not create curator queue named '"+queueName+"'",e);
		}
		try {
			channel.queueBind(queueName,exchangeName,routingKey);
		} catch (IOException e) {
			throw new ControllerException("Could not bind curator queue '"+queueName+"' to exchange '"+exchangeName+"' using routing key '"+routingKey+"'",e);
		}
	}

	private void publishMessage(Message message, String routingKey) throws IOException {
		LOGGER.debug("Publishing message {} to routing key {}...",message,routingKey);
		try {
			this.brokerController.
				channel().
					basicPublish(
						this.curatorConfiguration().exchangeName(),
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

}