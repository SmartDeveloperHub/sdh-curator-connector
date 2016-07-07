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
import com.google.common.base.Preconditions;

public final class EnrichmentResult {

	private final URI targetResource;
	private final Bindings additions;
	private final Bindings removals;

	private EnrichmentResult(final URI targetResource, final Bindings additions, final Bindings removals) {
		this.targetResource=targetResource;
		this.additions=additions;
		this.removals=removals;
	}

	public URI targetResource() {
		return this.targetResource;
	}

	public Bindings additions() {
		return this.additions;
	}

	public Bindings removals() {
		return this.removals;
	}

	public EnrichmentResult withTargetResource(final String targetResource) {
		URI target=null;
		if(targetResource!=null) {
			target = URI.create(targetResource);
		}
		return withTargetResource(target);
	}

	public EnrichmentResult withTargetResource(final URI targetResource) {
		return new EnrichmentResult(targetResource,this.additions,this.removals);
	}

	public EnrichmentResult withAdditions(final Bindings additions) {
		Preconditions.checkNotNull(additions,"Added bindings cannot be null");
		return new EnrichmentResult(this.targetResource,additions,this.removals);
	}

	public EnrichmentResult withRemovals(final Bindings removals) {
		Preconditions.checkNotNull(removals,"Removed bindings cannot be null");
		return new EnrichmentResult(this.targetResource,this.additions,removals);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.targetResource,this.additions,this.removals);
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof EnrichmentResult) {
			final EnrichmentResult that=(EnrichmentResult)obj;
			result=
				Objects.equals(this.targetResource,that.targetResource) &&
				Objects.equals(this.additions,that.additions) &&
				Objects.equals(this.removals,that.removals);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("targetResource",this.targetResource).
					add("additions",this.additions).
					add("removals",this.removals).
					toString();
	}

	public static EnrichmentResult newInstance() {
		return new EnrichmentResult(null,Bindings.newInstance(),Bindings.newInstance());
	}

}
