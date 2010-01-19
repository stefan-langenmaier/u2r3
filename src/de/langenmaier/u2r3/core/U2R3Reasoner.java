package de.langenmaier.u2r3.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.inference.OWLReasonerAdapter;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
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

public class U2R3Reasoner extends OWLReasonerAdapter {
	private RuleManager ruleManager;
	private RelationManager relationManager;
	private ReasonProcessor reasonProcessor;
	private Settings settings;
	private NodeIDMapper nidMapper;
	
	private boolean isClassified = false;

	protected U2R3Reasoner(OWLOntologyManager manager) throws OWLReasonerException {
		super(manager);
		
		ruleManager = new RuleManager(this);
		relationManager = new RelationManager(this);
		settings = new Settings();
		reasonProcessor = new ReasonProcessor(this);
		nidMapper = new NodeIDMapper();
		
		relationManager.initialize();
		ruleManager.initialize();
		reasonProcessor.initialize();
		
	}

	public U2R3Reasoner(OWLOntologyManager manager,
			Set<OWLOntology> importsClosure) throws OWLReasonerException {
		this(manager);

		loadOntologies(importsClosure);		
	}

	@Override
	protected void disposeReasoner() {
		throw new U2R3NotImplementedException();
	}

	@Override
	protected void handleOntologyChanges(List<OWLOntologyChange> changes)
			throws OWLException {
		OWL2RLDBRemover axiomRemover = new OWL2RLDBRemover(this);
		OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder(this);
		for (OWLOntologyChange change : changes) {
			if (change.isImportChange()) {
				throw new U2R3NotImplementedException();
			}
			if (change instanceof AddAxiom) {
				change.getAxiom().accept(axiomAdder);
			} else if (change instanceof RemoveAxiom) {
				change.getAxiom().accept(axiomRemover);
			}
		}
	}

	@Override
	protected void ontologiesChanged() throws OWLReasonerException {
		//add the axioms
		for(OWLOntology ont : getLoadedOntologies()) {
			//check if current ontologies are conform
			if (settings.checkProfile()) {
				OWL2RLProfile profile = new OWL2RLProfile();
				OWLProfileReport report = profile.checkOntology(ont);
				
				if (!report.isInProfile()) {
					for (OWLProfileViolation violation : report.getViolations()) {
						System.out.println(violation);
					}
					
					throw new U2R3NotInProfileException("OWL file is not in RL Profile!");
				}
			}

			OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder(this);
			for(OWLAxiom ax : ont.getLogicalAxioms()) {
				ax.accept(axiomAdder);
			}
			
		}

	}

	@Override
	protected void ontologiesCleared() throws OWLReasonerException {
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean isConsistent(OWLOntology arg0) throws OWLReasonerException {
		return reasonProcessor.isConsistent();
	}

	@Override
	public void classify() throws OWLReasonerException {
		reasonProcessor.classify();
		isClassified = true;
	}

	@Override
	public boolean isClassified() throws OWLReasonerException {
		return isClassified;
	}

	@Override
	public boolean isDefined(OWLClass arg0) throws OWLReasonerException {
		String clazz = arg0.getIRI().toString();
		String type = OWLRDFVocabulary.OWL_CLASS.getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionEnt).exists(clazz, type);
	}
	
	/**
	 * Determines if the specified entity is defined in the reasoner. If a entity is defined then the 
 	 * reasoner "knows" about it, if a entity is not defined then the reasoner doesn't know about it.
	 * @param entity
	 * @return  true if the entity is defined in the reasoner, or false if the entity is not defined in
	 * the reasoner.
	 * @throws OWLReasonerException
	 */
	public boolean isDefined(OWLEntity entity) throws OWLReasonerException {
		String clazz = entity.getIRI().toString();
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
	}

