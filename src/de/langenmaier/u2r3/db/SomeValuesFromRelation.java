package de.langenmaier.u2r3.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.Pair;
import de.langenmaier.u2r3.util.TableId;

public class SomeValuesFromRelation extends Relation {
	
	protected SomeValuesFromRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "someValuesFrom";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" part TEXT," +
					" property TEXT," +
					" total TEXT," +
					" PRIMARY KEY (id, part, property, total))");
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
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
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
					handleAnonymousObjectPropertyExpression(svf.getProperty());
				}
				if (svf.getFiller().isAnonymous()) {
					handleAnonymousClassExpression(svf.getFiller());
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
			if (o instanceof OWLObjectSomeValuesFrom) {
				OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom) o;
				
				String tid = TableId.getId();
				
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT id");
				sql.append("\n FROM someValuesFrom AS " + tid);
				sql.append("\nWHERE EXISTS (");
				getSubSQL(sql, svf.getProperty(), tid, "property");
				sql.append(") AND EXISTS (");
				getSubSQL(sql, svf.getFiller(), tid, "total");
				sql.append(")");
				
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql.toString());
				
				if (rs.next()) {
					relationManager.remove((UUID) rs.getObject("id"), RelationName.someValuesFrom);

					if (svf.getProperty().isAnonymous()) {
						removeAnonymousPropertyExpression(svf.getProperty());
					}
					
					if (svf.getFiller().isAnonymous()) {
						removeAnonymousClassExpression(svf.getFiller());
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
