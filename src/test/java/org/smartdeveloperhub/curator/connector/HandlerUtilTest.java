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
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Message;

public class HandlerUtilTest {

	public static class CustomMessage implements Message {
		@Override
		public UUID messageId() {
			return null;
		}
		@Override
		public DateTime submittedOn() {
			return null;
		}
		@Override
		public Agent submittedBy() {
			return null;
		}
	}

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(HandlerUtil.class),equalTo(true));
	}

	@Test
	public void testParsePayload$unsupportedMessageClass() throws Exception {
		assertThat(HandlerUtil.parsePayload("payload", CustomMessage.class),nullValue());
	}

	@Test
	public void testParsePayload$noDefinition() throws Exception {
		final String resource=
			ResourceUtil.
				loadResource("messages/enrichment_request.ttl");
		assertThat(HandlerUtil.parsePayload(resource, AcceptedMessage.class),nullValue());
	}

	@Test
	public void testParsePayload$multipleDefinitions() throws Exception {
		final String resource=
			ResourceUtil.
				loadResource("messages/multiple_accepted.ttl");
		assertThat(HandlerUtil.parsePayload(resource, AcceptedMessage.class),nullValue());
	}

	@Test
	public void testParsePayload$invalidDefinitions() throws Exception {
		final String resource=
			ResourceUtil.
				loadResource("messages/bad_accepted.ttl");
		assertThat(HandlerUtil.parsePayload(resource, AcceptedMessage.class),nullValue());
	}

}
