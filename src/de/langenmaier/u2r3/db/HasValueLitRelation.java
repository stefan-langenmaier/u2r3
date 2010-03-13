package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
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
		" value TEXT," +
		" language TEXT," +
		" type TEXT," +
		" PRIMARY KEY (id, colClass, property, value));" +
		" CREATE INDEX " + table + "_class ON " + table + "(colClass);" +
		" CREATE INDEX " + table + "_property ON " + table + "(property);" +
		" CREATE INDEX " + table + "_value ON " + table + "(value);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colClass, property, value, language, type) VALUES (?, ?, ?, ?, ?)";
	}
	
	@Override
	public void add(OWLObject ce) {
		try {
			if (ce instanceof  OWLDataHasValue) {
				OWLDataHasValue hv = (OWLDataHasValue) ce;
				PreparedStatement add = addStatement;

				for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
					add.setString(1, nidMapper.get(ce).toString());
					
					if (hv.getProperty().isAnonymous()) {
						add.setString(2, nidMapper.get(hv.getProperty()).toString());
					} else {
						add.setString(2, hv.getProperty().asOWLDataProperty().getIRI().toString());
					}
					
					if (hv.getValue().isOWLTypedLiteral()) {
						OWLTypedLiteral tl = (OWLTypedLiteral) hv.getValue();
						add.setString(3, DatatypeCheck.validateType(tl.getLiteral(), tl.getDatatype()));
						add.setString(5, tl.getDatatype().getIRI().toString());
						add.setNull(4, Types.LONGVARCHAR);
					} else {
						OWLStringLiteral sl = (OWLStringLiteral) hv.getValue();
						add.setString(3, sl.getLiteral());
						add.setString(5, OWLRDFVocabulary.RDF_PLAIN_LITERAL.getIRI().toString());
						add.setString(4, sl.getLang());
					}
	
					add.execute();
				}
				if (reasoner.isAdditionMode()) {
					reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
				} else {
					reasonProcessor.add(new AdditionReason(this));
				}
				
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
