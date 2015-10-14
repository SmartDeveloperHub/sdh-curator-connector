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
import java.net.URI;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.Notifier;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.BindingBuilder;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentResponseMessageBuilder;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.ResponseMessageBuilder;
import org.smartdeveloperhub.curator.connector.io.InvalidDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.connector.io.NoDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.TooManyDefinitionsFoundException;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.RequestMessage;
import org.smartdeveloperhub.curator.protocol.ResponseMessage;
import org.smartdeveloperhub.curator.protocol.Value;

public final class SimpleCurator implements MessageHandler {

	private static final Logger LOGGER=LoggerFactory.getLogger(SimpleCurator.class);

	private final DeliveryChannel connectorConfiguration;
	private final Notifier notifier;
	private final ResponseProvider provider;

	private ServerCuratorController curatorController;
	private ServerConnectorController connectorController;

	public SimpleCurator(DeliveryChannel connectorConfiguration, Notifier notifier, ResponseProvider provider) {
		this.connectorConfiguration = connectorConfiguration;
		this.notifier = notifier;
		this.provider = provider;
	}

	public void connect() throws IOException, ControllerException {
		this.curatorController=new ServerCuratorController(CuratorConfiguration.newInstance(),"curator");
		this.connectorController=new ServerConnectorController(this.connectorConfiguration, this.curatorController);
		this.curatorController.connect();
		this.curatorController.handleRequests(this);
		this.connectorController.connect();
	}

	public void disconnect() throws ControllerException {
		this.connectorController.disconnect();
		this.curatorController.disconnect();
	}

	@Override
	public void handlePayload(String payload) {
		EnrichmentRequestMessage erRequest=parsePayload(payload, EnrichmentRequestMessage.class);
		if(erRequest!=null) {
			processEnrichmentRequest(erRequest);
			return;
		}
		DisconnectMessage dRequest=parsePayload(payload,DisconnectMessage.class);
		if(dRequest!=null) {
			processDisconnect(dRequest);
			return;
		}
		LOGGER.error("Could not understand request:\n{}",payload);
	}

	private <T extends RequestMessage> T parsePayload(String payload, final Class<? extends T> messageClass) {
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

	private void processDisconnect(DisconnectMessage request) {
		LOGGER.info("Received disconnect from {}",request.submittedBy().agentId());
		this.notifier.onRequest(request);
	}

	private void processEnrichmentRequest(EnrichmentRequestMessage request) {
		this.notifier.onRequest(request);
		LOGGER.info("Received enrichment request {} from {}...",request.messageId(),request.submittedBy().agentId());
		ResponseMessage acknowledgement = createAcknowledgement(request);
		acknowledgeRequest(acknowledgement);
		if(acknowledgement instanceof AcceptedMessage) {
			LOGGER.info("Accepted enrichment request {} from {} with response {}",request.messageId(),request.submittedBy().agentId(),acknowledgement.messageId());
			ResponseMessage enrichment = createEnrichmentResponse(request);
			replyToEnrichment(enrichment);
		} else {
			LOGGER.info("Rejected enrichment request {} from {} with response {}",request.messageId(),request.submittedBy().agentId(),acknowledgement.messageId());
		}
	}

	private void acknowledgeRequest(ResponseMessage response) {
		try {
			sleep(TimeUnit.MILLISECONDS,150);
			this.curatorController.publishResponse(response);
			this.notifier.onResponse(response);
		} catch (IOException e) {
			LOGGER.error("Could not acknowledge {}",response,e);
		}
	}

	private void replyToEnrichment(ResponseMessage response) {
		try {
			sleep(TimeUnit.MILLISECONDS,150);
			this.connectorController.publishMessage(response);
			this.notifier.onResponse(response);
		} catch (IOException e) {
			LOGGER.error("Could not reply {}",response,e);
		}
	}

	private ResponseMessage createAcknowledgement(RequestMessage request) {
		ResponseMessageBuilder<?,?> builder=null;
		if(this.provider.isAccepted(request.messageId())) {
			builder=ProtocolFactory.newAcceptedMessage();
		} else {
			final Failure failure = this.provider.getFailure(request.messageId());
			builder=
				ProtocolFactory.
					newFailureMessage().
						withCode(failure.code()).
						withSubcode(failure.subcode().orNull()).
						withReason(failure.reason()).
						withDetail(failure.details());
		}
		return
			builder.
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

	private ResponseMessage createEnrichmentResponse(EnrichmentRequestMessage request) {
		final EnrichmentResult result = this.provider.getResult(request.messageId());
		final EnrichmentResponseMessageBuilder builder =
			ProtocolFactory.
				newEnrichmentResponseMessage().
					withMessageId(UUID.randomUUID()).
					withSubmittedOn(new Date()).
					withSubmittedBy(
						ProtocolFactory.
							newAgent().
								withAgentId(UUID.randomUUID())).
					withResponseTo(request.messageId()).
					withResponseNumber(1);
		if(result==null) {
			generateResult(request,builder);
		} else {
			populateResult(result, builder);
		}
		return	builder.build();
	}

	private void generateResult(EnrichmentRequestMessage request, EnrichmentResponseMessageBuilder builder) {
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
		builder.withTargetResource(request.targetResource());
	}

	private void populateResult(EnrichmentResult result, EnrichmentResponseMessageBuilder builder) {
		builder.withTargetResource(result.targetResource());
		for(URI property:result.addedProperties()) {
			builder.
			withAddition(
				createBinding(property, result.addedValue(property)));
		}
		for(URI property:result.removedProperties()) {
			builder.
				withRemoval(
					createBinding(property, result.removedValue(property)));
		}
	}

	private BindingBuilder createBinding(URI property, final Value value) {
		return
			ProtocolFactory.
				newBinding().
					withProperty(property).
					withValue(value);
	}

	private void sleep(final TimeUnit unit, final int timeout) {
		try {
			unit.sleep(timeout);
		} catch (InterruptedException e) {
			// IGNORE INTERRUPTION
		}
	}

}