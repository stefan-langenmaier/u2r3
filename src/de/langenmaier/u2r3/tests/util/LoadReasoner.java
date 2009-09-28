package de.langenmaier.u2r3.tests.util;

import java.net.URI;
import java.util.Collections;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.inference.OWLReasoner;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.langenmaier.u2r3.core.U2R3ReasonerFactory;

public class LoadReasoner {

	public static final String PHYSICAL_URI = "file:///home/stefan/workspace/u2r3/ontologien/sample.owl";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ALL);
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
			OWLOntology ont;
			ont = manager.loadOntologyFromPhysicalURI(URI.create(PHYSICAL_URI));
			System.out.println("Loaded " + ont.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			OWLReasoner reasoner = reasonerFactory.createReasoner(manager, manager.getImportsClosure(ont));
			
			reasoner.loadOntologies(Collections.singleton(ont));
			reasoner.classify();


			
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
