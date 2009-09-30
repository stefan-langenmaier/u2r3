package de.langenmaier.u2r3.tests.quality.fzitestcases;

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
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class RunTestCase {
	
	static Logger logger = Logger.getLogger(RunTestCase.class);
	enum CheckType {entailment_check, consistency_check};
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		
		String folder = "file:///home/stefan/workspace/u2r3/ontologien/tests/fzi/owl2rl/";
		String name;
		
		logger.info("Started Testcases");
		
		name = "rdfbased-sem-rdfs-domain-cond";
		runTestCase(name, folder, CheckType.entailment_check);
		
		name = "rdfbased-sem-rdfs-range-cond";
		runTestCase(name, folder, CheckType.entailment_check);
		
		//rdfbased-sem-class-nothing-ext
		name = "rdfbased-sem-class-nothing-ext";
		runTestCase(name, folder, CheckType.consistency_check);

	}
	
	public static void runTestCase(String name, String folder, CheckType checkType) {
		try {
			
			
			String premise_uri = folder + name + "/" + name + ".premisegraph.xml";
			if (checkType == CheckType.consistency_check) {
				premise_uri = folder + name + "/" + name + ".graph.xml";
			}
			String conclusion_uri = folder + name + "/" + name + ".conclusiongraph.xml";
			//String metadata_path = "/home/stefan/workspace/u2r3/ontologien/tests/fzi/owl2rl/" + name + "/" + name + ".metadata.properties";
			
			/*Properties prop = new Properties();
			prop.load(new FileInputStream(metadata_path));
			
			System.out.println(prop);
			System.out.println(prop.values());*/
			
			//CheckType checkType = CheckType.entailment_check;
			/*if (prop.getProperty("testcase.type").equals("POSTIVE_ENTAILMENT")) {
				checkType = CheckType.entailment_check;
			} else {
				checkType = CheckType.consistency_check;
			}*/
			
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
