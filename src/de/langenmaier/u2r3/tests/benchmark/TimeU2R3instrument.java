package de.langenmaier.u2r3.tests.benchmark;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class TimeU2R3instrument {

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
			Logger.getRootLogger().setLevel(Level.INFO);
			Logger logger = Logger.getLogger(TimeU2R3instrument.class);
			logger.info("Java loaded ");

			String file_uri = "file:///home/stefan/.workspace/u2r2/ontologien/owl2rl/gcmd-instrument.owl";
			String ONTO_URI = "http://geobrain.laits.gmu.edu/ontology/2004/11/gcmd-instrument.owl#";
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();

			OWLOntology ont;
			ont = manager.loadOntology(IRI.create(file_uri));
			logger.info("OWLAPI loaded " + ont.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(ont);
			
			logger.info("Ontology loaded in DB");
			
			reasoner.prepareReasoner();

			logger.info("FERTIG");
			
			long start = System.currentTimeMillis();
			OWLClass sub = factory.getOWLClass(IRI.create(ONTO_URI + "CAMERA"));
			OWLClass sup = factory.getOWLClass(IRI.create(ONTO_URI + "Instrument"));
			OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(sub, sup);
			boolean isEntailed = reasoner.isEntailed(ax);
			System.out.println("CAMERA <= Instrument: " + (System.currentTimeMillis() - start) +
				" results: " + isEntailed);

			// Addition
			OWLClass newcls = factory.getOWLClass(IRI.create(ONTO_URI + "TEST_CLS"));
			ax = factory.getOWLSubClassOfAxiom(newcls, sup);
			AddAxiom add = new AddAxiom(ont, ax);
			manager.applyChange(add);
			
			reasoner.prepareReasoner();
			
			start = System.currentTimeMillis();
			isEntailed = reasoner.isEntailed(ax);
			System.out.println("CAMERA <= Instrument: " + (System.currentTimeMillis() - start) +
				" results: " + isEntailed);

			//Deletion
			if (reasoner.getSettings().getDeletionType() == DeletionType.CASCADING) {
				RemoveAxiom del = new RemoveAxiom(ont, ax);
				manager.applyChange(del);
				
				reasoner.prepareReasoner();
				
				start = System.currentTimeMillis();

				isEntailed = reasoner.isEntailed(ax);
				System.out.println("CAMERA <= Instrument: " + (System.currentTimeMillis() - start) +
					" results: " + isEntailed);

			}
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

	}

}