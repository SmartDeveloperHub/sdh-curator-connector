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
import org.smartdeveloperhub.curator.connector.ValidationException;

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

		protected abstract class Provider<V> {

			private final String varName;
			private final String propertyName;
			private boolean optional;

			private Provider(String varName, String propertyName) {
				this.varName = varName;
				this.propertyName = propertyName;
			}

			protected void setOptional(boolean optional) {
				this.optional = optional;
			}

			protected final String variableName() {
				return this.varName;
			}

			protected final String propertyName() {
				return this.propertyName;
			}

			protected final boolean isOptional() {
				return this.optional;
			}

			protected abstract V provide(QuerySolution solution);

		}

		protected abstract class Consumer extends Provider<Void> {

			private Consumer(String varName, String propertyName) {
				super(varName,propertyName);
			}

			@Override
			protected Void provide(QuerySolution solution) {
				consume(solution);
				return null;
			}

			protected abstract void consume(QuerySolution solution);

		}

		protected abstract class LiteralConsumer extends Consumer {

			protected LiteralConsumer(String varName, String propertyName) {
				super(varName,propertyName);
			}

			@Override
			protected final void consume(QuerySolution solution) {
				Literal literalValue=literal(super.variableName(), super.propertyName(), super.isOptional());
				if(literalValue!=null) {
					try {
						consumeLiteral(Worker.this.builder,literalValue);
					} catch (ValidationException e) {
						throw failConversion(super.propertyName(),e);
					}
				}
			}

			protected abstract void consumeLiteral(B builder, Literal literal);

		}

		protected abstract class ResourceConsumer extends Consumer {

			protected ResourceConsumer(String varName, String propertyName) {
				super(varName,propertyName);
			}

			protected ResourceConsumer(String varName) {
				super(varName,"");
			}

			@Override
			protected final void consume(QuerySolution solution) {
				Resource resourceValue=resource(super.variableName(), super.propertyName(), super.isOptional());
				if(resourceValue!=null) {
					try {
						consumeResource(Worker.this.builder,resourceValue);
					} catch (ValidationException e) {
						throw failConversion(super.propertyName(),e);
					}
				}
			}

			protected abstract void consumeResource(B builder, Resource resource);

		}

		protected abstract class ResourceProvider<V> extends Provider<V> {

			protected ResourceProvider(String varName, String propertyName) {
				super(varName,propertyName);
			}

			@Override
			protected final V provide(QuerySolution solution) {
				Resource resourceValue=resource(super.variableName(), super.propertyName(), super.isOptional());
				if(resourceValue!=null) {
					try {
						return consumeResource(Worker.this.builder,resourceValue);
					} catch (ValidationException e) {
						throw failConversion(super.propertyName(),e);
					}
				}
				return null;
			}

			protected abstract V consumeResource(B builder, Resource resource);

		}

		private B builder;

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

		protected final B builder() {
			return this.builder;
		}

		protected final void optional(Consumer consumer) {
			consumer.setOptional(true);
			consumer.provide(this.solution);
		}

		protected final void mandatory(Consumer consumer) {
			consumer.setOptional(false);
			consumer.provide(this.solution);
		}

		protected final <V> V mandatory(Provider<V> provider) {
			provider.setOptional(false);
			return provider.provide(this.solution);
		}

		private Literal literal(String varName, String property, boolean nullable) {
			return acceptResolution(property, nullable, this.solution.getLiteral(varName));
		}

		private Resource resource(String varName, String property, boolean nullable) {
			return acceptResolution(property, nullable, this.solution.getResource(varName));
		}

		private ConversionException failConversion(String property, Throwable e) {
			return new ConversionException("Could not process "+property+" property for resource '"+Parser.this.resource+"'",e);
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
	private final String parsedType;
	private final String targetVariable;
	private final Query query;

	Parser(Model model, Resource resource, String parsedType, String targetVariable, Query query) {
		this.model = model;
		this.resource = resource;
		this.parsedType = parsedType;
		this.targetVariable = targetVariable;
		this.query = query;
	}

	final T parse() {
		return firstResult(parseCollection());
	}

	final List<T> parseCollection() {
		List<T> result=null;
		QuerySolutionMap parameters = new QuerySolutionMap();
		parameters.add(this.targetVariable,this.resource);
		QueryExecution queryExecution=QueryExecutionFactory.create(this.query,this.model);
		queryExecution.setInitialBinding(parameters);
		try {
			ResultSet results = queryExecution.execSelect();
			result=processResults(results);
		} finally {
			queryExecution.close();
		}
		return result;
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

	private T firstResult(List<T> result) {
		if(result.isEmpty()) {
			return null;
		} else if(result.size()==1) {
			return result.get(0);
		}
		throw new ConversionException("Too many "+this.parsedType+" definitions for resource '"+this.resource+"'");
	}

	protected abstract B newBuilder();

	protected abstract Worker solutionParser();

}