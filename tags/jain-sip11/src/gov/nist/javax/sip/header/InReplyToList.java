/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
* In-Reply-To SIP header. Keeps a list of CallIdentifiers
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public final class InReplyToList extends SIPHeaderList {
    
        /** Default constructor
         */    
	public InReplyToList() {
		super( InReplyTo.class, InReplyToHeader.NAME);
	}
        
}
