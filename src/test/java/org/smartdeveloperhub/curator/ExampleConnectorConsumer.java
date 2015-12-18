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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.1.0
 *   Bundle      : sdh-curator-connector-0.1.0.jar
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
import org.smartdeveloperhub.curator.connector.UseCase;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

public class ExampleConnectorConsumer {

	private static final Logger LOGGER=LoggerFactory.getLogger(ExampleConnectorConsumer.class);

	private static EnrichmentRequest request() {
		return
			EnrichmentRequest.
				newInstance().
					withTargetResource("http://localhost:8080/harvester/service/builds/1/").
						withFilters(
							Filters.
								newInstance().
									withFilter(UseCase.ci("forBranch"), "branch")).
						withConstraints(
							Constraints.
								newInstance().
									forVariable("repository").
										withProperty(RDF.TYPE).
											andResource(UseCase.scm("Repository")).
										withProperty(UseCase.doap("name")).
											andTypedLiteral("maven-hpi-plugin",XSD.STRING_TYPE).
										withProperty(UseCase.scm("hasBranch")).
											andVariable("branch").
									forVariable("branch").
										withProperty(RDF.TYPE).
											andResource(UseCase.scm("Branch")).
										withProperty(UseCase.doap("name")).
											andTypedLiteral("master",XSD.STRING_TYPE));
	}

	public static void main(final String[] args) throws Exception {
		final UUID agentId = UUID.randomUUID();
		final Broker broker =
			ProtocolFactory.
				newBroker().
					withHost("localhost").
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
						request(),
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
