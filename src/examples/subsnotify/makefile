

ROOT=../../../
include $(ROOT)/build-config

EXTRA_LIBS=$(XERCES_HOME)/xerces.jar


clean: emacsclean
	$(RM) *.class
	$(RM) -f *log*
	$(RM) -f *debug*

all:
	$(JAVAC) Subscriber.java Notifier.java MyRouter.java



#subscriber is the uac
subscriber: Subscriber.class
	$(RM) subscriberdebug.txt
	$(RM) subscriberlog.txt
	$(JAVA) examples.subsnotify.Subscriber 


#notifier is the uas
notifier: Notifier.class
	$(RM) -f notifierdebug.txt
	$(RM) -f notifierlog.txt
	$(JAVA)  examples.subsnotify.Notifier 

notifierlog:
	$(JAVA) tools.tracesviewer.TracesViewer -server_file notifierlog.txt

subscriberlog:
	$(JAVA) tools.tracesviewer.TracesViewer -server_file subscriberlog.txt


cleanlogs:
	$(RM) -f *debug.txt
	$(RM) -f *log.txt

