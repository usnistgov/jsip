ROOT=../../../../../
include  $(ROOT)/build-config

all: 
	$(JAVAC) *.java

test:
	$(JAVA) javax.sdp.SessionDescriptionImpl

clean:  emacsclean
	$(RM)  *.class
	$(RM)  $(DESTINATION)/gov/nist/javax/sdp/*.class

