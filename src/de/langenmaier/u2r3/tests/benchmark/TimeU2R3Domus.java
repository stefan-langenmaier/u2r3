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
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
import de.langenmaier.u2r3.util.Settings.DeletionType;

public class TimeU2R3Domus {

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
			Logger logger = Logger.getLogger(TimeU2R3Domus.class);
			logger.info("Java loaded ");


			String file_uri = "file:///home/stefan/.workspace/u2r2/ontologien/DomusAG-06-03-2010-X1-dtr.owl";
			String ONTO_URI = "http://www.informatik.uni-ulm.de/ki/Liebig/owl2/DomusAG.owx#";
			
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
			OWLClass test = factory.getOWLClass(IRI.create(ONTO_URI + "Test"));
			NodeSet<OWLNamedIndividual> kandidaten = reasoner.getInstances(test, false);
			System.out.println("TEST: " + (System.currentTimeMillis() - start) +
				" no of results: " + kandidaten.getFlattened().size());

			// Addition
			OWLNamedIndividual newnid = factory.getOWLNamedIndividual(IRI.create(ONTO_URI + "TEST_IND"));
			OWLClassAssertionAxiom newca = factory.getOWLClassAssertionAxiom(test, newnid);
			AddAxiom add = new AddAxiom(ont, newca);
			manager.applyChange(add);
			
			reasoner.prepareReasoner();
			
			start = System.currentTimeMillis();
			kandidaten = reasoner.getInstances(test, false);
			System.out.println("TEST: " + (System.currentTimeMillis() - start) +
				" no of results: " + kandidaten.getFlattened().size());

			//Deletion
			if (reasoner.getSettings().getDeletionType() == DeletionType.CASCADING) {
				RemoveAxiom del = new RemoveAxiom(ont, newca);
				manager.applyChange(del);
				
				reasoner.prepareReasoner();
				
				start = System.currentTimeMillis();
				kandidaten = reasoner.getInstances(test, false);
				System.out.println("Walter: " + (System.currentTimeMillis() - start) +
					" no of results: " + kandidaten.getFlattened().size());
			}
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

	}

}
