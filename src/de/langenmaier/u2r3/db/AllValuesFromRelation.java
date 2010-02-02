package de.langenmaier.u2r3.db;

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
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" part TEXT," +
					" property TEXT, " +
					" total TEXT, " +
					" PRIMARY KEY (id, part, property, total));" +
					" CREATE INDEX " + getTableName() + "_part ON " + getTableName() + "(part);" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_total ON " + getTableName() + "(total);");
			
			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (part, property, total) VALUES (?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (avf.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(avf.getProperty()).toString());
				} else {
					addStatement.setString(2, avf.getProperty().asOWLObjectProperty().getIRI().toString());
				}
				
				if (avf.getFiller().isAnonymous()) {
					addStatement.setString(3, nidMapper.get(avf.getFiller()).toString());
				} else {
					addStatement.setString(3, avf.getFiller().asOWLClass().getIRI().toString());
				}
				
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
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
