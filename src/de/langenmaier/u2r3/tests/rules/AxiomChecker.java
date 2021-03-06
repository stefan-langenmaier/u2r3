package de.langenmaier.u2r3.tests.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.owl.OWL2RLDBAdder;
import de.langenmaier.u2r3.util.U2R3Component;

public class AxiomChecker extends U2R3Component implements
		OWLAxiomVisitor {
	
	static Logger logger = Logger.getLogger(OWL2RLDBAdder.class);
	U2R3Reasoner reasoner;
	OWLDataFactory df;
	private boolean correct = true;
	private boolean used = true;
	
	public boolean isCorrect() {
		return correct && used;
	}

	public AxiomChecker(U2R3Reasoner reasoner) {
		super(reasoner);
		this.reasoner = reasoner;
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
			used = false;
			logger.trace("Testing for axiom:" + axiom.toString());

			if (!reasoner.isEntailed(axiom)) {
				correct = false;
			}
			
			used = true;
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		used = false;
		
		if (!reasoner.isEntailed(axiom)) {
			correct = false;
		}

		used = true;
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLDisjointClassesAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		used = false;
		
		if(!reasoner.isEntailed(axiom)) {
			correct = false;
		}
		used = true;
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		used = false;
		logger.trace("Testing for axiom:" + axiom.toString());
		
		if (!reasoner.isEntailed(axiom)) {
			correct = false;
		}
		used = true;
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		used = false;
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		used = false;
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		used = false;
		
		if(!reasoner.isEntailed(axiom)) {
			correct = false;
		}
		used = true;
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		used = false;
		logger.trace("Testing for axiom:" + axiom.toString());
		logger.trace("Is axiom defined?");
		
		if (!reasoner.isEntailed(axiom)) {
			correct = false;

		}
		used = true;
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		used = false;
		logger.trace("Testing for axiom:" + axiom.toString());
		
		if(!reasoner.isEntailed(axiom)) {
			correct = false;
		}
		
		used = true;
	}

	@Override
	public void visit(OWLDisjointUnionAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLClassAssertionAxiom arg0) {
		used = false;
		logger.trace("Testing for axiom:" + arg0.toString());
		logger.trace("Is axiom defined?");
		if(!reasoner.isEntailed(arg0)) {
			correct = false;
		}
		
		used = true;
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		used = false;
		logger.trace("Testing for axiom:" + axiom.toString());

		if (!reasoner.isEntailed(axiom)) {
			correct = false;
		}

		used = true;
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		used = false;
		logger.trace("Testing for axiom:" + axiom.toString());
		logger.trace("Is axiom defined?");
		
		if (!reasoner.isEntailed(axiom)) {
			correct = false;
		}
		used = true;
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		used = false;
		logger.trace("Testing for axiom:" + axiom.toString());
		logger.trace("Is axiom defined?");
		if(!reasoner.isEntailed(axiom)) {
			correct = false;
		}
		
		used = true;
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLHasKeyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(SWRLRule arg0) {
		used = false;

	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom arg0) {
		used = false;

	}

}
