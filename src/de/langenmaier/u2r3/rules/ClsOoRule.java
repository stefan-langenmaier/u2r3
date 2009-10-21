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
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.oneOf).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());

		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, class, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT l.element AS entity, oo.class AS class, ");
			sql.append(" MIN(oo.id) AS sourceId1, '" + RelationName.oneOf + "' AS sourceTable1");
		} else {
			sql.append(" (entity, class)");
			sql.append("\n\t SELECT DISTINCT l.element AS entity, oo.class AS class");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("oneOf") + " AS oo");
		sql.append("\n\t\t INNER JOIN list AS l ON oo.list = l.name");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.entity");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = l.element AND bottom.class = oo.class");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY l.element, oo.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(Y1..Yn, C) :- oneOf(X, C), list(X, Y1..Yn)";
	}

}
