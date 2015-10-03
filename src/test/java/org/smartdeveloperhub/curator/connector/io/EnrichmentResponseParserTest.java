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
package org.smartdeveloperhub.curator.connector.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class EnrichmentResponseParserTest {

	@Test
	public void testFromModel$happyPath() {
		new ParserTester("messages/enrichment_response.ttl",CURATOR.ENRICHMENT_RESPONSE_TYPE) {
			@Override
			protected void exercise(Model model, Resource target) {
				EnrichmentResponse result=EnrichmentResponseParser.fromModel(model, target);
				assertThat(result,notNullValue());
				System.out.println(result);
			}
		}.verify();
	}

//	@Test
//	public void testFromModel$fail$multiple() {
//		new ParserTester("data/agent/multiple.ttl",FOAF.AGENT_TYPE) {
//			@Override
//			protected void exercise(Model model, Resource target) {
//				try {
//					AgentParser.fromModel(model, target);
//					fail("Should not return an agent when multiple are available");
//				} catch (Exception e) {
//					assertThat(e.getMessage(),equalTo("Too many Agent definitions for resource '"+target+"'"));
//					assertThat(e.getCause(),nullValue());
//				}
//			}
//		}.verify();
//	}

}
