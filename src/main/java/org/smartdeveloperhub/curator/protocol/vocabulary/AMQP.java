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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.protocol.vocabulary;

public final class AMQP {

	public static final String NAMESPACE = "http://www.smartdeveloperhub.org/vocabulary/amqp#";
	public static final String PREFIX    = "amqp";

	public static final String PATH_TYPE = term("Path");
	public static final String NAME_TYPE = term("Name");
	public static final String BROKER_TYPE = term("Broker");
	public static final String ROUTING_KEY = term("routingKey");
	public static final String QUEUE_NAME = term("queueName");
	public static final String EXCHANGE_NAME = term("exchangeName");
	public static final String VIRTUAL_HOST = term("virtualHost");
	public static final String PORT = term("port");
	public static final String HOST = term("host");
	public static final String BROKER = term("broker");
	public static final String ROUTING_KEY_TYPE = term("RoutingKey");

	private AMQP() {
	}

	private static String term(String localName) {
		return NAMESPACE+localName;
	}

}
