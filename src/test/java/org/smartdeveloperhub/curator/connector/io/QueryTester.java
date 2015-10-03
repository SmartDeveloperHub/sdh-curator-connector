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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.smartdeveloperhub.curator.connector.rdf.Namespaces;
import org.smartdeveloperhub.curator.connector.rdf.SparqlFunctions;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;

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

	private String loadResource(String resourceName) {
		try {
			InputStream resourceAsStream = getClass().getResourceAsStream(resourceName);
			if(resourceAsStream==null) {
				throw new AssertionError("Could not find resource '"+resourceName+"'");
			}
			return IOUtils.toString(resourceAsStream, Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new AssertionError("Could not load resource '"+resourceName+"'");
		}
	}

	private Model loadData(String data) {
		Model model=ModelFactory.createDefaultModel();
		StringReader in = new StringReader(loadResource(data));
		model.read(in, "urn:","TURTLE");
		return model;
	}

	@Test
	public void testQuery() {
		Model model = loadData("/data/brokers.ttl");
		Query query =
			QueryFactory.
				create(
					loadResource("broker.sparql"));
		ResIterator iterator=
			model.
				listSubjectsWithProperty(
					model.createProperty(Namespaces.rdf("type")),
					model.createResource(AMQP.BROKER_TYPE));
		while(iterator.hasNext()) {
			Resource resource = iterator.next();
			QuerySolutionMap querySolutionMap = new QuerySolutionMap();
			querySolutionMap.add("broker", resource);
			QueryExecution queryExecution = null;
			try {
				System.out.println("Trying "+resource);
				queryExecution = QueryExecutionFactory.create(query, model);
				queryExecution.setInitialBinding(querySolutionMap);
				ResultSet results = queryExecution.execSelect();
				for(; results.hasNext();) {
					System.out.println("Solution found: ");
					QuerySolution solution = results.nextSolution();
					Iterator<String> varNames = solution.varNames();
					while(varNames.hasNext()) {
						String var=varNames.next();
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

	@Test
	public void testValidation() {
		SparqlFunctions.enable();
		Model model = loadData("/validation.ttl");
		Query query =
			QueryFactory.
				create(
					loadResource("/validation.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, model);
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				System.out.println("Solution found: ");
				QuerySolution solution = results.nextSolution();
				Iterator<String> varNames = solution.varNames();
				while(varNames.hasNext()) {
					String var=varNames.next();
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
