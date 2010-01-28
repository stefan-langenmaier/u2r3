package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ImplLitHvRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ImplSvfRule.class);
	
	ImplLitHvRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionLit;
		
		//relations on the right side
		relationManager.getRelation(RelationName.hasValueLit).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (literal, colClass, language, sourceId1, sourceTable1)");
			sql.append("\n\t SELECT hv.value, hv.type, hv.language, ");
			sql.append(" MIN(hv.id) AS sourceId1, '" + RelationName.hasValueLit + "' AS sourceTable1");
		} else {
			sql.append("(literal, colClass, language)");
			sql.append("\n\t SELECT DISTINCT hv.value, hv.type, hv.language");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("hasValueLit") + " AS hv");


		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT literal");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE NOT isSameLiteral(bottom.literal, hv.value, bottom.colClass, hv.type, bottom.language, hv.language)");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY hv.value, hv.type, hv.language");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionLit(O, T, L) :- hasValue(C, P, V, T, L)";
	}

}
