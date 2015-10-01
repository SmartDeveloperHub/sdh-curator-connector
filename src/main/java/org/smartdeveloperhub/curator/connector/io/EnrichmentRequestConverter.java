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

import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;

final class EnrichmentRequestConverter extends ModelMessageConverter<EnrichmentRequest> {

	@Override
	protected void toString(EnrichmentRequest message, ModelHelper helper) {
		helper.
			blankNode("request").
				type(CURATOR.ENRICHMENT_REQUEST_TYPE).
				property(CURATOR.MESSAGE_ID).
					withTypedLiteral(message.messageId(), TYPES.UUID_TYPE).
				property(CURATOR.SUBMITTED_BY).
					withBlankNode("agent").
				property(CURATOR.SUBMITTED_ON).
					withTypedLiteral(message.submittedOn(), XSD.DATE_TIME_STAMP).
				property(CURATOR.REPLY_TO).
					withBlankNode("deliveryChannel").
				property(CURATOR.TARGET_RESOURCE).
					withResource(message.targetResource()).
			blankNode("agent").
				type(FOAF.AGENT_TYPE).
				property(CURATOR.AGENT_ID).
					withTypedLiteral(message.submittedBy().agentId(), TYPES.UUID_TYPE).
			blankNode("deliveryChannel").
				type(CURATOR.DELIVERY_CHANNEL_TYPE);

		DeliveryChannel deliveryChannel = message.replyTo();
		Broker broker = deliveryChannel.broker();
		if(broker!=null) {
			helper.
				blankNode("deliveryChannel").
					property(AMQP.BROKER).
						withBlankNode("broker").
				blankNode("broker").
					type(AMQP.BROKER_TYPE).
					property(AMQP.HOST).
						withTypedLiteral(broker.host(),TYPES.HOSTNAME_TYPE).
					property(AMQP.PORT).
						withTypedLiteral(broker.port(),TYPES.PORT_TYPE).
					property(AMQP.VIRTUAL_HOST).
						withTypedLiteral(broker.virtualHost(),AMQP.PATH_TYPE);
		}
		deliveryChannelProperty(helper, AMQP.EXCHANGE_NAME, deliveryChannel.exchangeName(),AMQP.NAME_TYPE);
		deliveryChannelProperty(helper, AMQP.QUEUE_NAME, deliveryChannel.queueName(), AMQP.NAME_TYPE);
		deliveryChannelProperty(helper, AMQP.ROUTING_KEY, deliveryChannel.routingKey(), AMQP.PATH_TYPE);
	}

	private void deliveryChannelProperty(ModelHelper helper, String property, String value, String type) {
		if(value!=null) {
			helper.
				blankNode("deliveryChannel").
					property(property).
						withTypedLiteral(value,type);
		}
	}

}
