This example of a NIST only feature (not part of the JAIN-SIP standard)
shows how you can register a log factory with the stack to configure
logging. The log factory allows you to generate customized logging records
that can record envioronment conditions into the logging stream. This
facility is useful for logging, monitoring and debugging.

The stack property gov.nist.javax.sip.LOG_FACTORY points to a class which
SipStackImpl creates when it starts up.  Each time a message is recieved
by the stack or sent out by the stack, the registered log factory is
called to generate a logging record.

How to run it and what to look for
----------------------------------

How to run it
-------------
Run this example from two command prompts using 

ant shootme 

from another command prompt

ant shootist

What to look at
---------------

Then examine the log generated in shootistlog.txt

Compare it with the custom log record LogRecordImpl.java

Take a look at the configuration setup in Shootist.java

Take a look at the code in LogFactoryImpl.java
Take a look at the code in StackLoggerImpl.java
Take a look at the code in ServerLoggerImpl.java

Thanks to Jean Deruelle for providing support for a pluggable logger.


