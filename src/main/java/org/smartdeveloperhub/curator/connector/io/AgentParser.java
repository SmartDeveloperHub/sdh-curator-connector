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
import org.smartdeveloperhub.curator.connector.ProtocolFactory.AgentBuilder;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.Agent;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class AgentParser extends Parser<Agent,AgentBuilder>{

	private final class AgentWorker extends Worker {

		@Override
		protected void parse() {
			try {
				Literal host=literal("agentId","curator:agentId",false);
				this.builder.withAgentId(host.getLexicalForm());
			} catch (ValidationException e) {
				failConversion("curator:agentId", e);;
			}
		}

	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.loadResource(AgentParser.class,"agent.sparql"));

	private AgentParser(Model model, Resource resource) {
		super(model, resource);
	}

	@Override
	protected Worker solutionParser() {
		return new AgentWorker();
	}

	@Override
	protected String parsedType() {
		return "foaf:Agent";
	}

	@Override
	protected Query parserQuery() {
		return QUERY;
	}

	@Override
	protected String targetVariable() {
		return "agent";
	}

	@Override
	protected AgentBuilder newBuilder() {
		return ProtocolFactory.newAgent();
	}

	static Agent fromModel(Model model, Resource resource) {
		return new AgentParser(model, resource).parse();
	}

}