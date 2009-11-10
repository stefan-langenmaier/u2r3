package de.langenmaier.u2r3.tests.rules.fzitestcases;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
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
import org.semanticweb.owlapi.model.OWLIndividual;
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

public class FZITestAxiomChecker extends U2R3Component implements
		OWLAxiomVisitor {
	
	static Logger logger = Logger.getLogger(OWL2RLDBAdder.class);
	U2R3Reasoner reasoner;
	OWLDataFactory df;
	private boolean correct = true;
	private boolean used = true;
	
	public boolean isCorrect() {
		return correct && used;
	}

	protected FZITestAxiomChecker(U2R3Reasoner reasoner) {
		super(reasoner);
		this.reasoner = reasoner;
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		try {
			used = false;
			logger.trace("Testing for axiom:" + axiom.toString());

			if (!reasoner.isDefined(axiom.getEntity())) {
				correct = false;
			}
			
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		try {
			used = false;
			
			if (!(axiom.getSubClass().isAnonymous() || axiom.getSuperClass().isAnonymous())) {
				if (!reasoner.isSubClassOf(axiom.getSubClass(), axiom.getSuperClass())) {
					correct = false;
				}
			} else {
				correct = true;
			}
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
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
		try {
			used = false;
			
			if (!(axiom.getProperty().isAnonymous() || axiom.getDomain().isAnonymous())) {
				if(!reasoner.hasObjectPropertyDomain(axiom.getProperty().asOWLObjectProperty(), axiom.getDomain().asOWLClass())) {
					correct = false;
				}
			}
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		used = false;
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		/*try {
			used = false;
			
			if (!(axiom.getSubject().isAnonymous() || axiom.getObject().isAnonymous())) {
				if(!reasoner.hasObjectPropertyRelationship(axiom.getSubject().asNamedIndividual(), axiom.getProperty(), axiom.getObject().asNamedIndividual())) {
					correct = false;
				}
			}
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}*/
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
		try {
			used = false;
			
			if (!(axiom.getProperty().isAnonymous() || axiom.getRange().isAnonymous())) {
				if(!reasoner.hasObjectPropertyRange(axiom.getProperty().asOWLObjectProperty(), axiom.getRange().asOWLClass())) {
					correct = false;
				}
			}
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		try {
			used = false;
			logger.trace("Testing for axiom:" + axiom.toString());
			logger.trace("Is axiom defined?");
			
			if (!(axiom.getSubject().isAnonymous() || axiom.getObject().isAnonymous())) {
				if(!reasoner.hasObjectPropertyRelationship(axiom.getSubject().asNamedIndividual(), axiom.getProperty(), axiom.getObject().asNamedIndividual())) {
					correct = false;
				}
			}
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom arg0) {
		used = false;

	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		try {
			used = false;
			logger.trace("Testing for axiom:" + axiom.toString());
			
			if (!(axiom.getSubProperty().isAnonymous() || axiom.getSuperProperty().isAnonymous())) {
				if(!reasoner.isSubPropertyOf(axiom.getSubProperty(), axiom.getSuperProperty())) {
					correct = false;
				}
			}
			
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
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
		try {
			used = false;
			logger.trace("Testing for axiom:" + arg0.toString());
			logger.trace("Is axiom defined?");
			if(!reasoner.isDefined(arg0.getIndividual())) {
				correct = false;
			}
			logger.trace("Has axiom the type");
			if(!reasoner.hasType(arg0.getIndividual().asNamedIndividual(), arg0.getClassExpression(), true)) {
				correct = false;
			}
			
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom arg0) {
		try {
			used = false;
			logger.trace("Testing for axiom:" + arg0.toString());
			for (OWLClassExpression ce1 : arg0.getClassExpressions()) {
				for (OWLClassExpression ce2 : arg0.getClassExpressions()) {
					if (!ce1.equals(ce2)) {
						if (!reasoner.isEquivalentClass(ce1, ce2)) {
							correct = false;
						}
					}
				}
			}

			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		try {
			used = false;
			logger.trace("Testing for axiom:" + axiom.toString());
			logger.trace("Is axiom defined?");
			
			if (!(axiom.getSubject().isAnonymous())) {
				if(!reasoner.hasDataPropertyRelationship(axiom.getSubject().asNamedIndividual(), axiom.getProperty(), axiom.getObject())) {
					correct = false;
				}
				if (axiom.getObject().isTyped()) {
					//Type überprüfen XXX
					reasoner.hasType(axiom.getObject(), axiom.getObject().asOWLStringLiteral().getDatatype());
				}
			}
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
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
		try {
			used = false;
			logger.trace("Testing for axiom:" + axiom.toString());
			logger.trace("Is axiom defined?");
			for (OWLIndividual ind : axiom.getIndividuals()) {
				if(!((U2R3Reasoner) reasoner).hasSame(ind)) {
					correct = false;
				}
			}
			
			used = true;
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
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
