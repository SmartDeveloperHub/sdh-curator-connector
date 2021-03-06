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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;

public class BrokerControllerUsageTest {

	private static final String QUEUE_NAME = "test";
	private static final String ROUTING_KEY = "key";
	private static final String EXCHANGE_NAME = "exchange";

	@Test
	public void failOnMessagePublicationFailure() throws Exception {
		final BrokerController producer = createController("producer");
		final BrokerController consumer = createController("consumer");
		producer.connect();
		consumer.connect();
		try {
			producer.declareExchange(EXCHANGE_NAME);
			producer.
				publishMessage(
					ProtocolFactory.
						newDeliveryChannel().
							withExchangeName(EXCHANGE_NAME).
							withRoutingKey(ROUTING_KEY).
							build(),
					"message1");
			TimeUnit.MILLISECONDS.sleep(500);
			consumer.declareQueue(QUEUE_NAME);
			consumer.bindQueue(EXCHANGE_NAME, QUEUE_NAME, ROUTING_KEY);
			consumer.registerConsumer(new MessageHandler() {
				@Override
				public void handlePayload(final String payload) {
					System.out.println("Received "+payload);
				}}, QUEUE_NAME);
			producer.
				publishMessage(
					ProtocolFactory.
						newDeliveryChannel().
							withExchangeName(EXCHANGE_NAME).
							withRoutingKey(ROUTING_KEY).
							build(),
					"message2");
			TimeUnit.MILLISECONDS.sleep(500);
		} finally {
			consumer.disconnect();
			producer.disconnect();
		}
	}

	private BrokerController createController(final String name) {
		return
			new BrokerController(
				ProtocolFactory.
					newBroker().
						build(),
				name,
				ConversionContext.newInstance());
	}

}
