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
package org.smartdeveloperhub.curator.connector.protocol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.protocol.Binding;

import com.google.common.collect.ImmutableList;

public class ImmutableConstraintTest {

	private static final ImmutableConstraint DEFAULT =
		new ImmutableConstraint(
			ProtocolFactory.newResource("target"),
			ImmutableList.
				<Binding>of(
					ProtocolFactory.
						newBinding().
							withProperty("property").
							withValue(
								ProtocolFactory.
									newLiteral().
										withLexicalForm("value")).
							build()
				));

	@Test
	public void testEquals$differentType() throws Exception {
		assertThat((Object)DEFAULT,not(equalTo((Object)"another type")));
	}

	@Test
	public void testEquals$equal() throws Exception {
		ImmutableConstraint sut=
			new ImmutableConstraint(
				ProtocolFactory.newResource("target"),
				ImmutableList.
					<Binding>of(
						ProtocolFactory.
							newBinding().
								withProperty("property").
								withValue(
									ProtocolFactory.
										newLiteral().
											withLexicalForm("value")).
								build()
					));
		assertThat(sut,equalTo(DEFAULT));
	}

	@Test
	public void testEquals$differentTarget() throws Exception {
		ImmutableConstraint sut=
			new ImmutableConstraint(
				ProtocolFactory.newResource("anotherTarget"),
				ImmutableList.
					<Binding>of(
						ProtocolFactory.
							newBinding().
								withProperty("property").
								withValue(
									ProtocolFactory.
										newLiteral().
											withLexicalForm("value")).
								build()
					));
		assertThat(sut,not(equalTo(DEFAULT)));
	}

	@Test
	public void testEquals$differentBindings() throws Exception {
		ImmutableConstraint sut=
			new ImmutableConstraint(
				ProtocolFactory.newResource("target"),
				ImmutableList.
					<Binding>of(
						ProtocolFactory.
							newBinding().
								withProperty("anotherProperty").
								withValue(
									ProtocolFactory.
										newLiteral().
											withLexicalForm("value")).
								build()
					));
		assertThat(sut,not(equalTo(DEFAULT)));
	}

	@Test
	public void testHashCode$equal() throws Exception {
		ImmutableConstraint sut=
			new ImmutableConstraint(
				ProtocolFactory.newResource("target"),
				ImmutableList.
					<Binding>of(
						ProtocolFactory.
							newBinding().
								withProperty("property").
								withValue(
									ProtocolFactory.
										newLiteral().
											withLexicalForm("value")).
								build()
					));
		assertThat(sut.hashCode(),equalTo(DEFAULT.hashCode()));
	}

	@Test
	public void testHashCode$differentTarget() throws Exception {
		ImmutableConstraint sut=
			new ImmutableConstraint(
				ProtocolFactory.newResource("anotherTarget"),
				ImmutableList.
					<Binding>of(
						ProtocolFactory.
							newBinding().
								withProperty("property").
								withValue(
									ProtocolFactory.
										newLiteral().
											withLexicalForm("value")).
								build()
					));
		assertThat(sut.hashCode(),not(equalTo(DEFAULT.hashCode())));
	}

	@Test
	public void testHashCode$differentBindings() throws Exception {
		ImmutableConstraint sut=
			new ImmutableConstraint(
				ProtocolFactory.newResource("target"),
				ImmutableList.
					<Binding>of(
						ProtocolFactory.
							newBinding().
								withProperty("anotherProperty").
								withValue(
									ProtocolFactory.
										newLiteral().
											withLexicalForm("value")).
								build()
					));
		assertThat(sut.hashCode(),not(equalTo(DEFAULT.hashCode())));
	}

	@Test
	public void testHasCustomString() {
		assertThat(DEFAULT.toString(),not(equalTo(Utils.defaultToString(DEFAULT))));
	}

}
