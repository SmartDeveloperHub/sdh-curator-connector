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
 *   Artifact    : org.smartdeveloperhub.curator:sdh-curator-connector:0.2.0-SNAPSHOT
 *   Bundle      : sdh-curator-connector-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.curator.connector;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.*;

public final class Failure {

	private final long code;
	private final Long subcode;
	private final String reason;
	private final String details;

	private Failure(long code, Long subcode, String reason, String detail) {
		this.code=code;
		this.subcode=subcode;
		this.reason=reason;
		this.details=detail;
	}

	public long code() {
		return this.code;
	}

	public Optional<Long> subcode() {
		return Optional.fromNullable(subcode);
	}

	public String reason() {
		return this.reason;
	}

	public String details() {
		return this.details;
	}

	public Failure withCode(long code) {
		checkArgument(code>=0,"Failure code cannot be lower than zero (%s)",code);
		return new Failure(code,this.subcode,this.reason,this.details);
	}

	public Failure withSubcode(Long subcode) {
		checkArgument(subcode==null || subcode>=0,"Failure subcode cannot be lower than zero (%s)",subcode);
		return new Failure(this.code,subcode,this.reason,this.details);
	}

	public Failure withReason(String reason) {
		checkNotNull(reason,"Failure reason cannot be null");
		return new Failure(this.code,this.subcode,reason,this.details);
	}

	public Failure withDetail(String detail) {
		return new Failure(this.code,this.subcode,this.reason,detail);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.code,this.subcode,this.reason,this.details);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Failure) {
			Failure that=(Failure)obj;
			result=
				Objects.equals(this.code,that.code) &&
				Objects.equals(this.subcode,that.subcode) &&
				Objects.equals(this.reason,that.reason) &&
				Objects.equals(this.details,that.details);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("code",this.code).
					add("subcode",this.subcode).
					add("reason",this.reason).
					add("details",this.details).
					toString();
	}

	public static Failure newInstance() {
		return new Failure(0,null,"Unexpected failure",null);
	}

}
