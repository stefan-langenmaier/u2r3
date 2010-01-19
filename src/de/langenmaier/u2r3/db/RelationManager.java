package de.langenmaier.u2r3.db;

import java.util.Collection;
import java.util.HashMap;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.util.Settings.DeletionType;

/**
 * This class manages the access to and the creation of Relation objects.
 * @author stefan
 *
 */
public class RelationManager {
	public enum RelationName {classAssertionEnt, classAssertionLit, subClass,
		propertyDomain, sameAsLit, dataPropertyAssertion, hasValueLit,
		propertyRange, objectPropertyAssertion, sameAsEnt, list, intersectionOf,
		equivalentClass, differentFromEnt, subProperty,
		propertyChain, equivalentProperty, propertyDisjointWith, inverseOf,
		hasKey,  unionOf, complementOf, someValuesFrom, 
		allValuesFrom, hasValueEnt, maxCardinality, maxQualifiedCardinality,
		members, 
		//sourceIndividual, targetIndividual, assertionProperty, targetValue,
		//onClass, onProperty, distinctMembers,
		oneOf, disjointWith, differentFromLit,
		negativeObjectPropertyAssertion, negativeDataPropertyAssertion};

	private HashMap<RelationName, Relation> relations = new HashMap<RelationName, Relation>();
	private History history = null;
	
	private U2R3Reasoner reasoner;

	public synchronized Relation getRelation(RelationName name) {
		return relations.get(name);
	}
	
	public RelationManager(U2R3Reasoner reasoner) {
		this.reasoner = reasoner;
	}


	public void initialize() {
		history = new History(reasoner);
		DBSetup.setup();
		
		relations.put(RelationName.list, new ListRelation(reasoner));
		
		relations.put(RelationName.subClass, new SubClassRelation(reasoner));
		relations.put(RelationName.classAssertionEnt, new ClassAssertionEntRelation(reasoner));
		relations.put(RelationName.classAssertionLit, new ClassAssertionLitRelation(reasoner));		
		relations.put(RelationName.sameAsEnt, new SameAsEntRelation(reasoner));
		relations.put(RelationName.sameAsLit, new SameAsLitRelation(reasoner));
		relations.put(RelationName.intersectionOf, new IntersectionOfRelation(reasoner));
		relations.put(RelationName.equivalentClass, new EquivalentClassRelation(reasoner));
		relations.put(RelationName.objectPropertyAssertion, new ObjectPropertyAssertionRelation(reasoner));
		relations.put(RelationName.dataPropertyAssertion, new DataPropertyAssertionRelation(reasoner));
		relations.put(RelationName.propertyDomain, new PropertyDomainRelation(reasoner));
		relations.put(RelationName.propertyRange, new PropertyRangeRelation(reasoner));
		relations.put(RelationName.differentFromEnt, new DifferentFromEntRelation(reasoner));
		relations.put(RelationName.differentFromLit, new DifferentFromLitRelation(reasoner));
		relations.put(RelationName.members, new MembersRelation(reasoner));
		//relations.put(RelationName.distinctMembers, new DistinctMembersRelation(reasoner));
		relations.put(RelationName.subProperty, new SubPropertyRelation(reasoner));
		relations.put(RelationName.propertyChain, new PropertyChainRelation(reasoner));
		relations.put(RelationName.equivalentProperty, new EquivalentPropertyRelation(reasoner));
		relations.put(RelationName.propertyDisjointWith, new PropertyDisjointWithRelation(reasoner));
		relations.put(RelationName.inverseOf, new InverseOfRelation(reasoner));
		relations.put(RelationName.hasKey, new HasKeyRelation(reasoner));
		//relations.put(RelationName.sourceIndividual, new SourceIndividualRelation(reasoner));
		//relations.put(RelationName.targetIndividual, new TargetIndividualRelation(reasoner));
		//relations.put(RelationName.targetValue, new TargetValueRelation(reasoner));
		//relations.put(RelationName.assertionProperty, new AssertionPropertyRelation(reasoner));
		relations.put(RelationName.negativeDataPropertyAssertion, new NegativeDataPropertyAssertionRelation(reasoner));
		relations.put(RelationName.negativeObjectPropertyAssertion, new NegativeObjectPropertyAssertionRelation(reasoner));
		relations.put(RelationName.unionOf, new UnionOfRelation(reasoner));
		relations.put(RelationName.complementOf, new ComplementOfRelation(reasoner));
		relations.put(RelationName.someValuesFrom, new SomeValuesFromRelation(reasoner));
		//relations.put(RelationName.onProperty, new OnPropertyRelation(reasoner));
		relations.put(RelationName.allValuesFrom, new AllValuesFromRelation(reasoner));
		relations.put(RelationName.hasValueEnt, new HasValueEntRelation(reasoner));
		relations.put(RelationName.hasValueLit, new HasValueLitRelation(reasoner));
		relations.put(RelationName.maxCardinality, new MaxCardinalityRelation(reasoner));
		relations.put(RelationName.maxQualifiedCardinality, new MaxQualifiedCardinalityRelation(reasoner));
		//relations.put(RelationName.onClass, new OnClassRelation(reasoner));
		relations.put(RelationName.oneOf, new OneOfRelation(reasoner));
		relations.put(RelationName.disjointWith, new DisjointWithRelation(reasoner));
		
	}

	public Collection<Relation> getRelations() {
		return relations.values();
	}

	public void addHistory(String sql) {
		history.add(sql);
		
	}

	public void remove(Long id, RelationName name) {
		if (reasoner.getSettings().getDeletionType() == DeletionType.CASCADING) {
			history.remove(id, name);
		} //else if (Settings.getDeletionType() == DeletionType.CLEAN) {
		//	Settings.startClean(true);
		//}
		
	}

}
