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
*To SIP Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public final class To  extends AddressParametersHeader implements 
javax.sip.header.ToHeader {

        /** default Constructor.
         */
    public To() {
        super(TO);
    }

	/** Generate a TO header from a FROM header
	*/
     public To (From from) {
	super(TO);
	setAddress(from.address);
	setParameters(from.parameters);
     }
    
    /**
     * Compare two To headers for equality.
     * @param otherHeader Object to set
     * @return true if the two headers are the same.
     */
    public boolean equals(Object otherHeader) {
	try {
          if (address==null) return false;
          if (!otherHeader.getClass().equals(this.getClass())){
	      return false;
           }

          To otherTo = (To) otherHeader;
          if (! otherTo.getAddress().equals( address )) {
	      return false;
          }
	  return true;
	  // exitpoint = 3;
	} finally {
	    // System.out.println("equals " + retval + exitpoint);
	}
    }

   /**
    * Encode the header into a String.
    * @since 1.0
    * @return String
    */
    public String encode() {
        return headerName + COLON + SP + encodeBody() + NEWLINE;
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

   /**
    * Conveniance accessor function to get the hostPort field from the address.
    * Warning -- this assumes that the embedded URI is a SipURL.
    *
    * @return hostport field
    */
    public HostPort getHostPort() {
        if (address==null) return null;
        return address.getHostPort();
    }

   /**
    * Get the display name from the address.
    * @return Display name
    */
    public String getDisplayName() {
        if (address==null) return null;
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
        if ( parameters ==null) return false;
        return hasParameter(ParameterNames.TAG);
    }
    
      /** remove Tag member
       */
    public void removeTag() {
        if ( parameters  !=null) parameters.delete(ParameterNames.TAG);
    }   

   /**
    * Set the tag member. This should be set to null for the initial request
    * in a dialog.
    * @param t tag String to set.
    */
    public void setTag(String t) throws ParseException {
        if (t == null) throw new  NullPointerException("null tag ");
	else if (t.trim().equals("")) throw new ParseException("bad tag" ,0);
        this.setParameter(ParameterNames.TAG,t);
    }    

   /** Get the user@host port string.
    */
    public String getUserAtHostPort() {
        if (address==null) return null;
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
