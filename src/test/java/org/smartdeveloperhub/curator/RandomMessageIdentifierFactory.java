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
package org.smartdeveloperhub.curator;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.MessageIdentifierFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class RandomMessageIdentifierFactory implements MessageIdentifierFactory {

	private static final Logger LOGGER=LoggerFactory.getLogger(RandomMessageIdentifierFactory.class);

	private final List<UUID> generated;
	private final Queue<UUID> ids;
	private final List<UUID> consumed;

	private int count;

	private RandomMessageIdentifierFactory(List<UUID> ids) {
		this.count=0;
		this.generated=ImmutableList.copyOf(ids);
		this.consumed=Lists.newArrayList();
		this.ids=Lists.newLinkedList(ids);
	}

	public int size() {
		return this.generated.size();
	}

	public UUID generated(int index) {
		return this.generated.get(index);
	}

	@Override
	public UUID nextIdentifier() {
		final UUID result = this.ids.poll();
		this.consumed.add(result);
		LOGGER.trace("Generated ID {} --> {}",count,result);
		count++;
		return result;
	}

	public static RandomMessageIdentifierFactory create(int idCount) {
		List<UUID> generated=Lists.newArrayList();
		for(int i=0;i<idCount;i++) {
			generated.add(UUID.randomUUID());
		}
		return new RandomMessageIdentifierFactory(generated);
	}

}