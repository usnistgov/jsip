

ROOT=../../../
include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar


clean: emacsclean
	\rm -f *.class
	\rm -f *log*
	\rm -f *debug*

all: Shootist.class MyRouter.class Shootme.class

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
	$(JAVA) tools.tracesviewer.TracesViewer -f shootmelog.txt

shootistlog:
	$(JAVA) tools.tracesviewer.TracesViewer -f shootistlog.txt

