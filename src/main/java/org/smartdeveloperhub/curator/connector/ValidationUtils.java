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
package org.smartdeveloperhub.curator.connector;

import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;

final class ValidationUtils {

	private static final int MAX_SEMI_SHORT_STR_LENGTH = 127;

	private static final int MAX_SHORT_STR_LENGTH = 255;

	private static final Pattern DOMAIN_NAME=Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*(\\-?[a-zA-Z0-9]+)*(\\.[a-zA-Z][a-zA-Z0-9]*(\\-?[a-zA-Z0-9]+)*)*$");

	private static final Pattern AMQP_NAME=Pattern.compile("^[a-zA-Z0-9\\-_\\.:]*$");

	private static final Pattern AMQP_ROUTING_KEY=Pattern.compile("^[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*$");

	private ValidationUtils() {
	}

	/**
	 * See <a href="http://tools.ietf.org/html/rfc1034#section-3.5">RFC 1034</a>; <i>Section 3.5 : Preferred Syntax</i>: <br/><br/>
	 *
	 * <code>
	 * &lt;domain&gt;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;::= &lt;subdomain&gt; | " " <br/>
	 * &lt;subdomain&gt;&nbsp;&nbsp;&nbsp;::= &lt;label&gt; | &lt;subdomain&gt; "." &lt;label&gt;<br/>
	 * &lt;label&gt;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;::= &lt;letter&gt; [ [ &lt;ldh-str&gt; ] &lt;let-dig&gt; ]<br/>
	 * &lt;ldh-str&gt;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;::= &lt;let-dig-hyp&gt; | &lt;let-dig-hyp&gt; &lt;ldh-str&gt;<br/>
	 * &lt;let-dig-hyp&gt;&nbsp;::= &lt;let-dig&gt; | "-"<br/>
	 * &lt;let-dig&gt;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;::= &lt;letter&gt; | &lt;digit&gt;<br/><br/>
	 * &lt;letter&gt;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;::= any one of the 52 alphabetic characters A through Z in upper case and a through z in lower case<br/>
	 * &lt;digit&gt;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;::= any one of the ten digits 0 through 9<br/>
	 * </code>
	 */
	private static boolean isValidDomainName(String hostname) {
		return DOMAIN_NAME.matcher(hostname).matches();
	}

	static void validatePath(String path) {
		Preconditions.checkArgument(path!=null,"Path cannot be null");
		Preconditions.checkArgument(path.length()>0,"Path cannot be empty");
		Preconditions.checkArgument(path.length()<=MAX_SEMI_SHORT_STR_LENGTH,"Path cannot be larger than 127 octets ("+path.length()+")");
	}

	static void validateName(String name) {
		if(name==null) {
			return;
		}
		Preconditions.checkArgument(name.length()<=MAX_SEMI_SHORT_STR_LENGTH,"Name cannot be larger than 127 octets ("+name.length()+")");
		Preconditions.checkArgument(AMQP_NAME.matcher(name).matches(),"Invalid name syntax");
	}

	static void validateRoutingKey(String routingKey) {
		if(routingKey==null) {
			return;
		}
		if(routingKey.isEmpty()) {
			return;
		}
		Preconditions.checkArgument(routingKey.length()<=MAX_SHORT_STR_LENGTH,"Routing key cannot be larger than 255 octets ("+routingKey.length()+")");
		Preconditions.checkArgument(AMQP_ROUTING_KEY.matcher(routingKey).matches(),"Invalid routing key syntax");
	}

	static void validateHostname(String hostname) {
		Preconditions.checkArgument(InetAddresses.isInetAddress(hostname) || isValidDomainName(hostname),"Host name '%s' is not valid",hostname);
	}

	static void validatePort(int port) {
		Preconditions.checkArgument(port>=0,"Invalid port number (%s is lower than 0)",port);
		Preconditions.checkArgument(port<65536,"Invalid port number (%s is greater than 65535)",port);
	}

}
