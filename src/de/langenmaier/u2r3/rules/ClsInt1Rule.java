package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class ClsInt1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(ClsInt1Rule.class);
	
	ClsInt1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.classAssertionEnt).addAdditionRule(this);
		relationManager.getRelation(RelationName.intersectionOf).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		//TODO optimieren für anderen deletion type
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (entity, class, sourceId1, sourceTable1, sourceId2, sourceTable2)");
		} else {
			sql.append(" (entity, class)");
		}
		
		sql.append("\n\t SELECT dat.entity, dat.class, ");
		sql.append(" tca.id AS sourceId1, 'classAssertionEnt' AS sourceTable1, ");
		sql.append(" dat.sourceId2, dat.sourceTable2");
		sql.append("\n\t	FROM intersectionOf AS tint");
		sql.append("\n\t\t INNER JOIN list AS tl ON tint.list = tl.name");
		sql.append("\n\t\t INNER JOIN classAssertionEnt AS tca ON tca.class = tl.element");
		sql.append("\n\t\t INNER JOIN (");
		
//		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append("\n\t\t SELECT clsA.entity AS entity, int.class AS class,");
			sql.append(" MIN(int.id) AS sourceId2, '" + RelationName.intersectionOf + "' AS sourceTable2");
//		} else {
//			sql.append(" (entity, class)");
//			sql.append("\n\t SELECT DISTINCT clsA.entity AS entity, int.class AS class");
//		}
		
		sql.append("\n\t\t FROM (SELECT name, COUNT(name) AS anzahl FROM list GROUP BY name) AS anzl");
		sql.append("\n\t\t\t INNER JOIN list AS l ON anzl.name = l.name");
		sql.append("\n\t\t\t INNER JOIN " + delta.getDeltaName("classAssertionEnt") + " AS clsA ON l.element = clsA.class");
		sql.append("\n\t\t\t INNER JOIN " + delta.getDeltaName("intersectionOf") + " AS int ON int.list = l.name");
		
		if (again) {
			sql.append("\n\t\t WHERE NOT EXISTS (");
			sql.append("\n\t\t\t SELECT bottom.entity");
			sql.append("\n\t\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t\t WHERE bottom.entity = clsA.entity AND bottom.class = int.class");
			sql.append("\n\t\t )");
		}
		sql.append("\n\t\t GROUP BY l.name, clsA.entity, class");
		sql.append("\n\t\t HAVING COUNT(l.name) = anzl.anzahl");
		sql.append("\n\t) AS dat ON dat.entity = tca.entity AND dat.class = tint.class");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(Y,C) :- intersectionOf(C, X), list(X, C1..Cn), classAssertionEnt(Y, C1..Cn)";
	}

}
