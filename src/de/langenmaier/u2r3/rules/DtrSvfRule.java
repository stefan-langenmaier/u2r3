package de.langenmaier.u2r3.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLFacet;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class DtrSvfRule extends ApplicationRule {
	static Logger logger = Logger.getLogger(DtrSvfRule.class);
	
	DtrSvfRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = RelationName.classAssertionEnt;
		
		relationManager.getRelation(RelationName.dataPropertyAssertion).addAdditionRule(this);
		relationManager.getRelation(RelationName.someValuesFrom).addAdditionRule(this);
		relationManager.getRelation(RelationName.datatypeRestriction).addAdditionRule(this);
		
		relationManager.getRelation(targetRelation).addDeletionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		long rows = 0;
		String sql = null;
		try {
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet		
				sql = buildQuery(delta, aux, true, 0);
				logger.debug("Adding delta data (NO_DELTA): " + sql);
				//rows = statement.executeUpdate(sql);
			} else {
				sql = buildQuery(delta, aux, true, 0);
				logger.debug("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
				//rows = statement.executeUpdate(sql);
			}
			ResultSet rs = statement.executeQuery(sql);
			Statement restrictionsQuery = conn.createStatement();
			if (settings.getDeletionType() == DeletionType.CASCADING) {
				sql = "INSERT INTO " + aux.getDeltaName() +
				" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2, sourceId3, sourceTable3)" +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			} else {
				sql = "INSERT INTO " + aux.getDeltaName() +
				" (entity, colClass)" +
				" VALUES (?, ?)";
			}
			PreparedStatement addition = conn.prepareStatement(sql);
			while(rs.next()) {
				boolean add = true;
				
				sql = "SELECT facet, value, type, language" + 
					" FROM facetList AS fl " +
					" WHERE fl.name = '" + rs.getString("theList") + "'";
				ResultSet restrictions = restrictionsQuery.executeQuery(sql);
				int value = Integer.parseInt(rs.getString("theObject"));
				while(restrictions.next()) {
					// V in R for every facet
					String facet = restrictions.getString("facet");
					int comparision = Integer.parseInt(restrictions.getString("value"));
					
					//String type = restrictions.getString("type");
					if (facet.equals(OWLFacet.MIN_INCLUSIVE.getIRI().toString())) {
						if (!(value >= comparision)) {
							add = false;
						}
					}
					
					if (facet.equals(OWLFacet.MIN_EXCLUSIVE.getIRI().toString())) {
						if (!(value > comparision)) {
							add = false;
						}
					}
					
					if (facet.equals(OWLFacet.MAX_EXCLUSIVE.getIRI().toString())) {
						if (!(value < comparision)) {
							add = false;
						}
					}
					
					if (facet.equals(OWLFacet.MAX_INCLUSIVE.getIRI().toString())) {
						if (!(value <= comparision)) {
							add = false;
						}
					}
				}
				
				if (add) {
					addition.setString(1, rs.getString(1));
					addition.setString(2, rs.getString(2));
					if (settings.getDeletionType() == DeletionType.CASCADING) {
						addition.setString(3, rs.getString(7));
						addition.setString(4, rs.getString(8));
						addition.setString(5, rs.getString(9));
						addition.setString(6, rs.getString(10));
						addition.setString(7, rs.getString(11));
						addition.setString(8, rs.getString(12));
					}
					System.out.println("XXX: " + addition);
					addition.execute();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	@Override
	protected String buildQuery(DeltaRelation delta, DeltaRelation newDelta,
			boolean again, int run) {
		StringBuilder sql = new StringBuilder(400);
		
		//sql.append("INSERT INTO " + newDelta.getDeltaName());
		
		if (settings.getDeletionType() == DeletionType.CASCADING) {
			//sql.append(" (entity, colClass, sourceId1, sourceTable1, sourceId2, sourceTable2)");
			sql.append("\n\t SELECT dpa.subject, svf.part, dpa.object AS theObject, dpa.type, dpa.language, dtr.list AS theList,");
			sql.append(" MIN(dpa.id) AS sourceId1, '" + RelationName.dataPropertyAssertion + "' AS sourceTable1, ");
			sql.append(" MIN(svf.id) AS sourceId2, '" + RelationName.someValuesFrom + "' AS sourceTable2, ");
			sql.append(" MIN(dtr.id) AS sourceId3, '" + RelationName.datatypeRestriction + "' AS sourceTable3");
		} else {
			//sql.append(" (entity, colClass)");
			sql.append("\n\t SELECT DISTINCT dpa.subject, svf.part, dpa.object AS theObject, dpa.type, dpa.language, dtr.list AS theList");
		}
		
		sql.append("\n\t FROM " + delta.getDeltaName("dataPropertyAssertion") + " AS dpa");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("someValuesFrom") + " AS svf ON dpa.property = svf.property");
		sql.append("\n\t\t INNER JOIN " + delta.getDeltaName("datatypeRestriction") + " AS dtr ON dtr.colClass = svf.total");
		
		if (again) {
			sql.append("\n\t WHERE NOT EXISTS (");
			sql.append("\n\t\t SELECT 1");
			sql.append("\n\t\t FROM " + newDelta.getDeltaName() + " AS bottom");
			sql.append("\n\t\t WHERE bottom.entity = dpa.subject AND bottom.colClass = svf.part");
			sql.append("\n\t )");
		}
		//sql.append("\n\t  GROUP BY dpa.subject, svf.part");
		return sql.toString();
	}

	@Override
	public String toString() {
		return "classAssertionEnt(S, X) :- dataPropertyAssertion(S, P, V), someValuesFrom(X, P, Y), datatypeRestriction(Y, R), V in R";
	}

}
