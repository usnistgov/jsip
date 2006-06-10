
ROOT=../../../..

include $(ROOT)/build-config

EXTRA_LIBS=$(ROOT)/lib/junit.jar


all: 
	$(JAVAC) *.java

noninvite:
	$(JAVA) test.tck.msgflow.NonInviteServerTransactionsStateMachineTest
	
