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
package org.smartdeveloperhub.curator.connector.protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDFS;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

final class ParsingUtil {

	private ParsingUtil() {
	}

	private static DateTime newDateTime(Object value) {
		if(value==null) {
			return null;
		}
		try {
			return new DateTime(value);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(value,XSD.DATE_TIME_TYPE,"Not a valid date",e);
		}
	}

	static UUID toUUID(String value) {
		if(value==null) {
			return null;
		}
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(value,TYPES.UUID_TYPE,"Not a valid UUID",e);
		}
	}

	static DateTime toDateTime(String value) {
		return newDateTime(value);
	}

	static DateTime toDateTime(Date submittedOn) {
		return newDateTime(submittedOn);
	}

	static URI toURI(String value) {
		if(value==null) {
			return null;
		}
		try {
			return new URI(value);
		} catch (URISyntaxException e) {
			throw new ValidationException(value,RDFS.RESOURCE_TYPE,"Not a valid URI",e);
		}
	}

	static Integer toPort(String value) {
		if(value==null) {
			return null;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new ValidationException(value,TYPES.PORT_TYPE,"Not a valid number",e);
		}
	}

	static Long toUnsignedLong(String value) {
		if(value==null) {
			return null;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new ValidationException(value,XSD.UNSIGNED_LONG_TYPE,"Not a valid number",e);
		}
	}

	static Variable toVariable(String value) {
		if(value==null) {
			return null;
		}
		return ProtocolFactory.newVariable(value);
	}

}
