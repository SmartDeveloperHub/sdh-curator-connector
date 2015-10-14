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
import org.smartdeveloperhub.curator.connector.ProtocolFactory.FailureMessageBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.FailureMessage;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class FailureMessageParser extends ResponseMessageParser<FailureMessage, FailureMessageBuilder> {

	private final class FailureWorker extends ResponseWorker {

		@Override
		public void parse() {
			super.parse();
			updateCode();
			updateSubcode();
			updateReason();
			updateDetail();
		}

		private void updateDetail() {
			optional(
				new LiteralConsumer("detail","curator:detail") {
					@Override
					protected void consumeLiteral(FailureMessageBuilder builder, Literal literal) {
						builder.withDetail(literal.getLexicalForm());
					}
				}
			);
		}

		private void updateReason() {
			mandatory(
				new LiteralConsumer("reason","curator:reason") {
					@Override
					protected void consumeLiteral(FailureMessageBuilder builder, Literal literal) {
						builder.withReason(literal.getLexicalForm());
					}
				}
			);
		}

		private void updateSubcode() {
			optional(
				new LiteralConsumer("subcode","curator:subcode") {
					@Override
					protected void consumeLiteral(FailureMessageBuilder builder, Literal literal) {
						builder.withSubcode(literal.getLexicalForm());
					}
				}
			);
		}

		private void updateCode() {
			mandatory(
				new LiteralConsumer("code","curator:code") {
					@Override
					protected void consumeLiteral(FailureMessageBuilder builder, Literal literal) {
						builder.withCode(literal.getLexicalForm());
					}
				}
			);
		}
	}

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.
				loadResource(
					FailureMessageParser.class,
					"failure.sparql"));

	private FailureMessageParser(Model model, Resource resource) {
		super(model, resource,"curator:Failure","failure",QUERY);
	}

	@Override
	protected ResponseWorker solutionParser() {
		return new FailureWorker();
	}

	@Override
	protected FailureMessageBuilder newBuilder() {
		return ProtocolFactory.newFailureMessage();
	}

	static FailureMessage fromModel(Model model, Resource resource) {
		return new FailureMessageParser(model, resource).parse();
	}

}
