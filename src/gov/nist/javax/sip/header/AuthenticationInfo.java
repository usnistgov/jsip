package gov.nist.javax.sip.header;

import gov.nist.core.*;
import java.text.ParseException;

/**
 * Authentication info SIP Header.
 *
 * @author M. Ranganathan <mranga@nist.gov>  NIST/ITL/ANTD
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class AuthenticationInfo
	extends ParametersHeader
	implements javax.sip.header.AuthenticationInfoHeader {

	/** Default contstructor.
	 */
	public AuthenticationInfo() {
		super(NAME);
		parameters.setSeparator(COMMA); // Odd ball.
	}

	public void add(NameValue nv) {
		parameters.add(nv);
	}

	/** Value of header encoded in canonical form.
	 */

	protected String encodeBody() {
		return parameters.encode();

	}

	/** Get the name value pair for a given authentication info parameter.
	 *
	 *@param name is the name for which we want to retrieve the name value
	 *  list.
	 */

	public NameValue getAuthInfo(String name) {
		return parameters.getNameValue(name);
	}

	/**
	 * Returns the AuthenticationInfo value of this AuthenticationInfoHeader.
	 *
	 *
	 *
	 * @return the String representing the AuthenticationInfo
	 *
	 * @since JAIN SIP v1.1
	 *
	 */
	public String getAuthenticationInfo() {
		return this.encodeBody();
	}

	/** Returns the CNonce value of this AuthenticationInfoHeader.
	 *
	 * @return the String representing the cNonce information, null if value is
	 * not set.
	 * @since v1.1
	 */
	public String getCNonce() {
		return this.getParameter(ParameterNames.CNONCE);
	}

	/** Returns the nextNonce value of this AuthenticationInfoHeader.
	 *
	 * @return the String representing the nextNonce
	 * information, null if value is not set.
	 * @since v1.1
	 */
	public String getNextNonce() {
		return this.getParameter(ParameterNames.NEXT_NONCE);
	}

	/** Returns the Nonce Count value of this AuthenticationInfoHeader.
	 *
	 * @return the integer representing the nonceCount information, -1 if value is
	 * not set.
	 * @since v1.1
	 */
	public int getNonceCount() {
		return this.getParameterAsInt(ParameterNames.NONCE_COUNT);
	}

	/** Returns the messageQop value of this AuthenticationInfoHeader.
	 *
	 * @return the string representing the messageQop information, null if the
	 * value is not set.
	 * @since v1.1
	 */
	public String getQop() {
		return this.getParameter(ParameterNames.QOP);
	}

	/** Returns the Response value of this AuthenticationInfoHeader.
	 *
	 * @return the String representing the Response information.
	 * @since v1.1
	 */
	public String getResponse() {
		return this.getParameter(ParameterNames.RESPONSE_AUTH);
	}

	/** Sets the CNonce of the AuthenticationInfoHeader to the <var>cNonce</var>
	 * parameter value.
	 *
	 * @param cNonce - the new cNonce String of this AuthenticationInfoHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the cNonce value.
	 * @since v1.1
	 */
	public void setCNonce(String cNonce) throws ParseException {
		this.setParameter(ParameterNames.CNONCE, cNonce);
	}

	/** Sets the NextNonce of the AuthenticationInfoHeader to the <var>nextNonce</var>
	 * parameter value.
	 *
	 * @param nextNonce - the new nextNonce String of this AuthenticationInfoHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the nextNonce value.
	 * @since v1.1
	 */
	public void setNextNonce(String nextNonce) throws ParseException {
		this.setParameter(ParameterNames.NEXT_NONCE, nextNonce);
	}

	/** Sets the Nonce Count of the AuthenticationInfoHeader to the <var>nonceCount</var>
	 * parameter value.
	 *
	 * @param nonceCount - the new nonceCount integer of this AuthenticationInfoHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the nonceCount value.
	 * @since v1.1
	 */
	public void setNonceCount(int nonceCount) throws ParseException {
		if (nonceCount < 0)
			throw new ParseException("bad value", 0);
		String nc = Integer.toHexString(nonceCount);

		String base = "00000000";
		nc = base.substring(0, 8 - nc.length()) + nc;
		this.setParameter(ParameterNames.NC, nc);
	}

	/** Sets the Qop value of the AuthenticationInfoHeader to the new
	 * <var>qop</var> parameter value.
	 *
	 * @param qop - the new Qop string of this AuthenticationInfoHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the Qop value.
	 * @since v1.1
	 */
	public void setQop(String qop) throws ParseException {
		this.setParameter(ParameterNames.QOP, qop);
	}

	/** Sets the Response of the
	 * AuthenticationInfoHeader to the new <var>response</var>
	 * parameter value.
	 *
	 * @param response - the new response String of this
	 * AuthenticationInfoHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the Response.
	 * @since v1.1
	 */
	public void setResponse(String response) throws ParseException {
		this.setParameter(ParameterNames.RESPONSE, response);
	}

	public void setParameter(String name, String value) throws ParseException {
		if (name == null)
			throw new NullPointerException("null name");
		NameValue nv = super.parameters.getNameValue(name.toLowerCase());
		if (nv == null) {
			nv = new NameValue(name, value);
			if (name.equalsIgnoreCase(ParameterNames.QOP)
				|| name.equalsIgnoreCase(ParameterNames.NEXT_NONCE)
				|| name.equalsIgnoreCase(ParameterNames.REALM)
				|| name.equalsIgnoreCase(ParameterNames.CNONCE)
				|| name.equalsIgnoreCase(ParameterNames.NONCE)
				|| name.equalsIgnoreCase(ParameterNames.OPAQUE)
				|| name.equalsIgnoreCase(ParameterNames.USERNAME)
				|| name.equalsIgnoreCase(ParameterNames.DOMAIN)
				|| name.equalsIgnoreCase(ParameterNames.NEXT_NONCE)
				|| name.equalsIgnoreCase(ParameterNames.RESPONSE_AUTH)) {
				if (value == null)
					throw new NullPointerException("null value");
				if (value.startsWith(Separators.DOUBLE_QUOTE))
					throw new ParseException(
						value + " : Unexpected DOUBLE_QUOTE",
						0);
				nv.setQuotedValue();
			}
			super.setParameter(nv);
		} else
			nv.setValue(value);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
