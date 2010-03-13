package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class MaxQualifiedCardinalityRelation extends Relation {
	
	protected MaxQualifiedCardinalityRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "maxQualifiedCardinality";
			
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
		" total TEXT, " +
		" value TEXT," +
		" PRIMARY KEY (colClass, property, total, value));" +
		" CREATE INDEX " + table + "_class ON " + table + "(colClass);" +
		" CREATE INDEX " + table + "_property ON " + table + "(property);" +
		" CREATE INDEX " + table + "_total ON " + table + "(total);" +
		" CREATE INDEX " + table + "_value ON " + table + "(value);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colClass, property, total, value) VALUES (?, ?, ?, ?)";
	}

	
	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof OWLObjectMaxCardinality) {
				OWLObjectMaxCardinality mc = (OWLObjectMaxCardinality) ce;
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
					add.setString(1, nidMapper.get(ce).toString());
					
					if (mc.getProperty().isAnonymous()) {
						add.setString(2, nidMapper.get(mc.getProperty()).toString());
					} else {
						add.setString(2, mc.getProperty().asOWLObjectProperty().getIRI().toString());
					}
					
					if (mc.getFiller().isAnonymous()) {
						add.setString(3, nidMapper.get(mc.getFiller()).toString());
						handleAddAnonymousClassExpression(mc.getFiller());
					} else {
						add.setString(3, mc.getFiller().asOWLClass().getIRI().toString());
					}
					
					add.setString(4, Integer.toString(mc.getCardinality()));
					add.execute();
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				if (mc.getProperty().isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(mc.getProperty());
				}
			} else {
				throw new U2R3NotImplementedException();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
