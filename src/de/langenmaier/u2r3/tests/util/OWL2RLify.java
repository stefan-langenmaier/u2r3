package de.langenmaier.u2r3.tests.util;

import org.apache.log4j.BasicConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * This class tries to make an ontology conform to the RL Profile
 * e.g. Protege always creates individuals of type owl:Thing
 *   this is prohibited in the RL Profile
 * This class tries to make an old existing ontology conformant to
 * the RL profile.
 * @author stefan
 *
 */
public class OWL2RLify {

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
			IRI physicalURI = IRI.create(args[0]);
			OWLOntology ontology = manager.loadOntology(physicalURI);
	
			//OWL2RLProfile profile = new OWL2RLProfile();
			//OWLProfileReport report = profile.checkOntology(ontology, manager);
			
			//if (!report.isInProfile()) { throw new Exception("OWL file is not in RL Profile!"); }
			
			//OWL2RLDBAdder axiomAdder = new OWL2RLDBAdder();
			for(OWLAxiom ax : ontology.getAxioms()) {
				System.out.println(ax.toString());
				if (ax.getAxiomType() == AxiomType.CLASS_ASSERTION) {
					OWLClassAssertionAxiom naxiom = (OWLClassAssertionAxiom) ax;
					//System.out.println(naxiom.toString());
					if (naxiom.getClassExpression().isOWLThing()) {
						System.out.println("---DELETE");
						manager.removeAxiom(ontology, naxiom);
					}
				}
				//ax.accept(axiomAdder);
			}
			manager.saveOntology(ontology);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
