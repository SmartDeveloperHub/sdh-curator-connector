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
package org.smartdeveloperhub.curator.connector.io;

import java.net.URI;

import org.smartdeveloperhub.curator.connector.rdf.ModelHelper;
import org.smartdeveloperhub.curator.connector.rdf.PropertyHelper;
import org.smartdeveloperhub.curator.connector.rdf.ResourceHelper;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.Resource;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;

import com.google.common.collect.ImmutableList;

final class BindingSerializer {

	private static final ImmutableList<String>
	PROTECTED_NAMESPACES=ImmutableList.of(CURATOR.NAMESPACE,AMQP.NAMESPACE,TYPES.NAMESPACE);
	private final ModelHelper helper;

	private BindingSerializer(ModelHelper helper) {
		this.helper = helper;
	}

	private void serialize(NamedValue target, URI property, Value value) {
		final PropertyHelper propertyHelper=
				resourceHelper(target).
					property(property);
		if(value instanceof Resource) {
			final URI name = ((Resource)value).name();
			propertyHelper.withResource(name);
		} else if(value instanceof Variable) {
			final String name = ((Variable)value).name();
			propertyHelper.withBlankNode(name);
			this.helper.blankNode(name).type(CURATOR.VARIABLE_TYPE);
		} else { // MUST BE LITERAL
			Literal literal=(Literal)value;
			propertyHelper.
				withTypedLiteral(
					literal.lexicalForm(),
					literal.datatype());
		}
	}

	private ResourceHelper resourceHelper(NamedValue target) {
		ResourceHelper resourceHelper=null;
		if(target instanceof Resource) {
			resourceHelper=this.helper.resource(((Resource)target).name());
		} else {
			resourceHelper=this.helper.blankNode(((Variable)target).name());
			resourceHelper.type(CURATOR.VARIABLE_TYPE);
		}
		return resourceHelper;
	}

	private void verifySerializability(Binding binding) {
		verifyNotProtected("property", binding.property());
		if(binding.property().toString().equals(RDF.TYPE)) {
			final Value value = binding.value();
			if(value instanceof Resource) {
				Resource resource=(Resource)value;
				verifyNotProtected("value",resource.name());
			}
		}
	}

	private void verifyNotProtected(final String type, final URI value) {
		for(String protectedNamespace:PROTECTED_NAMESPACES) {
			if(value.toString().startsWith(protectedNamespace)) {
				throw new ForbiddenBindingException(value,type,protectedNamespace);
			}
		}
	}

	void serialize(NamedValue target, Binding binding) {
		verifySerializability(binding);
		serialize(target,binding.property(),binding.value());
	}

	static BindingSerializer newInstance(ModelHelper helper) {
		return new BindingSerializer(helper);
	}

}
