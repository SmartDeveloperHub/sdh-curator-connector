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
package org.smartdeveloperhub.curator.connector.protocol;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.curator.protocol.Value;

@RunWith(JMockit.class)
public class ImmutableBindingTest {

	@Injectable
	private URI property;

	@Injectable
	private Value value;

	@Tested
	private ImmutableBinding defaultBinding;

	@Mocked private URI anotherProperty;
	@Mocked private Value anotherValue;

	@Test
	public void testEquals$differentType() throws Exception {
		assertThat((Object)defaultBinding,not(equalTo((Object)"another object type")));
	}

	@Test
	public void testEquals$same() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(property,value);
		assertThat(sut,equalTo(defaultBinding));
	}

	@Test
	public void testEquals$differentProperty() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(anotherProperty,value);
		assertThat(sut,not(equalTo(defaultBinding)));
	}

	@Test
	public void testEquals$differentValue() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(property, anotherValue);
		assertThat(sut,not(equalTo(defaultBinding)));
	}

	@Test
	public void testEquals$totallyDifferent() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(anotherProperty, anotherValue);
		assertThat(sut,not(equalTo(defaultBinding)));
	}

	@Test
	public void testHashCode$same() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(property,value);
		assertThat(sut.hashCode(),equalTo(defaultBinding.hashCode()));
	}

	@Test
	public void testHashCode$differentProperty() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(anotherProperty,value);
		assertThat(sut.hashCode(),not(equalTo(defaultBinding.hashCode())));
	}

	@Test
	public void testHashCode$differentValue() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(property, anotherValue);
		assertThat(sut.hashCode(),not(equalTo(defaultBinding.hashCode())));
	}

	@Test
	public void testHashCode$totallyDifferent() throws Exception {
		final ImmutableBinding sut = new ImmutableBinding(anotherProperty, anotherValue);
		assertThat(sut.hashCode(),not(equalTo(defaultBinding.hashCode())));
	}

}
