/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;

import java.util.*;
import java.text.ParseException;
import javax.sip.header.*;

/**
* Authorization SIP header.
*
* @see ProxyAuthorization
*
* @author M. Ranganathan <mranga@nist.gov>  NIST/ITL/ANTD <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class Authorization extends 
	gov.nist.javax.sip.header.AuthenticationHeader
    implements javax.sip.header.AuthorizationHeader{
    
        
        /** Default constructor.
         */        
	public Authorization() {
		super(AuthorizationHeader.NAME);
	}

        
}
