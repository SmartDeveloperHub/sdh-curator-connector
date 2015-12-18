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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import org.junit.Test;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Constraint;

import com.google.common.collect.Iterators;

public class ConstraintsTest {

	private Constraints singleVariableConstraints() {
		return
			Constraints.
				newInstance().
					forVariable("target").
						withProperty("property").
							andVariable("variable");
	}

	private Constraints singleResourceConstraints() {
		return
			Constraints.
				newInstance().
					forResource("target").
						withProperty("property").
							andVariable("variable");
	}

	@Test
	public void testEquals$differentType() throws Exception {
		Constraints constraints = Constraints.newInstance();
		assertThat(constraints.equals("another object"),equalTo(false));
	}

	@Test
	public void testEquals$sameInstance() throws Exception {
		Constraints constraints=Constraints.newInstance();
		assertThat(constraints,equalTo(constraints));
	}

	@Test
	public void testEquals$equalInstance() throws Exception {
		Constraints constraints=singleVariableConstraints();
		assertThat(singleVariableConstraints(),equalTo(constraints));
	}

	@Test
	public void testEquals$differentInstance() throws Exception {
		Constraints contraints = singleVariableConstraints();
		assertThat(singleVariableConstraints(),equalTo(contraints));
	}

	@Test
	public void testEquals$differentConstraints() throws Exception {
		Constraints contraints = singleVariableConstraints();
		assertThat(singleResourceConstraints(),not(equalTo(contraints)));
	}

	@Test
	public void testHashCode$equalInstance() throws Exception {
		Constraints contraints = singleVariableConstraints();
		assertThat(singleVariableConstraints().hashCode(),equalTo(contraints.hashCode()));
	}

	@Test
	public void testHashCode$differentInstance() throws Exception {
		Constraints constraints = singleVariableConstraints();
		assertThat(Constraints.newInstance().hashCode(),not(equalTo(constraints.hashCode())));
	}

	@Test
	public void testMinimizesConstraints() throws Exception {
		Constraints sut=
			Constraints.
				newInstance().
					forVariable("target").
						withProperty("property").
							andVariable("variable").
					forVariable("target").
						withProperty("anotherProperty").
							andLiteral("literal");
		assertThat(Iterators.size(sut.iterator()),equalTo(1));
		Constraint constraint = Iterators.get(sut.iterator(),0);
		assertThat(constraint.bindings(),hasSize(2));
		assertThat(
			constraint.bindings(),
			hasItems(
				ProtocolFactory.
					newBinding().
						withProperty("anotherProperty").
							withValue(
								ProtocolFactory.
									newLiteral().
										withLexicalForm("literal")).
						build(),
				ProtocolFactory.
					newBinding().
						withProperty("property").
							withValue(
								ProtocolFactory.
									newVariable("variable")).
						build()));
	}

	@Test
	public void testNoDuplicateBindings() throws Exception {
		Constraints sut=
			Constraints.
				newInstance().
					forVariable("target").
						withProperty("property").
							andLanguageLiteral("literal","language").
					forVariable("target").
						withProperty("property").
							andLanguageLiteral("literal","language");
		assertThat(Iterators.size(sut.iterator()),equalTo(1));
		Constraint constraint = Iterators.get(sut.iterator(),0);
		assertThat(constraint.bindings(),hasSize(1));
		assertThat(
			constraint.bindings(),
			contains(
				ProtocolFactory.
					newBinding().
						withProperty("property").
						withValue(
							ProtocolFactory.
								newLiteral().
									withLexicalForm("literal").
									withLanguage("language")).
						build()));
	}
}
