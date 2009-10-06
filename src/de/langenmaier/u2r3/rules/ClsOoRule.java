package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsOoRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsOoRule.class);
	
	ClsOoRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		relationManager.getRelation(RelationName.oneOf).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());

		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (class, type, classSourceId, classSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT l.element AS class, oo.class AS type, MIN(l.id) AS classSourceId, '" + RelationName.list + "' AS classSourceTable, MIN(oo.id) AS typeSourceId, '" + RelationName.oneOf + "' AS typeSourceTable");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT l.element AS class, oo.class AS type");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("oneOf") + " AS oo");
		sql.append("\n\t\t INNER JOIN list AS l ON oo.list = l.name");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.class");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = l.element AND bottom.type = oo.class");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY l.element, oo.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(Y1..Yn, C) :- oneOf(X, C), list(X, Y1..Yn)";
	}

}
