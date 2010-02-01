package de.langenmaier.u2r3.db;

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
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colClass TEXT," +
					" property TEXT, " +
					" total TEXT, " +
					" value TEXT," +
					" PRIMARY KEY (colClass, property, total, value));" +
					" CREATE INDEX " + getTableName() + "_class ON " + getTableName() + "(colClass);" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_total ON " + getTableName() + "(total);" +
					" CREATE INDEX " + getTableName() + "_value ON " + getTableName() + "(value);");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName());

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colClass, property, total, value) VALUES (?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void add(OWLObject ce) {
		try {
			addStatement.setString(1, nidMapper.get(ce).toString());
			
			if (ce instanceof OWLObjectMaxCardinality) {
				OWLObjectMaxCardinality mc = (OWLObjectMaxCardinality) ce;
				
				if (mc.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(mc.getProperty()).toString());
				} else {
					addStatement.setString(2, mc.getProperty().asOWLObjectProperty().getIRI().toString());
				}
				
				if (mc.getFiller().isAnonymous()) {
					addStatement.setString(3, nidMapper.get(mc.getFiller()).toString());
					handleAnonymousClassExpression(mc.getFiller());
				} else {
					addStatement.setString(3, mc.getFiller().asOWLClass().getIRI().toString());
				}
				
				addStatement.setString(4, Integer.toString(mc.getCardinality()));
				addStatement.execute();
				
				if (mc.getProperty().isAnonymous()) {
					handleAnonymousObjectPropertyExpression(mc.getProperty());
				}
			} else {
				throw new U2R3NotImplementedException();
			}

			reasonProcessor.add(new AdditionReason(this));
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
