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
package org.smartdeveloperhub.curator.connector.rdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.StringReader;
import java.util.Iterator;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;

import com.google.common.base.MoreObjects;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class SparqlFunctionsTest {

	private static Model loadData(String data) {
		Model model=ModelFactory.createDefaultModel();
		StringReader in = new StringReader(ResourceUtil.loadResource(data));
		model.read(in, "urn:","TURTLE");
		return model;
	}

	private static class Triple {

		private final boolean equal;
		private final boolean lowerThan;
		private final boolean greaterThan;

		private Triple(boolean equal, boolean lowerThan, boolean greaterThan) {
			this.equal = equal;
			this.lowerThan = lowerThan;
			this.greaterThan = greaterThan;
		}

		@Override
		public boolean equals(Object obj) {
			Triple that=(Triple)obj;
			return
				this.equal==that.equal &&
				this.lowerThan==that.lowerThan &&
				this.greaterThan==that.greaterThan ;
		}

		public static Triple create(QuerySolution solution) {
			return
				new Triple(
					getVariable(solution, "equal"),
					getVariable(solution, "lowerThan"),
					getVariable(solution, "greaterThan")
				);
		}

		private static boolean getVariable(QuerySolution solution, String varName) {
			final Literal literal = solution.getLiteral(varName);
			if(literal==null) {
				System.out.println("Could not find "+varName);
				return false;
			}
			return literal.getBoolean();
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("equal",this.equal).
						add("lowerThan",this.lowerThan).
						add("greaterThan",this.greaterThan).
						toString();
		}

	}

	private abstract static class DateTimeTest {
		public final void test(String dataSource) {
			SparqlFunctions.enable();
			Model model = loadData(dataSource);
			Query query =
				QueryFactory.
					create(
						ResourceUtil.loadResource("queries/functions.sparql"));
			QueryExecution queryExecution = null;
			try {
				queryExecution = QueryExecutionFactory.create(query, model);
				ResultSet results = queryExecution.execSelect();
				for(; results.hasNext();) {
					QuerySolution solution = results.nextSolution();
					String caseId=solution.getResource("case").getURI();
					Triple triple=Triple.create(solution);
					evaluate(caseId, triple);
				}
			} finally {
				if (queryExecution != null) {
					queryExecution.close();
				}
			}
		}

		protected abstract void evaluate(String caseId, Triple triple);

	}

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(SparqlFunctions.class),equalTo(true));
	}

	@Test
	public void testComplexValidation() {
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

	@Test
	public void testDatetimes() {
		new DateTimeTest() {
			@Override
			protected void evaluate(String caseId, Triple triple) {
				System.out.println(caseId+" : "+triple);
			}
		}.test("data/functions/datetimes.ttl");
	}

	@Test
	public void testNonDatetimes() {
		new DateTimeTest() {
			@Override
			protected void evaluate(String caseId, Triple triple) {
				assertThat(triple.equal,equalTo(false));
				assertThat(triple.lowerThan,equalTo(false));
				assertThat(triple.greaterThan,equalTo(false));
			}
		}.test("data/functions/non-datetimes.ttl");
	}

	@Test
	public void testBadDatetimes() {
		new DateTimeTest() {
			@Override
			protected void evaluate(String caseId, Triple triple) {
				assertThat(triple.equal,equalTo(false));
				assertThat(triple.lowerThan,equalTo(false));
				assertThat(triple.greaterThan,equalTo(false));
			}
		}.test("data/functions/bad-datetimes.ttl");
	}

}
