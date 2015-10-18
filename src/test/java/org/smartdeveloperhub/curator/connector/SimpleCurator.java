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
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.BindingBuilder;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.EnrichmentResponseMessageBuilder;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.ResponseMessageBuilder;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
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

	public SimpleCurator(final DeliveryChannel connectorConfiguration, final Notifier notifier, final ResponseProvider provider) {
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
	public void handlePayload(final String payload) {
		final EnrichmentRequestMessage erRequest=HandlerUtil.parsePayload(payload,EnrichmentRequestMessage.class);
		if(erRequest!=null) {
			processEnrichmentRequest(erRequest);
			return;
		}
		final DisconnectMessage dRequest=HandlerUtil.parsePayload(payload,DisconnectMessage.class);
		if(dRequest!=null) {
			processDisconnect(dRequest);
			return;
		}
		LOGGER.error("Could not understand request:\n{}",payload);
	}

	private void processDisconnect(final DisconnectMessage request) {
		LOGGER.info("Received disconnect from {}",request.submittedBy().agentId());
		this.notifier.onRequest(request);
	}

	private void processEnrichmentRequest(final EnrichmentRequestMessage request) {
		this.notifier.onRequest(request);
		LOGGER.info("Received enrichment request {} from {}...",request.messageId(),request.submittedBy().agentId());
		if(this.provider.isAccepted(request.messageId())) {
			final ResponseMessage acknowledgement = acceptEnrichmentRequest(request);
			LOGGER.info(
				"Accepted enrichment request {} from {}{}",
				request.messageId(),
				request.submittedBy().agentId(),
				acknowledgement!=null?
					" with response "+acknowledgement.messageId():
					"");
		} else if(this.provider.isExpected(request.messageId())) {
			final ResponseMessage acknowledgement = rejectEnrichmentRequest(request);
			LOGGER.info(
				"Rejected enrichment request {} from {}{}",
				request.messageId(),
				request.submittedBy().agentId(),
				acknowledgement!=null?
					" with response "+acknowledgement.messageId():
					"");
		} else {
			LOGGER.info("Ignored enrichment request {} from {}",request.messageId(),request.submittedBy().agentId());
		}
	}

	private ResponseMessage acceptEnrichmentRequest(final EnrichmentRequestMessage request) {
		final ResponseMessage acknowledgement=completeResponse(ProtocolFactory.newAcceptedMessage(),request);
		acknowledgeRequest(request.messageId(),acknowledgement);
		final ResponseMessage enrichment = createEnrichmentResponse(request);
		replyToEnrichment(request.messageId(),enrichment);
		return acknowledgement;
	}

	private ResponseMessage rejectEnrichmentRequest(final EnrichmentRequestMessage request) {
		ResponseMessage acknowledgement=null;
		final Failure failure = this.provider.getFailure(request.messageId());
		if(failure!=null) {
			final ResponseMessageBuilder<?,?> builder=
				ProtocolFactory.
					newFailureMessage().
						withCode(failure.code()).
						withSubcode(failure.subcode().orNull()).
						withReason(failure.reason()).
						withDetail(failure.details());
			acknowledgement = completeResponse(builder, request);
		}
		acknowledgeRequest(request.messageId(),acknowledgement);
		return acknowledgement;
	}

	private void acknowledgeRequest(final UUID requestId, final ResponseMessage response) {
		try {
			sleep(TimeUnit.MILLISECONDS,this.provider.acknowledgeDelay(requestId,TimeUnit.MILLISECONDS));
			if(response!=null) {
				this.curatorController.publishResponse(response);
				this.notifier.onResponse(response);
			} else {
				this.curatorController.publishMessage("invalid acknowledge",this.curatorController.curatorConfiguration().responseRoutingKey());
				this.notifier.onError(requestId);
			}
		} catch (final IOException e) {
			LOGGER.error("Could not acknowledge {}",response,e);
		}
	}

	private void replyToEnrichment(final UUID requestId, final ResponseMessage response) {
		try {
			sleep(TimeUnit.MILLISECONDS,this.provider.acknowledgeDelay(requestId,TimeUnit.MILLISECONDS));
			if(response!=null) {
				this.connectorController.publishMessage(response);
				this.notifier.onResponse(response);
			} else {
				this.connectorController.publishMessage("invalid response");
				this.notifier.onError(requestId);
			}
		} catch (final IOException e) {
			LOGGER.error("Could not reply {}",response,e);
		}
	}

	private ResponseMessage completeResponse(final ResponseMessageBuilder<?, ?> builder, final RequestMessage request) {
		return builder.
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

	private ResponseMessage createEnrichmentResponse(final EnrichmentRequestMessage request) {
		final EnrichmentResult result = this.provider.getResult(request.messageId());
		ResponseMessage response=null;
		if(result!=null) {
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
			populateResult(result, builder);
			response=builder.build();
		}
		return response;
	}

	private void populateResult(final EnrichmentResult result, final EnrichmentResponseMessageBuilder builder) {
		builder.withTargetResource(result.targetResource());
		for(final URI property:result.addedProperties()) {
			builder.
			withAddition(
				createBinding(property, result.addedValue(property)));
		}
		for(final URI property:result.removedProperties()) {
			builder.
				withRemoval(
					createBinding(property, result.removedValue(property)));
		}
	}

	private BindingBuilder createBinding(final URI property, final Value value) {
		return
			ProtocolFactory.
				newBinding().
					withProperty(property).
					withValue(value);
	}

	private void sleep(final TimeUnit unit, final long timeout) {
		try {
			unit.sleep(timeout);
		} catch (final InterruptedException e) {
			// IGNORE INTERRUPTION
		}
	}

}