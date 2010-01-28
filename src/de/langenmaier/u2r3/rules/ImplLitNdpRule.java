package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ImplLitNdpRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ImplSvfRule.class);
	
	ImplLitNdpRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionLit;
		
		//relations on the right side
		relationManager.getRelation(RelationName.negativeDataPropertyAssertion).addAdditionRule(this);
		
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
			sql.append("\n\t SELECT ndpa.object, ndpa.type, ndpa.language,");
			sql.append(" MIN(ndpa.id) AS sourceId1, '" + RelationName.negativeDataPropertyAssertion + "' AS sourceTable1");
		} else {
			sql.append("(literal, colClass, language)");
			sql.append("\n\t SELECT DISTINCT ndpa.object, ndpa.type, ndpa.language");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("negativeDataPropertyAssertion") + " AS ndpa");


		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT literal");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE NOT isSameLiteral(bottom.literal, ndpa.object, bottom.colClass, ndpa.type, bottom.language, ndpa.language)");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ndpa.object, ndpa.type, ndpa.language");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionLit(O, T, L) :- negativeDataPropertyAssertion(S, P, O, T, L)";
	}

}
