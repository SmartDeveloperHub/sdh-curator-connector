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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Message;

final class ServerConnectorController extends ConnectorController {

	private static final Logger LOGGER=LoggerFactory.getLogger(ServerConnectorController.class);

	private final ConversionContext context;

	ServerConnectorController(final DeliveryChannel connectorConfiguration, final CuratorController curatorController, final ConversionContext context) {
		super(connectorConfiguration,curatorController);
		this.context = context;
	}

	void publishMessage(final Message message) throws IOException {
		try {
			publishMessage(
				MessageUtil.
					newInstance().
						withConversionContext(this.context).
						toString(message));
		} catch (final MessageConversionException e) {
			LOGGER.warn("Could not publish message {}: {}",message,e.getMessage());
			throw new IOException("Could not serialize message",e);
		}
	}

	void publishMessage(final String message) throws IOException {
		final String routingKey = effectiveConfiguration().routingKey();
		LOGGER.debug("Publishing message {} to routing key {}...",message,routingKey);
		try {
			brokerController().
				channel().
					basicPublish(
						effectiveConfiguration().exchangeName(),
						routingKey,
						null,
						message.getBytes());
		} catch (final IOException e) {
			LOGGER.warn("Could not publish message {} to routing key {}: {}",message,routingKey,e.getMessage());
			throw e;
		}
	}


}