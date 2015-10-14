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

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.Curator;
import org.smartdeveloperhub.curator.Notifier;
import org.smartdeveloperhub.curator.RandomMessageIdentifierFactory;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;

public class ConnectorTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(ConnectorTest.class);

	private DeliveryChannel deliveryChannel =
		ProtocolFactory.
			newDeliveryChannel().
				withQueueName("connector").
				build();

	private Phaser disconnected=new Phaser(2);
	private Phaser answered=new Phaser(3);

	private Curator curator;

	private RandomMessageIdentifierFactory factory;

	private class CustomNotifier extends Notifier {

		@Override
		public void onDisconnect(DisconnectMessage response) {
			disconnected.arrive();
		}

		@Override
		public void onEnrichmentResponse(EnrichmentResponseMessage response) {
			answered.arrive();
		}

	}

	@Before
	public void setUp() throws Exception {
		this.factory=RandomMessageIdentifierFactory.create(2);
		this.curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		this.curator.accept(
			this.factory.generated(0),
			EnrichmentResult.
				newInstance().
					withTargetResource(URI.create("urn:example")).
					withAddition(
						URI.create("urn:property"),
						ProtocolFactory.newResource("urn:result")));
		this.curator.connect();
	}

	@After
	public void tearDown() throws Exception {
		this.curator.disconnect();
	}

	@Test
	public void testRequestEnrichment() throws Exception {
		Connector connector =
			Connector.
				builder().
					withConnectorChannel(this.deliveryChannel).
					withMessageIdentifierFactory(this.factory).
					build();
		connector.connect();
		try {
			Future<Acknowledge> response=
				connector.
					requestEnrichment(
						EnrichmentSpecification.
							newInstance().
								withTargetResource(URI.create("urn:message")),
						new EnrichmentResultHandler() {
							@Override
							public void onResult(EnrichmentResult response) {
								LOGGER.debug("Received: {}",response);
								ConnectorTest.this.answered.arrive();
							}
						}
					);
			LOGGER.info("Acknowledge: {}",response.get());
			this.answered.arriveAndAwaitAdvance();
		} finally {
			connector.disconnect();
		}
		this.disconnected.arriveAndAwaitAdvance();
		LOGGER.info("Disconnection processed");
	}

}
