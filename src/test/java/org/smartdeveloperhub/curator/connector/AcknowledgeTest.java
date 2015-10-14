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
import static org.smartdeveloperhub.curator.connector.ProtocolFactory.*;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.FailureMessageBuilder;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;


public class AcknowledgeTest {

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

	private FailureMessage failure(boolean full) {
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
				withReason("A failure");
		if(full) {
			builder.
				withSubcode(1344).
				withDetail("The detail of the failure");
		}
		return builder.build();
	}

	@Test
	public void testIsAccepted$accepted() throws Exception {
		final Acknowledge sut = Acknowledge.of(accepted());
		assertThat(sut.isAccepted(),equalTo(true));
	}

	@Test
	public void testIsAccepted$failure() throws Exception {
		final Acknowledge sut = Acknowledge.of(failure(true));
		assertThat(sut.isAccepted(),equalTo(false));
	}

	@Test
	public void testGetFailure$accepted() throws Exception {
		final Acknowledge sut = Acknowledge.of(accepted());
		try {
			sut.getFailure();
			fail("Should not return a failure from an accepted message");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Request was accepted"));
		}
	}

	@Test
	public void testGetFailure$failure() throws Exception {
		final FailureMessage failure = failure(true);
		final Acknowledge sut = Acknowledge.of(failure);
		assertThat(sut.getFailure(),equalTo(ProtocolUtil.toFailureDescription(failure)));
	}

}
