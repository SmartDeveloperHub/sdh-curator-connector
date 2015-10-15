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
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newAcceptedMessage;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newAgent;
import static org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.newFailureMessage;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.FailureMessageBuilder;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;

@RunWith(JMockit.class)
public class EnrichmentTest {

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

	private FailureMessage failure() {
		FailureMessageBuilder builder=
			newFailureMessage().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					newAgent().
						withAgentId(UUID.randomUUID())).
				withResponseTo(UUID.randomUUID()).
				withResponseNumber(4).
				withCode(12).
				withReason("A failure").
				withSubcode(1344).
				withDetail("The detail of the failure");
		return builder.build();
	}

	@Test
	public void testOf$invalidMessage(@Mocked DisconnectMessage message) {
		try {
			Enrichment.of(message);
			fail("Should not create an enrichment from an invalid message");
		} catch (RuntimeConnectorException e) {
			assertThat(e.getMessage(),equalTo("Unexpected message "+message));
		}

	}

	@Test
	public void testIsAccepted$aborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		assertThat(sut.isAccepted(),equalTo(false));
	}

	@Test
	public void testIsAccepted$accepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		assertThat(sut.isAccepted(),equalTo(true));
	}

	@Test
	public void testIsAccepted$failed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		assertThat(sut.isAccepted(),equalTo(false));
	}

	@Test
	public void testIsAccepted$cancelledFromAborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		sut.cancel();
		assertThat(sut.isAccepted(),equalTo(false));
	}

	@Test
	public void testIsAccepted$cancelledFromAccepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		sut.cancel();
		assertThat(sut.isAccepted(),equalTo(true));
	}

	@Test
	public void testIsAccepted$cancelledFromFailed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		sut.cancel();
		assertThat(sut.isAccepted(),equalTo(false));
	}

	@Test
	public void testIsFailed$aborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		assertThat(sut.isFailed(),equalTo(false));
	}

	@Test
	public void testIsFailed$accepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		assertThat(sut.isFailed(),equalTo(false));
	}

	@Test
	public void testIsFailed$failed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		assertThat(sut.isFailed(),equalTo(true));
	}

	@Test
	public void testIsFailed$cancelledFromAborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		sut.cancel();
		assertThat(sut.isFailed(),equalTo(false));
	}

	@Test
	public void testIsFailed$cancelledFromAccepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		sut.cancel();
		assertThat(sut.isFailed(),equalTo(false));
	}

	@Test
	public void testIsFailed$cancelledFromFailed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		sut.cancel();
		assertThat(sut.isFailed(),equalTo(true));
	}

	@Test
	public void testIsAborted$aborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		assertThat(sut.isAborted(),equalTo(true));
	}

	@Test
	public void testIsAborted$accepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		assertThat(sut.isAborted(),equalTo(false));
	}

	@Test
	public void testIsAborted$failed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		assertThat(sut.isAborted(),equalTo(false));
	}

	@Test
	public void testIsAborted$cancelledFromAborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		sut.cancel();
		assertThat(sut.isAborted(),equalTo(true));
	}

	@Test
	public void testIsAborted$cancelledFromAccepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		sut.cancel();
		assertThat(sut.isAborted(),equalTo(false));
	}

	@Test
	public void testIsAborted$cancelledFromFailed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		sut.cancel();
		assertThat(sut.isAborted(),equalTo(false));
	}

	@Test
	public void testIsCancelled$aborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		assertThat(sut.isCancelled(),equalTo(false));
	}

	@Test
	public void testIsCancelled$accepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		assertThat(sut.isCancelled(),equalTo(false));
	}

	@Test
	public void testIsCancelled$failed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		assertThat(sut.isCancelled(),equalTo(false));
	}

	@Test
	public void testIsCancelled$cancelledFromAborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		sut.cancel();
		assertThat(sut.isCancelled(),equalTo(false));
	}

	@Test
	public void testIsCancelled$cancelledFromAccepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		sut.cancel();
		assertThat(sut.isCancelled(),equalTo(true));
	}

	@Test
	public void testIsCancelled$cancelledFromFailed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		sut.cancel();
		assertThat(sut.isCancelled(),equalTo(true));
	}

	@Test
	public void testIsActive$aborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		assertThat(sut.isActive(),equalTo(false));
	}

	@Test
	public void testIsActive$accepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		assertThat(sut.isActive(),equalTo(true));
	}

	@Test
	public void testIsActive$failed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		assertThat(sut.isActive(),equalTo(false));
	}

	@Test
	public void testIsActive$cancelledFromAborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		sut.cancel();
		assertThat(sut.isActive(),equalTo(false));
	}

	@Test
	public void testIsActive$cancelledFromAccepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		sut.cancel();
		assertThat(sut.isActive(),equalTo(false));
	}

	@Test
	public void testIsActive$cancelledFromFailed() throws Exception {
		final Enrichment sut = Enrichment.of(failure());
		sut.cancel();
		assertThat(sut.isActive(),equalTo(false));
	}

	@Test
	public void testGetFailure$aborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		try {
			sut.getFailure();
			fail("Should not return a failure from an aborted enrichment");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Request was accepted"));
		}
	}

	@Test
	public void testGetFailure$accepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		try {
			sut.getFailure();
			fail("Should not return a failure from an accepted enrichment");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Request was accepted"));
		}
	}

	@Test
	public void testGetFailure$failed() throws Exception {
		final FailureMessage failure = failure();
		final Enrichment sut = Enrichment.of(failure);
		assertThat(sut.getFailure(),equalTo(ProtocolUtil.toFailure(failure)));
	}

	@Test
	public void testGetFailure$cancelledFromAborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		sut.cancel();
		try {
			sut.getFailure();
			fail("Should not return a failure from an aborted enrichment");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Request was accepted"));
		}
	}

	@Test
	public void testGetFailure$cancelledFromAccepted() throws Exception {
		final Enrichment sut = Enrichment.of(accepted());
		sut.cancel();
		try {
			sut.getFailure();
			fail("Should not return a failure from an accepted enrichment");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Request was accepted"));
		}
	}

	@Test
	public void testGetFailure$cancelledFromFailed() throws Exception {
		final FailureMessage failure = failure();
		final Enrichment sut = Enrichment.of(failure);
		sut.cancel();
		assertThat(sut.getFailure(),equalTo(ProtocolUtil.toFailure(failure)));
	}

	@Test
	public void testMessageId$aborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		try {
			sut.messageId();
			fail("Should not return a message identifier from an aborted enrichment");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Request was aborted"));
		}
	}

	@Test
	public void testMessageId$accepted() throws Exception {
		final AcceptedMessage message = accepted();
		final Enrichment sut = Enrichment.of(message);
		assertThat(sut.messageId(),equalTo(message.messageId()));
	}

	@Test
	public void testMessageId$failed() throws Exception {
		final FailureMessage message = failure();
		final Enrichment sut = Enrichment.of(message);
		assertThat(sut.messageId(),equalTo(message.messageId()));
	}

	@Test
	public void testMessageId$cancelledFromAborted() throws Exception {
		final Enrichment sut = Enrichment.of(null);
		sut.cancel();
		try {
			sut.messageId();
			fail("Should not return a message identifier from an aborted enrichment");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Request was aborted"));
		}
	}

	@Test
	public void testMessageId$cancelledFromAccepted() throws Exception {
		final AcceptedMessage message = accepted();
		final Enrichment sut = Enrichment.of(message);
		assertThat(sut.messageId(),equalTo(message.messageId()));
	}

	@Test
	public void testMessageId$cancelledFromFailed() throws Exception {
		final FailureMessage message = failure();
		final Enrichment sut = Enrichment.of(message);
		assertThat(sut.messageId(),equalTo(message.messageId()));
	}

	@Test
	public void testStateValues() {
		assertThat(Arrays.asList(Enrichment.State.values()),contains(Enrichment.State.ACCEPTED,Enrichment.State.CANCELLED,Enrichment.State.FAILED,Enrichment.State.FAILED_CANCELLED,Enrichment.State.ABORTED));
	}

	@Test
	public void testStateValueOf() {
		for(Enrichment.State value:Enrichment.State.values()) {
			assertThat(Enrichment.State.valueOf(value.toString()),equalTo(value));
		}
	}

}
