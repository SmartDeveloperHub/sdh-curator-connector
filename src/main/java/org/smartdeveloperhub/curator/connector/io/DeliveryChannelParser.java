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

import java.util.List;

import org.smartdeveloperhub.curator.connector.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.ProtocolFactory.DeliveryChannelBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class DeliveryChannelParser {

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.loadResource(DeliveryChannelParser.class,"deliveryChannel.sparql"));

	private DeliveryChannelParser() {
	}

	static DeliveryChannel fromModel(Model model, Resource resource) {
		QuerySolutionMap parameters = new QuerySolutionMap();
		parameters.add("deliveryChannel", resource);
		QueryExecution queryExecution = null;
		try {
			queryExecution=QueryExecutionFactory.create(QUERY, model);
			queryExecution.setInitialBinding(parameters);
			ResultSet results = queryExecution.execSelect();
			List<DeliveryChannel> result=processResult(model,results);
			return selectResult(result,resource);
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	private static DeliveryChannel selectResult(List<DeliveryChannel> result, Resource resource) {
		if(result.isEmpty()) {
			return null;
		} else if(result.size()>1) {
			throw new IllegalArgumentException("Too many DeliveryChannel definitions for resource '"+resource+"'");
		} else {
			return result.get(0);
		}
	}

	private static List<DeliveryChannel> processResult(Model model,ResultSet results) {
		List<DeliveryChannel> result=Lists.newArrayList();
		for(; results.hasNext();) {
			QuerySolution solution = results.nextSolution();
			DeliveryChannelBuilder builder = ProtocolFactory.newDeliveryChannel();
			updateBroker(model,solution, builder, solution.getResource("deliveryChannel"));
			updateExchangeName(solution, builder, solution.getResource("deliveryChannel"));
			updateQueueName(solution, builder, solution.getResource("deliveryChannel"));
			updateRoutingKey(solution, builder, solution.getResource("deliveryChannel"));
			result.add(builder.build());
		}
		return result;
	}

	private static void updateBroker(Model model,QuerySolution solution, DeliveryChannelBuilder builder, Resource resource) {
		Resource broker=solution.getResource("broker");
		if(broker!=null) {
			try {
				builder.withBroker(BrokerParser.fromModel(model, broker));
			} catch (ValidationException e) {
				throw new ConversionException("Could not process curator:agentId property for resource '"+resource+"'",e);
			}
		}
	}

	private static void updateRoutingKey(QuerySolution solution, DeliveryChannelBuilder builder, Resource resource) {
		Literal routingKey=solution.getLiteral("routingKey");
		if(routingKey!=null) {
			try {
				builder.withRoutingKey(routingKey.getLexicalForm());
			} catch (ValidationException e) {
				throw new ConversionException("Could not process curator:agentId property for resource '"+resource+"'",e);
			}
		}
	}

	private static void updateQueueName(QuerySolution solution, DeliveryChannelBuilder builder, Resource resource) {
		Literal queueName=solution.getLiteral("queueName");
		if(queueName!=null) {
			try {
				builder.withQueueName(queueName.getLexicalForm());
			} catch (ValidationException e) {
				throw new ConversionException("Could not process curator:agentId property for resource '"+resource+"'",e);
			}
		}
	}

	private static void updateExchangeName(QuerySolution solution, DeliveryChannelBuilder builder, Resource resource) {
		Literal exchangeName=solution.getLiteral("exchangeName");
		if(exchangeName!=null) {
			try {
				builder.withExchangeName(exchangeName.getLexicalForm());
			} catch (ValidationException e) {
				throw new ConversionException("Could not process curator:agentId property for resource '"+resource+"'",e);
			}
		}
	}

}