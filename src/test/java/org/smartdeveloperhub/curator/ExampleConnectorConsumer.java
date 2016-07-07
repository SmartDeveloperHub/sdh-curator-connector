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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0
 *   Bundle      : sdh-curator-connector-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.Connector;
import org.smartdeveloperhub.curator.connector.Constraints;
import org.smartdeveloperhub.curator.connector.CuratorConfiguration;
import org.smartdeveloperhub.curator.connector.Enrichment;
import org.smartdeveloperhub.curator.connector.EnrichmentRequest;
import org.smartdeveloperhub.curator.connector.EnrichmentResult;
import org.smartdeveloperhub.curator.connector.EnrichmentResultHandler;
import org.smartdeveloperhub.curator.connector.Filters;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

public class ExampleConnectorConsumer {

	private static final Logger LOGGER=LoggerFactory.getLogger(ExampleConnectorConsumer.class);

	public static void main(final String[] args) throws Exception {
		final UUID agentId = UUID.randomUUID();
		final Broker broker =
			ProtocolFactory.
				newBroker().
					withHost(args[0]).
					build();
		final DeliveryChannel deliveryChannel =
			ProtocolFactory.
				newDeliveryChannel().
					withBroker(broker).
					withRoutingKey("connector."+agentId).
					build();
		final Connector connector =
			Connector.
				builder().
					withAgentIdentifier(agentId).
					withConnectorChannel(deliveryChannel).
					withCuratorConfiguration(CuratorConfiguration.newInstance().withBroker(broker)).
					withBase("http://localhost:8080/harvester/service/").
					withNamespacePrefix(UseCase.CI_NAMESPACE,"ci").
					withNamespacePrefix(UseCase.SCM_NAMESPACE,"scm").
					withNamespacePrefix(UseCase.DOAP_NAMESPACE,"doap").
					build();
		connector.connect();
		try {
			final Future<Enrichment> response=
				connector.
					requestEnrichment(
						UseCase.
							createRequest(
								"http://localhost:8080/harvester/service/builds/1/",
								"http://vps164.cesvima.upm.es/root/gitlab-enhancer.git",
								"redis-db",
								"92a370b9209690a7df1971c17886563c2c01003c"),
						new EnrichmentResultHandler() {
							@Override
							public void onResult(final EnrichmentResult response) {
								LOGGER.info("Received: {}",response);
							}
						}
					);
			final Enrichment enrichment = response.get();
			LOGGER.info("Received acknowledgement: {}",enrichment);
			LOGGER.info("Awaiting for responses...");
			TimeUnit.SECONDS.sleep(60);
		} finally {
			connector.disconnect();
		}
	}
}

final class UseCase {

	private static final Logger LOGGER=LoggerFactory.getLogger(UseCase.class);

	private static final String REPOSITORY = "repository";
	private static final String BRANCH     = "branch";
	private static final String COMMIT     = "commit";

	static final String DOAP_NAMESPACE = "http://usefulinc.com/ns/doap#";
	static final String SCM_NAMESPACE  = "http://www.smartdeveloperhub.org/vocabulary/scm#";
	static final String CI_NAMESPACE   = "http://www.smartdeveloperhub.org/vocabulary/ci#";

	static {
		LOGGER.warn("Execution enrichment request customization is still missing");
		LOGGER.warn("Execution enrichment result customized processing is still missing");
	}

	private UseCase() {
	}

	static EnrichmentRequest createRequest(final String targetResource, final String repositoryLocation, final String branchName, final String commitId) {
		return
			EnrichmentRequest.
				newInstance().
					withTargetResource(targetResource).
					withFilters(
						Filters.
							newInstance().
								withFilter(ci("forRepository"), REPOSITORY).
								withFilter(ci("forBranch"), BRANCH).
								withFilter(ci("forCommit"), COMMIT)).
					withConstraints(
						Constraints.
							newInstance().
								forVariable(REPOSITORY).
									withProperty(RDF.TYPE).
										andResource(scm("Repository")).
									withProperty(scm("location")).
										andTypedLiteral(repositoryLocation,XSD.ANY_URI_TYPE).
									withProperty(scm("hasBranch")).
										andVariable(BRANCH).
								forVariable(BRANCH).
									withProperty(RDF.TYPE).
										andResource(scm("Branch")).
									withProperty(doap("name")).
										andTypedLiteral(branchName,XSD.STRING_TYPE).
									withProperty(scm("hasCommit")).
										andVariable(COMMIT).
								forVariable(COMMIT).
									withProperty(RDF.TYPE).
										andResource(scm("Commit")).
									withProperty(scm("commitId")).
										andTypedLiteral(commitId,XSD.STRING_TYPE));
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