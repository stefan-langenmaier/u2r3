package de.langenmaier.u2r3.tests.util;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;

import de.langenmaier.u2r3.core.U2R3Reasoner;
import de.langenmaier.u2r3.core.U2R3ReasonerFactory;


public class LoadReasonerTL {

    public static final String LOG_URI = "http://www.polizei.hessen.de/CRIME/DomusAG#";

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            if ((new File("log4j.properties")).exists()) {
                PropertyConfigurator.configure("log4j.properties");
            } else {
                BasicConfigurator.configure();
            }
            Logger.getRootLogger().setLevel(Level.INFO);
            Logger logger = Logger.getLogger(LoadReasoner.class);
            logger.info("Java loaded ");

            if (args.length <= 0) {
                System.err.println("USAGE: java " + LoadReasoner.class.getName() + " <filename>");
                return;
            }

            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            OWLOntology ont;
            ont = manager.loadOntology(IRI.create(args[0]));
            logger.info("OWLAPI loaded " + ont.getOntologyID());

            OWLReasonerFactory reasonerFactory = new U2R3ReasonerFactory();
            U2R3Reasoner reasoner = (U2R3Reasoner) reasonerFactory.createReasoner(ont);
            //reasoner.loadOntologies(Collections.singleton(ont));
            logger.info("Ontology loaded in DB");

            reasoner.prepareReasoner();

            logger.info("FERTIG");

            OWLDataFactory factory = manager.getOWLDataFactory();

