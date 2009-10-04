package de.langenmaier.u2r3.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.util.Settings.DeletionType;

/**
 * This class manages the access to and the creation of Relation objects.
 * @author stefan
 *
 */
public class RelationManager {
	public enum RelationName {classAssertion, subClass, propertyDomain,
		propertyRange, propertyAssertion, sameAs, list, intersectionOf,
		equivalentClass, differentFrom, members, distinctMembers, subProperty,
		propertyChain, equivalentProperty, propertyDisjointWith, inverseOf,
		hasKey};

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
		
		relations.put(RelationName.subClass, new SubClassRelation(reasoner));
		relations.put(RelationName.classAssertion, new ClassAssertionRelation(reasoner));
		relations.put(RelationName.sameAs, new SameAsRelation(reasoner));
		relations.put(RelationName.list, new ListRelation(reasoner));
		relations.put(RelationName.intersectionOf, new IntersectionOfRelation(reasoner));
		relations.put(RelationName.equivalentClass, new EquivalentClassRelation(reasoner));
		relations.put(RelationName.propertyAssertion, new PropertyAssertionRelation(reasoner));
		relations.put(RelationName.propertyDomain, new PropertyDomainRelation(reasoner));
		relations.put(RelationName.propertyRange, new PropertyRangeRelation(reasoner));
		relations.put(RelationName.differentFrom, new DifferentFromRelation(reasoner));
		relations.put(RelationName.members, new MembersRelation(reasoner));
		relations.put(RelationName.distinctMembers, new DistinctMembersRelation(reasoner));
		relations.put(RelationName.subProperty, new SubPropertyRelation(reasoner));
		relations.put(RelationName.propertyChain, new PropertyChainRelation(reasoner));
		relations.put(RelationName.equivalentProperty, new EquivalentPropertyRelation(reasoner));
		relations.put(RelationName.propertyDisjointWith, new PropertyDisjointWithRelation(reasoner));
		relations.put(RelationName.inverseOf, new InverseOfRelation(reasoner));
		relations.put(RelationName.hasKey, new HasKeyRelation(reasoner));
	}

	public Collection<Relation> getRelations() {
		return relations.values();
	}

	public void addHistory(String sql) {
		history.add(sql);
		
	}

	public void remove(UUID id, RelationName name) {
		if (reasoner.getSettings().getDeletionType() == DeletionType.CASCADING) {
			history.remove(id, name);
		} //else if (Settings.getDeletionType() == DeletionType.CLEAN) {
		//	Settings.startClean(true);
		//}
		
	}

}
