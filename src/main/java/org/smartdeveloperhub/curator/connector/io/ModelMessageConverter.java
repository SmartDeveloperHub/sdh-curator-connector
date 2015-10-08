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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;
import org.smartdeveloperhub.curator.connector.ValidationException;
import org.smartdeveloperhub.curator.connector.rdf.ModelHelper;
import org.smartdeveloperhub.curator.connector.rdf.ModelUtil;
import org.smartdeveloperhub.curator.connector.rdf.Namespaces;
import org.smartdeveloperhub.curator.protocol.Message;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

abstract class ModelMessageConverter<T extends Message> implements MessageConverter<T> {

	private static final RDFFormat FORMAT = RDFFormat.TURTLE;

	private Resource getTargetResource(Model model) throws MessageConversionException {
		ResIterator iterator=
			model.
				listSubjectsWithProperty(
					model.createProperty(RDF.TYPE),
					model.createResource(messageType()));
		List<Resource> resources = Lists.newArrayList(iterator);
		if(resources.isEmpty()) {
			throw new NoDefinitionFoundException(messageType());
		} else if(resources.size()>1) {
			throw new TooManyDefinitionsFoundException(messageType(),resources.size());
		}
		return resources.get(0);
	}

	@Override
	public final T fromString(ConversionContext context, String body) throws MessageConversionException {
		try {
			Model model=ModelFactory.createDefaultModel();
			RDFDataMgr.
				read(
					model,
					new StringReader(body),
					context.base().toString(),
					FORMAT.getLang());
			return parse(model,getTargetResource(model));
		} catch (RiotException e) {
			throw new MessageConversionException("Could not parse body '"+body+"' as Turtle",e);
		} catch (ValidationException e) {
			throw new InvalidDefinitionFoundException(messageType(),e);
		}
	}

	@Override
	public final String toString(ConversionContext context, T message) throws MessageConversionException {
		StringWriter out = new StringWriter();
		try {
			Model model=ModelFactory.createDefaultModel();
			setUpNamespaces(context, model);
			ModelHelper helper=ModelUtil.createHelper(model);
			toString(message,helper);
			RDFDataMgr.write(out,model,FORMAT);
			out.close();
			return out.toString();
		} catch (IOException e) {
			IOUtils.closeQuietly(out);
			throw new MessageConversionException("Could not serialize message",e);
		}
	}

	private void setUpNamespaces(ConversionContext context, Model model) {
		Namespaces.setUpNamespacePrefixes(model);
		int prefixCounter=0;
		for(Entry<String,String> entry:context.namespacePrefixes().entrySet()) {
			final String namespace = entry.getKey();
			if(model.getNsURIPrefix(namespace)==null) {
				String prefix = entry.getValue();
				if(model.getNsPrefixURI(prefix)!=null) {
					prefixCounter++;
					prefix="pr"+prefixCounter;
				}
				model.setNsPrefix(prefix,namespace);
			}
		}
	}

	protected abstract void toString(T message, ModelHelper helper);

	protected abstract T parse(Model model, Resource resource);

	protected abstract String messageType();

}
