# This is a make file that builds everything.
# this works with the gnu make tool. If you are working
# with windows, please install cygwin to get  gnumake (goto
# http://www.sourceware.cygnus.com).
#
# Major make targets:
#
#	all 		cleans, builds everything.
#	clean		cleans everything
#       javadoc 	cleans and  builds javadoc.
#	classfiles	builds only classes 
#	srctar		tarfiles for source tree.
#	tarfile		builds a tarfile of all that is ship worthy.
#	examples	builds jain sip examples.
#	ri		builds a nist-sip-1.2.jar file.	
#

ROOT=./


include  $(ROOT)/build-config




DOCDIR= $(ROOT)/docs/api
WINDOWTITLE="JAIN-SIP 1.1 RI For the People!"
HDR= '<b> NIST SIP Parser and Stack (v1.2) API </b>'
#DATE= $(shell date +%D)
DATE= $(shell date)
CVST=REV-DATE
CVSTA=$(subst REV,$(REV),$(CVST))
CVSTAG=$(subst DATE,$(DATE),$(CVSTA))
HEADER=$(subst REV,$(CVSTAG),$(HDR))
TITL= 'NIST SIP Parser and Stack (v1.2)'
TITLE=$(subst REV,$(CVSTAG),$(TITL))
DOCTITLE=$(subst DATE,$(DATE),$(TITLE))
BOTTOM='<font size="-1"> \
<a href="http://www-x.antd.nist.gov/"> A product of the NIST/ITL Advanced Networking Technologies Division. </a><br>  \
<a href="{@docRoot}/uncopyright.html"> See conditions of use. </a> <br> \
<a href="http://www-x.antd.nist.gov/proj/iptel/nist-sip-requestform/nist-sip-1.2-requestform.htm"> Get the latest distribution. </a><br>  \
<a href="mailto:nist-sip-dev@antd.nist.gov">Submit a bug report or feature request. </a><br> \
</font>'


ifeq ($(JAIN_API_SOURCE),)
	SRCPATH=$(SRCROOT)
else
	SRCPATH="$(SRCROOT);$(JAIN_API_SOURCE)"
endif

GROUPCORE = '"Core Packages" "gov.nist.javax.sip.parser.*" \
"gov.nist.javax.sip.*" "gov.nist.javax.sip.header.*" 

javadoc: 
	$(JAVADOC) -sourcepath $(SRCPATH) 			\
		-overview $(DOCDIR)/overview.html 	\
		-classpath $(JAIN_API)			\
		-d  $(DOCDIR)				\
		-use					\
		-splitindex				\
		-windowtitle $(WINDOWTITLE) 		\
		-doctitle $(DOCTITLE)			\
		-version				\
		-author					\
		-header $(HEADER)			\
		-public					\
		-bottom $(BOTTOM)			\
		-link http://java.sun.com/products/j2se/1.4/docs/api \
		javax.sip					\
		javax.sip.header				\
		javax.sip.message				\
		javax.sip.address				\
		gov.nist.javax.sip				\
		gov.nist.javax.sip.address			\
		gov.nist.javax.sip.header			\
		gov.nist.javax.sip.stack			\
		gov.nist.javax.sip.parser			\
		javax.sdp					\
		gov.nist.javax.sdp				\
		gov.nist.core					\

		

DDATE=$(shell date +%D)
DATE=$(subst /,-,$(DDATE))
FILE_NAME = 'nist-sip-1.2.DATE.tar.gz'
#TARFILE_NAME = $(subst REV,$(REV),$(subst DATE,$(DATE),$(FILE_NAME)))
#ZIPFILE_NAME = $(subst REV,$(REV),$(subst DATE,$(DATE),$(ZFILE_NAME)))
ZIPFILE_NAME= nist-sip-1.2.zip
TARFILE_NAME = nist-sip-1.2.tar.gz
SRCTAR = nist-sip-1.2.src.tar.gz
SRCZIP= nist-sip-1.2.src.zip
PROXY_IM_ZIP=proxy-im-1.2.src.zip

