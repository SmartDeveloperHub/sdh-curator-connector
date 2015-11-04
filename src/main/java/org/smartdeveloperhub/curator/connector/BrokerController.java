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
import java.util.Deque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

final class BrokerController {

	interface Cleaner {

		void clean(Channel channel) throws IOException;

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BrokerController.class);

	private final Broker broker;
	private final ConversionContext context;
	private final String name;

	private final Lock read;
	private final Lock write;

	private final Deque<Cleaner> cleaners;

	private Connection connection;
	private Channel channel;
	private boolean connected;

	BrokerController(final Broker broker, final String name, final ConversionContext context) {
		this.broker=broker;
		this.name=name;
		this.context = context;
		final ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.cleaners=Lists.newLinkedList();
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
			final ConnectionFactory factory=new ConnectionFactory();
			factory.setHost(this.broker.host());
			factory.setPort(this.broker.port());
			factory.setVirtualHost(this.broker.virtualHost());
			factory.setThreadFactory(brokerThreadFactory());
			factory.setExceptionHandler(new BrokerControllerExceptionHandler(this));
			this.connection = factory.newConnection();
			createChannel();
		} catch(IOException | TimeoutException e) {
			this.connected=false;
			throw new ControllerException("Could not connect to broker",e);
		} finally {
			this.write.unlock();
		}
	}

	void register(final Cleaner cleaner) {
		this.read.lock();
		try {
			Preconditions.checkState(this.connected,"Not connected");
			this.cleaners.push(cleaner);
		} finally {
			this.read.unlock();
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
			cleanUp();
			closeChannelQuietly();
			closeConnectionQuietly();
			this.connected=false;
		} finally {
			this.write.unlock();
		}
	}

	void publishMessage(final DeliveryChannel replyTo, final Message message) throws IOException {
		try {
			publishMessage(
				replyTo,
				MessageUtil.
					newInstance().
						withConversionContext(this.context).
						toString(message));
		} catch (final MessageConversionException e) {
			LOGGER.warn("Could not publish message {}: {}",message,e.getMessage());
			throw new IOException("Could not serialize message",e);
		}
	}

	void publishMessage(final DeliveryChannel replyTo, final String message) throws IOException {
		final String exchangeName = replyTo.exchangeName();
		final String routingKey = replyTo.routingKey();
		LOGGER.debug("Publishing message to exchange '{}' and routing key '{}'. Payload: \n{}",exchangeName,routingKey,message);
		try {
			channel().
				basicPublish(
					exchangeName,
					routingKey,
					null,
					message.getBytes());
		} catch (final IOException e) {
			LOGGER.warn("Could not publish message {} to exchange '{}' and routing key '{}': {}",message,exchangeName,routingKey,e.getMessage());
			throw e;
		}
	}

	private void cleanUp() {
		LOGGER.debug("Cleaning up broker ({})...",this.cleaners.size());
		while(!this.cleaners.isEmpty()) {
			final Cleaner cleaner=this.cleaners.pop();
			try {
				cleaner.clean(this.channel);
				LOGGER.trace("{} completed",cleaner);
			} catch (final IOException e) {
				LOGGER.warn("{} failed. Full stacktrace follows",cleaner,e);
			}
		}
		LOGGER.debug("Broker clean-up completed.",this.cleaners.size());
	}

	private ThreadFactory brokerThreadFactory() {
		return
			new ThreadFactoryBuilder().
				setNameFormat(this.name+"-broker-%d").
				setUncaughtExceptionHandler(new BrokerControllerUncaughtExceptionHandler(this)).
				build();
	}

	private void createChannel() throws ControllerException {
		try {
			this.channel = this.connection.createChannel();
			if(this.channel==null) {
				throw new IllegalStateException("No channel available");
			}
			this.connected=true;
		} catch (final Exception e) {
			this.connected=false;
			closeConnectionQuietly();
			throw new ControllerException("Could not create channel for broker connection",e);
		}
	}

	private void closeChannelQuietly() {
		try {
			this.channel.close();
		} catch (final Exception e) {
			LOGGER.trace("Could not close channel gracefully",e);
		}
	}

	private void closeConnectionQuietly() {
		if(this.connection!=null) {
			try {
				this.connection.close();
			} catch (final Exception e) {
				LOGGER.trace("Could not close connection gracefully",e);
			}
		}
	}

}