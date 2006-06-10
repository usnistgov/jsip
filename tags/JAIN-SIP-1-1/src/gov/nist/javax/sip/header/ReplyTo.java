/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import javax.sip.header.*;

/**  
 * ReplyTo Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class ReplyTo
	extends AddressParametersHeader
	implements ReplyToHeader {

	/** Default constructor
	 */
	public ReplyTo() {
		super(NAME);
	}

	/** Default constructor given an address.
	 *
	 *@param address -- address of this header.
	 *
	 */
	public ReplyTo(AddressImpl address) {
		super(NAME);
		this.address = address;
	}

	/**
	 * Encode the header into a String.
	 * @return String
	 */
	public String encode() {
		return headerName + COLON + SP + encodeBody() + NEWLINE;
	}

	/**
	 * Encode the header content into a String.
	 * @return String
	 */
	public String encodeBody() {
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
	 * Conveniance accessor function to get the hostPort field from the address
	 * @return HostPort
	 */
	public HostPort getHostPort() {
		return address.getHostPort();
	}

	/**
	 * Get the display name from the address.
	 * @return String
	 */
	public String getDisplayName() {
		return address.getDisplayName();
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
