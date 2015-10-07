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
import org.smartdeveloperhub.curator.connector.rdf.ResourceHelper;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.Resource;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.Variable;

final class BindingSerializer {

	private final ModelHelper helper;
	private final NamedValue  target;

	private BindingSerializer(ModelHelper helper, NamedValue target) {
		this.helper = helper;
		this.target = target;
	}

	private void serialize(URI property, Value value) {
		if(value instanceof Resource) {
			resourceHelper().
				property(property).
					withResource(((Resource)value).name());
		} else if(value instanceof Variable) {
			resourceHelper().
				property(property).
					withBlankNode(((Variable)value).name());
		} else { // MUST BE LITERAL
			Literal literal=(Literal)value;
			resourceHelper().
				property(property).
					withTypedLiteral(
						literal.lexicalForm(),
						literal.datatype());
		}
	}

	private ResourceHelper resourceHelper() {
		ResourceHelper resourceHelper=null;
		if(this.target instanceof Resource) {
			resourceHelper=this.helper.resource(((Resource)this.target).name());
		} else {
			resourceHelper=this.helper.blankNode(((Variable)this.target).name());
		}
		return resourceHelper;
	}

	void serialize(Binding binding) {
		serialize(binding.property(),binding.value());
	}

	static BindingSerializer newInstance(ModelHelper helper, NamedValue target) {
		return new BindingSerializer(helper,target);
	}

}
