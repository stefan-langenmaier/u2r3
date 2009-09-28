package de.langenmaier.u2r3.tests.util;

import java.net.URI;
import java.util.Collections;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class LoadReasoner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ALL);
			Logger logger = Logger.getLogger(LoadReasoner.class);
			
			if (args.length<=0) {
				System.err.println("USAGE: java " + CheckOWL2RL.class.getName() + " <filename>");
				return;
			}
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
			OWLOntology ont;
			ont = manager.loadOntologyFromPhysicalURI(URI.create(args[0]));
			logger.info("Loaded " + ont.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, manager.getImportsClosure(ont));
			reasoner.getSettings().setDeltaIteration(DeltaIteration.IMMEDIATE);
			
			reasoner.loadOntologies(Collections.singleton(ont));
			reasoner.classify();


			
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}

	}

}
