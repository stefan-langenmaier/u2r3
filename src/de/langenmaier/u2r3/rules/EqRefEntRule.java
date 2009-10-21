package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRefEntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRefEntRule.class);
	
	EqRefEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsEnt;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		
		relationManager.getRelation(RelationName.sameAsEnt).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT entity AS left, class AS right, MIN(id) AS sourceId1, '" + RelationName.classAssertionEnt + "' AS sourceTable1, MIN(id) AS sourceId2, '" + RelationName.classAssertionEnt + "' AS sourceTable2");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT entity AS left, class AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT left, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = top.class AND bottom.right = top.class");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY left, right");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsEnt(A,A) :- classAssertionEnt(A, X)";
	}

}
