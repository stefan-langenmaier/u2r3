package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpSpo2Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpSpo2Rule.class);
	
	PrpSpo2Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.objectPropertyAssertion;
		
		//relations on the right side
		relationManager.getRelation(RelationName.propertyChain).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
	
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT start.start, pc.property, ende.ende,");
			sql.append(" ref.opaid AS sourceId1, '" + RelationName.objectPropertyAssertion + "' AS sourceTable1, ");
			sql.append(" pc.id AS sourceId2, '" + RelationName.propertyChain + "' AS sourceTable2");
		} else {
			sql.append("(subject, property, object)");
			sql.append("\n\t SELECT DISTINCT start.start, pc.property, ende.ende");
		}
		
		sql.append("\n FROM (");
		sql.append("\n\t SELECT lname, anz FROM (");
		addView(sql);
		sql.append("\n\t )");		
		sql.append("\n\t 		GROUP BY lname");		
		sql.append("\n\t 		HAVING COUNT(lname) = anz");		
		sql.append("\n\t 	) AS thel");
		sql.append("\n\t 	INNER JOIN (");
		sql.append("\n\t 		SELECT lname, start");
		sql.append("\n\t 		FROM (");
		addView(sql);
		sql.append("\n\t 			)");
		sql.append("\n\t 		WHERE vorgaenger IS NULL");
		sql.append("\n\t 	) AS start");
		sql.append("\n\t 		ON start.lname = thel.lname");
		sql.append("\n\t 	INNER JOIN (");
		sql.append("\n\t 		SELECT lname, ende");
		sql.append("\n\t 		FROM (");
		addView(sql);
		sql.append("\n\t 			)");
		sql.append("\n\t 		WHERE nachfolger IS NULL");
		sql.append("\n\t 	) AS ende");
		sql.append("\n\t 		ON ende.lname = thel.lname");
		sql.append("\n\t 	INNER JOIN (");
		sql.append("\n\t 		SELECT lname, opaid");
		sql.append("\n\t 		FROM (");
		addView(sql);
		sql.append("\n\t 			)");
		sql.append("\n\t 	) AS ref");
		sql.append("\n\t 		ON ref.lname = thel.lname");
		sql.append("\n\t 	INNER JOIN propertyChain AS pc");
		sql.append("\n\t 		ON pc.list = thel.lname");
	
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = start.start AND bottom.property = pc.property AND bottom.object = ende.ende");
			sql.append("\n\t )");
		}

		return sql.toString();
	}

	private void addView(StringBuilder sql) {
		sql.append("\n\t SELECT vopa.subject as vorgaenger, opa.subject AS start, opa.object AS ende, nopa.object as nachfolger, l.name AS lname, anzl.anz, opa.id AS opaid");
		sql.append("\n\t 	FROM list AS l");
		sql.append("\n\t 	INNER JOIN objectPropertyAssertion AS opa");
		sql.append("\n\t 		ON opa.property = l.element");
		sql.append("\n\t 	INNER JOIN (");
		sql.append("\n\t 		SELECT name, COUNT(name) AS anz");
		sql.append("\n\t 		FROM list");
		sql.append("\n\t 	) AS anzl ON anzl.name = l.name");
		sql.append("\n\t 	LEFT OUTER JOIN objectPropertyAssertion AS vopa");
		sql.append("\n\t 		ON vopa.object = opa.subject");
		sql.append("\n\t 	LEFT OUTER JOIN objectPropertyAssertion AS nopa");
		sql.append("\n\t 		ON nopa.subject = opa.object");
		sql.append("\n\t 		WHERE EXISTS (");
		sql.append("\n\t 		 SELECT 1");
		sql.append("\n\t 		 FROM list AS sl");
		sql.append("\n\t 		 	INNER JOIN objectPropertyAssertion AS sopa ON sl.element = sopa.property");
		sql.append("\n\t 		 WHERE l.name = sl.name AND (opa.object = sopa.subject OR opa.subject = sopa.object)");
		sql.append("\n\t 	)");
	}


	@Override
	public String toString() {
		return "objectPropertyAssertion(Y, P2, X) :- subProperty(P, P2), objectPropertyAssertion(X, P1, Y)";
	}

}
