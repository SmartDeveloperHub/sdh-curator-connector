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
package org.smartdeveloperhub.curator.connector.protocol;

import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.FailureMessage;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;

final class ImmutableFailureMessage extends ImmutableResponseMessage implements FailureMessage {

	private final long code;
	private final Optional<Long> subcode;
	private final String reason;
	private final String detail;

	ImmutableFailureMessage( // NOSONAR
		UUID messageId,
		DateTime submittedOn,
		Agent agent,
		UUID responseTo,
		long responseNumber,
		long code,
		Long subcode,
		String reason,
		String detail) {
		super(messageId, submittedOn, agent, responseTo,responseNumber);
		this.code=code;
		this.subcode=Optional.fromNullable(subcode);
		this.reason=reason;
		this.detail=detail;
	}

	@Override
	public long code() {
		return this.code;
	}

	@Override
	public Optional<Long> subcode() {
		return this.subcode;
	}

	@Override
	public String reason() {
		return this.reason;
	}

	@Override
	public String detail() {
		return this.detail;
	}

	@Override
	protected void toString(ToStringHelper helper) {
		super.toString(helper);
		helper.
			add("code",this.code).
			add("subcode",this.subcode.orNull()).
			add("reason",this.reason).
			add("detail",this.detail);
	}

}