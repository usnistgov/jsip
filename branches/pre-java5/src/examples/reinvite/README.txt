The program Shootist.java (named after a famous John Wayne movie), 
is a skeleton UAC.

The program Shootme.java (named after no movie in particular) is a skeleton
UAS.

To run this example, open up two windows.  From the first one, type "ant
shootme" and from the second one type "ant shootist". The shootist will
send up an invite to the shootme and the victim (shootme) will respond
like a UAC should (or it might even kick up its legs and die).

For illustrative purposes, this example turns debugging and loggin on. You
should do so when you are developing your applications.  You will
see shootmelog.txt and shootistlog.txt appear in this directory.
You can visualize the trace files using the ant target shootmelog
 shootistlog.
 
 Note that in this example, automatic dialog support is enabled and there is a re-invite
 that is issued from Shootme.

Mail questions to mranga@nist.gov or nist-sip-dev@antd.nist.gov
