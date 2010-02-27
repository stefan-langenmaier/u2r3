package de.langenmaier.u2r3.tests.benchmark;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
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
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.langenmaier.u2r3.tests.util.LoadReasoner;

public class TimeHermitDomus {

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
			
			String file_uri = "file:///home/stefan/.workspace/u2r2/ontologien/DomusAG.owl";
			String ONTO_URI = "http://www.polizei.hessen.de/CRIME/DomusAG#";
			
			Logger.getRootLogger().setLevel(Level.INFO);
			Logger logger = Logger.getLogger(LoadReasoner.class);
			logger.info("Java loaded ");
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			
			OWLOntology ont;
			ont = manager.loadOntology(IRI.create(file_uri));
			logger.info("OWLAPI loaded " + ont.getOntologyID());
			
			ReasonerFactory rf = new ReasonerFactory();
			// The factory can now be used to obtain an instance of HermiT as an OWLReasoner. 
			OWLReasoner reasoner = rf.createReasoner(ont);

			logger.info("Ontology loaded in DB");
			
			reasoner.prepareReasoner();

			logger.info("FINISHED");

			long start = System.currentTimeMillis();
			OWLClass test = factory.getOWLClass(IRI.create(ONTO_URI + "Test"));
			NodeSet<OWLNamedIndividual> kandidaten = reasoner.getInstances(test, false);
			System.out.println("TEST: " + (System.currentTimeMillis() - start) +
				" no of results: " + kandidaten.getFlattened().size());

			// Addition
			OWLNamedIndividual newnid = factory.getOWLNamedIndividual(IRI.create(ONTO_URI + "TEST_IND"));
			OWLClassAssertionAxiom newca = factory.getOWLClassAssertionAxiom(test, newnid);
			AddAxiom add = new AddAxiom(ont, newca);
			start = System.currentTimeMillis();
			manager.applyChange(add);
			
			reasoner.prepareReasoner();
			System.out.println("TEST: " + (System.currentTimeMillis() - start));
			
			start = System.currentTimeMillis();
			kandidaten = reasoner.getInstances(test, false);
			System.out.println("TEST: " + (System.currentTimeMillis() - start) +
				" no of results: " + kandidaten.getFlattened().size());

			//Deletion
			RemoveAxiom del = new RemoveAxiom(ont, newca);
			start = System.currentTimeMillis();
			manager.applyChange(del);
			
			reasoner.prepareReasoner();
			System.out.println("TEST: " + (System.currentTimeMillis() - start));
			
			start = System.currentTimeMillis();
			kandidaten = reasoner.getInstances(test, false);
			System.out.println("TEST: " + (System.currentTimeMillis() - start) +
				" no of results: " + kandidaten.getFlattened().size());

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

}