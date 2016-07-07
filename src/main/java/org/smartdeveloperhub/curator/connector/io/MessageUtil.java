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
package org.smartdeveloperhub.curator.connector.io;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Message;

import com.google.common.collect.Maps;

public final class MessageUtil {

	private static final ConcurrentMap<Class<?>,Class<?>> CONVERTERS=Maps.newConcurrentMap();

	static {
		MessageUtil.registerConverter(EnrichmentRequestMessage.class,EnrichmentRequestMessageConverter.class);
		MessageUtil.registerConverter(DisconnectMessage.class, DisconnectMessageConverter.class);
		MessageUtil.registerConverter(EnrichmentResponseMessage.class,EnrichmentResponseMessageConverter.class);
		MessageUtil.registerConverter(AcceptedMessage.class,AcceptedMessageConverter.class);
		MessageUtil.registerConverter(FailureMessage.class,FailureMessageConverter.class);
	}

	private volatile ConversionContext context;

	private MessageUtil() {
		this.context=ConversionContext.newInstance();
	}

	public MessageUtil withConversionContext(ConversionContext context) {
		this.context=
			context==null?
				ConversionContext.newInstance():
				context;
		return this;
	}

	public <T extends Message> T fromString(String body, Class<? extends T> messageClass) throws MessageConversionException {
		return converter(messageClass).fromString(this.context,body);
	}

	public <T extends Message> String toString(T message) throws MessageConversionException {
		return converter(message.getClass()).toString(this.context,message);
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
		if(converterClass!=null) {
			CONVERTERS.putIfAbsent(messageClass, converterClass);
		} else {
			CONVERTERS.remove(messageClass);
		}
	}

	public static MessageUtil newInstance() {
		return new MessageUtil();
	}


}
