/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;

/**
 * The WWWAuthenticate SIP header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:30 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @see WWWAuthenticateList SIPHeader which strings these together.
 */

public class WWWAuthenticate
	extends AuthenticationHeader
	implements WWWAuthenticateHeader {

	/**
	 * Default Constructor.
	 */
	public WWWAuthenticate() {
		super(NAME);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
