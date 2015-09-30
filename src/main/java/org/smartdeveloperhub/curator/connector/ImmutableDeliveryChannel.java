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

import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.google.common.base.MoreObjects;

final class ImmutableDeliveryChannel implements DeliveryChannel {

	private final String exchangeName;
	private final String routingKey;
	private final Broker broker;
	private final String virtualHost;
	private final String queueName;

	ImmutableDeliveryChannel(
			Broker broker,
			String virtualHost,
			String exchangeName,
			String queueName,
			String routingKey) {
		this.exchangeName = exchangeName;
		this.routingKey = routingKey;
		this.broker = broker;
		this.virtualHost = virtualHost;
		this.queueName = queueName;
	}

	@Override
	public Broker broker() {
		return this.broker;
	}

	@Override
	public String virtualHost() {
		return this.virtualHost;
	}

	@Override
	public String exchangeName() {
		return this.exchangeName;
	}

	@Override
	public String queueName() {
		return this.queueName;
	}

	@Override
	public String routingKey() {
		return this.routingKey;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("broker",this.broker).
					add("virtualHost",this.virtualHost).
					add("exchangeName",this.exchangeName).
					add("queueName",this.queueName).
					add("routingKey",this.routingKey).
					toString();
	}

}