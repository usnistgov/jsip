# The root make file
ROOT=../../../../../../

include $(ROOT)/build-config


all:  prep
	$(JAVAC) *.java


clean: emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/gov/nist/javax/sip/stack/*.class

