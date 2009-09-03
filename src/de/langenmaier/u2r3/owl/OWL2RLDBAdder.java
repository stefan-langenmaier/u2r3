package de.langenmaier.u2r3.owl;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
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

import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotInProfileException;

public class OWL2RLDBAdder implements OWLAxiomVisitor {
	static Logger logger = Logger.getLogger(OWL2RLDBAdder.class);


	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		logger.debug("  adding DataProptertyDomain:" + axiom.toString());
		RelationManager.getRelation(RelationName.dataPropertyDomain).add(axiom);
		//DataPropertyDomainRelation.getRelation().add(axiom);
		logger.debug("  added DataProptertyDomain");
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		logger.debug("  adding ObjectProptertyDomain:" + axiom.toString());
		RelationManager.getRelation(RelationName.objectPropertyDomain).add(axiom);
		//ObjectPropertyDomainRelation.getRelation().add(axiom);
		logger.debug("  added ObjectProptertyDomain");
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		logger.debug("  adding ObjectPropertyRange:" + axiom.toString());
		RelationManager.getRelation(RelationName.objectPropertyRange).add(axiom);
		//ObjectPropertyRangeRelation.getRelation().add(axiom);
		logger.debug("  added ObjectPropertyRange");
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		logger.debug("  adding ObjectPropertyAssertion:" + axiom.toString());
		RelationManager.getRelation(RelationName.objectPropertyAssertion).add(axiom);
		//ObjectPropertyAssertionRelation.getRelation().add(axiom);
		logger.debug("  added ObjectPropertyAssertion");
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		System.out.println("  NOTHING TO DO" + axiom.getAxiomType().toString());
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		logger.debug("  adding DataPropertyRange:" + axiom.toString());
		RelationManager.getRelation(RelationName.dataPropertyRange).add(axiom);
		//DataPropertyRangeRelation.getRelation().add(axiom);
		logger.debug("  added DataPropertyRange");
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		logger.debug("  adding ClassAssertionAxiom:" + axiom.toString());
		RelationManager.getRelation(RelationName.classAssertion).add(axiom);
		//ClassAssertionRelation.getRelation().add(axiom);
		logger.debug("  added ClassAssertionAxiom");
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}


	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}


	@Override
	public void visit(OWLInverseObjectPropertiesAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(SWRLRule arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		logger.debug("  adding SubClass:" + axiom.toString());
		RelationManager.getRelation(RelationName.subClass).add(axiom);
		//SubClassRelation.getRelation().add(axiom);
		logger.debug("  added SubClass");
		
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
//		logger.debug("  adding DataSubProperty:" + axiom.toString());
//		XXXSubDataPropertyRelation.getRelation().add(axiom);
//		logger.debug("  added DataSubProperty");
		throw new U2R3NotInProfileException(axiom);
		
	}

	@Override
	public void visit(OWLSameIndividualAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		
	}

	@Override
	public void visit(OWLHasKeyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
		
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
