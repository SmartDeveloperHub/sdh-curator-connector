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
package org.smartdeveloperhub.curator.connector.io;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

final class BindingValidatorFactory {

	private BindingValidatorFactory() {
	}

	static BindingValidator<Resource> resourceValidator() {
		return new BindingValidator<Resource>() {
			@Override
			public boolean isValid(RDFNode node) {
				return node.isResource();
			}
			@Override
			public Resource cast(RDFNode node) {
				return node.asResource();
			}
			@Override
			public String toString() {
				return "resource";
			}
		};
	}

	static BindingValidator<Literal> literalValidator() {
		return new BindingValidator<Literal>() {
			@Override
			public boolean isValid(RDFNode node) {
				return node.isLiteral();
			}
			@Override
			public Literal cast(RDFNode node) {
				return node.asLiteral();
			}
			@Override
			public String toString() {
				return "literal";
			}
		};
	}
	static BindingValidator<RDFNode> nodeValidator() {
		return new BindingValidator<RDFNode>() {
			@Override
			public boolean isValid(RDFNode node) {
				return true;
			}
			@Override
			public RDFNode cast(RDFNode node) {
				return node;
			}
		};
	}
}