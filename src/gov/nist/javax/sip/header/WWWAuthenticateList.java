/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;


/**
* WWWAuthenticate SIPHeader (of which there can be several?)
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class WWWAuthenticateList extends SIPHeaderList {
     
        /**
         * constructor.
         */
    public WWWAuthenticateList () {
        super( WWWAuthenticate.class, WWWAuthenticateHeader.NAME);
    }
        
}

