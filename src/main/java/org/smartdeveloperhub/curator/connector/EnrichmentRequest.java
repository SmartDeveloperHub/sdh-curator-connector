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

import java.net.URI;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class EnrichmentRequest {

	private final URI targetResource;
	private final Filters filters;
	private final Constraints constraints;

	private EnrichmentRequest(URI targetResource, Filters filters, Constraints constraints) {
		this.targetResource=targetResource;
		this.filters=filters;
		this.constraints=constraints;
	}

	public URI targetResource() {
		return this.targetResource;
	}

	public Filters filters() {
		return this.filters;
	}

	public Constraints constraints() {
		return this.constraints;
	}

	public EnrichmentRequest withTargetResource(String targetResource) {
		return withTargetResource(URI.create(targetResource));
	}

	public EnrichmentRequest withTargetResource(URI targetResource) {
		return new EnrichmentRequest(targetResource,this.filters,this.constraints);
	}

	public EnrichmentRequest withFilters(Filters filters) {
		return new EnrichmentRequest(this.targetResource,filters,this.constraints);
	}

	public EnrichmentRequest withConstraints(Constraints constraints) {
		return new EnrichmentRequest(this.targetResource,this.filters,constraints);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.targetResource,this.filters,this.constraints);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof EnrichmentRequest) {
			EnrichmentRequest that=(EnrichmentRequest)obj;
			result=
				Objects.equals(this.targetResource,that.targetResource) &&
				Objects.equals(this.filters,that.filters) &&
				Objects.equals(this.constraints,that.constraints);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("targetResource",this.targetResource).
					add("filters",this.filters).
					add("constraints",this.constraints).
					toString();
	}

	public static EnrichmentRequest newInstance() {
		return new EnrichmentRequest(null,Filters.newInstance(),Constraints.newInstance());
	}

}
