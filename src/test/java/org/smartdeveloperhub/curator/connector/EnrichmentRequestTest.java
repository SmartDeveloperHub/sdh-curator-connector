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
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.net.URI;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;

public class EnrichmentRequestTest {

	private final URI TR2=URI.create("targetResource2");

	@Test
	public void testEquals$differentType() throws Exception {
		final EnrichmentRequest sut = EnrichmentRequest.newInstance();
		assertThat((Object)sut,not(equalTo((Object)"object")));
	}

	@Test
	public void testEquals$equal() throws Exception {
		final EnrichmentRequest sut=defaultRequest();
		assertThat(sut,equalTo(defaultRequest()));
	}

	@Test
	public void testEquals$differentTargetResource() throws Exception {
		final EnrichmentRequest sut=withDifferentTargetResult();
		assertThat(sut,not(equalTo(defaultRequest())));
	}

	@Test
	public void testEquals$differentFilters() throws Exception {
		final EnrichmentRequest sut=withDifferentFilters();
		assertThat(sut,not(equalTo(defaultRequest())));
	}

	@Test
	public void testEquals$differentConstraints() throws Exception {
		final EnrichmentRequest sut=withDifferentConstraints();
		assertThat(sut,not(equalTo(defaultRequest())));
	}

	@Test
	public void testHashCode$equal() throws Exception {
		final EnrichmentRequest sut=defaultRequest();
		assertThat(sut.hashCode(),equalTo(defaultRequest().hashCode()));
	}

	@Test
	public void testHashCode$differentTargetResource() throws Exception {
		final EnrichmentRequest sut=withDifferentTargetResult();
		assertThat(sut.hashCode(),not(equalTo(defaultRequest().hashCode())));
	}

	@Test
	public void testHashCode$differentFilters() throws Exception {
		final EnrichmentRequest sut=withDifferentFilters();
		assertThat(sut.hashCode(),not(equalTo(defaultRequest().hashCode())));
	}

	@Test
	public void testHashCode$differentConstraints() throws Exception {
		final EnrichmentRequest sut=withDifferentConstraints();
		assertThat(sut.hashCode(),not(equalTo(defaultRequest().hashCode())));
	}

	private EnrichmentRequest withDifferentTargetResult() {
		return defaultRequest().withTargetResource(this.TR2);
	}

	private EnrichmentRequest withDifferentFilters() {
		return defaultRequest().withFilters(Filters.newInstance());
	}

	private EnrichmentRequest withDifferentConstraints() {
		return defaultRequest().withConstraints(Constraints.newInstance());
	}

	@Test
	public void testHasCustomString() {
		final EnrichmentRequest sut=defaultRequest();
		assertThat(sut.toString(),not(equalTo(Utils.defaultToString(sut))));
	}

	private EnrichmentRequest defaultRequest() {
		return
			EnrichmentRequest.
				newInstance().
					withTargetResource("targetResource").
					withFilters(
						Filters.newInstance().withFilter("property","variable")).
					withConstraints(
						Constraints.newInstance().forResource("resource").withProperty("aProperty").andLiteral("value"));
	}

}
