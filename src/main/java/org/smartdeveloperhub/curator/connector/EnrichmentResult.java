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
import java.util.Set;

import org.smartdeveloperhub.curator.protocol.Value;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public final class EnrichmentResult {

	private static ImmutableMap<URI, Value> EMPTY=ImmutableMap.<URI,Value>builder().build();

	private final URI targetResource;
	private final ImmutableMap<URI,Value> additions;
	private final ImmutableMap<URI,Value> removals;

	private EnrichmentResult(URI targetResource, ImmutableMap<URI,Value> additions, ImmutableMap<URI,Value> removals) {
		this.targetResource=targetResource;
		this.additions=additions;
		this.removals=removals;
	}

	private ImmutableMap<URI, Value> append(ImmutableMap<URI, Value> mappings, URI property, Value value) {
		return ImmutableMap.<URI,Value>builder().putAll(mappings).put(property,value).build();
	}

	public URI targetResource() {
		return this.targetResource;
	}

	public Set<URI> addedProperties() {
		return this.additions.keySet();
	}


	public Value addedValue(URI property) {
		return this.additions.get(property);
	}

	public Set<URI> removedProperties() {
		return this.removals.keySet();
	}

	public Value removedValue(URI property) {
		return this.removals.get(property);
	}

	public EnrichmentResult withTargetResource(URI targetResource) {
		return new EnrichmentResult(targetResource,this.additions,this.removals);
	}

	public EnrichmentResult withAddition(URI property, Value value) {
		return new EnrichmentResult(this.targetResource,append(this.additions,property,value),this.removals);
	}

	public EnrichmentResult withRemoval(URI property, Value value) {
		return new EnrichmentResult(this.targetResource,this.additions,append(removals,property,value));
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
		return new EnrichmentResult(null,EMPTY,EMPTY);
	}

}
