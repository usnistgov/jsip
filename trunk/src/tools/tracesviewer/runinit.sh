#!/bin/sh

# Start the GUI traces viewer 
echo "java tools.tracesviewer.TracesViewer -[debug_file|server_file] fileName"


echo $OS

if test $OS = "Windows_NT" ; then
  	java -classpath ".;../../../lib/xerces/xerces.jar;../../../classes" tools.tracesviewer.TracesViewer $*
else
	java -classpath "./:../../../lib/xerces/xerces.jar:../../../classes" tools.tracesviewer.TracesViewer $*
fi
