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
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import mockit.Mocked;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.protocol.Value;

public class FailureTest {

	@Mocked private Value value1;
	@Mocked private Value value2;

	@Test
	public void testWithCode$rejectBadValue() {
		try {
			Failure.newInstance().withCode(-1L);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Failure code cannot be lower than zero (-1)"));
		}
	}

	@Test
	public void testWithSubcode$nullClears() {
		assertThat(Failure.newInstance().withSubcode(1L).withSubcode(null).subcode().isPresent(),equalTo(false));
	}

	@Test
	public void testWithSubcode$rejectBadValue() {
		try {
			Failure.newInstance().withSubcode(-1L);
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Failure subcode cannot be lower than zero (-1)"));
		}
	}

	@Test
	public void testAccessors$default() {
		final Failure sut=Failure.newInstance();
		assertThat(sut.code(),equalTo(0L));
		assertThat(sut.subcode().isPresent(),equalTo(false));
		assertThat(sut.reason(),equalTo("Unexpected failure"));
		assertThat(sut.details(),nullValue());
	}

	@Test
	public void testAccessors$populated() {
		final Failure sut=defaultFailure();
		assertThat(sut.code(),equalTo(1L));
		assertThat(sut.subcode().isPresent(),equalTo(true));
		assertThat(sut.subcode().orNull(),equalTo(2L));
		assertThat(sut.reason(),equalTo("reason"));
		assertThat(sut.details(),equalTo("detail"));
	}

	@Test
	public void testEquals$differentType() throws Exception {
		final Failure sut=defaultFailure();
		assertThat((Object)sut,not(equalTo((Object)"object")));
	}

	@Test
	public void testEquals$equal() throws Exception {
		final Failure sut=defaultFailure();
		assertThat(sut,equalTo(defaultFailure()));
	}

	@Test
	public void testEquals$totallyDifferent() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(2).
					withSubcode(1L).
					withReason("anotherReason").
					withDetail("anotherDetail");
		assertThat(sut,not(equalTo(defaultFailure())));
	}

	@Test
	public void testEquals$differentCode() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(2).
					withSubcode(2L).
					withReason("reason").
					withDetail("detail");
		assertThat(sut,not(equalTo(defaultFailure())));
	}

	@Test
	public void testEquals$differentSubcode() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(1).
					withSubcode(1L).
					withReason("reason").
					withDetail("detail");
		assertThat(sut,not(equalTo(defaultFailure())));
	}

	@Test
	public void testEquals$differentReason() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(1).
					withSubcode(2L).
					withReason("anotherReason").
					withDetail("detail");
		assertThat(sut,not(equalTo(defaultFailure())));
	}

	@Test
	public void testEquals$differentDetail() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(1).
					withSubcode(2L).
					withReason("reason").
					withDetail("anotherDetail");
		assertThat(sut,not(equalTo(defaultFailure())));
	}

	@Test
	public void testHashCode$equal() throws Exception {
		final Failure sut=defaultFailure();
		assertThat(sut.hashCode(),equalTo(defaultFailure().hashCode()));
	}

	@Test
	public void testHashCode$totallyDifferent() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(2).
					withSubcode(1L).
					withReason("anotherReason").
					withDetail("anotherDetail");
		assertThat(sut.hashCode(),not(equalTo(defaultFailure().hashCode())));
	}

	@Test
	public void testHashCode$differentCode() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(2).
					withSubcode(2L).
					withReason("reason").
					withDetail("detail");
		assertThat(sut.hashCode(),not(equalTo(defaultFailure().hashCode())));
	}

	@Test
	public void testHashCode$differentSubcode() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(1).
					withSubcode(1L).
					withReason("reason").
					withDetail("detail");
		assertThat(sut.hashCode(),not(equalTo(defaultFailure().hashCode())));
	}

	@Test
	public void testHashCode$differentReason() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(1).
					withSubcode(2L).
					withReason("anotherReason").
					withDetail("detail");
		assertThat(sut.hashCode(),not(equalTo(defaultFailure().hashCode())));
	}

	@Test
	public void testHashCode$differentDetail() throws Exception {
		final Failure sut=
			Failure.
				newInstance().
					withCode(1).
					withSubcode(2L).
					withReason("reason").
					withDetail("anotherDetail");
		assertThat(sut.hashCode(),not(equalTo(defaultFailure().hashCode())));
	}

	@Test
	public void testHasCustomString() {
		final Failure sut=defaultFailure();
		assertThat(sut.toString(),not(equalTo(Utils.defaultToString(sut))));
	}

	private Failure defaultFailure() {
		return
			Failure.newInstance().withCode(1).withSubcode(2L).withReason("reason").withDetail("detail");
	}

}
