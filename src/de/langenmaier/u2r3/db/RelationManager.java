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
	public enum RelationName {classAssertion, dataPropertyDomain, dataPropertyRange,
		objectPropertyAssertion, objectPropertyDomain, objectPropertyRange, subClass,
		declaration, sameAs, dataPropertyAssertion, list, intersectionOf,
		equivalentClass};
	//private static boolean isInitialized = false;
	private HashMap<RelationName, Relation> relations = new HashMap<RelationName, Relation>();
	private History history = null;
	private U2R3Reasoner reasoner;

	public synchronized Relation getRelation(RelationName name) {
		/*if (! isInitialized) {
			initialize();
		}*/
		return relations.get(name);
	}
	
	public RelationManager(U2R3Reasoner reasoner) {
		this.reasoner = reasoner;
		//initialize(reasoner);
	}


	public void initialize() {
		history = new History(reasoner);
		
		relations.put(RelationName.subClass, new SubClassRelation(reasoner));
		relations.put(RelationName.classAssertion, new ClassAssertionRelation(reasoner));
		relations.put(RelationName.dataPropertyDomain, new DataPropertyDomainRelation(reasoner));
		relations.put(RelationName.dataPropertyRange, new DataPropertyRangeRelation(reasoner));
		relations.put(RelationName.objectPropertyAssertion, new ObjectPropertyAssertionRelation(reasoner));
		relations.put(RelationName.objectPropertyDomain, new ObjectPropertyDomainRelation(reasoner));
		relations.put(RelationName.objectPropertyRange, new ObjectPropertyRangeRelation(reasoner));
		relations.put(RelationName.declaration, new DeclarationRelation(reasoner));
		relations.put(RelationName.sameAs, new SameAsRelation(reasoner));
		relations.put(RelationName.dataPropertyAssertion, new DataPropertyAssertionRelation(reasoner));
		relations.put(RelationName.list, new ListRelation(reasoner));
		relations.put(RelationName.intersectionOf, new IntersectionOfRelation(reasoner));
		relations.put(RelationName.equivalentClass, new EquivalentClassRelation(reasoner));
		
		//isInitialized = true;
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
