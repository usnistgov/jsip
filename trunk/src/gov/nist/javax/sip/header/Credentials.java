/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;

/**
 * Credentials  that are used in authentication and authorization headers.
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
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
}
/*
 * $Log: not supported by cvs2svn $
 */
