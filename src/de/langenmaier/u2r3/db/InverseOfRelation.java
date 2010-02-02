package de.langenmaier.u2r3.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.TableId;

public class InverseOfRelation extends Relation {
	static Logger logger = Logger.getLogger(InverseOfRelation.class);
	
	protected InverseOfRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "inverseOf";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colLeft TEXT," +
					" colRight TEXT," +
					" PRIMARY KEY (colLeft, colRight));" +
					" CREATE INDEX " + getTableName() + "_left ON " + getTableName() + "(colLeft);" +
					" CREATE INDEX " + getTableName() + "_right ON " + getTableName() + "(colRight);");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colLeft, colRight) VALUES (?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLInverseObjectPropertiesAxiom) {
			OWLInverseObjectPropertiesAxiom naxiom = (OWLInverseObjectPropertiesAxiom) axiom;
			if (naxiom.getFirstProperty().isAnonymous()) {
				addStatement.setString(1, nidMapper.get(naxiom.getFirstProperty()).toString());
				handleAddAnonymousObjectPropertyExpression(naxiom.getFirstProperty());
			} else {
				addStatement.setString(1, naxiom.getFirstProperty().asOWLObjectProperty().getIRI().toString());
			}
			if (naxiom.getSecondProperty().isAnonymous()) {
				addStatement.setString(2, nidMapper.get(naxiom.getSecondProperty()).toString());
				handleAddAnonymousObjectPropertyExpression(naxiom.getSecondProperty());
			} else {
				addStatement.setString(2, naxiom.getSecondProperty().asOWLObjectProperty().getIRI().toString());
			}
			
			return AdditionMode.ADD;
		} else {
			throw new U2R3NotImplementedException();
		}
	}
	
	@Override
	public void add(OWLObject ce) {
		if (ce instanceof OWLObjectInverseOf) {
			OWLObjectInverseOf io = (OWLObjectInverseOf) ce;
			try {
				addStatement.setString(1, nidMapper.get(ce).toString());
				if (io.getInverse().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(io.getInverse()).toString());
				} else {
					addStatement.setString(2, io.getInverse().asOWLObjectProperty().getIRI().toString());
				}
				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (io.getInverse().isAnonymous()) {
					handleAddAnonymousObjectPropertyExpression(io.getInverse());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			throw new U2R3NotImplementedException();
		}
	}

	@Override
	public void getSubAxiomLocationImpl(StringBuilder sql, OWLObjectPropertyExpression pe, String tid, String col) {
		if (pe instanceof OWLObjectInverseOf) {
			OWLObjectInverseOf npe = (OWLObjectInverseOf) pe;
			String left = tid + "." + col;
			String right = npe.getNamedProperty().getIRI().toString();
			String ntid = TableId.getId();

			sql.append("SELECT 1");
			sql.append("\n FROM " + getTableName() + " AS " + ntid);
			sql.append("\nWHERE " + ntid + ".colLeft='" + left + "' AND " + ntid + ".colRight='" + right + "'");
		}
		throw new U2R3NotImplementedException();
	}


}
