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
import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public final class ConversionContext {

	private static final URI NULL_BASE = URI.create("");

	private final URI base;
	private final Map<String, String> namespacePrefixes;

	private ConversionContext(final URI base, final Map<String, String> namespacePrefixes) {
		this.base=base;
		this.namespacePrefixes=namespacePrefixes;
	}

	public URI base() {
		return this.base;
	}

	public Map<String,String> namespacePrefixes() {
		return Collections.unmodifiableMap(this.namespacePrefixes);
	}

	public ConversionContext withBase(final URI base) {
		return
			new ConversionContext(
				base==null?NULL_BASE:base,
				this.namespacePrefixes);
	}

	public ConversionContext withNamespacePrefix(final String namespace, final String prefix) {
		Preconditions.checkNotNull(namespace,"Namespace cannot be null");
		Preconditions.checkNotNull(prefix,"Prefix cannot be null");
		final Map<String, String> newNamespacePrefixes = Maps.newLinkedHashMap(this.namespacePrefixes);
		newNamespacePrefixes.put(namespace, prefix);
		return new ConversionContext(this.base,newNamespacePrefixes);
	}

	public static ConversionContext newInstance() {
		return new ConversionContext(NULL_BASE,Maps.<String,String>newLinkedHashMap());
	}

}
