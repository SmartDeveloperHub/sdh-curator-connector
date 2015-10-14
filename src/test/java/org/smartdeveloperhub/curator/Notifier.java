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
package org.smartdeveloperhub.curator;

import org.smartdeveloperhub.curator.protocol.AcceptedMessage;
import org.smartdeveloperhub.curator.protocol.DisconnectMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequestMessage;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;
import org.smartdeveloperhub.curator.protocol.FailureMessage;
import org.smartdeveloperhub.curator.protocol.RequestMessage;
import org.smartdeveloperhub.curator.protocol.ResponseMessage;

public class Notifier {

	public final void onResponse(ResponseMessage response) {
		if(response instanceof AcceptedMessage) {
			this.onAccepted((AcceptedMessage)response);
		} else if(response instanceof FailureMessage ) {
			this.onFailure((FailureMessage)response);
		} else if(response instanceof EnrichmentResponseMessage) {
			this.onEnrichmentResponse((EnrichmentResponseMessage)response);
		}
	}

	public final void onRequest(RequestMessage request) {
		if(request instanceof DisconnectMessage) {
			this.onDisconnect((DisconnectMessage)request);
		} else if(request instanceof EnrichmentRequestMessage) {
			this.onEnrichmentRequest((EnrichmentRequestMessage)request);
		}
	}

	public void onDisconnect(DisconnectMessage response) {
		// To be refined by subclasses
	}

	public void onEnrichmentRequest(EnrichmentRequestMessage request) {
		// To be refined by subclasses
	}

	public void onAccepted(AcceptedMessage accepted) {
		// To be refined by subclasses
	}

	public void onFailure(FailureMessage response) {
		// To be refined by subclasses
	}

	public void onEnrichmentResponse(EnrichmentResponseMessage response) {
		// To be refined by subclasses
	}

}