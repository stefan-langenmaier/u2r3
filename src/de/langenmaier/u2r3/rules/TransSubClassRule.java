package de.langenmaier.u2r3.rules;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import de.langenmaier.u2r3.DeltaRelation;
import de.langenmaier.u2r3.db.SubClassRelation;

public class TransSubClassRule extends Rule {
	static Logger logger = Logger.getLogger(TransSubClassRule.class);
	
	public TransSubClassRule() {
		try {
			//TODO zwei Abfragen machen, nur ein Delta und nur auf das gültige
			statement = conn.prepareStatement("INSERT INTO subClassAux (sub, super) SELECT sub,  super FROM ( " +
					" SELECT t1.sub AS sub, t2.super AS super " +
					" FROM subClassDelta AS t1 INNER JOIN subClassDelta AS t2 " +
					" WHERE t1.super = t2.sub  " +
					"	EXCEPT " +
					" SELECT sub, super " +
					" FROM subClassAux " +
					")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void apply(DeltaRelation delta) {
		long rows = 0;
		
		if (delta == null) {
			logger.debug(toString());
			try {
				statement = conn.prepareStatement("INSERT INTO subClassAux (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM subClass AS t1 INNER JOIN subClass AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"	EXCEPT " +
						" SELECT sub, super " +
						" FROM subClassAux " +
						")");
				rows = statement.executeUpdate();
				if (rows > 0) {
					//rp.add(new Reason(SubClassRelation.getRelation()));
					SubClassRelation.getRelation().setDirty(true);
					logger.debug("subClass is dirty");
					/**
					 * XXX
					 * Hier könnte man sofort eine neue Reason auslösen (immediate) oder 
					 * die Tabelle nur zum aktualisieren vormerken (collective).
					 * XXX
					 */
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			logger.debug(toString());
			try {
				statement = conn.prepareStatement("INSERT INTO subClassAux (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM subClassDelta AS t1 INNER JOIN subClass AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"	EXCEPT " +
						" SELECT sub, super " +
						" FROM subClassAux " +
						")");
				rows = statement.executeUpdate();
				if (rows > 0) {
					//rp.add(new Reason(SubClassRelation.getRelation()));
					SubClassRelation.getRelation().setDirty(true);
					logger.debug("subClass is dirty");
				}
				statement = conn.prepareStatement("INSERT INTO subClassAux (sub, super) SELECT sub,  super FROM ( " +
						" SELECT t1.sub AS sub, t2.super AS super " +
						" FROM subClass AS t1 INNER JOIN subClassDelta AS t2 " +
						" WHERE t1.super = t2.sub  " +
						"	EXCEPT " +
						" SELECT sub, super " +
						" FROM subClassAux " +
						")");
				rows = statement.executeUpdate();
				if (rows > 0) {
					//rp.add(new Reason(SubClassRelation.getRelation()));
					SubClassRelation.getRelation().setDirty(true);
					logger.debug("subClass is dirty");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		return "subClass(A,C) :- subClass(A,B), subClass(B,C)";
	}

}
