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
import static org.hamcrest.Matchers.sameInstance;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.Curator;
import org.smartdeveloperhub.curator.Notifier;
import org.smartdeveloperhub.curator.RandomMessageIdentifierFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Message;

@RunWith(JMockit.class)
public class ConnectorTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(ConnectorTest.class);

	final DeliveryChannel deliveryChannel =
			ProtocolFactory.
				newDeliveryChannel().
					withQueueName("connector").
					build();

	@Test
	public void testDefaultConfiguration() throws Exception {
		final Connector connector =
			Connector.
				builder().
					withConnectorChannel(null).
					withAgentIdentifier((String)null).
					withCuratorConfiguration(null).
					withMessageIdentifierFactory(null).
					build();
		final ConnectorConfiguration configuration = Deencapsulation.getField(connector,ConnectorConfiguration.class);
		assertThat(configuration.agent(),notNullValue());
		assertThat(configuration.connectorChannel(),notNullValue());
		assertThat(configuration.connectorChannel().queueName(),equalTo("connector"));
		assertThat(configuration.curatorConfiguration(),equalTo(CuratorConfiguration.newInstance()));
	}
	@Test

	public void testKeepsConfiguration() throws Exception {
		final CuratorConfiguration curatorConfiguration = CuratorConfiguration.newInstance();
		final UUID agentId = UUID.randomUUID();
		final DefaultMessageIdentifierFactory factory = new DefaultMessageIdentifierFactory();
		final Connector connector =
			Connector.
				builder().
					withConnectorChannel(this.deliveryChannel).
					withAgentIdentifier(agentId.toString()).
					withCuratorConfiguration(curatorConfiguration).
					withMessageIdentifierFactory(factory).
					build();
		final ConnectorConfiguration configuration = Deencapsulation.getField(connector,ConnectorConfiguration.class);
		assertThat(configuration.agent().agentId(),equalTo(agentId));
		assertThat(configuration.connectorChannel(),sameInstance(this.deliveryChannel));
		assertThat(configuration.curatorConfiguration(),sameInstance(curatorConfiguration));
	}

	@Test
	public void testOnlyConnectOnce() throws Exception {
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		connector.connect();
		try {
			connector.connect();
		} catch(final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Already connected"));
		} finally {
			connector.disconnect();
		}
	}

	@Test
	public void testConnect$failIfCuratorControllerCannotConnect() throws Exception {
		new MockUp<ClientCuratorController>() {
			@Mock
			void connect() throws ControllerException {
				throw new ControllerException("failure",new IllegalStateException());
			}
		};
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		try {
			connector.connect();
		} catch(final ConnectorException e) {
			assertThat(e,instanceOf(ControllerException.class));
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testConnect$failIfConnectorControllerCannotConnect() throws Exception {
		new MockUp<ClientConnectorController>() {
			@Mock
			void connect() throws ControllerException {
				throw new ControllerException("failure",new IllegalStateException());
			}
		};
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		try {
			connector.connect();
		} catch(final ConnectorException e) {
			assertThat(e,instanceOf(ControllerException.class));
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testConnect$failIfCannotAddResponseHandler() throws Exception {
		new MockUp<ClientCuratorController>() {
			@Mock
			void handleResponses(final MessageHandler handler) throws IOException {
				throw new IOException("failure",new IllegalStateException());
			}
		};
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		try {
			connector.connect();
		} catch(final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("Could not setup the curator response message handler"));
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testConnect$failIfCannotAddMessageHandler() throws Exception {
		new MockUp<ClientConnectorController>() {
			@Mock
			void handleMessage(final MessageHandler handler) throws IOException {
				throw new IOException("failure",new IllegalStateException());
			}
		};
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		try {
			connector.connect();
		} catch(final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("Could not setup the connector response message handler"));
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testDisconnect$cleanIfCannotSendDisconnect() throws Exception {
		new MockUp<ClientCuratorController>() {
			@Mock
			void publishRequest(final Message message) throws IOException {
				throw new IOException("failure",new IllegalStateException());
			}
		};
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		connector.connect();
		try {
			connector.disconnect();
		} catch(final ConnectorException e) {
			assertThat(e.getMessage(),equalTo("Could not send disconnect to curator"));
			assertThat(e.getCause(),instanceOf(IOException.class));
			assertThat(e.getCause().getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testDisconnect$cancelAwaitingEnrichments() throws Exception {
		new MockUp<ClientCuratorController>() {
			@Mock
			void connect() throws ControllerException {
			}
			@Mock
			void handleResponses(final MessageHandler handler) throws IOException {
			}
			@Mock
			void publishRequest(final Message message) throws IOException {
			}
			@Mock
			void disconnect() throws ControllerException {
			}
		};
		new MockUp<ClientConnectorController>() {
			@Mock
			void connect() throws ControllerException {
			}
			@Mock
			void handleMessage(final MessageHandler handler) throws IOException {
			}
			@Mock
			void disconnect() throws ControllerException {
			}
		};
		new MockUp<ConnectorConfiguration>() {
			@Mock
			public Agent agent() {
				return ProtocolFactory.newAgent().withAgentId(UUID.randomUUID()).build();
			}
			@Mock
			public DeliveryChannel connectorChannel() {
				return ConnectorTest.this.deliveryChannel;
			}
			@Override
			@Mock
			public String toString() {
				return "configuration";
			}
		};
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		connector.connect();
		final Phaser phaser=new Phaser(1);
		final AtomicReference<Enrichment> result=new AtomicReference<Enrichment>();
		final Thread thread = new Thread("awaiting-client") {
			@Override
			public void run() {
				try {
					final Future<Enrichment> response=
							connector.
								requestEnrichment(
									UseCase.EXAMPLE_REQUEST,
									new EnrichmentResultHandler() {
										@Override
										public void onResult(final EnrichmentResult response) {
											LOGGER.debug("Received: {}",response);
										}
									}
								);
					phaser.arrive();
					result.set(response.get());
				} catch (final Exception e) {
					LOGGER.info("Something failed",e);
				}
			}
		};
		thread.start();
		phaser.awaitAdvance(1);
		TimeUnit.MILLISECONDS.sleep(500);
		connector.disconnect();
		thread.join();
		final Enrichment enrichment=result.get();
		assertThat(enrichment.isAborted(),equalTo(true));
		assertThat(enrichment.isAccepted(),equalTo(false));
		assertThat(enrichment.isActive(),equalTo(false));
		assertThat(enrichment.isCancelled(),equalTo(false));
		LOGGER.info("Acknowledge: {}",enrichment);
	}

	@Test
	public void testOnlyDisconnectOnce() throws Exception {
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		connector.connect();
		connector.disconnect();
		try {
			connector.disconnect();
		} catch(final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Not connected"));
		}
	}

	@Test
	public void testRequestEnrichment$failIfDisconnected() throws Exception {
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		try {
			connector.
				requestEnrichment(
					UseCase.EXAMPLE_REQUEST,
					new EnrichmentResultHandler() {
						@Override
						public void onResult(final EnrichmentResult response) {
							LOGGER.debug("Received: {}",response);
						}
					}
				);
		} catch(final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Not connected"));
		}
	}

	@Test
	public void testRequestEnrichment$useCase() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(3);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onEnrichmentResponse(final EnrichmentResponseMessage response) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.accept(factory.generated(0),UseCase.EXAMPLE_RESULT);
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
									answered.arrive();
								}
							}
						);
				final Enrichment enrichment = response.get();
				assertThat(enrichment.isAborted(),equalTo(false));
				assertThat(enrichment.isAccepted(),equalTo(true));
				assertThat(enrichment.isActive(),equalTo(true));
				assertThat(enrichment.isCancelled(),equalTo(false));
				LOGGER.info("Acknowledge: {}",enrichment);
				answered.arriveAndAwaitAdvance();
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}

	@Test
	public void testRequestEnrichment$accepted() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(2);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onError(final UUID requestId) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.accept(factory.generated(0));
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				final Enrichment enrichment = response.get();
				assertThat(enrichment.isAborted(),equalTo(false));
				assertThat(enrichment.isAccepted(),equalTo(true));
				assertThat(enrichment.isActive(),equalTo(true));
				assertThat(enrichment.isCancelled(),equalTo(false));
				LOGGER.info("Acknowledge: {}",enrichment);
				answered.arriveAndAwaitAdvance();
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}

	@Test
	public void testRequestEnrichment$failed() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(2);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onFailure(final FailureMessage response) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.fail(factory.generated(0),Failure.newInstance().withCode(1).withReason("A failure"));
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				final Enrichment enrichment = response.get();
				assertThat(enrichment.isAborted(),equalTo(false));
				assertThat(enrichment.isAccepted(),equalTo(false));
				assertThat(enrichment.isActive(),equalTo(false));
				assertThat(enrichment.isCancelled(),equalTo(false));
				LOGGER.info("Acknowledge: {}",enrichment);
				answered.arriveAndAwaitAdvance();
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}

	@Test
	public void testRequestEnrichment$badAcknowledge() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(2);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onError(final UUID requestId) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.fail(factory.generated(0));
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				response.get(1000,TimeUnit.MILLISECONDS);
			} catch(final TimeoutException e) {
				answered.arriveAndAwaitAdvance();
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}
	@Test
	public void testRequestEnrichment$completionFailure() throws Exception {
		new MockUp<DefaultConnectorFuture>() {
			@Mock
			boolean complete(final Message message) throws InterruptedException {
				throw new InterruptedException("failure");
			}
		};
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(2);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onFailure(final FailureMessage response) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.fail(factory.generated(0),Failure.newInstance().withCode(1).withReason("A failure"));
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				response.get(1000,TimeUnit.MILLISECONDS);
			} catch(final TimeoutException e) {
				answered.arriveAndAwaitAdvance();
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}

	@Test
	public void testCancel$failIfDisconnected() throws Exception {
		final Connector connector =
				Connector.
					builder().
						withConnectorChannel(this.deliveryChannel).
						build();
		try {
			connector.cancelEnrichment(null);
		} catch(final IllegalStateException e) {
			assertThat(e.getMessage(),equalTo("Not connected"));
		}
	}

	@Test
	public void testCancel$active() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(2);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onError(final UUID requestId) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.accept(factory.generated(0));
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				final Enrichment enrichment = response.get();
				assertThat(enrichment.isAborted(),equalTo(false));
				assertThat(enrichment.isAccepted(),equalTo(true));
				assertThat(enrichment.isActive(),equalTo(true));
				assertThat(enrichment.isCancelled(),equalTo(false));
				LOGGER.info("Acknowledge: {}",enrichment);
				answered.arriveAndAwaitAdvance();
				connector.cancelEnrichment(enrichment);
				assertThat(enrichment.isCancelled(),equalTo(true));
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}

	@Test
	public void testLateAcknowledge() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(2);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onFailure(final FailureMessage response) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.fail(factory.generated(0),Failure.newInstance().withCode(1).withReason("A failure"));
		curator.delayAcknowledges(1000, TimeUnit.MILLISECONDS);
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				try {
					response.get(150,TimeUnit.MILLISECONDS);
				} catch(final TimeoutException e) {
					response.cancel(true);
					assertThat(response.isDone(),equalTo(true));
					assertThat(response.isCancelled(),equalTo(true));
				}
				answered.arriveAndAwaitAdvance();
				TimeUnit.MILLISECONDS.sleep(1000);
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}


	@Test
	public void testLateResponse() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final CountDownLatch accepted=new CountDownLatch(1);
		final CountDownLatch replied=new CountDownLatch(1);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
				LOGGER.info("Disconnect message sent");
			}
			@Override
			public void onAccepted(final AcceptedMessage response) {
				accepted.countDown();
				LOGGER.info("Accepted message sent");
			}
			@Override
			public void onEnrichmentResponse(final EnrichmentResponseMessage response) {
				replied.countDown();
				LOGGER.info("Response message sent");
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.accept(factory.generated(0),UseCase.EXAMPLE_RESULT);
		curator.delayAcknowledges(1,TimeUnit.MILLISECONDS);
		curator.delayResults(500,TimeUnit.MILLISECONDS);
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				response.cancel(true);
				assertThat(response.isDone(),equalTo(true));
				assertThat(response.isCancelled(),equalTo(true));
				accepted.await();
				replied.await();
				TimeUnit.MILLISECONDS.sleep(1000);
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}

	@Test
	public void testCancel$inactive() throws Exception {
		final Phaser disconnected=new Phaser(2);
		final Phaser answered=new Phaser(2);
		class CustomNotifier extends Notifier {
			@Override
			public void onDisconnect(final DisconnectMessage response) {
				disconnected.arrive();
			}
			@Override
			public void onError(final UUID requestId) {
				answered.arrive();
			}
		}
		final RandomMessageIdentifierFactory factory=RandomMessageIdentifierFactory.create(2);
		final Curator curator=Curator.newInstance(this.deliveryChannel,new CustomNotifier());
		curator.accept(factory.generated(0));
		curator.connect();
		try {
			final Connector connector =
					Connector.
						builder().
							withConnectorChannel(this.deliveryChannel).
							withMessageIdentifierFactory(factory).
							build();
			connector.connect();
			try {
				final Future<Enrichment> response=
					connector.
						requestEnrichment(
							UseCase.EXAMPLE_REQUEST,
							new EnrichmentResultHandler() {
								@Override
								public void onResult(final EnrichmentResult response) {
									LOGGER.debug("Received: {}",response);
								}
							}
						);
				final Enrichment enrichment = response.get();
				assertThat(enrichment.isAborted(),equalTo(false));
				assertThat(enrichment.isAccepted(),equalTo(true));
				assertThat(enrichment.isActive(),equalTo(true));
				assertThat(enrichment.isCancelled(),equalTo(false));
				LOGGER.info("Acknowledge: {}",enrichment);
				answered.arriveAndAwaitAdvance();
				connector.cancelEnrichment(enrichment);
				assertThat(enrichment.isCancelled(),equalTo(true));
				connector.cancelEnrichment(enrichment);
				assertThat(enrichment.isCancelled(),equalTo(true));
			} finally {
				connector.disconnect();
			}
			disconnected.arriveAndAwaitAdvance();
			LOGGER.info("Disconnection processed");
		} finally {
			curator.disconnect();
		}
	}
}
