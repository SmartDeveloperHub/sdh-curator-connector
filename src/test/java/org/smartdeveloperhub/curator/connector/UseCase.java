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

import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

public final class UseCase {

	public static final String DOAP_NAMESPACE = "http://usefulinc.com/ns/doap#";
	public static final String SCM_NAMESPACE  = "http://www.smartdeveloperhub.org/vocabulary/scm#";
	public static final String CI_NAMESPACE   = "http://www.smartdeveloperhub.org/vocabulary/ci#";

	public static final EnrichmentRequest EXAMPLE_REQUEST =
		EnrichmentRequest.
			newInstance().
				withTargetResource("http://localhost:8080/harvester/service/builds/1/").
					withFilters(
						Filters.
							newInstance().
								withFilter(ci("forBranch"), "branch").
								withFilter(ci("forCommit"), "commit")).
					withConstraints(
						Constraints.
							newInstance().
								forVariable("repository").
									withProperty(RDF.TYPE).
										andResource(scm("Repository")).
									withProperty(scm("location")).
										andTypedLiteral("git://github.com/ldp4j/ldp4j.git",XSD.ANY_URI_TYPE).
									withProperty(scm("hasBranch")).
										andVariable("branch").
								forVariable("branch").
									withProperty(RDF.TYPE).
										andResource(scm("Branch")).
									withProperty(doap("name")).
										andTypedLiteral("develop",XSD.STRING_TYPE).
									withProperty(scm("hasCommit")).
										andVariable("commit").
								forVariable("commit").
									withProperty(RDF.TYPE).
										andResource(scm("Commit")).
									withProperty(scm("commitId")).
										andTypedLiteral("f1efd1d8d8ceebef1d85eb66c69a44b0d713ed44",XSD.STRING_TYPE));

	public static final EnrichmentResult EXAMPLE_RESULT =
		EnrichmentResult.
			newInstance().
				withTargetResource("http://localhost:8080/harvester/service/builds/1/").
				withAdditions(
					Bindings.
						newInstance().
							withProperty(ci("forBranch")).
								andResource("http://localhost:8088/harvester/service/repositories/1/branches/2/").
							withProperty(ci("forCommit")).
								andResource("http://localhost:8088/harvester/service/repositories/1/commits/32/"));

	private UseCase() {
	}

	static String ci(final String term) {
		return CI_NAMESPACE+term;
	}

	static String scm(final String term) {
		return SCM_NAMESPACE+term;
	}

	static String doap(final String term) {
		return DOAP_NAMESPACE+term;
	}

}
