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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.RequestMessage;

final class DefaultConnectorFuture extends ConnectorFuture implements Future<Acknowledge> {

	private enum State {
		WAITING,
		DONE,
		CANCELLED
	}

	private final BlockingQueue<Acknowledge> replyQueue;
	private final RequestMessage request;
	private final Connector connector;
	private final Lock lock;

	private volatile State state = State.WAITING;
	private volatile Acknowledge acknowledge;

	DefaultConnectorFuture(Connector connector, RequestMessage request) {
		this.replyQueue= new ArrayBlockingQueue<>(1);
		this.connector = connector;
		this.request = request;
		this.lock=new ReentrantLock();
	}

	@Override
	UUID messageId() {
		return this.request.messageId();
	}

	@Override
	void start() {
		// NOTHING TO DO HERE FOR THE TIME BEING
	}

	@Override
	boolean complete(Message message) throws InterruptedException {
		this.lock.lock();
		try {
			boolean result=isWaiting();
			if(result) {
				this.state=State.DONE;
				this.acknowledge=Acknowledge.of(message);
				memoizeAcknowledge();
			}
			return result;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		this.lock.lock();
		try {
			boolean result=isWaiting();
			if(result) {
				this.state=State.CANCELLED;
				this.connector.cancelRequest(this);
			}
			return true;
		} finally {
			this.lock.unlock();
		}
	}

	@Override
	public boolean isCancelled() {
		return this.state.equals(State.CANCELLED);
	}

	@Override
	public boolean isDone() {
		return this.state.equals(State.DONE);
	}

	@Override
	public Acknowledge get() throws InterruptedException {
		checkNotCancelled();
		final Acknowledge reply=this.replyQueue.take();
		memoizeAcknowledge();
		return reply;
	}

	@Override
	public Acknowledge get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		checkNotCancelled();
		final Acknowledge replyOrNull=this.replyQueue.poll(timeout, unit);
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

	private void memoizeAcknowledge() throws InterruptedException {
		this.replyQueue.put(this.acknowledge);
	}

	private void checkNotCancelled() {
		if(isCancelled()) {
			throw new CancellationException("Acknowledgement of request "+this.request.messageId()+" has been already cancelled");
		}
	}

}