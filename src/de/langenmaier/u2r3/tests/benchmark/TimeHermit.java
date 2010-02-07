package de.langenmaier.u2r3.tests.benchmark;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.langenmaier.u2r3.tests.util.LoadReasoner;

public class TimeHermit {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if ((new File("log4j.properties")).exists()) {
				PropertyConfigurator.configure("log4j.properties");
			} else {
				BasicConfigurator.configure();
			}
			
			if (args.length<=0) {
				System.err.println("USAGE: java " + TimeHermit.class.getName() + " <filename>");
				return;
			}
			
			Logger.getRootLogger().setLevel(Level.INFO);
			Logger logger = Logger.getLogger(LoadReasoner.class);
			logger.info("Java loaded ");
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
			OWLOntology ont;
			ont = manager.loadOntology(IRI.create(args[0]));
			logger.info("OWLAPI loaded " + ont.getOntologyID());
			
			ReasonerFactory factory = new ReasonerFactory();
			// The factory can now be used to obtain an instance of HermiT as an OWLReasoner. 
			OWLReasoner reasoner=factory.createReasoner(ont);

			logger.info("Ontology loaded in DB");
			
			reasoner.prepareReasoner();

			logger.info("FINISHED");

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

}
