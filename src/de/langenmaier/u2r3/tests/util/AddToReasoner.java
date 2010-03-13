package de.langenmaier.u2r3.tests.util;

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
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;

public class AddToReasoner {

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
			Logger logger = Logger.getLogger(AddToReasoner.class);
			logger.info("Java loaded ");
			
			if (args.length<=0) {
				System.err.println("USAGE: java " + AddToReasoner.class.getName() + " <filename>");
				return;
			}
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
			OWLOntology ont;
			ont = manager.loadOntology(IRI.create(args[0]));
			logger.info("OWLAPI loaded " + ont.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(ont);
			//reasoner.loadOntologies(Collections.singleton(ont));
			logger.info("Ontology loaded in DB");
			
			reasoner.prepareReasoner();

			logger.info("FERTIG");
			
			OWLDataFactory df = manager.getOWLDataFactory();
			OWLIndividual ind1 = df.getOWLNamedIndividual(IRI.create("http://test/#test_ind"));
			OWLClass clazz =  df.getOWLClass(IRI.create("http://test/#test_class"));
			OWLClassAssertionAxiom ax = df.getOWLClassAssertionAxiom(clazz, ind1);
			AddAxiom add = new AddAxiom(ont, ax);
			manager.applyChange(add);
			
			reasoner.prepareReasoner();

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

	}

}
