package gov.nist.javax.sip.header;

/**
 * A list of commonly occuring parameter names. These are for conveniance
 * so as to avoid typo's
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2004-11-28 17:32:25 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public interface ParameterNames {
	// Issue reported by larryb
	public static final String NEXT_NONCE = "nextnonce";
	public static final String TAG = "tag";
	public static final String USERNAME = "username";
	public static final String URI = "uri";
	public static final String DOMAIN = "domain";
	public static final String CNONCE = "cnonce";
	public static final String PASSWORD = "password";
	public static final String RESPONSE = "response";
	public static final String RESPONSE_AUTH = "rspauth";
	public static final String OPAQUE = "opaque";
	public static final String ALGORITHM = "algorithm";
	public static final String DIGEST = "Digest";
	public static final String SIGNED_BY = "signed-by";
	public static final String SIGNATURE = "signature";
	public static final String NONCE = "nonce";
	// Issue reported by larryb
	public static final String NONCE_COUNT = "nc";
	public static final String PUBKEY = "pubkey";
	public static final String COOKIE = "cookie";
	public static final String REALM = "realm";
	public static final String VERSION = "version";
	public static final String STALE = "stale";
	public static final String QOP = "qop";
	public static final String NC = "nc";
	public static final String PURPOSE = "purpose";
	public static final String CARD = "card";
	public static final String INFO = "info";
	public static final String ACTION = "action";
	public static final String PROXY = "proxy";
	public static final String REDIRECT = "redirect";
	public static final String EXPIRES = "expires";
	public static final String Q = "q";
	public static final String RENDER = "render";
	public static final String SESSION = "session";
	public static final String ICON = "icon";
	public static final String ALERT = "alert";
	public static final String HANDLING = "handling";
	public static final String REQUIRED = "required";
	public static final String OPTIONAL = "optional";
	public static final String EMERGENCY = "emergency";
	public static final String URGENT = "urgent";
	public static final String NORMAL = "normal";
	public static final String NON_URGENT = "non-urgent";
	public static final String DURATION = "duration";
	public static final String BRANCH = "branch";
	public static final String HIDDEN = "hidden";
	public static final String RECEIVED = "received";
	public static final String MADDR = "maddr";
	public static final String TTL = "ttl";
	public static final String TRANSPORT = "transport";
	public static final String TEXT = "text";
	public static final String CAUSE = "cause";
	public static final String ID = "id";
    	//@@@ hagai
    	public static final String RPORT = "rport";
}
/*
 * $Log: not supported by cvs2svn $
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
