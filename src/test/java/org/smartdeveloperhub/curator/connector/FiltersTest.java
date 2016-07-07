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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.3.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.common.collect.Iterators;

public class FiltersTest {

	@Test
	public void testEquals$differentType() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		assertThat(filters.equals("another object"),equalTo(false));
	}

	@Test
	public void testEquals$sameInstance() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		assertThat(filters,equalTo(filters));
	}

	@Test
	public void testEquals$equalInstance() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		assertThat(Filters.newInstance().withFilter("property","variable"),equalTo(filters));
	}

	@Test
	public void testEquals$differentInstance() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		assertThat(Filters.newInstance().withFilter("property","variable2"),not(equalTo(filters)));
	}

	@Test
	public void testHashCode$equalInstance() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		assertThat(Filters.newInstance().withFilter("property","variable").hashCode(),equalTo(filters.hashCode()));
	}

	@Test
	public void testHashCode$differentInstance() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		assertThat(Filters.newInstance().withFilter("property","variable2").hashCode(),not(equalTo(filters.hashCode())));
	}

	@Test
	public void testWithFilter$discardDuplicates() throws Exception {
		Filters filters =
				Filters.
					newInstance().
						withFilter("property","variable").
						withFilter("property","variable");
		assertThat(Iterators.size(filters.iterator()),equalTo(1));
	}

	@Test
	public void testWithFilter$failOnRepeatedVariable() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		try {
			filters.withFilter("anotherProperty","variable");
			fail("Should not accept repeated variables");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Filter variable already defined"));
		}
	}

	@Test
	public void testWithFilter$failOnRepeatedProperty() throws Exception {
		Filters filters = Filters.newInstance().withFilter("property","variable");
		try {
			filters.withFilter("property","anotherVariable");
			fail("Should not accept repeated properties");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Filter property already defined"));
		}
	}
}
