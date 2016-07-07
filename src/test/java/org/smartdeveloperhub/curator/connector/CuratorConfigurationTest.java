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
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.protocol.Broker;

@RunWith(JMockit.class)
public class CuratorConfigurationTest {

	@Mocked private Broker anotherBroker;

	private CuratorConfiguration defaultCuratorConfiguration() {
		return CuratorConfiguration.newInstance();
	}

	private CuratorConfiguration withDifferentBroker() {
		return defaultCuratorConfiguration().withBroker(this.anotherBroker);
	}

	private CuratorConfiguration withDifferentExchangeName() {
		return defaultCuratorConfiguration().withExchangeName("anExchangeName");
	}

	private CuratorConfiguration withDifferentQueueName() {
		return defaultCuratorConfiguration().withQueueName("anotherRequestQueueName");
	}

	private CuratorConfiguration withDifferentRequestRoutingKey() {
		return defaultCuratorConfiguration().withRequestRoutingKey("anotherRequestRoutingKey");
	}

	private CuratorConfiguration withDifferentResponseRoutingKey() {
		return defaultCuratorConfiguration().withResponseRoutingKey("anotherResponseRoutingKey");
	}

	@Test
	public void testEquals$differentType() throws Exception {
		assertThat((Object)defaultCuratorConfiguration(),not(equalTo((Object)"another object type")));
	}

	@Test
	public void testEquals$equal() {
		final CuratorConfiguration sut = defaultCuratorConfiguration();
		assertThat(sut,equalTo(defaultCuratorConfiguration()));
	}

	@Test
	public void testEquals$differentBroker() {
		final CuratorConfiguration sut = withDifferentBroker();
		assertThat(sut,not(equalTo(defaultCuratorConfiguration())));
	}

	@Test
	public void testEquals$differentExchangeName() {
		final CuratorConfiguration sut = withDifferentExchangeName();
		assertThat(sut,not(equalTo(defaultCuratorConfiguration())));
	}

	@Test
	public void testEquals$differentQueueName() {
		final CuratorConfiguration sut = withDifferentQueueName();
		assertThat(sut,not(equalTo(defaultCuratorConfiguration())));
	}

	@Test
	public void testEquals$differentRequestRoutingKey() {
		final CuratorConfiguration sut = withDifferentRequestRoutingKey();
		assertThat(sut,not(equalTo(defaultCuratorConfiguration())));
	}

	@Test
	public void testEquals$differentResponseRoutingKey() {
		final CuratorConfiguration sut = withDifferentResponseRoutingKey();
		assertThat(sut,not(equalTo(defaultCuratorConfiguration())));
	}

	@Test
	public void testHashCode$equal() {
		final CuratorConfiguration sut = defaultCuratorConfiguration();
		assertThat(sut.hashCode(),equalTo(defaultCuratorConfiguration().hashCode()));
	}

	@Test
	public void testHashCode$differentBroker() {
		final CuratorConfiguration sut = withDifferentBroker();
		assertThat(sut.hashCode(),not(equalTo(defaultCuratorConfiguration().hashCode())));
	}

	@Test
	public void testHashCode$differentExchangeName() {
		final CuratorConfiguration sut = withDifferentExchangeName();
		assertThat(sut.hashCode(),not(equalTo(defaultCuratorConfiguration().hashCode())));
	}

	@Test
	public void testHashCode$differentQueueName() {
		final CuratorConfiguration sut = withDifferentQueueName();
		assertThat(sut.hashCode(),not(equalTo(defaultCuratorConfiguration().hashCode())));
	}

	@Test
	public void testHashCode$differentRequestRoutingKey() {
		final CuratorConfiguration sut = withDifferentRequestRoutingKey();
		assertThat(sut.hashCode(),not(equalTo(defaultCuratorConfiguration().hashCode())));
	}

	@Test
	public void testHashCode$differentResponseRoutingKey() {
		final CuratorConfiguration sut = withDifferentResponseRoutingKey();
		assertThat(sut.hashCode(),not(equalTo(defaultCuratorConfiguration().hashCode())));
	}

	@Test
	public void testHasCustomString() {
		final CuratorConfiguration sut = CuratorConfiguration.newInstance();
		assertThat(sut.toString(),not(equalTo(Utils.defaultToString(sut))));
	}

	@Test
	public void testWithBroker(@Mocked final Broker broker) throws Exception {
		assertThat(CuratorConfiguration.newInstance().withBroker(broker).broker(),equalTo(broker));
	}

}
