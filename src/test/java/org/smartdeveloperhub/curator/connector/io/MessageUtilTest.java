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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.3.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newAcceptedMessage;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newAgent;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newBinding;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newBroker;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newConstraint;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newDeliveryChannel;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newDisconnectMessage;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newEnrichmentRequestMessage;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newEnrichmentResponseMessage;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newFailureMessage;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newFilter;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newLiteral;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newResource;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newVariable;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.EnrichmentRequestMessageBuilder;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.EnrichmentResponseMessageBuilder;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.FailureMessageBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

@RunWith(JMockit.class)
public class MessageUtilTest {

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
		public UnknownMessage fromString(final ConversionContext context, final String body) throws MessageConversionException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(final ConversionContext context, final UnknownMessage message) throws MessageConversionException {
			throw new UnsupportedOperationException();
		}

	}

	private static final String DOAP = "http://usefulinc.com/ns/doap#";
	private static final String SCM = "http://www.smartdeveloperhub.org/vocabulary/scm#";
	private static final String CI = "http://www.smartdeveloperhub.org/vocabulary/ci#";
	private static final String ORG = "http://www.smartdeveloperhub.org/vocabulary/organization#";

	private EnrichmentRequestMessage request(final boolean full) {
		final EnrichmentRequestMessageBuilder builder =
			newEnrichmentRequestMessage().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withTargetResource(URI.create("execution")).
				withFilter(
					newFilter().
						withProperty(CI+"forBranch").
						withVariable(newVariable("branch"))).
				withFilter(
					newFilter().
						withProperty(CI+"forCommit").
						withVariable(newVariable("commit"))).
				withConstraint(
					newConstraint().
						withTarget(newResource("project")).
						withBinding(
							newBinding().
								withProperty(RDF.TYPE).
								withValue(newResource(ORG+"Project"))).
						withBinding(
							newBinding().
								withProperty(ORG+"hasRepository").
								withValue(newVariable("repository")))
						).
				withConstraint(
					newConstraint().
						withTarget(newVariable("repository")).
						withBinding(
							newBinding().
								withProperty(RDF.TYPE).
								withValue(newResource(SCM+"Repository"))).
						withBinding(
							newBinding().
								withProperty(SCM+"hasBranch").
								withValue(newVariable("branch"))).
						withBinding(
							newBinding().
								withProperty(SCM+"location").
								withValue(
									newLiteral().
										withLexicalForm("git://github.com/ldp4j/ldp4j.git").
											withDatatype(XSD.ANY_URI_TYPE)))
						).
				withConstraint(
					newConstraint().
						withTarget(newVariable("branch")).
						withBinding(
							newBinding().
								withProperty(RDF.TYPE).
								withValue(newResource(SCM+"Branch"))).
						withBinding(
							newBinding().
								withProperty(SCM+"hasCommit").
								withValue(newVariable("commit"))).
						withBinding(
							newBinding().
								withProperty(DOAP+"name").
								withValue(
									newLiteral().
										withLexicalForm("develop").
										withDatatype(XSD.STRING_TYPE)))
					).
				withConstraint(
					newConstraint().
						withTarget(newVariable("commit")).
						withBinding(
							newBinding().
								withProperty(RDF.TYPE).
								withValue(newResource(SCM+"Commit"))).
						withBinding(
							newBinding().
								withProperty(SCM+"commitId").
								withValue(
									newLiteral().
										withLexicalForm("f1efd1d8d8ceebef1d85eb66c69a44b0d713ed44").
										withDatatype(XSD.STRING_TYPE))));
		if(full) {
			builder.withReplyTo(
				newDeliveryChannel().
					withBroker(
						newBroker().
							withHost("127.0.0.1").
							withPort(12345).
							withVirtualHost("/virtualHost")).
					withExchangeName("exchange.name").
					withRoutingKey("routing.key"));
		} else {
			builder.withReplyTo(newDeliveryChannel().withRoutingKey("routing.key"));
		}
		return builder.build();
	}

	private EnrichmentResponseMessage response(final boolean full) {
		final EnrichmentResponseMessageBuilder builder =
			newEnrichmentResponseMessage().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withResponseTo(UUID.randomUUID()).
				withResponseNumber(2).
				withTargetResource(URI.create("execution"));
		if(full) {
			builder.
				withAddition(
					ProtocolFactory.
						newBinding().
							withProperty(CI+"forBranch").
							withValue(ProtocolFactory.newResource("newBranch"))).
				withAddition(
					ProtocolFactory.
						newBinding().
							withProperty(CI+"forCommit").
							withValue(ProtocolFactory.newResource("newCommit"))).
				withRemoval(
					ProtocolFactory.
						newBinding().
							withProperty(CI+"forBranch").
							withValue(ProtocolFactory.newResource("oldBranch"))).
				withRemoval(
					ProtocolFactory.
						newBinding().
							withProperty(CI+"forCommit").
							withValue(ProtocolFactory.newResource("oldCommit")));
		}
		return builder.build();
	}

	private AcceptedMessage accepted() {
		return
			newAcceptedMessage().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withResponseTo(UUID.randomUUID()).
				withResponseNumber(4).
				build();
	}

	private FailureMessage failure(final boolean full) {
		final FailureMessageBuilder builder=
			newFailureMessage().
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

	private DisconnectMessage disconnect() {
		return
			newDisconnectMessage().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				build();
	}

	private ConversionContext context() {
		return
			ConversionContext.
				newInstance().
					withBase(URI.create("urn:curator:")).
					withNamespacePrefix(CI,"ci").
					withNamespacePrefix(SCM,"scm").
					withNamespacePrefix(DOAP,"doap").
					withNamespacePrefix(ORG, "ci"). // DUPLICATED PREFIX
					withNamespacePrefix(STOA.NAMESPACE, "ci"); // DUPLICATED NAMESPACE
	}

	@Test
	public void testWithConversionContext$neverUpdateNull() throws Exception {
		final MessageUtil sut = MessageUtil.newInstance().withConversionContext(null);
		assertThat(Deencapsulation.getField(sut,"context"),notNullValue());
	}

	@Test
	public void testUnsupportedMessageClass() {
		MessageUtil.registerConverter(UnknownMessage.class,null);
		try {
			MessageUtil.newInstance().fromString("body",UnknownMessage.class);
			fail("Should not parse an unsupported class");
		} catch (final MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Cannot convert messages of type 'org.smartdeveloperhub.curator.connector.io.MessageUtilTest$UnknownMessage'"));
		}
	}

	@Test
	public void testFailingConverterClass() {
		MessageUtil.registerConverter(UnknownMessage.class,UnknownMessageConverter.class);
		try {
			MessageUtil.newInstance().fromString("body",UnknownMessage.class);
			fail("Should not parse an supported class if the converter cannot be instantiated");
		} catch (final MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Could not instantiate converter 'org.smartdeveloperhub.curator.connector.io.MessageUtilTest$UnknownMessageConverter' for message of type 'org.smartdeveloperhub.curator.connector.io.MessageUtilTest$UnknownMessage'"));
		}
	}

	@Test
	public void testFromString$badBody() {
		try {
			MessageUtil.newInstance().fromString("bad body",AcceptedMessage.class);
			fail("Should not parse an bad body");
		} catch (final MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Could not parse body 'bad body' as Turtle"));
		}
	}

	@Test
	public void testFromString$noDefinition() {
		try {
			MessageUtil.newInstance().fromString("",AcceptedMessage.class);
			fail("Should not parse input with no definition");
		} catch (final NoDefinitionFoundException e) {
			assertThat(e.getMessage(),equalTo("No <"+STOA.ACCEPTED_TYPE+"> definition found"));
			assertThat(e.getMissingDefinitionType(),equalTo(STOA.ACCEPTED_TYPE));
		} catch(final MessageConversionException e) {
			fail("Unexpected failure "+e.getMessage());
		}
	}

	@Test
	public void testFromString$manyDefinitions() {
		try {
			MessageUtil.newInstance().fromString(ResourceUtil.loadResource("messages/multiple_accepted.ttl"),AcceptedMessage.class);
			fail("Should not parse input with multiple definitions");
		} catch (final TooManyDefinitionsFoundException e) {
			assertThat(e.getMessage(),equalTo("Too many <"+STOA.ACCEPTED_TYPE+"> definitions found (2)"));
			assertThat(e.getDefinitionType(),equalTo(STOA.ACCEPTED_TYPE));
			assertThat(e.getDefinitionsFound(),equalTo(2));
		} catch(final MessageConversionException e) {
			fail("Unexpected failure "+e.getMessage());
		}
	}

	@Test
	public void testFromString$invalidDefinition() {
		try {
			MessageUtil.newInstance().fromString(ResourceUtil.loadResource("messages/bad_accepted.ttl"),AcceptedMessage.class);
			fail("Should not parse input with multiple definitions");
		} catch (final InvalidDefinitionFoundException e) {
			assertThat(e.getMessage(),equalTo("Invalid <"+STOA.ACCEPTED_TYPE+"> definition found: Value '-1' is not a valid http://www.w3.org/2001/XMLSchema#unsignedLong: Response number must be greater than 0 (-1)"));
			assertThat(e.getDefinitionType(),equalTo(STOA.ACCEPTED_TYPE));
		} catch(final MessageConversionException e) {
			fail("Unexpected failure "+e.getMessage());
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
		} catch (final MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Could not serialize message"));
		}
	}

	@Test
	public void testRoundtrip$enrichmentRequest() throws Exception {
		final String strRequest = MessageUtil.newInstance().withConversionContext(context()).toString(request(true));
		System.out.println(strRequest);
		System.out.println(MessageUtil.newInstance().fromString(strRequest, EnrichmentRequestMessage.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$enrichmentRequest$partialDeliveryChannel() throws Exception {
		final String strRequest = MessageUtil.newInstance().toString(request(false));
		System.out.println(strRequest);
		System.out.println(MessageUtil.newInstance().fromString(strRequest, EnrichmentRequestMessage.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$enrichmentResponse() throws Exception {
		final String strResponse = MessageUtil.newInstance().toString(response(true));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, EnrichmentResponseMessage.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$enrichmentResponse$partial() throws Exception {
		final String strResponse = MessageUtil.newInstance().toString(response(false));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, EnrichmentResponseMessage.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$accepted() throws Exception {
		final String strResponse = MessageUtil.newInstance().toString(accepted());
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, AcceptedMessage.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$failure() throws Exception {
		final String strResponse = MessageUtil.newInstance().toString(failure(true));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, FailureMessage.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$failure$min() throws Exception {
		final String strResponse = MessageUtil.newInstance().toString(failure(false));
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, FailureMessage.class));
		System.out.println();
	}

	@Test
	public void testRoundtrip$disconnect() throws Exception {
		final String strResponse = MessageUtil.newInstance().toString(disconnect());
		System.out.println(strResponse);
		System.out.println(MessageUtil.newInstance().fromString(strResponse, DisconnectMessage.class));
		System.out.println();
	}

}
