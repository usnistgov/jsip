
ROOT=../../../../../../

include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar


all: 
	$(JAVAC) *.java
	$(RMIC) -classpath $(ROOT)/classes -d $(ROOT)/classes  gov.nist.javax.sip.stack.MessageLogTableImpl

clean: emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/gov/nist/javax/sip/stack/*.class

