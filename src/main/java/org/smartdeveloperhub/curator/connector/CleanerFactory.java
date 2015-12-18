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

import java.io.IOException;

import org.smartdeveloperhub.curator.connector.BrokerController.Cleaner;

import com.rabbitmq.client.Channel;

final class CleanerFactory {

	private static final class UnbindQueueCleaner implements Cleaner {

		private final String exchangeName;
		private final String queueName;
		private final String routingKey;

		private UnbindQueueCleaner(final String exchangeName, final String queueName, final String routingKey) {
			this.exchangeName = exchangeName;
			this.queueName = queueName;
			this.routingKey = routingKey;
		}

		@Override
		public void clean(final Channel channel) throws IOException {
			channel.queueUnbind(this.queueName,this.exchangeName,this.routingKey);
		}

		@Override
		public String toString() {
			return "Unbind queue '"+this.queueName+"' from exchange '"+this.exchangeName+"' and routing key '"+this.routingKey+"'";
		}

	}

	private static final class DeleteQueueCleaner implements Cleaner {

		private final String queueName;

		private DeleteQueueCleaner(final String queueName) {
			this.queueName = queueName;
		}

		@Override
		public void clean(final Channel channel) throws IOException {
			channel.queueDelete(this.queueName);
		}

		@Override
		public String toString() {
			return "Delete queue '"+this.queueName+"'";
		}
	}

	private CleanerFactory() {
	}

	static Cleaner queueDelete(final String queueName) {		return new DeleteQueueCleaner(queueName);
	}

	static Cleaner queueUnbind(final String exchangeName, final String queueName, final String routingKey) {
		return new UnbindQueueCleaner(exchangeName, queueName, routingKey);
	}

}
