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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newAccepted;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newAgent;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newBroker;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newDeliveryChannel;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newDisconnect;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newEnrichmentRequest;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newEnrichmentResponse;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newFailure;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentRequestBuilder;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentResponseBuilder;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.FailureBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Disconnect;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;
import org.smartdeveloperhub.curator.protocol.Failure;
import org.smartdeveloperhub.curator.protocol.Message;

@RunWith(JMockit.class)
public class MessageUtilTest {

	private EnrichmentRequest request(boolean full) {
		final EnrichmentRequestBuilder builder =
			newEnrichmentRequest().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withTargetResource(URI.create("urn:example"));
		if(full) {
			builder.withReplyTo(
				newDeliveryChannel().
					withBroker(
						newBroker().
							withHost("127.0.0.1").
							withPort(12345).
							withVirtualHost("/virtualHost")).
					withExchangeName("exchange.name").
					withQueueName("queue.name").
					withRoutingKey("routing.key"));
		} else {
			builder.withReplyTo(newDeliveryChannel());
		}
		return builder.build();
	}

	private EnrichmentResponse response(boolean full) {
		final EnrichmentResponseBuilder builder =
			newEnrichmentResponse().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withResponseTo(UUID.randomUUID()).
				withResponseNumber(2).
				withTargetResource(URI.create("urn:example"));
		if(full) {
			builder.
				withAdditionTarget(URI.create("urn:add")).
				withRemovalTarget(URI.create("urn:remove"));
		}
		return builder.build();
	}

	private Accepted accepted() {
		return
			newAccepted().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withResponseTo(UUID.randomUUID()).
				withResponseNumber(4).
				build();
	}

	private Failure failure(boolean full) {
		FailureBuilder builder=
			newFailure().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withResponseTo(UUID.randomUUID()).
				withResponseNumber(4).
				withCode(12).
				withReason("A failure");
		if(full) {
			builder.
				withSubcode(1344).
				withDetail("The detail of the failure");
		}
		return builder.build();
	}

	private Disconnect disconnect() {
		return
			newDisconnect().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				build();
	}

	private static class UnknownMessage implements Message {

		@Override
		public UUID messageId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public DateTime submittedOn() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Agent submittedBy() {
			throw new UnsupportedOperationException();
		}

	}

	private static class UnknownMessageConverter implements MessageConverter<UnknownMessage> {

		private UnknownMessageConverter() {
		}

		@Override
		public UnknownMessage fromString(String body) throws MessageConversionException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(UnknownMessage message) throws MessageConversionException {
			throw new UnsupportedOperationException();
		}

	}

	@Test
	public void testUnsupportedMessageClass() {
		MessageUtil.registerConverter(UnknownMessage.class,null);
		try {
			MessageUtil.newInstance().fromString("body",UnknownMessage.class);
			fail("Should not parse an unsupported class");
		} catch (MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Cannot convert messages of type 'org.smartdeveloperhub.curator.connector.io.MessageUtilTest$UnknownMessage'"));
		}
	}

	@Test
	public void testFailingConverterClass() {
		MessageUtil.registerConverter(UnknownMessage.class,UnknownMessageConverter.class);
		try {
			MessageUtil.newInstance().fromString("body",UnknownMessage.class);
			fail("Should not parse an supported class if the converter cannot be instantiated");
		} catch (MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Could not instantiate converter 'org.smartdeveloperhub.curator.connector.io.MessageUtilTest$UnknownMessageConverter' for message of type 'org.smartdeveloperhub.curator.connector.io.MessageUtilTest$UnknownMessage'"));
		}
	}

	@Test
	public void testFromString$badBody() {
		try {
			MessageUtil.newInstance().fromString("bad body",Accepted.class);
			fail("Should not parse an bad body");
		} catch (MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Could not parse body 'bad body' as Turtle"));
		}
	}

	@Test
	public void testFromString$noDefinition() {
		try {
			MessageUtil.newInstance().fromString("",Accepted.class);
			fail("Should not parse input with no definition");
		} catch (MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("No <http://www.smartdeveloperhub.org/vocabulary/curator#Accepted> definition found"));
		}
	}

	@Test
	public void testFromString$manyDefinitions() {
		try {
			MessageUtil.newInstance().fromString(ResourceUtil.loadResource("messages/multiple_accepted.ttl"),Accepted.class);
			fail("Should not parse input with multiple definitions");
		} catch (MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Too many <http://www.smartdeveloperhub.org/vocabulary/curator#Accepted> definitions found (2)"));
		}
	}

	@Test
	public void testToString$failure() throws Exception {
		new MockUp<StringWriter>() {
			@Mock
			public void close() throws IOException {
				throw new IOException("fail");
			}
		};
		try {
			MessageUtil.newInstance().toString(request(true));
			fail("Should not produce result when serialization failure occurs");
		} catch (MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Could not serialize message"));
		}
	}

	@Test
	public void testRoundtrip$enrichmentRequest() throws Exception {
		String strRequest = MessageUtil.newInstance().toString(request(true));
		System.out.println(strRequest);
		System.out.println(MessageUtil.newInstance().fromString(strRequest, EnrichmentRequest.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$enrichmentRequest$partialDeliveryChannel() throws Exception {
		String strRequest = MessageUtil.newInstance().toString(request(false));
		System.out.println(strRequest);
		System.out.println(MessageUtil.newInstance().fromString(strRequest, EnrichmentRequest.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$enrichmentResponse() throws Exception {
		String strResponse = MessageUtil.newInstance().toString(response(true));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, EnrichmentResponse.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$enrichmentResponse$partial() throws Exception {
		String strResponse = MessageUtil.newInstance().toString(response(false));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, EnrichmentResponse.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$accepted() throws Exception {
		String strResponse = MessageUtil.newInstance().toString(accepted());
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, Accepted.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$failure() throws Exception {
		String strResponse = MessageUtil.newInstance().toString(failure(true));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, Failure.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$failure$min() throws Exception {
		String strResponse = MessageUtil.newInstance().toString(failure(false));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, Failure.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$disconnect() throws Exception {
		String strResponse = MessageUtil.newInstance().toString(disconnect());
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, Disconnect.class));
		System.out.println();
	}

}