	@Override
	public boolean isDefined(OWLObjectProperty arg0)
			throws OWLReasonerException {
		String clazz = arg0.getIRI().toString();
		String type = OWLRDFVocabulary.OWL_OBJECT_PROPERTY.getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionEnt).exists(clazz, type);
	}

	@Override
	public boolean isDefined(OWLDataProperty arg0) throws OWLReasonerException {
		String clazz = arg0.getIRI().toString();
		String type = OWLRDFVocabulary.OWL_DATA_PROPERTY.getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionEnt).exists(clazz, type);
	}

	@Override
	public boolean isDefined(OWLIndividual arg0) throws OWLReasonerException {
		String subject = arg0.asNamedIndividual().getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionEnt).exists(subject);
	}

	@Override
	public boolean isRealised() throws OWLReasonerException {
		return isClassified;
	}

	@Override
	public void realise() throws OWLReasonerException {
		classify();
	}

	@Override
	public Set<Set<OWLClass>> getAncestorClasses(OWLClassExpression arg0)
			throws OWLReasonerException {
		return getSuperClasses(arg0);
	}

	@Override
	public Set<Set<OWLClass>> getDescendantClasses(OWLClassExpression arg0)
			throws OWLReasonerException {
		return getSubClasses(arg0);
	}

	@Override
	public Set<OWLClass> getEquivalentClasses(OWLClassExpression ce)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLClass>> getSubClasses(OWLClassExpression arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLClass>> getSuperClasses(OWLClassExpression arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClass> getUnsatisfiableClasses() throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalentClass(OWLClassExpression ce1,
			OWLClassExpression ce2) throws OWLReasonerException {
		if (!(ce1.isAnonymous() || ce2.isAnonymous())) {
			return relationManager.getRelation(RelationName.equivalentClass).exists(ce1.asOWLClass().getIRI().toString(), ce2.asOWLClass().getIRI().toString());
		}
		throw new U2R3NotImplementedException();
	}

	@Override
	public boolean isSubClassOf(OWLClassExpression sub, OWLClassExpression sup)
			throws OWLReasonerException {
		if (!(sup.isAnonymous() || sub.isAnonymous())) {
			return relationManager.getRelation(RelationName.subClass).exists(sub.asOWLClass().getIRI().toString(), sup.asOWLClass().getIRI().toString());
		}
		throw new U2R3NotImplementedException();

	}

	@Override
	public boolean isSatisfiable(OWLClassExpression arg0)
			throws OWLReasonerException {
		throw new U2R3NotImplementedException();
	}

	@Override
	public Map<OWLDataProperty, Set<OWLLiteral>> getDataPropertyRelationships(
			OWLNamedIndividual arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLNamedIndividual> getIndividuals(OWLClassExpression clazz,	boolean arg1) throws OWLReasonerException {
		if (clazz.isAnonymous()) {
			return null;
		} else {
			ClassAssertionEntRelation ca = (ClassAssertionEntRelation) relationManager.getRelation(RelationName.classAssertionEnt);
			return ca.getIndividuals(clazz.asOWLClass());
		}
		
	}

	@Override
	public Map<OWLObjectProperty, Set<OWLNamedIndividual>> getObjectPropertyRelationships(
			OWLNamedIndividual arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLNamedIndividual> getRelatedIndividuals(
			OWLNamedIndividual ni, OWLObjectPropertyExpression op)
			throws OWLReasonerException {
		if (ni.isAnonymous() || op.isAnonymous()) {
			return null;
		} else {
			ObjectPropertyAssertionRelation opa = (ObjectPropertyAssertionRelation) relationManager.getRelation(RelationName.objectPropertyAssertion);
			return opa.RelatedIndividuals(ni, op.asOWLObjectProperty());
		}
	}

	@Override
	public Set<OWLLiteral> getRelatedValues(OWLNamedIndividual ni,
			OWLDataPropertyExpression dp) throws OWLReasonerException {
		if (ni.isAnonymous() ||dp.isAnonymous()) {
			return null;
		} else {
			DataPropertyAssertionRelation dpa = (DataPropertyAssertionRelation) relationManager.getRelation(RelationName.dataPropertyAssertion);
			return dpa.getRelatedValues(ni, dp.asOWLDataProperty());
		}
	}

	@Override
	public Set<Set<OWLClass>> getTypes(OWLNamedIndividual namedIndividual, boolean arg1)
			throws OWLReasonerException {
		ClassAssertionEntRelation ca = (ClassAssertionEntRelation) relationManager.getRelation(RelationName.classAssertionEnt);
		return ca.getTypes(namedIndividual);
	}

	@Override
	public boolean hasDataPropertyRelationship(OWLNamedIndividual arg0,
			OWLDataPropertyExpression arg1, OWLLiteral arg2)
			throws OWLReasonerException {
		String subject = arg0.getIRI().toString();
		String property = arg1.asOWLDataProperty().getIRI().toString();
		String object = arg2.getLiteral();
		if (!arg2.isTyped()) {
			String lang = arg2.asRDFTextLiteral().getLang();
			return relationManager.getRelation(RelationName.dataPropertyAssertion).exists(subject, property, object, lang);
		}
		return relationManager.getRelation(RelationName.dataPropertyAssertion).exists(subject, property, object);
	}

	@Override
	public boolean hasObjectPropertyRelationship(OWLNamedIndividual arg0,
			OWLObjectPropertyExpression arg1, OWLNamedIndividual arg2)
			throws OWLReasonerException {
		String subject = arg0.getIRI().toString();
		String property = arg1.asOWLObjectProperty().getIRI().toString();
		String object = arg2.getIRI().toString();
		return relationManager.getRelation(RelationName.objectPropertyAssertion).exists(subject, property, object);
	}

	@Override
	public boolean hasType(OWLNamedIndividual arg0, OWLClassExpression arg1,
			boolean arg2) throws OWLReasonerException {
		String clazz = arg0.getIRI().toString();
		String type = arg1.asOWLClass().getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionEnt).exists(clazz, type);
	}
	
	
	public boolean hasType(OWLLiteral arg0, OWLDatatype arg1) throws OWLReasonerException {
		String literal = arg0.getLiteral();
		String clazz = arg1.getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertionLit).exists(literal, clazz);
	}

	@Override
	public Set<Set<OWLObjectProperty>> getAncestorProperties(
			OWLObjectProperty arg0) throws OWLReasonerException {
		return getSuperProperties(arg0);
	}

	@Override
	public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty arg0)
			throws OWLReasonerException {
		return getSuperProperties(arg0);
	}

	@Override
	public Set<Set<OWLObjectProperty>> getDescendantProperties(
			OWLObjectProperty arg0) throws OWLReasonerException {
		return getSubProperties(arg0);
	}

	@Override
	public Set<Set<OWLDataProperty>> getDescendantProperties(
			OWLDataProperty arg0) throws OWLReasonerException {
		return getSubProperties(arg0);
	}

	@Override
	public Set<Set<OWLClassExpression>> getDomains(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLClassExpression>> getDomains(OWLDataProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLObjectProperty> getEquivalentProperties(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLDataProperty> getEquivalentProperties(OWLDataProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLObjectProperty>> getInverseProperties(
			OWLObjectProperty arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClassExpression> getRanges(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLDataRange> getRanges(OWLDataProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLObjectProperty>> getSubProperties(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLDataProperty>> getSubProperties(OWLDataProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLObjectProperty>> getSuperProperties(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLDataProperty>> getSuperProperties(OWLDataProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFunctional(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFunctional(OWLDataProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInverseFunctional(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIrreflexive(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReflexive(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSymmetric(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isAsymmetric(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTransitive(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
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
			.exists(ind.asNamedIndividual().getIRI().toString());
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


}
