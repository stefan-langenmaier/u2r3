package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class EqDiff1Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(EqDiff1Rule.class);
	
	EqDiff1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.sameAs).addAdditionRule(this);
		relationManager.getRelation(RelationName.differentFrom).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		if (delta.getRelation() == relationManager.getRelation(RelationName.sameAs)) {
			sql.append("\n FROM " + delta.getDeltaName() + " AS sa INNER JOIN differentFrom AS df ON sa.left=df.left AND sa.right = df.right");
		} else if (delta.getRelation() == relationManager.getRelation(RelationName.differentFrom)) {
			sql.append("\n FROM sameAs AS sa INNER JOIN " + delta.getDeltaName() + " AS df ON sa.left=df.left AND sa.right = df.right");
		}		

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertion(A, nothing)";
	}

}
