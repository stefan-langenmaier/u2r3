package de.langenmaier.u2r3.tests.quality.fzitestcases;

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
		
		if (args.length<=0) {
			System.err.println("USAGE: java " + RunTestCase.class.getName() + " <filename>");
			return;
		}
		String folder = args[0];
		//"/home/sl17/workspace/u2r2/ontologien/tests/fzi/owl2rl";
		String name;
		
		logger.info("Started Testcases");
		
		name = "rdfbased-sem-simple-subgraph-self";
		//name = "rdfbased-sem-rdfs-domain-cond";
		folder = folder + "/" + name;
		runTestCase(name, folder, CheckType.entailment_check);
		
	}
	
	public static void runTestCase(String name, String folder, CheckType checkType) {
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
