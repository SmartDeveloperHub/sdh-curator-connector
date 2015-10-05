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

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.collect.Maps;

public final class MessageUtil {

	private static final ConcurrentMap<Class<?>,Class<?>> CONVERTERS=Maps.newConcurrentMap();

	static {
		MessageUtil.registerConverter(EnrichmentRequest.class,EnrichmentRequestConverter.class);
		MessageUtil.registerConverter(EnrichmentResponse.class,EnrichmentResponseConverter.class);
		MessageUtil.registerConverter(Accepted.class,AcceptedConverter.class);
	}

	private MessageUtil() {
	}

	public <T extends Message> T fromString(String body, Class<? extends T> messageClass) throws MessageConversionException {
		return converter(messageClass).fromString(body);
	}

	public <T extends Message> String toString(T message) throws MessageConversionException {
		return converter(message.getClass()).toString(message);
	}

	private <T extends Message> MessageConverter<T> converter(Class<? extends T> messageClass) throws MessageConversionException {
		Class<? extends MessageConverter<T>> converterClass = getConverterClass(messageClass);
		if(converterClass==null) {
			throw new MessageConversionException("Cannot convert messages of type '"+messageClass.getName()+"'");
		}
		try {
			return converterClass.newInstance();
		} catch (Exception e) {
			throw new MessageConversionException("Could not instantiate converter '"+converterClass.getName()+"' for message of type '"+messageClass.getName()+"'",e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Message> Class<? extends MessageConverter<T>> getConverterClass(Class<? extends T> messageClass) {
		Class<?> result = CONVERTERS.get(messageClass);
		if(result==null) {
			for(Entry<Class<?>,Class<?>> entry:CONVERTERS.entrySet()) {
				if(entry.getKey().isAssignableFrom(messageClass)) {
					result=entry.getValue();
				}
			}
		}
		return (Class<? extends MessageConverter<T>>)result;
	}

	public static <T extends Message> void registerConverter(Class<? extends T> messageClass, Class<? extends MessageConverter<T>> converterClass) {
		CONVERTERS.putIfAbsent(messageClass, converterClass);
	}

	public static MessageUtil newInstance() {
		return new MessageUtil();
	}


}
