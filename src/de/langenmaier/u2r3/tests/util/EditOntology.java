package de.langenmaier.u2r3.tests.util;


/**
 * This test class edits an ontology file and the reasoned data in a h2 database
 * 
 * @author stefan
 * 
 */
public class EditOntology {

	/**
	 * @param args
	 *            Absolute file path to a OWL2 RL File
	 */
	public static void main(String[] args) {
		/*try {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.ALL);

			Settings.startClean(false);

			if (args.length <= 0) {
				System.err.println("USAGE: java " + CheckOWL2RL.class.getName()
						+ " <filename>");
				return;
			}
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			URI physicalURI = URI.create(args[0]);
			OWLOntology ontology = manager
					.loadOntologyFromPhysicalURI(physicalURI);

			OWL2RLProfile profile = new OWL2RLProfile();
			OWLProfileReport report = profile.checkOntology(ontology, manager);

			if (!report.isInProfile()) {
				throw new Exception("OWL file is not in RL Profile!");
			}

			RuleManager.initialize();

			OWLAxiom dax = null;
			String uri = null;
			for (OWLAxiom ax : ontology.getAxioms()) {

				if (ax.getAxiomType() == AxiomType.SUBCLASS_OF) {
					uri = ((OWLSubClassOfAxiom) ax).getSubClass().asOWLClass()
							.getURI().toString();
					if (uri
							.equals("http://www.langenmaier.de/u2r3/sample.owl#Book")) {
						System.out.println(ax.toString());
						dax = ax;
					}
					System.out.println(uri);
				}

			}
			RemoveAxiom removeAxiom = new RemoveAxiom(ontology, dax);

			manager.applyChange(removeAxiom);
			U2R3AxiomRemover axiomRemover = new U2R3AxiomRemover();
			dax.accept(axiomRemover);
			
			System.out.println(ReasonProcessor.getReasonProcessor().dump());
			
			URI physicalURI2 = URI.create("file:/tmp/MyOnt2.owl");
			manager.saveOntology(ontology, new DefaultOntologyFormat(),
					physicalURI2);

		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

}
