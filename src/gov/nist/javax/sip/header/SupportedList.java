/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import javax.sip.header.*;
/**
* A list of supported headers.
*@version 1.0
*@see Supported
*/

public class SupportedList extends SIPHeaderList {
    
        /** Default Constructor
         */    
	public SupportedList () {
		super( Supported.class, SupportedHeader.NAME);
	}

}
