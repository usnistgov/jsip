/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
* AlertInfo SIPHeader - there can be several AlertInfo headers.
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class AlertInfoList extends SIPHeaderList {
    
        /** default constructor
         */    
	public AlertInfoList() {
		super( AlertInfo.class,AlertInfoHeader.NAME);
	}
        
        
}
