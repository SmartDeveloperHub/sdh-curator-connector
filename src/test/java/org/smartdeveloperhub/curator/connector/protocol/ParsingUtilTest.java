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

import java.util.Date;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDFS;
import org.smartdeveloperhub.curator.protocol.vocabulary.TYPES;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

public class ParsingUtilTest {

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(ParsingUtil.class),equalTo(true));
	}

	@Test
	public void testToDateTime$failure() throws Exception {
		try {
			ParsingUtil.toDateTime("not a valid date");
			fail("Should not accept an invalid date");
		} catch (ValidationException e) {
			assertThat(e.getValue(),equalTo("not a valid date"));
			assertThat(e.getType(),equalTo(XSD.DATE_TIME_TYPE));
			assertThat(e.getDescription(),equalTo("Not a valid date"));
		}
	}

	@Test
	public void testToURI$failure() throws Exception {
		final String value = "http:/bad uri";
		try {
			ParsingUtil.toURI(value);
			fail("Should not accept an invalid uri");
		} catch (ValidationException e) {
			assertThat(e.getValue(),equalTo(value));
			assertThat(e.getType(),equalTo(RDFS.RESOURCE_TYPE));
			assertThat(e.getDescription(),equalTo("Not a valid URI"));
		}
	}

	@Test
	public void testToUnsignedLong$failure() throws Exception {
		final String value = "not a number";
		try {
			ParsingUtil.toUnsignedLong(value);
			fail("Should not accept an invalid long");
		} catch (ValidationException e) {
			assertThat(e.getValue(),equalTo(value));
			assertThat(e.getType(),equalTo(XSD.UNSIGNED_LONG_TYPE));
			assertThat(e.getDescription(),equalTo("Not a valid number"));
		}
	}

	@Test
	public void testToPort$failure() throws Exception {
		final String value = "not a number";
		try {
			ParsingUtil.toPort(value);
			fail("Should not accept an invalid port");
		} catch (ValidationException e) {
			assertThat(e.getValue(),equalTo(value));
			assertThat(e.getType(),equalTo(TYPES.PORT_TYPE));
			assertThat(e.getDescription(),equalTo("Not a valid number"));
		}
	}

	@Test
	public void testToDateTime$null$String() throws Exception {
		assertThat(ParsingUtil.toDateTime((String)null),nullValue());
	}

	@Test
	public void testToDateTime$null$Date() throws Exception {
		assertThat(ParsingUtil.toDateTime((Date)null),nullValue());
	}

	@Test
	public void testToURI$null() throws Exception {
		assertThat(ParsingUtil.toURI((String)null),nullValue());
	}

	@Test
	public void testToUUID$null() throws Exception {
		assertThat(ParsingUtil.toUUID((String)null),nullValue());
	}

	@Test
	public void testToPort$null() throws Exception {
		assertThat(ParsingUtil.toPort((String)null),nullValue());
	}

	@Test
	public void testToUnsignedLong$null() throws Exception {
		assertThat(ParsingUtil.toUnsignedLong((String)null),nullValue());
	}

	@Test
	public void testToVariable$null() throws Exception {
		assertThat(ParsingUtil.toVariable((String)null),nullValue());
	}

	@Test
	public void testToVariable$notNull() throws Exception {
		assertThat(ParsingUtil.toVariable("name").name(),equalTo("name"));
	}

}
