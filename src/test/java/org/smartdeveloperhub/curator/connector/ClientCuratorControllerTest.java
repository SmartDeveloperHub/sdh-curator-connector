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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.Channel;

@RunWith(JMockit.class)
public class ClientCuratorControllerTest {

	private final CuratorConfiguration configuration=
		CuratorConfiguration.
			newInstance().
				withExchangeName("exchangeName").
				withRequestQueueName("requestQueueName").
				withRequestRoutingKey("requestRoutingKey").
				withResponseQueueName("responseQueueName").
				withResponseRoutingKey("responseRoutingKey");

	@Mocked private Channel channel;

	@Mocked private Message message;

	private ClientCuratorController sut;

	@Before
	public void setUp() {
		this.sut=new ClientCuratorController(this.configuration, "client");
	}

	@Test
	public void testBrokerController() throws Exception {
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		assertThat(this.sut.brokerController(),notNullValue());
	}

	@Test
	public void testCuratorConfiguration() throws Exception {
		assertThat(this.sut.curatorConfiguration(),equalTo(this.configuration));
	}

	@Test
	public void testConnect$failWhenCannotCreateExchange() throws Exception {
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		new Expectations() {{
			ClientCuratorControllerTest.this.channel.exchangeDeclare(ClientCuratorControllerTest.this.configuration.exchangeName(), "direct");this.result=new IOException("failure");
		}};
		try {
			this.sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not create curator exchange named '"+this.configuration.exchangeName()+"'"));
		}
	}

	@Test
	public void testConnect$failWhenCannotCreateRequestQueue() throws Exception {
		final String exchangeName = this.configuration.exchangeName();
		final String requestQueueName = this.configuration.requestQueueName();
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		new Expectations() {{
			ClientCuratorControllerTest.this.channel.exchangeDeclare(exchangeName,"direct");
			ClientCuratorControllerTest.this.channel.queueDeclare(requestQueueName,true,false, false,null);this.result=new IOException("failure");
		}};
		try {
			this.sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not create curator queue named '"+requestQueueName+"'"));
		}
	}

	@Test
	public void testConnect$failWhenCannotBindRequestQueue() throws Exception {
		final String exchangeName = this.configuration.exchangeName();
		final String requestQueueName = this.configuration.requestQueueName();
		final String requestRoutingKey = this.configuration.requestRoutingKey();
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		new Expectations() {{
			ClientCuratorControllerTest.this.channel.exchangeDeclare(exchangeName,"direct");
			ClientCuratorControllerTest.this.channel.queueDeclare(requestQueueName,true,false,false,null);
			ClientCuratorControllerTest.this.channel.queueBind(requestQueueName,exchangeName,requestRoutingKey);this.result=new IOException("failure");
		}};
		try {
			this.sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not bind curator queue '"+requestQueueName+"' to exchange '"+exchangeName+"' using routing key '"+requestRoutingKey+"'"));
		}
	}

	@Test
	public void testConnect$failWhenCannotCreateResponseQueue() throws Exception {
		final String requestQueueName = this.configuration.responseQueueName();
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		new Expectations() {{
			ClientCuratorControllerTest.this.channel.queueDeclare(requestQueueName,true,false,false,null);this.result=new IOException("failure");
		}};
		try {
			this.sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not create curator queue named '"+requestQueueName+"'"));
		}
	}

	@Test
	public void testConnect$failWhenCannotBindResponseQueue() throws Exception {
		final String exchangeName = this.configuration.exchangeName();
		final String requestQueueName = this.configuration.responseQueueName();
		final String requestRoutingKey = this.configuration.responseRoutingKey();
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		new Expectations() {{
			ClientCuratorControllerTest.this.channel.queueBind(requestQueueName,exchangeName,requestRoutingKey);this.result=new IOException("failure");
		}};
		try {
			this.sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not bind curator queue '"+requestQueueName+"' to exchange '"+exchangeName+"' using routing key '"+requestRoutingKey+"'"));
		}
	}

	@Test
	public void testDisconnect() throws Exception {
		final AtomicBoolean disconnected=new AtomicBoolean(false);
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			void disconnect() {
				disconnected.set(true);
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		this.sut.disconnect();
		assertThat(disconnected.get(),equalTo(true));
	}

	@Test
	public void testPublishRequest$failMessageConversion() throws Exception {
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		try {
			this.sut.publishRequest(this.message);
		} catch (final IOException e) {
			assertThat(e.getCause(),instanceOf(MessageConversionException.class));
			assertThat(e.getCause().getMessage(),equalTo("Cannot convert messages of type '"+this.message.getClass().getName()+"'"));
			assertThat(e.getMessage(),equalTo("Could not serialize message"));
		}
	}

	@Test
	public void testPublishRequest$fail() throws Exception {
		new MockUp<BrokerController>() {
			@Mock
			void connect() {
			}
			@Mock
			Channel channel() {
				return ClientCuratorControllerTest.this.channel;
			}
		};
		new MockUp<MessageUtil>() {
			@Mock
			public <T extends Message> String toString(final T message) throws MessageConversionException {
				return "message";
			}
		};
		new Expectations() {{
			ClientCuratorControllerTest.this.channel.basicPublish(ClientCuratorControllerTest.this.configuration.exchangeName(),ClientCuratorControllerTest.this.configuration.requestRoutingKey(),null,"message".getBytes());this.result=new IOException("failure");
		}};
		try {
			this.sut.publishRequest(this.message);
		} catch (final IOException e) {
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}

}
