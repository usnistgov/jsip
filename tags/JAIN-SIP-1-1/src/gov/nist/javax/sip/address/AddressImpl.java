/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/
package gov.nist.javax.sip.address;
import gov.nist.core.*;
import javax.sip.address.*;

/**
 * Address structure. Imbeds a URI and adds a display name.
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *@version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2005-04-16 20:38:46 $
 *
 */
public final class AddressImpl
	extends NetObject
	implements javax.sip.address.Address {

	/** Constant field.
	 */
	public static final int NAME_ADDR = 1;

	/** constant field.
	 */
	public static final int ADDRESS_SPEC = 2;

	/** Constant field.
	 */
	public static final int WILD_CARD = 3;

	protected int addressType;

	/** displayName field
	 */
	protected String displayName;

	/** address field
	 */
	protected GenericURI address;

	/** Match on the address only.
	 * Dont care about the display name.
	 */

	public boolean match(Object other) {
		// TODO -- add the matcher;
		if (other == null)
			return true;
		if (!(other instanceof Address))
			return false;
		else {
			AddressImpl that = (AddressImpl) other;
			if (that.getMatcher() != null)
				return that.getMatcher().match(this.encode());
			else if (that.displayName != null && this.displayName == null)
				return false;
			else if (that.displayName == null)
				return address.match(that.address);
			else
				return displayName.equalsIgnoreCase(that.displayName)
					&& address.match(that.address);
		}

	}

	/** Get the host port portion of the address spec.
	 *@return host:port in a HostPort structure.
	 */
	public HostPort getHostPort() {
		if (!(address instanceof SipUri))
			throw new RuntimeException("address is not a SipUri");
		SipUri uri = (SipUri) address;
		return uri.getHostPort();
	}

	/** Get the port from the imbedded URI. This assumes that a SIP URL
	 * is encapsulated in this address object.
	 *
	 *@return the port from the address.
	 *
	 */
	public int getPort() {
		if (!(address instanceof SipUri))
			throw new RuntimeException("address is not a SipUri");
		SipUri uri = (SipUri) address;
		return uri.getHostPort().getPort();
	}

	/** Get the user@host:port for the address field. This assumes
	 * that the encapsulated object is a SipUri. 
	 *
	 * BUG Fix from Antonis Kadris.
	 *
	 *@return string containing user@host:port.
	 */
	public String getUserAtHostPort() {
		if (address instanceof SipUri) {
			SipUri uri = (SipUri) address;
			return uri.getUserAtHostPort();
		} else
			return address.toString();
	}

	/** Get the host name from the address.
	 *
	 *@return the host name.
	 */
	public String getHost() {
		if (!(address instanceof SipUri))
			throw new RuntimeException("address is not a SipUri");
		SipUri uri = (SipUri) address;
		return uri.getHostPort().getHost().getHostname();
	}

	/** Remove a parameter from the address.
	 *
	 *@param parameterName is the name of the parameter to remove.
	 */
	public void removeParameter(String parameterName) {
		if (!(address instanceof SipUri))
			throw new RuntimeException("address is not a SipUri");
		SipUri uri = (SipUri) address;
		uri.removeParameter(parameterName);
	}

	/**
	 * Encode the address as a string and return it.
	 * @return String canonical encoded version of this address.
	 */
	public String encode() {
		if (this.addressType == WILD_CARD)
			return "*";
		StringBuffer encoding = new StringBuffer();
		if (displayName != null) {
			encoding.append(DOUBLE_QUOTE).append(displayName).append(
				DOUBLE_QUOTE).append(
				SP);
		}
		if (address != null) {
			if (addressType == NAME_ADDR || displayName != null)
				encoding.append(LESS_THAN);
			encoding.append(address.encode());
			if (addressType == NAME_ADDR || displayName != null)
				encoding.append(GREATER_THAN);
		}
		return encoding.toString();
	}

	public AddressImpl() {
		this.addressType = NAME_ADDR;
	}

	/**
	 * Get the address type;
	 * @return int
	 */
	public int getAddressType() {
		return addressType;
	}

	/**
	 * Set the address type. The address can be NAME_ADDR, ADDR_SPEC or
	 * WILD_CARD
	 *
	 * @param atype int to set
	 *
	 */
	public void setAddressType(int atype) {
		addressType = atype;
	}

	/**
	 * get the display name
	 *
	 * @return String
	 *
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Set the displayName member
	 *
	 * @param displayName String to set
	 *
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		this.addressType = NAME_ADDR;
	}

	/**
	 * Set the address field
	 *
	 * @param address SipUri to set
	 *
	 */
	public void setAddess(javax.sip.address.URI address) {
		address = (GenericURI) address;
	}

	/**
	 * Compare two address specs for equality.
	 *
	 * @param other Object to compare this this address
	 *
	 * @return boolean
	 *
	 */
	public boolean equals(Object other) {

		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		AddressImpl that = (AddressImpl) other;
		if (this.addressType == WILD_CARD && that.addressType != WILD_CARD)
			return false;

		// Ignore the display name; only compare the address spec.
		boolean retval = this.address.equals(that.address);
		return retval;
	}

	/** return true if DisplayName exist.
	 *
	 * @return boolean
	 */
	public boolean hasDisplayName() {
		return (displayName != null);
	}

	/** remove the displayName field
	 */
	public void removeDisplayName() {
		displayName = null;
	}

	/** Return true if the imbedded URI is a sip URI.
	 *
	 * @return true if the imbedded URI is a SIP URI.
	 *
	 */
	public boolean isSIPAddress() {
		return address instanceof SipUri;
	}

	/** Returns the URI address of this Address. The type of URI can be
	 * determined by the scheme.
	 *
	 * @return address parmater of the Address object
	 */
	public URI getURI() {
		return this.address;
	}

	/** This determines if this address is a wildcard address. That is
	 * <code>Address.getAddress.getUserInfo() == *;</code>
	 *
	 * @return true if this name address is a wildcard, false otherwise.
	 */
	public boolean isWildcard() {
		return this.addressType == WILD_CARD;
	}

	/** Sets the URI address of this Address. The URI can be either a
	 * TelURL or a SipURI.
	 *
	 * @param address - the new URI address value of this NameAddress.
	 */
	public void setURI(URI address) {
		this.address = (GenericURI) address;
	}

	/** Set the user name for the imbedded URI.
	 *
	 *@param user -- user name to set for the imbedded URI.
	 */
	public void setUser(String user) {
		((SipUri) this.address).setUser(user);
	}

	/** Mark this a wild card address type.
	 * Also set the SIP URI to a special wild card address.
	 */
	public void setWildCardFlag() {
		this.addressType = WILD_CARD;
		this.address = new SipUri();
		((SipUri)this.address).setUser("*");
	}

	public Object clone() {
		AddressImpl retval = (AddressImpl) super.clone();
		if (this.address != null)
			retval.address = (GenericURI) this.address.clone();
		return retval;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2004/11/19 19:16:05  mranga
 * Issue number:  41
 * Obtained from:
 * Submitted by:
 * Reviewed by:   mranga
 *
 * Wild card address behavior getUser should return *
 *
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
