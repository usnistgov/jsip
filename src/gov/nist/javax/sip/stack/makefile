
ROOT=../../../../../../

include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar




all:  prep
	$(JAVAC) *.java


clean: emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/gov/nist/javax/sip/stack/*.class

