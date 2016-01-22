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

import java.util.UUID;

import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Enrichment {

	public enum State {
		ACCEPTED(false,false,false) {
			@Override
			State cancel() {
				return State.CANCELLED;
			}
		},
		CANCELLED(false,true,false),
		FAILED(true,false,false) {
			@Override
			State cancel() {
				return State.FAILED_CANCELLED;
			}
		},
		FAILED_CANCELLED(true,true,false),
		ABORTED(false,false,true),
		;

		private boolean failed;
		private boolean cancelled;
		private boolean aborted;

		private State(boolean failed, boolean cancelled, boolean aborted) {
			this.failed = failed;
			this.cancelled = cancelled;
			this.aborted = aborted;
		}

		State cancel() {
			return this;
		}

		boolean isAccepted() {
			return !isAborted() && !isFailed();
		}

		boolean isFailed() {
			return this.failed;
		}

		boolean isAborted() {
			return this.aborted;
		}

		boolean isActive() {
			return !isFailed() && !isCancelled() && !isAborted();
		}

		boolean isCancelled() {
			return this.cancelled;
		}

		Failure getFailure(Message message) {
			Preconditions.checkState(isFailed(),"Request was accepted");
			return ProtocolUtil.toFailure((FailureMessage)message);
		}

		public UUID getMessageId(Message message) {
			Preconditions.checkState(!isAborted(),"Request was aborted");
			return message.messageId();
		}

		static State fromMessage(Message message) {
			if(message==null) {
				return ABORTED;
			} else if(message instanceof AcceptedMessage) {
				return ACCEPTED;
			} else if(message instanceof FailureMessage) {
				return FAILED;
			} else {
				throw new RuntimeConnectorException("Unexpected message "+message);
			}
		}

	}

	private volatile State state;

	private final Message message;

	private Enrichment(Message message) {
		this.message = message;
		this.state=State.fromMessage(message);
	}

	synchronized boolean cancel() {
		State oldState=this.state;
		this.state=oldState.cancel();
		return oldState.isActive();
	}

	UUID messageId() {
		return this.state.getMessageId(this.message);
	}

	public boolean isAborted() {
		return this.state.isAborted();
	}

	public boolean isActive() {
		return this.state.isActive();
	}

	public boolean isAccepted() {
		return this.state.isAccepted();
	}

	public boolean isFailed() {
		return this.state.isFailed();
	}

	public boolean isCancelled() {
		return this.state.isCancelled();
	}

	public Failure getFailure() {
		return this.state.getFailure(this.message);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("state",this.state).
					add("message", this.message).
					toString();
	}

	static Enrichment of(Message message) {
		return new Enrichment(message);
	}

}
