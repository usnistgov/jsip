/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;

/**
 * Authorization SIP header.
 *
 * @see ProxyAuthorization
 *
 * @author M. Ranganathan <mranga@nist.gov>  NIST/ITL/ANTD <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class Authorization
	extends gov.nist.javax.sip.header.AuthenticationHeader
	implements javax.sip.header.AuthorizationHeader {

	/** Default constructor.
	 */
	public Authorization() {
		super(AuthorizationHeader.NAME);
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
