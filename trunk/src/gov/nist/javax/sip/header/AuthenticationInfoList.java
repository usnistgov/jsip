/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;


/**
* A list of AuthenticationInfo headers (there can be multiple in a message).
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="${docRoot}/uncopyright.html">This code is in the public domain.</a>
*/
public class AuthenticationInfoList extends SIPHeaderList{
    
    /** Creates a new instance of AuthenticationList */
    public AuthenticationInfoList() {
        super( AuthenticationInfo.class, AuthenticationInfoHeader.NAME);
    }
    
}
