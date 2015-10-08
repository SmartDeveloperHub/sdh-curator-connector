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
import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentResponseBuilder;
import org.smartdeveloperhub.curator.connector.io.InvalidDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.connector.io.NoDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.TooManyDefinitionsFoundException;
import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Disconnect;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Request;
import org.smartdeveloperhub.curator.protocol.Response;

final class ExampleCurator implements MessageHandler {

	private static final Logger LOGGER=LoggerFactory.getLogger(ExampleCurator.class);

	private final DeliveryChannel connectorConfiguration;
	private final Phaser disconnectable;
	private final Phaser answered;

	private ServerCuratorController curatorController;
	private ServerConnectorController connectorController;

	ExampleCurator(DeliveryChannel connectorConfiguration, Phaser disconnectable, Phaser answered) {
		this.connectorConfiguration = connectorConfiguration;
		this.disconnectable = disconnectable;
		this.answered = answered;
	}

	void connect() throws IOException, ControllerException {
		this.curatorController=new ServerCuratorController(CuratorConfiguration.newInstance(),"example-curator");
		this.connectorController=new ServerConnectorController(this.connectorConfiguration, this.curatorController);
		this.curatorController.connect();
		this.curatorController.handleRequests(this);
		this.connectorController.connect();
	}

	void disconnect() throws ControllerException {
		if(!this.answered.isTerminated()) {
			this.answered.arrive();
		}
		if(!this.disconnectable.isTerminated()) {
			this.disconnectable.arrive();
		}
		this.connectorController.disconnect();
		this.curatorController.disconnect();
	}

	@Override
	public void handlePayload(String payload) {
		EnrichmentRequest erRequest=parsePayload(payload, EnrichmentRequest.class);
		if(erRequest!=null) {
			processEnrichmentRequest(erRequest);
			return;
		}
		Disconnect dRequest=parsePayload(payload,Disconnect.class);
		if(dRequest!=null) {
			processDisconnect(dRequest);
			return;
		}
		LOGGER.error("Could not understand request:\n{}",payload);
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

	private void processDisconnect(Disconnect request) {
		LOGGER.info("Received disconnect from {}",request.submittedBy().agentId());
		if(!this.disconnectable.isTerminated()) {
			this.disconnectable.arrive();
		}
	}

	private void processEnrichmentRequest(EnrichmentRequest request) {
		LOGGER.info("Received enrichment request {} from {}...",request.messageId(),request.submittedBy().agentId());
		Response acknowledgement = createAcknowledgement(request);
		acknowledgeRequest(acknowledgement);
		if(acknowledgement instanceof Accepted) {
			LOGGER.info("Accepted enrichment request {} from {} with response {}",request.messageId(),request.submittedBy().agentId(),acknowledgement.messageId());
			Response enrichment = createEnrichmentResponse(request);
			replyToEnrichment(enrichment);
		} else {
			LOGGER.info("Rejected enrichment request {} from {} with response {}",request.messageId(),request.submittedBy().agentId(),acknowledgement.messageId());
		}
	}

	private void acknowledgeRequest(Response response) {
		try {
			sleep(TimeUnit.MILLISECONDS,150);
			this.curatorController.publishResponse(response);
		} catch (IOException e) {
			LOGGER.error("Could not acknowledge {}",response,e);
		}
	}

	private void replyToEnrichment(Response response) {
		try {
			sleep(TimeUnit.MILLISECONDS,150);
			this.connectorController.publishMessage(response);
			if(!this.answered.isTerminated()) {
				this.answered.arrive();
			}
		} catch (IOException e) {
			LOGGER.error("Could not reply {}",response,e);
		}
	}

	private Response createAcknowledgement(Request request) {
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

	private Response createEnrichmentResponse(EnrichmentRequest request) {
		final EnrichmentResponseBuilder builder =
			ProtocolFactory.
				newEnrichmentResponse().
					withMessageId(UUID.randomUUID()).
					withSubmittedOn(new Date()).
					withSubmittedBy(
						ProtocolFactory.
							newAgent().
								withAgentId(UUID.randomUUID())).
					withResponseTo(request.messageId()).
					withResponseNumber(1).
					withTargetResource(request.targetResource());
		int counter=0;
		for(Filter filter:request.filters()) {
			final int id = counter++;
			builder.
				withAddition(
					ProtocolFactory.
						newBinding().
							withProperty(filter.property()).
							withValue(ProtocolFactory.newResource("value_"+id+"_"+filter.variable().name())));
		}
		return	builder.build();
	}

	private void sleep(final TimeUnit unit, final int timeout) {
		try {
			unit.sleep(timeout);
		} catch (InterruptedException e) {
			// IGNORE INTERRUPTION
		}
	}

}