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
import static org.hamcrest.Matchers.*;

import java.net.URI;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.Value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@RunWith(JMockit.class)
public class ProtocolUtilTest {

	@Mocked private EnrichmentResponseMessage response;
	@Mocked private URI targetResource;
	@Mocked private Binding binding;
	@Mocked private URI property;
	@Mocked private Value value;

	@Test
	public void testProtocolUtil() throws Exception {
		assertThat(Utils.isUtilityClass(ProtocolUtil.class),equalTo(true));
	}

	@Test
	public void testToEnrichmentResult$additions() {
		new Expectations() {{
			response.targetResource();result=targetResource;
			response.removals();result=Lists.newArrayList();
			response.additions();result=ImmutableList.of(binding);
			binding.property();result=property;
			binding.value();result=value;
		}};
		EnrichmentResult result = ProtocolUtil.toEnrichmentResult(response);
		assertThat(result.addedProperties(),contains(property));
		assertThat(result.addedValue(property),equalTo(value));
	}

	@Test
	public void testToEnrichmentResult$removals() {
		new Expectations() {{
			response.targetResource();result=targetResource;
			response.additions();result=Lists.newArrayList();
			response.removals();result=ImmutableList.of(binding);
			binding.property();result=property;
			binding.value();result=value;
		}};
		EnrichmentResult result = ProtocolUtil.toEnrichmentResult(response);
		assertThat(result.removedProperties(),contains(property));
		assertThat(result.removedValue(property),equalTo(value));
	}

}
