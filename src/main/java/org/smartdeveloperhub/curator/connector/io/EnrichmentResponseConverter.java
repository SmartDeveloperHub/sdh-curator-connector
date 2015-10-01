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

import static org.smartdeveloperhub.curator.connector.io.Namespaces.curator;
import static org.smartdeveloperhub.curator.connector.io.Namespaces.foaf;
import static org.smartdeveloperhub.curator.connector.io.Namespaces.types;
import static org.smartdeveloperhub.curator.connector.io.Namespaces.xsd;

import java.net.URI;

import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;

final class EnrichmentResponseConverter extends ModelMessageConverter<EnrichmentResponse> {

	@Override
	protected void toString(EnrichmentResponse message, ModelHelper helper) {
		helper.
			blankNode("response").
				type(curator("EnrichmentResponse")).
				property(curator("messageId")).
					withTypedLiteral(message.messageId(), types("UUID")).
				property(curator("submittedBy")).
					withBlankNode("agent").
				property(curator("submittedOn")).
					withTypedLiteral(message.submittedOn(), xsd("dateTimeStamp")).
				property(curator("responseTo")).
					withTypedLiteral(message.responseTo(), types("UUID")).
				property(curator("responseNumber")).
					withTypedLiteral(message.responseNumber(), xsd("unsignedLong")).
				property(curator("targetResource")).
					withResource(message.targetResource()).
			blankNode("agent").
				type(foaf("Agent")).
				property(curator("agentId")).
					withTypedLiteral(message.submittedBy().agentId(), types("UUID"));
		target(helper,"additionTarget",message.additionTarget());
		target(helper,"removalTarget",message.removalTarget());
	}

	private void target(ModelHelper helper, String property, URI value) {
		if(value!=null) {
			helper.
				blankNode("response").
					property(curator(property)).
						withResource(value);
		}
	}

}
