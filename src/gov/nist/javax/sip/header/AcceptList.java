/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;


/**
* Accept List of  SIP headers. 
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*@see Accept
*/
public class AcceptList extends SIPHeaderList {

        /** default constructor
         */    
        public AcceptList() {
		super( Accept.class,AcceptHeader.NAME);
	}
        
}
