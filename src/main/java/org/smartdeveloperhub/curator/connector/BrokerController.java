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
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.protocol.Broker;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ExceptionHandler;
import com.rabbitmq.client.TopologyRecoveryException;

final class BrokerController {

	private final class BrokerExceptionHandler implements ExceptionHandler {

		@Override
		public void handleUnexpectedConnectionDriverException(Connection connection, Throwable exception) {
			LOGGER.error("Unexpected driver failure for connection {}",connection,exception);
		}

		@Override
		public void handleReturnListenerException(Channel channel, Throwable exception) {
			LOGGER.error("Unexpected return listener failure for channel {}",channel,exception);
		}

		@Override
		public void handleFlowListenerException(Channel channel, Throwable exception) {
			LOGGER.error("Unexpected flow listener failure for channel {}",channel,exception);
		}

		@Override
		public void handleConfirmListenerException(Channel channel, Throwable exception) {
			LOGGER.error("Unexpected confirm listener failure for channel {}",channel,exception);
		}

		@Override
		public void handleBlockedListenerException(Connection connection, Throwable exception) {
			LOGGER.error("Unexpected blocked listener failure for connection {}",connection,exception);
		}

		@Override
		public void handleConsumerException(Channel channel, Throwable exception, Consumer consumer, String consumerTag, String methodName) {
			LOGGER.error("Unexpected consumer {} ({}) failure in method {} for channel {}",consumer,consumerTag,methodName,channel,exception);
		}

		@Override
		public void handleConnectionRecoveryException(Connection connection, Throwable exception) {
			LOGGER.error("Unexpected recovery failure for connection {}",connection,exception);
		}

		@Override
		public void handleChannelRecoveryException(Channel channel, Throwable exception) {
			LOGGER.error("Unexpected recovery failure for channel {}",channel,exception);
		}

		@Override
		public void handleTopologyRecoveryException(Connection connection, Channel channel, TopologyRecoveryException exception) {
			LOGGER.error("Unexpected topology recovery failure for connection {} and channel {}",connection,channel,exception);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BrokerController.class);

	private final Broker broker;
	private final Lock read;
	private final Lock write;

	private Connection connection;
	private Channel channel;
	private boolean connected;

	private String name;

	BrokerController(Broker broker, String name) {
		this.broker = broker;
		this.name = name;
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
	}

	Broker broker() {
		return this.broker;
	}

	void connect() throws ControllerException {
		this.write.lock();
		try {
			if(this.connected) {
				return;
			}
			ConnectionFactory factory=new ConnectionFactory();
			factory.setHost(this.broker.host());
			factory.setPort(this.broker.port());
			factory.setVirtualHost(this.broker.virtualHost());
			factory.setThreadFactory(brokerThreadFactory());
			factory.setExceptionHandler(new BrokerExceptionHandler());
			this.connection = factory.newConnection();
			createChannel();
		} catch(IOException | TimeoutException e) {
			this.connected=false;
			throw new ControllerException("Could not connect to broker",e);
		} finally {
			this.write.unlock();
		}
	}

	Channel channel() {
		this.read.lock();
		try {
			Preconditions.checkState(this.connected,"Not connected");
			return this.channel;
		} finally {
			this.read.unlock();
		}
	}

	void disconnect() {
		this.write.lock();
		try {
			if(!this.connected) {
				return;
			}
			closeChannelQuietly();
			closeConnectionQuietly();
			this.connected=false;
		} finally {
			this.write.unlock();
		}
	}

	private ThreadFactory brokerThreadFactory() {
		return
			new ThreadFactoryBuilder().
				setNameFormat(this.name+"-broker-%d").
				setUncaughtExceptionHandler(
					new UncaughtExceptionHandler(){
						@Override
						public void uncaughtException(Thread t, Throwable e) {
							LOGGER.error("Unexpected failure on thread {}",t.getName(),e);
						}
					}
				).
				build();
	}

	private void createChannel() throws ControllerException {
		try {
			this.channel = this.connection.createChannel();
			this.connected=true;
		} catch (Exception e) {
			this.connected=false;
			closeConnectionQuietly();
			throw new ControllerException("Could not create channel for broker connection",e);
		}
	}

	private void closeChannelQuietly() {
		if(this.channel!=null) {
			try {
				this.channel.close();
			} catch (Exception e) {
				LOGGER.trace("Could not close channel gracefully",e);
			}
		}
	}

	private void closeConnectionQuietly() {
		if(this.connection!=null) {
			try {
				this.connection.close();
			} catch (Exception e) {
				LOGGER.trace("Could not close connection gracefully",e);
			}
		}
	}

}