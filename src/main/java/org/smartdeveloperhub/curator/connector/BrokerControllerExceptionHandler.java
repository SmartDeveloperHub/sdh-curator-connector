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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.1.0
 *   Bundle      : sdh-curator-connector-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.TopologyRecoveryException;

final class BrokerControllerExceptionHandler implements ExceptionHandler {

	private static final Logger LOGGER=LoggerFactory.getLogger(BrokerController.class);

	private final BrokerController controller;

	BrokerControllerExceptionHandler(final BrokerController controller) {
		this.controller = controller;
	}

	@Override
	public void handleUnexpectedConnectionDriverException(final Connection connection, final Throwable exception) {
		LOGGER.error("[{}] Unexpected driver failure for connection {}",this.controller.broker(),connection,exception);
	}

	@Override
	public void handleReturnListenerException(final Channel channel, final Throwable exception) {
		LOGGER.error("[{}] Unexpected return listener failure for channel {}",this.controller.broker(),channel,exception);
	}

	@Override
	public void handleFlowListenerException(final Channel channel, final Throwable exception) {
		LOGGER.error("[{}] Unexpected flow listener failure for channel {}",this.controller.broker(),channel,exception);
	}

	@Override
	public void handleConfirmListenerException(final Channel channel, final Throwable exception) {
		LOGGER.error("[{}] Unexpected confirm listener failure for channel {}",this.controller.broker(),channel,exception);
	}

	@Override
	public void handleBlockedListenerException(final Connection connection, final Throwable exception) {
		LOGGER.error("[{}] Unexpected blocked listener failure for connection {}",this.controller.broker(),connection,exception);
	}

	@Override
	public void handleConsumerException(final Channel channel, final Throwable exception, final Consumer consumer, final String consumerTag, final String methodName) {
		LOGGER.error("[{}] Unexpected consumer {} ({}) failure in method {} for channel {}",this.controller.broker(),consumer,consumerTag,methodName,channel,exception);
	}

	@Override
	public void handleConnectionRecoveryException(final Connection connection, final Throwable exception) {
		LOGGER.error("[{}] Unexpected recovery failure for connection {}",this.controller.broker(),connection,exception);
	}

	@Override
	public void handleChannelRecoveryException(final Channel channel, final Throwable exception) {
		LOGGER.error("[{}] Unexpected recovery failure for channel {}",this.controller.broker(),channel,exception);
	}

	@Override
	public void handleTopologyRecoveryException(final Connection connection, final Channel channel, final TopologyRecoveryException exception) {
		LOGGER.error("[{}] Unexpected topology recovery failure for connection {} and channel {}",this.controller.broker(),connection,channel,exception);
	}

}