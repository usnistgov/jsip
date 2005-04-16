/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;

/**
 * Credentials  that are used in authentication and authorization headers.
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:49 $
 */
public class Credentials extends SIPObject {

	private static String DOMAIN = ParameterNames.DOMAIN;
	private static String REALM = ParameterNames.REALM;
	private static String OPAQUE = ParameterNames.OPAQUE;
	private static String ALGORITHM = ParameterNames.ALGORITHM;
	private static String QOP = ParameterNames.QOP;
	private static String STALE = ParameterNames.STALE;
	private static String SIGNATURE = ParameterNames.SIGNATURE;
	private static String RESPONSE = ParameterNames.RESPONSE;
	private static String SIGNED_BY = ParameterNames.SIGNED_BY;
	private static String URI = ParameterNames.URI;
	private static String NONCE = ParameterNames.NONCE;
	private static String NONCE_COUNT = ParameterNames.NONCE_COUNT;
	private static String CNONCE = ParameterNames.CNONCE;
	private static String USERNAME = ParameterNames.USERNAME;

	protected String scheme;

	/**
	 * parameters list.
	 */
	protected NameValueList parameters;

	/**
	 * Default constructor
	 */
	public Credentials() {
		parameters = new NameValueList();
		parameters.setSeparator(COMMA);
	}

	/**
	 * get the parameters list.
	 * @return NameValueList
	 */
	public NameValueList getCredentials() {
		return parameters;
	}

	/**
	 * get the scheme field.
	 * @return String.
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * Set the scheme member
	 * @param s String to set
	 */
	public void setScheme(String s) {
		scheme = s;
	}

	/**
	 * Set the parameters member
	 * @param c NameValueList to set.
	 */
	public void setCredentials(NameValueList c) {
		parameters = c;
	}

	public String encode() {
		String retval = scheme;
		if (!parameters.isEmpty()) {
			retval += SP + parameters.encode();
		}
		return retval;
	}

	public void setCredential(NameValue nameValue) {
		if (nameValue.getName().compareToIgnoreCase(URI) == 0)
			nameValue.setQuotedValue();
		else if (nameValue.getName().compareToIgnoreCase(NONCE) == 0)
			nameValue.setQuotedValue();
		else if (nameValue.getName().compareToIgnoreCase(REALM) == 0)
			nameValue.setQuotedValue();
		else if (nameValue.getName().compareToIgnoreCase(CNONCE) == 0)
			nameValue.setQuotedValue();
		else if (nameValue.getName().compareToIgnoreCase(RESPONSE) == 0)
			nameValue.setQuotedValue();
		else if (nameValue.getName().compareToIgnoreCase(OPAQUE) == 0)
			nameValue.setQuotedValue();
		else if (nameValue.getName().compareToIgnoreCase(USERNAME) == 0)
			nameValue.setQuotedValue();
		else if (nameValue.getName().compareToIgnoreCase(DOMAIN) == 0)
			nameValue.setQuotedValue();
		parameters.set(nameValue);
	}

	public Object clone() {
		Credentials retval = (Credentials) super.clone();
		if (this.parameters != null)
			retval.parameters = (NameValueList) this.parameters.clone();
		return retval;
	}
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
