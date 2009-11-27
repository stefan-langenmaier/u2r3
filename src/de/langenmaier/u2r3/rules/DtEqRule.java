package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class DtEqRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(DtEqRule.class);
	
	DtEqRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsLit;
		
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta, boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);

		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, left_type, right_type, left_language, right_language, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT ca1.literal, ca2.literal, ca1.class, ca2.class, ca1.language, ca2.language, MIN(ca1.id) AS sourceId1, '" + RelationName.classAssertionLit + "' AS sourceTable1, MIN(ca2.id) AS sourceId2, '" + RelationName.classAssertionLit + "' AS sourceTable2");
		} else {
			sql.append(" (left, right, left_type, right_type, left_language, right_language)");
			sql.append("\n\t SELECT DISTINCT ca1.left, ca2.right, ca1.class, ca2.class, ca1.language, ca2.language ");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertionLit") + " AS ca1 ");
		sql.append("\n\t\t CROSS JOIN classAssertionLit AS ca2");
		sql.append("\n\t WHERE ca1.literal = ca2.literal AND ca1.class = ca2.class AND ca1.language = ca2.language"); //TODO durch bessere Funktion ersetzen
		
		if (again) {
			sql.append("\n\t AND NOT EXISTS (");
			sql.append("\n\t\t SELECT left, right");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.left = ca1.literal AND bottom.right = ca2.literal AND bottom.left_type = ca1.class AND bottom.right_type = ca2.class AND bottom.left_language = ca1.language AND bottom.right_language = ca2.language) ");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ca1.literal, ca2.literal");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAsLit(lt1,lt2) :- classAssertionLit(lt1,A), classAssertionLit(lt2,B), lt1 same value as lt2";
	}

}
