/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
*  Content Encoding SIP header List. Keeps a list of ContentEncoding headers
*/
public final class ContentEncodingList extends SIPHeaderList {
    
        /** Default constructor.
         */    
	public ContentEncodingList () {
		super( ContentEncoding.class,
			ContentEncodingHeader.NAME);
	}
        
}
