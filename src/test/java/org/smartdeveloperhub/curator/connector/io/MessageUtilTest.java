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
package org.smartdeveloperhub.curator.connector.io;

import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.*;

public class MessageUtilTest {

	@Test
	public void testToString$enrichmentRequest() throws Exception {
		System.out.println(
			MessageUtil.
				newInstance().
					toString(
						newEnrichmentRequest().
							withMessageId(UUID.randomUUID()).
							withSubmittedOn(new Date()).
							withSubmittedBy(
								newAgent().
									withAgentId(UUID.randomUUID())).
							withReplyTo(
								newDeliveryChannel().
									withBroker(
										newBroker().
											withHost("127.0.0.1").
											withPort(12345).
											withVirtualHost("/virtualHost")).
									withExchangeName("exchange.name").
									withQueueName("queue.name").
									withRoutingKey("routing.key")).
							withTargetResource(URI.create("urn:example")).
							build()
					)
				);
	}

	@Test
	public void testToString$enrichmentResponse() throws Exception {
		System.out.println(
			MessageUtil.
				newInstance().
					toString(
						newEnrichmentResponse().
							withMessageId(UUID.randomUUID()).
							withSubmittedOn(new Date()).
							withSubmittedBy(
								newAgent().
									withAgentId(UUID.randomUUID())).
							withResponseTo(UUID.randomUUID()).
							withResponseNumber(2).
							withTargetResource(URI.create("urn:example")).
							withAdditionTarget(URI.create("urn:add")).
							withRemovalTarget(URI.create("urn:remove")).
							build()
					)
				);
	}
}
