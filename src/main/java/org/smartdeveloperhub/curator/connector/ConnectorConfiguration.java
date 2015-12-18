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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
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

	private String queueName;

	ConnectorConfiguration() {
	}

	Agent agent() {
		return this.agent;
	}

	String queueName() {
		return this.queueName;
	}

	DeliveryChannel connectorChannel() {
		return this.connectorChannel;
	}

	CuratorConfiguration curatorConfiguration() {
		return this.curatorConfiguration;
	}

	ConnectorConfiguration withCuratorConfiguration(final CuratorConfiguration configuration) {
		this.curatorConfiguration=configuration;
		return this;
	}

	ConnectorConfiguration withQueueName(final String queueName) {
		this.queueName=queueName;
		return this;
	}

	ConnectorConfiguration withAgent(final Agent agent) {
		this.agent = agent;
		return this;
	}

	ConnectorConfiguration withConnectorChannel(final DeliveryChannel connectorConfiguration) {
		this.connectorChannel = connectorConfiguration;
		return this;
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		builder.append("-- Connector details:").append(ConnectorConfiguration.NL);
		builder.append("   + Agent: ").append(this.agent.agentId()).append(ConnectorConfiguration.NL);
		builder.append("   + Curator configuration:").append(ConnectorConfiguration.NL);
		appendBrokerDetails(builder, this.curatorConfiguration.broker());
		appendExchangeName(builder, this.curatorConfiguration.exchangeName());
		appendCuratorQueueDetails(builder, this.curatorConfiguration.queueName(), this.curatorConfiguration.requestRoutingKey(), this.curatorConfiguration.responseRoutingKey());
		builder.append("   + Connector configuration:").append(ConnectorConfiguration.NL);
		appendBrokerDetails(builder, this.connectorChannel.broker());
		appendExchangeName(builder, this.connectorChannel.exchangeName());
		appendConnectorQueueDetails(builder, this.queueName,this.connectorChannel().routingKey());
		return builder.toString();
	}

	private void appendCuratorQueueDetails(final StringBuilder builder, final String queueName, final String requestRoutingKey, final String responseRoutingKey) {
		builder.append("     - Queue name..........: ").append(queueName).append(ConnectorConfiguration.NL);
		builder.append("     - Request routing key.: ").append(requestRoutingKey).append(ConnectorConfiguration.NL);
		builder.append("     - Response routing key: ").append(responseRoutingKey).append(ConnectorConfiguration.NL);
	}

	private void appendConnectorQueueDetails(final StringBuilder builder, final String queueName, final String routingKey) {
		builder.append("     - Queue name..........: ").append(queueName).append(ConnectorConfiguration.NL);
		builder.append("     - Routing key.........: ").append(routingKey).append(ConnectorConfiguration.NL);
	}

	private void appendExchangeName(final StringBuilder builder, final String exchangeName) {
		builder.append("     - Exchange name.......: ").append(exchangeName).append(ConnectorConfiguration.NL);
	}

	private void appendBrokerDetails(final StringBuilder builder, final Broker broker) {
		builder.append("     - Broker:").append(ConnectorConfiguration.NL);
		builder.append("       + Host..............: ").append(broker.host()).append(ConnectorConfiguration.NL);
		builder.append("       + Port..............: ").append(broker.port()).append(ConnectorConfiguration.NL);
		builder.append("       + Virtual host......: ").append(broker.virtualHost()).append(ConnectorConfiguration.NL);
	}

}