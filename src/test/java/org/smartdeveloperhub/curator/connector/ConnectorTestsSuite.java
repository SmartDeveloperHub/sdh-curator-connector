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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	RuntimeConnectorExceptionTest.class,
	HandlerUtilTest.class,
	ProtocolUtilTest.class,
	FiltersTest.class,
	ConstraintsTest.class,
	BindingsTest.class,
	FailureTest.class,
	EnrichmentTest.class,
	EnrichmentRequestTest.class,
	EnrichmentResultTest.class,
	DefaultConnectorFutureTest.class,
	LoggedConnectorFutureTest.class,
	DefaultMessageIdentifierFactoryTest.class,
	CuratorConfigurationTest.class,
	CleanerFactoryTest.class,
	FailureAnalyzerTest.class,
	BrokerControllerUncaughtExceptionHandlerTest.class,
	BrokerControllerExceptionHandlerTest.class,
	BrokerControllerTest.class,
	ClientCuratorControllerTest.class,
	ClientConnectorControllerTest.class,
	BrokerControllerUncaughtExceptionHandlerTest.class,
	ConnectorTest.class
})
public class ConnectorTestsSuite {

}
