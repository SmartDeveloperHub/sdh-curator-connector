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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.rabbitmq.client.AMQP.Channel.Close;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownSignalException;

final class FailureAnalyzer {

	private static final class PreconditionFailure {

		private static final Pattern PRECONDITION_FAILED_PATTERN=Pattern.compile("PRECONDITION_FAILED - inequivalent arg '(.+)' for exchange '(.+)' in vhost '(.+)': received '(.+)' but current is '(.+)'");

		private final String argument;
		private final String exchangeName;
		private final String virtualHost;
		private final String requestedValue;
		private final String actualValue;

		public PreconditionFailure(final Matcher matcher) {
			this.argument=matcher.group(1);
			this.exchangeName=matcher.group(2);
			this.virtualHost=matcher.group(3);
			this.requestedValue=matcher.group(4);
			this.actualValue=matcher.group(5);
		}

		String argument() {
			return this.argument;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("virtualHost", this.virtualHost).
						add("exchangeName", this.exchangeName).
						add("argument", this.argument).
						add("requestedValue", this.requestedValue).
						add("actualValue", this.actualValue).
						toString();
		}

		private static PreconditionFailure fromString(final String message) {
			PreconditionFailure result=null;
			final Matcher matcher = PRECONDITION_FAILED_PATTERN.matcher(message);
			if(matcher.matches()) {
				result=new PreconditionFailure(matcher);
			}
			return result;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(FailureAnalyzer.class);

	private FailureAnalyzer() {
	}

	static boolean isExchangeDeclarationRecoverable(final IOException e) {
		boolean recoverable=false;
		final Throwable cause = e.getCause();
		if(cause instanceof ShutdownSignalException) {
			final Method method = ((ShutdownSignalException)cause).getReason();
			if(method instanceof Close) {
				recoverable=isValidClose((Close)method);
			}
		}
		return recoverable;
	}

	private static boolean isValidClose(final Close close) {
		boolean result=false;
		if(close.getReplyCode()==406 && close.getMethodId()==10) {
			final PreconditionFailure failure=PreconditionFailure.fromString(close.getReplyText());
			if(failure!=null) {
				final String argument = failure.argument();
				result=!"type".equals(argument) && !"internal".equals(argument);
				if(!result) {
					LOGGER.error("Cannot recover from {}",failure);
				}
			}
		}
		return result;
	}

}
