package de.langenmaier.u2r3.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLObject;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.util.AdditionReason;

public class DatatypeRestrictionRelation extends Relation {
	
	protected DatatypeRestrictionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "datatypeRestriction";
			
			createMainStatement = conn.prepareStatement(getCreateStatement(getTableName()));

			create();
			addStatement = conn.prepareStatement(getAddStatement(getTableName()));
			addListStatement = conn.prepareStatement("INSERT INTO facetList (name, facet, value, type, language) VALUES (?, ?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	protected String getCreateStatement(String table) {
		return "CREATE TABLE " + table + " (" +
		" id BIGINT DEFAULT NEXT VALUE FOR uid NOT NULL," +
		" colClass TEXT," +
		" list TEXT," +
		" PRIMARY KEY (colClass, list));" +
		" CREATE INDEX " + table + "_class ON " + table + "(colClass);" +
		" CREATE INDEX " + table + "_list ON " + table + "(list);";
	}
	
	protected String getAddStatement(String table) {
		return "INSERT INTO " + table + " (colClass, list) VALUES (?, ?)";
	}
	
	@Override
	public void add(OWLObject ce) {
		OWLDatatypeRestriction dtr = (OWLDatatypeRestriction) ce;
		try {
			NodeID nid = NodeID.getNodeID();
			PreparedStatement add = addStatement;

			for(int run=0; run<=0 || (run<=1 && reasoner.isAdditionMode()); add = nextRound(), ++run) {
				add.setString(1, nidMapper.get(ce).toString());
				add.setString(2, nid.toString());
				add.execute();
			}
			if (reasoner.isAdditionMode()) {
				reasonProcessor.add(new AdditionReason(this, new DeltaRelation(this, getDelta())));
			} else {
				reasonProcessor.add(new AdditionReason(this));
			}
			for (OWLFacetRestriction fr : dtr.getFacetRestrictions()) {
				addListStatement.setString(1, nid.toString());
				addListStatement.setString(2, fr.getFacet().getIRI().toString());
				addListStatement.setString(3, fr.getFacetValue().getLiteral());
				
				if (fr.getFacetValue().isOWLTypedLiteral()) {
					addListStatement.setString(4, fr.getFacetValue().asOWLTypedLiteral().getDatatype().getIRI().toString());
					addListStatement.setString(5, "");
				} else {
					addListStatement.setString(5, fr.getFacetValue().asOWLStringLiteral().getLang());
					addListStatement.setString(4, "");
				}
				
				addListStatement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
