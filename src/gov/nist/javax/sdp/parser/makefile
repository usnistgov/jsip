# this works with the gnu make tool. If you are working with windows, please install cygwin to get
# gnumake (goto http://www.sourceware.cygnus.com).

ROOT=../../../../../../

include  $(ROOT)/build-config

all: 
	$(JAVAC) *.java

clean:  emacsclean
	$(RM) *.class
	$(RM) $(DESTINATION)/gov/nist/javax/sip/parser/*.class


origin: OriginFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.OriginFieldParser

attribparse: AttributeFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.AttributeFieldParser

bwparse: BandwidthFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.BandwidthFieldParser

cfparse: ConnectionFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.ConnectionFieldParser

emparse: EmailFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.EmailFieldParser

iparse: InformationFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.InformationFieldParser

keyparse: KeyFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.KeyFieldParser

mparse: MediaFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.MediaFieldParser

phoneparse: PhoneFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.PhoneFieldParser

protoparse: ProtoVersionFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.ProtoVersionFieldParser

repeat: RepeatFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.RepeatFieldParser

uri: URIFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.URIFieldParser

timeparse: TimeFieldParser.class
	$(JAVA)    gov.nist.javax.sdp.parser.TimeFieldParser
