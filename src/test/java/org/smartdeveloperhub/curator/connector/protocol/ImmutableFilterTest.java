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
package org.smartdeveloperhub.curator.connector.protocol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.net.URI;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

public class ImmutableFilterTest {

	private static final ImmutableFilter DEFAULT = new ImmutableFilter(URI.create("property"),new ImmutableVariable("name"));

	@Test
	public void testEquals$differentType() throws Exception {
		assertThat((Object)DEFAULT,not(equalTo((Object)"another type")));
	}

	@Test
	public void testEquals$equal() throws Exception {
		ImmutableFilter sut=new ImmutableFilter(URI.create("property"),new ImmutableVariable("name"));
		assertThat(sut,equalTo(DEFAULT));
	}

	@Test
	public void testEquals$differentProperty() throws Exception {
		ImmutableFilter sut=new ImmutableFilter(URI.create("anotherProperty"),new ImmutableVariable("name"));
		assertThat(sut,not(equalTo(DEFAULT)));
	}

	@Test
	public void testEquals$differentVariable() throws Exception {
		ImmutableFilter sut=new ImmutableFilter(URI.create("property"),new ImmutableVariable("anotherName"));
		assertThat(sut,not(equalTo(DEFAULT)));
	}

	@Test
	public void testHashCode$equal() throws Exception {
		ImmutableFilter sut=new ImmutableFilter(URI.create("property"),new ImmutableVariable("name"));
		assertThat(sut.hashCode(),equalTo(DEFAULT.hashCode()));
	}

	@Test
	public void testHashCode$differentProperty() throws Exception {
		ImmutableFilter sut=new ImmutableFilter(URI.create("anotherProperty"),new ImmutableVariable("name"));
		assertThat(sut.hashCode(),not(equalTo(DEFAULT.hashCode())));
	}

	@Test
	public void testHashCode$differentVariable() throws Exception {
		ImmutableFilter sut=new ImmutableFilter(URI.create("property"),new ImmutableVariable("anotherName"));
		assertThat(sut.hashCode(),not(equalTo(DEFAULT.hashCode())));
	}

	@Test
	public void testHasCustomString() {
		assertThat(DEFAULT.toString(),not(equalTo(Utils.defaultToString(DEFAULT))));
	}

}
