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
import org.smartdeveloperhub.curator.connector.protocol.ValidationException;
import org.smartdeveloperhub.curator.connector.rdf.ModelUtil;
import org.smartdeveloperhub.curator.connector.util.Builder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
							}
						}
					);
					mandatory(
						new ResourceConsumer("mandatoryResource","curator:submittedBy") {
							@Override
							protected void consumeResource(CustomBuilder builder, Resource resource) {
							}
						}
					);
					optional(
						new ResourceProvider<Resource>("providerFailure","rdfs:comment") {
							@Override
							protected Resource consumeResource(CustomBuilder builder, Resource resource) {
								throw new ValidationException(resource,"Failure");
							}
						}
					);
					optional(
						new LiteralConsumer("optionalLiteral","rdfs:label") {
							@Override
							protected void consumeLiteral(CustomBuilder builder, Literal literal) {
								throw new ValidationException(literal,"xsd:string");
							}
						}
					);
					optional(
						new ResourceConsumer("optionalResource","curator:replyTo") {
							@Override
							protected void consumeResource(CustomBuilder builder, Resource resource) {
								throw new ValidationException(resource,"curator:DeliveryChannel");
							}
						}
					);
					optional(
						new NodeConsumer("optionalNode","rdfs:isDefinedBy") {
							@Override
							protected void consumeNode(CustomBuilder builder, RDFNode node) {
								throw new ValidationException(node,"xsd:string");
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
				} catch (VariableNotBoundException e) {
					assertThat(e.variableName(),equalTo("mandatoryLiteral"));
					assertThat(e.variableType(),equalTo("literal"));
					assertThat(e.property(),equalTo("curator:messageId"));
					assertThat(e.resource(),equalTo(ModelUtil.toString(target)));
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
				} catch (VariableNotBoundException e) {
					assertThat(e.variableName(),equalTo("mandatoryResource"));
					assertThat(e.variableType(),equalTo("resource"));
					assertThat(e.property(),equalTo("curator:submittedBy"));
					assertThat(e.resource(),equalTo(ModelUtil.toString(target)));
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

	@Test
	public void testFromModel$failure$badNode() {
		new ParserTester("data/custom/bad_node.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					CustomParser.fromModel(model, target);
					fail("Should not parse invalid input");
				} catch (ConversionException e) {
					assertThat(e.getMessage(),equalTo("Could not process rdfs:isDefinedBy property for resource '"+target+"'"));
				}
			}
		}.verify();
	}

	@Test
	public void testFromModel$failure$providerFailure() {
		new ParserTester("data/custom/provider_failure.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					CustomParser.fromModel(model, target);
					fail("Should not parse invalid input");
				} catch (ConversionException e) {
					assertThat(e.getMessage(),equalTo("Could not process rdfs:comment property for resource '"+target+"'"));
				}
			}
		}.verify();
	}

	@Test
	public void testFromModel$failure$cannotBindLiteral() {
		new ParserTester("data/custom/cannot_bind_literal.ttl",CURATOR.MESSAGE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					CustomParser.fromModel(model, target);
					fail("Should not parse invalid input");
				} catch (InvalidVariableBindingException e) {
					assertThat(e.variableName(),equalTo("optionalLiteral"));
					assertThat(e.variableType(),equalTo("literal"));
					assertThat(e.property(),equalTo("rdfs:label"));
					assertThat(e.resource(),equalTo(ModelUtil.toString(target)));
					assertThat(e.boundValue(),equalTo("http://www.smartdeveloperhub.org/failure (URIRef)"));
				}
			}
		}.verify();
	}

}
