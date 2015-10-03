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

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionBase2;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;

public final class SparqlFunctions {

	private static final String NAMESPACE    = "http://www.smartdeveloperhub.org/sparql#";

	private static final String EQUAL        = qualify("equal");
	private static final String LOWER_THAN   = qualify("lowerThan");
	private static final String GREATER_THAN = qualify("greaterThan");

	private static final class CustomFunctionFactory implements FunctionFactory {

		@Override
		public Function create(String uri) {
			Function result=null;
			if(uri.equals(GREATER_THAN)) {
				result=new GreaterThan();
			} else if(uri.equals(LOWER_THAN)) {
				result=new LowerThan();
			} else if(uri.equals(EQUAL)) {
				result=new Equal();
			}
			return result;
		}

	}

	private static class GreaterThan extends FunctionBase2 {
		@Override
		public NodeValue exec(NodeValue v1, NodeValue v2) {
			if(!v1.isDateTime()) {
				return NodeValue.makeBoolean(false);
			}
			if(!v2.isDateTime()) {
				return NodeValue.makeBoolean(false);
			}
			return NodeValue.makeBoolean(v1.getDateTime().compare(v2.getDateTime())>0);
		}
	}

	private static class LowerThan extends FunctionBase2 {
		@Override
		public NodeValue exec(NodeValue v1, NodeValue v2) {
			if(!v1.isDateTime()) {
				return NodeValue.makeBoolean(false);
			}
			if(!v2.isDateTime()) {
				return NodeValue.makeBoolean(false);
			}
			return NodeValue.makeBoolean(v1.getDateTime().compare(v2.getDateTime())<0);
		}
	}

	private static class Equal extends FunctionBase2 {
		@Override
		public NodeValue exec(NodeValue v1, NodeValue v2) {
			if(!v1.isDateTime()) {
				return NodeValue.makeBoolean(false);
			}
			if(!v2.isDateTime()) {
				return NodeValue.makeBoolean(false);
			}
			return NodeValue.makeBoolean(v1.getDateTime().compare(v2.getDateTime())==0);
		}
	}

	private SparqlFunctions() {
	}

	public static void enable() {
		CustomFunctionFactory factory = new CustomFunctionFactory();
		FunctionRegistry.get().put(GREATER_THAN,factory);
		FunctionRegistry.get().put(LOWER_THAN,factory);
		FunctionRegistry.get().put(EQUAL,factory);
	}

	private static String qualify(String string) {
		return NAMESPACE+string;
	}

}
