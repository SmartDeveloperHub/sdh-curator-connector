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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.rabbitmq.client.ConnectionFactory;

public class ValidationUtilsTest {

	private void assertInvalidHostName(String hostname, String failure) {
		try {
			ValidationUtils.validateHostname(hostname);
			fail(failure);
		} catch (Exception e) {
			assertThat(e.getMessage(),equalTo("Host name '"+hostname+"' is not valid"));
		}
	}

	private void assertInvalidRoutingKey(String routingKey, String failure) {
		try {
			ValidationUtils.validateRoutingKey(routingKey);
			fail(failure);
		} catch (Exception e) {
			assertThat(e.getMessage(),equalTo("Invalid routing key syntax"));
		}
	}

	@Test
	public void testValidatePath$valid() throws Exception {
		ValidationUtils.validatePath("valid path name");
	}

	@Test
	public void testValidatePath$invalid$null() throws Exception {
		try {
			ValidationUtils.validatePath(null);
			fail("Should not accept a null path");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Path cannot be null"));
		}
	}

	@Test
	public void testValidatePath$invalid$empty() throws Exception {
		try {
			ValidationUtils.validatePath("");
			fail("Should not accept an empty path");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Path cannot be empty"));
		}
	}

	@Test
	public void testValidatePath$invalid$tooLong() throws Exception {
		char[] chars=new char[128];
		Arrays.fill(chars, 'A');
		try {
			ValidationUtils.validatePath(new String(chars));
			fail("Should not accept a long string");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Path cannot be larger than 127 octets (128)"));
		}
	}

	@Test
	public void testValidateName$valid() throws Exception {
		ValidationUtils.validateName("a-valid.name.0001");
	}

	@Test
	public void testValidateName$null() throws Exception {
		ValidationUtils.validateName(null);
	}

	@Test
	public void testValidateName$empty() throws Exception {
		ValidationUtils.validateName("");
	}

	@Test
	public void testValidateName$invalid$tooLong() throws Exception {
		char[] chars=new char[128];
		Arrays.fill(chars, 'A');
		try {
			ValidationUtils.validateName(new String(chars));
			fail("Should not accept a long string");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Name cannot be larger than 127 octets (128)"));
		}
	}

	@Test
	public void testValidateName$invalid$badChars() throws Exception {
		try {
			ValidationUtils.validateName("white spaces not allowed");
			fail("Should not accept string with invalid characters");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Invalid name syntax"));
		}
	}

	@Test
	public void testValidateRoutingKey$valid() throws Exception {
		ValidationUtils.validateRoutingKey("valid.hostname");
	}

	@Test
	public void testValidateRoutingKey$null() throws Exception {
		ValidationUtils.validateRoutingKey(null);
	}

	@Test
	public void testValidateRoutingKey$empty() throws Exception {
		ValidationUtils.validateRoutingKey("");
	}

	@Test
	public void testValidateRoutingKey$invalid$tooLong() throws Exception {
		char[] chars=new char[256];
		Arrays.fill(chars, 'A');
		try {
			ValidationUtils.validateRoutingKey(new String(chars));
			fail("Should not accept a long string");
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Routing key cannot be larger than 255 octets (256)"));
		}
	}

	@Test
	public void testValidateRoutingKey$invalid$badSyntax() throws Exception {
		assertInvalidRoutingKey("valid.host-name.with-lots-of-labels","Should not accept routing key with bad chars");
	}

	@Test
	public void testValidateRoutingKey$invalid$trailingDots() throws Exception {
		assertInvalidRoutingKey("invalid.hostname.", "Should not accept a routing key with trailing dots");
	}

	@Test
	public void testValidateRoutingKey$invalid$prefixDots() throws Exception {
		assertInvalidRoutingKey(".invalid.hostname", "Should not accept a routing key with prefix dots");
	}

	@Test
	public void testValidateRoutingKey$invalid$innerDots() throws Exception {
		assertInvalidRoutingKey("invalid...hostname", "Should not accept a routing key with inner dots");
	}

	@Test
	public void testValidateHostname$ip4$valid() throws Exception {
		ValidationUtils.validateHostname("219.120.22.23");
	}

	@Test
	public void testValidateHostname$ip4$invalid() throws Exception {
		assertInvalidHostName("299.120.22.23", "Should not accept invalid IPv4 addresses");
	}

	@Test
	public void testValidateHostname$ip6$valid() throws Exception {
		ValidationUtils.validateHostname("::219.120.22.23");
	}

	@Test
	public void testValidateHostname$ip6$invalid() throws Exception {
		assertInvalidHostName("::299.120.22.23", "Should not accept invalid IPv6 addresses");
	}

	@Test
	public void testValidateHostname$domainName$valid() throws Exception {
		ValidationUtils.validateHostname("valid.hostname");
	}

	@Test
	public void testValidateHostname$domainName$valid$withHyphens() throws Exception {
		ValidationUtils.validateHostname("valid.host-name.with-lots-of-labels");
	}

	@Test
	public void testValidateHostname$domainName$trailingDots() throws Exception {
		assertInvalidHostName("invalid.hostname.", "Should not accept a domain name with trailing dots");
	}

	@Test
	public void testValidateHostname$domainName$invalidLabel$startWithDigit() throws Exception {
		assertInvalidHostName("1nvalid.hostname.", "Should not accept a domain name with label starting with digit");
	}

	@Test
	public void testValidateHostname$domainName$invalidLabel$startsWithHyphen() throws Exception {
		assertInvalidHostName("-invalid.hostname.", "Should not accept a domain name with label starting with hyphen");
	}

	@Test
	public void testValidateHostname$domainName$invalidLabel$endsWithHyphen() throws Exception {
		assertInvalidHostName("invalid-.hostname.", "Should not accept a domain name with label ending with hyphen");
	}

	@Test
	public void testValidateHostname$domainName$prefixDots() throws Exception {
		assertInvalidHostName(".invalid.hostname", "Should not accept a domain name with prefix dots");
	}

	@Test
	public void testValidateHostname$domainName$innerDots() throws Exception {
		assertInvalidHostName("invalid...hostname", "Should not accept a domain name with inner dots");
	}

	@Test
	public void testValidatePort$valid() {
		ValidationUtils.validatePort(ConnectionFactory.DEFAULT_AMQP_PORT);
	}

	@Test
	public void testValidatePort$invalid$lowerThatZero() {
		try {
			ValidationUtils.validatePort(-1);
			fail("Should not accept a negative port");
		} catch (Exception e) {
			assertThat(e.getMessage(),equalTo("Invalid port number (-1 is lower than 0)"));
		}
	}

	@Test
	public void testValidatePort$invalid$greaterThanMaxPort() {
		try {
			ValidationUtils.validatePort(65536);
		} catch (Exception e) {
			assertThat(e.getMessage(),equalTo("Invalid port number (65536 is greater than 65535)"));
		}
	}

}
