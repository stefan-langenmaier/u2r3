package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRefOEntRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRefOEntRule.class);
	
	EqRefOEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsEnt;
		
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
		relationManager.getRelation(RelationName.sameAsEnt).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT object AS left, object AS right,");
			sql.append(" MIN(id) AS sourceId1, '" + RelationName.objectPropertyAssertion + "' AS sourceTable1");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT object AS left, object AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT left, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = top.object AND bottom.right = top.object");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY object");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsEnt(A,A) :- objectPropertyAssertion(S, P, A)";
	}

}
