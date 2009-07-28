package de.langenmaier.u2r3.tests.util;

import java.net.URI;

import org.apache.log4j.BasicConfigurator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.profiles.OWLProfileReport;
import org.semanticweb.owl.profiles.RLProfile;

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
	
			RLProfile profile = new RLProfile();
			OWLProfileReport report = profile.checkOntology(ontology, manager);
			
			if (!report.isInProfile()) { new Exception("OWL file is not in RL Profile!"); }
			
			OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder();
			for(OWLAxiom ax : ontology.getAxioms()) {
				//System.out.println(ax);
				//System.out.println(ax.getAxiomType() + ": " + ax.getAxiomType().getIndex());
				//for(OWLEntity e : ax.getSignature()) {
				//	System.out.println("  " + e.getURI());
				//}
				ax.accept(axiomAdder);
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
