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

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

final class ImmutablePropertyHelper implements ModelHelper, ResourceHelper, PropertyHelper {

	private final ImmutableResourceHelper delegate;
	private final Resource resource;
	private final Property property;

	ImmutablePropertyHelper(
			ImmutableResourceHelper delegate,
			Resource resource,
			Property property) {
		this.delegate = delegate;
		this.resource = resource;
		this.property = property;
	}

	private Model model() {
		return this.delegate.model();
	}

	@SuppressWarnings("unchecked")
	private <T extends PropertyHelper & ResourceHelper & ModelHelper> T addStatement(RDFNode object) {
		model().add(this.resource, this.property, object);
		return (T)this;
	}

	@Override
	public <T extends PropertyHelper & ResourceHelper & ModelHelper> T withLiteral(Object value) {
		return addStatement(model().createLiteral(value.toString()));
	}

	@Override
	public <T extends PropertyHelper & ResourceHelper & ModelHelper> T withTypedLiteral(Object value, String type) {
		return addStatement(model().createTypedLiteral(value.toString(),type));
	}

	@Override
	public <T extends PropertyHelper & ResourceHelper & ModelHelper> T withLanguageLiteral(Object value, String lang) {
		return addStatement(model().createLiteral(value.toString(),lang));
	}

	@Override
	public <T extends PropertyHelper & ResourceHelper & ModelHelper> T withResource(String resourceId) {
		return addStatement(model().createResource(resourceId));
	}

	@Override
	public <T extends PropertyHelper & ResourceHelper & ModelHelper> T withResource(URI resourceId) {
		return withResource(resourceId.toString());
	}

	@Override
	public <T extends PropertyHelper & ResourceHelper & ModelHelper> T withResource(URL resourceId) {
		return withResource(resourceId.toString());
	}

	@Override
	public <T extends PropertyHelper & ResourceHelper & ModelHelper> T withBlankNode(String value) {
		return addStatement(model().createResource(AnonId.create(value)));
	}

	@Override
	public PropertyHelper property(String property) {
		return this.delegate.property(property);
	}

	@Override
	public PropertyHelper property(URI property) {
		return this.delegate.property(property);
	}

	@Override
	public <T extends ResourceHelper & ModelHelper> T type(String type) {
		return this.delegate.type(type);
	}

	@Override
	public <T extends ResourceHelper & ModelHelper> T type(URI type) {
		return this.delegate.type(type);
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