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

import java.util.List;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.rdf.ModelHelper;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class EnrichmentResponseMessageConverter extends ModelMessageConverter<EnrichmentResponseMessage> {

	private static final String AGENT_BNODE    = "agent";
	private static final String RESPONSE_BNODE = "response";
	private static final String ADDITION_BNODE = "addition";
	private static final String REMOVAL_BNODE  = "removal";

	@Override
	protected void toString(EnrichmentResponseMessage message, ModelHelper helper) {
		EnrichmentUtil util=
			EnrichmentUtil.
				builder().
					withBindings(message.additions()).
					withBindings(message.removals()).
					build();
		helper.
			blankNode(util.blankNode(RESPONSE_BNODE)).
				type(STOA.ENRICHMENT_RESPONSE_TYPE).
				property(STOA.MESSAGE_ID).
					withTypedLiteral(message.messageId(), TYPES.UUID_TYPE).
				property(STOA.SUBMITTED_BY).
					withBlankNode(util.blankNode(AGENT_BNODE)).
				property(STOA.SUBMITTED_ON).
					withTypedLiteral(message.submittedOn(), XSD.DATE_TIME_TYPE).
				property(STOA.RESPONSE_TO).
					withTypedLiteral(message.responseTo(), TYPES.UUID_TYPE).
				property(STOA.RESPONSE_NUMBER).
					withTypedLiteral(message.responseNumber(), XSD.UNSIGNED_LONG_TYPE).
				property(STOA.TARGET_RESOURCE).
					withResource(message.targetResource()).
			blankNode(util.blankNode(AGENT_BNODE)).
				type(FOAF.AGENT_TYPE).
				property(STOA.AGENT_ID).
					withTypedLiteral(message.submittedBy().agentId(), TYPES.UUID_TYPE);
		serializeBindings(util,helper,STOA.ADDITION_TARGET,ADDITION_BNODE,message.additions());
		serializeBindings(util,helper,STOA.REMOVAL_TARGET,REMOVAL_BNODE,message.removals());
	}

	private void serializeBindings(EnrichmentUtil util, ModelHelper helper, String property, String bnode, List<Binding> bindings) {
		helper.
			blankNode(util.blankNode(RESPONSE_BNODE)).
				property(property).
					withBlankNode(bnode);
		final Variable target=ProtocolFactory.newVariable(bnode);
		final BindingSerializer serializer=BindingSerializer.newInstance(helper);
		for(Binding binding:bindings) {
			serializer.serialize(target,binding);
		}
	}

	@Override
	protected EnrichmentResponseMessage parse(Model model, Resource resource) {
		return EnrichmentResponseMessageParser.fromModel(model, resource);
	}

	@Override
	protected String messageType() {
		return STOA.ENRICHMENT_RESPONSE_TYPE;
	}

}
