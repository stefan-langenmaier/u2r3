package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.TableId;

public class ComplementOfRelation extends Relation {
	
	protected ComplementOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "complementOf";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" PRIMARY KEY (colLeft, colRight));" +
					" CREATE INDEX " + getTableName() + "_colLeft ON " + getTableName() + "(colLeft);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(colRight);");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colLeft, colRight) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void add(OWLObject ce) {
		if (ce instanceof OWLObjectComplementOf) {
			OWLObjectComplementOf oco = (OWLObjectComplementOf) ce;
			try {
				addStatement.setString(1, nidMapper.get(ce).toString());
				if (oco.getOperand().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(oco.getOperand()).toString());
				} else {
					addStatement.setString(2, oco.getOperand().asOWLClass().getIRI().toString());
				}
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (oco.getOperand().isAnonymous()) {
					handleAddAnonymousClassExpression(oco.getOperand());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			throw new U2R3NotImplementedException();
		}
	}
	
	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLClassExpression ce, String tid, String col) {
		if (ce instanceof OWLObjectComplementOf) {
			OWLObjectComplementOf nax = (OWLObjectComplementOf) ce;
			String right = null;
			String tableId = TableId.getId();
			
			if (!nax.getOperand().isAnonymous()) {
				right = nax.getOperand().asOWLClass().getIRI().toString();
			}
			
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE " + tableId + ".colLeft=" + tid + "." + col + " AND ");
			if (right != null) {
				sql.append("colRight='" + right + "' ");
			} else {
				sql.append("EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getOperand(), tableId, "colRight");
			}

			return;
		}
		throw new U2R3RuntimeException();
	}


}
