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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.1.0
 *   Bundle      : sdh-curator-connector-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.net.URI;

import mockit.Mocked;

import org.junit.Test;

public class EnrichmentResultTest {

	private final URI TR1=URI.create("targetResource1");
	private final URI TR2=URI.create("targetResource2");

	@Mocked private Bindings addition1;
	@Mocked private Bindings addition2;
	@Mocked private Bindings removal1;
	@Mocked private Bindings removal2;

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
					withAdditions(this.addition1).
					withRemovals(this.removal1);
		assertThat(sut,not(equalTo(defaultResult())));
	}

	@Test
	public void testEquals$differentAdditionResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAdditions(this.addition2).
					withRemovals(this.removal1);
		assertThat(sut,not(equalTo(defaultResult())));
	}

	@Test
	public void testEquals$differentRemovalResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAdditions(this.addition1).
					withRemovals(this.removal2);
		assertThat(sut,not(equalTo(defaultResult())));
	}

	@Test
	public void testHashCode$differentTargetResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR2).
					withAdditions(this.addition1).
					withRemovals(this.removal1);
		assertThat(sut.hashCode(),not(equalTo(defaultResult().hashCode())));
	}

	@Test
	public void testHashCode$differentAdditionResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAdditions(this.addition2).
					withRemovals(this.removal1);
		assertThat(sut.hashCode(),not(equalTo(defaultResult().hashCode())));
	}

	@Test
	public void testHashCode$differentRemovalResource() throws Exception {
		final EnrichmentResult sut =
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAdditions(this.addition1).
					withRemovals(this.removal2);
		assertThat(sut.hashCode(),not(equalTo(defaultResult().hashCode())));
	}

	private EnrichmentResult defaultResult() {
		return
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withAdditions(this.addition1).
					withRemovals(this.removal1);
	}

	@Test
	public void testWithTargetResource$nullString() throws Exception {
		assertThat(
			EnrichmentResult.
				newInstance().
					withTargetResource(this.TR1).
					withTargetResource((String)null).
					targetResource(),
			nullValue());
	}

}
