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

import org.smartdeveloperhub.curator.protocol.Accepted;
import org.smartdeveloperhub.curator.protocol.Disconnect;
import org.smartdeveloperhub.curator.protocol.EnrichmentRequest;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponse;
import org.smartdeveloperhub.curator.protocol.Failure;
import org.smartdeveloperhub.curator.protocol.Request;
import org.smartdeveloperhub.curator.protocol.Response;

public class Notifier {

	public final void onResponse(Response response) {
		if(response instanceof Accepted) {
			this.onAccepted((Accepted)response);
		} else if(response instanceof Failure ) {
			this.onFailure((Failure)response);
		} else if(response instanceof EnrichmentResponse) {
			this.onEnrichmentResponse((EnrichmentResponse)response);
		}
	}

	public final void onRequest(Request request) {
		if(request instanceof Disconnect) {
			this.onDisconnect((Disconnect)request);
		} else if(request instanceof EnrichmentRequest) {
			this.onEnrichmentRequest((EnrichmentRequest)request);
		}
	}

	public void onDisconnect(Disconnect response) {
		// To be refined by subclasses
	}

	public void onEnrichmentRequest(EnrichmentRequest request) {
		// To be refined by subclasses
	}

	public void onAccepted(Accepted accepted) {
		// To be refined by subclasses
	}

	public void onFailure(Failure response) {
		// To be refined by subclasses
	}

	public void onEnrichmentResponse(EnrichmentResponse response) {
		// To be refined by subclasses
	}

}