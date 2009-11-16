package de.langenmaier.u2r3.tests.rules.owl2rl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
import de.langenmaier.u2r3.tests.rules.AxiomChecker;
import de.langenmaier.u2r3.tests.rules.CheckType;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class RunTestCase {
	
	static Logger logger = Logger.getLogger(RunTestCase.class);
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		
		if (args.length<=2) {
			System.err.println("USAGE: java " + RunTestCase.class.getName() + " <folder> <name> <number>");
			return;
		}
		String folder = args[0];
		String name = args[1];
		String number = args[2];
		
		String path = folder + "/" + name + "/" + number + ".owl";
		
		File f = new File(path);
		
		if (!f.exists()) {
			runEntailmentTestCase(folder, name, number);
		} else {
			runConsistencyTestCase(folder, name, number);
		}
		//runTestCase(folder, name, number, type);
	}
	
	public static void runEntailmentTestCase(String folder, String name, String number) {
		try {
			
			folder = "file://" + folder + "/" + name + "/";
			String premise_uri = folder + number + ".premise.owl";
			String conclusion_uri = folder + number + ".conclusion.owl";
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			OWLOntology premise;
			premise = manager.loadOntologyFromPhysicalURI(URI.create(premise_uri));
			logger.debug("Loaded " + premise.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, null);
			reasoner.getSettings().setDeltaIteration(DeltaIteration.COLLECTIVE);
			//reasoner.getSettings().checkProfile(false);
			reasoner.loadOntologies(Collections.singleton(premise));
		
			reasoner.classify();
			//System.exit(0);

			OWLOntology conclusion;
			conclusion = manager.loadOntologyFromPhysicalURI(URI.create(conclusion_uri));
			logger.debug("Loaded " + conclusion.getOntologyID());
			
			AxiomChecker axiomChecker = new AxiomChecker(reasoner);
			for(OWLAxiom ax : conclusion.getAxioms()) {
				ax.accept(axiomChecker);
			}
			
			if (!axiomChecker.isCorrect()) {
				logger.error("Fehler in Testfall <" + name + ">!");
			} else {
				logger.info("Tesfall <" + name + "> okay.");
			}

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}
	
	public static void runConsistencyTestCase(String folder, String name, String number) {
		try {
			
			folder = "file://" + folder + "/" + name + "/";
			String ont_uri = folder +  number + ".owl";

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			OWLOntology ont;
			ont = manager.loadOntologyFromPhysicalURI(URI.create(ont_uri));
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, null);
			reasoner.getSettings().setDeltaIteration(DeltaIteration.COLLECTIVE);
			//reasoner.getSettings().checkProfile(false);
			reasoner.loadOntologies(Collections.singleton(ont));
		
			reasoner.classify();

			if (!reasoner.isConsistent(ont)) {
				logger.info("Tesfall <" + name + "> okay.");
			} else {
				logger.error("Fehler in Testfall <" + name + ">!");
			}

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}
	
	public static void runTestCase(String folder, String name, String number, CheckType checkType) {
		try {
			
			folder = "file://" + folder + "/";
			String premise_uri = folder +  name + ".premisegraph.ttl";
			if (checkType == CheckType.consistency_check) {
				premise_uri = folder + name + ".graph.ttl";
			}
			String conclusion_uri = folder + name + ".conclusiongraph.ttl";
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			OWLOntology premise;
			premise = manager.loadOntologyFromPhysicalURI(URI.create(premise_uri));
			logger.debug("Loaded " + premise.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, null);
			reasoner.getSettings().setDeltaIteration(DeltaIteration.COLLECTIVE);
			//reasoner.getSettings().checkProfile(false);
			reasoner.loadOntologies(Collections.singleton(premise));
		
			reasoner.classify();

			if (checkType == CheckType.entailment_check) {
				OWLOntology conclusion;
				conclusion = manager.loadOntologyFromPhysicalURI(URI.create(conclusion_uri));
				logger.debug("Loaded " + conclusion.getOntologyID());
				
				AxiomChecker axiomChecker = new AxiomChecker(reasoner);
				for(OWLAxiom ax : conclusion.getAxioms()) {
					ax.accept(axiomChecker);
				}
				
				if (!axiomChecker.isCorrect()) {
					logger.error("Fehler in Testfall <" + name + ">!");
				} else {
					logger.info("Tesfall <" + name + "> okay.");
				}
			} else {
				if (!reasoner.isConsistent(premise)) {
					logger.info("Tesfall <" + name + "> okay.");
				} else {
					logger.error("Fehler in Testfall <" + name + ">!");
				}
			}
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}
	}

}