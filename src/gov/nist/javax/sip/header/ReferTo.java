
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import java.util.Iterator;
import javax.sip.address.*;
import java.text.ParseException;


/**  
*ReferTo SIP Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public final class ReferTo  extends AddressParametersHeader implements 
javax.sip.header.ReferToHeader {

        /** default Constructor.
         */
    public ReferTo() {
        super(NAME);
    }



   /**
    * Encode the header content into a String.
    * @return String
    */
    protected String encodeBody() {
        if (address==null) return null;
	String retval = "";
        if ( address.getAddressType() == 
		AddressImpl.ADDRESS_SPEC) {
            retval += LESS_THAN;
        }
        retval += address.encode();
        if (address.getAddressType() == 
		AddressImpl.ADDRESS_SPEC) {
            retval += GREATER_THAN;
        }

        if (!parameters.isEmpty() ) {
            retval += SEMICOLON +parameters.encode();
        }
        return retval;
    }


}
