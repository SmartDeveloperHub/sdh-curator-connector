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
import org.smartdeveloperhub.curator.connector.ProtocolFactory.BrokerBuilder;
import org.smartdeveloperhub.curator.protocol.Broker;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public final class BrokerParser {

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.loadResource(BrokerParser.class,"broker.sparql"));

	public static Broker fromModel(Model model, Resource resource) {
		QuerySolutionMap parameters = new QuerySolutionMap();
		parameters.add("broker", resource);
		QueryExecution queryExecution = null;
		try {
			queryExecution=QueryExecutionFactory.create(QUERY, model);
			queryExecution.setInitialBinding(parameters);
			ResultSet results = queryExecution.execSelect();
			List<Broker> result=processResult(results);
			return selectResult(result,resource);
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	private static Broker selectResult(List<Broker> result, Resource resource) {
		if(result.isEmpty()) {
			return null;
		} else if(result.size()>1) {
			throw new IllegalArgumentException("Too much many definitions for resource '"+resource+"'");
		} else {
			return result.get(0);
		}
	}

	private static List<Broker> processResult(ResultSet results) {
		List<Broker> result=Lists.newArrayList();
		for(; results.hasNext();) {
			QuerySolution solution = results.nextSolution();
			BrokerBuilder builder = ProtocolFactory.newBroker();
			updateHost(solution, builder);
			updatePort(solution, builder);
			updateVirtualHost(solution, builder);
			result.add(builder.build());
		}
		return result;
	}

	private static void updateVirtualHost(QuerySolution solution, BrokerBuilder builder) {
		Literal virtualHost=solution.getLiteral("virtualHost");
		if(virtualHost!=null) {
			builder.withVirtualHost(virtualHost.getLexicalForm());
		}
	}

	private static void updatePort(QuerySolution solution, BrokerBuilder builder) {
		Literal port=solution.getLiteral("port");
		if(port!=null) {
			String rawPort = port.getLexicalForm();
			try {
				builder.withPort(Integer.parseInt(rawPort));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(rawPort+" is not a valid integer");
			}
		}
	}

	private static void updateHost(QuerySolution solution, BrokerBuilder builder) {
		Literal host=solution.getLiteral("host");
		if(host!=null) {
			builder.withHost(host.getLexicalForm());
		}
	}

}