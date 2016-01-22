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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.protocol;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.connector.util.Builder;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.RequestMessage;
import org.smartdeveloperhub.curator.protocol.Resource;
import org.smartdeveloperhub.curator.protocol.ResponseMessage;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDFS;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.google.common.collect.Lists;
import com.rabbitmq.client.ConnectionFactory;

public final class ProtocolFactory {

	public static final class BrokerBuilder implements Builder<Broker> {

		private String host;
		private Integer port;
		private String virtualHost;

		private BrokerBuilder() {
		}

		private void setPort(final Integer port) {
			this.port=port;
		}

		public BrokerBuilder withHost(final String host) {
			this.host=host;
			return this;
		}

		public BrokerBuilder withPort(final String port) {
			setPort(ParsingUtil.toPort(port));
			return this;
		}

		public BrokerBuilder withPort(final int port) {
			setPort(port);
			return this;
		}

		public BrokerBuilder withVirtualHost(final String virtualHost) {
			this.virtualHost = virtualHost;
			return this;
		}

		@Override
		public Broker build() {
			return
				new ImmutableBroker(
					this.host!=null?ValidationUtil.validateHostname(this.host):ConnectionFactory.DEFAULT_HOST,
					this.port!=null?ValidationUtil.validatePort(this.port):ConnectionFactory.DEFAULT_AMQP_PORT,
					this.virtualHost!=null?ValidationUtil.validatePath(this.virtualHost):ConnectionFactory.DEFAULT_VHOST);
		}

	}

	public static final class AgentBuilder implements Builder<Agent> {

		private UUID agentId;

		private AgentBuilder() {
		}

		public AgentBuilder withAgentId(final UUID agentId) {
			this.agentId=agentId;
			return this;
		}

		public AgentBuilder withAgentId(final String agentId) {
			return withAgentId(ParsingUtil.toUUID(agentId));
		}

		@Override
		public Agent build() {
			return
				new ImmutableAgent(
					ValidationUtil.checkNotNull(this.agentId,RDFS.RESOURCE_TYPE,"Agent identifier cannot be null"));
		}

	}

	public static final class DeliveryChannelBuilder implements Builder<DeliveryChannel> {

		private Broker broker;
		private String exchangeName;
		private String routingKey;

		private DeliveryChannelBuilder() {
		}

		public DeliveryChannelBuilder withBroker(final Broker broker) {
			this.broker=broker;
			return this;
		}

		public DeliveryChannelBuilder withBroker(final Builder<Broker> builder) {
			return withBroker(builder.build());
		}

		public DeliveryChannelBuilder withExchangeName(final String exchangeName) {
			this.exchangeName = exchangeName;
			return this;
		}

		public DeliveryChannelBuilder withRoutingKey(final String routingKey) {
			this.routingKey = routingKey;
			return this;
		}

		@Override
		public DeliveryChannel build() {
			return
				new ImmutableDeliveryChannel(
					this.broker,
					ValidationUtil.validateName(this.exchangeName),
					ValidationUtil.checkNotNull(
						ValidationUtil.validateRoutingKey(this.routingKey),
						AMQP.ROUTING_KEY_TYPE,
						"Routing key cannot be null"));
		}

	}

	public abstract static class MessageBuilder<T extends Message, B extends MessageBuilder<T,B>> implements Builder<T> {

		private final B builder;

		private UUID messageId;
		private DateTime submittedOn;
		private Agent agent;

		private MessageBuilder(final Class<? extends B> builderClass) {
			this.builder = builderClass.cast(this);
		}

		protected UUID id() {
			return ValidationUtil.checkNotNull(this.messageId,TYPES.UUID_TYPE,"Message identifier cannot be null");
		}

		protected DateTime submissionDate() {
			return ValidationUtil.checkNotNull(this.submittedOn,XSD.DATE_TIME_TYPE,"Submission date cannot be null");
		}

		protected Agent agent() {
			return ValidationUtil.checkNotNull(this.agent,FOAF.AGENT_TYPE,"Agent cannot be null");
		}

		protected B builder() {
			return this.builder;
		}

		public B withMessageId(final UUID messageId) {
			this.messageId=messageId;
			return this.builder;
		}

		public B withMessageId(final String messageId) {
			return withMessageId(ParsingUtil.toUUID(messageId));
		}

		public B withSubmittedOn(final DateTime submittedOn) {
			this.submittedOn = submittedOn;
			return this.builder;
		}

		public B withSubmittedOn(final Date submittedOn) {
			return withSubmittedOn(ParsingUtil.toDateTime(submittedOn));
		}

		public B withSubmittedOn(final String submittedOn) {
			return withSubmittedOn(ParsingUtil.toDateTime(submittedOn));
		}

