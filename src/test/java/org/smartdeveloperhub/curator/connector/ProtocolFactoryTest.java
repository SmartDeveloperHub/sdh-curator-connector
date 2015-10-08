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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.AgentBuilder;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.DeliveryChannelBuilder;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;

public class ProtocolFactoryTest {

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(ProtocolFactory.class),equalTo(true));
	}

	@Test
	public void testNewBroker() throws Exception {
		Broker build =
			ProtocolFactory.
				newBroker().
					withHost("hostname").
					withPort(12345).
					build();
		System.out.println(build);
	}

	@Test
	public void testMessageBuilder$WithSubmittedBy$nullBuilder() throws Exception {
		try {
			ProtocolFactory.
				newEnrichmentRequest().
					withSubmittedBy((AgentBuilder)null).
					withSubmittedOn(new Date()).
					withMessageId(UUID.randomUUID()).
					withTargetResource("target").
					withReplyTo(ProtocolFactory.newDeliveryChannel()).
					build();
			fail("Should not create a message with no agent");
		} catch (ValidationException e) {
			assertThat(e.getDescription(),equalTo("Agent cannot be null"));
			assertThat(e.getType(),equalTo(FOAF.AGENT_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testRequestBuilder$WithReplyTo$nullBuilder() throws Exception {
		try {
			ProtocolFactory.
				newEnrichmentRequest().
					withSubmittedBy(ProtocolFactory.newAgent().withAgentId(UUID.randomUUID())).
						withSubmittedOn(new Date()).
						withMessageId(UUID.randomUUID()).
						withTargetResource("target").
						withReplyTo((DeliveryChannelBuilder)null).
						build();
			fail("Should not create a request with no delivery channel");
		} catch (ValidationException e) {
			assertThat(e.getDescription(),equalTo("Reply delivery channel cannot be null"));
			assertThat(e.getType(),equalTo(CURATOR.DELIVERY_CHANNEL_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}
}
