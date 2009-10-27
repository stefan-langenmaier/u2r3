package de.langenmaier.u2r3.tests.util;

import java.net.URI;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;

/**
 * Tools to check OWL files for testing purposes.
 * @author Stefan Langenmaier
 *
 */
public class CheckOWL2RL {

	/**
	 * Checks if the specified file is in the OWL2 RL Fragment
	 * 
	 * @author Stefan Langenmaier
	 * @param args OWL file
	 * 
	 */
	public static void main(String[] args) {
		try {
			if (args.length<=0) {
				System.out.println("USAGE: java " + CheckOWL2RL.class.getName() + " <filename>");
				return;
			}
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			URI physicalURI = URI.create(args[0]);
			OWLOntology ontology = manager.loadOntologyFromPhysicalURI(physicalURI);

			OWL2RLProfile profile = new OWL2RLProfile();
			OWLProfileReport report = profile.checkOntology(ontology);
			
			System.out.println("Is " + physicalURI.toString() + " in OWL2 RL?");
			System.out.println(report.isInProfile());
			if (!report.isInProfile()) {
				System.out.println("Number of violations: " + report.getViolations().size());
			}
			for(OWLProfileViolation violation : report.getViolations()) {
				System.out.println(violation.toString());
			}
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}


}
