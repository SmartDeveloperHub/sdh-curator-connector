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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

@RunWith(JMockit.class)
public class ClientCuratorControllerTest {

	private final CuratorConfiguration configuration=
		CuratorConfiguration.
			newInstance().
				withExchangeName("exchangeName").
				withQueueName("requestQueueName").
				withRequestRoutingKey("requestRoutingKey").
				withResponseRoutingKey("responseRoutingKey");

	@Mocked private Channel channel;

	@Mocked private EnrichmentRequestMessage message;

	@Mocked private Message unsupportedMessage;

	private final Agent agent =
		ProtocolFactory.
			newAgent().
				withAgentId(UUID.randomUUID()).
					build();

	private ClientCuratorController sut;

	private String requestRoutingKey() {
		return this.configuration.requestRoutingKey()+".enrichment";
	}

	@Before
	public void setUp() {
		this.sut=new ClientCuratorController(this.configuration, "client", ConversionContext.newInstance());
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
			ClientCuratorControllerTest.this.channel.exchangeDeclare(ClientCuratorControllerTest.this.configuration.exchangeName(), "topic",true,true,null);this.result=new IOException("failure");
		}};
		try {
			this.sut.connect(this.agent);
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not create client exchange named '"+this.configuration.exchangeName()+"'"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConnect$failWhenCannotCreateResponseQueue() throws Exception {
		final String requestQueueName = this.configuration.queueName();
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
			ClientCuratorControllerTest.this.channel.queueDeclare(requestQueueName,true,false,true,(Map<String,Object>)this.any);this.result=new IOException("failure");
		}};
		try {
			this.sut.connect(this.agent);
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not create client queue named '"+requestQueueName+"'"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConnect$failWhenCannotBindResponseQueue(@Mocked final DeclareOk ok) throws Exception {
		final String exchangeName = this.configuration.exchangeName();
		final String queueName = this.configuration.queueName();
		final String routingKey = this.configuration.responseRoutingKey()+"."+this.agent.agentId();
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
			ClientCuratorControllerTest.this.channel.queueDeclare(queueName,true,false,true,(Map<String,Object>)this.any);this.result=ok;
			ok.getQueue();this.result=queueName;
			ClientCuratorControllerTest.this.channel.queueBind(queueName,exchangeName,routingKey);this.result=new IOException("failure");
		}};
		try {
			this.sut.connect(this.agent);
		} catch (final ControllerException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not bind client queue '"+queueName+"' to exchange '"+exchangeName+"' using routing key '"+routingKey+"'"));
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
			this.sut.publishRequest(this.unsupportedMessage);
		} catch (final IOException e) {
			assertThat(e.getCause(),instanceOf(MessageConversionException.class));
			assertThat(e.getCause().getMessage(),equalTo("Cannot convert messages of type '"+this.unsupportedMessage.getClass().getName()+"'"));
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
			Channel createNewChannel() {
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
			ClientCuratorControllerTest.this.channel.basicPublish(ClientCuratorControllerTest.this.configuration.exchangeName(),requestRoutingKey(),true,(BasicProperties)this.any,"message".getBytes());this.result=new IOException("failure");
		}};
		try {
			this.sut.publishRequest(this.message);
		} catch (final IOException e) {
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}

}
