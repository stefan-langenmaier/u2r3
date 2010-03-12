package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
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
		" part TEXT," +
		" property TEXT," +
		" total TEXT," +
		" PRIMARY KEY (id, part, property, total));" +
		" CREATE INDEX " + table + "_part ON " + table + "(part);" +
		" CREATE INDEX " + table + "_property ON " + table + "(property);" +
		" CREATE INDEX " + table + "_total ON " + table + "(total)";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (part, property, total) VALUES (?, ?, ?)";
	}

	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) ce;
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
					add.setString(1, nidMapper.get(ce).toString());
					
					if (svf.getProperty().isAnonymous()) {
						add.setString(2, nidMapper.get(svf.getProperty()).toString());
					} else {
						add.setString(2, svf.getProperty().asOWLObjectProperty().getIRI().toString());
					}
					
					if (svf.getFiller().isAnonymous()) {
						add.setString(3, nidMapper.get(svf.getFiller()).toString());
					} else {
						add.setString(3, svf.getFiller().asOWLClass().getIRI().toString());
					}
					//System.out.println(addStatement);
					add.execute();
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				
				if (svf.getProperty().isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(svf.getProperty());
				}
				if (svf.getFiller().isAnonymous()) {
					handleAddAnonymousClassExpression(svf.getFiller());
				}
				
			} else if (ce instanceof  OWLDataSomeValuesFrom) {
				OWLDataSomeValuesFrom svf = (OWLDataSomeValuesFrom) ce;
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); nextRound(add), ++run) {
					add.setString(1, nidMapper.get(ce).toString());
					
					if (svf.getProperty().isAnonymous()) {
						add.setString(2, nidMapper.get(svf.getProperty()).toString());
					} else {
						add.setString(2, svf.getProperty().asOWLDataProperty().getIRI().toString());
					}
					
					if (!svf.getFiller().isDatatype()) {
						add.setString(3, nidMapper.get(svf.getFiller()).toString());
					} else {
						add.setString(3, svf.getFiller().asOWLDatatype().getIRI().toString());
					}
					//System.out.println(addStatement);
					add.execute();
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				
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
