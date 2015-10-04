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

import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.Channel;

final class CuratorController {

	private static final String CURATOR_ROUTING_KEY = "";

	private final CuratorConfiguration configuration;
	private final BrokerController brokerController;

	CuratorController(CuratorConfiguration configuration) {
		this.configuration=configuration;
		this.brokerController=new BrokerController(configuration.broker());
	}

	BrokerController brokerController() {
		return this.brokerController;
	}

	void connect() throws ControllerException {
		this.brokerController.connect();
		configureBroker();
	}

	void publish(Message message) throws IOException {
		try {
			this.brokerController.
				channel().
					basicPublish(
						this.configuration.exchangeName(),
						CURATOR_ROUTING_KEY,
						null,
						MessageUtil.
							newInstance().
								toString(message).
									getBytes());
		} catch (MessageConversionException e) {
			throw new IOException("Could not serialize message",e);
		}
	}

	void handleResponses(final MessageHandler handler) throws IOException {
		Channel channel = this.brokerController.channel();
		channel.basicConsume(
			this.configuration.responseQueueName(),
			true,
			new MessageHandlerConsumer(channel, handler)
		);
	}

	void handleRequests(final MessageHandler handler) throws IOException {
		Channel channel = this.brokerController.channel();
		channel.basicConsume(
			this.configuration.requestQueueName(),
			true,
			new MessageHandlerConsumer(channel, handler)
		);
	}

	void disconnect() {
		this.brokerController.disconnect();
	}

	private void configureBroker() throws ControllerException {
		Channel channel = this.brokerController.channel();
		prepareExchange(channel, this.configuration.exchangeName());
		prepareQueue(channel, this.configuration.exchangeName(), this.configuration.requestQueueName());
		prepareQueue(channel, this.configuration.exchangeName(), this.configuration.responseQueueName());
	}

	private void prepareExchange(Channel channel, String exchangeName) throws ControllerException {
		try {
			channel.exchangeDeclare(exchangeName,"direct");
		} catch (IOException e) {
			throw new ControllerException("Could not create curator exchange named '"+exchangeName+"'",e);
		}
	}

	private void prepareQueue(Channel channel, String exchangeName, String queueName) throws ControllerException {
		try {
			channel.queueDeclare(queueName,true,false,false,null);
		} catch (IOException e) {
			throw new ControllerException("Could not create curator queue named '"+queueName+"'",e);
		}
		try {
			channel.queueBind(queueName,exchangeName,CURATOR_ROUTING_KEY);
		} catch (IOException e) {
			throw new ControllerException("Could not bind curator queue '"+queueName+"' to exchange '"+exchangeName+"'",e);
		}
	}

}