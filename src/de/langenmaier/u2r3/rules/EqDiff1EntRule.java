package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class EqDiff1EntRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(EqDiff1EntRule.class);
	
	EqDiff1EntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.sameAsEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.differentFromEnt).addAdditionRule(this);
		
		//add deletion rule
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("SELECT '1' AS res");
		sql.append("\n FROM " + delta.getDeltaName("sameAsEnt") + " AS sa");
		sql.append("\n\t INNER JOIN " + delta.getDeltaName("differentFromEnt") + " AS df");
		sql.append("\n\t\t ON sa.colLeft=df.colLeft AND sa.colRight = df.colRight");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- sameAsEnt(X, Y), differentFromLit(X, Y)";
	}

}
