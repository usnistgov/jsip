
ROOT=../../../

include $(ROOT)/build-config

EXTRA_LIBS=$(ROOT)/lib/junit/junit.jar



clean: emacsclean
	$(RM)  *.class
	$(RM) $(ROOT)/classes/test/tck/*.class


all: 
	cd factory; make all
	cd msgflow; make all 
	cd gui; make all
	$(JAVAC) *.java
	

tck-text:
	$(RM) tcklog.txt
	$(JAVA)  test.tck.Tck -mode text -path gov.nist -abortonfail  \
		-logfile tcklog.txt

tck-gui:
	$(RM) tcklog.txt
	$(JAVA) test.tck.Tck -mode gui -path gov.nist -abortonfail \
		-logfile tcklog.txt


cleanlogs:
	$(RM) *Log.txt
	$(RM) *log.txt

		
