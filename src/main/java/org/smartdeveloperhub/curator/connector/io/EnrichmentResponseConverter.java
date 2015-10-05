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

import java.net.URI;

import org.smartdeveloperhub.curator.connector.rdf.ModelHelper;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class EnrichmentResponseConverter extends ModelMessageConverter<EnrichmentResponse> {

	private static final String RESPONSE_BNODE = "response";

	@Override
	protected void toString(EnrichmentResponse message, ModelHelper helper) {
		helper.
			blankNode(RESPONSE_BNODE).
				type(CURATOR.ENRICHMENT_RESPONSE_TYPE).
				property(CURATOR.MESSAGE_ID).
					withTypedLiteral(message.messageId(), TYPES.UUID_TYPE).
				property(CURATOR.SUBMITTED_BY).
					withBlankNode("agent").
				property(CURATOR.SUBMITTED_ON).
					withTypedLiteral(message.submittedOn(), XSD.DATE_TIME_TYPE).
				property(CURATOR.RESPONSE_TO).
					withTypedLiteral(message.responseTo(), TYPES.UUID_TYPE).
				property(CURATOR.RESPONSE_NUMBER).
					withTypedLiteral(message.responseNumber(), XSD.UNSIGNED_LONG_TYPE).
				property(CURATOR.TARGET_RESOURCE).
					withResource(message.targetResource()).
			blankNode("agent").
				type(FOAF.AGENT_TYPE).
				property(CURATOR.AGENT_ID).
					withTypedLiteral(message.submittedBy().agentId(), TYPES.UUID_TYPE);
		target(helper,CURATOR.ADDITION_TARGET,message.additionTarget());
		target(helper,CURATOR.REMOVAL_TARGET,message.removalTarget());
	}

	private void target(ModelHelper helper, String property, URI value) {
		if(value!=null) {
			helper.
				blankNode(RESPONSE_BNODE).
					property(property).
						withResource(value);
		}
	}

	@Override
	protected EnrichmentResponse parse(Model model, Resource resource) {
		return EnrichmentResponseParser.fromModel(model, resource);
	}

	@Override
	protected String messageType() {
		return CURATOR.ENRICHMENT_RESPONSE_TYPE;
	}

}
