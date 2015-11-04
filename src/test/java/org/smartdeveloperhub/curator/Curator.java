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
package org.smartdeveloperhub.curator;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.EnrichmentResult;
import org.smartdeveloperhub.curator.connector.Failure;
import org.smartdeveloperhub.curator.connector.ResponseProvider;
import org.smartdeveloperhub.curator.connector.SimpleCurator;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public final class Curator {

	private final class CustomResponseProvider implements ResponseProvider {

		@Override
		public boolean isExpected(final UUID messageId) {
			return isAccepted(messageId) || isRejected(messageId);
		}

		boolean isRejected(final UUID messageId) {
			return Curator.this.failures.containsKey(messageId) || Curator.this.rejected.contains(messageId) ;
		}

		@Override
		public boolean isAccepted(final UUID messageId) {
			return Curator.this.results.containsKey(messageId) || Curator.this.accepted.contains(messageId) ;
		}

		@Override
		public Failure getFailure(final UUID messageId) {
			final Failure result = getNext(messageId, Curator.this.failures, Curator.this.rejected);
			LOGGER.info("Consuming failure {} for message {}",result,messageId);
			return result;
		}

		@Override
		public EnrichmentResult getResult(final UUID messageId) {
			final EnrichmentResult result = getNext(messageId, Curator.this.results, Curator.this.accepted);
			LOGGER.info("Consuming result {} for message {}",result,messageId);
			return result;
		}

		private <T> T getNext(final UUID messageId, final Multimap<UUID, T> mappings, final List<UUID> generic) {
			T result=null;
			final Collection<T> collection = mappings.get(messageId);
			if(collection!=null) {
				try {
					result=Iterables.get(collection,0);
					mappings.remove(messageId, result);
				} catch (final Exception e) {
					// No response available
				}
			} else {
				generic.remove(messageId);
			}
			return result;
		}

		@Override
		public long acknowledgeDelay(final UUID messageId, final TimeUnit unit) {
			return unit.convert(Curator.this.acknowledgeDelay, Curator.this.acknowledgeDelayUnit);
		}

		@Override
		public long resultDelay(final UUID messageId, final TimeUnit unit) {
			return unit.convert(Curator.this.resultDelay, Curator.this.resultDelayUnit);
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(Curator.class);

	private final SimpleCurator delegate;
	private final List<UUID> accepted;
	private final List<UUID> rejected;
	private final Multimap<UUID,Failure> failures;
	private final Multimap<UUID,EnrichmentResult> results;

	private long acknowledgeDelay=150;
	private TimeUnit acknowledgeDelayUnit=TimeUnit.MILLISECONDS;

	private long resultDelay=150;
	private TimeUnit resultDelayUnit=TimeUnit.MILLISECONDS;

	private Curator(final DeliveryChannel connector, final Notifier notifier, final ConversionContext context) {
		this.delegate=new SimpleCurator(connector,notifier,new CustomResponseProvider(),context);
		this.failures=ArrayListMultimap.create();
		this.results=ArrayListMultimap.create();
		this.accepted=Lists.newArrayList();
		this.rejected=Lists.newArrayList();
	}

	public void connect(final Agent agent) {
		try {
			this.delegate.connect(agent);
		} catch (final Exception e) {
			throw new IllegalStateException("Could not connect curator",e);
		}
	}

	public Curator delayAcknowledges(final long delay, final TimeUnit unit) {
		this.acknowledgeDelay=delay;
		this.acknowledgeDelayUnit=unit;
		return this;
	}

	public Curator delayResults(final long delay, final TimeUnit unit) {
		this.resultDelay=delay;
		this.resultDelayUnit=unit;
		return this;
	}

	public Curator accept(final UUID messageId) {
		LOGGER.info("Accept {}",messageId);
		this.accepted.add(messageId);
		return this;
	}

	public Curator fail(final UUID messageId) {
		LOGGER.info("Fail {}",messageId);
		this.rejected.add(messageId);
		return this;
	}

	public Curator accept(final UUID messageId, final EnrichmentResult result) {
		LOGGER.info("Accept {} with {}",messageId,result);
		this.results.put(messageId,result);
		return this;
	}

	public Curator fail(final UUID messageId, final Failure description) {
		LOGGER.info("Fail {} with {}",messageId,description);
		this.failures.put(messageId,description);
		return this;
	}

	public void disconnect() {
		try {
			this.delegate.disconnect();
		} catch (final Exception e) {
			throw new IllegalStateException("Could not disconnect curator",e);
		}
	}

	public static Curator newInstance(final DeliveryChannel connector, final Notifier notifier) {
		return new Curator(connector,notifier,ConversionContext.newInstance());
	}

	public static Curator newInstance(final DeliveryChannel connector, final Notifier notifier, final ConversionContext context) {
		return new Curator(connector,notifier,context);
	}

}
