#!/bin/bash
CP=".:../import/h2-1.1.116.jar:../import/log4j-1.2.15.jar:../import/owlapi-bin.jar"
cd build
java -cp "$CP" de.langenmaier.u2r3.tests.util.LoadReasoner $1