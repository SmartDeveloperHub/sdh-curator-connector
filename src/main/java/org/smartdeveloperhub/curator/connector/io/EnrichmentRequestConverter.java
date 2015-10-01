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

import static org.smartdeveloperhub.curator.connector.io.Namespaces.*;

import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;

final class EnrichmentRequestConverter extends ModelMessageConverter<EnrichmentRequest> {

	@Override
	protected void toString(EnrichmentRequest message, ModelHelper helper) {
		helper.
			blankNode("request").
				type(curator("EnrichmentRequest")).
				property(curator("messageId")).
					withTypedLiteral(message.messageId(), types("UUID")).
				property(curator("submittedBy")).
					withBlankNode("agent").
				property(curator("submittedOn")).
					withTypedLiteral(message.submittedOn(), xsd("dateTimeStamp")).
				property(curator("replyTo")).
					withBlankNode("deliveryChannel").
				property(curator("targetResource")).
					withResource(message.targetResource()).
			blankNode("agent").
				type(foaf("Agent")).
				property(curator("agentId")).
					withTypedLiteral(message.submittedBy().agentId(), types("UUID")).
			blankNode("deliveryChannel").
				type(curator("DeliveryChannel"));

		DeliveryChannel deliveryChannel = message.replyTo();
		Broker broker = deliveryChannel.broker();
		if(broker!=null) {
			helper.
				blankNode("deliveryChannel").
					property(amqp("broker")).
						withBlankNode("broker").
				blankNode("broker").
					type(amqp("Broker")).
					property(amqp("host")).
						withTypedLiteral(broker.host(),types("Hostname")).
					property(amqp("port")).
						withTypedLiteral(broker.port(),types("Port")).
					property(amqp("virtualHost")).
						withTypedLiteral(broker.virtualHost(),amqp("Path"));
		}
		deliveryChannelProperty(helper, "exchangeName", deliveryChannel.exchangeName(),"Name");
		deliveryChannelProperty(helper, "queueName", deliveryChannel.queueName(), "Name");
		deliveryChannelProperty(helper, "routingKey", deliveryChannel.routingKey(), "Path");
	}

	private void deliveryChannelProperty(ModelHelper helper, String property, String value, String type) {
		if(value!=null) {
			helper.
				blankNode("deliveryChannel").
					property(amqp(property)).
						withTypedLiteral(value,amqp(type));
		}
	}

}
