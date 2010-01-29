package de.langenmaier.u2r3.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.AxiomNotInProfileException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ClassExpressionNotInProfileException;
import org.semanticweb.owlapi.reasoner.FreshEntitiesException;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerException;
import org.semanticweb.owlapi.reasoner.ReasonerInterruptedException;
import org.semanticweb.owlapi.reasoner.TimeOutException;
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
import org.semanticweb.owlapi.util.Version;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.db.ClassAssertionEntRelation;
import de.langenmaier.u2r3.db.DataPropertyAssertionRelation;
import de.langenmaier.u2r3.db.ObjectPropertyAssertionRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3NotInProfileException;
import de.langenmaier.u2r3.exceptions.U2R3ReasonerException;
import de.langenmaier.u2r3.owl.OWL2RLDBAdder;
import de.langenmaier.u2r3.owl.OWL2RLDBRemover;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.util.NodeIDMapper;
import de.langenmaier.u2r3.util.Settings;

public class U2R3Reasoner extends OWLReasonerBase {
	private RuleManager ruleManager;
	private RelationManager relationManager;
	private ReasonProcessor reasonProcessor;
	private Settings settings;
	private NodeIDMapper nidMapper;
	
	public U2R3Reasoner(OWLOntology ontology, OWLReasonerConfiguration config,
			BufferingMode non_buffering) {
		super(ontology, config, non_buffering);
		
		ruleManager = new RuleManager(this);
		relationManager = new RelationManager(this);
		settings = new Settings();
		reasonProcessor = new ReasonProcessor(this);
		nidMapper = new NodeIDMapper();
		
		relationManager.initialize();
		ruleManager.initialize();
		reasonProcessor.initialize();
		
		if (settings.checkProfile()) {
			OWL2RLProfile profile = new OWL2RLProfile();
			OWLProfileReport report = profile.checkOntology(ontology);
			
			if (!report.isInProfile()) {
				for (OWLProfileViolation violation : report.getViolations()) {
					System.out.println(violation);
				}
				
				throw new U2R3NotInProfileException("OWL file is not in RL Profile!");
			}
		}

		OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder(this);
		for(OWLAxiom ax : ontology.getLogicalAxioms()) {
			ax.accept(axiomAdder);
		}
	}

	@Override
	protected void handleChanges(Set<OWLAxiom> addAxioms,
			Set<OWLAxiom> removeAxioms) {
		OWL2RLDBRemover axiomRemover = new OWL2RLDBRemover(this);
		OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder(this);
		for (OWLAxiom change : removeAxioms) {
			change.accept(axiomRemover);
		}
		for (OWLAxiom change : addAxioms) {
			change.accept(axiomAdder);
		}
	}
	
	@Override
	public NodeSet<OWLClass> getTypes(OWLNamedIndividual namedIndividual, boolean arg1) {
		ClassAssertionEntRelation ca = (ClassAssertionEntRelation) relationManager.getRelation(RelationName.classAssertionEnt);
		return ca.getTypes(namedIndividual);
	}	

	public Settings getSettings() {
		return settings;
	}

	public RelationManager getRelationManager() {
		return relationManager;
	}

	public RuleManager getRuleManager() {
		return ruleManager;
	}

	public ReasonProcessor getReasonProcessor() {
		return reasonProcessor;
	}

	public NodeIDMapper getNIDMapper() {
		return nidMapper;
	}

	//TODO wird von isEntailed ersetzt
	public boolean hasObjectPropertyDomain(
			OWLObjectProperty prop, OWLClass domain) throws U2R3ReasonerException {
		if (!(prop.isAnonymous() || domain.isAnonymous())) {
			return relationManager.getRelation(RelationName.propertyDomain)
				.exists(prop.getIRI().toString(), domain.getIRI().toString());
		}
		return false;
	}
	
	//TODO wird von isEntailed ersetzt
	public boolean hasObjectPropertyRange(
			OWLObjectProperty prop, OWLClass range) throws U2R3ReasonerException {
		return relationManager.getRelation(RelationName.propertyRange)
			.exists(prop.getIRI().toString(), range.getIRI().toString());
	}

