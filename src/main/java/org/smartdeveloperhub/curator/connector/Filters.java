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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Filter;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

public final class Filters implements Iterable<Filter> {

	private final ImmutableMap<URI,String> filters;

	private Filters(ImmutableMap<URI,String> filters) {
		this.filters = filters;
	}

	private ImmutableList<Filter> toList() {
		Builder<Filter> builder = ImmutableList.<Filter>builder();
		for(Entry<URI,String> entry:this.filters.entrySet()) {
			builder.add(
				ProtocolFactory.
					newFilter().
						withProperty(entry.getKey()).
						withVariable(ProtocolFactory.newVariable(entry.getValue())).
						build());
		}
		return builder.build();
	}

	public Filters withFilter(String property, String variableName) {
		checkNotNull("Filter property cannot be null");
		return withFilter(URI.create(property),variableName);
	}

	public Filters withFilter(URI property, String variableName) {
		checkNotNull("Filter property cannot be null");
		checkNotNull("Filter variable name cannot be null");
		if(variableName.equals(this.filters.get(property))) {
			return this;
		}
		checkArgument(!this.filters.containsKey(property),"Filter property already defined");
		checkArgument(!this.filters.containsValue(variableName),"Filter variable already defined");
		return
			new Filters(
				ImmutableMap.
					<URI,String>builder().
						putAll(this.filters).
						put(property,variableName).
						build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.filters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof Filters) {
			Filters that=(Filters)obj;
			result=Objects.equals(this.filters,that.filters);
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
					addValue(this.filters).
					toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Filter> iterator() {
		return toList().iterator();
	}

	public static Filters newInstance() {
		return new Filters(ImmutableMap.<URI,String>of());
	}

}
