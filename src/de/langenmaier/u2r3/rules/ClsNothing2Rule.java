package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;


public class ClsNothing2Rule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(ClsNothing2Rule.class);
	
	ClsNothing2Rule() {
		targetRelation = null;
		
		RelationManager.getRelation(RelationName.declaration).addAdditionRule(this);
		
		//RelationManager.getRelation(RelationName.sameAs).addDeletionRule(this);
	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		long rows = 0;
		String sql = null;
		try {
			sql = buildQuery(delta, aux, false, 0);
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet						
				logger.trace("Adding delta data (NO_DELTA): " + sql);
			} else {
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
			}
			if (statement.executeQuery(sql).next()) {
				logger.warn("Inconsistency found!");
				throw new U2R3RuntimeException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rows;
	}

	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		long rows = 0;
		String sql = null;
		try {
			sql = buildQuery(delta, newDelta, false, 0);
			if (delta.getDelta() == DeltaRelation.NO_DELTA) {
				//There are no deltas yet						
				logger.trace("Adding delta data (NO_DELTA): " + sql);
			} else {
				logger.trace("Adding delta data (" + delta.getDelta() + ", 0): " + sql);
			}
			if (statement.executeQuery(sql).next()) {
				logger.warn("Inconsistency found!");
				throw new U2R3RuntimeException();
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
		
		sql.append("SELECT subject, type");
		sql.append("\nFROM " + delta.getDeltaName());
		sql.append("\nWHERE type = '" + OWLRDFVocabulary.OWL_NOTHING.getURI().toString() + "'");
		

		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- declaration(A, nothing)";
	}

}
