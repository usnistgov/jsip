
ROOT=../../../../

-include  $(ROOT)/build-config

all: 
	$(JAVAC) *.java

clean:  emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/gov/nist/core/*.class

hparse:
	$(JAVA) gov.nist.core.HostNameParser

