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
		targetRelation = RelationName.propertyAssertion;
		
		//relations on the right side
		relationManager.getRelation(RelationName.propertyChain).addAdditionRule(this);
		relationManager.getRelation(RelationName.propertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		//on the left side, aka targetRelation
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	
	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
	
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, property, object, subjectSourceId, subjectSourceTable, propertySourceId, propertySourceTable, objectSourceId, objectSourceTable)");
			sql.append("\n\t prpAnfang.subject, pc.property, prpEnde.object, MIN(prpAnfang.id) AS subjectSourceId, 'propertyAssertion' AS subjectSourceTable, MIN(pc.id) AS propertySourceId, 'propertyChain' AS propertySourceTable, MIN(prpEnde.id) AS objectSourceId, 'propertyAssertion' AS objectSourceTable");
		} else {
			sql.append("(subject, property, object)");
			sql.append("\n\t SELECT DISTINCT prpAnfang.subject, pc.property, prpEnde.object");
		}
		
		sql.append("\n\t FROM propertyChain AS pc");
		sql.append("\n\t\t INNER JOIN propertyAssertion AS prpAnfang ON pc.list = anfangsE.name");
		sql.append("\n\t\t INNER JOIN list AS anfangsE ON anfangsE.element = prpAnfang.property");
		sql.append("\n\t\t INNER JOIN (");
		sql.append("\n\t\t SELECT  l1.name, MIN(l1.ordnung) AS first");
		sql.append("\n\t\t FROM (SELECT name, COUNT(name) AS anzahl FROM list GROUP BY name) AS  anzl1");
		sql.append("\n\t\t\t INNER JOIN list AS l1 ON anzl1.name = l1.name");
		sql.append("\n\t\t\t INNER JOIN propertyAssertion AS prp1 ON l1.element = prp1.property");
		sql.append("\n\t\t WHERE EXISTS (");
		sql.append("\n\t\t\t SELECT prp1_n.subject");
		sql.append("\n\t\t\t FROM list AS l1_n INNER JOIN propertyAssertion AS prp1_n ON l1_n.element = prp1_n.property");
		sql.append("\n\t\t\t WHERE (prp1.object = prp1_n.subject AND l1.name = l1_n.name) OR (prp1.subject = prp1_n.object AND l1.name = l1_n.name)");
		sql.append("\n\t\t )");
		sql.append("\n\t\t GROUP BY l1.name");
		sql.append("\n\t\t HAVING COUNT(l1.name) = anzl1.anzahl");
		sql.append("\n\t ) AS anfangsl ON anfangsl.name = anfangsE.name AND anfangsl.first = anfangsE.ordnung");
		sql.append("\n\t\t INNER JOIN propertyAssertion AS prpEnde ON pc.list = endeE.name");
		sql.append("\n\t\t INNER JOIN list AS endeE ON endeE.element = prpEnde.property");
		sql.append("\n\t\t INNER JOIN (");
		sql.append("\n\t\t SELECT  l2.name, MAX(l2.ordnung) AS last");
		sql.append("\n\t\t FROM (SELECT name, COUNT(name) AS anzahl FROM list GROUP BY name) AS  anzl2");
		sql.append("\n\t\t\t INNER JOIN list AS l2 ON anzl2.name = l2.name");
		sql.append("\n\t\t\t INNER JOIN propertyAssertion AS prp2 ON l2.element = prp2.property");
		sql.append("\n\t\t WHERE EXISTS (");
		sql.append("\n\t\t\t SELECT prp2_n.subject");
		sql.append("\n\t\t\t FROM list AS l2_n INNER JOIN propertyAssertion AS prp2_n ON l2_n.element = prp2_n.property");
		sql.append("\n\t\t\t WHERE (prp2.object = prp2_n.subject AND l2.name = l2_n.name) OR (prp2.subject = prp2_n.object AND l2.name = l2_n.name)");
		sql.append("\n\t\t )");
		sql.append("\n\t\t GROUP BY l2.name");
		sql.append("\n\t\t HAVING COUNT(l2.name) = anzl2.anzahl");
		sql.append("\n\t\t ) AS endel ON endel.name = endeE.name AND endel.last = endeE.ordnung");
	

		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t bottom.subject = prpAnfang.subject AND bottom.property = pc.property AND bottom.object = prpEnde.object");
			sql.append("\n\t )");
		}
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t GROUP BY prpAnfang.subject, pc.property, prpEnde.object");
		}
		return sql.toString();
	}

	@Override
	public String toString() {
		return "propertyAssertion(Y, P2, X) :- subProperty(P, P2), propertyAssertion(X, P1, Y)";
	}

}
