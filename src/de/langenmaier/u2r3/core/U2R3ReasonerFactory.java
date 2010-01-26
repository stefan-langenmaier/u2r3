package de.langenmaier.u2r3.core;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

public class U2R3ReasonerFactory implements OWLReasonerFactory {

	@Override
	public String getReasonerName() {
		return "u2r3";
	}


	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology) {
		return createNonBufferingReasoner(ontology, new SimpleConfiguration());
	}


	@Override
	public OWLReasoner createNonBufferingReasoner(OWLOntology ontology,
			OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		return new U2R3Reasoner(ontology, config, BufferingMode.NON_BUFFERING);
	}


	@Override
	public OWLReasoner createReasoner(OWLOntology ontology) {
		return createReasoner(ontology, new SimpleConfiguration());
	}


	@Override
	public OWLReasoner createReasoner(OWLOntology ontology,
			OWLReasonerConfiguration config)
			throws IllegalConfigurationException {
		return new U2R3Reasoner(ontology, config, BufferingMode.NON_BUFFERING);
	}

}
