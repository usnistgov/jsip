/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import javax.sip.header.*;

/**
*   List of Unsupported headers.
*/
public class UnsupportedList extends SIPHeaderList {
    
    /** Default Constructor
     */
    public UnsupportedList () {
        super( Unsupported.class, UnsupportedHeader.NAME);
	}
        
}

