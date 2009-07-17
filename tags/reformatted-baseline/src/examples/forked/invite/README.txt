This is in honor of the great "forking is just plain wrong" discussion thread 
in the sipping mailing list.  

This example illustrates how a client may deal with forked INVITE calls. 
In this case the client sends a request through a proxy server to two other clients
These clients are modeled by two instances of Shootme.java. 
Note that the Shootist has to deal with two responses to the 
outgoing invite and has to pick one and assign a dialog to it. It issues a BYE 
on one of the Dialogs and ACKs the other one.

To run :

1. Start the proxy using the proxy target
2. Start the uas using the shootme and shootme2 targets
3. Start the uac using the shootist target

