/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import java.util.Iterator;
import javax.sip.address.*;
import java.text.ParseException;
/**
 *From SIP Header.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public final class From extends AddressParametersHeader
implements javax.sip.header.FromHeader {
    
 
    /** Default constructor
     */
    public From() {
        super(NAME);
    }
    
    /** Generate a FROM header from a TO header
     */
    public From(To to) {
        super(NAME);
        address = to.address;
        parameters = to.parameters;
    }
    
    
    
    
    
    /**
     * Compare two To headers for equality.
     * @param otherHeader Object to set
     * @return true if the two headers are the same.
     */
    public boolean equals(Object otherHeader) {
        try {
            if (!otherHeader.getClass().equals(this.getClass())){
                return false;
            }
            
            From otherTo = (From) otherHeader;
            if (! otherTo.getAddress().equals(address)) {
                return false;
            }
            return true;
            // exitpoint = 3;
            // return parms.equals(otherTo.parms);
        } finally {
            // System.out.println("equals " + retval + exitpoint);
        }
    }
    
    /**
     * Encode the header into a String.
     *
     * @return String
     */
    public String encode() {
        return headerName + COLON + SP + encodeBody() + NEWLINE;
    }
    
    /**
     * Encode the header content into a String.
     *
     * @return String
     */
    protected String encodeBody() {
        String retval = "";
        if (address.getAddressType() ==
            AddressImpl.ADDRESS_SPEC) {
            retval += LESS_THAN;
        }
        retval += address.encode();
        if (address.getAddressType() ==
            AddressImpl.ADDRESS_SPEC) {
            retval += GREATER_THAN;
        }
        if (!parameters.isEmpty() ) {
            retval += SEMICOLON + parameters.encode();
        }
        return retval;
    }
    
   
    
    
    /**
     * Conveniance accessor function to get the hostPort field from the address.
     * Warning -- this assumes that the embedded URI is a SipURL.
     *
     * @return hostport field
     */
    public HostPort getHostPort() {
        return address.getHostPort();
    }
    
    
    /**
     * Get the display name from the address.
     * @return Display name
     */
    public String getDisplayName() {
        return address.getDisplayName();
    }
    
    
    /**
     * Get the tag parameter from the address parm list.
     * @return tag field
     */
    public String getTag() {
        if ( parameters ==null) return null;
        return getParameter(ParameterNames.TAG);
    }
    
    
    
    /** Boolean function
     * @return true if the Tag exist
     */
    public boolean hasTag() {
        return hasParameter(ParameterNames.TAG);
    }
    
    /** remove Tag member
     */
    public void removeTag() {
       parameters.delete(ParameterNames.TAG);
    }
    
    
    /**
     * Set the address member
     * @param address Address to set
     */
    public void setAddress(javax.sip.address.Address address) {
        this.address = (AddressImpl) address;
    }
    
    /**
     * Set the tag member
     * @param t tag to set. From tags are mandatory.
     */
    public void setTag(String t) throws ParseException{
        if (t == null) throw new  NullPointerException("null tag ");
	else if (t.trim().equals("")) throw new ParseException("bad tag",0);
        this.setParameter(ParameterNames.TAG,t);
    }
    
    
    
    
    /** Get the user@host port string.
     */
    public String getUserAtHostPort() {
        return address.getUserAtHostPort();
    }
    
    
    
    
    /** Gets a string representation of the Header. This method overrides the
     * toString method in java.lang.Object.
     *
     * @return string representation of Header
     */
    public String toString() {
        return this.encode();
    }
    
   
    
    
}
