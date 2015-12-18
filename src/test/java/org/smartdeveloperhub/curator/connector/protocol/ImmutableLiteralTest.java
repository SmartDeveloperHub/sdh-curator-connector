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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.protocol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.net.URI;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

public class ImmutableLiteralTest {

	private static final ImmutableLiteral DEFAULT = new ImmutableLiteral("lexicalForm",URI.create("datatype"),"language");

	@Test
	public void testEquals$differentType() throws Exception {
		assertThat((Object)DEFAULT,not(equalTo((Object)"another type")));
	}

	@Test
	public void testEquals$all() throws Exception {
		ImmutableLiteral sut=DEFAULT;
		assertThat(sut,equalTo(sut));
	}

	@Test
	public void testEquals$differentLexicalForm() throws Exception {
		ImmutableLiteral sut=new ImmutableLiteral("anotherLexicalForm",URI.create("datatype"),"language");
		assertThat(sut,not(equalTo(DEFAULT)));
	}

	@Test
	public void testEquals$differentDatatype() throws Exception {
		ImmutableLiteral sut=new ImmutableLiteral("lexicalForm",URI.create("anotherDatatype"),"language");
		assertThat(sut,not(equalTo(DEFAULT)));
	}

	@Test
	public void testEquals$differentLanguage() throws Exception {
		ImmutableLiteral sut=new ImmutableLiteral("lexicalForm",URI.create("datatype"),"anotherLanguage");
		assertThat(sut,not(equalTo(DEFAULT)));
	}

	@Test
	public void testHashCode$differentLexicalForm() throws Exception {
		ImmutableLiteral sut=new ImmutableLiteral("anotherLexicalForm",URI.create("datatype"),"language");
		assertThat(sut.hashCode(),not(equalTo(DEFAULT.hashCode())));
	}

	@Test
	public void testHashCode$differentDatatype() throws Exception {
		ImmutableLiteral sut=new ImmutableLiteral("lexicalForm",URI.create("anotherDatatype"),"language");
		assertThat(sut.hashCode(),not(equalTo(DEFAULT.hashCode())));
	}

	@Test
	public void testHashCode$differentLanguage() throws Exception {
		ImmutableLiteral sut=new ImmutableLiteral("lexicalForm",URI.create("datatype"),"anotherLanguage");
		assertThat(sut.hashCode(),not(equalTo(DEFAULT.hashCode())));
	}

	@Test
	public void testHasCustomString() {
		assertThat(DEFAULT.toString(),not(equalTo(Utils.defaultToString(DEFAULT))));
	}

}
