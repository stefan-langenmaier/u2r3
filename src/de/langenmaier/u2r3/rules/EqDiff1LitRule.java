package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class EqDiff1LitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(EqDiff1LitRule.class);
	
	EqDiff1LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.sameAsLit).addAdditionRule(this);
		relationManager.getRelation(RelationName.differentFromLit).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		sql.append("\n FROM " + delta.getDeltaName("sameAsLit") + " AS sa");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("differentFromLit") + " AS df");
		sql.append("\n\t\t ON sa.colLeft=df.colLeft AND sa.right = df.right");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- sameAsLit(X, Y), differentFromLit(X, Y)";
	}

}
