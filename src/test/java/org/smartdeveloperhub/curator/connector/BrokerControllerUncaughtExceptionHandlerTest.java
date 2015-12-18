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
import static org.hamcrest.Matchers.sameInstance;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.smartdeveloperhub.curator.protocol.Broker;

@RunWith(JMockit.class)
public class BrokerControllerUncaughtExceptionHandlerTest {

	@Mocked private BrokerController controller;
	@Mocked private Broker broker;

	@Test
	public void testUncaughtException() throws Exception {
		final RuntimeException failure = new RuntimeException();
		new MockUp<Logger>() {
			@Mock
			void trace(final String message, final Object... args) {
				assertThat(args.length,equalTo(3));
				assertThat(args[0],equalTo((Object)BrokerControllerUncaughtExceptionHandlerTest.this.broker));
				assertThat(args[1],equalTo((Object)Thread.currentThread().getName()));
				assertThat(args[2],sameInstance((Object)failure));
			}
		};
		final BrokerControllerUncaughtExceptionHandler sut=new BrokerControllerUncaughtExceptionHandler(this.controller);
		new Expectations() {{
			BrokerControllerUncaughtExceptionHandlerTest.this.controller.broker();this.result=BrokerControllerUncaughtExceptionHandlerTest.this.broker;
		}};
		sut.uncaughtException(Thread.currentThread(), failure);
	}

}
