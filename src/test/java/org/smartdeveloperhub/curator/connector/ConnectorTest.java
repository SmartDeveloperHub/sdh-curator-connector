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
import java.util.concurrent.Phaser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectorTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(ConnectorTest.class);

	private Phaser phaser=new Phaser(2);
	private ExampleCurator curator;

	@Before
	public void setUp() throws Exception {
		this.curator=new ExampleCurator(phaser);
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
					withConnectorChannel(
						ProtocolFactory.
							newDeliveryChannel().
								withQueueName("connector").
								build()).
					build();
		connector.connect();
		try {
			Acknowledge response=connector.requestEnrichment(URI.create("urn:message"));
			LOGGER.info("Acknowledge: {}",response);
		} finally {
			connector.disconnect();
		}
		phaser.arriveAndAwaitAdvance();
		LOGGER.info("Disconnection processed");
	}

}
