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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.RequestMessage;


@RunWith(JMockit.class)
public class DefaultConnectorFutureTest {

	private static abstract class Client<T> extends Thread {

		private Phaser phaser;
		private ConnectorFuture sut;

		private Exception failure;
		private T result;

		public Client(final String name) {
			super();
			setName(name);
		}

		public final Client<T> withPhaser(final Phaser phaser) {
			this.phaser = phaser;
			return this;
		}

		public final Client<T> withSut(final ConnectorFuture sut) {
			this.sut = sut;
			return this;
		}

		@Override
		public final void run() {
			this.phaser.arriveAndAwaitAdvance();
			final LoggedConnectorFuture future = new LoggedConnectorFuture(this.sut);
			future.start();
			try {
				this.result=execute(future);
			} catch (final Exception e) {
				this.failure=e;
			}
		}

		protected abstract T execute(ConnectorFuture future) throws Exception;

		public synchronized final T getResult() throws Exception {
			if(this.failure!=null) {
				throw this.failure;
			}
			return this.result;
		}

	}

	@Injectable private RequestMessage request;
	@Injectable private Connector connector;

	@Tested private DefaultConnectorFuture sut;

	@Mocked private AcceptedMessage accepted;
	@Mocked private FailureMessage failed;

	private <T> T complete(final Client<T> behaviour, final Message message) throws Exception {
		final LoggedConnectorFuture loggedSut = new LoggedConnectorFuture(this.sut);
		loggedSut.start();
		final Phaser phaser=new Phaser(1);
		behaviour.
			withPhaser(phaser).
			withSut(this.sut).
			start();
		phaser.awaitAdvance(1);
		TimeUnit.SECONDS.sleep(1);
		loggedSut.complete(message);
		behaviour.join();
		return behaviour.getResult();
	}

	private void orchestrate(final Client<?> one, final Client<?> another) throws Exception {
		final LoggedConnectorFuture loggedSut = new LoggedConnectorFuture(this.sut);
		loggedSut.start();
		final Phaser phaser=new Phaser(2);
		one.
			withPhaser(phaser).
			withSut(this.sut).
			start();
		another.
			withPhaser(phaser).
			withSut(this.sut).
			start();
		phaser.awaitAdvance(2);
		TimeUnit.SECONDS.sleep(1);
		one.join();
		another.join();
	}

	private <T> T cancel(final Client<T> behaviour) throws Exception {
		final LoggedConnectorFuture loggedSut = new LoggedConnectorFuture(this.sut);
		loggedSut.start();
		final Phaser phaser=new Phaser(1);
		behaviour.
			withPhaser(phaser).
			withSut(this.sut).
			start();
		phaser.awaitAdvance(1);
		TimeUnit.SECONDS.sleep(1);
		loggedSut.cancel(true);
		behaviour.join();
		return behaviour.getResult();
	}

	@Test
	public void testStateValues() {
		assertThat(Arrays.asList(DefaultConnectorFuture.State.values()),contains(DefaultConnectorFuture.State.WAITING,DefaultConnectorFuture.State.DONE,DefaultConnectorFuture.State.CANCELLED));
	}

	@Test
	public void testStateValueOf() {
		for(final DefaultConnectorFuture.State value:DefaultConnectorFuture.State.values()) {
			assertThat(DefaultConnectorFuture.State.valueOf(value.toString()),equalTo(value));
		}
	}

