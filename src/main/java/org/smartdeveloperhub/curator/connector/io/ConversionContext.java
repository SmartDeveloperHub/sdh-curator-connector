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
package org.smartdeveloperhub.curator.connector.io;

import java.net.URI;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

final class ConversionContext {

	private static final URI NULL_BASE = URI.create("");

	private final URI base;
	private final ImmutableMap<String, String> namespacePrefixes;

	private ConversionContext(URI base, ImmutableMap<String, String> namespacePrefixes) {
		this.base=base;
		this.namespacePrefixes=namespacePrefixes;
	}

	URI base() {
		return this.base;
	}

	Map<String,String> namespacePrefixes() {
		return this.namespacePrefixes;
	}

	ConversionContext withBase(URI base) {
		return
			new ConversionContext(
				base==null?NULL_BASE:base,
				this.namespacePrefixes);
	}

	ConversionContext withNamespacePrefix(String namespace, String prefix) {
		return
			new ConversionContext(
				this.base,
				ImmutableMap.
					<String,String>builder().
						putAll(this.namespacePrefixes).
						put(namespace,prefix).
						build());
	}

	static ConversionContext newInstance() {
		return new ConversionContext(NULL_BASE,ImmutableMap.<String,String>builder().build());
	}

}
