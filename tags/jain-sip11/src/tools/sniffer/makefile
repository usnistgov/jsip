#
# Please download the xerces parser from xml.apache.org for this to build.
#


ROOT=../../../
include $(ROOT)/build-config
EXTRA_LIBS=$(XERCES_HOME)/xerces.jar

all:	
	$(JAVAC) *.java

clean: emacsclean
	\rm -f *.class

test:
	$(JAVA)  tools.sniffer.SniffFileParser pingtel.txt


