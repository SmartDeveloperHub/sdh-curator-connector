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
package org.smartdeveloperhub.curator.connector.protocol;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;

final class ImmutableEnrichmentResponseMessage extends ImmutableResponseMessage implements EnrichmentResponseMessage {

	private final URI targetResource;
	private final List<Binding> additions;
	private final List<Binding> removals;

	ImmutableEnrichmentResponseMessage( // NOSONAR
		UUID messageId,
		DateTime submittedOn,
		Agent agent,
		UUID responseTo,
		long responseNumber,
		URI targetResource,
		List<Binding> additions,
		List<Binding> removals) {
		super(messageId, submittedOn, agent, responseTo,responseNumber);
		this.targetResource=targetResource;
		this.additions=ImmutableList.<Binding>copyOf(additions);
		this.removals=ImmutableList.<Binding>copyOf(removals);
	}

	@Override
	public URI targetResource() {
		return this.targetResource;
	}

	@Override
	public List<Binding> additions() {
		return this.additions;
	}

	@Override
	public List<Binding> removals() {
		return this.removals;
	}

	@Override
	protected void toString(ToStringHelper helper) {
		super.toString(helper);
		helper.
			add("targetResource",this.targetResource).
			add("additions",this.additions).
			add("removals",this.removals);
	}



}