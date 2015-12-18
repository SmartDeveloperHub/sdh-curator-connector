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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.1.0
 *   Bundle      : sdh-curator-connector-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.ConstraintBuilder;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.Value;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class Constraints implements Iterable<Constraint> {

	public static final class TargetedConstraints extends Constraints { // NOSONAR

		private final NamedValue target;

		private TargetedConstraints(final Constraints constraints, final NamedValue target) {
			super(constraints);
			this.target = target;
		}

		public BindingValueBuilder withProperty(final String property) {
			return withProperty(URI.create(property));
		}

		public BindingValueBuilder withProperty(final URI property) {
			return new BindingValueBuilder(this,this.target,property);
		}

	}

	public static final class BindingValueBuilder {

		private final Constraints constraints;
		private final NamedValue target;
		private final URI property;

		private BindingValueBuilder(final Constraints constraints, final NamedValue target, final URI property) {
			this.constraints = constraints;
			this.target = target;
			this.property = property;
		}

		private TargetedConstraints appendValue(final Value value) {
			this.constraints.add(this.target,this.property,value);
			return new TargetedConstraints(this.constraints,this.target);
		}

		private TargetedConstraints appendLiteralValue(final Object value, final URI datatype, final String language) {
			return
				appendValue(
					ProtocolFactory.
						newLiteral().
							withLexicalForm(value).
							withDatatype(datatype).
							withLanguage(language).
							build());
		}

		public TargetedConstraints andVariable(final String name) {
			return appendValue(ProtocolFactory.newVariable(name));
		}

		public TargetedConstraints andResource(final String name) {
			return andResource(URI.create(name));
		}

		public TargetedConstraints andResource(final URI name) {
			return appendValue(ProtocolFactory.newResource(name));
		}

		public TargetedConstraints andLiteral(final Object value) {
			return andLanguageLiteral(value.toString(),null);
		}

		public TargetedConstraints andTypedLiteral(final Object value, final String dataype) {
			return andTypedLiteral(value,URI.create(dataype));
		}

		public TargetedConstraints andTypedLiteral(final Object value, final URI datatype) {
			return appendLiteralValue(value,datatype,null);
		}

		public TargetedConstraints andLanguageLiteral(final String value, final String language) {
			return appendLiteralValue(value,null,language);
		}

	}

	public static final class BindingPropertyBuilder {

		protected final Constraints constraints;
		protected final NamedValue target;

		private BindingPropertyBuilder(final Constraints constraints, final NamedValue target) {
			this.constraints = constraints;
			this.target = target;
		}

		public BindingValueBuilder withProperty(final String property) {
			return withProperty(URI.create(property));
		}

		public BindingValueBuilder withProperty(final URI property) {
			return new BindingValueBuilder(this.constraints,this.target,property);
		}

	}

	private Multimap<NamedValue,Binding> bindings;

	private Constraints(final Constraints constraints) {
		this.bindings=constraints.bindings;
	}

	private Constraints() {
		this.bindings=LinkedHashMultimap.create();
	}

	/**
	 * Copy-on-write
	 */
	private void add(final NamedValue target, final URI property, final Value value) {
		this.bindings=LinkedHashMultimap.create(this.bindings);
		this.bindings.put(
			target,
			ProtocolFactory.
				newBinding().
					withProperty(property).
					withValue(value).
					build());
	}

	private ImmutableList<Constraint> toList() {
		final Builder<Constraint> builder = ImmutableList.<Constraint>builder();
		for(final Entry<NamedValue, Collection<Binding>> constraint:this.bindings.asMap().entrySet()) {
			final ConstraintBuilder cb=
				ProtocolFactory.
					newConstraint().
						withTarget(constraint.getKey());
			for(final Binding binding:constraint.getValue()) {
				cb.withBinding(binding);
			}
			builder.add(cb.build());
		}
		return builder.build();
	}

	public BindingPropertyBuilder forVariable(final String name) {
		return new BindingPropertyBuilder(this,ProtocolFactory.newVariable(name));
	}

	public BindingPropertyBuilder forResource(final String name) {
		return forResource(URI.create(name));
	}

	public BindingPropertyBuilder forResource(final URI name) {
		return new BindingPropertyBuilder(this,ProtocolFactory.newResource(name));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.bindings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean result = false;
		if(obj instanceof Constraints) {
			final Constraints that=(Constraints)obj;
			result=Objects.equals(this.bindings,that.bindings);
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
				toStringHelper(Constraints.class).
					addValue(this.bindings).
					toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Constraint> iterator() {
		return toList().iterator();
	}

	public static Constraints newInstance() {
		return new Constraints();
	}

	public static Constraints of(final Collection<Constraint> constraints) {
		final Constraints result=newInstance();
		for(final Constraint constraint:constraints) {
			for(final Binding binding:constraint.bindings()) {
				result.bindings.put(constraint.target(),binding);
			}
		}
		return result;
	}

}