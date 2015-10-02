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

import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Disconnect;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;
import org.smartdeveloperhub.curator.protocol.Failure;
import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.Response;

import com.google.common.base.Preconditions;
import com.rabbitmq.client.ConnectionFactory;

public final class ProtocolFactory {

	public interface Builder<T> {

		T build();

	}

	public static final class BrokerBuilder implements Builder<Broker> {

		private String host;
		private Integer port;
		private String virtualHost;

		private BrokerBuilder() {
		}

		public BrokerBuilder withHost(String host) {
			ValidationUtils.validateHostname(host);
			this.host=host;
			return this;
		}

		public BrokerBuilder withPort(int port) {
			ValidationUtils.validatePort(port);
			this.port=port;
			return this;
		}

		public BrokerBuilder withVirtualHost(String virtualHost) {
			ValidationUtils.validatePath(virtualHost);
			this.virtualHost = virtualHost;
			return this;
		}

		@Override
		public Broker build() {
			return
				new ImmutableBroker(
					this.host!=null?this.host:ConnectionFactory.DEFAULT_HOST,
					this.port!=null?this.port:ConnectionFactory.DEFAULT_AMQP_PORT,
					this.virtualHost!=null?this.virtualHost:ConnectionFactory.DEFAULT_VHOST);
		}

	}

	public static final class AgentBuilder implements Builder<Agent> {

		private UUID agentId;

		private AgentBuilder() {
		}

		public AgentBuilder withAgentId(UUID agentId) {
			this.agentId=agentId;
			return this;
		}

		public AgentBuilder withAgentId(String agentId) {
			UUID id=ValidationUtils.toUUID(agentId);
			return withAgentId(id);
		}

		@Override
		public Agent build() {
			return
				new ImmutableAgent(
					Objects.requireNonNull(this.agentId,"Agent id cannot be null"));
		}

	}

	public static final class DeliveryChannelBuilder implements Builder<DeliveryChannel> {

		private Broker broker;
		private String exchangeName;
		private String queueName;
		private String routingKey;

		private DeliveryChannelBuilder() {
		}

		public DeliveryChannelBuilder withBroker(Broker broker) {
			this.broker=broker;
			return this;
		}

		public DeliveryChannelBuilder withBroker(Builder<Broker> builder) {
			return withBroker(builder.build());
		}

		public DeliveryChannelBuilder withExchangeName(String exchangeName) {
			ValidationUtils.validateName(exchangeName);
			this.exchangeName = exchangeName;
			return this;
		}

		public DeliveryChannelBuilder withQueueName(String queueName) {
			ValidationUtils.validateName(queueName);
			this.queueName = queueName;
			return this;
		}

		public DeliveryChannelBuilder withRoutingKey(String routingKey) {
			ValidationUtils.validateRoutingKey(routingKey);
			this.routingKey = routingKey;
			return this;
		}

		@Override
		public DeliveryChannel build() {
			return
				new ImmutableDeliveryChannel(
					this.broker,
					this.exchangeName,
					this.queueName,
					this.routingKey);
		}

	}

	public abstract static class MessageBuilder<T extends Message, B extends MessageBuilder<T,B>> implements Builder<T> {

		private final B builder;

		private UUID messageId;
		private DateTime submittedOn;
		private Agent agent;

		private DeliveryChannel deliveryChannel;

		private MessageBuilder(Class<? extends B> builderClass) {
			this.builder = builderClass.cast(this);
		}

		protected UUID id() {
			return Objects.requireNonNull(this.messageId,"Message identifier cannot be null");
		}

		protected DateTime submissionDate() {
			return Objects.requireNonNull(this.submittedOn,"Submission date cannot be null");
		}

		protected DeliveryChannel deliveryChannel() {
			return this.deliveryChannel;
		}

		protected Agent agent() {
			return this.agent;
		}

		protected B builder() {
			return this.builder;
		}

		public B withMessageId(UUID messageId) {
			this.messageId=messageId;
			return this.builder;
		}

		public B withMessageId(String messageId) {
			return withMessageId(UUID.fromString(messageId));
		}

		public B withSubmittedOn(DateTime submittedOn) {
			this.submittedOn = submittedOn;
			return this.builder;
		}

		public B withSubmittedOn(Date submittedOn) {
			this.submittedOn = new DateTime(submittedOn);
			return this.builder;
		}

		public B withSubmittedBy(Agent agent) {
			this.agent=agent;
			return this.builder;
		}

		public B withSubmittedBy(Builder<Agent> builder) {
			return withSubmittedBy(builder.build());
		}

		public B withReplyTo(DeliveryChannel deliveryChannel) {
			this.deliveryChannel=deliveryChannel;
			return this.builder;
		}

		public B withReplyTo(Builder<DeliveryChannel> builder) {
			return withReplyTo(builder.build());
		}

	}

	public static final class EnrichmentRequestBuilder extends MessageBuilder<EnrichmentRequest,EnrichmentRequestBuilder> {

		private URI targetResource;

		private EnrichmentRequestBuilder() {
			super(EnrichmentRequestBuilder.class);
		}

		public EnrichmentRequestBuilder withTargetResource(URI targetResource) {
			this.targetResource = targetResource;
			return this;
		}

		@Override
		public EnrichmentRequest build() {
			return
				new ImmutableEnrichnmentRequest(
					id(),
					submissionDate(),
					agent(),
					Objects.requireNonNull(deliveryChannel(),"Reply delivery channel cannot be null"),
					Objects.requireNonNull(this.targetResource,"Target resource cannot be null"));
		}

	}

