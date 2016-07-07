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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.3.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.net.URI;

import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.rdf.ModelHelper;
import org.smartdeveloperhub.curator.connector.rdf.ModelUtil;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

@RunWith(JMockit.class)
public class BindingSerializerTest {

	@Test
	public void testSerialize$failForProtectedProperty(@Mocked NamedValue target) throws Exception {
		final Model model = ModelFactory.createDefaultModel();
		ModelHelper helper = ModelUtil.createHelper(model);
		BindingSerializer sut = BindingSerializer.newInstance(helper);
		try {
			sut.serialize(target, ProtocolFactory.newBinding().withProperty(STOA.ADDITION_TARGET).withValue(target).build());
		} catch (ForbiddenBindingException e) {
			assertThat(e.offendingTerm(),equalTo(URI.create(STOA.ADDITION_TARGET)));
		}
	}

	@Test
	public void testSerialize$failForType(@Mocked NamedValue target) throws Exception {
		final Model model = ModelFactory.createDefaultModel();
		ModelHelper helper = ModelUtil.createHelper(model);
		BindingSerializer sut = BindingSerializer.newInstance(helper);
		try {
			sut.serialize(target, ProtocolFactory.newBinding().withProperty(RDF.TYPE).withValue(ProtocolFactory.newResource(STOA.ENRICHMENT_REQUEST_TYPE)).build());
		} catch (ForbiddenBindingException e) {
			assertThat(e.offendingTerm(),equalTo(URI.create(STOA.ENRICHMENT_REQUEST_TYPE)));
		}
	}

	@Test
	public void testSerialize$acceptNonConflictingValues() throws Exception {
		Variable target=ProtocolFactory.newVariable("node");
		final Model model = ModelFactory.createDefaultModel();
		ModelHelper helper = ModelUtil.createHelper(model);
		BindingSerializer sut = BindingSerializer.newInstance(helper);
		sut.serialize(target, ProtocolFactory.newBinding().withProperty(RDF.TYPE).withValue(ProtocolFactory.newResource(XSD.ANY_URI_TYPE)).build());
		final Resource resource = model.createResource(AnonId.create(target.name()));
		assertThat(resource,notNullValue());
		Statement property = resource.getProperty(model.createProperty(RDF.TYPE));
		assertThat(property,notNullValue());
		assertThat(property.getObject().asResource().getURI(),equalTo(XSD.ANY_URI_TYPE));
	}

	@Test
	public void testSerialize$acceptInvalidTypes() throws Exception {
		Variable target=ProtocolFactory.newVariable("node");
		final Model model = ModelFactory.createDefaultModel();
		ModelHelper helper = ModelUtil.createHelper(model);
		BindingSerializer sut = BindingSerializer.newInstance(helper);
		sut.serialize(target, ProtocolFactory.newBinding().withProperty(RDF.TYPE).withValue(target).build());
		final Resource resource = model.createResource(AnonId.create(target.name()));
		assertThat(resource,notNullValue());
		Statement property = resource.getProperty(model.createProperty(RDF.TYPE));
		assertThat(property,notNullValue());
		assertThat(property.getObject().asResource(),equalTo(resource));
	}

}
