/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.address.*;
import java.util.*;

/**
 *Route  SIPHeader Object
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public class Route extends AddressParametersHeader 
	implements javax.sip.header.RouteHeader {
    
    
    /** Default constructor
     */
    public Route() {
        super(NAME);
    }

    /** Default constructor given an address.
     *
     *@param address -- address of this header.
     *
     */

    public Route(AddressImpl address) {
	super(NAME);
	this.address = address;
     }
    
    /**
     * Equality predicate.
     * Two routes are equal if their addresses are equal.
     *
     *@param that is the other object to compare with.
     *@return true if the route addresses are equal.
     */
    public boolean equals(Object that) {
        if (! this.getClass().equals(that.getClass())) return false;
        Route thatRoute = (Route) that;
        return  this.address.getHostPort().
        equals(thatRoute.address.getHostPort());
    }
    
    /**
     * Hashcode so this header can be inserted into a set.
     *
     *@return the hashcode of the encoded address.
     */
    public int hashCode() {
        return this.address.getHostPort().encode().toLowerCase().hashCode();
    }
    
    
    
    
    /**
     * Encode into canonical form.
     * Acknowledgement: contains a bug fix for a bug reported by
     * Laurent Schwizer
     *
     *@return a canonical encoding of the header.
     */
    public String encodeBody() {
        boolean addrFlag =
        address.getAddressType() ==
                AddressImpl.NAME_ADDR;
        StringBuffer encoding =  new StringBuffer();
        if (!addrFlag) {
            encoding.append( "<").
            append(address.encode()).
            append( ">");
        } else {
            encoding.append(address.encode());
        }
        if (!parameters.isEmpty()) {
            encoding.append(SEMICOLON).
            append(parameters.encode());
        }
        return encoding.toString();
    }

    
}

