
A utility that converts Ethereal Frames into a format that can be used
by the trace viewer and then invokes the trace viewer to display the
trace as a call flow.  The call flow matches requests and responses
and separates calls by callId, thus making the trace easier to read
and make sense of. 

----------------------------------------------------------------------
To Capture traces using the ethereal sniffer:

1) Download and install Ethereal (www.ethereal.com) and WinPcap
(winpcap.polito.it)
2) Launch Ethereal 
3) Go to Capture->Start
4) Under "Filter:" type "port 5060"
5) Click "OK"
6) Capture packets, press "Stop" when finished
7) Go to File->Print
8) Under "File:" type the file name for this capture
9) Make sure the following are selected:
   a) "Plain Text"
   b) "Print detail"
   c) "Expand all levels"
9) Click "OK"
10) Your done

Ethereal also comes with a command line version (tethereal) that may be
used to capture SIP messages:
   tethereal port 5060 -V

----------------------------------------------------------------------

Now you can visualize the trace as a call flow:

1. Capture SIP Messages using an ethereal sniffer into a file by following
the instructions above ( the file is referred to as sniffertrace below ).

2. To visualize the sniffer trace:

Under Unix:

java -classpath \
../../../lib/antlr/antlrall.jar:../../../lib/xerces/xerces.jar: \
../../ tools.sniffer.SniffFileParser sniffertrace

Under windows:

java -classpath \
../../../lib/antlr/antlrall.jar;../../../lib/xerces/xerces.jar: \
../../ tools.sniffer.SniffFileParser sniffertrace

For an example of how to invoke the visualizer tool, 
see the make target "test" in this directory. 
The sniffertrace to visualize is in a file called pingtel.txt

----------------------------------------------------------------------

Acknowledgement:

This code was contributed by Tim Bardzil <bardzil@colorado.edu>.
This code was completed as part of a class project in TLEN 5843 Singaling
Protocols, taught by Professor Douglas C. Sicker, Ph.D. at the University
of Colorado, Boulder.  Minor modifications to the code were made by
M. Ranganathan <mranga@nist.gov>.

----------------------------------------------------------------------


