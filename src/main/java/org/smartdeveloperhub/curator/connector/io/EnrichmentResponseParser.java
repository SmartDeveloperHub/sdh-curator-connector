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

import java.util.List;

import org.smartdeveloperhub.curator.connector.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentResponseBuilder;
import org.smartdeveloperhub.curator.connector.rdf.SparqlFunctions;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
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

	static {
		SparqlFunctions.enable();
	}

	EnrichmentResponseParser(Model model, Resource resource) {
		super(model, resource);
	}

	@Override
	protected ResponseWorker createWorker() {
		return new EnrichmentResponseWorker();
	}

	static EnrichmentResponse fromModel(Model model, Resource resource) {
		QuerySolutionMap parameters = new QuerySolutionMap();
		parameters.add("enrichmentResponse", resource);
		QueryExecution queryExecution = null;
		try {
			queryExecution=QueryExecutionFactory.create(QUERY, model);
			queryExecution.setInitialBinding(parameters);
			ResultSet results = queryExecution.execSelect();
			List<EnrichmentResponse> result=processResult(new EnrichmentResponseParser(model, resource),results);
			return selectResult(result,resource);
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	private static EnrichmentResponse selectResult(List<EnrichmentResponse> result, Resource resource) {
		if(result.isEmpty()) {
			return null;
		} else if(result.size()>1) {
			throw new IllegalArgumentException("Too many EnrichmentResponse definitions for resource '"+resource+"'");
		} else {
			return result.get(0);
		}
	}

	private static List<EnrichmentResponse> processResult(EnrichmentResponseParser parser,ResultSet results) {
		List<EnrichmentResponse> result=Lists.newArrayList();
		for(; results.hasNext();) {
			QuerySolution solution = results.nextSolution();
			EnrichmentResponseBuilder builder = ProtocolFactory.newEnrichmentResponse();
			parser.parse(solution, builder);
			result.add(builder.build());
		}
		return result;
	}

}
