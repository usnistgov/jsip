/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
 * SIPHeader names that are supported by this parser 
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public interface SIPHeaderNames {

	public static final String MIN_EXPIRES = MinExpiresHeader.NAME; //1
	public static final String ERROR_INFO = ErrorInfoHeader.NAME; //2
	public static final String MIME_VERSION = MimeVersionHeader.NAME; //3
	public static final String IN_REPLY_TO = InReplyToHeader.NAME; //4
	public static final String ALLOW = AllowHeader.NAME; //5
	public static final String CONTENT_LANGUAGE = ContentLanguageHeader.NAME;
	//6
	public static final String CALL_INFO = CallInfoHeader.NAME; //7
	public static final String CSEQ = CSeqHeader.NAME; //8
	public static final String ALERT_INFO = AlertInfoHeader.NAME; //9
	public static final String ACCEPT_ENCODING = AcceptEncodingHeader.NAME;
	//10
	public static final String ACCEPT = AcceptHeader.NAME; //11
	public static final String ACCEPT_LANGUAGE = AcceptLanguageHeader.NAME;
	//12
	public static final String RECORD_ROUTE = RecordRouteHeader.NAME; //13
	public static final String TIMESTAMP = TimeStampHeader.NAME; //14
	public static final String TO = ToHeader.NAME; //15
	public static final String VIA = ViaHeader.NAME; //16
	public static final String FROM = FromHeader.NAME; //17
	public static final String CALL_ID = CallIdHeader.NAME; //18
	public static final String AUTHORIZATION = AuthorizationHeader.NAME; //19
	public static final String PROXY_AUTHENTICATE =
		ProxyAuthenticateHeader.NAME;
	//20
	public static final String SERVER = ServerHeader.NAME; //21
	public static final String UNSUPPORTED = UnsupportedHeader.NAME; //22
	public static final String RETRY_AFTER = RetryAfterHeader.NAME; //23
	public static final String CONTENT_TYPE = ContentTypeHeader.NAME; //24
	public static final String CONTENT_ENCODING = ContentEncodingHeader.NAME;
	//25
	public static final String CONTENT_LENGTH = ContentLengthHeader.NAME; //26
	public static final String ROUTE = RouteHeader.NAME; //27
	public static final String CONTACT = ContactHeader.NAME; //28
	public static final String WWW_AUTHENTICATE = WWWAuthenticateHeader.NAME;
	//29
	public static final String MAX_FORWARDS = MaxForwardsHeader.NAME; //30
	public static final String ORGANIZATION = OrganizationHeader.NAME; //31
	public static final String PROXY_AUTHORIZATION =
		ProxyAuthorizationHeader.NAME;
	//32
	public static final String PROXY_REQUIRE = ProxyRequireHeader.NAME; //33
	public static final String REQUIRE = RequireHeader.NAME; //34
	public static final String CONTENT_DISPOSITION =
		ContentDispositionHeader.NAME;
	//35
	public static final String SUBJECT = SubjectHeader.NAME; //36
	public static final String USER_AGENT = UserAgentHeader.NAME; //37
	public static final String WARNING = WarningHeader.NAME; //38
	public static final String PRIORITY = PriorityHeader.NAME; //39
	public static final String DATE = DateHeader.NAME; //40
	public static final String EXPIRES = ExpiresHeader.NAME; //41
	public static final String SUPPORTED = SupportedHeader.NAME; //42
	public static final String AUTHENTICATION_INFO =
		AuthenticationInfoHeader.NAME;
	//43
	public static final String REPLY_TO = ReplyToHeader.NAME; //44
	public static final String RACK = RAckHeader.NAME; //45
	public static final String RSEQ = RSeqHeader.NAME; //46
	public static final String REASON = ReasonHeader.NAME; //47
	public static final String SUBSCRIPTION_STATE =
		SubscriptionStateHeader.NAME;
	//48
	public static final String EVENT = EventHeader.NAME; //44
	public static final String ALLOW_EVENTS = AllowEventsHeader.NAME; //45
}

/*
 * $Log: not supported by cvs2svn $
 */
