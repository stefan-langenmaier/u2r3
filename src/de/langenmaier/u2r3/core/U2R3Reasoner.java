package de.langenmaier.u2r3.core;

import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
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

	//TODO wird in isEntailed wandern
	public boolean isEquivalentClass(OWLClassExpression ce1,
			OWLClassExpression ce2) throws OWLReasonerException {
		if (!(ce1.isAnonymous() || ce2.isAnonymous())) {
			return relationManager.getRelation(RelationName.equivalentClass).exists(ce1.asOWLClass().getIRI().toString(), ce2.asOWLClass().getIRI().toString());
		}
		throw new U2R3NotImplementedException();
	}

	//TODO wird in isEntailed wandern
	public boolean isSubClassOf(OWLClassExpression sub, OWLClassExpression sup)
			throws OWLReasonerException {
		if (!(sup.isAnonymous() || sub.isAnonymous())) {
			return relationManager.getRelation(RelationName.subClass).exists(sub.asOWLClass().getIRI().toString(), sup.asOWLClass().getIRI().toString());
		}
		throw new U2R3NotImplementedException();

	}
	
	@Override
	public NodeSet<OWLClass> getTypes(OWLNamedIndividual namedIndividual, boolean arg1) {
		ClassAssertionEntRelation ca = (ClassAssertionEntRelation) relationManager.getRelation(RelationName.classAssertionEnt);
		return ca.getTypes(namedIndividual);
	}

	//TODO wird in isEntailed wandern
	public boolean hasDataPropertyRelationship(OWLNamedIndividual arg0,
			OWLDataPropertyExpression arg1, OWLLiteral arg2)
			throws OWLReasonerException {
		String subject = arg0.getIRI().toString();
		String property = arg1.asOWLDataProperty().getIRI().toString();
		String object = arg2.getLiteral();
		if (!arg2.isOWLTypedLiteral()) {
			String lang = arg2.getLang();
			return relationManager.getRelation(RelationName.dataPropertyAssertion).exists(subject, property, object, lang);
		}
		return relationManager.getRelation(RelationName.dataPropertyAssertion).exists(subject, property, object);
	}

	//TODO wird in isEntailed wandern
	public boolean hasObjectPropertyRelationship(OWLNamedIndividual arg0,
			OWLObjectPropertyExpression arg1, OWLNamedIndividual arg2)
			throws OWLReasonerException {
		String subject = arg0.getIRI().toString();
		String property = arg1.asOWLObjectProperty().getIRI().toString();
		String object = arg2.getIRI().toString();
		return relationManager.getRelation(RelationName.objectPropertyAssertion).exists(subject, property, object);
	}

	//TODO wird in isEntailed wandern
	public boolean hasType(OWLNamedIndividual arg0, OWLClassExpression arg1,
			boolean arg2) throws OWLReasonerException {
		String clazz = arg0.getIRI().toString();
		String type = arg1.asOWLClass().getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionEnt).exists(clazz, type);
	}
	
	//TODO wird in isEntailed wandern
	public boolean hasType(OWLLiteral arg0, OWLDatatype arg1) throws OWLReasonerException {
		String literal = arg0.getLiteral();
		String clazz = arg1.getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionLit).exists(literal, clazz);
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

	public boolean hasSame(OWLIndividual ind) throws OWLReasonerException {
		return relationManager.getRelation(RelationName.sameAsEnt)
			.exists(ind.asOWLNamedIndividual().getIRI().toString());
	}
	
	public OWLDataFactory getDataFactory() {
		return getOWLDataFactory();
	}

	public NodeIDMapper getNIDMapper() {
		return nidMapper;
	}

	public boolean isSubPropertyOf(OWLObjectPropertyExpression sub,
			OWLObjectPropertyExpression sup) throws U2R3ReasonerException {
		if (!(sub.isAnonymous() || sup.isAnonymous())) {
			return relationManager.getRelation(RelationName.subProperty)
				.exists(sub.asOWLObjectProperty().getIRI().toString(), sup.asOWLObjectProperty().getIRI().toString());
		}
		return false;
		
	}

	public boolean hasObjectPropertyDomain(
			OWLObjectProperty prop, OWLClass domain) throws U2R3ReasonerException {
		if (!(prop.isAnonymous() || domain.isAnonymous())) {
			return relationManager.getRelation(RelationName.propertyDomain)
				.exists(prop.getIRI().toString(), domain.getIRI().toString());
		}
		return false;
	}
	
	public boolean hasObjectPropertyRange(
			OWLObjectProperty prop, OWLClass range) throws U2R3ReasonerException {
		return relationManager.getRelation(RelationName.propertyRange)
			.exists(prop.getIRI().toString(), range.getIRI().toString());
	}

	public boolean isEquivalentProperties(OWLObjectPropertyExpression pe1,
			OWLObjectPropertyExpression pe2) throws U2R3ReasonerException {
		if (!(pe1.isAnonymous() || pe2.isAnonymous())) {
			return relationManager.getRelation(RelationName.equivalentProperty).exists(pe1.asOWLObjectProperty().getIRI().toString(), pe2.asOWLObjectProperty().getIRI().toString());
		}
		throw new U2R3NotImplementedException();
	}
	
	public boolean isDifferentFrom(OWLLiteral l1, OWLLiteral l2) {
		return l1.equals(l2);
	}



	@Override
	public Node<OWLClass> getBottomClassNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectProperty> getBottomObjectPropertyNode() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Version getReasonerVersion() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectProperty> getTopObjectPropertyNode() {
		// TODO Auto-generated method stub
		return null;
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
		//XXX axiom statt entity
		/*String clazz = axiom.getIRI().toString();
		String type;
		if (entity.getEntityType() == EntityType.ANNOTATION_PROPERTY) {
			type = OWLRDFVocabulary.OWL_ANNOTATION_PROPERTY.getIRI().toString();
		} else if (entity.getEntityType() == EntityType.OBJECT_PROPERTY) {
			type = OWLRDFVocabulary.OWL_OBJECT_PROPERTY.getIRI().toString();
		} else if (entity.getEntityType() == EntityType.DATA_PROPERTY) {
			type = OWLRDFVocabulary.OWL_DATA_PROPERTY.getIRI().toString();
		} else if (entity.getEntityType() == EntityType.NAMED_INDIVIDUAL) {
			type = OWLRDFVocabulary.OWL_NAMED_INDIVIDUAL.getIRI().toString();
		} else if (entity.getEntityType() == EntityType.CLASS) {
			type = OWLRDFVocabulary.OWL_CLASS.getIRI().toString();
		} else if (entity.getEntityType() == EntityType.DATATYPE) {
			type = OWLRDFVocabulary.OWL_DATATYPE.getIRI().toString();
		} else {
			throw new U2R3NotImplementedException();
		}
		return relationManager.getRelation(RelationName.classAssertionEnt).exists(clazz, type);
		*/
		return false;
	}

	@Override
	public boolean isEntailed(Set<? extends OWLAxiom> axioms)
			throws ReasonerInterruptedException,
			UnsupportedEntailmentTypeException, TimeOutException,
			AxiomNotInProfileException, FreshEntitiesException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
		return true;
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
		// TODO Auto-generated method stub
		return null;
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
