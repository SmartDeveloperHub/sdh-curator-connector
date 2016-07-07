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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.rdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.net.URI;
import java.net.URL;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

@RunWith(JMockit.class)
public class ImmutablePropertyHelperTest {

	private static final String LANGUAGE = "language";

	private static final String ID = "http://www.smartdeveloperhub.org";

	@Mocked private Model model;

	@Mocked private Resource resource;

	@Mocked private Resource anotherResource;

	@Mocked private Resource type;

	@Mocked private Property property;

	@Mocked private Literal literal;

	private ImmutablePropertyHelper sut() {
		return
			new ImmutablePropertyHelper(
				new ImmutableResourceHelper(
					new ImmutableModelHelper(model),
					resource),
				resource,
				property);
	}

	@Test
	public void testType$URI() throws Exception {
		new Expectations() {{
			model.createProperty(RDF.TYPE);result=property;
			model.createResource(ID);result=type;
			model.add(resource,property,type);
		}};
		ImmutablePropertyHelper sut = sut();
		sut.type(URI.create(ID));
	}

	@Test
	public void testType$String() throws Exception {
		new Expectations() {{
			model.createProperty(RDF.TYPE);result=property;
			model.createResource(ID);result=type;
			model.add(resource,property,type);
		}};
		ImmutablePropertyHelper sut = sut();
		sut.type(ID);
	}

	@Test
	public void testWithLanguageLiteral() throws Exception {
		new Expectations() {{
			model.createLiteral(ID,LANGUAGE);result=literal;
			model.add(resource,property,literal);
		}};
		ImmutablePropertyHelper sut = sut();
		sut.withLanguageLiteral(ID,LANGUAGE);
	}

	@Test
	public void testWithLiteral() throws Exception {
		new Expectations() {{
			model.createLiteral(ID);result=literal;
			model.add(resource,property,literal);
		}};
		ImmutablePropertyHelper sut = sut();
		sut.withLiteral(ID);
	}

	@Test
	public void testWithResource() throws Exception {
		new Expectations() {{
			model.createResource(ID);result=anotherResource;
			model.add(resource,property,anotherResource);
		}};
		ImmutablePropertyHelper sut = sut();
		sut.withResource(new URL(ID));
	}

	@Test
	public void testProperty$URI() throws Exception {
		new Expectations() {{
			model.createProperty(ID);result=property;
		}};
		ImmutablePropertyHelper sut = sut();
		PropertyHelper helper=sut.property(URI.create(ID));
		assertThat((Resource)Deencapsulation.getField(helper,"resource"),equalTo(this.resource));
		assertThat((Property)Deencapsulation.getField(helper,"property"),equalTo(this.property));
	}

}