	public static final class DisconnectBuilder extends MessageBuilder<Disconnect,DisconnectBuilder> {

		private DisconnectBuilder() {
			super(DisconnectBuilder.class);
		}

		@Override
		public Disconnect build() {
			return
				new ImmutableDisconnect(
					id(),
					submissionDate(),
					agent());
		}

	}

	public abstract static class ResponseBuilder<T extends Response, B extends ResponseBuilder<T,B>> extends MessageBuilder<T,B> {

		private UUID responseTo;
		private Long responseNumber;

		private ResponseBuilder(Class<? extends B> builderClass) {
			super(builderClass);
		}

		protected UUID responseTo() {
			return Objects.requireNonNull(this.responseTo,"ResponseTo Message identifier cannot be null");
		}

		protected long responseNumber() {
			Objects.requireNonNull(this.responseNumber,"Response number cannot be null");
			Preconditions.checkArgument(this.responseNumber>=0,"Response number must be greater than 0 (%s)",this.responseNumber);
			return this.responseNumber;
		}

		public final B withResponseTo(UUID messageId) {
			this.responseTo = messageId;
			return builder();
		}

		public final B withMessageId(String messageId) {
			return withMessageId(UUID.fromString(messageId));
		}

		public final B withResponseNumber(long responseNumber) {
			this.responseNumber = responseNumber;
			return builder();
		}

	}

	public static final class AcceptedBuilder extends ResponseBuilder<Accepted,AcceptedBuilder> {

		private AcceptedBuilder() {
			super(AcceptedBuilder.class);
		}

		@Override
		public Accepted build() {
			return
				new ImmutableAccepted(
					id(),
					submissionDate(),
					agent(),
					responseTo(),
					responseNumber());
		}

	}

	public static final class FailureBuilder extends ResponseBuilder<Failure,FailureBuilder> {

		private Long code;
		private Long subcode;

		private String reason;
		private String detail;

		private FailureBuilder() {
			super(FailureBuilder.class);
		}

		public FailureBuilder withCode(long code) {
			this.code=code;
			return this;
		}

		public FailureBuilder withSubcode(long subcode) {
			this.subcode = subcode;
			return this;
		}

		public FailureBuilder withReason(String reason) {
			this.reason = reason;
			return this;
		}

		public FailureBuilder withDetail(String detail) {
			this.detail = detail;
			return this;
		}

		@Override
		public Failure build() {
			Objects.requireNonNull(this.code,"Failure code cannot be null");
			Preconditions.checkArgument(this.code>=0, "Failure code must be greater or equal than 0 (%s)",this.code);
			if(this.subcode!=null) {
				Preconditions.checkArgument(this.subcode>=0, "Failure subcode must be greater or equal than 0 (%s)",this.code);
			}
			Objects.requireNonNull(this.reason, "Failure reason cannot be null");
			return
				new ImmutableFailure(
					id(),
					submissionDate(),
					agent(),
					responseTo(),
					responseNumber(),
					this.code,
					this.subcode,
					this.reason,
					this.detail);
		}

	}

	public static final class EnrichmentResponseBuilder extends ResponseBuilder<EnrichmentResponse,EnrichmentResponseBuilder> {

		private URI targetResource;
		private URI additionTarget;
		private URI removalTarget;

		private EnrichmentResponseBuilder() {
			super(EnrichmentResponseBuilder.class);
		}

		public EnrichmentResponseBuilder withTargetResource(URI targetResource) {
			this.targetResource = targetResource;
			return this;
		}

		public EnrichmentResponseBuilder withAdditionTarget(URI additionTarget) {
			this.additionTarget = additionTarget;
			return this;
		}

		public EnrichmentResponseBuilder withRemovalTarget(URI removalTarget) {
			this.removalTarget = removalTarget;
			return this;
		}

		@Override
		public EnrichmentResponse build() {
			Preconditions.checkArgument(this.additionTarget!=this.removalTarget || this.additionTarget==null,"Addition target and removal target resources must be different");
			return
				new ImmutableEnrichmentResponse(
					id(),
					submissionDate(),
					agent(),
					responseTo(),
					responseNumber(),
					Objects.requireNonNull(this.targetResource,"Target resource cannot be null"),
					Objects.requireNonNull(this.additionTarget,"Addition target resource cannot be null"),
					Objects.requireNonNull(this.removalTarget,"Removal target resource cannot be null"));
		}

	}

	private ProtocolFactory() {
	}

	public static BrokerBuilder newBroker() {
		return new BrokerBuilder();
	}

	public static AgentBuilder newAgent() {
		return new AgentBuilder();
	}

	public static DeliveryChannelBuilder newDeliveryChannel() {
		return new DeliveryChannelBuilder();
	}

	public static EnrichmentRequestBuilder newEnrichmentRequest() {
		return new EnrichmentRequestBuilder();
	}

	public static DisconnectBuilder newDisconnect() {
		return new DisconnectBuilder();
	}

	public static AcceptedBuilder newAccepted() {
		return new AcceptedBuilder();
	}

	public static FailureBuilder newFailure() {
		return new FailureBuilder();
	}

	public static EnrichmentResponseBuilder newEnrichmentResponse() {
		return new EnrichmentResponseBuilder();
	}

}
