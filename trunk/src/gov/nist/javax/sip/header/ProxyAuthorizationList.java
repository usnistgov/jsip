/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;

/**
 * List of ProxyAuthorization headers.
 * @version JAIN-SIP-1.1 $Revision: 1.1 $ $Date: 2005-02-24 15:52:21 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ProxyAuthorizationList extends SIPHeaderList {

	/** Default constructor
	 */
	public ProxyAuthorizationList() {
		super(ProxyAuthorization.class, ProxyAuthorizationHeader.NAME);
	}
}
