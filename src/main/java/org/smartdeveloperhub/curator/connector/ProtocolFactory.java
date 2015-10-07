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
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Disconnect;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;
import org.smartdeveloperhub.curator.protocol.Failure;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.Request;
import org.smartdeveloperhub.curator.protocol.Resource;
import org.smartdeveloperhub.curator.protocol.Response;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDFS;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.google.common.collect.Lists;
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

		private void setPort(Integer port) {
			this.port=port;
		}

		public BrokerBuilder withHost(String host) {
			this.host=host;
			return this;
		}

		public BrokerBuilder withPort(String port) {
			setPort(ParsingUtil.toPort(port));
			return this;
		}

		public BrokerBuilder withPort(int port) {
			setPort(port);
			return this;
		}

		public BrokerBuilder withVirtualHost(String virtualHost) {
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

		public AgentBuilder withAgentId(UUID agentId) {
			this.agentId=agentId;
			return this;
		}

		public AgentBuilder withAgentId(String agentId) {
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
			this.exchangeName = exchangeName;
			return this;
		}

		public DeliveryChannelBuilder withQueueName(String queueName) {
			this.queueName = queueName;
			return this;
		}

		public DeliveryChannelBuilder withRoutingKey(String routingKey) {
			this.routingKey = routingKey;
			return this;
		}

		@Override
		public DeliveryChannel build() {
			return
				new ImmutableDeliveryChannel(
					this.broker,
					ValidationUtil.validateName(this.exchangeName),
					ValidationUtil.validateName(this.queueName),
					ValidationUtil.validateRoutingKey(this.routingKey));
		}

	}

	public abstract static class MessageBuilder<T extends Message, B extends MessageBuilder<T,B>> implements Builder<T> {

		private final B builder;

		private UUID messageId;
		private DateTime submittedOn;
		private Agent agent;

		private MessageBuilder(Class<? extends B> builderClass) {
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

		public B withMessageId(UUID messageId) {
			this.messageId=messageId;
			return this.builder;
		}

		public B withMessageId(String messageId) {
			return withMessageId(ParsingUtil.toUUID(messageId));
		}

		public B withSubmittedOn(DateTime submittedOn) {
			this.submittedOn = submittedOn;
			return this.builder;
		}

		public B withSubmittedOn(Date submittedOn) {
			return withSubmittedOn(ParsingUtil.toDateTime(submittedOn));
		}

		public B withSubmittedOn(String submittedOn) {
			return withSubmittedOn(ParsingUtil.toDateTime(submittedOn));
		}

		public B withSubmittedBy(Agent agent) {
			this.agent=agent;
			return this.builder;
		}

		public B withSubmittedBy(Builder<Agent> builder) {
			if(builder==null) {
				return this.builder;
			}
			return withSubmittedBy(builder.build());
		}

	}

	public abstract static class RequestBuilder<T extends Request, B extends RequestBuilder<T,B>> extends MessageBuilder<T,B> {

		private DeliveryChannel deliveryChannel;

		private RequestBuilder(Class<? extends B> builderClass) {
			super(builderClass);
		}

		protected DeliveryChannel deliveryChannel() {
			return this.deliveryChannel;
		}

		public B withReplyTo(DeliveryChannel deliveryChannel) {
			this.deliveryChannel=deliveryChannel;
			return builder();
		}

		public B withReplyTo(Builder<DeliveryChannel> builder) {
			if(builder==null) {
				return builder();
			}
			return withReplyTo(builder.build());
		}

	}

	public static final class EnrichmentRequestBuilder extends RequestBuilder<EnrichmentRequest,EnrichmentRequestBuilder> {

		private final List<Filter> filters;
		private final List<Constraint> constraints;

		private URI targetResource;

		private EnrichmentRequestBuilder() {
			super(EnrichmentRequestBuilder.class);
			this.filters=Lists.newArrayList();
			this.constraints=Lists.newArrayList();
		}

		public EnrichmentRequestBuilder withTargetResource(String targetResource) {
			return withTargetResource(ParsingUtil.toURI(targetResource));
		}

		public EnrichmentRequestBuilder withTargetResource(URI targetResource) {
			this.targetResource = targetResource;
			return this;
		}

		public EnrichmentRequestBuilder withFilter(Filter filter) {
			if(filter!=null) {
				this.filters.add(filter);
			}
			return this;
		}

		public EnrichmentRequestBuilder withFilter(FilterBuilder builder) {
			if(builder!=null) {
				return withFilter(builder.build());
			}
			return this;
		}

		public EnrichmentRequestBuilder withConstraint(Constraint constraint) {
			if(constraint!=null) {
				this.constraints.add(constraint);
			}
			return this;
		}

		public EnrichmentRequestBuilder withConstraint(Builder<Constraint> builder) {
			if(builder==null) {
				return this;
			}
			return withConstraint(builder.build());
		}


		@Override
		public EnrichmentRequest build() {
/** UNCOMMENT WHEN PARSER IS READY
			if(this.filters.isEmpty()) {
				throw new ValidationException(null,RDFS.RESOURCE_TYPE,"No filters specified");
			}
			if(this.constraints.isEmpty()) {
				throw new ValidationException(null,RDFS.RESOURCE_TYPE,"No constraints specified");
			}
**/
			return
				new ImmutableEnrichmentRequest(
					id(),
					submissionDate(),
					agent(),
					ValidationUtil.checkNotNull(deliveryChannel(),CURATOR.DELIVERY_CHANNEL_TYPE,"Reply delivery channel cannot be null"),
					ValidationUtil.checkNotNull(this.targetResource,RDFS.RESOURCE_TYPE,"Target resource cannot be null"),
					this.filters,
					this.constraints);
		}

	}

	public static final class DisconnectBuilder extends RequestBuilder<Disconnect,DisconnectBuilder> {

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

		private void setResponseNumber(Long responseNumber) {
			this.responseNumber = responseNumber;
		}

		protected UUID responseTo() {
			return ValidationUtil.checkNotNull(this.responseTo,TYPES.UUID_TYPE,"ResponseTo message identifier cannot be null");
		}

		protected long responseNumber() {
			return ValidationUtil.validateUnsignedLong(this.responseNumber,"Response number");
		}

		public final B withResponseTo(String messageId) {
			return withResponseTo(ParsingUtil.toUUID(messageId));
		}

		public final B withResponseTo(UUID messageId) {
			this.responseTo = messageId;
			return builder();
		}

		public final B withResponseNumber(long responseNumber) {
			setResponseNumber(responseNumber);
			return builder();
		}

		public final B withResponseNumber(String responseNumber) {
			setResponseNumber(ParsingUtil.toUnsignedLong(responseNumber));
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

		private void setCode(long code) {
			this.code=code;
		}

		private void setSubcode(long subcode) {
			this.subcode=subcode;
		}

		public FailureBuilder withCode(long code) {
			setCode(code);
			return this;
		}

		public FailureBuilder withCode(String code) {
			setCode(ParsingUtil.toUnsignedLong(code));
			return this;
		}

		public FailureBuilder withSubcode(long subcode) {
			setSubcode(subcode);
			return this;
		}

		public FailureBuilder withSubcode(String subcode) {
			setSubcode(ParsingUtil.toUnsignedLong(subcode));
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
			return
				new ImmutableFailure(
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

		public EnrichmentResponseBuilder withTargetResource(String targetResource) {
			return withTargetResource(ParsingUtil.toURI(targetResource));
		}

		public EnrichmentResponseBuilder withAdditionTarget(URI additionTarget) {
			this.additionTarget = additionTarget;
			return this;
		}

		public EnrichmentResponseBuilder withAdditionTarget(String additionTarget) {
			return withAdditionTarget(ParsingUtil.toURI(additionTarget));
		}

		public EnrichmentResponseBuilder withRemovalTarget(URI removalTarget) {
			this.removalTarget = removalTarget;
			return this;
		}

		public EnrichmentResponseBuilder withRemovalTarget(String removalTarget) {
			return withRemovalTarget(ParsingUtil.toURI(removalTarget));
		}

		@Override
		public EnrichmentResponse build() {
			if(this.additionTarget!=null && this.additionTarget.equals(this.removalTarget)) {
				throw new ValidationException(this.removalTarget,RDFS.RESOURCE_TYPE,"Addition target and removal target resources must be different");
			}
			return
				new ImmutableEnrichmentResponse(
					id(),
					submissionDate(),
					agent(),
					responseTo(),
					responseNumber(),
					ValidationUtil.checkNotNull(this.targetResource,RDFS.RESOURCE_TYPE,"Target resource cannot be null"),
					this.additionTarget,
					this.removalTarget);
		}

	}

	public static final class LiteralBuilder implements Builder<Literal> {

		private String lexicalForm;
		private URI datatype;
		private String language;

		private LiteralBuilder() {
		}

		public LiteralBuilder withLexicalForm(Object object) {
			if(object!=null) {
				this.lexicalForm=object.toString();
			}
			return this;
		}

		public LiteralBuilder withDatatype(String datatype) {
			return withDatatype(ParsingUtil.toURI(datatype));
		}

		public LiteralBuilder withDatatype(URI datatype) {
			this.datatype = datatype;
			return this;
		}

		public LiteralBuilder withLanguage(String language) {
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

		public BindingBuilder withProperty(String property) {
			return withProperty(ParsingUtil.toURI(property));
		}

		public BindingBuilder withProperty(URI property) {
			this.property = property;
			return this;
		}

		public BindingBuilder withValue(Value value) {
			this.value=value;
			return this;
		}

		public BindingBuilder withValue(Builder<? extends Value> builder) {
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
		private List<Binding> bindings;

		private ConstraintBuilder() {
			this.bindings=Lists.newArrayList();
		}

		public ConstraintBuilder withTarget(NamedValue target) {
			this.target = target;
			return this;
		}

		public ConstraintBuilder withBinding(Binding binding) {
			if(binding!=null) {
				this.bindings.add(binding);
			}
			return this;
		}

		public ConstraintBuilder withBinding(Builder<Binding> builder) {
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

		public FilterBuilder withVariable(Variable variable) {
			this.variable = variable;
			return this;
		}

		public FilterBuilder withProperty(String property) {
			return withProperty(ParsingUtil.toURI(property));
		}

		public FilterBuilder withProperty(URI property) {
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

	public static Variable newVariable(final String name) {
		ValidationUtil.checkNotNull(name, XSD.STRING_TYPE,"Variable name cannot be null");
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
