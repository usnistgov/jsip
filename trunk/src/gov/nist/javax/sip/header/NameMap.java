/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import java.util.Hashtable;

/**
 * A mapping class that returns the SIPHeader for a given header name.
 * Add new classes to this map if you are implementing new header types if
 * you want some of the introspection based methods to work.
 * @version 1.2 $Revision: 1.4 $ $Date: 2006-07-02 09:50:35 $
 * @since 1.1
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

		putNameMap(TIMESTAMP, "TimeStamp"); //14

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

		putNameMap(SUBSCRIPTION_STATE, "SubscriptionState"); //44

		putNameMap(EVENT, "Event"); //45

		putNameMap(ALLOW_EVENTS, "AllowEvents"); //46
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.3  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.2  2005/10/06 14:54:44  mranga
 * Reverting back to 1.4 by popular demand
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
 *
 * Revision 1.3  2004/05/07 11:31:40  mranga
 * Submitted by:  Henry Fernandes
 * Reviewed by:   mranga
 * Bad entry in NameMap for TimeStamp
 *
 * Revision 1.2  2004/01/22 13:26:29  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
