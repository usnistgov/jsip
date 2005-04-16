/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;

/**
 * Challenge part of the Auth header. This is only used by the parser interface
 *
 * @author M. Ranganathan <mranga@nist.gov>  NIST/ITL/ANTD <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:49 $
 *
 * <a href="${docRoot}/uncopyright.html">This code is in the public domain.</a>
*/
public class Challenge extends SIPObject {

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

	/**
	 * scheme field
	 */
	protected String scheme;

	/**
	 * authParms list
	 */
	protected NameValueList authParams;

	/**
	 * Default constructor     
	 */
	public Challenge() {
		authParams = new NameValueList("authParams");
		authParams.setSeparator(COMMA);
	}

	/**
	 * Encode the challenge in canonical form.
	 * @return String
	 */
	public String encode() {
		return new StringBuffer(scheme)
			.append(SP)
			.append(authParams.encode())
			.toString();
	}

	/**
	 * get the scheme field
	 * @return String
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * get AuthParms list.
	 * @return NameValueList
	 */
	public NameValueList getAuthParams() {
		return authParams;
	}

	/**
	 * get the domain
	 * @return String
	 */
	public String getDomain() {
		return (String) authParams.getValue(DOMAIN);
	}

	/**
	 * get the URI field
	 * @return String
	 */
	public String getURI() {
		return (String) authParams.getValue(URI);
	}

	/**
	 * get the Opaque field
	 * @return String
	 */
	public String getOpaque() {
		return (String) authParams.getValue(OPAQUE);
	}

	/**
	 * get QOP value
	 * @return String
	 */
	public String getQOP() {
		return (String) authParams.getValue(QOP);
	}

	/**
	 * get the Algorithm value.
	 * @return String
	 */
	public String getAlgorithm() {
		return (String) authParams.getValue(ALGORITHM);
	}

	/**
	 * get the State value.
	 * @return String
	 */
	public String getStale() {
		return (String) authParams.getValue(STALE);
	}

	/**
	 * get the Signature value.
	 * @return String
	 */
	public String getSignature() {
		return (String) authParams.getValue(SIGNATURE);
	}

	/**
	 * get the signedBy value.
	 * @return String
	 */
	public String getSignedBy() {
		return (String) authParams.getValue(SIGNED_BY);
	}

	/**
	 * get the Response value.
	 * @return String
	 */
	public String getResponse() {
		return (String) authParams.getValue(RESPONSE);
	}

	/**
	 * get the realm value.
	 * @return String.
	 */
	public String getRealm() {
		return (String) authParams.getValue(REALM);
	}

	/**
	 * get the specified parameter
	 * @param name String to set
	 * @return String to set
	 */
	public String getParameter(String name) {
		return (String) authParams.getValue(name);
	}

	/**
	 * boolean function
	 * @param name String to set
	 * @return true if this header has the specified parameter, false otherwise.
	 */
	public boolean hasParameter(String name) {
		return authParams.getNameValue(name) != null;
	}

	/**
	 * Boolean function
	 * @return true if this header has some parameters.
	 */
	public boolean hasParameters() {
		return authParams.size() != 0;
	}

	/**
	 * delete the specified parameter
	 * @param name String
	 * @return true if the specified parameter has been removed, false
	 * otherwise.
	 */
	public boolean removeParameter(String name) {
		return authParams.delete(name);
	}

	/**
	 * remove all parameters
	 */
	public void removeParameters() {
		authParams = new NameValueList("authParams");
	}

	/**
	 * set the specified parameter
	 * @param nv NameValue to set
	 */
	public void setParameter(NameValue nv) {
		authParams.add(nv);
	}

	/**
	 * Set the scheme member
	 * @param s String to set
	 */
	public void setScheme(String s) {
		scheme = s;
	}

	/**
	 * Set the authParams member
	 * @param a NameValueList to set
	 */
	public void setAuthParams(NameValueList a) {
		authParams = a;
	}

	public Object clone() {
		Challenge retval = (Challenge) super.clone();
		if (this.authParams != null)
			retval.authParams = (NameValueList) this.authParams.clone();
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
