package de.langenmaier.u2r3.owl;

import org.apache.log4j.Logger;
import org.semanticweb.owl.model.OWLAntiSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLAxiomAnnotationAxiom;
import org.semanticweb.owl.model.OWLAxiomVisitor;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLDataSubPropertyAxiom;
import org.semanticweb.owl.model.OWLDeclarationAxiom;
import org.semanticweb.owl.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owl.model.OWLDisjointClassesAxiom;
import org.semanticweb.owl.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLDisjointUnionAxiom;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owl.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owl.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owl.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLImportsDeclaration;
import org.semanticweb.owl.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owl.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyChainSubPropertyAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owl.model.OWLObjectSubPropertyAxiom;
import org.semanticweb.owl.model.OWLOntologyAnnotationAxiom;
import org.semanticweb.owl.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLSameIndividualsAxiom;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owl.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.SWRLRule;

import de.langenmaier.u2r3.db.ClassAssertionRelation;
import de.langenmaier.u2r3.db.DataPropertyDomainRelation;
import de.langenmaier.u2r3.db.DataPropertyRangeRelation;
import de.langenmaier.u2r3.db.DataSubPropertyRelation;
import de.langenmaier.u2r3.db.ObjectPropertyAssertionRelation;
import de.langenmaier.u2r3.db.ObjectPropertyDomainRelation;
import de.langenmaier.u2r3.db.ObjectPropertyRangeRelation;
import de.langenmaier.u2r3.db.SubClassRelation;
import de.langenmaier.u2r3.exceptions.U2R3NotInProfileException;

public class OWL2RLDBAdder implements OWLAxiomVisitor {
	static Logger logger = Logger.getLogger(OWL2RLDBAdder.class);

	@Override
	public void visit(OWLSubClassAxiom axiom) {
		logger.debug("  adding SubClass:" + axiom.toString());
		SubClassRelation.getRelation().add(axiom);
		logger.debug("  added SubClass");
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLAntiSymmetricObjectPropertyAxiom arg0) {
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
		DataPropertyDomainRelation.getRelation().add(axiom);
		logger.debug("  added DataProptertyDomain");
	}

	@Override
	public void visit(OWLImportsDeclaration arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLAxiomAnnotationAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		logger.debug("  adding ObjectProptertyDomain:" + axiom.toString());
		ObjectPropertyDomainRelation.getRelation().add(axiom);
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
		ObjectPropertyRangeRelation.getRelation().add(axiom);
		logger.debug("  added ObjectPropertyRange");
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		logger.debug("  adding ObjectPropertyAssertion:" + axiom.toString());
		ObjectPropertyAssertionRelation.getRelation().add(axiom);
		logger.debug("  added ObjectPropertyAssertion");
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLObjectSubPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDeclarationAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLEntityAnnotationAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLOntologyAnnotationAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		logger.debug("  adding DataPropertyRange:" + axiom.toString());
		DataPropertyRangeRelation.getRelation().add(axiom);
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
		ClassAssertionRelation.getRelation().add(axiom);
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
	public void visit(OWLDataSubPropertyAxiom axiom) {
		logger.debug("  adding DataSubProperty:" + axiom.toString());
		DataSubPropertyRelation.getRelation().add(axiom);
		logger.debug("  added DataSubProperty");
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLSameIndividualsAxiom arg0) {
		throw new U2R3NotInProfileException(arg0);
	}

	@Override
	public void visit(OWLObjectPropertyChainSubPropertyAxiom arg0) {
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

}
