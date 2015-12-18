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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.protocol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.UUID;

import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.AgentBuilder;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.DeliveryChannelBuilder;
import org.smartdeveloperhub.curator.connector.util.Builder;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDFS;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

@RunWith(JMockit.class)
public class ProtocolFactoryTest {

	@Mocked private UUID messageId;
	@Mocked private Agent agent;
	@Mocked private Constraint constraint;
	@Mocked private DeliveryChannel deliveryChannel;
	@Mocked private Filter filter;
	@Mocked private Binding addition;
	@Mocked private Binding removal;
	@Mocked private Date date;

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(ProtocolFactory.class),equalTo(true));
	}

	@Test
	public void testMessageBuilder$WithSubmittedBy$nullBuilder() throws Exception {
		try {
			ProtocolFactory.
				newEnrichmentRequestMessage().
					withSubmittedBy((AgentBuilder)null).
					withSubmittedOn(new Date()).
					withMessageId(UUID.randomUUID()).
					withTargetResource("target").
					withReplyTo(ProtocolFactory.newDeliveryChannel().withRoutingKey("routingKey")).
					withFilter(this.filter).
					withConstraint(this.constraint).
					build();
			fail("Should not create a message with no agent");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("Agent cannot be null"));
			assertThat(e.getType(),equalTo(FOAF.AGENT_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testRequestMessageBuilder$WithReplyTo$nullBuilder() throws Exception {
		try {
			ProtocolFactory.
				newEnrichmentRequestMessage().
					withSubmittedBy(ProtocolFactory.newAgent().withAgentId(UUID.randomUUID())).
					withSubmittedOn(new Date()).
					withMessageId(UUID.randomUUID()).
					withTargetResource("target").
					withReplyTo((DeliveryChannelBuilder)null).
					withFilter(this.filter).
					withConstraint(this.constraint).
					build();
			fail("Should not create a request with no delivery channel");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("No enrichment request reply delivery channel specified"));
			assertThat(e.getType(),equalTo(CURATOR.DELIVERY_CHANNEL_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testBindingBuilder$withValue$nullBuilder() {
		try {
			ProtocolFactory.
				newBinding().
					withProperty("property").
					withValue((Builder<Value>)null).
					build();
			fail("Should not create a binding with no value");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("Binding value cannot be null"));
			assertThat(e.getType(),equalTo(RDFS.RESOURCE_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testConstraintBuilder$withBinding$nullBuilder() {
		try {
			ProtocolFactory.
				newConstraint().
					withTarget(ProtocolFactory.newResource("target")).
					withBinding((Builder<Binding>)null).
					build();
			fail("Should not create a constraint without bindings");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("No constraint bindings specified"));
			assertThat(e.getType(),equalTo(RDFS.RESOURCE_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testConstraintBuilder$withBinding$nullBinding() {
		try {
			ProtocolFactory.
				newConstraint().
					withTarget(ProtocolFactory.newResource("target")).
					withBinding((Binding)null).
					build();
			fail("Should not create a constraint without bindings");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("No constraint bindings specified"));
			assertThat(e.getType(),equalTo(RDFS.RESOURCE_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testFailureMessageBuilder$withSubcode$null() {
		final FailureMessage message=
			ProtocolFactory.
				newFailureMessage().
					withMessageId(UUID.randomUUID()).
					withSubmittedOn(new Date()).
					withSubmittedBy(ProtocolFactory.newAgent().withAgentId(UUID.randomUUID())).
					withResponseTo(UUID.randomUUID()).
					withResponseNumber(1).
					withCode(0).
					withReason("reason").
					withSubcode((Long)null).
					build();
		assertThat(message.subcode().isPresent(),equalTo(false));
	}

	@Test
	public void testFailureMessageBuilder$withSubcode$Long() {
		final FailureMessage message=
			ProtocolFactory.
				newFailureMessage().
					withMessageId(UUID.randomUUID()).
					withSubmittedOn(new Date()).
					withSubmittedBy(ProtocolFactory.newAgent().withAgentId(UUID.randomUUID())).
					withResponseTo(UUID.randomUUID()).
					withResponseNumber(1).
					withCode(0).
					withReason("reason").
					withSubcode(new Long(1L)).
					build();
		assertThat(message.subcode().get(),equalTo(1L));
	}

	@Test
	public void testEnrichmentRequestMessageBuilder$withFilter$nullBuilder() {
		try {
			ProtocolFactory.
				newEnrichmentRequestMessage().
					withMessageId(this.messageId).
					withSubmittedOn(new Date()).
					withSubmittedBy(this.agent).
					withReplyTo(this.deliveryChannel).
					withTargetResource("target").
					withFilter((Builder<Filter>)null).
					withConstraint(this.constraint).
					build();
			fail("Should not create an enrichment request message without filters");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("No enrichment request filters specified"));
			assertThat(e.getType(),equalTo(RDFS.RESOURCE_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testEnrichmentRequestMessageBuilder$withConstraint$nullBuilder() {
		try {
			ProtocolFactory.
				newEnrichmentRequestMessage().
					withMessageId(this.messageId).
					withSubmittedOn(new Date()).
					withSubmittedBy(this.agent).
					withReplyTo(this.deliveryChannel).
					withTargetResource("target").
					withFilter(this.filter).
					withConstraint((Builder<Constraint>)null).
					build();
			fail("Should not create an enrichment request message without constraints");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("No enrichment request constraints specified"));
			assertThat(e.getType(),equalTo(RDFS.RESOURCE_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testEnrichmentRequestMessageBuilder$withFilterAndConstraint() {
		final EnrichmentRequestMessage request =
			ProtocolFactory.
				newEnrichmentRequestMessage().
					withMessageId(this.messageId).
					withSubmittedOn(this.date).
					withSubmittedBy(this.agent).
					withReplyTo(this.deliveryChannel).
					withTargetResource("target").
					withFilter((Filter)null).
					withFilter(this.filter).
					withConstraint((Constraint)null).
					withConstraint(this.constraint).
					build();
		assertThat(request.filters(),contains(this.filter));
		assertThat(request.constraints(),contains(this.constraint));
	}

	@Test
	public void testEnrichmentResponseMessageBuilder$withoutAdditions() throws Exception {
		final EnrichmentResponseMessage message=
			ProtocolFactory.
				newEnrichmentResponseMessage().
					withMessageId(this.messageId).
					withSubmittedOn(new Date()).
					withSubmittedBy(this.agent).
					withTargetResource("target").
					withResponseTo(this.messageId).
					withResponseNumber(1L).
					withAddition((Binding)null).
					withAddition((Builder<Binding>)null).
					withRemoval(this.removal).
					build();
		assertThat(message.additions(),hasSize(0));
		assertThat(message.removals(),contains(this.removal));
	}

	@Test
	public void testEnrichmentResponseMessageBuilder$withoutRemovals() throws Exception {
		final EnrichmentResponseMessage message=
			ProtocolFactory.
				newEnrichmentResponseMessage().
					withMessageId(this.messageId).
					withSubmittedOn(new Date()).
					withSubmittedBy(this.agent).
					withTargetResource("target").
					withResponseTo(this.messageId).
					withResponseNumber(1L).
					withRemoval((Binding)null).
					withRemoval((Builder<Binding>)null).
					withAddition(this.addition).
					build();
		assertThat(message.removals(),hasSize(0));
		assertThat(message.additions(),contains(this.addition));
	}

	@Test
	public void testLiteralBuilder$withLexicalForm() {
		final String lexicalForm = "string";
		final Literal literal =
			ProtocolFactory.
				newLiteral().
					withLexicalForm(lexicalForm).
					build();
		assertThat(literal.lexicalForm(),equalTo(lexicalForm));
		assertThat(literal.datatype(),nullValue());
		assertThat(literal.language(),nullValue());
	}

	@Test
	public void testLiteralBuilder$nullLexicalForm() {
		try {
			ProtocolFactory.
				newLiteral().
					withLexicalForm(null).
					build();
			fail("Should not create a literal without a lexical form");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("Lexical form cannot be null"));
			assertThat(e.getType(),equalTo(XSD.STRING_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

	@Test
	public void testDeliveryChannelBuilder$nullRoutingKey() {
		try {
			ProtocolFactory.
				newDeliveryChannel().
					withRoutingKey(null).
					build();
			fail("Should not create a delivery channel without routing key");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("Routing key cannot be null"));
			assertThat(e.getType(),equalTo(AMQP.ROUTING_KEY_TYPE));
			assertThat(e.getValue(),nullValue());
		}
	}

}
