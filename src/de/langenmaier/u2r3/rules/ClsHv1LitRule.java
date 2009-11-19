package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsHv1LitRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsHv1LitRule.class);
	
	ClsHv1LitRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.dataPropertyAssertion;
		
		relationManager.getRelation(RelationName.hasValueLit).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, language, type, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT ca.entity AS subject, hv.property AS property, hv.value as object, hv.language, hv.type, ");
			sql.append(" MIN(ca.id) AS sourceId1, '" + RelationName.classAssertionEnt + "' AS sourceTable1, ");
			sql.append(" MIN(hv.id) AS sourceId2, '" + RelationName.hasValueLit +"' AS sourceTable2");
		} else {
			sql.append(" (subject, property, object, language, type)");
			sql.append("\n\t SELECT DISTINCT ca.entity AS subject, hv.property AS property, hv.value as object, hv.language, hv.type");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("hasValueLit") + " AS hv");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS ca ON ca.class = hv.class");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = ca.entity AND bottom.property = hv.property AND bottom.object = hv.value");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ca.entity, hv.property, hv.value");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "dataPropertyAssertion(U, P, Y) :- hasValueLit(X, Y), onProperty(X, P), classAssertionEnt(U, X)";
	}

}
