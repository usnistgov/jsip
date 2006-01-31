/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**  
*To SIP Header.
*
*@version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2006-01-31 06:56:21 $
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public final class To
	extends AddressParametersHeader
	implements javax.sip.header.ToHeader {

	/** default Constructor.
	 */
	public To() {
		super(TO);
	}

	/** Generate a TO header from a FROM header
	*/
	public To(From from) {
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
			if (address == null)
				return false;
			if (!otherHeader.getClass().equals(this.getClass())) {
				return false;
			}

			To otherTo = (To) otherHeader;
			if (!otherTo.getAddress().equals(address)) {
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
		if (address == null)
			return null;
		String retval = "";
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += LESS_THAN;
		}
		retval += address.encode();
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += GREATER_THAN;
		}

		if (!parameters.isEmpty()) {
		  synchronized(parameters) {
			  retval += SEMICOLON + parameters.encode();
			}
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
		if (address == null)
			return null;
		return address.getHostPort();
	}

	/**
	 * Get the display name from the address.
	 * @return Display name
	 */
	public String getDisplayName() {
		if (address == null)
			return null;
		return address.getDisplayName();
	}

	/**
	 * Get the tag parameter from the address parm list.
	 * @return tag field
	 */
	public String getTag() {
		if (parameters == null)
			return null;
		synchronized (this.parameters) {
			return getParameter(ParameterNames.TAG);
		}
	}

	/** Boolean function
	 * @return true if the Tag exist
	 */
	public boolean hasTag() {
		if (parameters == null)
			return false;
	        synchronized (this.parameters) {
		   return hasParameter(ParameterNames.TAG);
		}
	}

	/** remove Tag member
	 */
	public void removeTag() {
	      synchronized (this.parameters) {
		if (parameters != null)
			parameters.delete(ParameterNames.TAG);
	      }
	}

	/**
	 * Set the tag member. This should be set to null for the initial request
	 * in a dialog.
	 * @param t tag String to set.
	 */
	public void setTag(String t) throws ParseException {
		synchronized (this.parameters) {
		   if (t == null)
			throw new NullPointerException("null tag ");
		   else if (t.trim().equals(""))
			throw new ParseException("bad tag", 0);
		   this.setParameter(ParameterNames.TAG, t);
		}
	}

	/** Get the user@host port string.
	 */
	public String getUserAtHostPort() {
		if (address == null)
			return null;
		return address.getUserAtHostPort();
	}

	/** Gets a string representation of the Header. 
	 * This method overrides the
	 * toString method in java.lang.Object.
	 *
	 * @return string representation of Header
	 */
	public String toString() {
		return this.encode();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/06/17 15:22:29  mranga
 * Reviewed by:   mranga
 *
 * Added buffering of out-of-order in-dialog requests for more efficient
 * processing of such requests (this is a performance optimization ).
 *
 * Revision 1.3  2004/01/22 13:26:30  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
