/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
 * Proxy Require SIPSIPObject (list of option tags)
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ProxyRequireList extends SIPHeaderList {

	/** Default Constructor
	 */
	public ProxyRequireList() {
		super(ProxyRequire.class, ProxyRequireHeader.NAME);
	}

	/** Constructor
	 * @param sip SIPObjectList to set
	 */
	public ProxyRequireList(SIPObjectList sip) {
		super(sip, PROXY_REQUIRE);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
