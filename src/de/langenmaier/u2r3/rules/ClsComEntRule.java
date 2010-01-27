package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;


public class ClsComEntRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsComEntRule.class);
	
	ClsComEntRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.complementOf).addAdditionRule(this);
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

		sql.append("SELECT 1 AS res");
		sql.append("\nFROM " + delta.getDeltaName("complementOf") + " AS co");
		if (run == 0) {
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca1 ON co.colLeft = ca1.colClass");
			sql.append("\n\t INNER JOIN classAssertionEnt AS ca2 ON co.colRight = ca2.colClass");
		} else if (run == 1) {
			sql.append("\n\t INNER JOIN classAssertionEnt AS ca1 ON co.colLeft = ca1.colClass");
			sql.append("\n\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca2 ON co.colRight = ca2.colClass");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- complementOf(C1, C2), classAssertionEnt(X, C1), classAssertionEnt(X, C2)";
	}

}
