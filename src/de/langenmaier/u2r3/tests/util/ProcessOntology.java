package de.langenmaier.u2r3.tests.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.langenmaier.u2r3.Reason;
import de.langenmaier.u2r3.ReasonProcessor;
import de.langenmaier.u2r3.db.RelationManager;
import de.langenmaier.u2r3.db.RelationManager.RelationName;
import de.langenmaier.u2r3.rules.TransSubClassRule;
import de.langenmaier.u2r3.util.Settings;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

/**
 * Processes axioms that are stored in a database. So it assumes tables and content are set up.
 * @author stefan
 *
 */
public class ProcessOntology {
	static Logger logger = Logger.getLogger(TransSubClassRule.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Settings.startClean(false);
		Settings.setDeltaIteration(DeltaIteration.COLLECTIVE);
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		
		ReasonProcessor rp = ReasonProcessor.getReasonProcessor();
		Reason r = new Reason(RelationManager.getRelation(RelationName.subClass));
		rp.add(r);
		
		rp.classify();
	}

}
