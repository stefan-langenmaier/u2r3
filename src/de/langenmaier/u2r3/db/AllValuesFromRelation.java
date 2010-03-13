package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.exceptions.U2R3RuntimeException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.TableId;

public class AllValuesFromRelation extends Relation {
	
	protected AllValuesFromRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "allValuesFrom";
			
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
			" property TEXT, " +
			" total TEXT, " +
			" PRIMARY KEY (id, part, property, total));" +
			" CREATE INDEX " + table + "_part ON " + getTableName() + "(part);" +
			" CREATE INDEX " + table + "_property ON " + getTableName() + "(property);" +
			" CREATE INDEX " + table + "_total ON " + getTableName() + "(total);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (part, property, total) VALUES (?, ?, ?)";
	}

	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) ce;
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
					add.setString(1, nidMapper.get(ce).toString());
					
					if (avf.getProperty().isAnonymous()) {
						add.setString(2, nidMapper.get(avf.getProperty()).toString());
					} else {
						add.setString(2, avf.getProperty().asOWLObjectProperty().getIRI().toString());
					}
					
					if (avf.getFiller().isAnonymous()) {
						add.setString(3, nidMapper.get(avf.getFiller()).toString());
					} else {
						add.setString(3, avf.getFiller().asOWLClass().getIRI().toString());
					}
					
					add.execute();
					
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				
				//WATCHOUT recursive calls
				if (avf.getProperty().isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(avf.getProperty());
				}
				if (avf.getFiller().isAnonymous()) {
					handleAddAnonymousClassExpression(avf.getFiller());
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
			if (o instanceof OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) o;
				
				if (avf.getProperty().isAnonymous()) {
					removeObject(avf.getProperty());
				}
				
				if (avf.getFiller().isAnonymous()) {
					removeObject(avf.getFiller());
				}

			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLClassExpression ce, String tid, String col) {
		if (ce instanceof OWLObjectAllValuesFrom) {
			OWLObjectAllValuesFrom nax = (OWLObjectAllValuesFrom) ce;
			String property = null;
			String filler = null;
			String tableId = TableId.getId();
			
			if (nax.getProperty().isAnonymous()) {
				property = nax.getProperty().asOWLObjectProperty().getIRI().toString();
			}
			
			if (!nax.getFiller().isAnonymous()) {
				filler = nax.getFiller().asOWLClass().getIRI().toString();
			}			
			
			
			sql.append("SELECT id, '" + getTableName() + "' AS colTable ");
			sql.append("\nFROM  " + getTableName() + " AS " + tableId);
			sql.append("\nWHERE ");
			if (property != null) {
				sql.append("property='" + property + "' ");
			} else {
				sql.append("EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getProperty(), tableId, "property");
			}
			
			if (property != null) {
				sql.append(" AND total='" + filler + "' ");
			} else {
				sql.append(" EXISTS ");
				handleSubAxiomLocationImpl(sql, nax.getFiller(), tableId, "total");
			}

			return;
		}
		throw new U2R3RuntimeException();
	}

}
