/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

/** The WWWAuthenticate SIP header.
*
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
* @see WWWAuthenticateList SIPHeader which strings these together.
*/

public class WWWAuthenticate extends AuthenticationHeader  implements
WWWAuthenticateHeader {
    
        /**
         * Default Constructor.
         */
    public WWWAuthenticate() {
	super(NAME);
    }
    
}
