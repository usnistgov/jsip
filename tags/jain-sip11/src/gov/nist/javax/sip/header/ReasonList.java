/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import javax.sip.header.*;

/**
* List of Reason headers.
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*
*/
public final class ReasonList extends SIPHeaderList {

        /** Default constructor
         */    
	public ReasonList () {
		   super( Reason.class, ReasonHeader.NAME);
	}

         /** Constructor
         * @param sip SIPObjectList to set
         */    
	public ReasonList (SIPObjectList sip) {
		super(sip, ReasonHeader.NAME);
	}
        

}

