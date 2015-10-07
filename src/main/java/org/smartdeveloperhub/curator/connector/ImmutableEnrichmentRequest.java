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
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Policy;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;

final class ImmutableEnrichmentRequest extends ImmutableRequest implements EnrichmentRequest {

	private final URI targetResource;
	private final ImmutableList<Filter> filters;
	private final ImmutableList<Constraint> constraints;

	ImmutableEnrichmentRequest(
			UUID messageId,
			DateTime submittedOn,
			Agent agent,
			DeliveryChannel deliveryChannel,
			URI targetResource,
			List<Filter> filters,
			List<Constraint> constraints) {
		super(messageId,submittedOn,agent,deliveryChannel);
		this.targetResource = targetResource;
		this.filters=ImmutableList.copyOf(filters);
		this.constraints=ImmutableList.copyOf(constraints);
	}

	@Override
	public URI targetResource() {
		return this.targetResource;
	}

	@Override
	public Policy apply() {
		return null;
	}

	@Override
	public List<Filter> filters() {
		return this.filters;
	}

	@Override
	public List<Constraint> constraints() {
		return this.constraints;
	}

	@Override
	protected void toString(ToStringHelper helper) {
		super.toString(helper);
		helper.
			add("targetResource", this.targetResource).
			add("filters",this.filters).
			add("constraints",this.constraints);
	}

}