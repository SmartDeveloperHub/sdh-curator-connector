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

import org.smartdeveloperhub.curator.connector.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.DeliveryChannelBuilder;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class DeliveryChannelParser extends Parser<DeliveryChannel,DeliveryChannelBuilder> {

	private final class DeliveryChannelWorker extends Worker {
		@Override
		protected void parse() {
			updateBroker();
			updateExchangeName();
			updateQueueName();
			updateRoutingKey();
		}

		private void updateBroker() {
			Resource broker=resource("broker","amqp:broker",true);
			if(broker!=null) {
				try {
					this.builder.withBroker(BrokerParser.fromModel(model(), broker));
				} catch (ValidationException e) {
					failConversion("amqp:broker", e);
				}
			}
		}

		private void updateRoutingKey() {
			Literal routingKey=literal("routingKey","amqp:routingKey",true);
			if(routingKey!=null) {
				try {
					this.builder.withRoutingKey(routingKey.getLexicalForm());
				} catch (ValidationException e) {
					failConversion("amqp:routingKey",e);
				}
			}
		}

		private void updateQueueName() {
			Literal queueName=literal("queueName","amqp:queueName",true);
			if(queueName!=null) {
				try {
					builder.withQueueName(queueName.getLexicalForm());
				} catch (ValidationException e) {
					failConversion("amqp:routingKey",e);
				}
			}
		}

		private void updateExchangeName() {
			Literal exchangeName=literal("exchangeName","amqp:exchangeName",true);
			if(exchangeName!=null) {
				try {
					this.builder.withExchangeName(exchangeName.getLexicalForm());
				} catch (ValidationException e) {
					failConversion("amqp:routingKey",e);
				}
			}
		}
	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.loadResource(DeliveryChannelParser.class,"deliveryChannel.sparql"));

	private DeliveryChannelParser(Model model, Resource resource) {
		super(model, resource);
	}

	@Override
	protected Worker solutionParser() {
		return new DeliveryChannelWorker();
	}

	@Override
	protected String parsedType() {
		return "curator:DeliveryChannel";
	}

	@Override
	protected Query parserQuery() {
		return QUERY;
	}

	@Override
	protected String targetVariable() {
		return "deliveryChannel";
	}

	@Override
	protected DeliveryChannelBuilder newBuilder() {
		return ProtocolFactory.newDeliveryChannel();
	}

	static DeliveryChannel fromModel(Model model, Resource resource) {
		return new DeliveryChannelParser(model, resource).parse();
	}

}