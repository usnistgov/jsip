
ROOT=../../../

include $(ROOT)/build-config

EXTRA_LIBS=$(ROOT)/lib/junit/junit.jar



clean: emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/test/tck/*.class


all: 
	cd factory; make all
	cd msgflow; make all 
	cd gui; make all
	$(JAVAC) *.java
	

tck-text:
	\rm -f tcklog.txt
	$(JAVA)  test.tck.Tck -mode text -path gov.nist -abortonfail  \
		-logfile tcklog.txt

tck-gui:
	\rm -f tcklog.txt
	$(JAVA) test.tck.Tck -mode gui -path gov.nist -abortonfail \
		-logfile tcklog.txt

		
