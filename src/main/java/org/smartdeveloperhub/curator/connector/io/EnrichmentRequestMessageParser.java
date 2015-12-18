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

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.EnrichmentRequestMessageBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Variable;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class EnrichmentRequestMessageParser extends RequestMessageParser<EnrichmentRequestMessage, EnrichmentRequestMessageBuilder> {

	private final class EnrichmentRequestWorker extends RequestWorker {

		@Override
		public void parse() {
			super.parse();
			Resource targetResource=
				mandatory(
					new ResourceProvider<Resource>("targetResource","curator:targetResource") {
						@Override
						protected Resource consumeResource(EnrichmentRequestMessageBuilder builder, Resource resource) {
							builder.withTargetResource(resource.getURI());
							return resource;
						}
					}
				);
			List<Variable> variables = parseFilters();
			parseConstraints(targetResource, variables);
		}

		private void parseConstraints(Resource targetResource, List<Variable> variables) {
			List<Constraint> constraints=ConstraintParser.fromModel(model(),targetResource,variables);
			for(Constraint constraint:constraints) {
				builder().withConstraint(constraint);
			}
		}

		private List<Variable> parseFilters() {
			List<Variable> variables=Lists.newArrayList();
			List<Filter> filters = FilterParser.fromModel(model(), resource());
			for(Filter filter:filters) {
				builder().withFilter(filter);
				variables.add(filter.variable());
			}
			return variables;
		}

	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.
				loadResource(
					EnrichmentRequestMessageParser.class,
					"enrichmentRequest.sparql"));

	private EnrichmentRequestMessageParser(Model model, Resource resource) {
		super(model, resource, "curator:EnrichmentRequest", "enrichmentRequest", QUERY);
	}

	@Override
	protected RequestWorker solutionParser() {
		return new EnrichmentRequestWorker();
	}

	@Override
	protected EnrichmentRequestMessageBuilder newBuilder() {
		return ProtocolFactory.newEnrichmentRequestMessage();
	}

	static EnrichmentRequestMessage fromModel(Model model, Resource resource) {
		return new EnrichmentRequestMessageParser(model, resource).parse();
	}

}
