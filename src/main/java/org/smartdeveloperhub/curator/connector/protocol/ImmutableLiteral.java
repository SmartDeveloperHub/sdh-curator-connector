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
package org.smartdeveloperhub.curator.connector.protocol;

import java.net.URI;
import java.util.Objects;

import org.smartdeveloperhub.curator.protocol.Literal;

import com.google.common.base.MoreObjects;

final class ImmutableLiteral implements Literal {

	private final String lexicalForm;
	private final URI datatype;
	private final String language;

	ImmutableLiteral(String lexicalForm, URI datatype, String language) {
		this.lexicalForm = lexicalForm;
		this.datatype = datatype;
		this.language = language;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String lexicalForm() {
		return this.lexicalForm;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URI datatype() {
		return this.datatype;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String language() {
		return this.language;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.lexicalForm,this.datatype,this.language);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof Literal) {
			Literal that=(Literal)obj;
			result=
				Objects.equals(this.lexicalForm,that.lexicalForm()) &&
				Objects.equals(this.datatype,that.datatype()) &&
				Objects.equals(this.language,that.language());
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
					add("lexicalForm",this.lexicalForm).
					add("datatype",this.datatype).
					add("language",this.language).
					toString();
	}

}