all: 
	$(MAKE) clean
	$(MKDIR)  -p $(DESTINATION)
	$(MAKE) jainapi
	$(MAKE) javadoc
	$(MAKE) ri
	$(MAKE) examples
	$(MAKE) tools
	$(MAKE) sdp


# Bug in makefile noticed by Andreas Bystrom
sdp:
	cd $(SRCROOT)/javax/sdp;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sdp;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sdp/parser;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sdp/fields;$(MAKE) all
	cd $(DESTINATION); $(JAR) -cvf ../$(SDP_JAR)	\
		./gov/nist/javax/sdp  			\
		./gov/nist/core ./javax/sdp



examples: 
	cd $(SRCROOT)/examples/shootist;$(MAKE) all

# build the JAIN SIP API. 
jainapi:
	cd $(SRCROOT)/javax/sip/;$(MAKE) clean;$(MAKE) all
	cd $(SRCROOT)/javax/sip/header;$(MAKE) clean;$(MAKE) all
	cd $(SRCROOT)/javax/sip/address;$(MAKE) clean;$(MAKE) all
	cd $(SRCROOT)/javax/sip/message;$(MAKE) clean;$(MAKE) all
	cd classes;					 \
	$(JAR) -cvf ../$(JAIN_API_JAR) javax/sip

#JUST the RI build docs using javadoc target
ri: 
	mkdir -p $(DESTINATION)
	cd $(SRCROOT)/gov/nist/core;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sip/;$(MAKE)  all
	cd $(SRCROOT)/gov/nist/javax/sip/parser;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sip/address;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sip/header;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sip/stack;$(MAKE) all
	cd $(SRCROOT)/gov/nist/javax/sip/message;$(MAKE) all
	$(MAKE) jarfile
tools:
	cd $(SRCROOT)/tools/tracesviewer;$(MAKE) all
	cd $(SRCROOT)/tools/sniffer;$(MAKE) all
	

classfiles: ri examples tools sdp

jarfile:
	$(RM) $(RI_JAR)
	cd classes;	\
	$(JAR) -cvf ../$(RI_JAR) ./gov/nist/javax/sip ./gov/nist/core



clean: emacsclean
	cd $(SRCROOT)/gov/nist/javax/sip;$(MAKE)  clean 
	cd $(SRCROOT)/gov/nist/javax/sip/stack;$(MAKE)  clean 
	cd $(SRCROOT)/gov/nist/javax/sip/address;$(MAKE) clean
	cd $(SRCROOT)/gov/nist/javax/sip/header;$(MAKE) clean
	cd $(SRCROOT)/gov/nist/javax/sip/parser;$(MAKE) clean
	cd $(SRCROOT)/gov/nist/javax/sip/message;$(MAKE) clean
	cd $(SRCROOT)/gov/nist/core;$(MAKE) clean
	cd $(SRCROOT)/examples/shootist; $(MAKE) clean
	$(RM) -rf classes
	$(RM) -f $(RI_JAR)
	$(RM) -f $(SDP_JAR)
	$(RM) -f $(JAIN_API)
	$(RM) -f timestamp

cleandocs:
	cd docs/api/;make clean

backup:
	tar -cvzf $(SRCTAR)  --exclude CVS			\
	--exclude filesystem.attributes				\
	--exclude .cvsignore					\
	--exclude \.#*						\
	--exclude \#*						\
	--exclude \*~						\
	--exclude *.log						\
	--exclude debug.txt					\
	--exclude api						\
	./src/gov/nist/javax			\
	./src/gov/nist/core			\
	./src/examples/shootist		        \
	./src/tools/tracesviewer	        
	


stamp: 
	echo "This archive generated on " >  timestamp
	echo $(DATE) >>  timestamp

