
ROOT=../../../../

include $(ROOT)/build-config

EXTRA_LIBS=$(ROOT)/lib/junit.jar


clean: emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/test/tck/*.class


all: 
	$(JAVAC) *.java

af: AddressFactoryTest.class
	$(JAVA) test.tck.factory.AddressFactoryTest  -Djavax.sip.tck.PATH=gov.nist


hf: HeaderFactoryTest.class
	$(JAVA) test.tck.factory.HeaderFactoryTest -Djavax.sip.tck.PATH=gov.nist

mf: MessageFactoryTest.class
	$(JAVA) test.tck.factory.MessageFactoryTest -Djavax.sip.tck.PATH=gov.nist
