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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.io;

import java.net.URI;
import java.util.List;

import org.smartdeveloperhub.curator.connector.rdf.ModelHelper;
import org.smartdeveloperhub.curator.connector.rdf.ResourceHelper;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.CURATOR;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class EnrichmentRequestMessageConverter extends ModelMessageConverter<EnrichmentRequestMessage> {

	private static final String REQUEST_BNODE          = "request";
	private static final String AGENT_BNODE            = "agent";
	private static final String BROKER_BNODE           = "broker";
	private static final String DELIVERY_CHANNEL_BNODE = "deliveryChannel";

	@Override
	protected void toString(final EnrichmentRequestMessage message, final ModelHelper helper) {
		final EnrichmentUtil util=
			EnrichmentUtil.
				builder().
					withFilters(message.filters()).
					withConstraints(message.constraints()).
						build();
		helper.
			blankNode(util.blankNode(REQUEST_BNODE)).
				type(CURATOR.ENRICHMENT_REQUEST_TYPE).
				property(CURATOR.MESSAGE_ID).
					withTypedLiteral(message.messageId(), TYPES.UUID_TYPE).
				property(CURATOR.SUBMITTED_BY).
					withBlankNode(util.blankNode(AGENT_BNODE)).
				property(CURATOR.SUBMITTED_ON).
					withTypedLiteral(message.submittedOn(), XSD.DATE_TIME_TYPE).
				property(CURATOR.REPLY_TO).
					withBlankNode(util.blankNode(DELIVERY_CHANNEL_BNODE)).
				property(CURATOR.TARGET_RESOURCE).
					withResource(message.targetResource()).
			blankNode(util.blankNode(AGENT_BNODE)).
				type(FOAF.AGENT_TYPE).
				property(CURATOR.AGENT_ID).
					withTypedLiteral(message.submittedBy().agentId(), TYPES.UUID_TYPE).
			blankNode(util.blankNode(DELIVERY_CHANNEL_BNODE)).
				type(CURATOR.DELIVERY_CHANNEL_TYPE);

		serializeReplyTo(util,helper, message.replyTo());
		serializeFilters(helper,message.targetResource(),message.filters());
		serializeConstraints(helper,message.constraints());
	}

	@Override
	protected EnrichmentRequestMessage parse(final Model model, final Resource resource) {
		return EnrichmentRequestMessageParser.fromModel(model, resource);
	}

	@Override
	protected String messageType() {
		return CURATOR.ENRICHMENT_REQUEST_TYPE;
	}

	private void serializeConstraints(final ModelHelper helper, final List<Constraint> constraints) {
		final BindingSerializer serializer=BindingSerializer.newInstance(helper);
		for(final Constraint constraint:constraints) {
			final NamedValue target=constraint.target();
			for(final Binding binding:constraint.bindings()) {
				serializer.serialize(target,binding);
			}
		}
	}

	private void serializeFilters(final ModelHelper helper, final URI targetResource, final List<Filter> filters) {
		final ResourceHelper resource=helper.resource(targetResource);
		for(final Filter filter:filters) {
			resource.
				property(filter.property()).
					withBlankNode(filter.variable().name()).
				blankNode(filter.variable().name()).
					type(CURATOR.VARIABLE_TYPE);
		}
	}

	private void serializeReplyTo(final EnrichmentUtil util, final ModelHelper helper, final DeliveryChannel deliveryChannel) {
		serializeBroker(util, helper, deliveryChannel.broker());
		deliveryChannelProperty(util,helper,AMQP.EXCHANGE_NAME,deliveryChannel.exchangeName(),AMQP.NAME_TYPE);
		deliveryChannelProperty(util,helper,AMQP.ROUTING_KEY,deliveryChannel.routingKey(), AMQP.ROUTING_KEY_TYPE);
	}

	private void serializeBroker(final EnrichmentUtil util, final ModelHelper helper, final Broker broker) {
		if(broker==null) {
			return;
		}
		helper.
			blankNode(util.blankNode(DELIVERY_CHANNEL_BNODE)).
				property(AMQP.BROKER).
					withBlankNode(util.blankNode(BROKER_BNODE)).
			blankNode(util.blankNode(BROKER_BNODE)).
				type(AMQP.BROKER_TYPE).
				property(AMQP.HOST).
					withTypedLiteral(broker.host(),TYPES.HOSTNAME_TYPE).
				property(AMQP.PORT).
					withTypedLiteral(broker.port(),TYPES.PORT_TYPE).
				property(AMQP.VIRTUAL_HOST).
					withTypedLiteral(broker.virtualHost(),AMQP.PATH_TYPE);
	}

	private void deliveryChannelProperty(final EnrichmentUtil util, final ModelHelper helper, final String property, final String value, final String type) {
		if(value!=null) {
			helper.
				blankNode(util.blankNode(DELIVERY_CHANNEL_BNODE)).
					property(property).
						withTypedLiteral(value,type);
		}
	}

}
