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
package org.smartdeveloperhub.curator.protocol.vocabulary;


public final class CURATOR {

	public static final String NAMESPACE = "http://www.smartdeveloperhub.org/vocabulary/curator#";
	public static final String PREFIX    = "curator";

	public static final String DELIVERY_CHANNEL_TYPE = term("DeliveryChannel");
	public static final String AGENT_ID = term("agentId");
	public static final String TARGET_RESOURCE = term("targetResource");
	public static final String REPLY_TO = term("replyTo");
	public static final String SUBMITTED_ON = term("submittedOn");
	public static final String SUBMITTED_BY = term("submittedBy");
	public static final String MESSAGE_ID = term("messageId");
	public static final String ENRICHMENT_REQUEST_TYPE = term("EnrichmentRequest");
	public static final String ENRICHMENT_RESPONSE_TYPE = term("EnrichmentResponse");
	public static final String RESPONSE_TO = term("responseTo");
	public static final String RESPONSE_NUMBER = term("responseNumber");
	public static final String ADDITION_TARGET = term("additionTarget");
	public static final String REMOVAL_TARGET = term("removalTarget");

	private CURATOR() {
	}

	private static String term(String localName) {
		return NAMESPACE+localName;
	}

}
