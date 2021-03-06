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
package org.smartdeveloperhub.curator.connector.protocol;

import org.smartdeveloperhub.curator.connector.RuntimeConnectorException;

public class ValidationException extends RuntimeConnectorException {

	private static final long serialVersionUID = 6617159761808495488L;

	private final String value;

	private final String type;

	private final String description;

	public ValidationException(Object value, String type) {
		this(value,type,null,null);
	}

	public ValidationException(Object value, String type, String description) {
		this(value,type,description,null);
	}

	public ValidationException(Object value, String type, String description, Throwable cause) {
		super("Value "+toString(value)+" is not a valid "+type+(description!=null?": "+description:""),cause);
		this.value = value!=null?value.toString():null;
		this.type = type;
		this.description = description;
	}

	public String getValue() {
		return this.value;
	}

	public String getType() {
		return this.type;
	}

	public String getDescription() {
		return this.description;
	}

	private static String toString(Object value) {
		return
			value==null?
				"<null>":
				"'"+value+"'";
	}

}
