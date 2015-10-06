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

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.io.InvalidDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.connector.io.NoDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.TooManyDefinitionsFoundException;
import org.smartdeveloperhub.curator.protocol.Disconnect;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.Request;
import org.smartdeveloperhub.curator.protocol.Response;

final class ExampleCurator {

	private final class CuratorMessageHandler implements MessageHandler {

		@Override
		public void handlePayload(String payload) {
			Request request=parsePayload(payload, EnrichmentRequest.class);
			if(request!=null) {
				LOGGER.info("Received enrichment request {} from {}...",request.messageId(),request.submittedBy().agentId());
				Response response = createResponse(request);
				reply(response);
				LOGGER.info("Accepted enrichment request {} from {} with response {}",request.messageId(),request.submittedBy().agentId(),response.messageId());
			} else {
				request=parsePayload(payload,Disconnect.class);
				if(request==null) {
					LOGGER.error("Could not understand request:\n{}",payload);
				} else {
					LOGGER.info("Received disconnect from {}",request.submittedBy().agentId());
					if(!ExampleCurator.this.disconnectable.isTerminated()) {
						ExampleCurator.this.disconnectable.arriveAndAwaitAdvance();
					}
				}
			}
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ExampleCurator.class);

	private final Phaser disconnectable;

	private CuratorController controller;

	ExampleCurator(Phaser disconnectable) {
		this.disconnectable = disconnectable;
	}

	void connect() throws IOException, ControllerException {
		this.controller = new CuratorController(CuratorConfiguration.newInstance(),"example-curator");
		this.controller.connect();
		this.controller.handleRequests(new CuratorMessageHandler());
	}

	void disconnect() throws ControllerException {
		if(!this.disconnectable.isTerminated()) {
			this.disconnectable.arrive();
		}
		this.controller.disconnect();
	}

	private <T extends Request> T parsePayload(String payload, final Class<? extends T> messageClass) {
		T request=null;
		try {
			request=
				MessageUtil.
					newInstance().
						fromString(payload, messageClass);
		} catch (NoDefinitionFoundException e) {
			LOGGER.trace("Request cannot be parsed as {}",messageClass.getName(),payload);
		} catch (TooManyDefinitionsFoundException e) {
			LOGGER.trace("Too many {} definitions found",messageClass.getName(),payload);
		} catch (InvalidDefinitionFoundException e) {
			LOGGER.trace("Could not parse a valid {} from the payload:\n{}",messageClass.getName(),messageClass.getName(),payload,e);
		} catch (MessageConversionException e) {
			LOGGER.trace("Failed to parse the payload:\n{}",payload,e);
		}
		return request;
	}

	private Response createResponse(Request request) {
		return
			ProtocolFactory.
				newAccepted().
					withMessageId(UUID.randomUUID()).
					withSubmittedOn(new Date()).
					withSubmittedBy(
						ProtocolFactory.
							newAgent().
								withAgentId(UUID.randomUUID())).
					withResponseTo(request.messageId()).
					withResponseNumber(1).
					build();
	}

	private void reply(Response response) {
		try {
			try {
				TimeUnit.MILLISECONDS.sleep(150);
			} catch (InterruptedException e) {
			}
			this.controller.publishResponse(response);
		} catch (IOException e) {
			LOGGER.error("Could not reply {}",response,e);
		}
	}

}