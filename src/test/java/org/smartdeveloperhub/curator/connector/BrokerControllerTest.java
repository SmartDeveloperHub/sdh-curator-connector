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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.1.0
 *   Bundle      : sdh-curator-connector-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;

@RunWith(JMockit.class)
public class BrokerControllerTest {

	private final Broker broker=ProtocolFactory.newBroker().build();

	@Mocked private Connection connection;
	@Mocked private Channel channel;
	@Mocked private ConversionContext context;

	private final String name="name";

	private BrokerController newInstance() {
		return new BrokerController(this.broker, this.name, this.context);
	}

	@Test
	public void testBroker() throws Exception {
		assertThat(newInstance().broker(),equalTo(this.broker));
	}

	@Test
	public void testConnect$failConnection$couldNotCreateConnection$IOException() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				throw new IOException("Could not connect");
			}
		};
		try {
			sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getMessage(),equalTo("Could not connect to broker at localhost:5672 using virtual host /"));
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("Could not connect"));
		}
	}

	@Test
	public void testConnect$failConnection$couldNotCreateConnection$TimeoutException() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				throw new TimeoutException("Could not connect");
			}
		};
		try {
			sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getMessage(),equalTo("Could not connect to broker at localhost:5672 using virtual host /"));
			assertThat(e.getCause(),instanceOf(TimeoutException.class));
			assertThat(e.getCause().getMessage(),equalTo("Could not connect"));
		}
	}

	@Test
	public void testConnect$failConnection$couldNotCreateChannel$failure() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=new IOException("Could not create channel");
			BrokerControllerTest.this.connection.close();this.result=new IOException("Could not close connection");
		}};
		try {
			sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getMessage(),equalTo("Could not create channel for broker connection"));
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("Could not create channel"));
		}
	}

	@Test
	public void testConnect$failConnection$couldNotCreateChannel$nullConnection() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return null;
			}
		};
		try {
			sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getMessage(),equalTo("Could not create channel for broker connection"));
			assertThat(e.getCause(),instanceOf(IllegalStateException.class));
		}
	}

	@Test
	public void testConnect$failConnection$couldNotCreateChannel$nullChannel() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=null;
			BrokerControllerTest.this.connection.close();
		}};
		try {
			sut.connect();
		} catch (final ControllerException e) {
			assertThat(e.getMessage(),equalTo("Could not create channel for broker connection"));
			assertThat(e.getCause(),instanceOf(IllegalStateException.class));
			assertThat(e.getCause().getMessage(),equalTo("No channel available"));
		}
	}

	@Test
	public void testConnect$connected() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
		}};
		sut.connect();
		sut.connect();
	}


	@Test
	public void testPublishMessage$unexpectedFailure() throws Exception {
		final BrokerController sut=newInstance();
		final DeliveryChannel dc =
			ProtocolFactory.
				newDeliveryChannel().
					withExchangeName("exchangeName").
					withRoutingKey("routingKey").
					build();
		final String message = "message";
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
			BrokerControllerTest.this.channel.basicPublish(dc.exchangeName(), dc.routingKey(), true, (BasicProperties)this.any, message.getBytes());this.result=new ShutdownSignalException(true,true,null,BrokerControllerTest.this.channel);
		}};
		sut.connect();
		try {
			sut.publishMessage(dc, message);
		} catch (final IOException e) {
			assertThat(e.getMessage(),equalTo("Unexpected failure while publishing message [message] to exchange 'exchangeName' and routing key 'routingKey' using broker localhost:5672/: clean connection shutdown"));
			assertThat(e.getCause(),instanceOf(ShutdownSignalException.class));
		}
		new Verifications() {{
			BasicProperties s;
			BrokerControllerTest.this.channel.basicPublish(dc.exchangeName(), dc.routingKey(), true, s=withCapture(), message.getBytes());
			assertThat(s.getDeliveryMode(),equalTo(2));
			assertThat(s.getHeaders().get(BrokerController.BROKER_CONTROLLER_MESSAGE),instanceOf(Long.class));
		}};
	}

	@Test
	public void testPublishMessage$shareChannelInThread() throws Exception {
		final BrokerController sut=newInstance();
		final DeliveryChannel dc =
			ProtocolFactory.
				newDeliveryChannel().
					withExchangeName("exchangeName").
					withRoutingKey("routingKey").
					build();
		final String message = "message";
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;this.minTimes=2;this.maxTimes=2;
			BrokerControllerTest.this.channel.basicPublish(dc.exchangeName(), dc.routingKey(), true, (BasicProperties)this.any, message.getBytes());this.minTimes=2;this.maxTimes=2;
		}};
		sut.connect();
		sut.publishMessage(dc, message);
		sut.publishMessage(dc, message);
		new Verifications() {{
			BasicProperties s;
			BrokerControllerTest.this.channel.basicPublish(dc.exchangeName(), dc.routingKey(), true, s=withCapture(), message.getBytes());
			assertThat(s.getDeliveryMode(),equalTo(2));
			assertThat(s.getHeaders().get(BrokerController.BROKER_CONTROLLER_MESSAGE),instanceOf(Long.class));
		}};
	}

	@Test
	public void testDisconnect$connected() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
			BrokerControllerTest.this.channel.isOpen();this.result=true;
			BrokerControllerTest.this.channel.close();this.result=new IOException("Could not close channel");
			BrokerControllerTest.this.connection.close();
		}};
		sut.connect();
		sut.disconnect();
		try {
			sut.disconnect();
		} catch (final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Not connected"));
		}
	}

	@Test
	public void testDisconnect$unlockOnError() throws Exception {
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
			BrokerControllerTest.this.channel.isOpen();this.result=true;
			BrokerControllerTest.this.channel.close();
			BrokerControllerTest.this.connection.close();this.result=new Throwable("failure");
		}};
		sut.connect();
		try {
			sut.disconnect();
			fail("Should not complete disconnection if failure happens");
		} catch (final Throwable e) {
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testDisconnect$notConnected() throws Exception {
		final BrokerController sut=newInstance();
		try {
			sut.disconnect();
		} catch (final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Not connected"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDisconnect$ignoreCleanerFailures(@Mocked final DeclareOk ok) throws Exception {
		final String routingKey="routingKey";
		final String queueName="queueName";
		final String exchangeName="exchangeName";
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
			BrokerControllerTest.this.channel.queueDeclare(queueName,true,false,true,(Map<String,Object>)this.any);this.result=ok;
			ok.getQueue();this.result=queueName;
			BrokerControllerTest.this.channel.queueUnbind(queueName,exchangeName,routingKey);this.result=new IOException("failure");
		}};
		sut.connect();
		sut.declareQueue(queueName);
		sut.bindQueue(exchangeName, queueName, routingKey);
		sut.disconnect();
	}

	@Test
	public void testRegisterHandler$unlockOnError() throws Exception {
		final String queueName="queueName";
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
			BrokerControllerTest.this.channel.basicConsume(queueName, true, (MessageHandlerConsumer)this.any);this.result=new IOException("failure");
		}};
		sut.connect();
		try {
			sut.registerConsumer(null, queueName);
		} catch (final IOException e) {
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testDeclareQueue$null() throws Exception {
		final BrokerController sut=new BrokerController(ProtocolFactory.newBroker().build(), this.name, this.context);
		sut.connect();
		final String queueName = sut.declareQueue(null);
		assertThat(queueName,not(isEmptyOrNullString()));
		sut.disconnect();
	}

	@Test
	public void testDeclareExchange$failure$recreateChannelIfRecoverable() throws Exception {
		final String exchangeName="exchangeName";
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new MockUp<FailureAnalyzer>() {
			@Mock
			public boolean isExchangeDeclarationRecoverable(final IOException e) {
				return true;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
			BrokerControllerTest.this.channel.exchangeDeclare(exchangeName,"topic",true,true,null);this.result=new IOException("failure");
		}};
		sut.connect();
		sut.declareExchange(exchangeName);
	}

	@Test
	public void testDeclareExchange$failure$abortIfNotRecoverable() throws Exception {
		final String exchangeName="exchangeName";
		final BrokerController sut=newInstance();
		new MockUp<ConnectionFactory>() {
			@Mock
			public void setHost(final String host) {
			}
			@Mock
			public void setPort(final int port) {
			}
			@Mock
			public void setVirtualHost(final String virtualHost) {
			}
			@Mock
			public void setThreadFactory(final ThreadFactory threadFactory) {
			}
			@Mock
			public Connection newConnection() throws IOException, TimeoutException {
				return BrokerControllerTest.this.connection;
			}
		};
		new Expectations() {{
			BrokerControllerTest.this.connection.createChannel();this.result=BrokerControllerTest.this.channel;
			BrokerControllerTest.this.channel.exchangeDeclare(exchangeName,"topic",true,true,null);this.result=new IOException("failure");
		}};
		sut.connect();
		try {
			sut.declareExchange(exchangeName);
			fail("Should not declare a exception if the broker fails");
		} catch (final ControllerException e) {
			assertThat(e.getMessage(),equalTo("Could not create name exchange named 'exchangeName'"));
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
		}
	}

}