            long start = System.currentTimeMillis();
            OWLClass Deutscher = factory.getOWLClass(IRI.create(LOG_URI + "Deutscher"));
            NodeSet<OWLNamedIndividual> ind2 = reasoner.getInstances(Deutscher, false);
            System.out.println("Deutscher / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind2.getFlattened().size());

            OWLClass orte = factory.getOWLClass(IRI.create(LOG_URI + "Ort"));
            start = System.currentTimeMillis();
            NodeSet<OWLNamedIndividual> ind = reasoner.getInstances(orte, true);
            System.out.println("Orte / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind.getFlattened().size());
            //for (OWLNamedIndividual in : ind.getFlattened()) {
            //    System.out.println("    " + in);
            //}

            start = System.currentTimeMillis();
            OWLClass Osteurop = factory.getOWLClass(IRI.create(LOG_URI + "Osteuropaeer"));
            NodeSet<OWLNamedIndividual> ind3 = reasoner.getInstances(Osteurop, false);
            System.out.println("Osteuropaeer / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind3.getFlattened().size());

            start = System.currentTimeMillis();
            OWLClass ueber18 = factory.getOWLClass(IRI.create(LOG_URI + "Ueber18"));
            NodeSet<OWLNamedIndividual> ind4 = reasoner.getInstances(ueber18, false);
            System.out.println("Ueber18 / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind4.getFlattened().size());

            start = System.currentTimeMillis();
            OWLClass AdrFrankfurt = factory.getOWLClass(IRI.create(LOG_URI + "AdrFrankfurt"));
            NodeSet<OWLNamedIndividual> ind5 = reasoner.getInstances(AdrFrankfurt, false);
            System.out.println("AdrFrankfurt / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind5.getFlattened().size());

            start = System.currentTimeMillis();
            OWLClass Verwandte = factory.getOWLClass(IRI.create(LOG_URI + "Verwandte-von-Afehefura"));
            NodeSet<OWLNamedIndividual> ind6 = reasoner.getInstances(Verwandte, false);
            System.out.println("Verwandte-von-Afehefura / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind6.getFlattened().size());

            start = System.currentTimeMillis();
            @SuppressWarnings("unused")
            OWLObjectProperty lib = factory.getOWLObjectProperty(IRI.create(LOG_URI + "liegt-in-bundesland"));
            OWLObjectProperty inv_lib = factory.getOWLObjectProperty(IRI.create(LOG_URI + "hat-orte"));
            OWLNamedIndividual hessen = factory.getOWLNamedIndividual(IRI.create(LOG_URI + "Hessen"));
            ind = reasoner.getObjectPropertyValues(hessen, inv_lib);
            //ind = reasoner.getObjectPropertyValues(hessen, lib.getInverseProperty());
            System.out.println("Orte in Hessen / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind.getFlattened().size());
            //for (OWLNamedIndividual in : ind.getFlattened()) {
            //    System.out.println("    " + in);
            //}

                                    start = System.currentTimeMillis();
            OWLClass AdrHessen = factory.getOWLClass(IRI.create(LOG_URI + "AdrHessen"));
            NodeSet<OWLNamedIndividual> ind0 = reasoner.getInstances(AdrHessen, false);
            System.out.println("AdrHessen / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind0.getFlattened().size());

                        start = System.currentTimeMillis();
            OWLClass StraftH = factory.getOWLClass(IRI.create(LOG_URI + "StraftatHessen"));
            NodeSet<OWLNamedIndividual> ind7 = reasoner.getInstances(StraftH, false);
            System.out.println("StraftatHessen / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind7.getFlattened().size());

                                    start = System.currentTimeMillis();
            OWLClass Auslaender = factory.getOWLClass(IRI.create(LOG_URI + "Auslaender"));
            NodeSet<OWLNamedIndividual> ind8 = reasoner.getInstances(Auslaender, false);
            System.out.println("Auslaender / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind8.getFlattened().size());
            
            start = System.currentTimeMillis();
            //OWLNamedIndividual indi = factory.getOWLNamedIndividual(IRI.create(LOG_URI + "I1154852"));
            OWLNamedIndividual indi = factory.getOWLNamedIndividual(IRI.create(LOG_URI + "I1016426"));

            OWLObjectProperty verw = factory.getOWLObjectProperty(IRI.create(LOG_URI + "verwandt-mit"));
            NodeSet<OWLNamedIndividual> xyz = reasoner.getObjectPropertyValues(indi, verw);
            System.out.println("Verw. von I1016426 / Time: " + (System.currentTimeMillis() - start) / 1000.0 + " no of results: " + xyz.getFlattened().size());


            OWLClass verd = factory.getOWLClass(IRI.create(LOG_URI + "Verdaechtiger"));
            OWLClass frau = factory.getOWLClass(IRI.create(LOG_URI + "Frau"));
            OWLClassExpression verdfrau = factory.getOWLObjectIntersectionOf(verd, frau);
            OWLClass neu = factory.getOWLClass(IRI.create(LOG_URI + "Neu"));
            OWLDeclarationAxiom neudecl = factory.getOWLDeclarationAxiom(neu);
            AddAxiom addAx1 = new AddAxiom(ont, neudecl);
            OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(verdfrau, neu);
            AddAxiom addAx2 = new AddAxiom(ont, ax);
            manager.applyChange(addAx1);
            manager.applyChange(addAx2);

            OWL2RLProfile profile = new OWL2RLProfile();
			OWLProfileReport report = profile.checkOntology(ont);
            System.out.print("Is in OWL2 RL? ");
			System.out.println(report.isInProfile());
			if (!report.isInProfile()) {
				System.out.println("Number of violations: " + report.getViolations().size());
			}
			for(OWLProfileViolation violation : report.getViolations()) {
				System.out.println(violation.toString());
			}

            start = System.currentTimeMillis();
            reasoner.prepareReasoner();
            NodeSet<OWLNamedIndividual> ind_neu = reasoner.getInstances(neu, false);
            System.out.println("Neu / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind_neu.getFlattened().size());

            OWLObjectProperty adr = factory.getOWLObjectProperty(IRI.create(LOG_URI + "hat-adresse"));
            OWLObjectProperty ort = factory.getOWLObjectProperty(IRI.create(LOG_URI + "ort"));
            OWLObjectProperty bliegt = factory.getOWLObjectProperty(IRI.create(LOG_URI + "liegt-in-bundesland"));

            OWLClassExpression orthes = factory.getOWLObjectHasValue(bliegt, hessen);
            OWLClassExpression someoh = factory.getOWLObjectSomeValuesFrom(ort, orthes);
            OWLClassExpression hasvh = factory.getOWLObjectSomeValuesFrom(adr, someoh);
            //@SuppressWarnings("unused")
            OWLClassExpression verdadr = factory.getOWLObjectIntersectionOf(verd, hasvh);
            OWLClass neu2 = factory.getOWLClass(IRI.create(LOG_URI + "Neu2"));
            OWLDeclarationAxiom neu2decl = factory.getOWLDeclarationAxiom(neu2);
            AddAxiom addAx3 = new AddAxiom(ont, neu2decl);
            OWLSubClassOfAxiom ax5 = factory.getOWLSubClassOfAxiom(verdadr, neu2);
            AddAxiom addAx4 = new AddAxiom(ont, ax5);
            manager.applyChange(addAx3);
            manager.applyChange(addAx4);

            System.out.print("Is in OWL2 RL? ");
			System.out.println(report.isInProfile());
			if (!report.isInProfile()) {
				System.out.println("Number of violations: " + report.getViolations().size());
			}
			for(OWLProfileViolation violation : report.getViolations()) {
				System.out.println(violation.toString());
			}

            start = System.currentTimeMillis();
            reasoner.prepareReasoner();
            NodeSet<OWLNamedIndividual> ind_neu2 = reasoner.getInstances(neu2, false);
            System.out.println("Neu2 / Time: " + (System.currentTimeMillis() - start) / 1000.0 +
                    " no of results: " + ind_neu2.getFlattened().size());

            // Abfragen mit komplexen Strukturen sind noch nicht implementiert
//            NodeSet<OWLNamedIndividual> verdhss = reasoner.getInstances(verdadr, false);
//            System.out.println("Verd. mit Adr in Hessen Time: " + (System.currentTimeMillis() - start) / 1000.0 +
//                    " no of results: " + verdhss.getFlattened().size());
//        for (OWLIndividual in : verdhss) {
//            System.out.println(in);
//        }

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

    }


}