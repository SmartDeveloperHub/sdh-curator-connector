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
import org.smartdeveloperhub.curator.connector.ProtocolFactory.BrokerBuilder;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.Broker;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class BrokerParser extends Parser<Broker,BrokerBuilder> {

	private final class BrokerWorker extends Worker {

		@Override
		protected void parse() {
			updateHost();
			updatePort();
			updateVirtualHost();
		}

		private void updateVirtualHost() {
			Literal virtualHost=literal("virtualHost","amqp:virtualHost",true);
			if(virtualHost!=null) {
				try {
					this.builder.withVirtualHost(virtualHost.getLexicalForm());
				} catch (ValidationException e) {
					failConversion("amqp:virtualHost",e);
				}
			}
		}

		private void updatePort() {
			Literal port=literal("port","amqp:port",true);
			if(port!=null) {
				try {
					this.builder.withPort(port.getLexicalForm());
				} catch (ValidationException e) {
					failConversion("amqp:port",e);
				}
			}
		}

		private void updateHost() {
			Literal host=literal("host","amqp:host",true);
			if(host!=null) {
				try {
					this.builder.withHost(host.getLexicalForm());
				} catch (ValidationException e) {
					failConversion("amqp:host",e);
				}
			}
		}

	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.loadResource(BrokerParser.class,"broker.sparql"));

	private BrokerParser(Model model, Resource resource) {
		super(model, resource);
	}

	@Override
	protected Worker solutionParser() {
		return new BrokerWorker();
	}

	@Override
	protected String parsedType() {
		return "amqp:Broker";
	}

	@Override
	protected Query parserQuery() {
		return QUERY;
	}

	@Override
	protected String targetVariable() {
		return "broker";
	}

	@Override
	protected BrokerBuilder newBuilder() {
		return ProtocolFactory.newBroker();
	}

	static Broker fromModel(Model model, Resource resource) {
		return new BrokerParser(model, resource).parse();
	}

}