zipfile:
	cd ../;		\
	$(RM) $(ZIPFILE_NAME);	\
	zip $(ZIPFILE_NAME)			        	\
	-r ./$(PROJECT_ROOT)/lib/xerces/xerces.jar	        \
	-r ./$(PROJECT_ROOT)/lib/junit/junit.jar	        \
	-r ./$(PROJECT_ROOT)/src/gov/nist/javax			\
	-r ./$(PROJECT_ROOT)/src/gov/nist/core		        \
	-r ./$(PROJECT_ROOT)/src/tools/tracesviewer	        \
	-r ./$(PROJECT_ROOT)/src/tools/sniffer		        \
	-r ./$(PROJECT_ROOT)/src/examples/shootist	        	\
	-r ./$(PROJECT_ROOT)/src/javax				\
	-r ./$(PROJECT_ROOT)/src/test/tck				\
	-r ./$(PROJECT_ROOT)/classes				\
	-r ./$(PROJECT_ROOT)/README				\
	-r ./$(PROJECT_ROOT)/makefile				\
	-r ./$(PROJECT_ROOT)/build.xml				\
	-r ./$(PROJECT_ROOT)/build-config				\
	-r ./$(PROJECT_ROOT)/docs					\
	-r ./$(PROJECT_ROOT)/$(RI_JAR)				\
	-r ./$(PROJECT_ROOT)/$(SDP_JAR)				\
	-r ./$(PROJECT_ROOT)/$(JAIN_API_JAR)			\
	-x \*.nbattrs						\
	-x \*CVS\*					        \
	-x \*filesystem.attributes				\
	-x \*.log				       		\
	-x \*debug.txt						\
	-x \*~							\
	-x \*log.txt						\
	-x \*.cvsignore						

srctar: stamp
	cd ../;							\
	tar -cvzf $(SRCTAR)  --exclude CVS			\
	--exclude filesystem.attributes				\
	--exclude .cvsignore					\
	--exclude \.#*						\
	--exclude \#*						\
	--exclude \*~						\
	--exclude *.log						\
	--exclude debug.txt					\
	--exclude api						\
	./$(PROJECT_ROOT)/manifest.tck				\
	./$(PROJECT_ROOT)/timestamp				\
	./$(PROJECT_ROOT)/src/javax				\
	./$(PROJECT_ROOT)/src/gov/nist/javax			\
	./$(PROJECT_ROOT)/src/gov/nist/core		        \
	./$(PROJECT_ROOT)/src/tools/tracesviewer		        \
	./$(PROJECT_ROOT)/src/tools/sniffer		        \
	./$(PROJECT_ROOT)/src/examples/shootist		        \
	./$(PROJECT_ROOT)/src/test/tck		        	\
	./$(PROJECT_ROOT)/README					\
	./$(PROJECT_ROOT)/makefile					\
	./$(PROJECT_ROOT)/build.xml				\
	./$(PROJECT_ROOT)/build-config				\
	./$(PROJECT_ROOT)/docs					

srczip: stamp
	cd ../;							\
	$(RM) $(SRCZIP);					\
	zip $(SRCZIP)			        		\
	-r  ./$(PROJECT_ROOT)/timestamp				\
	-r ./$(PROJECT_ROOT)/manifest.tck				\
	-r ./$(PROJECT_ROOT)/src/gov/nist/javax			\
	-r ./$(PROJECT_ROOT)/src/gov/nist/core		        \
	-r ./$(PROJECT_ROOT)/src/tools/tracesviewer	        \
	-r ./$(PROJECT_ROOT)/src/tools/sniffer		        \
	-r ./$(PROJECT_ROOT)/src/examples/shootist	        	\
	-r ./$(PROJECT_ROOT)/src/test/tck		        	\
	-r ./$(PROJECT_ROOT)/src/javax				\
	-r ./$(PROJECT_ROOT)/README				\
	-r ./$(PROJECT_ROOT)/makefile				\
	-r ./$(PROJECT_ROOT)/build.xml				\
	-r ./$(PROJECT_ROOT)/build-config				\
	-r ./$(PROJECT_ROOT)/docs					\
	-x \*.nbattrs						\
	-x \*CVS\*					        \
	-x \*filesystem.attributes				\
	-x \*.log				       		\
	-x \*debug.txt						\
	-x \*~							\
	-r \#*						\
	-r \.#*						\
	-x \*log.txt						\
	-x \*.cvsignore						



