package de.langenmaier.u2r3.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import de.langenmaier.u2r3.util.Settings;
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
	private static boolean isInitialized = false;
	private static HashMap<RelationName, Relation> relations = new HashMap<RelationName, Relation>();
	private static History history = null;
	

	public synchronized static Relation getRelation(RelationName name) {
		if (! isInitialized) {
			initialize();
		}
		return relations.get(name);
	}
	
	//There should be no objects of this class
	private RelationManager() {}


	private static void initialize() {
		history = new History();
		
		relations.put(RelationName.subClass, new SubClassRelation());
		relations.put(RelationName.classAssertion, new ClassAssertionRelation());
		relations.put(RelationName.dataPropertyDomain, new DataPropertyDomainRelation());
		relations.put(RelationName.dataPropertyRange, new DataPropertyRangeRelation());
		relations.put(RelationName.objectPropertyAssertion, new ObjectPropertyAssertionRelation());
		relations.put(RelationName.objectPropertyDomain, new ObjectPropertyDomainRelation());
		relations.put(RelationName.objectPropertyRange, new ObjectPropertyRangeRelation());
		relations.put(RelationName.declaration, new DeclarationRelation());
		relations.put(RelationName.sameAs, new SameAsRelation());
		relations.put(RelationName.dataPropertyAssertion, new DataPropertyAssertionRelation());
		relations.put(RelationName.list, new ListRelation());
		relations.put(RelationName.intersectionOf, new IntersectionOfRelation());
		relations.put(RelationName.equivalentClass, new EquivalentClassRelation());
		
		isInitialized = true;
	}

	public static Collection<Relation> getRelations() {
		return relations.values();
	}

	public static void addHistory(String sql) {
		history.add(sql);
		
	}

	public static void remove(UUID id, RelationName name) {
		if (Settings.getDeletionType() == DeletionType.CASCADING) {
			history.remove(id, name);
		} //else if (Settings.getDeletionType() == DeletionType.CLEAN) {
		//	Settings.startClean(true);
		//}
		
	}

}
