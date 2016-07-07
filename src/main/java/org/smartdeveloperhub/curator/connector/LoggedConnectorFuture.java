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

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.base.Stopwatch;

final class LoggedConnectorFuture extends ConnectorFuture {


	private static final Logger LOGGER=LoggerFactory.getLogger(LoggedConnectorFuture.class);

	private final ConnectorFuture delegate;
	private final Stopwatch completion;

	LoggedConnectorFuture(final ConnectorFuture delegate) {
		this.delegate = delegate;
		this.completion=Stopwatch.createUnstarted();
	}

	@Override
	UUID messageId() {
		return this.delegate.messageId();
	}

	@Override
	void start() {
		this.delegate.start();
		this.completion.start();
		LOGGER.
			trace(
				"Awaiting acknowledgement of request {}...",
				messageId());
	}

	@Override
	boolean complete(final Message message) throws InterruptedException {
		final boolean completed=this.delegate.complete(message);
		if(completed) {
			stopTimer();
			LOGGER.
				trace(
					"Completed acknowledgement of request {} with response {} after {} milliseconds",
					messageId(),
					message.messageId(),
					this.completion.elapsed(TimeUnit.MILLISECONDS));
		}
		return completed;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		final boolean cancelled = this.delegate.cancel(mayInterruptIfRunning);
		if(cancelled) {
			stopTimer();
			LOGGER.
				trace(
					"Cancelled acknowledgement of request {} after waiting for {} milliseconds",
					messageId(),
					this.completion.elapsed(TimeUnit.MILLISECONDS));
		}
		return cancelled;
	}

	private void stopTimer() {
		if(this.completion.isRunning()) {
			this.completion.stop();
		}
	}

	@Override
	public boolean isCancelled() {
		return this.delegate.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.delegate.isDone();
	}

	@Override
	public Enrichment get() throws InterruptedException, ExecutionException {
		final Stopwatch waiting=Stopwatch.createStarted();
		LOGGER.trace("Waiting for acknowledgment...");
		final Enrichment reply=this.delegate.get();
		waiting.stop();
		logAcknowledgeReception(waiting);
		return reply;
	}

	@Override
	public Enrichment get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		final Stopwatch waiting=Stopwatch.createStarted();
		LOGGER.trace("Waiting for acknowledgment...");
		try {
			final Enrichment replyOrNull=this.delegate.get(timeout, unit);
			logAcknowledgeReception(waiting);
			return replyOrNull;
		} catch (final Exception e) {
			LOGGER.trace("Did not receive acknowledgment after {} milliseconds",waiting.elapsed(TimeUnit.MILLISECONDS));
			throw e;
		}
	}

	private void logAcknowledgeReception(final Stopwatch waiting) {
		LOGGER.trace(
			"{} {} milliseconds",
			this.delegate.isCancelled()?
				"Cancelled while waiting for acknowledgement for":
				"Received acknowledgement after ",
			waiting.elapsed(TimeUnit.MILLISECONDS));
	}

}
