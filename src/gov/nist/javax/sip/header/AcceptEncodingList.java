/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import javax.sip.header.*;

/**
* AcceptEncodingList of AccepEncoding headers.
*
*@author M. Ranganathan <mranga@nist.gov> 
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*/
public class AcceptEncodingList extends SIPHeaderList {
    
        /** default constructor
         */    
	public AcceptEncodingList () {
    	  super (AcceptEncoding.class, AcceptEncodingHeader.NAME);
	}


}

