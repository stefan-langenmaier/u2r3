package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsHv2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsHv2Rule.class);
	
	ClsHv2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		relationManager.getRelation(RelationName.hasValue).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (class, type, classSourceId, classSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT prp.subject AS class, hv.class AS type, MIN(prp.id) AS classSourceId, '" + RelationName.propertyAssertion + "' AS classSourceTable, MIN(hv.id) AS typeSourceId, '" + RelationName.hasValue + "' AS typeSourceTable ");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT prp.subject AS class, hv.class AS type");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("hasValue") + " AS hv");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON hv.class = op.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("propertyAssertion") + " AS prp ON prp.property = op.property");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.class");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = prp.subject AND bottom.type = hv.class");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prp.subject, hv.class");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(U, X) :- hasValue(X, Y), onProperty(X, P), propertyAssertion(U, P, Y)";
	}

}
