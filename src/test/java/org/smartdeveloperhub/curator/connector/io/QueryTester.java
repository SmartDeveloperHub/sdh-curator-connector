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

import java.io.StringReader;
import java.util.Iterator;

import org.junit.Test;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

public class QueryTester {

	private Model loadData(final String data) {
		final Model model=ModelFactory.createDefaultModel();
		final StringReader in = new StringReader(ResourceUtil.loadResource(data));
		model.read(in, "urn:","TURTLE");
		return model;
	}

	@Test
	public void testQuery() {
		final Model model = loadData("/data/brokers.ttl");
		final Query query =
			QueryFactory.
				create(
					ResourceUtil.loadResource("broker.sparql"));
		final ResIterator iterator=
			model.
				listSubjectsWithProperty(
					model.createProperty(RDF.TYPE),
					model.createResource(AMQP.BROKER_TYPE));
		while(iterator.hasNext()) {
			final Resource resource = iterator.next();
			final QuerySolutionMap querySolutionMap = new QuerySolutionMap();
			querySolutionMap.add("broker", resource);
			QueryExecution queryExecution = null;
			try {
				System.out.println("Trying "+resource);
				queryExecution = QueryExecutionFactory.create(query, model);
				queryExecution.setInitialBinding(querySolutionMap);
				final ResultSet results = queryExecution.execSelect();
				for(; results.hasNext();) {
					System.out.println("Solution found: ");
					final QuerySolution solution = results.nextSolution();
					final Iterator<String> varNames = solution.varNames();
					while(varNames.hasNext()) {
						final String var=varNames.next();
						System.out.println(" - "+var+" : "+solution.get(var));
					}
				}
			} finally {
				if (queryExecution != null) {
					queryExecution.close();
				}
			}
		}
	}

}
