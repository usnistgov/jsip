#!/bin/sh

# Start the GUI traces viewer 
echo "java tools.tracesviewer.TracesViewer -[debug_file|server_file] fileName"


echo $OS

#if you are using 1.3

# EXTRA_LIBS= ../../../lib/xerces/xerces.jar

if test $OS = "Windows_NT" ; then
  	java -classpath "../../../classes" tools.tracesviewer.TracesViewer $*
else
	java -classpath "../../../classes" tools.tracesviewer.TracesViewer $*
fi
