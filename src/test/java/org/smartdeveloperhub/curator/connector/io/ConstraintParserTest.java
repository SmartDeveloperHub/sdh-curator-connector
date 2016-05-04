/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.fail;

import java.util.List;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

@RunWith(JMockit.class)
public class ConstraintParserTest {

	@Test
	public void testFromModel$literalTypes() throws Exception {
		new ParserTester("data/constraints/literal_type.ttl",STOA.ENRICHMENT_REQUEST_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				Binding binding = getBinding(model, target);
				assertThat(binding.value(),instanceOf(Literal.class));
				Literal literal=(Literal)binding.value();
				assertThat(literal.lexicalForm(),equalTo("data"));
				assertThat(literal.datatype().toString(),equalTo(XSD.STRING_TYPE));
				assertThat(literal.language().toString(),equalTo(""));
			}

		}.verify();
	}

	@Test
	public void testFromModel$localTypes() throws Exception {
		new ParserTester("data/constraints/local_type.ttl",STOA.ENRICHMENT_REQUEST_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				Binding binding = getBinding(model, target);
				assertThat(binding.value(),instanceOf(Variable.class));
			}

		}.verify();
	}

	@Test
	public void testFromModel$failProtected() throws Exception {
		new MockUp<ConstraintParser>() {
			@Mock
			boolean isProtected(Binding binding) {
				throw new ConversionException("failure");
			}

		};
		new ParserTester("data/constraints/local_type.ttl",STOA.ENRICHMENT_REQUEST_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					getConstraints(model, target);
					fail("Should not return constraints if internal failure happens");
				} catch(Exception e) {
					assertThat(e,instanceOf(ConversionException.class));
					assertThat(e.getMessage(),equalTo("failure"));
				}
			}

		}.verify();
	}

	@Test
	public void testFromModel$failAddPending() throws Exception {
		new MockUp<ConstraintParser>() {
			@Mock
			void addPendingResource(Invocation invocation, Resource resource) {
				if(invocation.getInvocationIndex()==0) {
					invocation.proceed(resource);
				} else {
					throw new ConversionException("failAppend");
				}
			}

		};
		new ParserTester("data/constraints/bad_referral.ttl",STOA.ENRICHMENT_REQUEST_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				try {
					getConstraints(model, target);
					fail("Should not return constraints if internal failure happens");
				} catch(Exception e) {
					assertThat(e,instanceOf(ConversionException.class));
					assertThat(e.getMessage(),equalTo("failAppend"));
				}
			}

		}.verify();
	}

	private Binding getBinding(Model model, Resource target) {
		List<Constraint> constraints = getConstraints(model, target);
		assertThat(constraints,hasSize(1));

		List<Binding> bindings = constraints.get(0).bindings();
		assertThat(bindings,hasSize(1));

		Binding binding=bindings.get(0);
		assertThat(binding.property().toString(),equalTo(RDF.TYPE));
		return binding;
	}

	private List<Constraint> getConstraints(Model model, Resource target) {
		Resource targetResource = getTargetResource(model, target);
		List<Variable> variables = getVariables(model, targetResource);
		return ConstraintParser.fromModel(model,targetResource,variables);
	}

	private Resource getTargetResource(Model model, Resource target) {
		RDFNode targetResource = target.getProperty(model.createProperty(STOA.TARGET_RESOURCE)).getObject();
		final Resource asResource = targetResource.asResource();
		return asResource;
	}

	private List<Variable> getVariables(Model model, final Resource asResource) {
		RDFNode filter = asResource.getProperty(model.createProperty("urn:filter")).getObject();
		Resource variable = filter.asResource();
		List<Variable> variables = ImmutableList.of(ProtocolFactory.newVariable(variable.getId().getLabelString()));
		return variables;
	}

}
