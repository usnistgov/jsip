

ROOT=../../../
include $(ROOT)/build-config

EXTRA_LIBS=$(ROOT)/lib/jakarta-regexp-1.2.jar


clean: emacsclean
	$(RM) *.class
	$(RM) -f *log*
	$(RM) -f *debug*

all:  
	$(JAVAC) *.java



#shootist is the uac
matchtest: MatchTest.class
	$(JAVA) examples.match.MatchTest

regexp: RegexpMatchTest.class
	$(JAVA) examples.match.RegexpMatchTest


