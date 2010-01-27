package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRefOLitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRefOLitRule.class);
	
	EqRefOLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsLit;
		
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		
		relationManager.getRelation(RelationName.sameAsLit).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (colLeft, right, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT object AS colLeft, object AS right,");
			sql.append(" MIN(id) AS sourceId1, '" + RelationName.objectPropertyAssertion + "' AS sourceTable1");
		} else {
			sql.append("(colLeft, right)");
			sql.append("\n\t SELECT DISTINCT object AS colLeft, object AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("dataPropertyAssertion") + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT colLeft, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.colLeft = top.object AND bottom.right = top.object");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY object");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsLitt(A,A) :- dataPropertyAssertion(S, P, A)";
	}

}
