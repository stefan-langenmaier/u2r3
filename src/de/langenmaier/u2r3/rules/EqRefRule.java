package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class EqRefRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(EqRefRule.class);
	
	EqRefRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAs;
		
		relationManager.getRelation(RelationName.declaration).addAdditionRule(this);
		
		relationManager.getRelation(RelationName.sameAs).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT subject AS left, subject AS right, MIN(id) AS leftSourceId, '" + RelationName.declaration.toString() + "' AS leftSourceTable, MIN(id) AS rightSourceId, '" + RelationName.declaration.toString() + "' AS rightSourceTable");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT subject AS left, subject AS right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName() + " AS top");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT left, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = top.subject AND bottom.right = top.subject");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY left, right");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAs(A,A) :- declaration(A, X)";
	}

}
