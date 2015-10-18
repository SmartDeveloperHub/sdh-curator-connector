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
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.net.URI;

import mockit.Mocked;

import org.junit.Test;
import org.smartdeveloperhub.curator.protocol.Value;

public class EnrichmentResultTest {

	private final URI TR1=URI.create("targetResource1");
	private final URI TR2=URI.create("targetResource2");

	@Mocked private Value value1;
	@Mocked private Value value2;

	@Test
	public void testEquals$differentType() throws Exception {
		final EnrichmentResult sut = EnrichmentResult.newInstance();
		assertThat((Object)sut,not(equalTo((Object)"object")));
	}

	@Test
	public void testEquals$differentTargetResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR2).
					withAddition(URI.create("addition"),this.value1).
					withRemoval(URI.create("removal"),this.value2);
		assertThat(sut,not(equalTo(defaultResult())));
	}

	@Test
	public void testEquals$differentAdditionResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAddition(URI.create("addition"),this.value2).
					withRemoval(URI.create("removal"),this.value2);
		assertThat(sut,not(equalTo(defaultResult())));
	}

	@Test
	public void testEquals$differentRemovalResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAddition(URI.create("addition"),this.value1).
					withRemoval(URI.create("removal"),this.value1);
		assertThat(sut,not(equalTo(defaultResult())));
	}

	@Test
	public void testHashCode$differentTargetResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR2).
					withAddition(URI.create("addition"),this.value1).
					withRemoval(URI.create("removal"),this.value2);
		assertThat(sut.hashCode(),not(equalTo(defaultResult().hashCode())));
	}

	@Test
	public void testHashCode$differentAdditionResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAddition(URI.create("addition"),this.value2).
					withRemoval(URI.create("removal"),this.value2);
		assertThat(sut.hashCode(),not(equalTo(defaultResult().hashCode())));
	}

	@Test
	public void testHashCode$differentRemovalResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAddition(URI.create("addition"),this.value1).
					withRemoval(URI.create("removal"),this.value1);
		assertThat(sut.hashCode(),not(equalTo(defaultResult().hashCode())));
	}

	private EnrichmentResult defaultResult() {
		return
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAddition(URI.create("addition"),this.value1).
					withRemoval(URI.create("removal"),this.value2);
	}

}