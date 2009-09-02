package de.langenmaier.u2r3.tests.util;

import org.apache.log4j.BasicConfigurator;

import de.langenmaier.u2r3.Reason;
import de.langenmaier.u2r3.ReasonProcessor;
import de.langenmaier.u2r3.Settings;
import de.langenmaier.u2r3.db.RelationMananger;
import de.langenmaier.u2r3.db.RelationMananger.RelationName;

/**
 * Processes axioms that are stored in a database. So it assumes tables and content are set up.
 * @author stefan
 *
 */
public class ProcessOntology {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Settings.startClean(false);
		BasicConfigurator.configure();
		ReasonProcessor rp = ReasonProcessor.getReasonProcessor();
		rp.add(new Reason(RelationMananger.getRelation(RelationName.subClass)));
		
		rp.classify();
	}

}
