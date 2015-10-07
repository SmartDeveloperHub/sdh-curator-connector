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

import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

final class ConnectorConfiguration {

	private static final String NL = System.lineSeparator();

	private CuratorConfiguration curatorConfiguration;
	private DeliveryChannel connectorChannel;
	private Agent agent;

	ConnectorConfiguration() {
	}

	Agent agent() {
		return this.agent;
	}

	DeliveryChannel connectorChannel() {
		return this.connectorChannel;
	}

	ConnectorConfiguration withCuratorConfiguration(CuratorConfiguration configuration) {
		this.curatorConfiguration=configuration;
		return this;
	}

	ConnectorConfiguration withAgent(Agent agent) {
		this.agent = agent;
		return this;
	}

	ConnectorConfiguration withConnectorChannel(DeliveryChannel connectorConfiguration) {
		this.connectorChannel = connectorConfiguration;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		builder.append("-- Connector details:").append(ConnectorConfiguration.NL);
		builder.append("   + Agent: ").append(this.agent.agentId()).append(ConnectorConfiguration.NL);
		builder.append("   + Curator configuration:").append(ConnectorConfiguration.NL);
		appendBrokerDetails(builder, this.curatorConfiguration.broker());
		appendExchangeName(builder, this.curatorConfiguration.exchangeName());
		builder.append("     - Request queue name.: ").append(this.curatorConfiguration.requestQueueName()).append(ConnectorConfiguration.NL);
		appendResponseQueueDetails(builder, this.curatorConfiguration.responseQueueName());
		builder.append("   + Connector configuration:").append(ConnectorConfiguration.NL);
		appendBrokerDetails(builder, this.connectorChannel.broker());
		appendExchangeName(builder, this.connectorChannel.exchangeName());
		appendResponseQueueDetails(builder, this.connectorChannel.queueName());
		builder.append("     - Routing key........: ").append(this.connectorChannel.routingKey());
		return builder.toString();
	}

	private void appendResponseQueueDetails(StringBuilder builder, String responseQueueName) {
		builder.append("     - Response queue name: ").append(responseQueueName).append(ConnectorConfiguration.NL);
	}

	private void appendExchangeName(StringBuilder builder, String exchangeName) {
		builder.append("     - Exchange name......: ").append(exchangeName).append(ConnectorConfiguration.NL);
	}

	private void appendBrokerDetails(StringBuilder builder, Broker broker) {
		builder.append("     - Broker").append(ConnectorConfiguration.NL);
		builder.append("       + Host.............: ").append(broker.host()).append(ConnectorConfiguration.NL);
		builder.append("       + Port.............: ").append(broker.port()).append(ConnectorConfiguration.NL);
		builder.append("       + Virtual host.....: ").append(broker.virtualHost()).append(ConnectorConfiguration.NL);
	}

}