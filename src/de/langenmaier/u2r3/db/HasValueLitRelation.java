package de.langenmaier.u2r3.db;

import java.sql.SQLException;
import java.sql.Types;

import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLStringLiteral;
import org.semanticweb.owlapi.model.OWLTypedLiteral;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.AdditionReason;
import de.langenmaier.u2r3.util.DatatypeCheck;

public class HasValueLitRelation extends Relation {
	
	protected HasValueLitRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "hasValueLit";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
					" colClass TEXT," +
					" property TEXT, " +
					" value TEXT," +
					" language TEXT," +
					" type TEXT," +
					" PRIMARY KEY (id, colClass, property, value));" +
					" CREATE INDEX " + getTableName() + "_class ON " + getTableName() + "(colClass);" +
					" CREATE INDEX " + getTableName() + "_property ON " + getTableName() + "(property);" +
					" CREATE INDEX " + getTableName() + "_value ON " + getTableName() + "(value);");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (colClass, property, value, language, type) VALUES (?, ?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLDataHasValue) {
				OWLDataHasValue hv = (OWLDataHasValue) ce;
				addStatement.setString(1, nidMapper.get(ce).toString());
				
				if (hv.getProperty().isAnonymous()) {
					addStatement.setString(2, nidMapper.get(hv.getProperty()).toString());
				} else {
					addStatement.setString(2, hv.getProperty().asOWLDataProperty().getIRI().toString());
				}
				
				if (hv.getValue().isOWLTypedLiteral()) {
					OWLTypedLiteral tl = (OWLTypedLiteral) hv.getValue();
					addStatement.setString(3, DatatypeCheck.validateType(tl.getLiteral(), tl.getDatatype()));
					addStatement.setString(5, tl.getDatatype().getIRI().toString());
					addStatement.setNull(4, Types.LONGVARCHAR);
				} else {
					OWLStringLiteral sl = (OWLStringLiteral) hv.getValue();
					addStatement.setString(3, sl.getLiteral());
					addStatement.setString(5, OWLRDFVocabulary.RDF_PLAIN_LITERAL.getIRI().toString());
					addStatement.setString(4, sl.getLang());
				}

				addStatement.execute();
				reasonProcessor.add(new AdditionReason(this));
				
				if (hv.getProperty().isAnonymous()) {
					handleAddAnonymousDataPropertyExpression(hv.getProperty());
				}
			} else {
				throw new U2R3NotImplementedException();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
