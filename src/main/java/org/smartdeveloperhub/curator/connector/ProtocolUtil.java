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
package org.smartdeveloperhub.curator.connector;

import java.util.List;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.EnrichmentRequestMessageBuilder;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.Filter;

final class ProtocolUtil {

	private ProtocolUtil() {
	}

	private static Bindings toBindings(final List<Binding> bindings) {
		Bindings result=Bindings.newInstance();
		for(final Binding binding:bindings) {
			result=result.withProperty(binding.property()).andValue(binding.value());
		}
		return result;
	}

	static EnrichmentResult toEnrichmentResult(final EnrichmentResponseMessage response) {
		return
			EnrichmentResult.
				newInstance().
					withTargetResource(response.targetResource()).
					withAdditions(toBindings(response.additions())).
					withRemovals(toBindings(response.removals()));
	}

	static EnrichmentRequestMessageBuilder toRequestBuilder(final EnrichmentRequest specification) {
		final EnrichmentRequestMessageBuilder builder=
			ProtocolFactory.
				newEnrichmentRequestMessage().
					withTargetResource(specification.targetResource());
		for(final Filter filter:specification.filters()) {
			builder.withFilter(filter);
		}
		for(final Constraint constraint:specification.constraints()) {
			builder.withConstraint(constraint);
		}
		return builder;
	}

	static Failure toFailure(final FailureMessage message) {
		return
			Failure.
				newInstance().
					withCode(message.code()).
					withSubcode(message.subcode().orNull()).
					withReason(message.reason()).
					withDetail(message.detail());
	}

}