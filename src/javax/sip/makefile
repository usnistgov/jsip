
# this works with the gnu make tool. 
# If you are working with windows, please install cygwin to get
# gnumake (goto http://www.sourceware.cygnus.com).

ROOT=../../../
-include  $(ROOT)/build-config

ifeq (${OS},Windows_NT)
	JAVAC=$(JDK_HOME)/bin/javac -classpath "$(JDK_HOME);$(DESTINATION);$(EXTRA_LIBS)" -sourcepath $(SRCROOT) -d $(DESTINATION) -deprecation
else
	JAVAC=$(JDK_HOME)/bin/javac  -sourcepath $(SRCROOT) -d $(DESTINATION) -O -classpath "$(JDK_HOME):$(ROOT):./:$(DESTINATION):$(EXTRA_LIBS)" -deprecation
endif



all: 
	$(JAVAC) *.java

clean: emacsclean
	\rm -f *.class

