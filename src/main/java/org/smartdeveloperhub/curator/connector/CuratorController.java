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
import java.util.concurrent.TimeoutException;

import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.Channel;

final class CuratorController {

	private final CuratorConfiguration configuration;
	private final BrokerController brokerController;

	CuratorController(CuratorConfiguration configuration) {
		this.configuration=configuration;
		this.brokerController=new BrokerController(configuration.broker());
	}

	BrokerController brokerController() {
		return this.brokerController;
	}

	private void configureBroker() throws IOException {
		Channel channel = this.brokerController.channel();
		channel.exchangeDeclare(this.configuration.exchangeName(),"direct");

		channel.queueDeclare(this.configuration.requestQueueName(),true,false,false,null);
		channel.queueBind(this.configuration.requestQueueName(),this.configuration.exchangeName(),"");

		channel.queueDeclare(this.configuration.responseQueueName(),true,false,false,null);
		channel.queueBind(this.configuration.responseQueueName(),this.configuration.exchangeName(),"");
	}

	void connect() throws IOException, TimeoutException {
		this.brokerController.connect();
		configureBroker();
	}

	void publish(Message message) throws IOException {
		this.brokerController.
			channel().
				basicPublish(
					this.configuration.exchangeName(),
					"",
					null,
					message.toString().getBytes());

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

}