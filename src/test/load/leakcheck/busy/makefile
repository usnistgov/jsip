

ROOT=../../../../..
include $(ROOT)/build-config


clean: emacsclean
	$(RM) *.class
	$(RM) -f *log*
	$(RM) -f *debug*

all:  prep
ifeq (${SIMULATION},true)
	echo "${SIMULATION}"
	$(JAVAC) *.java
else
	$(JAVAC) Shootist.java Shootme.java
endif



#shootist is the uac
shootist: Shootist.class
	$(RM) shootistdebug.txt
	$(RM) shootistlog.txt
	$(JAVA) test.load.leakcheck.busy.Shootist 


#shootme is the uas
shootme: Shootme.class
	$(RM) -f shootmedebug.txt
	$(RM) -f shootmelog.txt
	$(JAVA)  test.load.leakcheck.busy.Shootme 


cleanlogs:
	$(RM) -f *debug.txt
	$(RM) -f *log.txt

