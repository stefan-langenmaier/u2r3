package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class CaxScoRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(CaxScoRule.class);
	
	CaxScoRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.subClass).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (class, type, classSourceId, classSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT clsA.class, sc.super, MIN(clsA.id) AS classSourceId, '" + RelationName.classAssertion.toString() + "' AS classSourceTable, MIN(sc.id) AS typeSourceId, '" + RelationName.subClass.toString() + "' AS typeSourceTable");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT clsA.class, sc.super");
		}
		
		if (delta.getDelta() == DeltaRelation.NO_DELTA) {
			sql.append("\n\t FROM classAssertion AS clsA INNER JOIN subClass AS sc ON clsA.type = sc.sub");
		} else {
			if (delta.getRelation() == relationManager.getRelation(RelationName.classAssertion)) {
				sql.append("\n\t FROM " + delta.getDeltaName() + " AS clsA INNER JOIN subClass AS sc ON clsA.type = sc.sub");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.subClass)) {
				sql.append("\n\t FROM classAssertion AS clsA INNER JOIN " + delta.getDeltaName() + " AS sc ON clsA.type = sc.sub");
			}
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT class, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = clsA.class AND bottom.type = sc.super");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.class, sc.super");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(X,C2) :- classAssertion(X, C1), subClass(C1, C2)";
	}

}
