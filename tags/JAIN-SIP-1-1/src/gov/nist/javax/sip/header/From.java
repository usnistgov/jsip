/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**
 * From SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-08-23 23:56:20 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class From
	extends AddressParametersHeader
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
			if (!otherHeader.getClass().equals(this.getClass())) {
				return false;
			}

			From otherTo = (From) otherHeader;
			if (!otherTo.getAddress().equals(address)) {
				return false;
			}
			return true;
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
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += LESS_THAN;
		}
		retval += address.encode();
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += GREATER_THAN;
		}
		if (!parameters.isEmpty()) {
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
		if (parameters == null)
			return null;
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
	public void setTag(String t) throws ParseException {
		if (t == null)
			throw new NullPointerException("null tag ");
		else if (t.trim().equals(""))
			throw new ParseException("bad tag", 0);
		this.setParameter(ParameterNames.TAG, t);
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
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2004/01/22 13:26:29  sverker
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
