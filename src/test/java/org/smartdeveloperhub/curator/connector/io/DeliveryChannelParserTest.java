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
package org.smartdeveloperhub.curator.connector.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class DeliveryChannelParserTest {

	@Test
	public void testFromModel$happyPath() {
		new ParserTester("data/deliveryChannel/full.ttl",STOA.DELIVERY_CHANNEL_TYPE) {
			@Override
			protected void exercise(final Model model, final Resource target) {
				final DeliveryChannel result=DeliveryChannelParser.fromModel(model, target);
				assertThat(result,notNullValue());
				System.out.println(result);
			}
		}.verify();
	}

	@Test
	public void testFromModel$fail$noRoutingKey() {
		new ParserTester("data/deliveryChannel/missing_routing_key.ttl",STOA.DELIVERY_CHANNEL_TYPE) {
			@Override
			protected void exercise(final Model model, final Resource target) {
				try {
					DeliveryChannelParser.fromModel(model, target);
					fail("Should not return a delivery channel without routing key");
				} catch (final Exception e) {
					assertThat(e.getMessage(),equalTo("Variable routingKey (literal) not bound when resolving property amqp:routingKey of resource "+target+""));
					assertThat(e.getCause(),nullValue());
				}
			}
		}.verify();
	}

}
