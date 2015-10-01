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


final class CURATOR {

	static final String NAMESPACE = "http://www.smartdeveloperhub.org/vocabulary/curator#";
	static final String PREFIX    = "curator";

	static final String DELIVERY_CHANNEL_TYPE = term("DeliveryChannel");
	static final String AGENT_ID = term("agentId");
	static final String TARGET_RESOURCE = term("targetResource");
	static final String REPLY_TO = term("replyTo");
	static final String SUBMITTED_ON = term("submittedOn");
	static final String SUBMITTED_BY = term("submittedBy");
	static final String MESSAGE_ID = term("messageId");
	static final String ENRICHMENT_REQUEST_TYPE = term("EnrichmentRequest");
	static final String ENRICHMENT_RESPONSE_TYPE = term("EnrichmentResponse");
	static final String RESPONSE_TO = term("responseTo");
	static final String RESPONSE_NUMBER = term("responseNumber");
	static final String ADDITION_TARGET = term("additionTarget");
	static final String REMOVAL_TARGET = term("removalTarget");

	private CURATOR() {
	}

	static String term(String localName) {
		return NAMESPACE+localName;
	}

}
