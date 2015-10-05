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

import org.smartdeveloperhub.curator.connector.ProtocolFactory.Builder;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

abstract class Parser<T, B extends Builder<T>> {

	protected abstract class Worker {

		protected B builder;
		private QuerySolution solution;

		protected Worker() {
		}

		private final Worker withBuilder(B builder) {
			this.builder = builder;
			return this;
		}

		private final Worker withSolution(QuerySolution solution) {
			this.solution = solution;
			return this;
		}

		protected abstract void parse();

		protected final Model model() {
			return Parser.this.model;
		}

		protected final Resource resource() {
			return Parser.this.resource;
		}

		protected final Literal literal(String varName, String property, boolean nullable) {
			return acceptResolution(property, nullable, this.solution.getLiteral(varName));
		}

		protected final Resource resource(String varName, String property, boolean nullable) {
			return acceptResolution(property, nullable, this.solution.getResource(varName));
		}

		protected final void failConversion(String property, Throwable e) {
			throw new ConversionException("Could not process "+property+" property for resource '"+Parser.this.resource+"'",e);
		}

		private <V> V acceptResolution(String property, boolean nullable, V value) {
			if(value==null && !nullable) {
				throw new ConversionException("Could not find required property "+property+" for resource '"+Parser.this.resource+"'");
			}
			return value;
		}

	}

	private final Model model;
	private final Resource resource;

	Parser(Model model, Resource resource) {
		this.model = model;
		this.resource = resource;
	}

	final T parse() {
		QuerySolutionMap parameters = new QuerySolutionMap();
		parameters.add(targetVariable(), resource);
		QueryExecution queryExecution=QueryExecutionFactory.create(parserQuery(), model);
		queryExecution.setInitialBinding(parameters);
		try {
			ResultSet results = queryExecution.execSelect();
			List<T> result=processResults(results);
			return firstResult(result,resource,parsedType());
		} finally {
			closeQuietly(queryExecution);
		}
	}

	private List<T> processResults(ResultSet results) {
		List<T> result=Lists.newArrayList();
		for(; results.hasNext();) {
			QuerySolution solution = results.nextSolution();
			B builder = newBuilder();
			solutionParser().
				withSolution(solution).
				withBuilder(builder).
				parse();
			result.add(builder.build());
		}
		return result;
	}

	private T firstResult(List<T> result, Resource resource, String type) {
		if(result.isEmpty()) {
			return null;
		} else if(result.size()==1) {
			return result.get(0);
		}
		throw new ConversionException("Too many "+type+" definitions for resource '"+resource+"'");
	}

	private void closeQuietly(QueryExecution closeable) {
		if(closeable!=null) {
			closeable.close();
		}
	}

	protected abstract String parsedType();

	protected abstract Query parserQuery();

	protected abstract String targetVariable();

	protected abstract B newBuilder();

	protected abstract Worker solutionParser();

}