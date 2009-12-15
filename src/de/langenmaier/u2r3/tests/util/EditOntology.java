package de.langenmaier.u2r3.tests.util;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.inference.OWLReasonerException;
import org.semanticweb.owlapi.inference.OWLReasonerFactory;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
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
			
			System.out.println("changes:" + remover.getChanges());
			
			try {
				manager.applyChanges(remover.getChanges());
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
			
			logger.info("r removed");
			
			reasoner.classify();
			
			logger.info("reclassified");
			
			OWLOntology ontAdd;
			ontAdd = manager.loadOntologyFromPhysicalURI(URI.create("file:///home/sl17/workspace/u2r2/ontologien/owl2rl-t1-r-removed.owl"));
			
			logger.info("Loaded " + ontAdd.getOntologyID());
			
			List<OWLOntologyChange> addChanges = new LinkedList<OWLOntologyChange>();
			
			for (OWLAxiom ax : ontAdd.getLogicalAxioms()) {
				System.out.println(ax);
				AddAxiom addition = new AddAxiom(ont, ax);
				addChanges.add(addition);
				//ax.accept(adder);
			}
			
			System.out.println(addChanges);
			try {
				manager.applyChanges(addChanges);
			} catch (OWLOntologyChangeException e) {
				e.printStackTrace();
			}
			
			logger.info("ontology changes added");
			
			reasoner.classify();
			
			logger.info("changes reclassified");

			logger.info("FERTIG");

			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}

	}

}
