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
import java.net.URL;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

final class ImmutableResourceHelper implements ResourceHelper, ModelHelper  {

	private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	private final ImmutableModelHelper delegate;
	private final Resource resource;

	ImmutableResourceHelper(ImmutableModelHelper delegate, Resource resource) {
		this.delegate = delegate;
		this.resource = resource;
	}

	Model model() {
		return this.delegate.model;
	}

	@Override
	public ImmutablePropertyHelper property(String property) {
		return new ImmutablePropertyHelper(this,resource,this.delegate.model().createProperty(property));
	}

	@Override
	public PropertyHelper property(URI property) {
		return property(property.toString());
	}

	@Override
	public <T extends ResourceHelper & ModelHelper> T type(String type) {
		return property(RDF_TYPE).withResource(type);
	}

	@Override
	public <T extends ResourceHelper & ModelHelper> T type(URI type) {
		return type(type.toString());
	}

	@Override
	public ResourceHelper resource(String resourceId) {
		return this.delegate.resource(resourceId);
	}

	@Override
	public ResourceHelper resource(URI resourceId) {
		return this.delegate.resource(resourceId);
	}

	@Override
	public ResourceHelper resource(URL resourceId) {
		return this.delegate.resource(resourceId);
	}

	@Override
	public ResourceHelper blankNode(String bnode) {
		return this.delegate.blankNode(bnode);
	}

}