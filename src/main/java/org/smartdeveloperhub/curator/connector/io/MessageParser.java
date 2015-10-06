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

import org.smartdeveloperhub.curator.connector.ProtocolFactory.MessageBuilder;
import org.smartdeveloperhub.curator.protocol.Message;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

abstract class MessageParser<T extends Message, B extends MessageBuilder<T,B>> extends Parser<T,B> {

	protected class MessageWorker extends Worker {

		@Override
		protected void parse() {
			updateMessageId();
			updateSubmittedOn();
			updateSubmittedBy();
		}

		private void updateMessageId() {
			mandatory(
				new LiteralConsumer("messageId", "curator:messageId") {
					@Override
					protected void consumeLiteral(B builder, Literal literal) {
						builder.withMessageId(literal.getLexicalForm());
					}
				}
			);
		}

		private void updateSubmittedOn() {
			mandatory(
				new LiteralConsumer("submittedOn","curator:submittedOn") {
					@Override
					protected void consumeLiteral(B builder, Literal literal) {
						builder.withSubmittedOn(literal.getLexicalForm());
					}
				}
			);
		}

		private void updateSubmittedBy() {
			mandatory(
				new ResourceConsumer("submittedBy","curator:submittedBy") {
					@Override
					protected void consumeResource(B builder, Resource resource) {
						builder.withSubmittedBy(AgentParser.fromModel(model(), resource));
					}
				}
			);
		}

	}

	MessageParser(Model model, Resource resource, String parsedType, String targetVariable, Query query) {
		super(model,resource,parsedType,targetVariable,query);
	}

	@Override
	protected abstract MessageWorker solutionParser();

}