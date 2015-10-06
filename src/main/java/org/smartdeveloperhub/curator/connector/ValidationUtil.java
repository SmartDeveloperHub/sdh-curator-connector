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

import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.google.common.net.InetAddresses;

final class ValidationUtil {

	private static final int MAX_SEMI_SHORT_STR_LENGTH = 127;

	private static final int MAX_SHORT_STR_LENGTH = 255;

	private static final Pattern DOMAIN_NAME=Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*(\\-?[a-zA-Z0-9]+)*(\\.[a-zA-Z][a-zA-Z0-9]*(\\-?[a-zA-Z0-9]+)*)*$");

	private static final Pattern AMQP_NAME=Pattern.compile("^[a-zA-Z0-9\\-_\\.:]*$");

	private static final Pattern AMQP_ROUTING_KEY=Pattern.compile("^[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)*$");

	private ValidationUtil() {
	}

	private static <T> T checkArgument(boolean passes, String type, T value, String message) {
		if(!passes) {
			throw new ValidationException(value,type,String.format(message,value));
		}
		return value;
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

	static <T> T checkNotNull(T value, String valueType,String description) {
		return checkArgument(value!=null,valueType,value,description);
	}

	static String validatePath(String path) {
		checkArgument(path!=null,AMQP.PATH_TYPE,path,"Path cannot be null");
		checkArgument(path.length()>0,AMQP.PATH_TYPE,path,"Path cannot be empty");
		checkArgument(path.length()<=MAX_SEMI_SHORT_STR_LENGTH,AMQP.PATH_TYPE,path,"Path cannot be larger than 127 octets ("+path.length()+")");
		return path;
	}

	static String validateName(String name) {
		if(name!=null) {
			checkArgument(name.length()<=MAX_SEMI_SHORT_STR_LENGTH,AMQP.NAME_TYPE,name,"Name cannot be larger than 127 octets ("+name.length()+")");
			checkArgument(AMQP_NAME.matcher(name).matches(),AMQP.NAME_TYPE,name,"Invalid name syntax");
		}
		return name;
	}

	static String validateRoutingKey(String routingKey) {
		if(routingKey!=null && !routingKey.isEmpty()) {
			checkArgument(routingKey.length()<=MAX_SHORT_STR_LENGTH,AMQP.PATH_TYPE,routingKey,"Routing key cannot be larger than 255 octets ("+routingKey.length()+")");
			checkArgument(AMQP_ROUTING_KEY.matcher(routingKey).matches(),AMQP.PATH_TYPE,routingKey,"Invalid routing key syntax");
		}
		return routingKey;
	}

	static String validateHostname(String hostname) {
		checkArgument(InetAddresses.isInetAddress(hostname) || isValidDomainName(hostname),TYPES.HOSTNAME_TYPE,hostname,"Host name '%s' is not valid");
		return hostname;
	}

	static int validatePort(int port) {
		checkArgument(port>=0,TYPES.PORT_TYPE,port,"Invalid port number (%s is lower than 0)");
		checkArgument(port<65536,TYPES.PORT_TYPE,port,"Invalid port number (%s is greater than 65535)");
		return port;
	}

	static long validateÙnsignedLong(Long responseNumber, String name) {
		checkNotNull(responseNumber,XSD.UNSIGNED_LONG_TYPE,name+" cannot be null");
		return checkArgument(responseNumber>=0,XSD.UNSIGNED_LONG_TYPE,responseNumber,name+" must be greater than 0 ("+responseNumber+")");
	}


}
