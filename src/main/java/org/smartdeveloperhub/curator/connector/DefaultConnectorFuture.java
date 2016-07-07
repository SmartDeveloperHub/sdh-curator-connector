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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.3.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.RequestMessage;

final class DefaultConnectorFuture extends ConnectorFuture implements Future<Enrichment> {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultConnectorFuture.class);

	enum State {
		WAITING,
		DONE,
		CANCELLED
	}

	private final BlockingQueue<Enrichment> replyQueue;
	private final RequestMessage request;
	private final Connector connector;
	private final Lock lock;

	private volatile State state = State.WAITING;
	private volatile Enrichment acknowledge;

	DefaultConnectorFuture(final Connector connector, final RequestMessage request) {
		this.replyQueue= new ArrayBlockingQueue<>(1);
		this.connector = connector;
		this.request = request;
		this.lock=new ReentrantLock();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	UUID messageId() {
		return this.request.messageId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void start() {
		// NOTHING TO DO HERE FOR THE TIME BEING
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean complete(final Message message) throws InterruptedException {
		this.lock.lock();
		try {
			if(isWaiting()) {
				this.acknowledge=Enrichment.of(message);
				try {
					memoizeAcknowledge();
					this.state=State.DONE;
				} catch (final InterruptedException e) {
					LOGGER.warn("Could not memoize acknowledgement {}",this.acknowledge,e);
				}
			}
			return isCompleted();
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		this.lock.lock();
		try {
			if(isWaiting()) {
				this.connector.abortRequest(this);
				this.acknowledge=Enrichment.of(null);
				try {
					memoizeAcknowledge();
					this.state=State.CANCELLED;
				} catch (final InterruptedException e) {
					LOGGER.warn("Could not memoize cancellation",e);
				}
			}
			return isCancelled();
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCancelled() {
		return this.state.equals(State.CANCELLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDone() {
		return !this.state.equals(State.WAITING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enrichment get() throws InterruptedException {
		final Enrichment reply=this.replyQueue.take();
		memoizeAcknowledge();
		return reply;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enrichment get(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
		final Enrichment replyOrNull=this.replyQueue.poll(timeout, unit);
		if(replyOrNull==null) {
			throw new TimeoutException();
		} else {
			memoizeAcknowledge();
		}
		return replyOrNull;
	}

	private boolean isWaiting() {
		return this.state.equals(State.WAITING);
	}

	private boolean isCompleted() {
		return this.state.equals(State.DONE);
	}

	private void memoizeAcknowledge() throws InterruptedException {
		this.replyQueue.put(this.acknowledge);
	}

}