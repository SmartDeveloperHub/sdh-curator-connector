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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.Filter;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

public final class EnrichmentSpecification {

	private final URI targetResource;
	private final ImmutableList<Filter> filters;
	private final ImmutableList<Constraint> constraints;

	private EnrichmentSpecification(URI targetResource, ImmutableList<Filter> filters, ImmutableList<Constraint> constraints) {
		this.targetResource=targetResource;
		this.filters=filters;
		this.constraints=constraints;
	}

	public URI targetResource() {
		return this.targetResource;
	}

	public EnrichmentSpecification withTargetResource(URI targetResource) {
		return new EnrichmentSpecification(targetResource,this.filters,this.constraints);
	}

	public List<Filter> filters() {
		return this.filters;
	}

	public EnrichmentSpecification withFilters(Collection<? extends Filter> filters) {
		return new EnrichmentSpecification(this.targetResource,ImmutableList.<Filter>copyOf(filters),this.constraints);
	}

	public List<Constraint> constraints() {
		return this.constraints;
	}

	public EnrichmentSpecification withConstraints(Collection<? extends Constraint> constraints) {
		return new EnrichmentSpecification(this.targetResource,this.filters,ImmutableList.<Constraint>copyOf(constraints));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.targetResource,this.filters,this.constraints);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof EnrichmentSpecification) {
			EnrichmentSpecification that=(EnrichmentSpecification)obj;
			result=
				Objects.equals(this.targetResource,that.targetResource) &&
				Objects.equals(this.filters,that.filters) &&
				Objects.equals(this.constraints,that.constraints);
		}
		return result;
	}

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

	public static EnrichmentSpecification newInstance() {
		return new EnrichmentSpecification(null,ImmutableList.<Filter>of(),ImmutableList.<Constraint>of());
	}

}
