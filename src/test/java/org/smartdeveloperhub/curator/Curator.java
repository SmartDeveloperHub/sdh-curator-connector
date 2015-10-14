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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.EnrichmentResult;
import org.smartdeveloperhub.curator.connector.Failure;
import org.smartdeveloperhub.curator.connector.ResponseProvider;
import org.smartdeveloperhub.curator.connector.SimpleCurator;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public final class Curator {

	private final class CustomResponseProvider implements ResponseProvider {

		@Override
		public boolean isExpected(UUID messageId) {
			return isAccepted(messageId) || isRejected(messageId);
		}

		boolean isRejected(UUID messageId) {
			return failures.containsKey(messageId) || rejected.contains(messageId) ;
		}

		@Override
		public boolean isAccepted(UUID messageId) {
			return accept || results.containsKey(messageId) || accepted.contains(messageId) ;
		}

		@Override
		public Failure getFailure(UUID messageId) {
			final Failure result = getNext(messageId, failures, rejected);
			LOGGER.info("Consuming failure {} for message {}",result,messageId);
			return result;
		}

		@Override
		public EnrichmentResult getResult(UUID messageId) {
			final EnrichmentResult result = getNext(messageId, results, accepted);
			LOGGER.info("Consuming result {} for message {}",result,messageId);
			return result;
		}

		private <T> T getNext(UUID messageId, Multimap<UUID, T> mappings, List<UUID> generic) {
			T result=null;
			final Collection<T> collection = mappings.get(messageId);
			if(collection!=null) {
				try {
					result=Iterables.get(collection,0);
					mappings.remove(messageId, result);
				} catch (Exception e) {
					// No response available
				}
			} else {
				generic.remove(messageId);
			}
			return result;
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(Curator.class);

	private final SimpleCurator delegate;
	private final List<UUID> accepted;
	private final List<UUID> rejected;
	private final Multimap<UUID,Failure> failures;
	private final Multimap<UUID,EnrichmentResult> results;
	private boolean accept;

	private Curator(DeliveryChannel connector, Notifier notifier) {
		this.delegate=new SimpleCurator(connector,notifier,new CustomResponseProvider());
		this.failures=ArrayListMultimap.create();
		this.results=ArrayListMultimap.create();
		this.accepted=Lists.newArrayList();
		this.rejected=Lists.newArrayList();
		this.accept=true;
	}

	public void connect() {
		try {
			this.delegate.connect();
		} catch (Exception e) {
			throw new IllegalStateException("Could not connect curator",e);
		}
	}

	public Curator acceptByDefault(boolean accept) {
		LOGGER.info("Accept by default: {}",accept);
		this.accept = accept;
		return this;
	}

	public Curator accept(UUID messageId) {
		LOGGER.info("Accept {}",messageId);
		this.accepted.add(messageId);
		return this;
	}

	public Curator fail(UUID messageId) {
		LOGGER.info("Fail {}",messageId);
		this.rejected.add(messageId);
		return this;
	}

	public Curator accept(UUID messageId, EnrichmentResult result) {
		LOGGER.info("Accept {} with {}",messageId,result);
		this.results.put(messageId,result);
		return this;
	}

	public Curator fail(UUID messageId, Failure description) {
		LOGGER.info("Fail {} with {}",messageId,description);
		this.failures.put(messageId,description);
		return this;
	}

	public void disconnect() {
		try {
			this.delegate.disconnect();
		} catch (Exception e) {
			throw new IllegalStateException("Could not disconnect curator",e);
		}
	}

	public static Curator newInstance(DeliveryChannel connector, Notifier notifier) {
		return new Curator(connector,notifier);
	}

}
