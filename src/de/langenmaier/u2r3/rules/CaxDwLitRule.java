package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class CaxDwLitRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(CaxDwLitRule.class);
	
	CaxDwLitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.disjointWith).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		return applyCollectiveTwice(delta, aux);
	}

	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		return applyImmediateTwice(delta, newDelta);
	}


	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
	 	
		sql.append("SELECT '1' AS res");
		sql.append("\nFROM " + delta.getDeltaName("disjointWith") + " AS dw");
		if (run == 0) {
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionLit") + " AS ca1 ON dw.colLeft = ca1.class");
			sql.append("\n\t INNER JOIN classAssertionLit AS ca2 ON dw.colRight = ca2.class");
		} else if (run == 1) {
			sql.append("\n\t INNER JOIN classAssertionLit AS ca1 ON dw.colLeft = ca1.class");
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionLit") + " AS ca2 ON dw.colRight = ca2.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- disjointWith(C1, C2), classAssertionLit(X, C1), classAssertionLit(X, C2)";
	}

}
