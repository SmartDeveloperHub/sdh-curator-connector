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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.3.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.io.InvalidDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.MessageConversionException;
import org.smartdeveloperhub.curator.connector.io.MessageUtil;
import org.smartdeveloperhub.curator.connector.io.NoDefinitionFoundException;
import org.smartdeveloperhub.curator.connector.io.TooManyDefinitionsFoundException;
import org.smartdeveloperhub.curator.protocol.Message;

final class HandlerUtil {

	private static final Logger LOGGER=LoggerFactory.getLogger(HandlerUtil.class);

	private HandlerUtil() {
	}

	static <T extends Message> T parsePayload(final String payload, final Class<? extends T> messageClass) {
		T request=null;
		try {
			request=
				MessageUtil.
					newInstance().
						fromString(payload, messageClass);
		} catch (final NoDefinitionFoundException e) {
			trace("Payload cannot be parsed as {}:\n{}", messageClass.getName(),payload,e);
		} catch (final TooManyDefinitionsFoundException e) {
			trace("Too many {} definitions found in the payload:\n{}",messageClass.getName(),payload,e);
		} catch (final InvalidDefinitionFoundException e) {
			trace("Could not parse a valid {} from the payload:\n{}",messageClass.getName(),payload,e);
		} catch (final MessageConversionException e) {
			trace("Failed to parse the payload:\n{}",payload,e);
		}
		return request;
	}

	private static void trace(final String message, final Object... args) {
		LOGGER.trace(message,args);
	}

}