	@Test
	public void testCancel() throws Exception {
		new Expectations() {{
			DefaultConnectorFutureTest.this.connector.abortRequest(DefaultConnectorFutureTest.this.sut);
		}};
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.cancel(true),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(true));
		assertThat(this.sut.isDone(),equalTo(true));
	}

	@Test
	public void testCancel$onlyAbortOnce() throws Exception {
		new Expectations() {{
			DefaultConnectorFutureTest.this.connector.abortRequest(DefaultConnectorFutureTest.this.sut);this.maxTimes=1;
		}};
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.cancel(true),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(true));
		assertThat(this.sut.isDone(),equalTo(true));
		assertThat(this.sut.cancel(true),equalTo(true));
	}

	@Test
	public void testCancel$abortOnAbortFailure() throws Exception {
		new Expectations() {{
			DefaultConnectorFutureTest.this.connector.abortRequest(DefaultConnectorFutureTest.this.sut);this.result=new RuntimeException("failure");
		}};
		assertThat(this.sut.isCancelled(),equalTo(false));
		try {
			this.sut.cancel(true);
		} catch (final RuntimeException e) {
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}

	@Test
	public void testCancel$abortOnMemoizeFailure() throws Exception {
		new MockUp<DefaultConnectorFuture>() {
			@Mock
			public void memoizeAcknowledge() throws InterruptedException {
				throw new InterruptedException("Failure");
			}
		};
		new Expectations() {{
			DefaultConnectorFutureTest.this.connector.abortRequest(DefaultConnectorFutureTest.this.sut);this.maxTimes=1;
		}};
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.cancel(true),equalTo(false));
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.isDone(),equalTo(false));
	}

	@Test
	public void testComplete$doesNotOverwrite() throws Exception {
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.isDone(),equalTo(false));
		assertThat(this.sut.complete(this.accepted),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.isDone(),equalTo(true));
		final Enrichment er1 = this.sut.get();
		assertThat(this.sut.complete(this.failed),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.isDone(),equalTo(true));
		final Enrichment er2 = this.sut.get();
		assertThat(er2,sameInstance(er1));
	}

	@Test
	public void testComplete$abortOnMemoizeFailure() throws Exception {
		new MockUp<DefaultConnectorFuture>() {
			@Mock
			public void memoizeAcknowledge() throws InterruptedException {
				throw new InterruptedException("Failure");
			}
		};
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.isDone(),equalTo(false));
		assertThat(this.sut.complete(this.accepted),equalTo(false));
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(this.sut.isDone(),equalTo(false));
	}

	@Test
	public void testComplete$abortOnHardMemoizeFailure() throws Exception {
		new MockUp<DefaultConnectorFuture>() {
			@Mock
			public void memoizeAcknowledge() throws InterruptedException {
				throw new RuntimeException("failure");
			}
		};
		try {
			this.sut.complete(this.accepted);
		} catch (final RuntimeException e) {
			assertThat(e.getMessage(),equalTo("failure"));
		}
	}


	@Test
	public void testGet$awaitForCompletion() throws Exception {
		final Client<Enrichment> behaviour=new Client<Enrichment>("get-completion") {
			@Override
			protected Enrichment execute(final ConnectorFuture future) throws InterruptedException, ExecutionException {
				return future.get();
			}
		};
		final Enrichment result=complete(behaviour,this.accepted);
		assertThat(this.sut.isDone(),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(result,notNullValue());
	}

	@Test
	public void testGet$cancelWhileWaitingForCompletion() throws Exception {
		final Client<Enrichment> behaviour=new Client<Enrichment>("get-cancel-while-waiting") {
			@Override
			protected Enrichment execute(final ConnectorFuture future) throws InterruptedException, ExecutionException {
				return future.get();
			}
		};
		final Enrichment result=cancel(behaviour);
		assertThat(this.sut.isDone(),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(true));
		assertThat(result,notNullValue());
	}

	@Test
	public void testGet$cancelAfterComplete() throws Exception {
		final Client<Enrichment> client=new Client<Enrichment>("client-get") {
			@Override
			protected Enrichment execute(final ConnectorFuture future) throws InterruptedException, ExecutionException {
				return future.get();
			}
		};
		final Client<Boolean> server=new Client<Boolean>("server-complete-cancel") {
			@Override
			protected Boolean execute(final ConnectorFuture future) throws InterruptedException, ExecutionException {
				future.complete(DefaultConnectorFutureTest.this.accepted);
				return future.cancel(true);
			}
		};
		orchestrate(client,server);
		assertThat(this.sut.isDone(),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(client.getResult(),notNullValue());
		assertThat(server.getResult(),equalTo(false));
	}

	@Test
	public void testGet$completeAfterCancel() throws Exception {
		final Client<Enrichment> client=new Client<Enrichment>("client-get") {
			@Override
			protected Enrichment execute(final ConnectorFuture future) throws InterruptedException, ExecutionException {
				return future.get();
			}
		};
		final Client<Boolean> server=new Client<Boolean>("server-cancel-complete") {
			@Override
			protected Boolean execute(final ConnectorFuture future) throws InterruptedException, ExecutionException {
				future.cancel(true);
				return future.complete(DefaultConnectorFutureTest.this.accepted);
			}
		};
		orchestrate(client,server);
		assertThat(this.sut.isDone(),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(true));
		assertThat(client.getResult(),notNullValue());
		assertThat(server.getResult(),equalTo(false));
	}

	@Test
	public void testTimedGet$abortIfNotCompleted() throws Exception {
		try {
			this.sut.get(10,TimeUnit.MILLISECONDS);
		} catch (final TimeoutException e) {
			assertThat(e.getMessage(),nullValue());
		}
	}

	@Test
	public void testTimedGet$awaitForCompletion() throws Exception {
		final Client<Enrichment> behaviour=new Client<Enrichment>("timed-get-completion") {
			@Override
			protected Enrichment execute(final ConnectorFuture future) throws InterruptedException, ExecutionException, TimeoutException {
				return future.get(2,TimeUnit.SECONDS);
			}
		};
		final Enrichment result=complete(behaviour, this.accepted);
		assertThat(this.sut.isDone(),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(false));
		assertThat(result,notNullValue());
	}

	@Test
	public void testTimedGet$lateCompletion() throws Exception {
		final Client<Enrichment> behaviour=new Client<Enrichment>("timed-get-late-completion") {
			@Override
			protected Enrichment execute(final ConnectorFuture future) throws InterruptedException, ExecutionException, TimeoutException {
				return future.get(10,TimeUnit.MILLISECONDS);
			}
		};
		try {
			complete(behaviour, this.accepted);
			fail("Should not complete when timed-out");
		} catch (final Exception e) {
			assertThat(e,instanceOf(TimeoutException.class));
		}
		assertThat(this.sut.isDone(),equalTo(true));
		assertThat(this.sut.isCancelled(),equalTo(false));
	}


}
