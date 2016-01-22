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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Message;

import com.rabbitmq.client.Channel;

@RunWith(JMockit.class)
public class ClientConnectorControllerTest {

	private final CuratorConfiguration configuration=
		CuratorConfiguration.
			newInstance().
				withExchangeName("exchangeName").
				withQueueName("requestQueueName").
				withRequestRoutingKey("requestRoutingKey").
				withResponseRoutingKey("responseRoutingKey");

	@Mocked private Channel channel;
	@Mocked private Message message;
	@Mocked private ClientCuratorController curatorController;
	@Mocked private BrokerController brokerController;
	@Mocked private Broker defaultBroker;
	@Mocked private Broker anotherBroker;
	@Mocked private ConversionContext context;

	private ClientConnectorController newController(final String queueName, final DeliveryChannel cfg) {
		return new ClientConnectorController(queueName,cfg,this.context,this.curatorController);
	}

	@Test
	public void testConnect$failOnExchangeDeclaration$sameBrokerAndDifferentExchangeName() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.brokerController();this.result=ClientConnectorControllerTest.this.brokerController;
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
			ClientConnectorControllerTest.this.brokerController.declareExchange("connectorExchange");this.result=new ConnectorException("failure",null);
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.defaultBroker).
						withExchangeName("connectorExchange").
						withRoutingKey("routingKey").
						build());
		try {
			sut.connect();
		} catch (final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("failure"));
			assertThat(e.getCause(),nullValue());
		}
	}

	@Test
	public void testConnect$failOnExchangeDeclaration$differentBroker() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
			ClientConnectorControllerTest.this.brokerController.declareExchange(ClientConnectorControllerTest.this.configuration.exchangeName());this.result=new ConnectorException("failure",null);
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.anotherBroker).
						withExchangeName(this.configuration.exchangeName()).
						withRoutingKey("routingKey").
						build());
		try {
			sut.connect();
		} catch (final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("failure"));
			assertThat(e.getCause(),nullValue());
		}
	}

	@Test
	public void testConnect$failOnAnonymousQueueDeclaration() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.brokerController();this.result=ClientConnectorControllerTest.this.brokerController;
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
			ClientConnectorControllerTest.this.brokerController.declareQueue(null);this.result=new ConnectorException("failure",null);
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.defaultBroker).
						withRoutingKey("routingKey").
						build());
		try {
			sut.connect();
		} catch (final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("failure"));
			assertThat(e.getCause(),nullValue());
		}
	}

	@Test
	public void testConnect$failOnAnonymousQueueDeclarationOnDifferentBroker() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
			ClientConnectorControllerTest.this.brokerController.declareQueue(null);this.result=new ConnectorException("failure",null);
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.anotherBroker).
						withRoutingKey("routingKey").
						build());
		try {
			sut.connect();
		} catch (final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("failure"));
			assertThat(e.getCause(),nullValue());
		}
	}

	@Test
	public void testConnect$failOnDifferentQueueDeclaration() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.brokerController();this.result=ClientConnectorControllerTest.this.brokerController;
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
			ClientConnectorControllerTest.this.brokerController.declareQueue("connectorQueueName");this.result=new ConnectorException("failure",null);
		}};
		final ClientConnectorController sut=
			newController(
				"connectorQueueName",
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.defaultBroker).
						withExchangeName(this.configuration.exchangeName()).
						withRoutingKey("routingKey").
						build());
		try {
			sut.connect();
		} catch (final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("failure"));
			assertThat(e.getCause(),nullValue());
		}
	}

	@Test
	public void testConnect$failOnAnonymousQueueBinding() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
			ClientConnectorControllerTest.this.brokerController.declareQueue(null);this.result="queue";
			ClientConnectorControllerTest.this.brokerController.bindQueue(ClientConnectorControllerTest.this.configuration.exchangeName(), "queue", "routingKey");
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.anotherBroker).
						withRoutingKey("routingKey").
						build());
		try {
			sut.connect();
		} catch (final ConnectorException e) {
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
			assertThat(e.getMessage(),equalTo("Could not bind connector queue 'queue' using routing key 'routingKey' to exchange 'exchangeName'"));
		}
	}

	@Test
	public void testConnect$failOnRequestQueueBinding() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
			ClientConnectorControllerTest.this.curatorController.brokerController();this.result=ClientConnectorControllerTest.this.brokerController;
			ClientConnectorControllerTest.this.brokerController.bindQueue(ClientConnectorControllerTest.this.configuration.exchangeName(), ClientConnectorControllerTest.this.configuration.queueName(), "routingKey");this.result=new ConnectorException("failure",null);
		}};
		final ClientConnectorController sut=
			newController(
				this.configuration.queueName(),
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.defaultBroker).
						withRoutingKey("routingKey").
						build());
		try {
			sut.connect();
		} catch (final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("failure"));
			assertThat(e.getCause(),nullValue());
		}
	}

	@Test
	public void testBrokerController$sameBrokerAsCuratorController() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.brokerController();this.result=ClientConnectorControllerTest.this.brokerController;
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.defaultBroker).
						withRoutingKey("routingKey").
						build());
		assertThat(sut.brokerController(),equalTo(this.brokerController));
	}

	@Test
	public void testBrokerController$differentBrokerAsCuratorController() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.curatorConfiguration();this.result=ClientConnectorControllerTest.this.configuration.withBroker(ClientConnectorControllerTest.this.defaultBroker);
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker(this.anotherBroker).
						withRoutingKey("routingKey").
						build());
		assertThat(sut.brokerController(),not(equalTo(this.brokerController)));
	}

	@Test
	public void testBrokerController$nullBrokerFromCuratorController() throws Exception {
		new Expectations() {{
			ClientConnectorControllerTest.this.curatorController.brokerController();this.result=ClientConnectorControllerTest.this.brokerController;
		}};
		final ClientConnectorController sut=
			newController(
				null,
				ProtocolFactory.
					newDeliveryChannel().
						withBroker((Broker)null).
						withRoutingKey("routingKey").
						build());
		assertThat(sut.brokerController(),equalTo(this.brokerController));
	}

}
