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

import java.net.URI;
import java.net.URL;

public interface PropertyHelper {

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withLiteral(Object value);

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withLanguageLiteral(Object value, String lang);

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withTypedLiteral(Object value, URI type);

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withTypedLiteral(Object value, String type);

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withResource(String value);

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withResource(URI value);

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withResource(URL value);

	<T extends PropertyHelper & ResourceHelper & ModelHelper> T withBlankNode(String value);

}