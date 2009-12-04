package de.langenmaier.u2r3.tests.rules.owl2rl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class RunTestCase {
	
	static Logger logger = Logger.getLogger(RunTestCase.class);
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if(new File("log4j.properties").exists()) {
			PropertyConfigurator.configure("log4j.properties");
		} else {
			BasicConfigurator.configure();
		}

		
		if (args.length<=0) {
			System.err.println("USAGE: java " + RunTestCase.class.getName() + " <folder> [<name> [<number>]]");
			return;
		}
		
		String folder = null;
		String name = null;
		String number = null;
		
		if (args.length >= 1) {
			folder = args[0];
		}
		if (args.length >= 2) {
			name = args[1];
		}
		if (args.length >= 3) {
			number = args[2];
		}
		
		if (args.length == 1) {
			runTestCase(folder);
		}
		if (args.length == 2) {
			runTestCase(folder, name);
		}
		if (args.length == 3) {
			runTestCase(folder, name, number);
		}
	}
	
	public static void runTestCase(String folder) {
		File dir = new File(folder);
		
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() && !file.isHidden();
			}
		};
		
		File[] files = dir.listFiles(fileFilter);
		
		for (File f : files) {
			runTestCase(folder, f.getName());
		}
		
	}
	
	public static void runTestCase(String folder, String name) {
		File dir = new File(folder + "/" + name);
		
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				boolean isInt = true;
				try {
					Integer.parseInt(file.getName());
				} catch (NumberFormatException ex) {
					isInt = false;
				}
				return file.isDirectory() && !file.isHidden() && isInt;
			}
		};
		
		File[] files = dir.listFiles(fileFilter);
		
		for (File f : files) {
			runTestCase(folder, name, f.getName());
		}
		
	}
	
	public static void runTestCase(String folder, String name, String number) {
		String path = folder + "/" + name + "/" + number +"/" + number + ".owl";
		
		File f = new File(path);
		
		if (!f.exists()) {
			runEntailmentTestCase(folder, name, number);
		} else {
			runConsistencyTestCase(folder, name, number);
		}
	}
	
	public static void runEntailmentTestCase(String folder, String name, String number) {
		try {
			
			folder = "file://" + folder + "/" + name + "/" + number + "/";
			String premise_uri = folder + number + ".premise.owl";
			String conclusion_uri = folder + number + ".conclusion.owl";
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			OWLOntology premise;
			premise = manager.loadOntologyFromPhysicalURI(URI.create(premise_uri));
			logger.debug("Loaded " + premise.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, null);
			reasoner.getSettings().setDeltaIteration(DeltaIteration.COLLECTIVE);
			reasoner.getSettings().checkProfile(false);
			reasoner.loadOntologies(Collections.singleton(premise));
		
			reasoner.classify();
			//System.exit(0);

			OWLOntology conclusion;
			conclusion = manager.loadOntologyFromPhysicalURI(URI.create(conclusion_uri));
			logger.debug("Loaded " + conclusion.getOntologyID());
			
			AxiomChecker axiomChecker = new AxiomChecker(reasoner);
			for(OWLAxiom ax : conclusion.getLogicalAxioms()) {
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
			
			folder = "file://" + folder + "/" + name + "/" + number + "/";
			String ont_uri = folder +  number + ".owl";

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			OWLOntology ont;
			ont = manager.loadOntologyFromPhysicalURI(URI.create(ont_uri));
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, null);
			reasoner.getSettings().setDeltaIteration(DeltaIteration.COLLECTIVE);
			reasoner.getSettings().checkProfile(false);
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
	

}
