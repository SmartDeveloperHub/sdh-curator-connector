/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.io;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.AcceptedMessageBuilder;
import org.smartdeveloperhub.curator.connector.util.ResourceUtil;
import org.smartdeveloperhub.curator.protocol.AcceptedMessage;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class AcceptedMessageParser extends ResponseMessageParser<AcceptedMessage, AcceptedMessageBuilder> {

	private static final Query QUERY=
		QueryFactory.create(
			ResourceUtil.
				loadResource(
					AcceptedMessageParser.class,
					"accepted.sparql"));

	private AcceptedMessageParser(Model model, Resource resource) {
		super(model, resource,"curator:Accepted","accepted",QUERY);
	}

	@Override
	protected ResponseWorker solutionParser() {
		return new ResponseWorker();
	}

	@Override
	protected AcceptedMessageBuilder newBuilder() {
		return ProtocolFactory.newAcceptedMessage();
	}

	static AcceptedMessage fromModel(Model model, Resource resource) {
		return new AcceptedMessageParser(model, resource).parse();
	}

}
