package de.langenmaier.u2r3.tests.util;

import java.net.URI;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.profiles.ConstructNotAllowed;
import org.semanticweb.owl.profiles.OWLProfileReport;
import org.semanticweb.owl.profiles.RLProfile;

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

			RLProfile profile = new RLProfile();
			OWLProfileReport report = profile.checkOntology(ontology, manager);
			
			System.out.println("Is " + physicalURI.toString() + " in OWL2 RL?");
			System.out.println(report.isInProfile());
			
			for(ConstructNotAllowed<?> cna : report.getDisallowedConstructs()) {
				System.out.println(cna.toString());
			}
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}


}
