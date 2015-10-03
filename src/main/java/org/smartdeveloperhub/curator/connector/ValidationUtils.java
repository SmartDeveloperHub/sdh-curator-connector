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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.joda.time.DateTime;

import com.google.common.net.InetAddresses;

final class ValidationUtils {

	private static final int MAX_SEMI_SHORT_STR_LENGTH = 127;

	private static final int MAX_SHORT_STR_LENGTH = 255;

	private static final Pattern DOMAIN_NAME=Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*(\\-?[a-zA-Z0-9]+)*(\\.[a-zA-Z][a-zA-Z0-9]*(\\-?[a-zA-Z0-9]+)*)*$");

	private static final Pattern AMQP_NAME=Pattern.compile("^[a-zA-Z0-9\\-_\\.:]*$");

	private static final Pattern AMQP_ROUTING_KEY=Pattern.compile("^[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*$");

	private ValidationUtils() {
	}

	private static void checkArgument(boolean passes, String type, Object value, String message) {
		if(!passes) {
			throw new ValidationException(value,type,String.format(message,value));
		}
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
		checkArgument(path!=null,"amqp:Path",path,"Path cannot be null");
		checkArgument(path.length()>0,"amqp:Path",path,"Path cannot be empty");
		checkArgument(path.length()<=MAX_SEMI_SHORT_STR_LENGTH,"amqp:Path",path,"Path cannot be larger than 127 octets ("+path.length()+")");
	}

	static void validateName(String name) {
		if(name==null) {
			return;
		}
		checkArgument(name.length()<=MAX_SEMI_SHORT_STR_LENGTH,"amqp:Name",name,"Name cannot be larger than 127 octets ("+name.length()+")");
		checkArgument(AMQP_NAME.matcher(name).matches(),"amqp:Name",name,"Invalid name syntax");
	}

	static void validateRoutingKey(String routingKey) {
		if(routingKey==null) {
			return;
		}
		if(routingKey.isEmpty()) {
			return;
		}
		checkArgument(routingKey.length()<=MAX_SHORT_STR_LENGTH,"amqp:Path",routingKey,"Routing key cannot be larger than 255 octets ("+routingKey.length()+")");
		checkArgument(AMQP_ROUTING_KEY.matcher(routingKey).matches(),"amqp:Path",routingKey,"Invalid routing key syntax");
	}

	static void validateHostname(String hostname) {
		checkArgument(InetAddresses.isInetAddress(hostname) || isValidDomainName(hostname),"types:Hostname",hostname,"Host name '%s' is not valid");
	}

	static void validatePort(int port) {
		checkArgument(port>=0,"types:Port",port,"Invalid port number (%s is lower than 0)");
		checkArgument(port<65536,"types:Port",port,"Invalid port number (%s is greater than 65535)");
	}

	static UUID toUUID(String value) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(value,"types:UUID",e);
		}
	}

	static DateTime toDateTime(String value) {
		try {
			return new DateTime(value);
		} catch (IllegalArgumentException e) {
			throw new ValidationException(value,"xsd:DateTimeStamp",e);
		}
	}

	static URI toURI(String value) {
		try {
			return new URI(value);
		} catch (URISyntaxException e) {
			throw new ValidationException(value,"xsd:anyURI",e);
		}
	}

}
