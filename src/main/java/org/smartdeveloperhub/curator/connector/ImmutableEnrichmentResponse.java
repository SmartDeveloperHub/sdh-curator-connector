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

import java.net.URI;
import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;

import com.google.common.base.MoreObjects.ToStringHelper;

final class ImmutableEnrichmentResponse extends ImmutableResponse implements EnrichmentResponse {

	private final URI targetResource;
	private final URI additionTarget;
	private final URI removalTarget;

	ImmutableEnrichmentResponse( // NOSONAR
		UUID messageId,
		DateTime submittedOn,
		Agent agent,
		UUID responseTo,
		long responseNumber,
		URI targetResource,
		URI additionTarget,
		URI removalTarget) {
		super(messageId, submittedOn, agent, null, responseTo,responseNumber);
		this.targetResource=targetResource;
		this.additionTarget=additionTarget;
		this.removalTarget=removalTarget;
	}

	@Override
	public URI targetResource() {
		return this.targetResource;
	}

	@Override
	public URI additionTarget() {
		return this.additionTarget;
	}

	@Override
	public URI removalTarget() {
		return this.removalTarget;
	}

	@Override
	protected void toString(ToStringHelper helper) {
		super.toString(helper);
		helper.
			add("targetResource",this.targetResource).
			add("additionTarget",this.additionTarget).
			add("removalTarget",this.removalTarget);
	}



}