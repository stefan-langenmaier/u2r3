package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsHv1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsHv1Rule.class);
	
	ClsHv1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.propertyAssertion;
		
		relationManager.getRelation(RelationName.hasValue).addAdditionRule(this);
		relationManager.getRelation(RelationName.onProperty).addAdditionRule(this);
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, subjectSourceId, subjectSourceTable, propertySourceId, propertySourceTable, objectSourceId, objectSourceTable)");
			sql.append("\n\t SELECT ca.class AS subject, op.property AS property, hv.object as object, MIN(ca.id) AS subjectSourceId, '" + RelationName.classAssertion + "' AS subjectSourceTable, MIN(op.id) AS propertySourceId, '" + RelationName.onProperty + "' AS propertySourceTable, MIN(hv.id) AS objectSourceId, '" + RelationName.hasValue +"' ");
		} else {
			sql.append(" (subject, property, object)");
			sql.append("\n\t SELECT DISTINCT ca.class AS subject, op.property AS property, hv.object as object");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("hasValue") + " AS hv");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("onProperty") + " AS op ON hv.class = op.class");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("classAssertion") + " AS ca ON ca.type = hv.class");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = ca.class AND bottom.property = op.property AND bottom.object = hv.object");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY ca.class, op.property, hv.object");
		}

		return sql.toString();
	}

	@Override
	public String toString() {
		return "propertyAssertion(U, P, Y) :- hasValue(X, Y), onProperty(X, P), classAssertion(U, X)";
	}

}
