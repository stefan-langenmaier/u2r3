package de.langenmaier.u2r3.tests.util;

import java.net.URI;
import java.util.Collections;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;
import de.langenmaier.u2r3.util.Settings.DeletionType;
import de.langenmaier.u2r3.util.Settings.DeltaIteration;

public class EditOntology {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.INFO);
			Logger logger = Logger.getLogger(EditOntology.class);
			
			if (args.length<=0) {
				System.err.println("USAGE: java " + EditOntology.class.getName() + " <filename>");
				return;
			}
			
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
			OWLOntology ont;
			ont = manager.loadOntologyFromPhysicalURI(URI.create(args[0]));
			logger.info("Loaded " + ont.getOntologyID());
			
			OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
			U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(manager, null);
			reasoner.getSettings().setDeltaIteration(DeltaIteration.COLLECTIVE);
			reasoner.getSettings().setDeletionType(DeletionType.CASCADING);
			reasoner.loadOntologies(Collections.singleton(ont));
			reasoner.classify();
			
			logger.info("Classified");
			
			OWLEntityRemover remover = new OWLEntityRemover(manager, Collections.singleton(ont));
			
			OWLObjectProperty ope = manager.getOWLDataFactory().getOWLObjectProperty(IRI.create("http://www.informatik.uni-ulm.de/ki/Liebig/owl/owl2rl-t1.owl#r"));
			
			ope.accept(remover);
			
			System.out.println(remover.getChanges());
			
			try {
				manager.applyChanges(remover.getChanges());
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}

			logger.info("FERTIG");

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}

	}

}
