package de.langenmaier.u2r3.rules;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.DeltaRelation;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.util.DatatypeCheck;


public class DtNotTypeRule extends ConsistencyRule {
	static Logger logger = Logger.getLogger(DtNotTypeRule.class);
	
	DtNotTypeRule(U2R3Reasoner reasoner) {
		super(reasoner);
		targetRelation = null;
		
		relationManager.getRelation(RelationName.classAssertionLit).addAdditionRule(this);

	}
	
	@Override
	protected long applyCollective(DeltaRelation delta, DeltaRelation aux) {
		return checkDtNotType(delta, aux);
	}
	
	@Override
	protected long applyImmediate(DeltaRelation delta, DeltaRelation newDelta) {
		return checkDtNotType(delta, newDelta);
	}

	private long checkDtNotType(DeltaRelation delta, DeltaRelation newDelta) {
		long rows = 0;
		String sql = null;
		try {
			sql = buildQuery(delta, newDelta, false, 0);
			logger.debug("Checking consistency: " + sql);
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()) {
				OWLDatatype dt = dataFactory.getOWLDatatype(IRI.create(rs.getString("class")));
				if (DatatypeCheck.isValid(rs.getString("literal"), dt)) {
					logger.warn("Inconsistency found!");
					reasonProcessor.setInconsistent(this);
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

		sql.append("SELECT literal, class");
		sql.append("\n FROM " + delta.getDeltaName("classAssertionLit") + " AS ca");
		
		return sql.toString();
	}

	@Override
	public String toString() {
		return "FALSE :- classAssertionLit(lt, X not type of lt)";
	}

}
