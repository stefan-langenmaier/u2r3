package de.langenmaier.u2r3.rules;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class CaxEqc1Rule extends ApplicationRule {
	static Logger logger = Logger.getLogger(CaxEqc1Rule.class);
	
	CaxEqc1Rule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertion;
		
		relationManager.getRelation(RelationName.classAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.equivalentClass).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			sql.append(" (class, type, classSourceId, classSourceTable, typeSourceId, typeSourceTable)");
			sql.append("\n\t SELECT clsA.class, ec.right, MIN(clsA.id) AS classSourceId, '" + RelationName.classAssertion.toString() + "' AS classSourceTable, MIN(ec.id) AS typeSourceId, '" + RelationName.equivalentClass.toString() + "' AS typeSourceTable");
		} else {
			sql.append(" (class, type)");
			sql.append("\n\t SELECT DISTINCT clsA.class, ec.right");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("classAssertion") + " AS clsA INNER JOIN " + delta.getDeltaName("equivalentClass") + " AS ec ON clsA.type = ec.left");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT class, type");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.class = clsA.class AND bottom.type = ec.right");
			sql.append("\n\t )");
		}
		sql.append("\n\t  GROUP BY clsA.class, ec.right");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertion(X,C2) :- classAssertion(X, C1), equivalentClass(C1, C2)";
	}

}