		public B withSubmittedBy(final Agent agent) {
			this.agent=agent;
			return this.builder;
		}

		public B withSubmittedBy(final Builder<Agent> builder) {
			if(builder==null) {
				return this.builder;
			}
			return withSubmittedBy(builder.build());
		}

	}

	public abstract static class RequestMessageBuilder<T extends RequestMessage, B extends RequestMessageBuilder<T,B>> extends MessageBuilder<T,B> {

		private DeliveryChannel deliveryChannel;

		private RequestMessageBuilder(final Class<? extends B> builderClass) {
			super(builderClass);
		}

		protected DeliveryChannel deliveryChannel() {
			return this.deliveryChannel;
		}

		public B withReplyTo(final DeliveryChannel deliveryChannel) {
			this.deliveryChannel=deliveryChannel;
			return builder();
		}

		public B withReplyTo(final Builder<DeliveryChannel> builder) {
			if(builder==null) {
				return builder();
			}
			return withReplyTo(builder.build());
		}

	}

	public static final class EnrichmentRequestMessageBuilder extends RequestMessageBuilder<EnrichmentRequestMessage,EnrichmentRequestMessageBuilder> {

		private final List<Filter> filters;
		private final List<Constraint> constraints;

		private URI targetResource;

		private EnrichmentRequestMessageBuilder() {
			super(EnrichmentRequestMessageBuilder.class);
			this.filters=Lists.newArrayList();
			this.constraints=Lists.newArrayList();
		}

		public EnrichmentRequestMessageBuilder withTargetResource(final String targetResource) {
			return withTargetResource(ParsingUtil.toURI(targetResource));
		}

		public EnrichmentRequestMessageBuilder withTargetResource(final URI targetResource) {
			this.targetResource = targetResource;
			return this;
		}

		public EnrichmentRequestMessageBuilder withFilter(final Filter filter) {
			if(filter!=null) {
				this.filters.add(filter);
			}
			return this;
		}

		public EnrichmentRequestMessageBuilder withFilter(final Builder<Filter> builder) {
			if(builder==null) {
				return this;
			}
			return withFilter(builder.build());
		}

		public EnrichmentRequestMessageBuilder withConstraint(final Constraint constraint) {
			if(constraint!=null) {
				this.constraints.add(constraint);
			}
			return this;
		}

		public EnrichmentRequestMessageBuilder withConstraint(final Builder<Constraint> builder) {
			if(builder==null) {
				return this;
			}
			return withConstraint(builder.build());
		}


		@Override
		public EnrichmentRequestMessage build() {
			if(this.filters.isEmpty()) {
				throw new ValidationException(null,RDFS.RESOURCE_TYPE,"No enrichment request filters specified");
			}
			if(this.constraints.isEmpty()) {
				throw new ValidationException(null,RDFS.RESOURCE_TYPE,"No enrichment request constraints specified");
			}
			return
				new ImmutableEnrichmentRequestMessage(
					id(),
					submissionDate(),
					agent(),
					ValidationUtil.checkNotNull(deliveryChannel(),CURATOR.DELIVERY_CHANNEL_TYPE,"No enrichment request reply delivery channel specified"),
					ValidationUtil.checkNotNull(this.targetResource,RDFS.RESOURCE_TYPE,"No enrichment request target resource specified"),
					this.filters,
					this.constraints);
		}

	}

	public static final class DisconnectMessageBuilder extends RequestMessageBuilder<DisconnectMessage,DisconnectMessageBuilder> {

		private DisconnectMessageBuilder() {
			super(DisconnectMessageBuilder.class);
		}

		@Override
		public DisconnectMessage build() {
			return
				new ImmutableDisconnectMessage(
					id(),
					submissionDate(),
					agent());
		}

	}

	public abstract static class ResponseMessageBuilder<T extends ResponseMessage, B extends ResponseMessageBuilder<T,B>> extends MessageBuilder<T,B> {

		private UUID responseTo;
		private Long responseNumber;

		private ResponseMessageBuilder(final Class<? extends B> builderClass) {
			super(builderClass);
		}

		private void setResponseNumber(final Long responseNumber) {
			this.responseNumber = responseNumber;
		}

		protected UUID responseTo() {
			return ValidationUtil.checkNotNull(this.responseTo,TYPES.UUID_TYPE,"ResponseTo message identifier cannot be null");
		}

		protected long responseNumber() {
			return ValidationUtil.validateUnsignedLong(this.responseNumber,"Response number");
		}

		public final B withResponseTo(final String messageId) {
			return withResponseTo(ParsingUtil.toUUID(messageId));
		}

		public final B withResponseTo(final UUID messageId) {
			this.responseTo = messageId;
			return builder();
		}

		public final B withResponseNumber(final long responseNumber) {
			setResponseNumber(responseNumber);
			return builder();
		}

