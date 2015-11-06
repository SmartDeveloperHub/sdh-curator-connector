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

import java.util.Objects;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Broker;

import com.google.common.base.MoreObjects;

public final class CuratorConfiguration {

	public static final String DEFAULT_EXCHANGE_NAME        = "sdh";

	public static final String DEFAULT_REQUEST_QUEUE_NAME   = "curator.queue";

	public static final String DEFAULT_REQUEST_ROUTING_KEY  = "curator.request";

	public static final String DEFAULT_RESPONSE_ROUTING_KEY = "curator.response";

	public static final Broker DEFAULT_BROKER = ProtocolFactory.newBroker().build();

	private final Broker broker;
	private final String exchangeName;
	private final String queueName;
	private final String requestRoutingKey;
	private final String responseRoutingKey;

	private CuratorConfiguration(
			final Broker broker,
			final String exchangeName,
			final String queueName,
			final String requestRoutingKey,
			final String responseRoutingKey) {
		this.broker = Objects.requireNonNull(broker,"Broker cannot be null");
		this.exchangeName = Objects.requireNonNull(exchangeName,"Exchange name cannot be null");
		this.queueName = Objects.requireNonNull(queueName,"Queue name cannot be null");
		this.requestRoutingKey = Objects.requireNonNull(requestRoutingKey,"Response routing key cannot be null");
		this.responseRoutingKey = Objects.requireNonNull(responseRoutingKey,"Response routing key cannot be null");
	}

	public Broker broker() {
		return this.broker;
	}

	public String exchangeName() {
		return this.exchangeName;
	}

	public String queueName() {
		return this.queueName;
	}

	public String requestRoutingKey() {
		return this.requestRoutingKey;
	}

	public String responseRoutingKey() {
		return this.responseRoutingKey;
	}

	public CuratorConfiguration withBroker(final Broker broker) {
		return new CuratorConfiguration(broker,this.exchangeName,this.queueName,this.requestRoutingKey,this.responseRoutingKey);
	}

	public CuratorConfiguration withExchangeName(final String exchangeName) {
		return new CuratorConfiguration(this.broker,exchangeName,this.queueName,this.requestRoutingKey,this.responseRoutingKey);
	}

	public CuratorConfiguration withQueueName(final String requestQueueName) {
		return new CuratorConfiguration(this.broker,this.exchangeName,requestQueueName,this.requestRoutingKey,this.responseRoutingKey);
	}

	public CuratorConfiguration withRequestRoutingKey(final String requestRoutingKey) {
		return new CuratorConfiguration(this.broker,this.exchangeName,this.queueName,requestRoutingKey,this.responseRoutingKey);
	}

	public CuratorConfiguration withResponseRoutingKey(final String responseRoutingKey) {
		return new CuratorConfiguration(this.broker,this.exchangeName,this.queueName,this.requestRoutingKey,responseRoutingKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return
			Objects.
				hash(
					this.broker,
					this.exchangeName,
					this.queueName,
					this.requestRoutingKey,
					this.responseRoutingKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof CuratorConfiguration) {
			final CuratorConfiguration that=(CuratorConfiguration)obj;
			result=
				Objects.equals(this.broker,that.broker) &&
				Objects.equals(this.exchangeName,that.exchangeName) &&
				hasSameQueueConfig(that);
		}
		return result;
	}

	private boolean hasSameQueueConfig(final CuratorConfiguration that) {
		return
			Objects.equals(this.queueName,that.queueName) &&
			Objects.equals(this.requestRoutingKey,that.requestRoutingKey) &&
			Objects.equals(this.responseRoutingKey,that.responseRoutingKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("broker",this.broker).
					add("exchangeName", this.exchangeName).
					add("queueName",this.queueName).
					add("requestRoutingKey",this.requestRoutingKey).
					add("responseRoutingKey",this.responseRoutingKey).
					toString();
	}

	public static CuratorConfiguration newInstance() {
		return
			new CuratorConfiguration(
				DEFAULT_BROKER,
				DEFAULT_EXCHANGE_NAME,
				DEFAULT_REQUEST_QUEUE_NAME,
				DEFAULT_REQUEST_ROUTING_KEY,
				DEFAULT_RESPONSE_ROUTING_KEY);
	}

}