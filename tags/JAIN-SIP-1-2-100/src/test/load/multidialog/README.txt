
This example illustrates interleaved concurrent calls launched by the UAC. 
The UAC  can handle multiple dialogs concurrently. Neither the UAC nor the
UAS is allowed to block in the listener. Note that the UAS starts a timer 
task  to delay responding but does not sleep in the Listener to introduce
this delay.

Run it using the ant build.xml file

make shootme

make shootist 

From two command prompts.



