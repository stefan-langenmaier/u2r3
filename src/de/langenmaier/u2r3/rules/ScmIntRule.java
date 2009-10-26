package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ScmIntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ScmIntRule.class);
	
	ScmIntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.subClass;
		
		relationManager.getRelation(RelationName.intersectionOf).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());

		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (sub, super, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT is.class, l.element,");
			sql.append(" MIN(is.id) AS sourceId1, '" + RelationName.intersectionOf + "' AS sourceTable1");
		} else {
			sql.append(" (sub, super)");
			sql.append("\n\t SELECT DISTINCT is.class, l.element");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("intersectionOf") + " AS is");
		sql.append("\n\t\t INNER JOIN list AS l ON is.list = l.name");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.sub, bottom.super");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.sub = is.class AND bottom.super = l.element");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY is.class, l.element");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "subClass(C, C1..Cn) :- intersectionOf(C, X), list(X, C1..Cn)";
	}

}
