package de.langenmaier.u2r3.core;

import java.util.Set;

import org.semanticweb.owlapi.inference.OWLReasoner;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class U2R3ReasonerFactory implements OWLReasonerFactory {

	@Override
	public OWLReasoner createReasoner(OWLOntologyManager arg0,
			Set<OWLOntology> arg1) {
		try {
			if (arg1 == null) {
				return new U2R3Reasoner(arg0);
			}
			return new U2R3Reasoner(arg0, arg1);
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	@Override
	public String getReasonerName() {
		return "u2r3";
	}

}
