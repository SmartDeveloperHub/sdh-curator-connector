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

import org.smartdeveloperhub.curator.connector.ProtocolFactory.EnrichmentRequestMessageBuilder;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Filter;

final class ProtocolUtil {

	private ProtocolUtil() {
	}

	static EnrichmentResult toEnrichmentResult(EnrichmentResponseMessage response) {
		EnrichmentResult result=EnrichmentResult.newInstance().withTargetResource(response.targetResource());
		for(Binding addition:response.additions()) {
			result=result.withAddition(addition.property(),addition.value());
		}
		for(Binding removal:response.removals()) {
			result=result.withRemoval(removal.property(),removal.value());
		}
		return result;
	}

	static EnrichmentRequestMessageBuilder toRequestBuilder(EnrichmentSpecification specification) {
		final EnrichmentRequestMessageBuilder builder=
			ProtocolFactory.
				newEnrichmentRequestMessage().
					withTargetResource(specification.targetResource());
		for(Filter filter:specification.filters()) {
			builder.withFilter(filter);
		}
		for(Constraint constraint:specification.constraints()) {
			builder.withConstraint(constraint);
		}
		return builder;
	}

	static FailureDescription toFailureDescription(FailureMessage message) {
		return
			FailureDescription.
				newInstance().
					withCode(message.code()).
					withSubcode(message.subcode().orNull()).
					withReason(message.reason()).
					withDetail(message.detail());
	}

}