/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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

import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ReturnListener;

final class BrokerController {

	interface Cleaner {

		void clean(Channel channel) throws IOException;

	}

	private final class LoggingReturnListener implements ReturnListener {

		@Override
		public void handleReturn(final int replyCode, final String replyText, final String exchange, final String routingKey, final BasicProperties properties, final byte[] body) throws IOException {
			LOGGER.warn(
				"Message {} publication in {}:{} failed ({}): {}",
				properties.getHeaders().get(BROKER_CONTROLLER_MESSAGE),
				exchange,
				routingKey,
				replyCode,
				replyText);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BrokerController.class);

	static final String EXCHANGE_TYPE="topic";

	static final String BROKER_CONTROLLER_MESSAGE = "X-BrokerController-Message";

	private final Broker broker;
	private final ConversionContext context;
	private final String name;

	private final Lock read;
	private final Lock write;

	private final Map<Long,Channel> channels;

	private final Deque<Cleaner> cleaners;
	private final List<MessageHandlerConsumer> callbacks;

	private final AtomicLong messageCounter;

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
		this.channels=Maps.newLinkedHashMap();
		this.cleaners=Lists.newLinkedList();
		this.callbacks=Lists.newArrayList();
		this.messageCounter=new AtomicLong();
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
			final String message = String.format("Could not connect to broker at %s:%s using virtual host %s",this.broker.host(),this.broker.port(),this.broker.virtualHost());
			throw new ControllerException(message,e);
		} finally {
			this.write.unlock();
		}
	}

	void disconnect() {
		this.write.lock();
		try {
			if(!this.connected) {
				return;
			}
			cleanUp();
			closeChannelsQuietly();
			closeConnectionQuietly();
			this.callbacks.clear();
			this.connected=false;
		} finally {
			this.write.unlock();
		}
	}

	void declareExchange(final String exchangeName) throws ControllerException {
		this.read.lock();
		try {
			channel().exchangeDeclare(exchangeName,EXCHANGE_TYPE,true,true,null);
		} catch (final IOException e) {
			if(FailureAnalyzer.isExchangeDeclarationRecoverable(e)) {
				createChannel();
			} else {
				throw new ControllerException("Could not create "+this.name+" exchange named '"+exchangeName+"'",e);
			}
		} finally {
			this.read.unlock();
		}
	}

	/**
	 * The declared queues are durable, shared and auto-delete, and expire if no
	 * client uses them after 1 second.
	 */
	String declareQueue(final String queueName) throws ControllerException {
		final String targetQueueName=Optional.fromNullable(queueName).or("");
		this.read.lock();
		try {
			final Map<String, Object> args=
				ImmutableMap.
					<String, Object>builder().
						put("x-expires",1000).
						build();
			final DeclareOk ok = channel().queueDeclare(targetQueueName,true,false,true,args);
			final String declaredQueueName = ok.getQueue();
			this.cleaners.push(CleanerFactory.queueDelete(declaredQueueName));
			return declaredQueueName;
		} catch (final IOException e) {
			throw new ControllerException("Could not create "+this.name+" queue named '"+targetQueueName+"'",e);
		} finally {
			this.read.unlock();
		}
	}

	void bindQueue(final String exchangeName, final String queueName, final String routingKey) throws ControllerException {
		this.read.lock();
		try {
			channel().queueBind(queueName,exchangeName,routingKey);
			this.cleaners.push(CleanerFactory.queueUnbind(exchangeName,queueName,routingKey));
		} catch (final IOException e) {
			throw new ControllerException("Could not bind "+this.name+" queue '"+queueName+"' to exchange '"+exchangeName+"' using routing key '"+routingKey+"'",e);
		} finally {
			this.read.unlock();
		}
	}

	String prepareQueue(final String exchangeName, final String queueName, final String routingKey) throws ControllerException {
		this.read.lock();
		try {
			final String declaredQueue = declareQueue(queueName);
			bindQueue(exchangeName, declaredQueue, routingKey);
			return declaredQueue;
		} finally {
			this.read.unlock();
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
			LOGGER.warn("Could not serialize message {}: {}",message,e.getMessage());
			throw new IOException("Could not serialize message",e);
		}
	}

	void publishMessage(final DeliveryChannel replyTo, final String message) throws IOException {
		this.read.lock();
		try {
			publishMessage(replyTo.exchangeName(), replyTo.routingKey(), message);
		} finally {
			this.read.unlock();
		}
	}

	void registerConsumer(final MessageHandler handler, final String queueName) throws IOException {
		this.read.lock();
		try {
			final MessageHandlerConsumer callback = new MessageHandlerConsumer(this.channel, handler);
			channel().
				basicConsume(
					queueName,
					true,
					callback
				);
			this.callbacks.add(callback);
		} finally {
			this.read.unlock();
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

	private Channel channel() {
		Preconditions.checkState(this.connected,"Not connected");
		return this.channel;
	}

	private void createChannel() throws ControllerException {
		try {
			this.channel = createNewChannel();
			this.connected=true;
		} catch (final Exception e) {
			this.connected=false;
			closeConnectionQuietly();
			throw new ControllerException("Could not create channel for broker connection",e);
		}
	}

	private Channel createNewChannel() throws IOException {
		Preconditions.checkState(this.connection!=null,"No connection available");
		final Channel result = this.connection.createChannel();
		Preconditions.checkState(result!=null,"No channel available");
		result.addReturnListener(new LoggingReturnListener());
		return result;
	}

	private void publishMessage(final String exchangeName, final String routingKey, final String message) throws IOException {
		final Channel aChannel = currentChannel();
		try {
			LOGGER.debug("Publishing message to exchange '{}' and routing key '{}'. Payload: \n{}",exchangeName,routingKey,message);
			final Map<String, Object> headers=Maps.newLinkedHashMap();
			headers.put(BROKER_CONTROLLER_MESSAGE,this.messageCounter.incrementAndGet());
			aChannel.
				basicPublish(
					exchangeName,
					routingKey,
					true,
					MessageProperties.MINIMAL_PERSISTENT_BASIC.builder().headers(headers).build(),
					message.getBytes());
		} catch (final IOException e) {
			discardChannel(aChannel);
			LOGGER.warn("Could not publish message [{}] to exchange '{}' and routing key '{}': {}",message,exchangeName,routingKey,e.getMessage());
			throw e;
		} catch (final Exception e) {
			discardChannel(aChannel);
			final String errorMessage = String.format("Unexpected failure while publishing message [%s] to exchange '%s' and routing key '%s' using broker %s:%s%s: %s",message,exchangeName,routingKey,this.broker.host(),this.broker.port(),this.broker.virtualHost(),e.getMessage());
			LOGGER.error(errorMessage);
			throw new IOException(errorMessage,e);
		}
	}

	private void discardChannel(final Channel channel) {
		final long threadId = Thread.currentThread().getId();
		closeQuietly(channel);
		synchronized(this.channels) {
			this.channels.remove(threadId);
		}
	}

	private Channel currentChannel() throws IOException {
		final long threadId = Thread.currentThread().getId();
		Channel result=null;
		synchronized(this.channels) {
			result=this.channels.get(threadId);
			if(result==null) {
				result=createNewChannel();
				this.channels.put(threadId,result);
			}
		}
		return result;
	}

	private void closeQuietly(final Channel channel) {
		if(channel.isOpen()) {
			try {
				channel.close();
			} catch (final Exception e) {
				LOGGER.trace("Could not close channel gracefully",e);
			}
		}
	}

	private void closeChannelsQuietly() {
		closeQuietly(this.channel);
		for(final Channel tmpChannel:this.channels.values()) {
			closeQuietly(tmpChannel);
		}
		this.channels.clear();
		this.channel=null;
	}

	private void closeConnectionQuietly() {
		if(this.connection!=null) {
			try {
				this.connection.close();
			} catch (final Exception e) {
				LOGGER.trace("Could not close connection gracefully",e);
			}
			this.connection=null;
		}
	}

}