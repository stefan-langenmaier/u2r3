package de.langenmaier.u2r3.db;

import java.util.HashMap;

import de.langenmaier.u2r3.rules.RuleManager;

/**
 * This class manages the access to and the creation of Relation objects.
 * @author stefan
 *
 */
public class RelationMananger {
	public enum RelationName {subClass};
	private static boolean isInitialized = false;
	private static HashMap<RelationName, Relation> relations = new HashMap<RelationName, Relation>();
	

	public static Relation getRelation(RelationName name) {
		if (! isInitialized) {
			initialize();
		}
		return relations.get(name);
	}


	private static void initialize() {
		Relation newRelation;
		
		newRelation = new SubClassRelation();
		relations.put(RelationName.subClass, newRelation);
		newRelation.addRules(RuleManager.getRule(RuleManager.RuleName.transSubClass));
		
		isInitialized = true;
	}



}
