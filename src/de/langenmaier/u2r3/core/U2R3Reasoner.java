package de.langenmaier.u2r3.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.inference.OWLReasonerAdapter;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
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

import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotInProfileException;
import de.langenmaier.u2r3.owl.OWL2RLDBAdder;
import de.langenmaier.u2r3.rules.RuleManager;
import de.langenmaier.u2r3.util.Settings;

public class U2R3Reasoner extends OWLReasonerAdapter {
	private RuleManager ruleManager;
	private RelationManager relationManager;
	private ReasonProcessor reasonProcessor;
	private Settings settings;
	
	private boolean isClassified = false;

	protected U2R3Reasoner(OWLOntologyManager manager) throws OWLReasonerException {
		super(manager);
		
		ruleManager = new RuleManager(this);
		relationManager = new RelationManager(this);
		settings = new Settings();
		reasonProcessor = new ReasonProcessor(this);
		
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
		// TODO Auto-generated method stub

	}

	@Override
	protected void handleOntologyChanges(List<OWLOntologyChange> arg0)
			throws OWLException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void ontologiesChanged() throws OWLReasonerException {
		//check if current ontologies are conform and add the axioms
		for(OWLOntology ont : getLoadedOntologies()) {
			if (settings.checkProfile()) {
				OWL2RLProfile profile = new OWL2RLProfile();
				OWLProfileReport report = profile.checkOntology(ont, getOWLOntologyManager());
				
				if (!report.isInProfile()) { throw new U2R3NotInProfileException("OWL file is not in RL Profile!"); }
			}
			
			OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder(this);
			for(OWLAxiom ax : ont.getLogicalAxioms()) {
				ax.accept(axiomAdder);
			}
			
		}

	}

	@Override
	protected void ontologiesCleared() throws OWLReasonerException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConsistent(OWLOntology arg0) throws OWLReasonerException {
		//System.out.println(reasonProcessor.isConsistent());
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefined(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefined(OWLDataProperty arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefined(OWLIndividual arg0) throws OWLReasonerException {
		String subject = arg0.asNamedIndividual().getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertion).exists(subject);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLClass>> getDescendantClasses(OWLClassExpression arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClass> getEquivalentClasses(OWLClassExpression arg0)
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
	public boolean isEquivalentClass(OWLClassExpression arg0,
			OWLClassExpression arg1) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSubClassOf(OWLClassExpression arg0, OWLClassExpression arg1)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfiable(OWLClassExpression arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<OWLDataProperty, Set<OWLLiteral>> getDataPropertyRelationships(
			OWLNamedIndividual arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLNamedIndividual> getIndividuals(OWLClassExpression arg0,
			boolean arg1) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<OWLObjectProperty, Set<OWLNamedIndividual>> getObjectPropertyRelationships(
			OWLNamedIndividual arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLNamedIndividual> getRelatedIndividuals(
			OWLNamedIndividual arg0, OWLObjectPropertyExpression arg1)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLLiteral> getRelatedValues(OWLNamedIndividual arg0,
			OWLDataPropertyExpression arg1) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLClass>> getTypes(OWLNamedIndividual arg0, boolean arg1)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasDataPropertyRelationship(OWLNamedIndividual arg0,
			OWLDataPropertyExpression arg1, OWLLiteral arg2)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasObjectPropertyRelationship(OWLNamedIndividual arg0,
			OWLObjectPropertyExpression arg1, OWLNamedIndividual arg2)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasType(OWLNamedIndividual arg0, OWLClassExpression arg1,
			boolean arg2) throws OWLReasonerException {
		String clazz = arg0.getIRI().toString();
		String type = arg1.asOWLClass().getIRI().toString();
		return relationManager.getRelation(RelationName.classAssertion).exists(clazz, type);
	}

	@Override
	public Set<Set<OWLObjectProperty>> getAncestorProperties(
			OWLObjectProperty arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLDataProperty>> getAncestorProperties(OWLDataProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLObjectProperty>> getDescendantProperties(
			OWLObjectProperty arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Set<OWLDataProperty>> getDescendantProperties(
			OWLDataProperty arg0) throws OWLReasonerException {
		// TODO Auto-generated method stub
		return null;
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
	public boolean isAntiSymmetric(OWLObjectProperty arg0)
			throws OWLReasonerException {
		// TODO Auto-generated method stub
		return false;
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

}
