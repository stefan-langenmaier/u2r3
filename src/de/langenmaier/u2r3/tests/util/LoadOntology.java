package de.langenmaier.u2r3.tests.util;

import java.net.URI;

import org.apache.log4j.BasicConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;

import de.langenmaier.u2r3.owl.OWL2RLDBAdder;

/**
 * This test class loads an ontology file into a h2 database
 * @author stefan
 *
 */
public class LoadOntology {

	/**
	 * @param args Absolute file path to a OWL2 RL File
	 */
	public static void main(String[] args) {
		try {
			BasicConfigurator.configure();

			if (args.length<=0) {
				System.err.println("USAGE: java " + CheckOWL2RL.class.getName() + " <filename>");
				return;
			}
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			URI physicalURI = URI.create(args[0]);
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(physicalURI);
	
			OWL2RLProfile profile = new OWL2RLProfile();
			OWLProfileReport report = profile.checkOntology(ontology, manager);
			
			if (!report.isInProfile()) { new Exception("OWL file is not in RL Profile!"); }
			
			OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder();
			for(OWLAxiom ax : ontology.getAxioms()) {
				ax.accept(axiomAdder);
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
