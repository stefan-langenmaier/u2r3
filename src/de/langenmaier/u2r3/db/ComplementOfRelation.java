package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

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

}
