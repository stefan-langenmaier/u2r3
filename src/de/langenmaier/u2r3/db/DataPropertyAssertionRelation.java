package de.langenmaier.u2r3.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.exceptions.U2R3NotImplementedException;
import de.langenmaier.u2r3.util.Pair;

public class DataPropertyAssertionRelation extends Relation {
	static Logger logger = Logger.getLogger(DataPropertyAssertionRelation.class);
	
	protected DataPropertyAssertionRelation(U2R3Reasoner reasoner) {
		super(reasoner);
		try {
			tableName = "dataPropertyAssertion";
			
			createMainStatement = conn.prepareStatement("CREATE TABLE " + getTableName() + " (" +
					" id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" language TEXT NULL,"+
					" type TEXT NULL,"+
					" PRIMARY KEY (subject, property, object))");
			dropMainStatement = conn.prepareStatement("DROP TABLE " + getTableName() + " IF EXISTS ");

			create();
			addStatement = conn.prepareStatement("INSERT INTO " + getTableName() + " (subject, property, object, language, type) VALUES (?, ?, ?, ?, ?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public AdditionMode addImpl(OWLAxiom axiom) throws SQLException {
		if (axiom instanceof OWLDataPropertyAssertionAxiom) {
			OWLDataPropertyAssertionAxiom naxiom = (OWLDataPropertyAssertionAxiom) axiom;
			if (naxiom.getSubject().isAnonymous()) {
				addStatement.setString(1, naxiom.getSubject().asAnonymousIndividual().toStringID());
			} else {
				addStatement.setString(1, naxiom.getSubject().asNamedIndividual().getIRI().toString());
			}
			addStatement.setString(2, naxiom.getProperty().asOWLDataProperty().getIRI().toString());
			addStatement.setString(3, naxiom.getObject().getLiteral());
			if (!naxiom.getObject().isTyped()) {
				addStatement.setString(4, naxiom.getObject().asRDFTextLiteral().getLang());
				addStatement.setNull(5, Types.LONGVARCHAR);
			} else {
				addStatement.setNull(4, Types.LONGVARCHAR);
				addStatement.setString(5, naxiom.getObject().asOWLStringLiteral().getDatatype().getIRI().toString());
			}
			
		}
		return AdditionMode.ADD;
	}

	@Override
	public void createDeltaImpl(int id) {
		try {
			dropDelta(id);
			//max 3 Quellen
			createDeltaStatement.execute("CREATE TABLE " + getDeltaName(id) + "" +
					" (id UUID DEFAULT RANDOM_UUID() NOT NULL UNIQUE," +
					" subject TEXT," +
					" property TEXT," +
					" object TEXT," +
					" language TEXT," +
					" sourceId1 UUID," +
					" sourceTable1 VARCHAR(100)," +
					" sourceId2 UUID," +
					" sourceTable2 VARCHAR(100)," +
					" sourceId3 UUID," +
					" sourceTable3 VARCHAR(100)," +
					" PRIMARY KEY (subject, property, object))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void merge(DeltaRelation delta) {
		//FIXME  hier kommen Daten an die behandelt werden sollten
		//throw new U2R3NotImplementedException();
	}

	@Override
	public Pair<UUID, RelationName> removeImpl(OWLAxiom axiom)
			throws SQLException {
		throw new U2R3NotImplementedException();
	}

	@Override
	protected String existsImpl(String... args) {
		if (args.length == 3) {
			return "SELECT '1' FROM " + getTableName() + " WHERE subject = '" + args[0] + "' AND property = '" + args[1] + "' AND object ='" + args[2] + "'";
		} else if (args.length == 4) {
			return "SELECT '1' FROM " + getTableName() + " WHERE subject = '" + args[0] + "' AND property = '" + args[1] + "' AND object ='" + args[2] + "' AND (language = '" + args[3] + "' OR language IS NULL)";
		}
		throw new U2R3NotImplementedException();
	}

	public Set<OWLLiteral> getRelatedValues(OWLNamedIndividual ni,
			OWLDataProperty dp) throws OWLReasonerException {
		try {
			StringBuilder sql = new StringBuilder();
		
			Statement stmt = conn.createStatement();
			ResultSet rs;
			
			Set<OWLLiteral> ret = new HashSet<OWLLiteral>();
			
			
			sql.append("SELECT object, language, type");
			sql.append("\nFROM " + getTableName());
			sql.append("\nWHERE subject = '" + ni.getIRI().toString() + "' AND property = '" + dp.getIRI().toString() + "'");
			
			rs = stmt.executeQuery(sql.toString());
			
			while(rs.next()) {
				String lit = rs.getString("object");
				String type = rs.getString("type");
				//TODO Sprache oder Typ erzeugen
				if (type == null) {
					ret.add(dataFactory.getOWLStringLiteral(lit));
				} else {
					ret.add(dataFactory.getOWLTypedLiteral(lit));
				}
				
			}
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