	//TODO wird von isEntailed ersetzt
	public boolean isEquivalentProperties(OWLObjectPropertyExpression pe1,
			OWLObjectPropertyExpression pe2) throws U2R3ReasonerException {
		if (!(pe1.isAnonymous() || pe2.isAnonymous())) {
			return relationManager.getRelation(RelationName.equivalentProperty).exists(pe1.asOWLObjectProperty().getIRI().toString(), pe2.asOWLObjectProperty().getIRI().toString());
		}
		throw new U2R3NotImplementedException();
	}
	
	//TODO wird von isEntailed ersetzt
	public boolean isDifferentFrom(OWLLiteral l1, OWLLiteral l2) {
		return l1.equals(l2);
	}


	@Override
	public Node<OWLClass> getBottomClassNode() {
		OWLDataFactory df = getOWLDataFactory();
		return getEquivalentClasses(df.getOWLClass(OWLRDFVocabulary.OWL_NOTHING.getIRI()));
	}

	@Override
	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		OWLDataFactory df = getOWLDataFactory();
		return getEquivalentDataProperties(df.getOWLDataProperty(OWLRDFVocabulary.OWL_BOTTOM_DATA_PROPERTY.getIRI()));
	}

	@Override
	public Node<OWLObjectProperty> getBottomObjectPropertyNode() {
		OWLDataFactory df = getOWLDataFactory();
		return getEquivalentObjectProperties(df.getOWLObjectProperty(OWLRDFVocabulary.OWL_BOTTOM_OBJECT_PROPERTY.getIRI()));
	}

	@Override
	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe,
			boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind,
			OWLDataProperty pe) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		if (ind.isAnonymous() || pe.isAnonymous()) {
			return null;
		} else {
			DataPropertyAssertionRelation dpa = (DataPropertyAssertionRelation) relationManager.getRelation(RelationName.dataPropertyAssertion);
			return dpa.getDataPropertyValues(ind, pe.asOWLDataProperty());
		}
	}

	@Override
	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
			OWLNamedIndividual ind) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce,
			boolean direct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getDisjointDataProperties(
			OWLDataPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectProperty> getDisjointObjectProperties(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectProperty> getEquivalentObjectProperties(
			OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce,
			boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		if (ce.isAnonymous()) {
			return null;
		} else {
			ClassAssertionEntRelation ca = (ClassAssertionEntRelation) relationManager.getRelation(RelationName.classAssertionEnt);
			try {
				return ca.getIndividuals(ce.asOWLClass());
			} catch (OWLReasonerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Node<OWLObjectProperty> getInverseObjectProperties(
			OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyDomains(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyRanges(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
			OWLNamedIndividual ind, OWLObjectPropertyExpression pe)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		if (ind.isAnonymous() || pe.isAnonymous()) {
			return null;
		} else {
			ObjectPropertyAssertionRelation opa = (ObjectPropertyAssertionRelation) relationManager.getRelation(RelationName.objectPropertyAssertion);
			return opa.getObjectPropertyValues(ind, pe.asOWLObjectProperty());
		}
	}

	@Override
	public String getReasonerName() {
		return "u2r3";
	}

	@Override
	public Version getReasonerVersion() {
		return new Version(0, 0, 0, 380);
	}

	@Override
	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe,
			boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectProperty> getSubObjectProperties(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce,
			boolean direct) throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe,
			boolean direct) throws InconsistentOntologyException,
			FreshEntitiesException, ReasonerInterruptedException,
			TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectProperty> getSuperObjectProperties(
			OWLObjectPropertyExpression pe, boolean direct)
			throws InconsistentOntologyException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLClass> getTopClassNode() {
		OWLDataFactory df = getOWLDataFactory();
		return getEquivalentClasses(df.getOWLClass(OWLRDFVocabulary.OWL_THING.getIRI()));
	}

	@Override
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		OWLDataFactory df = getOWLDataFactory();
		return getEquivalentDataProperties(df.getOWLDataProperty(OWLRDFVocabulary.OWL_TOP_DATA_PROPERTY.getIRI()));
	}

	@Override
	public Node<OWLObjectProperty> getTopObjectPropertyNode() {
		OWLDataFactory df = getOWLDataFactory();
		return getEquivalentObjectProperties(df.getOWLObjectProperty(OWLRDFVocabulary.OWL_TOP_OBJECT_PROPERTY.getIRI()));

	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConsistent() throws ReasonerInterruptedException,
			TimeOutException {
		return reasonProcessor.isConsistent();
	}

	@Override
	public boolean isEntailed(OWLAxiom axiom)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException {
		try {
			AxiomType<?> aType = axiom.getAxiomType();
			PreparedStatement stmt = null;
			
			if (aType == AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION) {
				stmt = relationManager.getRelation(RelationName.negativeDataPropertyAssertion).getAxiomLocation(axiom);
			} else if (aType == AxiomType.DATA_PROPERTY_ASSERTION) {
				stmt = relationManager.getRelation(RelationName.dataPropertyAssertion).getAxiomLocation(axiom);
			} else if (aType == AxiomType.OBJECT_PROPERTY_ASSERTION) {
				stmt = relationManager.getRelation(RelationName.objectPropertyAssertion).getAxiomLocation(axiom);
			} else if (aType == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION) {
				stmt = relationManager.getRelation(RelationName.negativeObjectPropertyAssertion).getAxiomLocation(axiom);
			} else if (aType == AxiomType.CLASS_ASSERTION) {
				stmt = relationManager.getRelation(RelationName.classAssertionEnt).getAxiomLocation(axiom);
			} else if (aType == AxiomType.SUBCLASS_OF) {
				stmt = relationManager.getRelation(RelationName.subClass).getAxiomLocation(axiom);
			} else if (aType == AxiomType.SUB_OBJECT_PROPERTY) {
				stmt = relationManager.getRelation(RelationName.subProperty).getAxiomLocation(axiom);
			} else if (aType == AxiomType.SUB_DATA_PROPERTY) {
				stmt = relationManager.getRelation(RelationName.subProperty).getAxiomLocation(axiom);
			}  else if (aType == AxiomType.SAME_INDIVIDUAL) {
				stmt = relationManager.getRelation(RelationName.sameAsEnt).getAxiomLocation(axiom);
			}
			// eq-class koennen mehr als zwei sein, komplizierter
			else if (aType == AxiomType.EQUIVALENT_CLASSES) {
				stmt = relationManager.getRelation(RelationName.equivalentClass).getAxiomLocation(axiom);
			} else {
				throw new U2R3NotImplementedException();
			}
			
			return stmt.execute();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new UnsupportedEntailmentTypeException(axiom);
		}
	}

	@Override
	public boolean isEntailed(Set<? extends OWLAxiom> axioms)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException {
		for(OWLAxiom ax : axioms) {
			if (!isEntailed(ax)) return false;
		}
		return true;
	}

	@Override
	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		if (axiomType == AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION
				|| axiomType == AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION
				|| axiomType == AxiomType.DATA_PROPERTY_ASSERTION
				|| axiomType == AxiomType.OBJECT_PROPERTY_ASSERTION
				|| axiomType == AxiomType.CLASS_ASSERTION
				|| axiomType == AxiomType.SUBCLASS_OF
				|| axiomType == AxiomType.EQUIVALENT_CLASSES
				|| axiomType == AxiomType.SUB_DATA_PROPERTY
				|| axiomType == AxiomType.SUB_OBJECT_PROPERTY) {
			return true;
		}
		return false;
	}

	@Override
	public void prepareReasoner() throws ReasonerInterruptedException,
			TimeOutException {
		reasonProcessor.classify();
	}

	@Override
	public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce)
			throws InconsistentOntologyException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			ReasonerInterruptedException, TimeOutException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLClass> getUnsatisfiableClasses()
			throws ReasonerInterruptedException, TimeOutException {
		return getBottomClassNode();
	}

	@Override
	public boolean isSatisfiable(OWLClassExpression classExpression)
			throws ReasonerInterruptedException, TimeOutException,
			ClassExpressionNotInProfileException, FreshEntitiesException,
			InconsistentOntologyException {
		// TODO Auto-generated method stub
		return false;
	}


}
