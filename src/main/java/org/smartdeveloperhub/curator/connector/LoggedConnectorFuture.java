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

	LoggedConnectorFuture(ConnectorFuture delegate) {
		this.delegate = delegate;
		this.completion=Stopwatch.createUnstarted();
	}

	@Override
	UUID messageId() {
		return this.delegate.messageId();
	}

	@Override
	void start() {
		this.completion.start();
		LOGGER.
			trace(
				"Awaiting acknowledgement of request {}...",
				messageId());
	}

	@Override
	boolean complete(Message message) throws InterruptedException {
		final boolean completed=this.delegate.complete(message);
		if(completed) {
			this.completion.stop();
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
	public boolean cancel(boolean mayInterruptIfRunning) {
		final boolean cancelled = this.delegate.cancel(mayInterruptIfRunning);
		if(cancelled) {
			this.completion.stop();
			LOGGER.
				trace(
					"Cancelled acknowledgement of request {} after waiting for {} milliseconds",
					messageId(),
					this.completion.elapsed(TimeUnit.MILLISECONDS));

		}
		return cancelled;
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
	public Acknowledge get() throws InterruptedException, ExecutionException {
		Stopwatch waiting=Stopwatch.createStarted();
		LOGGER.trace("Waiting for acknowledgment...");
		final Acknowledge reply=this.delegate.get();
		waiting.stop();
		LOGGER.trace("Received acknowledgment after {} milliseconds",waiting.elapsed(TimeUnit.MILLISECONDS));
		return reply;
	}

	@Override
	public Acknowledge get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		Stopwatch waiting=Stopwatch.createStarted();
		LOGGER.trace("Waiting for acknowledgment...");
		try {
			Acknowledge replyOrNull=this.delegate.get(timeout, unit);
			LOGGER.trace("Received acknowledgment after {} milliseconds",waiting.elapsed(TimeUnit.MILLISECONDS));
			return replyOrNull;
		} catch (Exception e) {
			LOGGER.trace("Did not receive acknowledgment after {} milliseconds",waiting.elapsed(TimeUnit.MILLISECONDS));
			throw e;
		}
	}

}
