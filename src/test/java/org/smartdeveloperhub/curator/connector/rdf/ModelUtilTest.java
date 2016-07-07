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
package org.smartdeveloperhub.curator.connector.rdf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


public class ModelUtilTest {

	private Model model;
	private Literal literal;
	private Resource resource;
	private Resource bnode;

	@Before
	public void setUp() {
		this.model = ModelFactory.createDefaultModel();
		this.literal = this.model.createLiteral("1");
		this.resource= this.model.createResource("URIRef");
		this.bnode=this.model.createResource(AnonId.create("bnode"));
	}

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(ModelUtil.class),equalTo(true));
	}

	@Test
	public void testNodeType$null() throws Exception {
		assertThat(ModelUtil.nodeType(null),equalTo("unknown"));
	}

	@Test
	public void testNodeType$literal() throws Exception {
		assertThat(ModelUtil.nodeType(this.literal),equalTo("literal"));
	}

	@Test
	public void testNodeType$URIRef() throws Exception {
		assertThat(ModelUtil.nodeType(this.resource),equalTo("URIRef"));
	}

	@Test
	public void testNodeType$blankNode() throws Exception {
		assertThat(ModelUtil.nodeType(this.bnode),equalTo("blank node"));
	}

	@Test
	public void testToString$null() throws Exception {
		assertThat(ModelUtil.toString(null),equalTo("null (unknown)"));
	}

	@Test
	public void testToString$literal() throws Exception {
		assertThat(ModelUtil.toString(literal),equalTo("1 (literal)"));
	}
	@Test
	public void testToString$URIRef() throws Exception {
		assertThat(ModelUtil.toString(this.resource),equalTo("URIRef (URIRef)"));
	}
	@Test
	public void testToString$blankNode() throws Exception {
		assertThat(ModelUtil.toString(this.bnode),equalTo("bnode (blank node)"));
	}
}