tarfile: 
	cd ../;							\
	tar -cvzf $(TARFILE_NAME)  --exclude CVS		\
	--exclude filesystem.attributes				\
	--exclude .cvsignore					\
	--exclude \#*						\
	--exclude \*~						\
	--exclude *.log						\
	--exclude debug.txt					\
	./$(PROJECT_ROOT)/manifest.tck				\
	./$(PROJECT_ROOT)/lib/xerces/xerces.jar		        \
	./$(PROJECT_ROOT)/lib/junit/junit.jar		        \
	./$(PROJECT_ROOT)/src/javax				\
	./$(PROJECT_ROOT)/src/test/tck				\
	./$(PROJECT_ROOT)/src/gov/nist/javax			\
	./$(PROJECT_ROOT)/src/gov/nist/core		        \
	./$(PROJECT_ROOT)/src/tools/tracesviewer		        \
	./$(PROJECT_ROOT)/src/tools/sniffer		        \
	./$(PROJECT_ROOT)/src/examples/shootist		        \
	./$(PROJECT_ROOT)/classes					\
	./$(PROJECT_ROOT)/README					\
	./$(PROJECT_ROOT)/makefile					\
	./$(PROJECT_ROOT)/build.xml				\
	./$(PROJECT_ROOT)/build-config				\
	./$(PROJECT_ROOT)/docs					\
	./$(PROJECT_ROOT)/$(RI_JAR)				\
	./$(PROJECT_ROOT)/$(SDP_JAR)				\
	./$(PROJECT_ROOT)/$(JAIN_API_JAR)

	
archives: tarfile 
	
# build the ship tar.gz  and zip image.
ship:  all
	$(MAKE) tarfile


#	$(JAIN_API_JAR) $(RI_JAR)  lib/junit/junit.jar

# Build the jain api TCK
tck:
	cd src/test/tck;$(MAKE) all			
	cd lib/junit;jar -xvf junit.jar			
	jar  cvfm jain-sip-1.1.tck.jar  		\
	 manifest.tck  					\
	-C ./lib/junit junit				\
	-C ./classes test/tck/ 				\
	-C ./classes gov/nist/javax/sip 		\
	-C ./classes gov/nist/core 			\
	-C ./classes javax/sip/ 			

libzip:
	zip libs.zip 		      			\
	-r lib/xerces/xerces.jar			\
	-r lib/junit/junit.jar				\
	-r lib/xerces/LICENSE			

#Builds the traces viewer jar file:
viewerjar:
	cd src/tools/tracesviewer;$(MAKE) all
	cd lib/xerces;jar -xvf xerces.jar	
	jar  cvfm tracesviewer.jar  		\
	 manifest.viewer  			\
	-C ./classes ./tools/tracesviewer			\
	-C ./lib/xerces      org				\
	-C ./lib/xerces      javax				

export:
	tar -cvzf export.tar.gz  --exclude CVS	\
	--exclude filesystem.attributes		\
	--exclude .cvsignore			\
	--exclude \.#*				\
	--exclude \#*				\
	--exclude \*~				\
	--exclude *.log				\
	--exclude debug.txt			\
	--exclude api				\
	--exclude build-config			\
	./manifest.tck				\
	./timestamp				\
	./src/javax				\
	./src/gov/nist/javax			\
	./src/gov/nist/core		        \
	./src/tools/tracesviewer	        \
	./src/tools/sniffer		        \
	./src/examples/shootist		        \
	./src/test/tck		        	\
	./README				\
	./makefile				\
	./build.xml				\
	./build-config				\
	./docs					
	mv export.tar.gz ../java-net/jain-sip
