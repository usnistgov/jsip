
ROOT=../../../../../../

include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar




ifeq (${SIMULATION},false)
all:  prep
	$(JAVAC) *.java
	$(RMIC) -classpath $(ROOT)/classes -d $(ROOT)/classes  gov.nist.javax.sip.stack.MessageLogTableImpl
else
all:  prep
	$(JAVAC) *.java
endif


clean: emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/gov/nist/javax/sip/stack/*.class

