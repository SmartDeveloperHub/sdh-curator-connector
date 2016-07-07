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
package org.smartdeveloperhub.curator.connector.io;


import java.util.List;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.EnrichmentResponseMessageBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class EnrichmentResponseMessageParser extends ResponseMessageParser<EnrichmentResponseMessage, EnrichmentResponseMessageBuilder> {

	private final class EnrichmentResponseWorker extends ResponseWorker {

		@Override
		public void parse() {
			super.parse();
			updateTargetResource();
			Resource additionTarget=findAdditionTarget();
			if(additionTarget!=null) {
				List<Binding> bindings=BindingParser.fromModel(model(),additionTarget);
				for(Binding binding:bindings) {
					builder().withAddition(binding);
				}
			}
			Resource removalTarget=findRemovalTarget();
			if(removalTarget!=null) {
				List<Binding> bindings=BindingParser.fromModel(model(),removalTarget);
				for(Binding binding:bindings) {
					builder().withRemoval(binding);
				}
			}
		}

		private void updateTargetResource() {
			mandatory(
				new ResourceConsumer("targetResource","curator:targetResource") {
					@Override
					protected void consumeResource(EnrichmentResponseMessageBuilder builder, Resource resource) {
						builder.withTargetResource(resource.getURI());
					}
				}
			);
		}

		private Resource findAdditionTarget() {
			return
				optional(
					new ResourceProvider<Resource>("additionTarget","curator:additionTarget") {
						@Override
						protected Resource consumeResource(EnrichmentResponseMessageBuilder builder, Resource resource) {
							return resource;
						}
					}
				);
		}

		private Resource findRemovalTarget() {
			return
				optional(
					new ResourceProvider<Resource>("removalTarget","curator:removalTarget") {
						@Override
						protected Resource consumeResource(EnrichmentResponseMessageBuilder builder, Resource resource) {
							return resource;
						}
					}
				);
		}

	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.
				loadResource(
					EnrichmentResponseMessageParser.class,
					"enrichmentResponse.sparql"));

	private EnrichmentResponseMessageParser(Model model, Resource resource) {
		super(model, resource, "curator:EnrichmentResponse", "enrichmentResponse", QUERY);
	}

	@Override
	protected ResponseWorker solutionParser() {
		return new EnrichmentResponseWorker();
	}

	@Override
	protected EnrichmentResponseMessageBuilder newBuilder() {
		return ProtocolFactory.newEnrichmentResponseMessage();
	}

	static EnrichmentResponseMessage fromModel(Model model, Resource resource) {
		return new EnrichmentResponseMessageParser(model, resource).parse();
	}

}
