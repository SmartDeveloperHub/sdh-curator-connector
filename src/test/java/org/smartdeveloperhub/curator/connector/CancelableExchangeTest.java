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
import static org.hamcrest.Matchers.*;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.smartdeveloperhub.curator.protocol.Accepted;

public class CancelableExchangeTest {

	private final class TestThread extends Thread {

		private final CancelableExchange exchange;

		private boolean finished=false;

		private TestThread(CancelableExchange exchange) {
			this.exchange = exchange;
		}

		@Override
		public void run() {
			try {
				this.exchange.exchange();
			} catch (InterruptedException e) {
				// IGNORE
			}
			this.finished=true;
		}
	}

	private Accepted message() {
		return ProtocolFactory.
			newAccepted().
				withMessageId(UUID.randomUUID()).
				withSubmittedOn(new Date()).
				withSubmittedBy(
					ProtocolFactory.
						newAgent().
							withAgentId(UUID.randomUUID())).
				withResponseTo(UUID.randomUUID()).
				withResponseNumber(23).
				build();
	}

	@Test
	public void testCancel() throws Exception {
		final CancelableExchange exchange=new CancelableExchange(message());
		TestThread thread = new TestThread(exchange);
		thread.start();
		TimeUnit.MILLISECONDS.sleep(200);
		exchange.cancel();
		thread.join();
		assertThat(thread.finished,equalTo(true));
	}

	@Test
	public void testCancel$withInterruption() throws Exception {
		final CancelableExchange exchange=new CancelableExchange(message());
		TestThread thread = new TestThread(exchange);
		thread.start();
		TimeUnit.MILLISECONDS.sleep(200);
		thread.interrupt();
		assertThat(thread.isAlive(),equalTo(true));
		TimeUnit.MILLISECONDS.sleep(200);
		exchange.cancel();
		thread.join();
		assertThat(thread.finished,equalTo(true));
	}

}
