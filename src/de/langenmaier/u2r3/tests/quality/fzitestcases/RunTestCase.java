package de.langenmaier.u2r3.tests.quality.fzitestcases;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;

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
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class RunTestCase {
	
	static Logger logger = Logger.getLogger(RunTestCase.class);
	enum CheckType {entailment_check, consistency_check};
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		
		String folder = null; // = "file:///home/stefan/workspace/u2r3/ontologien/tests/fzi/owl2rl/";
		String name;
		
		File dir = new File("/home/stefan/workspace/u2r3/ontologien/tests/fzi/owl2rl/");
	    
	    // This filter only returns directories
	    FileFilter fileFilter = new FileFilter() {
	        public boolean accept(File file) {
	            return file.isDirectory();
	        }
	    };
	    File[] files = dir.listFiles(fileFilter);
	    
	    for (File f : files) {
	    	name = f.getName();
	    	folder = f.getAbsolutePath();
	    	
	    	Properties prop = new Properties();
			prop.loadFromXML(new FileInputStream(folder + "/" + name + ".metadata.properties"));
			if (prop.getProperty("testcase.type").equals("POSITIVE_ENTAILMENT")) {
				runTestCase(name, folder, CheckType.entailment_check);
			} else {
				runTestCase(name, folder, CheckType.consistency_check);
			}
			
			
	    }

		
		
		logger.info("Started Testcases");
		
		name = "rdfbased-sem-rdfs-domain-cond";
		folder = folder + "/" + name;
		runTestCase(name, folder, CheckType.entailment_check);
		
		name = "rdfbased-sem-rdfs-range-cond";
		folder = folder + "/" + name;
		runTestCase(name, folder, CheckType.entailment_check);
		
		//rdfbased-sem-class-nothing-ext
		name = "rdfbased-sem-class-nothing-ext";
		folder = folder + "/" + name;
		runTestCase(name, folder, CheckType.consistency_check);

	}
	
	public static void runTestCase(String name, String folder, CheckType checkType) {
		try {
			
			folder = "file://" + folder + "/";
			String premise_uri = folder +  name + ".premisegraph.xml";
			if (checkType == CheckType.consistency_check) {
				premise_uri = folder + name + ".graph.xml";
			}
			String conclusion_uri = folder + name + ".conclusiongraph.xml";
			
			System.out.println(premise_uri);
			System.out.println(conclusion_uri);

			
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

			if (checkType == CheckType.entailment_check) {
				OWLOntology conclusion;
				conclusion = manager.loadOntologyFromPhysicalURI(URI.create(conclusion_uri));
				logger.debug("Loaded " + conclusion.getOntologyID());
				
				FZITestAxiomChecker axiomChecker = new FZITestAxiomChecker(reasoner);
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
