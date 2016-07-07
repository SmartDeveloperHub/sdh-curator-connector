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
package org.smartdeveloperhub.curator.connector.io;

import org.smartdeveloperhub.curator.connector.rdf.ModelHelper;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class FailureMessageConverter extends ModelMessageConverter<FailureMessage> {

	private static final String RESPONSE_BNODE = "response";
	private static final String AGENT_BNODE    = "agent";

	@Override
	protected void toString(FailureMessage message, ModelHelper helper) {
		helper.
			blankNode(RESPONSE_BNODE).
				type(messageType()).
				property(STOA.MESSAGE_ID).
					withTypedLiteral(message.messageId(), TYPES.UUID_TYPE).
				property(STOA.SUBMITTED_BY).
					withBlankNode(AGENT_BNODE).
				property(STOA.SUBMITTED_ON).
					withTypedLiteral(message.submittedOn(), XSD.DATE_TIME_TYPE).
				property(STOA.RESPONSE_TO).
					withTypedLiteral(message.responseTo(), TYPES.UUID_TYPE).
				property(STOA.RESPONSE_NUMBER).
					withTypedLiteral(message.responseNumber(), XSD.UNSIGNED_LONG_TYPE).
				property(STOA.CODE).
					withTypedLiteral(message.code(), XSD.UNSIGNED_LONG_TYPE).
				property(STOA.REASON).
					withTypedLiteral(message.reason(), XSD.STRING_TYPE).
			blankNode(AGENT_BNODE).
				type(FOAF.AGENT_TYPE).
				property(STOA.AGENT_ID).
					withTypedLiteral(message.submittedBy().agentId(), TYPES.UUID_TYPE);
		if(message.subcode().isPresent()) {
			helper.
				blankNode(RESPONSE_BNODE).
					property(STOA.SUBCODE).
						withTypedLiteral(message.subcode().get(), XSD.UNSIGNED_LONG_TYPE);
		}
		if(message.detail()!=null) {
			helper.
				blankNode(RESPONSE_BNODE).
					property(STOA.DETAIL).
						withTypedLiteral(message.detail(), XSD.STRING_TYPE);
		}
	}

	@Override
	protected FailureMessage parse(Model model, Resource resource) {
		return FailureMessageParser.fromModel(model, resource);
	}

	@Override
	protected String messageType() {
		return STOA.FAILURE_TYPE;
	}

}
