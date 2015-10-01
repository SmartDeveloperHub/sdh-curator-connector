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
package org.smartdeveloperhub.curator.connector;

import java.util.Objects;

import org.smartdeveloperhub.curator.protocol.Broker;

public final class CuratorConfiguration {

	public static final String DEFAULT_EXCHANGE_NAME       = "sdh";

	public static final String DEFAULT_REQUEST_QUEUE_NAME  = "curator.requests";

	public static final String DEFAULT_RESPONSE_QUEUE_NAME = "curator.responses";

	public static final Broker DEFAULT_BROKER =
		ProtocolFactory.
			newBroker().
				build();

	private final Broker broker;
	private final String exchangeName;
	private final String requestQueueName;
	private final String responseQueueName;

	private CuratorConfiguration(
			Broker broker,
			String exchangeName,
			String requestQueueName,
			String responseQueueName) {
		this.broker = Objects.requireNonNull(broker,"Broker cannot be null");
		this.exchangeName = Objects.requireNonNull(exchangeName,"Exchange name cannot be null");
		this.requestQueueName = Objects.requireNonNull(requestQueueName,"Request queue name cannot be null");
		this.responseQueueName = Objects.requireNonNull(responseQueueName,"Response queue name cannot be null");
	}

	public Broker broker() {
		return this.broker;
	}

	public String exchangeName() {
		return this.exchangeName;
	}

	public String requestQueueName() {
		return this.requestQueueName;
	}

	public String responseQueueName() {
		return this.responseQueueName;
	}

	public CuratorConfiguration withBroker(Broker broker) {
		return new CuratorConfiguration(broker,this.exchangeName,this.requestQueueName,this.responseQueueName);
	}

	public CuratorConfiguration withExchangeName(String exchangeName) {
		return new CuratorConfiguration(this.broker,exchangeName,this.requestQueueName,this.responseQueueName);
	}

	public CuratorConfiguration withRequestQueueName(String requestQueueName) {
		return new CuratorConfiguration(this.broker,this.exchangeName,requestQueueName,this.responseQueueName);
	}

	public CuratorConfiguration withResponseQueueName(String responseQueueName) {
		return new CuratorConfiguration(this.broker,this.exchangeName,this.requestQueueName,responseQueueName);
	}

	public static CuratorConfiguration newInstance() {
		return
			new CuratorConfiguration(
				DEFAULT_BROKER,
				DEFAULT_EXCHANGE_NAME,
				DEFAULT_REQUEST_QUEUE_NAME,
				DEFAULT_RESPONSE_QUEUE_NAME);
	}

}