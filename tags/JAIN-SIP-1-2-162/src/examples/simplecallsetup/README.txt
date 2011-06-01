The program Shootist.java (named after a famous John Wayne movie), 
is a skeleton UAC.

The program Shootme.java (named after no movie in particular) is a skeleton
UAS.

To run this example, open up two windows.  

From the first one, type

 "ant shootme" 
 
 and from the second one type 

"ant shootist". 

The shootist will send up an invite to the shootme and the victim (shootme) will respond
like a UAC should (or it might even kick up its legs and die).

To change the direction from which tbe BYE is sent, edit the Shootme.java file and change
the global flag callerSendsBye.

For illustrative purposes, this example turns debugging and loggin on. You
should do so when you are developing your applications.  You will
see shootmelog.txt and shootistlog.txt appear in this directory.
You can visualize the trace files using the ant target shootmelog
and shootistlog.

Disclaimer: NIST does not necessarily endorse John Wayne.

Mail questions to mranga@nist.gov or nist-sip-dev@antd.nist.gov
