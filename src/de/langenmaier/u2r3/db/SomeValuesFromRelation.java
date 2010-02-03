package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;

public class SomeValuesFromRelation extends Relation {
	
	protected SomeValuesFromRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "someValuesFrom";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" part TEXT," +
					" property TEXT," +
					" total TEXT," +
					" PRIMARY KEY (id, part, property, total));" +
					" CREATE INDEX " + getTableName() + "_part ON " + getTableName() + "(part);" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_total ON " + getTableName() + "(total)");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (part, property, total) VALUES (?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (svf.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(svf.getProperty()).toString());
				} else {
					addStatement.setString(2, svf.getProperty().asOWLObjectProperty().getIRI().toString());
				}
				
				if (svf.getFiller().isAnonymous()) {
					addStatement.setString(3, nidMapper.get(svf.getFiller()).toString());
				} else {
					addStatement.setString(3, svf.getFiller().asOWLClass().getIRI().toString());
				}
				//System.out.println(addStatement);
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (svf.getProperty().isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(svf.getProperty());
				}
				if (svf.getFiller().isAnonymous()) {
					handleAddAnonymousClassExpression(svf.getFiller());
				}
				
			} else if (ce instanceof  OWLDataSomeValuesFrom) {
				OWLDataSomeValuesFrom svf = (OWLDataSomeValuesFrom) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (svf.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(svf.getProperty()).toString());
				} else {
					addStatement.setString(2, svf.getProperty().asOWLDataProperty().getIRI().toString());
				}
				
				if (!svf.getFiller().isDatatype()) {
					addStatement.setString(3, nidMapper.get(svf.getFiller()).toString());
				} else {
					addStatement.setString(3, svf.getFiller().asOWLDatatype().getIRI().toString());
				}
				//System.out.println(addStatement);
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (svf.getProperty().isAnonymous()) {
					handleAddAnonymousDataPropertyExpression(svf.getProperty());
				}
				if (!svf.getFiller().isDatatype()) {
					handleAddAnonymousDataRangeExpression(svf.getFiller());
				}
				
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void removeImpl(OWLObject o) {
		try {
			if (o instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) o;
				
				if (svf.getProperty().isAnonymous()) {
					removeObject(svf.getProperty());
				}
				
				if (svf.getFiller().isAnonymous()) {
					removeObject(svf.getFiller());
				}
				
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
	}

}
