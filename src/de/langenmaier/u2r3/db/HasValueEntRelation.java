package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectHasValue;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class HasValueEntRelation extends Relation {
	
	protected HasValueEntRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "hasValueEnt";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colClass TEXT," +
		" property TEXT, " +
		" value TEXT," +
		" PRIMARY KEY (id, colClass, property, value));" +
		" CREATE INDEX " + table + "_class ON " + table + "(colClass);" +
		" CREATE INDEX " + table + "_property ON " + table + "(property);" +
		" CREATE INDEX " + table + "_value ON " + table + "(value);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colClass, property, value) VALUES (?, ?, ?)";
	}


	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLObjectHasValue) {
				OWLObjectHasValue hv = (OWLObjectHasValue) ce;
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
					add.setString(1, nidMapper.get(ce).toString());
					
					if (hv.getProperty().isAnonymous()) {
						add.setString(2, nidMapper.get(hv.getProperty()).toString());
					} else {
						add.setString(2, hv.getProperty().asOWLObjectProperty().getIRI().toString());
					}
					
					if (hv.getValue().isAnonymous()) {
						add.setString(3, hv.getValue().asOWLAnonymousIndividual().getID().toString());
					} else {
						add.setString(3, hv.getValue().asOWLNamedIndividual().getIRI().toString());
					}
					
					add.execute();
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				
				if (hv.getProperty().isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(hv.getProperty());
				}
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
