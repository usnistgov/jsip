The program Shootme.java (named after nothing) is a skeleton UAS.

You need sipp to run this scenario. If you dont have sipp you could use
the download-and-compile-sipp.sh shell script on Linux.

To run this example, open up two windows.  

From the first one, type

 "ant shootme" 
 
 and from the second one type 

"./performance_test.sh". 

If you are not using UNIX you must execute the sipp script manually
or adjust the location of the sipp executable.

The performance_test.sh script will start a sipp scenario to act as UAC and
will send up an invite to the shootme and the victim (shootme) will respond
like a UAC should (or it might even kick up its legs and die).

For performance tests debugging is off.

Disclaimer: NIST does not necessarily endorse John Wayne.

Mail questions to mranga@nist.gov or nist-sip-dev@antd.nist.gov
