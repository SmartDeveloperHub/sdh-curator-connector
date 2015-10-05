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
import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentResponseBuilder;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class EnrichmentResponseParser extends ResponseParser<EnrichmentResponse, EnrichmentResponseBuilder> {

	private final class EnrichmentResponseWorker extends ResponseWorker {

		@Override
		public void parse() {
			super.parse();
			updateTargetResource();
			updateAdditionTarget();
			updateRemovalTarget();
		}

		private void updateTargetResource() {
			Resource targetResource = resource("targetResource", "curator:targetResource",false);
			try {
				this.builder.withTargetResource(targetResource.getURI());
			} catch (ValidationException e) {
				failConversion("curator:targetResource",e);
			}
		}

		private void updateAdditionTarget() {
			Resource additionTarget = resource("additionTarget", "curator:additionTarget",true);
			if(additionTarget!=null) {
				try {
					this.builder.withAdditionTarget(additionTarget.getURI());
				} catch (ValidationException e) {
					failConversion("curator:additionTarget",e);
				}
			}
		}

		private void updateRemovalTarget() {
			Resource removalTarget = resource("removalTarget", "curator:removalTarget",true);
			if(removalTarget!=null) {
				try {
					this.builder.withRemovalTarget(removalTarget.getURI());
				} catch (ValidationException e) {
					failConversion("curator:removalTarget",e);
				}
			}
		}

	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.
				loadResource(
					EnrichmentResponseParser.class,
					"enrichmentResponse.sparql"));

	private EnrichmentResponseParser(Model model, Resource resource) {
		super(model, resource);
	}

	@Override
	protected ResponseWorker solutionParser() {
		return new EnrichmentResponseWorker();
	}

	@Override
	protected String parsedType() {
		return "curator:EnrichmentResponse";
	}

	@Override
	protected Query parserQuery() {
		return QUERY;
	}

	@Override
	protected String targetVariable() {
		return "enrichmentResponse";
	}

	@Override
	protected EnrichmentResponseBuilder newBuilder() {
		return ProtocolFactory.newEnrichmentResponse();
	}

	static EnrichmentResponse fromModel(Model model, Resource resource) {
		return new EnrichmentResponseParser(model, resource).parse();
	}

}
