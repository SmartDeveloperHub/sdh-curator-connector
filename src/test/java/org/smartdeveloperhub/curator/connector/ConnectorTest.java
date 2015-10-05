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
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;

public class ConnectorTest {


	private CuratorController controller;

	@Before
	public void setUp() throws Exception {
		this.controller = new CuratorController(CuratorConfiguration.newInstance(),"client");
		this.controller.connect();
		this.controller.handleRequests(
			new MessageHandler() {
				@Override
				public void handleCancel() {
					System.out.println("Received cancel request");
				}
				@Override
				public void handlePayload(String payload) {
					System.out.println("Received request: "+payload);
					try {
						EnrichmentRequest request=
							MessageUtil.
								newInstance().
									fromString(payload, EnrichmentRequest.class);
						Accepted response = ProtocolFactory.
							newAccepted().
								withMessageId(UUID.randomUUID()).
								withSubmittedOn(new Date()).
								withSubmittedBy(
									ProtocolFactory.
										newAgent().
											withAgentId(UUID.randomUUID())).
								withResponseTo(request.messageId()).
								withResponseNumber(1).
								build();
						System.out.println("Sending response: "+response);
						controller.publishResponse(response);
					} catch (IOException | MessageConversionException e) {
						e.printStackTrace();
					}
				}
			}
		);
	}

	@After
	public void tearDown() throws Exception {
		this.controller.disconnect();
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
			connector.requestEnrichment(URI.create("urn:message"));
			TimeUnit.SECONDS.sleep(3);
		} finally {
			connector.disconnect();
		}
	}

}
