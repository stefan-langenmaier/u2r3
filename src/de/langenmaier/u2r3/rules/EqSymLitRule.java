package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqSymLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqSymLitRule.class);
	
	EqSymLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsLit;
		
		//relations on the right side
		relationManager.getRelation(RelationName.sameAsLit).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (colLeft, colRight, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT colRight, colLeft, id AS sourceId1, '" + RelationName.sameAsLit + "' AS sourceTable1");
		} else {
			sql.append("(colLeft, colRight)");
			sql.append("\n\t SELECT colRight, colLeft");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("sameAsLit") + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT colRight, colLeft");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.colLeft = top.colLeft AND bottom.colRight = top.colRight");
			sql.append("\n\t )");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsLit(B,A) :- sameAsLit(A,B)";
	}

}
