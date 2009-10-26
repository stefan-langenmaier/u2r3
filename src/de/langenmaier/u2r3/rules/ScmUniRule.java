package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmUniRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmUniRule.class);
	
	ScmUniRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.unionOf).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());

		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, sourceId1, sourceTable1");
			sql.append("\n\t SELECT l.element, uni.class,");
			sql.append(" MIN(uni.id) AS sourceId1, '" + RelationName.unionOf + "' AS sourceTable1");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT l.element, uni.class");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("unionOf") + " AS uni");
		sql.append("\n\t\t INNER JOIN list AS l ON uni.list = l.name");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.sub, bottom.super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = l.element AND bottom.super = uni.class");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY l.element, uni.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C1..Cn, C) :- unionOf(C, X), list(X, C1..Cn)";
	}

}
