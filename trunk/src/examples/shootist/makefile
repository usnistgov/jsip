

ROOT=../../../
include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar


clean: emacsclean
	$(RM) *.class
	$(RM) -f *log*
	$(RM) -f *debug*

all:  prep
ifeq (${SIMULATION},true)
	echo "${SIMULATION}"
	$(JAVAC) *.java
else
	$(JAVAC) *.java
endif



#shootist is the uac
shootist: Shootist.class
	$(RM) shootistdebug.txt
	$(RM) shootistlog.txt
	$(JAVA) examples.shootist.Shootist 


#shootme is the uas
shootme: Shootme.class
	$(RM) -f shootmedebug.txt
	$(RM) -f shootmelog.txt
	$(JAVA)  examples.shootist.Shootme 

shootmelog:
	$(JAVA) tools.tracesviewer.TracesViewer -server_file shootmelog.txt

shootistlog:
	$(JAVA) tools.tracesviewer.TracesViewer -server_file shootistlog.txt

sim:
	$(JAVA) examples.shootist.Shoot

cleanlogs:
	$(RM) -f *debug.txt
	$(RM) -f *log.txt

