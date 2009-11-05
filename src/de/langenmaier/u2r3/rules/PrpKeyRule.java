package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class PrpKeyRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(PrpKeyRule.class);
	
	PrpKeyRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.sameAsEnt;
		
		//relations on the right side
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.objectPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.hasKey).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
	
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (left, right, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3, sourceId4, sourceTable4, sourceId5, sourceTable5)");
			sql.append("\n\t SELECT ca1.entity AS left, ca2.entity AS right, ");
			sql.append(" hk.id AS sourceId1, 'hasKey' AS sourceTable1, ");
			sql.append(" ca1.id AS sourceId2, 'classAssertionEnt' AS sourceTable2, ");
			sql.append(" pa1.id AS sourceId3, pa1.type AS sourceTable1, ");
			sql.append(" ca2.id AS sourceId4, 'classAssertionEnt' AS sourceTable4, ");
			sql.append(" pa2.id AS sourceId5, pa2.type AS sourceTable5");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT ca1.entity AS left, ca2.entity AS right");
		}

		
		sql.append("\n FROM hasKey AS hk");
		sql.append("\n\t INNER JOIN list AS l");
		sql.append("\n\t\t ON l.name = hk.list");
		sql.append("\n\t INNER JOIN classAssertionEnt AS ca1");
		sql.append("\n\t 	ON ca1.class = hk.class");
		sql.append("\n\t INNER JOIN (");
		addUnion(sql);
		sql.append("\n\t ) AS pa1");
		sql.append("\n\t 	ON pa1.subject = ca1.entity AND pa1.property = l.element");
		sql.append("\n\t INNER JOIN classAssertionEnt AS ca2");
		sql.append("\n\t 	ON ca2.class = hk.class");
		sql.append("\n\t INNER JOIN (");
		addUnion(sql);
		sql.append("\n\t ) AS pa2");
		sql.append("\n\t 	ON pa2.subject = ca2.entity AND pa2.property = l.element AND pa1.object = pa2.object");
		sql.append("\n\t INNER JOIN (");
		addValid(sql);
		sql.append("\n\t ) AS valid1");
		sql.append("\n\t 	ON valid1.subject = ca1.entity");
		sql.append("\n\t INNER JOIN (");
		addValid(sql);
		sql.append("\n\t ) AS valid2");
		sql.append("\n\t 	ON valid2.subject = ca2.entity");
	
		if (again) {
			sql.append("\n\t\t WHERE NOT EXISTS (");
			sql.append("\n\t\t\t SELECT bottom.left");
			sql.append("\n\t\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t\t WHERE bottom.left = ca1.entity AND bottom.right = ca2.entity");
			sql.append("\n\t\t )");
		}

		return sql.toString();
	}

	private void addValid(StringBuilder sql) {
		sql.append("\n\t 	SELECT pax.subject, sl.name, anzl.anz");
		sql.append("\n\t 	FROM list AS sl");
		sql.append("\n\t 		INNER JOIN (");
		sql.append("\n\t 			SELECT name, COUNT(name) AS anz");
		sql.append("\n\t 			FROM list");
		sql.append("\n\t 			GROUP BY name");
		sql.append("\n\t 		) AS anzl");
		sql.append("\n\t 			ON anzl.name = sl.name");
		sql.append("\n\t 		INNER JOIN (");
		sql.append("\n\t 			SELECT id, subject, property, object");
		sql.append("\n\t 			FROM objectPropertyAssertion");
		sql.append("\n\t 			UNION");
		sql.append("\n\t 			SELECT id, subject, property, object");
		sql.append("\n\t 			FROM dataPropertyAssertion");
		sql.append("\n\t 		) AS pax");
		sql.append("\n\t 			ON sl.element = pax.property");
		sql.append("\n\t 		INNER JOIN (");
		sql.append("\n\t 			SELECT id, subject, property, object");
		sql.append("\n\t 			FROM objectPropertyAssertion");
		sql.append("\n\t 			UNION");
		sql.append("\n\t 			SELECT id, subject, property, object");
		sql.append("\n\t 			FROM dataPropertyAssertion");
		sql.append("\n\t 		) AS pay");
		sql.append("\n\t 			ON sl.element = pay.property AND pax.property = pay.property AND pax.object = pay.object");
		sql.append("\n\t 	GROUP BY pax.subject, sl.name");
		sql.append("\n\t 	HAVING COUNT(sl.name) = 2*anz");
	}


	private void addUnion(StringBuilder sql) {
		sql.append("\n\t 	SELECT id, subject, property, object, 'objectPropertyAssertion' AS type");
		sql.append("\n\t 	FROM objectPropertyAssertion");
		sql.append("\n\t 	UNION");
		sql.append("\n\t 	SELECT id, subject, property, object, 'dataPropertyAssertion' AS type");
		sql.append("\n\t 	FROM dataPropertyAssertion");
	}


	@Override
	public String toString() {
		return "sameAsEnt(X, Y) :- hasKey(C, U), list(U, P1..Pn), classAssertionEnt(X, C), propertyAssertionXXX(X, P1..Pn, Z1..Zn), classAssertionEnt(Y, C), propertyAssertionXXX(Y, P1..Pn, Z1..Zn)";
	}

}
