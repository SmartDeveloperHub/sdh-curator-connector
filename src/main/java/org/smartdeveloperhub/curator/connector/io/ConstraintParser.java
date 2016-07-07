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
package org.smartdeveloperhub.curator.connector.io;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.ConstraintBuilder;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.NamedValue;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.Variable;
import org.smartdeveloperhub.curator.protocol.vocabulary.STOA;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

final class ConstraintParser {

	private static final Logger LOGGER=LoggerFactory.getLogger(ConstraintParser.class);

	private final Deque<Resource> pendingResources;
	private final List<Resource> parsedResources;
	private final Map<Resource,NamedValue> resourceTarget;
	private final Map<Resource,List<Binding>> resourceBindings;
	private final Model model;
	private final Resource resource;

	private ConstraintParser(Model model, Resource resource, List<Variable> variables) {
		this.model = model;
		this.resource = resource;
		this.pendingResources=Lists.newLinkedList();
		this.parsedResources=Lists.newArrayList();
		this.resourceTarget=Maps.newLinkedHashMap();
		this.resourceBindings=Maps.newLinkedHashMap();
		boostrap(variables);
	}

	private List<Constraint> parse() {
		LOGGER.trace("Parsing constraints for {}",this.resource);
		while(!this.pendingResources.isEmpty()) {
			Resource pendingResource=nextPendingResource();
			LOGGER.trace("Processing {}...",pendingResource);
			createBindings(pendingResource);
			enqueueReferrals(pendingResource);
		}
		return buildConstraints();
	}

	private void boostrap(List<Variable> variables) {
		for(Variable variable:variables) {
			Resource blankNode = this.model.createResource(AnonId.create(variable.name()));
			this.pendingResources.add(blankNode);
			this.resourceTarget.put(blankNode, variable);
		}
	}

	private List<Constraint> buildConstraints() {
		List<Constraint> constraints=Lists.newArrayList();
		for(Entry<Resource,NamedValue> entry:this.resourceTarget.entrySet()) {
			List<Binding> bindings=this.resourceBindings.get(entry.getKey());
			if(!bindings.isEmpty()) {
				constraints.add(assembleConstraint(entry.getValue(),bindings));
			}
		}
		return constraints;
	}

	private Constraint assembleConstraint(NamedValue target, List<Binding> bindings) {
		ConstraintBuilder builder = ProtocolFactory.newConstraint().withTarget(target);
		for(Binding binding:bindings) {
			builder.withBinding(binding);
		}
		LOGGER.trace("Created constraint for {} with {} binding(s)",target.name(),bindings.size());
		return builder.build();
	}

	private void createBindings(Resource resource) {
		List<Binding> bindings=resolveResourceBindings(resource);
		StmtIterator iterator=this.model.listStatements(resource, null,(RDFNode)null);
		try {
			while(iterator.hasNext()) {
				final Binding binding = createBinding(iterator.next());
				String message="    + Rejected value {}";
				if(!isProtected(binding)) {
					message="    + Added value {}";
					bindings.add(binding);
				}
				LOGGER.trace(message,binding.value());
			}
		} finally {
			iterator.close();
		}
	}

	private boolean isProtected(Binding binding) {
		if(!binding.property().toString().equals(RDF.TYPE)) {
			return false;
		}
		final Value rawValue = binding.value();
		if(!(rawValue instanceof org.smartdeveloperhub.curator.protocol.Resource)) {
			return false;
		}
		org.smartdeveloperhub.curator.protocol.Resource value=(org.smartdeveloperhub.curator.protocol.Resource)rawValue;
		return value.name().toString().equals(STOA.VARIABLE_TYPE);
	}

	private void enqueueReferrals(Resource resource) {
		StmtIterator iterator=this.model.listStatements(null,null,resource);
		try {
			while(iterator.hasNext()) {
				final Statement statement = iterator.next();
				final Resource subject = statement.getSubject();
				final Property predicate = statement.getPredicate();
				LOGGER.trace("  - Found {} referred by {} via {}",resource,subject,predicate);
				addPendingResource(subject);
			}
		} finally {
			iterator.close();
		}
	}

	private List<Binding> resolveResourceBindings(Resource resource) {
		resolveResource(resource,false);
		List<Binding> bindings=Lists.newArrayList();
		this.resourceBindings.put(resource,bindings);
		return bindings;
	}

	private Resource nextPendingResource() {
		Resource pendingResource=this.pendingResources.pop();
		this.parsedResources.add(pendingResource);
		return pendingResource;
	}

	private Binding createBinding(Statement statement) {
		final String predicate = statement.getPredicate().getURI();
		LOGGER.trace("  - Found binding for {}",predicate);
		return
			ProtocolFactory.
				newBinding().
					withProperty(predicate).
					withValue(createValue(statement.getObject())).
					build();
	}

	private Value createValue(RDFNode object) {
		if(object.isLiteral()) {
			Literal literal = object.asLiteral();
			return
				ProtocolFactory.
					newLiteral().
						withLexicalForm(literal.getLexicalForm()).
						withDatatype(literal.getDatatypeURI()).
						withLanguage(literal.getLanguage()).
						build();
		} else {
			return resolveResource(object.asResource(),true);
		}
	}

	private NamedValue resolveResource(Resource resource, boolean enqueue) {
		if(enqueue) {
			addPendingResource(resource);
		}
		NamedValue target=this.resourceTarget.get(resource);
		if(target==null) {
			target=createNamedValue(resource);
			this.resourceTarget.put(resource, target);
		}
		return target;
	}

	private void addPendingResource(Resource resource) {
		if(!this.resource.equals(resource) && !this.pendingResources.contains(resource) && !this.parsedResources.contains(resource)) {
			LOGGER.trace("    + Added pending resource {}",resource);
			this.pendingResources.add(resource);
		}
	}

	private NamedValue createNamedValue(Resource resource) {
		NamedValue result=null;
		if(resource.isAnon()) {
			result=ProtocolFactory.newVariable(resource.getId().getLabelString());
		} else { // MUST BE RESOURCE
			result=ProtocolFactory.newResource(resource.getURI());
		}
		return result;
	}

	static List<Constraint> fromModel(Model model, Resource resource, List<Variable> variables) {
		return new ConstraintParser(model,resource,variables).parse();
	}

}
