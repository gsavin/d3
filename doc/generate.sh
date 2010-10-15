#!/bin/bash

HEADER=src/org/ri2c/d3/tools/doclet/header.ih
FOOTER=src/org/ri2c/d3/tools/doclet/footer.ih

DOCLET=org.ri2c.d3.tools.D3Doclet
DOCLET_PATH=/home/raziel/workspace/d3/bin/

CLASSPATH="/home/raziel/workspace/d3/bin/:/opt/sun-jdk-1.6.0.17/lib/tools.jar"

javadoc -header $HEADER -footer $FOOTER -doclet $DOCLET -docletpath $DOCLET_PATH -classpath $CLASSPATH $*
