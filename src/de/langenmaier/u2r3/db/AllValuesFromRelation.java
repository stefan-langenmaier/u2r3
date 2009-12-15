package de.langenmaier.u2r3.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.TableId;

public class AllValuesFromRelation extends Relation {
	
	protected AllValuesFromRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "allValuesFrom";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" part TEXT," +
					" property TEXT, " +
					" total TEXT, " +
					" PRIMARY KEY (part, total))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (part, property, total) VALUES (?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		throw new U2R3NotImplementedException();

	}

	@Override
	public void createDeltaImpl(int id) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void merge(DeltaRelation delta) {
		throw new U2R3NotImplementedException();
	}

	@Override
	public void removeImpl(OWLAxiom axiom)
			throws SQLException {
		
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		throw new U2R3NotImplementedException();
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
					handleAnonymousObjectPropertyExpression(avf.getProperty());
				}
				if (avf.getFiller().isAnonymous()) {
					handleAnonymousClassExpression(avf.getFiller());
				}
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void remove(OWLObject o) {
		try {
			if (o instanceof OWLObjectAllValuesFrom) {
				OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) o;
				
				String tid = TableId.getId();
				
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT id");
				sql.append("\n FROM allValuesFrom AS " + tid);
				sql.append("\nWHERE EXISTS (");
				getSubSQL(sql, avf.getProperty(), tid, "property");
				sql.append(") AND EXISTS (");
				getSubSQL(sql, avf.getFiller(), tid, "total");
				sql.append(")");
				
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql.toString());
				
				if (rs.next()) {
					relationManager.remove((UUID) rs.getObject("id"), RelationName.allValuesFrom);

					if (avf.getProperty().isAnonymous()) {
						removeAnonymousPropertyExpression(avf.getProperty());
					}
					
					if (avf.getFiller().isAnonymous()) {
						removeAnonymousClassExpression(avf.getFiller());
					}
					
					
				}
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
	}

}
