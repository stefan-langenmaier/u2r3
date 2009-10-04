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
		targetRelation = RelationName.sameAs;
		
		//relations on the right side
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
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
			sql.append(" (left, right, leftSourceId, leftSourceTable, rightSourceId, rightSourceTable)");
			sql.append("\n\t SELECT cls1.class AS left, cls2.class AS right, MIN(cls1.id) AS leftSourceId, 'classAssertion' AS  leftSourceId, MIN(cls2.id) AS rightSourceId, 'classAssertion' AS rightSourceTable");
		} else {
			sql.append("(left, right)");
			sql.append("\n\t SELECT DISTINCT cls1.class AS left, cls2.class AS right");
		}
	
		sql.append("\n\t FROM hasKey AS hk");
		sql.append("\n\t\t INNER JOIN list AS l ON hk.list = l.name");
		sql.append("\n\t\t INNER JOIN (");
		sql.append("\n\t\t\t SELECT name, COUNT(name) AS anzahl");
		sql.append("\n\t\t\t FROM list");
		sql.append("\n\t\t\t GROUP BY name");
		sql.append("\n\t\t ) AS anzl ON l.name = anzl.name");
		sql.append("\n\t\t INNER JOIN classAssertion AS cls1 ON hk.class = cls1.type");
		sql.append("\n\t\t INNER JOIN propertyAssertion prp1 ON cls1.class = prp1.subject AND l.element = prp1.property");
		sql.append("\n\t\t INNER JOIN classAssertion AS cls2 ON hk.class = cls2.type");
		sql.append("\n\t\t INNER JOIN propertyAssertion prp2 ON cls2.class = prp2.subject AND l.element = prp2.property");
		sql.append("\n\t WHERE cls1.class != cls2.class AND prp1.property = prp2.property");

		if (again) {
			sql.append("\n\t\t AND NOT EXISTS (");
			sql.append("\n\t\t\t SELECT bottom.left");
			sql.append("\n\t\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t\t WHERE bottom.left = cls1.class AND bottom.right = cls2.class");
			sql.append("\n\t\t )");
		}
		
		sql.append("\n\t GROUP BY cls1.class, cls2.class");
		sql.append("\n\t HAVING COUNT(*) = anzl.anzahl");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "sameAs(X, Y) :- hasKey(C, U), list(U, P1..Pn), classAssertion(X, C), propertyAssertion(X, P1..Pn, Z1..Zn), classAssertion(Y, C), propertyAssertion(Y, P1..Pn, Z1..Zn)";
	}

}
