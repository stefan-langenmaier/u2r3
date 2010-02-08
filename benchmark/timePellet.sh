#!/bin/bash
PELLET_FOLDER="/home/stefan/apps/pellet-2.0.1/"
ONTO_FOLDER="/home/stefan/.workspace/u2r2/ontologien/"
cd ${PELLET_FOLDER}
./pellet.sh classify --loader OWLAPIv3 --input-format RDF/XML ${ONTO_FOLDER}/owl2rl/disease_ontology.owl 
