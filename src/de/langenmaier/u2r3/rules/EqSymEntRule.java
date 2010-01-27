package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqSymEntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqSymEntRule.class);
	
	EqSymEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsEnt;
		
		//relations on the right side
		relationManager.getRelation(RelationName.sameAsEnt).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (colLeft, right, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT right, colLeft, id AS sourceId1, '" + RelationName.sameAsEnt + "' AS sourceTable1");
		} else {
			sql.append("(colLeft, right)");
			sql.append("\n\t SELECT right, colLeft");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("sameAsEnt") + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT right, colLeft");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.colLeft = top.colLeft AND bottom.right = top.right");
			sql.append("\n\t )");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsEnt(B,A) :- sameAsEnt(A,B)";
	}

}
