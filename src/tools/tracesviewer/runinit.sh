#!/bin/sh

# Start the GUI traces viewer 
echo "traceviewer -rmiHost rmihost -stackId stackid"
echo "rmiHost is where the RMI registry is running (defaults to 127.0.0.1)"
echo "stackid is the JAIN stack name  you assigned to the stack"


echo $OS

if test $OS = "Windows_NT" ; then
  	java -classpath ".;../../../lib/xerces/xerces.jar;../../../classes" tools.tracesviewer.TracesViewer $*
else
	java -classpath "./:../../../lib/xerces/xerces.jar:../../../classes" tools.tracesviewer.TracesViewer $*
fi
