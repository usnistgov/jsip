# this works with the gnu make tool. If you are working with windows, please install cygwin to get
# gnumake (goto http://www.sourceware.cygnus.com).

ROOT=../../../../../../

include  $(ROOT)/build-config

%.class: %.java
	$(JAVAC) $<

all:  prep
	$(JAVAC) *.java

clean:  emacsclean
	\rm -f *.class
	\rm -f $(ROOT)/classes/gov/nist/javax/sip/parser/*.class

test: clength requestlineparse hostnameparse \
      retryafterparse contact statuslineparse \
      requireparse  callidparse \
      cseqparse addressparse urlparse fromparse \
      toparse routeparse expiresparse viaparse \
      wwwauth contenttype warningparse useragentparse \
      unsupportedparse timestampparse supportedparse \
      subscriptionparse subjectparse serverparse retryafterparse \
      requireparse replytoparse reasonparse rseqparse rackparse \
      proxyrequireparse priorityparse organizationparse minexpiresparse \
      proxyauthorizationparse \
      mimeversionparse inreplytoparse eventparse contentlanguageparse \
      contentencodingparse contentdispositionparse callinfoparse \
      authenticationinfoparse allowparse alloweventsparse alertinfoparse \
      acceptparse

stringparse: StringMsgParser.class
	$(JAVA)    gov.nist.javax.sip.parser.StringMsgParser

date: DateParser.class
	$(JAVA)    gov.nist.javax.sip.parser.DateParser

clength: ContentLengthParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ContentLengthParser

contact: ContactParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ContactParser

requestlineparse: RequestLineParser.class
	$(JAVA)    gov.nist.javax.sip.parser.RequestLineParser

hostnameparse: HostNameParser.class
	$(JAVA)    gov.nist.javax.sip.parser.HostNameParser

statuslineparse: TokenTypes.class  Lexer.class Parser.class StatusLineParser.class
	$(JAVA)    gov.nist.javax.sip.parser.StatusLineParser

callidparse: CallIDParser.class
	$(JAVA)    gov.nist.javax.sip.parser.CallIDParser

urlparse: URLParser.class
	$(JAVA)    gov.nist.javax.sip.parser.URLParser

cseqparse: CSeqParser.class
	$(JAVA)    gov.nist.javax.sip.parser.CSeqParser

addressparse: AddressParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AddressParser

fromparse: FromParser.class
	$(JAVA)    gov.nist.javax.sip.parser.FromParser

toparse: ToParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ToParser

refertoparse: ReferToParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ReferToParser

routeparse: RouteParser.class
	$(JAVA)    gov.nist.javax.sip.parser.RouteParser

expiresparse: ExpiresParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ExpiresParser

viaparse: ViaParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ViaParser

wwwauth: WWWAuthenticateParser.class
	$(JAVA)    gov.nist.javax.sip.parser.WWWAuthenticateParser

authparse: AuthorizationParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AuthorizationParser

contenttype: ContentTypeParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ContentTypeParser

warningparse: WarningParser.class
	$(JAVA)    gov.nist.javax.sip.parser.WarningParser

useragentparse: UserAgentParser.class
	$(JAVA)    gov.nist.javax.sip.parser.UserAgentParser

unsupportedparse: UnsupportedParser.class
	$(JAVA)    gov.nist.javax.sip.parser.UnsupportedParser

timestampparse: TimeStampParser.class
	$(JAVA)    gov.nist.javax.sip.parser.TimeStampParser

supportedparse: SupportedParser.class
	$(JAVA)    gov.nist.javax.sip.parser.SupportedParser

subscriptionparse: SubscriptionStateParser.class
	$(JAVA)    gov.nist.javax.sip.parser.SubscriptionStateParser

subjectparse: SubjectParser.class
	$(JAVA)    gov.nist.javax.sip.parser.SubjectParser

serverparse: ServerParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ServerParser

retryafterparse: RetryAfterParser.class
	$(JAVA)    gov.nist.javax.sip.parser.RetryAfterParser

requireparse: RequireParser.class
	$(JAVA)    gov.nist.javax.sip.parser.RequireParser

replytoparse: ReplyToParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ReplyToParser

reasonparse: ReasonParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ReasonParser

rseqparse: RSeqParser.class
	$(JAVA)    gov.nist.javax.sip.parser.RSeqParser

rackparse: RAckParser.class
	$(JAVA)    gov.nist.javax.sip.parser.RAckParser

proxyrequireparse: ProxyRequireParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ProxyRequireParser

proxyauthorizationparse: ProxyAuthorizationParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ProxyAuthorizationParser

priorityparse: PriorityParser.class
	$(JAVA)    gov.nist.javax.sip.parser.PriorityParser

organizationparse: OrganizationParser.class
	$(JAVA)    gov.nist.javax.sip.parser.OrganizationParser

minexpiresparse: MinExpiresParser.class
	$(JAVA)    gov.nist.javax.sip.parser.MinExpiresParser

mimeversionparse: MimeVersionParser.class
	$(JAVA)    gov.nist.javax.sip.parser.MimeVersionParser

inreplytoparse: InReplyToParser.class
	$(JAVA)    gov.nist.javax.sip.parser.InReplyToParser

eventparse: EventParser.class
	$(JAVA)    gov.nist.javax.sip.parser.EventParser

errorinfoparse: ErrorInfoParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ErrorInfoParser

contentlanguageparse: ContentLanguageParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ContentLanguageParser

contentencodingparse: ContentEncodingParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ContentEncodingParser

contentdispositionparse: ContentDispositionParser.class
	$(JAVA)    gov.nist.javax.sip.parser.ContentDispositionParser

callinfoparse: CallInfoParser.class
	$(JAVA)    gov.nist.javax.sip.parser.CallInfoParser

authenticationinfoparse: AuthenticationInfoParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AuthenticationInfoParser

allowparse: AllowParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AllowParser

alloweventsparse: AllowEventsParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AllowEventsParser

alertinfoparse: AlertInfoParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AlertInfoParser

acceptparse: AcceptParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AcceptParser

alp: AcceptLanguageParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AcceptLanguageParser

ae: AcceptEncodingParser.class
	$(JAVA)    gov.nist.javax.sip.parser.AcceptEncodingParser

