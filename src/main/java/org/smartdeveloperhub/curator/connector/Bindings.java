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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Value;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Maps;

public class Bindings implements Iterable<Binding> {

	public static final class BindingValueBuilder {

		private final Bindings constraints;
		private final URI property;

		private BindingValueBuilder(final Bindings constraints,final URI property) {
			this.constraints = constraints;
			this.property = property;
		}

		private Bindings andLiteralValue(final Object value, final URI datatype, final String language) {
			return
				andValue(
					ProtocolFactory.
						newLiteral().
							withLexicalForm(value).
							withDatatype(datatype).
							withLanguage(language).
							build());
		}

		public Bindings andValue(final Value value) {
			return this.constraints.append(this.property,value);
		}

		public Bindings andVariable(final String name) {
			return andValue(ProtocolFactory.newVariable(name));
		}

		public Bindings andResource(final String name) {
			return andResource(URI.create(name));
		}

		public Bindings andResource(final URI name) {
			return andValue(ProtocolFactory.newResource(name));
		}

		public Bindings andLiteral(final Object value) {
			return andLanguageLiteral(value.toString(),null);
		}

		public Bindings andTypedLiteral(final Object value, final String dataype) {
			return andTypedLiteral(value,URI.create(dataype));
		}

		public Bindings andTypedLiteral(final Object value, final URI datatype) {
			return andLiteralValue(value,datatype,null);
		}

		public Bindings andLanguageLiteral(final String value, final String language) {
			return andLiteralValue(value,null,language);
		}

	}

	private final Map<URI,Value> propertyValues;

	private Bindings(final Map<URI,Value> propertyValues) {
		this.propertyValues=propertyValues;
	}

	private Bindings() {
		this(Maps.<URI,Value>newLinkedHashMap());
	}

	private Bindings append(final URI property, final Value value) {
		final LinkedHashMap<URI, Value> newPropertyValues = Maps.newLinkedHashMap(this.propertyValues);
		newPropertyValues.put(property, value);
		return new Bindings(newPropertyValues);
	}

	private ImmutableList<Binding> toList() {
		final Builder<Binding> builder = ImmutableList.<Binding>builder();
		for(final Entry<URI, Value> binding:this.propertyValues.entrySet()) {
			builder.add(
				ProtocolFactory.
					newBinding().
						withProperty(binding.getKey()).
						withValue(binding.getValue()).
						build());
		}
		return builder.build();
	}

	public Set<URI> properties() {
		return this.propertyValues.keySet();
	}

	public Value value(final URI property) {
		return this.propertyValues.get(property);
	}

	public BindingValueBuilder withProperty(final String property) {
		return withProperty(URI.create(property));
	}

	public BindingValueBuilder withProperty(final URI property) {
		return new BindingValueBuilder(this,property);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.propertyValues);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean result = false;
		if(obj instanceof Bindings) {
			final Bindings that=(Bindings)obj;
			result=Objects.equals(this.propertyValues,that.propertyValues);
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
				toStringHelper(Bindings.class).
					addValue(this.propertyValues).
					toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Binding> iterator() {
		return toList().iterator();
	}

	public static Bindings newInstance() {
		return new Bindings();
	}

}