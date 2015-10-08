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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newAccepted;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newAgent;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newBinding;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newBroker;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newConstraint;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newDeliveryChannel;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newDisconnect;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newEnrichmentRequest;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newEnrichmentResponse;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newFailure;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newFilter;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newLiteral;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newResource;
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.newVariable;

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
import org.smartdeveloperhub.curator.connector.ProtocolFactory;
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
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
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
		public UnknownMessage fromString(ConversionContext context, String body) throws MessageConversionException {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(ConversionContext context, UnknownMessage message) throws MessageConversionException {
			throw new UnsupportedOperationException();
		}

	}

	private static final String DOAP = "http://usefulinc.com/ns/doap#";
	private static final String SCM = "http://www.smartdeveloperhub.org/vocabulary/scm#";
	private static final String CI = "http://www.smartdeveloperhub.org/vocabulary/ci#";
	private static final String ORG = "http://www.smartdeveloperhub.org/vocabulary/organization#";

	private EnrichmentRequest request(boolean full) {
		final EnrichmentRequestBuilder builder =
			newEnrichmentRequest().
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

	private ConversionContext context() {
		return
			ConversionContext.
				newInstance().
					withBase(URI.create("urn:curator:")).
					withNamespacePrefix(CI,"ci").
					withNamespacePrefix(SCM,"scm").
					withNamespacePrefix(DOAP,"doap").
					withNamespacePrefix(ORG, "ci"). // DUPLICATED PREFIX
					withNamespacePrefix(CURATOR.NAMESPACE, "ci"); // DUPLICATED NAMESPACE
	}

	@Test
	public void testWithConversionContext$neverUpdateNull() throws Exception {
		MessageUtil sut = MessageUtil.newInstance().withConversionContext(null);
		assertThat(Deencapsulation.getField(sut,"context"),notNullValue());
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
		} catch (NoDefinitionFoundException e) {
			assertThat(e.getMessage(),equalTo("No <http://www.smartdeveloperhub.org/vocabulary/curator#Accepted> definition found"));
			assertThat(e.getMissingDefinitionType(),equalTo(CURATOR.ACCEPTED_TYPE));
		} catch(MessageConversionException e) {
			fail("Unexpected failure "+e.getMessage());
		}
	}

	@Test
	public void testFromString$manyDefinitions() {
		try {
			MessageUtil.newInstance().fromString(ResourceUtil.loadResource("messages/multiple_accepted.ttl"),Accepted.class);
			fail("Should not parse input with multiple definitions");
		} catch (TooManyDefinitionsFoundException e) {
			assertThat(e.getMessage(),equalTo("Too many <http://www.smartdeveloperhub.org/vocabulary/curator#Accepted> definitions found (2)"));
			assertThat(e.getDefinitionType(),equalTo(CURATOR.ACCEPTED_TYPE));
			assertThat(e.getDefinitionsFound(),equalTo(2));
		} catch(MessageConversionException e) {
			fail("Unexpected failure "+e.getMessage());
		}
	}

	@Test
	public void testFromString$invalidDefinition() {
		try {
			MessageUtil.newInstance().fromString(ResourceUtil.loadResource("messages/bad_accepted.ttl"),Accepted.class);
			fail("Should not parse input with multiple definitions");
		} catch (InvalidDefinitionFoundException e) {
			assertThat(e.getMessage(),equalTo("Invalid <http://www.smartdeveloperhub.org/vocabulary/curator#Accepted> definition found: Value '-1' is not a valid http://www.w3.org/2001/XMLSchema#unsignedLong: Response number must be greater than 0 (-1)"));
			assertThat(e.getDefinitionType(),equalTo(CURATOR.ACCEPTED_TYPE));
		} catch(MessageConversionException e) {
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
		} catch (MessageConversionException e) {
			assertThat(e.getMessage(),equalTo("Could not serialize message"));
		}
	}

	@Test
	public void testRoundtrip$enrichmentRequest() throws Exception {
		String strRequest = MessageUtil.newInstance().withConversionContext(context()).toString(request(true));
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
