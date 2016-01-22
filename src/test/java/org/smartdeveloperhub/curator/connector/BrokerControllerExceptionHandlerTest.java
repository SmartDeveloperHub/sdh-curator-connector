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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.smartdeveloperhub.curator.protocol.Broker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.TopologyRecoveryException;

@RunWith(JMockit.class)
public class BrokerControllerExceptionHandlerTest {

	@Mocked private BrokerController controller;
	@Mocked private Broker broker;

	@Mocked private Logger logger;

	@Mocked private Connection connection;
	@Mocked private Channel channel;
	@Mocked private Consumer consumer;
	@Mocked private TopologyRecoveryException topologyException;

	private final RuntimeException exception = new RuntimeException();

	private BrokerControllerExceptionHandler prepareSut() {
		new MockUp<Logger>() {
			@Mock
			public void trace(final String message, final Object[] args) {
				assertThat(args.length,greaterThan(2));
				assertThat(args[args.length-1],instanceOf(Throwable.class));
			}
		};
		final BrokerControllerExceptionHandler sut=new BrokerControllerExceptionHandler(this.controller);
		new Expectations() {{
			BrokerControllerExceptionHandlerTest.this.controller.broker();this.result=BrokerControllerExceptionHandlerTest.this.broker;
		}};
		return sut;
	}

	@Test
	public void testHandleUnexpectedConnectionDriverException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleUnexpectedConnectionDriverException(this.connection, this.exception);
	}

	@Test
	public void testHandleReturnListenerException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleReturnListenerException(this.channel, this.exception);
	}

	@Test
	public void testHandleFlowListenerException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleFlowListenerException(this.channel, this.exception);
	}

	@Test
	public void testHandleConfirmListenerException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleConfirmListenerException(this.channel, this.exception);
	}

	@Test
	public void testHandleBlockedListenerException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleBlockedListenerException(this.connection, this.exception);
	}

	@Test
	public void testHandleConsumerException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleConsumerException(this.channel, this.exception, this.consumer, "tag", "methodName");
	}

	@Test
	public void testHandleConnectionRecoveryException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleConnectionRecoveryException(this.connection, this.exception);
	}

	@Test
	public void testHandleChannelRecoveryException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleChannelRecoveryException(this.channel, this.exception);
	}

	@Test
	public void testHandleTopologyRecoveryException() throws Exception {
		final BrokerControllerExceptionHandler sut = prepareSut();
		sut.handleTopologyRecoveryException(this.connection, this.channel, this.topologyException);
	}

}
