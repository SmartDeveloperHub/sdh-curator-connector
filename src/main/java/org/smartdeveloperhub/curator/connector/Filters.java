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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Collection;
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

	private final ImmutableMap<URI,String> mappings;

	private Filters(final ImmutableMap<URI,String> filters) {
		this.mappings = filters;
	}

	private ImmutableList<Filter> toList() {
		final Builder<Filter> builder = ImmutableList.<Filter>builder();
		for(final Entry<URI,String> entry:this.mappings.entrySet()) {
			builder.add(
				ProtocolFactory.
					newFilter().
						withProperty(entry.getKey()).
						withVariable(ProtocolFactory.newVariable(entry.getValue())).
						build());
		}
		return builder.build();
	}

	public Filters withFilter(final String property, final String variableName) {
		checkNotNull("Filter property cannot be null");
		return withFilter(URI.create(property),variableName);
	}

	public Filters withFilter(final URI property, final String variableName) {
		checkNotNull("Filter property cannot be null");
		checkNotNull("Filter variable name cannot be null");
		if(variableName.equals(this.mappings.get(property))) {
			return this;
		}
		checkArgument(!this.mappings.containsKey(property),"Filter property already defined");
		checkArgument(!this.mappings.containsValue(variableName),"Filter variable already defined");
		return
			new Filters(
				ImmutableMap.
					<URI,String>builder().
						putAll(this.mappings).
						put(property,variableName).
						build());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.mappings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean result = false;
		if(obj instanceof Filters) {
			final Filters that=(Filters)obj;
			result=Objects.equals(this.mappings,that.mappings);
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
					addValue(this.mappings).
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

	public static Filters of(final Collection<Filter> filters) {
		final ImmutableMap.Builder<URI, String> builder = ImmutableMap.<URI,String>builder();
		for(final Filter filter:filters) {
			builder.put(filter.property(),filter.variable().name());
		}
		return new Filters(builder.build());
	}

}
