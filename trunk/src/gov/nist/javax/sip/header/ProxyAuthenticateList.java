/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;


/**
* List of ProxyAuthenticate headers.
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ProxyAuthenticateList extends SIPHeaderList {
    
        /** Default constructor
         */    
	public ProxyAuthenticateList() {
		super( ProxyAuthenticate.class,
				ProxyAuthenticateHeader.NAME);
	}
        
}
