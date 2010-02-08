#!/bin/bash
CP="../dist/u2r3.jar:../import/owlapi-bin.jar:../import/org.semanticweb.HermiT.jar:../import/log4j-1.2.15.jar:../bin/"
ONTO_IRI="file:///home/stefan/.workspace/u2r2/ontologien/owl2rl/sample.owl"
java -cp "$CP" de.langenmaier.u2r3.tests.benchmark.TimeHermit "$ONTO_IRI"