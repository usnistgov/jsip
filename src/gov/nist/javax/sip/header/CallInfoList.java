/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;


/**
* A list of CallInfo headers (there can be multiple in a message).
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="${docRoot}/uncopyright.html">This code is in the public domain.</a>
*/
public class CallInfoList extends SIPHeaderList {
    
        /** Default constructor
         */    
	public CallInfoList() {
		super( CallInfo.class,CallInfoHeader.NAME);
	}

}
