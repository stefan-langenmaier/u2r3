package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.TableId;

public class IntersectionOfRelation extends Relation {
	static Logger logger = Logger.getLogger(IntersectionOfRelation.class);
	
	
	protected IntersectionOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "intersectionOf";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));
			addListStatement = conn.prepareStatement("INSERT INTO list (name, element) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colClass TEXT," +
		" list TEXT," +
		" PRIMARY KEY (colClass, list));" +
		" CREATE INDEX " + table + "_class ON " + table + "(colClass);" +
		" CREATE INDEX " + table + "_list ON " + table + "(list);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colClass, list) VALUES (?, ?)";
	}

	
	@Override
	public void add(OWLObject ce) {
		OWLObjectIntersectionOf oio = (OWLObjectIntersectionOf) ce;
		try {
			NodeID nid = NodeID.getNodeID();
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
				add.setString(1, nidMapper.get(ce).toString());
				add.setString(2, nid.toString());
				add.execute();
			}
			if (reasoner.isAdditionMode()) {
				reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
			} else {
				reasonProcessor.add(new AdditionReason(this));
			}
			
			for (OWLClassExpression nce : oio.getOperands()) {
				addListStatement.setString(1, nid.toString());
				if (nce.isAnonymous()) {
					addListStatement.setString(2, nidMapper.get(nce).toString());
				} else {
					addListStatement.setString(2, nce.asOWLClass().getIRI().toString());
				}
				
				addListStatement.execute();
				
				if (nce.isAnonymous()) {
					handleAddAnonymousClassExpression(nce);
				}
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void removeImpl(OWLObject o) {
		try {
			if (o instanceof OWLObjectIntersectionOf) {
				OWLObjectIntersectionOf oi = (OWLObjectIntersectionOf) o;
				
				for(OWLClassExpression ce : oi.getOperands()) {
					if (ce.isAnonymous()) {
						removeObject(ce);
					}
				}
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLClassExpression ce, String tid, String col) {
		if (ce instanceof OWLObjectIntersectionOf) {
			OWLObjectIntersectionOf nax = (OWLObjectIntersectionOf) ce;

			String tableId = TableId.getId();
			String ltid;
			
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			if (tid == null) {
				sql.append("\nWHERE 1=1"); //tid
			} else {
				sql.append("\nWHERE " + tableId + ".colClass = " + tid +"." + col); //tid
			}
			
			for(OWLClassExpression element : nax.getOperands()) {
				ltid = TableId.getId();
				sql.append(" AND ");
				sql.append(" EXISTS (");
				sql.append("\nSELECT element");
				sql.append("\nFROM list AS " + ltid);
				sql.append("\nWHERE " + ltid +".name = " + tableId + ".list");
				
				if (!element.isAnonymous()) {
					sql.append(" AND element='' ");
				} else {
					sql.append(" AND EXISTS ");
					getSubAxiomLocationImpl(sql, element, ltid, "element");
				}
				
				sql.append(")"); 
			}
			return;
		}
		throw new U2R3RuntimeException();
	}

}
