#!/bin/sh
cp="h2-1.2.126.jar:../dist/u2r3.jar:../import/owlapi-bin.jar"
if [ -n "$H2DRIVERS" ] ; then
  cp="$cp:$H2DRIVERS"
fi
if [ -n "$CLASSPATH" ] ; then
  cp="$cp:$CLASSPATH"
fi
java -cp "$cp" org.h2.tools.Console %@

