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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.BindingBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.Binding;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

final class BindingParser extends Parser<Binding, BindingBuilder> {

	private final class BindingWorker extends Worker {

		@Override
		public void parse() {
			LOGGER.trace("Processing binding for {}",resource());
			mandatory(
				new ResourceConsumer("bindingProperty") {
					@Override
					protected void consumeResource(BindingBuilder builder, Resource resource) {
						LOGGER.trace("- Property {}",resource);
						builder.withProperty(resource.getURI());
					}
				}
			);
			mandatory(
				new NodeConsumer("bindingValue") {
					@Override
					protected void consumeNode(BindingBuilder builder, RDFNode node) {
						populateBindingValue(builder, node);
					}
				}
			);
		}
		private void populateBindingValue(BindingBuilder builder, RDFNode node) {
			LOGGER.trace("- Value {}",node);
			if(node.isLiteral()) {
				Literal literal=node.asLiteral();
				builder.
					withValue(
						ProtocolFactory.
							newLiteral().
								withLexicalForm(literal.getLexicalForm()).
								withDatatype(literal.getDatatypeURI()).
								withLanguage(literal.getLanguage()));
			} else if(node.isURIResource()) {
				builder.withValue(ProtocolFactory.newResource(node.asResource().getURI()));
			} else {
				builder.withValue(ProtocolFactory.newVariable(node.asResource().getId().getLabelString()));
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BindingParser.class);

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.
				loadResource(
					BindingParser.class,
					"bindings.sparql"));

	private BindingParser(Model model, Resource resource) {
		super(model, resource,"curator:Variable","target",QUERY);
	}

	@Override
	protected Worker solutionParser() {
		return new BindingWorker();
	}

	@Override
	protected BindingBuilder newBuilder() {
		return ProtocolFactory.newBinding();
	}

	static List<Binding> fromModel(Model model, Resource resource) {
		return new BindingParser(model, resource).parseCollection();
	}

}
