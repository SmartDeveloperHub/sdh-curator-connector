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
package org.smartdeveloperhub.curator.connector.protocol;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.protocol.vocabulary.AMQP;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;

import com.rabbitmq.client.ConnectionFactory;

public class ValidationUtilTest {

	private void assertInvalidHostName(final String hostname, final String failure) {
		try {
			ValidationUtil.validateHostname(hostname);
			fail(failure);
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo(hostname));
			assertThat(e.getType(),equalTo(TYPES.HOSTNAME_TYPE));
			assertThat(e.getDescription(),equalTo("Host name '"+hostname+"' is not valid"));
		}
	}

	private void assertInvalidRoutingKey(final String routingKey, final String failure,final String description) {
		try {
			ValidationUtil.validateRoutingKey(routingKey);
			fail(failure);
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo(routingKey));
			assertThat(e.getType(),equalTo(AMQP.ROUTING_KEY_TYPE));
			assertThat(e.getDescription(),equalTo(description));
		}
	}

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(ValidationUtil.class),equalTo(true));
	}

	@Test
	public void testValidatePath$valid() throws Exception {
		ValidationUtil.validatePath("valid path name");
	}

	@Test
	public void testValidatePath$invalid$null() throws Exception {
		try {
			ValidationUtil.validatePath(null);
			fail("Should not accept a null path");
		} catch (final ValidationException e) {
			assertThat(e.getValue(),nullValue());
			assertThat(e.getType(),equalTo(AMQP.PATH_TYPE));
			assertThat(e.getDescription(),equalTo("Path cannot be null"));
		}
	}

	@Test
	public void testValidatePath$invalid$empty() throws Exception {
		try {
			ValidationUtil.validatePath("");
			fail("Should not accept an empty path");
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo(""));
			assertThat(e.getType(),equalTo(AMQP.PATH_TYPE));
			assertThat(e.getDescription(),equalTo("Path cannot be empty"));
		}
	}

	@Test
	public void testValidatePath$invalid$tooLong() throws Exception {
		final char[] chars=new char[128];
		Arrays.fill(chars, 'A');
		final String path = new String(chars);
		try {
			ValidationUtil.validatePath(path);
			fail("Should not accept a long string");
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo(path));
			assertThat(e.getType(),equalTo(AMQP.PATH_TYPE));
			assertThat(e.getDescription(),equalTo("Path cannot be larger than 127 octets (128)"));
		}
	}

	@Test
	public void testValidateName$valid() throws Exception {
		ValidationUtil.validateName("a-valid.name.0001");
	}

	@Test
	public void testValidateName$null() throws Exception {
		ValidationUtil.validateName(null);
	}

	@Test
	public void testValidateName$empty() throws Exception {
		ValidationUtil.validateName("");
	}

	@Test
	public void testValidateName$invalid$tooLong() throws Exception {
		final char[] chars=new char[128];
		Arrays.fill(chars, 'A');
		final String name = new String(chars);
		try {
			ValidationUtil.validateName(name);
			fail("Should not accept a long string");
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo(name));
			assertThat(e.getType(),equalTo(AMQP.NAME_TYPE));
			assertThat(e.getDescription(),equalTo("Name cannot be larger than 127 octets (128)"));
		}
	}

	@Test
	public void testValidateName$invalid$badChars() throws Exception {
		try {
			ValidationUtil.validateName("white spaces not allowed");
			fail("Should not accept string with invalid characters");
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo("white spaces not allowed"));
			assertThat(e.getType(),equalTo(AMQP.NAME_TYPE));
			assertThat(e.getDescription(),equalTo("Invalid name syntax"));
		}
	}

	@Test
	public void testValidateRoutingKey$valid() throws Exception {
		ValidationUtil.validateRoutingKey("valid.hostname");
	}

	@Test
	public void testValidateRoutingKey$null() throws Exception {
		ValidationUtil.validateRoutingKey(null);
	}

	@Test
	public void testValidateRoutingKey$empty() throws Exception {
		ValidationUtil.validateRoutingKey("");
	}

	@Test
	public void testValidateRoutingKey$invalid$tooLong() throws Exception {
		final char[] chars=new char[256];
		Arrays.fill(chars, 'A');
		final String name = new String(chars);
		assertInvalidRoutingKey(name,"Should not accept a long string","Routing key cannot be larger than 255 octets (256)");
	}

	@Test
	public void testValidateRoutingKey$invalid$badSyntax() throws Exception {
		assertInvalidRoutingKey("valid.host-name.with-lots-of-labels.bad$char","Should not accept routing key with bad chars","Invalid routing key syntax");
	}

	@Test
	public void testValidateRoutingKey$invalid$trailingDots() throws Exception {
		assertInvalidRoutingKey("invalid.hostname.", "Should not accept a routing key with trailing dots","Invalid routing key syntax");
	}

	@Test
	public void testValidateRoutingKey$invalid$prefixDots() throws Exception {
		assertInvalidRoutingKey(".invalid.hostname", "Should not accept a routing key with prefix dots","Invalid routing key syntax");
	}

	@Test
	public void testValidateRoutingKey$invalid$innerDots() throws Exception {
		assertInvalidRoutingKey("invalid...hostname", "Should not accept a routing key with inner dots","Invalid routing key syntax");
	}

	@Test
	public void testValidateHostname$ip4$valid() throws Exception {
		ValidationUtil.validateHostname("219.120.22.23");
	}

	@Test
	public void testValidateHostname$ip4$invalid() throws Exception {
		assertInvalidHostName("299.120.22.23", "Should not accept invalid IPv4 addresses");
	}

	@Test
	public void testValidateHostname$ip6$valid() throws Exception {
		ValidationUtil.validateHostname("::219.120.22.23");
	}

	@Test
	public void testValidateHostname$ip6$invalid() throws Exception {
		assertInvalidHostName("::299.120.22.23", "Should not accept invalid IPv6 addresses");
	}

	@Test
	public void testValidateHostname$domainName$valid() throws Exception {
		ValidationUtil.validateHostname("valid.hostname");
	}

	@Test
	public void testValidateHostname$domainName$valid$withHyphens() throws Exception {
		ValidationUtil.validateHostname("valid.host-name.with-lots-of-labels");
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
		ValidationUtil.validatePort(ConnectionFactory.DEFAULT_AMQP_PORT);
	}

	@Test
	public void testValidatePort$invalid$lowerThatZero() {
		try {
			ValidationUtil.validatePort(-1);
			fail("Should not accept a negative port");
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo("-1"));
			assertThat(e.getType(),equalTo(TYPES.PORT_TYPE));
			assertThat(e.getDescription(),equalTo("Invalid port number (-1 is lower than 0)"));
		}
	}

	@Test
	public void testValidatePort$invalid$greaterThanMaxPort() {
		try {
			ValidationUtil.validatePort(65536);
			fail("Should not accept a port over 65535");
		} catch (final ValidationException e) {
			assertThat(e.getValue(),equalTo("65536"));
			assertThat(e.getType(),equalTo(TYPES.PORT_TYPE));
			assertThat(e.getDescription(),equalTo("Invalid port number (65536 is greater than 65535)"));
		}
	}

	@Test
	public void testCheckNotNull$null() throws Exception {
		try {
			ValidationUtil.checkNotNull(null, "type", "description");
		} catch (final ValidationException e) {
			assertThat(e.getDescription(),equalTo("description"));
			assertThat(e.getType(),equalTo("type"));
			assertThat(e.getValue(),nullValue());
		}
	}
}
