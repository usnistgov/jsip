
# this works with the gnu make tool. 
# If you are working with windows, please install cygwin to get
# gnumake (goto http://www.sourceware.cygnus.com).

ROOT=../../../../

-include  $(ROOT)/build-config


%.class: %.java
	$(JAVAC) $<


all: 
	$(JAVAC) *.java

clean: 
	\rm -f *.class

