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

import static org.smartdeveloperhub.curator.connector.io.Namespaces.setUpNamespacePrefixes;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.smartdeveloperhub.curator.protocol.Message;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

abstract class ModelMessageConverter<T extends Message> implements MessageConverter<T> {

	@Override
	public final T fromString(String body) throws MessageConversionException {
		throw new MessageConversionException("String parsing not supported");
	}

	@Override
	public final String toString(T message) throws MessageConversionException {
		StringWriter out = new StringWriter();
		try {
			Model model=ModelFactory.createDefaultModel();
			setUpNamespacePrefixes(model);
			ModelHelper helper=ModelUtil.createHelper(model);
			toString(message,helper);
			RDFDataMgr.write(out,model,RDFFormat.TURTLE);
			out.close();
			return out.toString();
		} catch (IOException e) {
			closeQuietly(out);
			throw new MessageConversionException("Could not serialize message",e);
		}
	}

	private static void closeQuietly(Closeable closeable) {
		try {
			if(closeable!=null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	protected abstract void toString(T message, ModelHelper helper);

}
