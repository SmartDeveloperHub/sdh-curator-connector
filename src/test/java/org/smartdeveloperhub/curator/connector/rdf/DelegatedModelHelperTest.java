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
package org.smartdeveloperhub.curator.connector.rdf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.net.URI;
import java.net.URL;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class DelegatedModelHelperTest {

	@Mocked private ImmutableResourceHelper helper;
	@Mocked private ResourceHelper resourceHelper;

	private String strId="http://www.smartdeveloperhub.org/";
	private URI uriId=URI.create(strId);

	private static class Subclass extends DelegatedModelHelper<ModelHelper> {

		Subclass(ModelHelper delegate) {
			super(delegate);
		}

	}

	@Test
	public void testDelegate() throws Exception {
		Subclass subclass = new Subclass(helper);
		assertThat(subclass.delegate(),sameInstance((ModelHelper)helper));
	}

	@Test
	public void testResourceString() throws Exception {
		new Expectations() {{
			helper.resource(strId);result=resourceHelper;
		}};
		Subclass subclass = new Subclass(helper);
		ResourceHelper result = subclass.resource(strId);
		assertThat(result,sameInstance(resourceHelper));
	}

	@Test
	public void testResourceURI() throws Exception {
		new Expectations() {{
			helper.resource(uriId);result=resourceHelper;
		}};
		Subclass subclass = new Subclass(helper);
		ResourceHelper result = subclass.resource(uriId);
		assertThat(result,sameInstance(resourceHelper));
	}

	@Test
	public void testResourceURL() throws Exception {
		final URL urlId = uriId.toURL();
		new Expectations() {{
			helper.resource(urlId);result=resourceHelper;
		}};
		Subclass subclass = new Subclass(helper);
		ResourceHelper result = subclass.resource(urlId);
		assertThat(result,sameInstance(resourceHelper));
	}

}
