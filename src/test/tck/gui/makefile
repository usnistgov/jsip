
ROOT=../../../..

include $(ROOT)/build-config

EXTRA_LIBS=$(ROOT)/lib/junit/junit.jar


all: 
	$(JAVAC) *.java
	rm -rf $(DESTIONATION)/test/tck/gui/images; cp -r ./images $(DESTINATION)/test/tck/gui
	
