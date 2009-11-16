package de.langenmaier.u2r3.tests.util;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class LoadReasoner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ERROR);
			Logger logger = Logger.getLogger(LoadReasoner.class);
			
			if (args.length<=0) {
				System.err.println("USAGE: java " + LoadReasoner.class.getName() + " <filename>");
				return;
			}
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
			OWLOntology ont;
			ont = manager.loadOntologyFromPhysicalURI(URI.create(args[0]));
			logger.info("Loaded " + ont.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, null);
			reasoner.getSettings().setDeltaIteration(DeltaIteration.COLLECTIVE);
			
			reasoner.loadOntologies(Collections.singleton(ont));
			reasoner.classify();
			
			OWLDataFactory df = reasoner.getDataFactory();
			OWLNamedIndividual ni = df.getOWLNamedIndividual(IRI.create("http://www.langenmaier.de/u2r3/sample.owl#WiMoSkript"));
			
			Set<Set<OWLClass>> res = reasoner.getTypes(ni, true);
			for(Set<OWLClass> res1 : res) {
				for (OWLClass clazz : res1) {
					System.out.println(clazz);
				}
			}

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}

	}

}
