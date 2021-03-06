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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector.rdf;

import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;
import org.smartdeveloperhub.curator.protocol.vocabulary.FOAF;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDFS;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.rdf.model.Model;

public final class Namespaces {

	private Namespaces() {
	}

	public static void setUpNamespacePrefixes(Model model) {
		model.setNsPrefix(RDF.PREFIX,RDF.NAMESPACE);
		model.setNsPrefix(RDFS.PREFIX,RDFS.NAMESPACE);
		model.setNsPrefix(XSD.PREFIX,XSD.NAMESPACE);
		model.setNsPrefix(FOAF.PREFIX,FOAF.NAMESPACE);
		model.setNsPrefix(STOA.PREFIX,STOA.NAMESPACE);
		model.setNsPrefix(AMQP.PREFIX,AMQP.NAMESPACE);
		model.setNsPrefix(TYPES.PREFIX,TYPES.NAMESPACE);
	}

}
