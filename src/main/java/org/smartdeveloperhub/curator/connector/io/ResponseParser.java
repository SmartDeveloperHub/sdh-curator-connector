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

import org.smartdeveloperhub.curator.connector.ProtocolFactory.ResponseBuilder;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.protocol.Response;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

abstract class ResponseParser<T extends Response, B extends ResponseBuilder<T,B>> extends MessageParser<T,B> {

	protected class ResponseWorker extends MessageWorker {

		@Override
		public void parse() {
			super.parse();
			updateResponseTo();
			updateResponseNumber();
		}

		private void updateResponseTo() {
			Literal responseTo = literal("responseTo", "curator:responseTo",false);
			try {
				this.builder.withResponseTo(responseTo.getLexicalForm());
			} catch (ValidationException e) {
				failConversion("curator:responseTo",e);
			}
		}

		private void updateResponseNumber() {
			Literal responseTo = literal("responseNumber", "curator:responseNumber",false);
			try {
				this.builder.withResponseNumber(responseTo.getLong());
			} catch (NumberFormatException e) {
				failConversion("curator:responseTo",e);
			} catch (ValidationException e) {
				failConversion("curator:responseTo",e);
			}
		}
	}

	ResponseParser(Model model, Resource resource) {
		super(model,resource);
	}

	@Override
	protected abstract ResponseWorker createWorker();

}
