ROOT=../../../
include  $(ROOT)/build-config

all: 
	$(JAVAC) *.java

clean:  emacsclean
	\rm -f *.class
	\rm -f $(DESTINATION)/javax/sdp/ *.class

test: SdpFactory.class
	$(JAVA) javax.sdp.SdpFactory


