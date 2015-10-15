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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionBase2;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;

public final class SparqlFunctions {

	private static final Logger LOGGER=LoggerFactory.getLogger(SparqlFunctions.class);

	private static final String NAMESPACE    = "http://www.smartdeveloperhub.org/sparql#";

	private static final String EQUAL        = qualify("equal");
	private static final String LOWER_THAN   = qualify("lowerThan");
	private static final String GREATER_THAN = qualify("greaterThan");

	private static final class CustomFunctionFactory implements FunctionFactory {

		@Override
		public Function create(String uri) {
			Function result=null;
			if(GREATER_THAN.equals(uri)) {
				result=new GreaterThan();
			} else if(LOWER_THAN.equals(uri)) {
				result=new LowerThan();
			} else { // MUST BE EQUAL
				result=new Equal();
			}
			return result;
		}

	}

	private abstract static class DateTimeComparisonFunction extends FunctionBase2 {

		private boolean firstNonDatetime;
		private boolean secondNonDatetime;

		private DateTimeComparisonFunction(boolean firstNonDatetime, boolean secondNonDatetime) {
			this.firstNonDatetime = firstNonDatetime;
			this.secondNonDatetime = secondNonDatetime;
		}

		@Override
		public final NodeValue exec(NodeValue v1, NodeValue v2) {
			DateTime d1 = toDateTime(v1);
			if(d1==null) {
				return NodeValue.makeBoolean(this.firstNonDatetime);
			}
			DateTime d2 = toDateTime(v2);
			if(d2==null) {
				return NodeValue.makeBoolean(this.secondNonDatetime);
			}
			return NodeValue.makeBoolean(compare(d1, d2));
		}

		private DateTime toDateTime(NodeValue node) {
			DateTime result=null;
			if(XSD.DATE_TIME_TYPE.equals(node.getDatatypeURI())) {
				try {
					result=new DateTime(node.asUnquotedString());
				} catch (IllegalArgumentException e) {
					ignore(node,e);
				}
			}
			return result;
		}

		private void ignore(NodeValue node, IllegalArgumentException e) {
			LOGGER.debug("Invalid date time {}",node,e.getMessage());
		}

		protected abstract boolean compare(DateTime d1, DateTime d2);

	}

	private static class GreaterThan extends DateTimeComparisonFunction {

		private GreaterThan() {
			super(false,false);
		}

		@Override
		protected boolean compare(DateTime d1, DateTime d2) {
			return d1.isAfter(d2);
		}

	}

	private static class LowerThan extends DateTimeComparisonFunction {

		private LowerThan() {
			super(false,false);
		}

		@Override
		protected boolean compare(DateTime d1, DateTime d2) {
			return d1.isBefore(d2);
		}

	}


	private static class Equal extends DateTimeComparisonFunction {

		private Equal() {
			super(false,false);
		}

		@Override
		protected boolean compare(DateTime d1, DateTime d2) {
			return d1.isEqual(d2);
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
