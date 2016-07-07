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
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.base.Stopwatch;

@RunWith(JMockit.class)
public class LoggedConnectorFutureTest {

	@Mocked private DefaultConnectorFuture delegate;

	private LoggedConnectorFuture sut;

	@Mocked private Message message;

	@Test
	public void testComplete() throws Exception {
		this.sut=new LoggedConnectorFuture(this.delegate);
		new MockUp<Stopwatch>() {
			@Mock
			boolean isRunning() {
				return false;
			}
		};
		new Expectations() {{
			LoggedConnectorFutureTest.this.delegate.complete(LoggedConnectorFutureTest.this.message);this.result=true;
		}};
		assertThat(this.sut.complete(this.message),equalTo(true));
	}

	@Test
	public void testCancel() throws Exception {
		this.sut=new LoggedConnectorFuture(this.delegate);
		new MockUp<Stopwatch>() {
			@Mock
			boolean isRunning() {
				return false;
			}
		};
		new Expectations() {{
			LoggedConnectorFutureTest.this.delegate.cancel(true);this.result=true;
		}};
		assertThat(this.sut.cancel(true),equalTo(true));
	}

	@Test
	public void testIsCancelled() throws Exception {
		this.sut=new LoggedConnectorFuture(this.delegate);
		new Expectations() {{
			LoggedConnectorFutureTest.this.delegate.isCancelled();this.result=true;
		}};
		assertThat(this.sut.isCancelled(),equalTo(true));
	}

	@Test
	public void testIsDone() throws Exception {
		this.sut=new LoggedConnectorFuture(this.delegate);
		new Expectations() {{
			LoggedConnectorFutureTest.this.delegate.isDone();this.result=true;
		}};
		assertThat(this.sut.isDone(),equalTo(true));
	}

}
