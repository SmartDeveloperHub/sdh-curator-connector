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

import org.smartdeveloperhub.curator.connector.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentRequestBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class EnrichmentRequestParser extends MessageParser<EnrichmentRequest, EnrichmentRequestBuilder> {

	private final class EnrichmentRequestWorker extends MessageWorker {

		@Override
		public void parse() {
			super.parse();
			mandatory(
				new ResourceConsumer("targetResource","curator:targetResource") {
					@Override
					protected void consumeResource(EnrichmentRequestBuilder builder, Resource resource) {
						builder.withTargetResource(resource.getURI());
					}
				}
			);
		}

	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.
				loadResource(
					EnrichmentRequestParser.class,
					"enrichmentRequest.sparql"));

	private EnrichmentRequestParser(Model model, Resource resource) {
		super(model, resource, "curator:EnrichmentRequest", "enrichmentRequest", QUERY);
	}

	@Override
	protected MessageWorker solutionParser() {
		return new EnrichmentRequestWorker();
	}

	@Override
	protected EnrichmentRequestBuilder newBuilder() {
		return ProtocolFactory.newEnrichmentRequest();
	}

	static EnrichmentRequest fromModel(Model model, Resource resource) {
		return new EnrichmentRequestParser(model, resource).parse();
	}

}
