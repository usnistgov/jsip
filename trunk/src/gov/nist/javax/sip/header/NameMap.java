package gov.nist.javax.sip.header;
import gov.nist.core.*;
import java.util.Hashtable;

/**
 * A mapping class that returns the SIPHeader for a given header name.
 * Add new classes to this map if you are implementing new header types if
 * you want some of the introspection based methods to work.
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 */
public class NameMap implements SIPHeaderNames, PackageNames {
	static Hashtable nameMap;
	static {
		initializeNameMap();
	}

	protected static void putNameMap(String headerName, String className) {
		nameMap.put(
			headerName.toLowerCase(),
			SIPHEADERS_PACKAGE + "." + className);
	}

	public static Class getClassFromName(String headerName) {
		String className = (String) nameMap.get(headerName.toLowerCase());
		if (className == null)
			return null;
		else {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException ex) {
				return null;
			}
		}
	}

	/** add an extension header to this map.
	*@param headerName is the extension header name.
	*@param className is the fully qualified class name that implements
	* the header (does not have to belong to the nist-sip package).
	* Use this if you want to use the introspection-based methods.
	*/

	public static void addExtensionHeader(
		String headerName,
		String className) {
		nameMap.put(headerName.toLowerCase(), className);
	}

	private static void initializeNameMap() {
		nameMap = new Hashtable();
		putNameMap(MIN_EXPIRES, "MinExpires"); // 1

		putNameMap(ERROR_INFO, "ErrorInfo"); // 2

		putNameMap(MIME_VERSION, "MimeVersion"); // 3

		putNameMap(IN_REPLY_TO, "InReplyTo"); // 4

		putNameMap(ALLOW, "Allow"); // 5

		putNameMap(CONTENT_LANGUAGE, "ContentLanguage"); // 6

		putNameMap(CALL_INFO, "CallInfo"); //7

		putNameMap(CSEQ, "CSeq"); //8

		putNameMap(ALERT_INFO, "AlertInfo"); //9

		putNameMap(ACCEPT_ENCODING, "AcceptEncoding"); //10

		putNameMap(ACCEPT, "Accept"); //11

		putNameMap(ACCEPT_LANGUAGE, "AcceptLanguage"); //12

		putNameMap(RECORD_ROUTE, "RecordRoute"); //13

		putNameMap(TIMESTAMP, "Timestamp"); //14

		putNameMap(TO, "To"); //15

		putNameMap(VIA, "Via"); //16

		putNameMap(FROM, "From"); //17

		putNameMap(CALL_ID, "CallID"); //18

		putNameMap(AUTHORIZATION, "Authorization"); //19

		putNameMap(PROXY_AUTHENTICATE, "ProxyAuthenticate"); //20

		putNameMap(SERVER, "Server"); //21

		putNameMap(UNSUPPORTED, "Unsupported"); //22

		putNameMap(RETRY_AFTER, "RetryAfter"); //23

		putNameMap(CONTENT_TYPE, "ContentType"); //24

		putNameMap(CONTENT_ENCODING, "ContentEncoding"); //25

		putNameMap(CONTENT_LENGTH, "ContentLength"); //26

		putNameMap(ROUTE, "Route"); //27

		putNameMap(CONTACT, "Contact"); //28

		putNameMap(WWW_AUTHENTICATE, "WWWAuthenticate"); //29

		putNameMap(MAX_FORWARDS, "MaxForwards"); //30

		putNameMap(ORGANIZATION, "Organization"); //31

		putNameMap(PROXY_AUTHORIZATION, "ProxyAuthorization"); //32

		putNameMap(PROXY_REQUIRE, "ProxyRequire"); //33

		putNameMap(REQUIRE, "Require"); //34

		putNameMap(CONTENT_DISPOSITION, "ContentDisposition"); //35

		putNameMap(SUBJECT, "Subject"); //36

		putNameMap(USER_AGENT, "UserAgent"); //37

		putNameMap(WARNING, "Warning"); //38

		putNameMap(PRIORITY, "Priority"); //39

		putNameMap(DATE, "SIPDateHeader"); //40

		putNameMap(EXPIRES, "Expires"); //41

		putNameMap(SUPPORTED, "Supported"); //42

		putNameMap(REPLY_TO, "ReplyTo"); // 43

		putNameMap(SUBSCRIPTION_STATE, "SubscriptionState");
		//44

		putNameMap(EVENT, "Event"); //45

		putNameMap(ALLOW_EVENTS, "AllowEvents");
		//46
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
