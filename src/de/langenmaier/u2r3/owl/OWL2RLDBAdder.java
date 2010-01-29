package de.langenmaier.u2r3.owl;

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
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotInProfileException;
import de.langenmaier.u2r3.util.U2R3Component;

public class OWL2RLDBAdder extends U2R3Component implements OWLAxiomVisitor {
	OWLDataFactory df;
	
	public OWL2RLDBAdder(U2R3Reasoner reasoner) {
		super(reasoner);
		 df = reasoner.getOWLDataFactory();
	}

	static Logger logger = Logger.getLogger(OWL2RLDBAdder.class);


	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		logger.debug("  adding NegativeObjectProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.negativeObjectPropertyAssertion).add(axiom);
		logger.debug("  added NegativeObjectProperty");
	}

	@Override
	/**
	 * Not allowed in the OWL2 RL Profile
	 */
	public void visit(OWLReflexiveObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		logger.debug("  adding DisjointClasses:" + axiom.toString());
		if (axiom.getClassExpressions().size() == 2) {
			relationManager.getRelation(RelationName.disjointWith).add(axiom);
		} else {
			relationManager.getRelation(RelationName.members).add(axiom);
		}
		logger.debug("  added DisjointClasses");
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		logger.debug("  adding DataProptertyDomain:" + axiom.toString());
		relationManager.getRelation(RelationName.propertyDomain).add(axiom);
		logger.debug("  added DataProptertyDomain");
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		logger.debug("  adding ObjectProptertyDomain:" + axiom.toString());
		relationManager.getRelation(RelationName.propertyDomain).add(axiom);
		logger.debug("  added ObjectProptertyDomain");
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		logger.debug("  adding EquivalentObjectProperties:" + axiom.toString());
		relationManager.getRelation(RelationName.equivalentProperty).add(axiom);
		logger.debug("  added EquivalentObjectProperties");
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		logger.debug("  adding NegativeDataProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.negativeDataPropertyAssertion).add(axiom);
		logger.debug("  added NegativeDataProperty");
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		logger.debug("  adding DifferentIndividuals:" + axiom.toString());
		if (axiom.getIndividuals().size() == 2) {
			relationManager.getRelation(RelationName.differentFromEnt).add(axiom);
		} else {
			relationManager.getRelation(RelationName.members).add(axiom);
		}
		logger.debug("  added DifferentIndividuals");
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		logger.debug("  adding DisjointDataProperties:" + axiom.toString());
		if (axiom.getProperties().size() == 2) {
			relationManager.getRelation(RelationName.propertyDisjointWith).add(axiom);
		} else {
			relationManager.getRelation(RelationName.members).add(axiom);
		}
		logger.debug("  added DisjointDataProperties");
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		logger.debug("  adding DisjointObjectProperties:" + axiom.toString());
		if (axiom.getProperties().size() == 2) {
			relationManager.getRelation(RelationName.propertyDisjointWith).add(axiom);
		} else {
			relationManager.getRelation(RelationName.members).add(axiom);
		}
		logger.debug("  added DisjointObjectProperties");
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		logger.debug("  adding ObjectPropertyRange:" + axiom.toString());
		relationManager.getRelation(RelationName.propertyRange).add(axiom);
		logger.debug("  added ObjectPropertyRange");
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		logger.debug("  adding ObjectPropertyAssertion:" + axiom.toString());
		relationManager.getRelation(RelationName.objectPropertyAssertion).add(axiom);
		logger.debug("  added ObjectPropertyAssertion");
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		logger.debug("  adding FunctionalObjectProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added FunctionalObjectProperty");
	}

	@Override
	/**
	 * Not allowed in the OWL2 RL Profile
	 */
	public void visit(OWLDisjointUnionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	/**
	 * Not allowed because this is already implicit defined
	 */
	public void visit(OWLDeclarationAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		//logger.debug("  adding Declaration:" + axiom.toString());
		//relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		//System.out.println("IGNORIERT: " + axiom);
		//logger.debug("  added Declaration");
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		logger.debug("  adding SymmetricObjectProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added SymmetricObjectProperty");
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		logger.debug("  adding DataPropertyRange:" + axiom.toString());
		relationManager.getRelation(RelationName.propertyRange).add(axiom);
		logger.debug("  added DataPropertyRange");
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		logger.debug("  adding FunctionalDataProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added FunctionalDataProperty");
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		logger.debug("  adding EquivalentDataProperties:" + axiom.toString());
		relationManager.getRelation(RelationName.equivalentProperty).add(axiom);
		logger.debug("  added EquivalentDataProperties");
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		logger.debug("  adding ClassAssertionAxiom:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added ClassAssertionAxiom");
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		logger.debug("  adding EquivalentClassesAxiom:" + axiom.toString());
		relationManager.getRelation(RelationName.equivalentClass).add(axiom);
		logger.debug("  added EquivalentClassesAxiom");
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		logger.debug("  adding DataPropertyAssertionAxiom:" + axiom.toString());
		relationManager.getRelation(RelationName.dataPropertyAssertion).add(axiom);
		logger.debug("  added DataPropertyAssertionAxiom");
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		logger.debug("  adding TransitiveObjectProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added TransitiveObjectProperty");
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		logger.debug("  adding IrreflexiveObjectProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added IrreflexiveObjectProperty");
	}


	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		logger.debug("  adding InverseFunctionalObjectProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added InverseFunctionalObjectProperty");
	}


	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		logger.debug("  adding InverseObjectProperties:" + axiom.toString());
		relationManager.getRelation(RelationName.inverseOf).add(axiom);
		logger.debug("  added InverseObjectProperties");
	}

	@Override
	/**
	 * Not allowed in the OWL2 RL Profile
	 */
	public void visit(SWRLRule arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		logger.debug("  adding SubClass:" + axiom.toString());
		relationManager.getRelation(RelationName.subClass).add(axiom);
		logger.debug("  added SubClass");
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		logger.debug("  adding AsymmetricObjectProperty:" + axiom.toString());
		relationManager.getRelation(RelationName.classAssertionEnt).add(axiom);
		logger.debug("  added AsymmetricObjectProperty");
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		logger.debug("  adding SubObjectPropertyOf:" + axiom.toString());
		relationManager.getRelation(RelationName.subProperty).add(axiom);
		logger.debug("  added SubObjectPropertyOf");
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		logger.debug(" adding SubDataPropertyOf:" + axiom.toString());
		relationManager.getRelation(RelationName.subProperty).add(axiom);
		logger.debug(" added SubDataPropertyOf");
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		logger.debug(" adding SameIndividual:" + axiom.toString());
		relationManager.getRelation(RelationName.sameAsEnt).add(axiom);
		logger.debug(" added SameIndividual");
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		logger.debug(" adding PropertyChain:" + axiom.toString());
		relationManager.getRelation(RelationName.propertyChain).add(axiom);
		logger.debug(" added PropertyChain");
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		logger.debug(" adding HasKey:" + axiom.toString());
		relationManager.getRelation(RelationName.hasKey).add(axiom);
		logger.debug(" added HasKey");		
	}

	
	@Override
	/**
	 * Not allowed in the OWL2 RL Profile
	 */
	public void visit(OWLDatatypeDefinitionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}
	
	/*
	 * annotations are ignored because they are not part of the reasoning
	 */

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		throw new U2R3NotInProfileException(axiom);
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}


}
