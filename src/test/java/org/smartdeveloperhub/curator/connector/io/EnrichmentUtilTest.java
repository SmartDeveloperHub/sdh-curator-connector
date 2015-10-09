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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import mockit.Deencapsulation;

import org.junit.Test;
import org.smartdeveloperhub.curator.connector.ProtocolFactory;

import com.google.common.collect.ImmutableList;

public class EnrichmentUtilTest {

	@Test
	public void testBuilder$acceptsNullPrefix() throws Exception {
		EnrichmentUtil sut = EnrichmentUtil.builder().withBlankNodePrefix(null).build();
		assertThat(sut,notNullValue());
		assertThat((String)Deencapsulation.getField(sut, "blankNodePrefix"),equalTo("bn"));
	}

	@Test
	public void testBuilder$acceptsEmptyPrefix() throws Exception {
		EnrichmentUtil sut = EnrichmentUtil.builder().withBlankNodePrefix("").build();
		assertThat(sut,notNullValue());
		assertThat((String)Deencapsulation.getField(sut, "blankNodePrefix"),equalTo("bn"));
	}

	@Test
	public void testBuilder$acceptsNullFilters() throws Exception {
		EnrichmentUtil sut = EnrichmentUtil.builder().withFilters(null).build();
		assertThat(sut,notNullValue());
	}

	@Test
	public void testBuilder$acceptsNullConstraints() throws Exception {
		EnrichmentUtil sut = EnrichmentUtil.builder().withConstraints(null).build();
		assertThat(sut,notNullValue());
	}

	@Test
	public void testBuilder$acceptsNullBindings() throws Exception {
		EnrichmentUtil sut = EnrichmentUtil.builder().withBindings(null).build();
		assertThat(sut,notNullValue());
	}

	@Test
	public void testBlankNode$keepsPreferred() throws Exception {
		EnrichmentUtil sut = sut();
		assertThat(sut.blankNode("new"),equalTo("new"));
		assertThat(sut.blankNode("new"),equalTo("new"));
	}

	@Test
	public void testBlankNode$generatesFreshForReserved() throws Exception {
		EnrichmentUtil sut = sut();
		assertThat(sut.blankNode("var1"),not(equalTo("var1")));
	}

	@Test
	public void testBlankNode$remembersGeneratedValues() throws Exception {
		EnrichmentUtil sut = sut();
		String bn = sut.blankNode("var1");
		assertThat(sut.blankNode("var1"),equalTo(bn));
	}

	@Test
	public void testBlankNode$generatedHaveCustomPrefix() throws Exception {
		EnrichmentUtil sut = sut();
		assertThat(sut.blankNode("var1"),startsWith("blankNode"));
	}

	private EnrichmentUtil sut() {
		EnrichmentUtil sut =
			EnrichmentUtil.
				builder().
					withBlankNodePrefix("blankNode").
					withFilters(
						ImmutableList.of(
							ProtocolFactory.
								newFilter().
									withProperty("property1").
									withVariable(ProtocolFactory.newVariable("var1")).
							build())).
					withBindings(
						ImmutableList.of(
							ProtocolFactory.
								newBinding().
									withProperty("property2").
									withValue(ProtocolFactory.newVariable("var3")).
							build())).
					withConstraints(
						ImmutableList.of(
							ProtocolFactory.
								newConstraint().
									withTarget(
										ProtocolFactory.newVariable("var2")).
									withBinding(
										ProtocolFactory.
											newBinding().
												withProperty("property2").
												withValue(ProtocolFactory.newVariable("var3"))).
							build())).
					build();
		return sut;
	}

}
