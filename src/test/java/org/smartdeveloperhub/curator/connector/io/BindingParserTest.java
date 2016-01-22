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

import java.util.List;

import org.junit.Test;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class BindingParserTest {

	@Test
	public void testFromModel$literalBindings() throws Exception {
		new ParserTester("data/bindings/with_literals.ttl",CURATOR.VARIABLE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				List<Binding> bindings=BindingParser.fromModel(model, target);
				assertThat(bindings,hasSize(2));
				Binding binding = bindings.get(0);
				if(binding.property().toString().equals(RDF.TYPE)) {
					binding=bindings.get(1);
				}
				assertThat(binding.property().toString(),equalTo("http://www.smartdeveloperhub.org/vocabulary/scm#commitId"));
				assertThat(binding.value(),instanceOf(Literal.class));
				Literal literal=(Literal)binding.value();
				assertThat(literal.lexicalForm(),equalTo("f1efd1d8d8ceebef1d85eb66c69a44b0d713ed44"));
				assertThat(literal.datatype().toString(),equalTo(XSD.STRING_TYPE));
				assertThat(literal.language().toString(),equalTo(""));
			}
		}.verify();
	}

	@Test
	public void testFromModel$variableBindings() throws Exception {
		new ParserTester("data/bindings/with_variables.ttl",CURATOR.VARIABLE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				List<Binding> bindings=BindingParser.fromModel(model, target);
				assertThat(bindings,hasSize(2));
				Binding binding = bindings.get(0);
				if(binding.property().toString().equals(RDF.TYPE)) {
					binding=bindings.get(1);
				}
				assertThat(binding.property().toString(),equalTo("http://www.smartdeveloperhub.org/vocabulary/scm#hasCommit"));
				assertThat(binding.value(),instanceOf(Variable.class));
			}
		}.verify();
	}

}
