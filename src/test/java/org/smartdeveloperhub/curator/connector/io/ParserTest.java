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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;



import org.junit.Test;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.Builder;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ParserTest {

	public static class Custom {

	}

	public static class CustomBuilder implements Builder<Custom> {

		@Override
		public Custom build() {
			return new Custom();
		}

	}

	public static class CustomParser extends Parser<Custom, CustomBuilder> {

		private static final Query QUERY=
				QueryFactory.create(
					ResourceUtil.loadResource("queries/customMessage.sparql"));

		private CustomParser(Model model, Resource resource) {
			super(model, resource, CURATOR.MESSAGE_TYPE, "message", QUERY);
		}

		static Custom fromModel(Model model, Resource resource) {
			return new CustomParser(model, resource).parse();
		}

		@Override
		protected CustomBuilder newBuilder() {
			return new CustomBuilder();
		}

		@Override
		protected Worker solutionParser() {
			return new Worker() {
				@Override
				protected void parse() {
					mandatory(
						new LiteralConsumer("mandatoryLiteral","curator:messageId") {
							@Override
							protected void consumeLiteral(CustomBuilder builder, Literal literal) {
								System.out.println("Mandatory literal: "+literal.getLexicalForm());
							}
						}
					);
					mandatory(
						new ResourceConsumer("mandatoryResource","curator:submittedBy") {
							@Override
							protected void consumeResource(CustomBuilder builder, Resource resource) {
								System.out.println("Mandatory resource: "+resource);
							}
						}
					);
					optional(
						new LiteralConsumer("optionalLiteral","rdfs:label") {
							@Override
							protected void consumeLiteral(CustomBuilder builder, Literal literal) {
								System.out.println("Optional literal: "+literal.getLexicalForm());
								throw new ValidationException(literal,"xsd:string");
							}
						}
					);
					optional(
						new ResourceConsumer("optionalResource","curator:replyTo") {
							@Override
							protected void consumeResource(CustomBuilder builder, Resource resource) {
								System.out.println("Optional resource: "+resource);
								throw new ValidationException(resource,"curator:DeliveryChannel");
							}
						}
					);
				}
			};
		}

	}

	@Test
	public void testFromModel$happyPath() {
		new ParserTester("data/custom/only_mandatory.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				Custom agent=CustomParser.fromModel(model, target);
				assertThat(agent,notNullValue());
			}
		}.verify();
	}

	@Test
	public void testFromModel$failure$missingLiteral() {
		new ParserTester("data/custom/missing_mandatory_literal.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					CustomParser.fromModel(model, target);
					fail("Should not parse invalid input");
				} catch (ConversionException e) {
					assertThat(e.getMessage(),equalTo("Could not find required property curator:messageId for resource '"+target+"'"));
				}
			}
		}.verify();
	}

	@Test
	public void testFromModel$failure$missingResource() {
		new ParserTester("data/custom/missing_mandatory_resource.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					CustomParser.fromModel(model, target);
					fail("Should not parse invalid input");
				} catch (ConversionException e) {
					assertThat(e.getMessage(),equalTo("Could not find required property curator:submittedBy for resource '"+target+"'"));
				}
			}
		}.verify();
	}

	@Test
	public void testFromModel$failure$badLiteral() {
		new ParserTester("data/custom/bad_literal.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					CustomParser.fromModel(model, target);
					fail("Should not parse invalid input");
				} catch (ConversionException e) {
					assertThat(e.getMessage(),equalTo("Could not process rdfs:label property for resource '"+target+"'"));
				}
			}
		}.verify();
	}

	@Test
	public void testFromModel$failure$badResource() {
		new ParserTester("data/custom/bad_resource.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					CustomParser.fromModel(model, target);
					fail("Should not parse invalid input");
				} catch (ConversionException e) {
					assertThat(e.getMessage(),equalTo("Could not process curator:replyTo property for resource '"+target+"'"));
				}
			}
		}.verify();
	}
}
