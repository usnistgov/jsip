#
# Please download the xerces parser from xml.apache.org for this to build.
#



ROOT=../../../
include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar

all:	
	$(JAVAC) *.java

tests: all
	$(RM)  -f testout.xml
	$(JAVA) test.torture.Torture torture.xml

clean: emacsclean
	$(RM) -f testout.xml