		public final B withResponseNumber(final String responseNumber) {
			setResponseNumber(ParsingUtil.toUnsignedLong(responseNumber));
			return builder();
		}

	}

	public static final class AcceptedMessageBuilder extends ResponseMessageBuilder<AcceptedMessage,AcceptedMessageBuilder> {

		private AcceptedMessageBuilder() {
			super(AcceptedMessageBuilder.class);
		}

		@Override
		public AcceptedMessage build() {
			return
				new ImmutableAcceptedMessage(
					id(),
					submissionDate(),
					agent(),
					responseTo(),
					responseNumber());
		}

	}

	public static final class FailureMessageBuilder extends ResponseMessageBuilder<FailureMessage,FailureMessageBuilder> {

		private Long code;
		private Long subcode;

		private String reason;
		private String detail;

		private FailureMessageBuilder() {
			super(FailureMessageBuilder.class);
		}

		private void setCode(final long code) {
			this.code=code;
		}

		private void setSubcode(final long subcode) {
			this.subcode=subcode;
		}

		public FailureMessageBuilder withCode(final long code) {
			setCode(code);
			return this;
		}

		public FailureMessageBuilder withCode(final String code) {
			setCode(ParsingUtil.toUnsignedLong(code));
			return this;
		}

		public FailureMessageBuilder withSubcode(final long subcode) {
			setSubcode(subcode);
			return this;
		}

		public FailureMessageBuilder withSubcode(final Long subcode) {
			if(subcode!=null) {
				setSubcode(subcode);
			}
			return this;
		}

		public FailureMessageBuilder withSubcode(final String subcode) {
			setSubcode(ParsingUtil.toUnsignedLong(subcode));
			return this;
		}


		public FailureMessageBuilder withReason(final String reason) {
			this.reason = reason;
			return this;
		}

		public FailureMessageBuilder withDetail(final String detail) {
			this.detail = detail;
			return this;
		}

		@Override
		public FailureMessage build() {
			return
				new ImmutableFailureMessage(
					id(),
					submissionDate(),
					agent(),
					responseTo(),
					responseNumber(),
					ValidationUtil.validateUnsignedLong(this.code,"Failure code"),
					this.subcode==null?null:ValidationUtil.validateUnsignedLong(this.subcode,"Failure subcode"),
					ValidationUtil.checkNotNull(this.reason,XSD.STRING_TYPE,"Failure reason cannot be null"),
					this.detail);
		}

	}

	public static final class EnrichmentResponseMessageBuilder extends ResponseMessageBuilder<EnrichmentResponseMessage,EnrichmentResponseMessageBuilder> {

		private URI targetResource;
		private final List<Binding> additions;
		private final List<Binding> removals;

		private EnrichmentResponseMessageBuilder() {
			super(EnrichmentResponseMessageBuilder.class);
			this.additions=Lists.newArrayList();
			this.removals=Lists.newArrayList();
		}

		public EnrichmentResponseMessageBuilder withTargetResource(final URI targetResource) {
			this.targetResource = targetResource;
			return this;
		}

		public EnrichmentResponseMessageBuilder withTargetResource(final String targetResource) {
			return withTargetResource(ParsingUtil.toURI(targetResource));
		}

		public EnrichmentResponseMessageBuilder withAddition(final Binding binding) {
			if(binding!=null) {
				this.additions.add(binding);
			}
			return this;
		}

		public EnrichmentResponseMessageBuilder withAddition(final Builder<Binding> builder) {
			if(builder==null) {
				return this;
			}
			return withAddition(builder.build());
		}

		public EnrichmentResponseMessageBuilder withRemoval(final Binding binding) {
			if(binding!=null) {
				this.removals.add(binding);
			}
			return this;
		}

		public EnrichmentResponseMessageBuilder withRemoval(final Builder<Binding> builder) {
			if(builder==null) {
				return this;
			}
			return withRemoval(builder.build());
		}

		@Override
		public EnrichmentResponseMessage build() {
			return
				new ImmutableEnrichmentResponseMessage(
					id(),
					submissionDate(),
					agent(),
					responseTo(),
					responseNumber(),
					ValidationUtil.checkNotNull(this.targetResource,RDFS.RESOURCE_TYPE,"Target resource cannot be null"),
					this.additions,
					this.removals);
		}

	}

	public static final class LiteralBuilder implements Builder<Literal> {

		private String lexicalForm;
		private URI datatype;
		private String language;

		private LiteralBuilder() {
		}

		public LiteralBuilder withLexicalForm(final Object object) {
			if(object!=null) {
				this.lexicalForm=object.toString();
			}
			return this;
		}

