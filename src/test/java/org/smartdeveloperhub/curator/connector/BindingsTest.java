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
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;

import com.google.common.collect.Lists;

public class BindingsTest {

	private Bindings defaultBindings() {
		return
			Bindings.
				newInstance().
					withProperty(AMQP.HOST).
						andTypedLiteral("localhost", TYPES.HOSTNAME_TYPE);
	}

	private Bindings withDifferentValue() {
		return defaultBindings().withProperty(AMQP.HOST).andLiteral("localhost");
	}

	private Bindings withDifferentProperties() {
		return defaultBindings().withProperty(UseCase.ci("forBranch")).andVariable("branch");
	}

	@Test
	public void testEquals$differentType() throws Exception {
		final Bindings sut=defaultBindings();
		assertThat((Object)sut,not(equalTo((Object)"object")));
	}

	@Test
	public void testEquals$equal() throws Exception {
		final Bindings sut=defaultBindings();
		assertThat(sut,equalTo(defaultBindings()));
	}

	@Test
	public void testEquals$withDifferentValue() throws Exception {
		final Bindings sut=withDifferentValue();
		assertThat(sut,not(equalTo(defaultBindings())));
	}

	@Test
	public void testEquals$withDifferentProperties() throws Exception {
		final Bindings sut=withDifferentProperties();
		assertThat(sut,not(equalTo(defaultBindings())));
	}

	@Test
	public void testHashCode$equal() throws Exception {
		final Bindings sut=defaultBindings();
		assertThat(sut.hashCode(),equalTo(defaultBindings().hashCode()));
	}

	@Test
	public void testHashCode$withDifferentValue() throws Exception {
		final Bindings sut=withDifferentValue();
		assertThat(sut.hashCode(),not(equalTo(defaultBindings().hashCode())));
	}

	@Test
	public void testHashCode$withDifferentProperties() throws Exception {
		final Bindings sut=withDifferentProperties();
		assertThat(sut.hashCode(),not(equalTo(defaultBindings().hashCode())));
	}

	@Test
	public void testIterator() throws Exception {
		assertThat(
			Lists.newArrayList(defaultBindings()),
			contains(
				ProtocolFactory.
					newBinding().
						withProperty(AMQP.HOST).
						withValue(
							ProtocolFactory.
								newLiteral().
									withLexicalForm("localhost").
									withDatatype(TYPES.HOSTNAME_TYPE)).
						build()));
	}

	@Test
	public void testHasCustomString() {
		final Bindings sut=defaultBindings();
		assertThat(sut.toString(),not(equalTo(Utils.defaultToString(sut))));
	}

}
