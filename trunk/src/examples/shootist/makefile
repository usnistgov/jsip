

ROOT=../../../
include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar


clean: emacsclean
	\rm -f *.class
	\rm -f *log*
	\rm -f *debug*

all:  prep
ifeq (${SIMULATION},true)
	echo "${SIMULATION}"
	$(JAVAC) *.java
else
	$(JAVAC) *.java
endif



#shootist is the uac
shootist: Shootist.class
	\rm -f shootistdebug.txt
	\rm -f shootistlog.txt
	$(JAVA) examples.shootist.Shootist 


#shootme is the uas
shootme: Shootme.class
	\rm -f shootmedebug.txt
	\rm -f shootmelog.txt
	$(JAVA)  examples.shootist.Shootme 

shootmelog:
	$(JAVA) tools.tracesviewer.TracesViewer -server_file shootmelog.txt

shootistlog:
	$(JAVA) tools.tracesviewer.TracesViewer -server_file shootistlog.txt

sim:
	$(JAVA) examples.shootist.Shoot

cleanlogs:
	\rm -f *debug.txt
	\rm -f *log.txt

