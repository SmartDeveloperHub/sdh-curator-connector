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
package org.smartdeveloperhub.curator.connector.protocol;

import java.net.URI;
import java.util.Objects;

import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Value;

import com.google.common.base.MoreObjects;

final class ImmutableBinding implements Binding {

	private final Value value;
	private final URI property;

	ImmutableBinding(URI property, Value value) {
		this.property = property;
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI property() {
		return this.property;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value value() {
		return this.value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.property,this.value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof Binding) {
			Binding that=(Binding)obj;
			result=
				Objects.equals(this.property,that.property()) &&
				Objects.equals(this.value,that.value());
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
					omitNullValues().
					add("property",this.property).
					add("value",this.value).
					toString();
	}

}