		public LiteralBuilder withDatatype(final String datatype) {
			return withDatatype(ParsingUtil.toURI(datatype));
		}

		public LiteralBuilder withDatatype(final URI datatype) {
			this.datatype = datatype;
			return this;
		}

		public LiteralBuilder withLanguage(final String language) {
			this.language = language;
			return this;
		}

		@Override
		public Literal build() {
			return
				new ImmutableLiteral(
					ValidationUtil.checkNotNull(this.lexicalForm,XSD.STRING_TYPE,"Lexical form cannot be null"),
					this.datatype,
					this.language
				);
		}

	}

	public static final class BindingBuilder implements Builder<Binding> {

		private URI property;
		private Value value;

		public BindingBuilder withProperty(final String property) {
			return withProperty(ParsingUtil.toURI(property));
		}

		public BindingBuilder withProperty(final URI property) {
			this.property = property;
			return this;
		}

		public BindingBuilder withValue(final Value value) {
			this.value=value;
			return this;
		}

		public BindingBuilder withValue(final Builder<? extends Value> builder) {
			if(builder==null) {
				return this;
			}
			return withValue(builder.build());
		}

		@Override
		public Binding build() {
			return new ImmutableBinding(
				ValidationUtil.checkNotNull(this.property,RDFS.RESOURCE_TYPE,"Binding property cannot be null"),
				ValidationUtil.checkNotNull(this.value,RDFS.RESOURCE_TYPE,"Binding value cannot be null")
			);
		}

	}

	public static final class ConstraintBuilder implements Builder<Constraint> {

		private NamedValue target;
		private final List<Binding> bindings;

		private ConstraintBuilder() {
			this.bindings=Lists.newArrayList();
		}

		public ConstraintBuilder withTarget(final NamedValue target) {
			this.target = target;
			return this;
		}

		public ConstraintBuilder withBinding(final Binding binding) {
			if(binding!=null) {
				this.bindings.add(binding);
			}
			return this;
		}

		public ConstraintBuilder withBinding(final Builder<Binding> builder) {
			if(builder==null) {
				return this;
			}
			return withBinding(builder.build());
		}

		@Override
		public Constraint build() {
			if(this.bindings.isEmpty()) {
				throw new ValidationException(null,RDFS.RESOURCE_TYPE,"No constraint bindings specified");
			}
			return new ImmutableConstraint(
				ValidationUtil.checkNotNull(this.target,RDFS.RESOURCE_TYPE,"Constraint target cannot be null"),
				this.bindings
			);
		}

	}

	public static final class FilterBuilder implements Builder<Filter> {

		private URI property;
		private Variable variable;

		private FilterBuilder() {
		}

		public FilterBuilder withVariable(final Variable variable) {
			this.variable = variable;
			return this;
		}

		public FilterBuilder withProperty(final String property) {
			return withProperty(ParsingUtil.toURI(property));
		}

		public FilterBuilder withProperty(final URI property) {
			this.property = property;
			return this;
		}

		@Override
		public Filter build() {
			return new ImmutableFilter(
				ValidationUtil.checkNotNull(this.property,RDFS.RESOURCE_TYPE,"Filter property cannot be null"),
				ValidationUtil.checkNotNull(this.variable,RDFS.RESOURCE_TYPE,"Filter variable cannot be null")
			);
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

	public static EnrichmentRequestMessageBuilder newEnrichmentRequestMessage() {
		return new EnrichmentRequestMessageBuilder();
	}

	public static DisconnectMessageBuilder newDisconnectMessage() {
		return new DisconnectMessageBuilder();
	}

	public static AcceptedMessageBuilder newAcceptedMessage() {
		return new AcceptedMessageBuilder();
	}

	public static FailureMessageBuilder newFailureMessage() {
		return new FailureMessageBuilder();
	}

	public static EnrichmentResponseMessageBuilder newEnrichmentResponseMessage() {
		return new EnrichmentResponseMessageBuilder();
	}

	public static Variable newVariable(final String name) {
		ValidationUtil.checkNotNull(name, CURATOR.VARIABLE_TYPE,"Variable name cannot be null");
		return new ImmutableVariable(name);
	}

	public static Resource newResource(final String name) {
		return newResource(ParsingUtil.toURI(name));
	}

	public static Resource newResource(final URI name) {
		ValidationUtil.checkNotNull(name, RDFS.RESOURCE_TYPE,"Resource name cannot be null");
		return new ImmutableResource(name);
	}

	public static LiteralBuilder newLiteral() {
		return new LiteralBuilder();
	}

	public static BindingBuilder newBinding() {
		return new BindingBuilder();
	}

	public static ConstraintBuilder newConstraint() {
		return new ConstraintBuilder();
	}

	public static FilterBuilder newFilter() {
		return new FilterBuilder();
	}

}
