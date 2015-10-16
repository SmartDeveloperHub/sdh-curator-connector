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

final class BrokerController {

	private static final Logger LOGGER=LoggerFactory.getLogger(BrokerController.class);

	private final Broker broker;
	private final Lock read;
	private final Lock write;

	private Connection connection;
	private Channel channel;
	private boolean connected;

	private final String name;

	BrokerController(final Broker broker, final String name) {
		this.broker = broker;
		this.name = name;
		final ReadWriteLock lock=new ReentrantReadWriteLock();
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