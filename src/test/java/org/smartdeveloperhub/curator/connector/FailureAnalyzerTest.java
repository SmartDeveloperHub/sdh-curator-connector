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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;

import com.rabbitmq.client.AMQP.Channel.Close;
import com.rabbitmq.client.ShutdownSignalException;

@RunWith(JMockit.class)
public class FailureAnalyzerTest {

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(FailureAnalyzer.class),equalTo(true));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$noCause() throws Exception {
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException("failure")),equalTo(false));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$invalidCause() throws Exception {
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException("failure",new RuntimeException())),equalTo(false));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$noReason(@Mocked final ShutdownSignalException cause) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=null;
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(false));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$invalidReplyCode(@Mocked final ShutdownSignalException cause, @Mocked final Close reason) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=reason;
			reason.getReplyCode();this.result=400;
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(false));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$invalidMethodId(@Mocked final ShutdownSignalException cause, @Mocked final Close reason) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=reason;
			reason.getReplyCode();this.result=406;
			reason.getMethodId();this.result=4;
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(false));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$invalidReplyText(@Mocked final ShutdownSignalException cause, @Mocked final Close reason) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=reason;
			reason.getReplyCode();this.result=406;
			reason.getMethodId();this.result=10;
			reason.getReplyText();this.result="ERROR";
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(false));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$exchangeTypeMismatch(@Mocked final ShutdownSignalException cause, @Mocked final Close reason) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=reason;
			reason.getReplyCode();this.result=406;
			reason.getMethodId();this.result=10;
			reason.getReplyText();this.result="PRECONDITION_FAILED - inequivalent arg 'type' for exchange 'sdh' in vhost '/': received 'topic' but current is 'fanout'";
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(false));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$durabilityMismatch(@Mocked final ShutdownSignalException cause, @Mocked final Close reason) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=reason;
			reason.getReplyCode();this.result=406;
			reason.getMethodId();this.result=10;
			reason.getReplyText();this.result="PRECONDITION_FAILED - inequivalent arg 'durable' for exchange 'sdh' in vhost '/': received 'true' but current is 'false'";
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(true));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$autoDeleteMismatch(@Mocked final ShutdownSignalException cause, @Mocked final Close reason) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=reason;
			reason.getReplyCode();this.result=406;
			reason.getMethodId();this.result=10;
			reason.getReplyText();this.result="PRECONDITION_FAILED - inequivalent arg 'auto_delete' for exchange 'sdh' in vhost '/': received 'true' but current is 'false'";
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(true));
	}

	@Test
	public void testIsExchangeDeclarationRecoverable$internalMismatch(@Mocked final ShutdownSignalException cause, @Mocked final Close reason) throws Exception {
		new Expectations() {{
			cause.getReason();this.result=reason;
			reason.getReplyCode();this.result=406;
			reason.getMethodId();this.result=10;
			reason.getReplyText();this.result="PRECONDITION_FAILED - inequivalent arg 'internal' for exchange 'sdh' in vhost '/': received 'false' but current is 'true'";
		}};
		assertThat(FailureAnalyzer.isExchangeDeclarationRecoverable(new IOException(cause)),equalTo(false));
	}

}
