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

import org.smartdeveloperhub.curator.connector.ProtocolFactory.Builder;

import com.hp.hpl.jena.query.QuerySolution;
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

	final void parse(QuerySolution solution, B builder) {
		createWorker().
			withSolution(solution).
			withBuilder(builder).
			parse();
	}

	protected abstract Worker createWorker();

}