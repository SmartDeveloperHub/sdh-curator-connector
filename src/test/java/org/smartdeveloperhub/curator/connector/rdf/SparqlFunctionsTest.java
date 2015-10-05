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
package org.smartdeveloperhub.curator.connector.rdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.StringReader;
import java.util.Iterator;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class SparqlFunctionsTest {

	private Model loadData(String data) {
		Model model=ModelFactory.createDefaultModel();
		StringReader in = new StringReader(ResourceUtil.loadResource(data));
		model.read(in, "urn:","TURTLE");
		return model;
	}

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(SparqlFunctions.class),equalTo(true));
	}

	@Test
	public void testGreaterThan() {
		SparqlFunctions.enable();
		Model model = loadData("data/validation.ttl");
		Query query =
			QueryFactory.
				create(
					ResourceUtil.loadResource("queries/validation.sparql"));
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
