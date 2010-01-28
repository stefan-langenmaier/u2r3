package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ImplObjIntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ImplObjIntRule.class);
	
	ImplObjIntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		//relations on the right side
		relationManager.getRelation(RelationName.intersectionOf).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		String clazz = OWLRDFVocabulary.OWL_CLASS.getIRI().toString();
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, colClass, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT io.colClass, '" +  clazz + "', ");
			sql.append(" MIN(io.id) AS sourceId1, '" + RelationName.intersectionOf + "' AS sourceTable1");
		} else {
			sql.append("(entity, colClass)");
			sql.append("\n\t SELECT DISTINCT io.colClass, '" +  clazz + "'");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("intersectionOf") + " AS io");


		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT entity, colClass");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = io.colClass AND bottom.colClass = '" +  clazz + "'");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY io.colClass");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(X, Class) :- intersectionOf(X, L)";
	}

}
