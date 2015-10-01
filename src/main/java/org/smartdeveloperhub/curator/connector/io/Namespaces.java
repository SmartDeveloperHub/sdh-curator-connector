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

import com.hp.hpl.jena.rdf.model.Model;

final class Namespaces {

	private static final String RDF_NAMESPACE        = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String RDFS_NAMESPACE       = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema#";
	private static final String FOAF_NAMESPACE       = "http://xmlns.com/foaf/0.1/";
	private static final String CURATOR_NAMESPACE    = "http://www.smartdeveloperhub.org/vocabulary/curator#";
	private static final String AMQP_NAMESPACE       = "http://www.smartdeveloperhub.org/vocabulary/amqp#";
	private static final String TYPES_NAMESPACE      = "http://www.smartdeveloperhub.org/vocabulary/types#";

	private Namespaces() {
	}

	static String rdf(String localName) {
		return RDF_NAMESPACE+localName;
	}

	static String rdfs(String localName) {
		return RDFS_NAMESPACE+localName;
	}

	static String curator(String localName) {
		return CURATOR_NAMESPACE+localName;
	}

	static String amqp(String localName) {
		return AMQP_NAMESPACE+localName;
	}

	static String types(String localName) {
		return TYPES_NAMESPACE+localName;
	}

	static String xsd(String localName) {
		return XML_SCHEMA_NAMESPACE+localName;
	}

	static String foaf(String localName) {
		return FOAF_NAMESPACE+localName;
	}

	static void setUpNamespacePrefixes(Model model) {
		model.setNsPrefix("rdf",RDF_NAMESPACE);
		model.setNsPrefix("rdfs",RDFS_NAMESPACE);
		model.setNsPrefix("xsd",XML_SCHEMA_NAMESPACE);
		model.setNsPrefix("foaf",FOAF_NAMESPACE);
		model.setNsPrefix("curator",CURATOR_NAMESPACE);
		model.setNsPrefix("amqp",AMQP_NAMESPACE);
		model.setNsPrefix("types",TYPES_NAMESPACE);
	}

}
