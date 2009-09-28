package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsInt1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsInt1Rule.class);
	
	ClsInt1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.declaration;
		
		relationManager.getRelation(RelationName.declaration).addAdditionRule(this);
		relationManager.getRelation(RelationName.intersectionOf).addAdditionRule(this);
		relationManager.getRelation(RelationName.list).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (subject, type, subjectSourceId, subjectSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT dec.subject AS subject, int.class AS type, MIN(dec.id) AS subjectSourceId, '" + RelationName.declaration.toString() + "' AS subjectSourceTable, MIN(int.id) AS typeSourceId, '" + RelationName.intersectionOf.toString() + "' AS typeSourceTable");
		} else {
			sql.append(" (subject, type)");
			sql.append("\n\t SELECT DISTINCT dec.subject AS subject, int.class AS type");
		}
		
		sql.append("\n\t FROM (SELECT name, COUNT(name) AS anzahl FROM list GROUP BY name) AS  anzl");
		sql.append("\n\t\t INNER JOIN list AS l ON anzl.name = l.name");
		if (delta.getDelta() == DeltaRelation.NO_DELTA || delta.getRelation() == relationManager.getRelation(RelationName.list)) {	
			sql.append("\n\t\t INNER JOIN declaration AS dec ON l.element = dec.type");
			sql.append("\n\t\t INNER JOIN intersectionOf AS int ON int.list = l.name");
		} else {
			if (delta.getRelation() == relationManager.getRelation(RelationName.intersectionOf)) {
				sql.append("\n\t\t INNER JOIN declaration AS dec ON l.element = dec.type");
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS int ON int.list = l.name");
			} else if (delta.getRelation() == relationManager.getRelation(RelationName.declaration)) {
				sql.append("\n\t\t INNER JOIN " + delta.getDeltaName() + " AS dec ON l.element = dec.type");
				sql.append("\n\t\t INNER JOIN intersectionOf AS int ON int.list = l.name");
			}
		}
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT bottom.subject");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.subject = dec.subject AND bottom.type = int.class");
			sql.append("\n\t )");
		}
		sql.append("\n\t GROUP BY l.name, dec.subject, type");
		sql.append("\n\t HAVING COUNT(l.name) = anzl.anzahl");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "declaration(Y,C) :- intersectionOf(C, X), list(X, C1..Cn), declaration(Y, C1..Cn)";
	}

}
