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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.io;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.Variable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

final class EnrichmentUtil {

	static final class Builder {

		private static final String DEFAULT_BLANK_NODE_PREFIX = "bn";

		private final Set<String> filterVariables=Sets.newLinkedHashSet();
		private final Set<String> constraintVariables=Sets.newLinkedHashSet();
		private final Set<String> bindingVariables=Sets.newLinkedHashSet();

		private String blankNodePrefix;

		Builder withFilters(List<Filter> filters) {
			if(filters!=null ) {
				this.filterVariables.clear();
				for(Filter filter:filters) {
					this.filterVariables.add(filter.variable().name());
				}
			}
			return this;
		}

		Builder withConstraints(List<Constraint> constraints) {
			if(constraints!=null ) {
				this.constraintVariables.clear();
				for(Constraint constraint:constraints) {
					checkValue(this.constraintVariables,constraint.target());
					checkBindings(this.constraintVariables,constraint.bindings());
				}
			}
			return this;
		}

		Builder withBindings(final List<Binding> bindings) {
			checkBindings(this.bindingVariables,bindings);
			return this;
		}

		void checkBindings(Set<String> reserved, List<Binding> bindings) {
			if(bindings!=null) {
				for(Binding binding:bindings) {
					checkValue(reserved,binding.value());
				}
			}
		}

		Builder withBlankNodePrefix(String blankNodePrefix) {
			this.blankNodePrefix = blankNodePrefix;
			return this;
		}

		EnrichmentUtil build() {
			return new EnrichmentUtil(
				blankNodePrefix(),
				ImmutableSet.
					<String>builder().
						addAll(this.filterVariables).
						addAll(this.constraintVariables).
						addAll(this.bindingVariables).
						build());
		}

		private String blankNodePrefix() {
			return
				this.blankNodePrefix==null?
					DEFAULT_BLANK_NODE_PREFIX:
					this.blankNodePrefix.isEmpty()?
						DEFAULT_BLANK_NODE_PREFIX:
						this.blankNodePrefix;
		}

		private void checkValue(Set<String> reserved, final Value target) {
			if(target instanceof Variable) {
				Variable variable=(Variable)target;
				reserved.add(variable.name());
			}
		}

	}

	private final Set<String> reserved;
	private final Map<String,String> overridenPreferences;
	private final String blankNodePrefix;
	private int overrides;

	private EnrichmentUtil(String blankNodePrefix, Set<String> reserved) {
		this.blankNodePrefix = blankNodePrefix;
		this.reserved = reserved;
		this.overridenPreferences=Maps.newLinkedHashMap();
		this.overrides=0;
	}

	String blankNode(String preferred) {
		String result=this.overridenPreferences.get(preferred);
		if(result==null) {
			if(this.reserved.contains(preferred)) {
				this.overrides++;
				result=this.blankNodePrefix+overrides;
				this.overridenPreferences.put(preferred,result);
			} else {
				result=preferred;
			}
		}
		return result;
	}

	static Builder builder() {
		return new Builder();
	}

}
