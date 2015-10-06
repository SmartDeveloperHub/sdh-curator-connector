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

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.base.Stopwatch;

public class CancelableExchange extends Exchanger<Message> {

	private static final Logger LOGGER=LoggerFactory.getLogger(CancelableExchange.class);

	private final Message message;

	private volatile boolean cancelled;

	public CancelableExchange(Message message) {
		this.message=message;
	}

	public final Message exchange() throws InterruptedException {
		final AtomicInteger interruptions=new AtomicInteger();
		final Stopwatch waiting=Stopwatch.createStarted();
		while(!this.cancelled) {
			final Stopwatch timeOut=Stopwatch.createStarted();
			try {
				final Message result = exchange(this.message,100,TimeUnit.MILLISECONDS);
				logCompletion(interruptions, waiting, "completed with message "+result.messageId()+")");
				return result;
			} catch (Exception e) {
				processInterruption(interruptions,waiting,timeOut,e);
			}
		}
		logCompletion(interruptions, waiting, "cancelled");
		throw new InterruptedException("Exchange cancelled");
	}

	private void logCompletion(AtomicInteger interruptions, Stopwatch waiting, final String completion) {
		LOGGER.trace(
			"Exchange for message {} {} after {} tries. Waited for {} milliseconds",
			this.message.messageId(),
			completion,
			interruptions,
			waiting.elapsed(TimeUnit.MILLISECONDS));
	}

	private void processInterruption(AtomicInteger interruptions, Stopwatch waiting, Stopwatch timeOut, Throwable cause) {
		interruptions.incrementAndGet();
		LOGGER.trace(
			"Exchange retry #{} for message {} {} after milliseconds {}. Waiting for exchange for {} milliseconds. Retrying one more time...",
			interruptions,
			this.message.messageId(),
			cause instanceof TimeoutException?"timed-out":"interrupted",
			timeOut.elapsed(TimeUnit.MILLISECONDS),
			waiting.elapsed(TimeUnit.MILLISECONDS));
	}

	public void cancel() {
		this.cancelled=true;
	}

}
