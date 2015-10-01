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

import org.smartdeveloperhub.curator.connector.ProtocolFactory.Builder;
import org.smartdeveloperhub.curator.protocol.Broker;

import com.rabbitmq.client.ConnectionFactory;

public final class BrokerBuilder implements Builder<Broker> {

	private String host;
	private Integer port;
	private String virtualHost;

	BrokerBuilder() {
	}

	public BrokerBuilder withHost(String host) {
		ValidationUtils.validateHostname(host);
		this.host=host;
		return this;
	}

	public BrokerBuilder withPort(int port) {
		ValidationUtils.validatePort(port);
		this.port=port;
		return this;
	}

	public BrokerBuilder withVirtualHost(String virtualHost) {
		ValidationUtils.validatePath(virtualHost);
		this.virtualHost = virtualHost;
		return this;
	}

	public Broker build() {
		return
			new ImmutableBroker(
				this.host!=null?this.host:ConnectionFactory.DEFAULT_HOST,
				this.port!=null?this.port:ConnectionFactory.DEFAULT_AMQP_PORT,
				this.virtualHost!=null?this.virtualHost:ConnectionFactory.DEFAULT_VHOST);
	}

}