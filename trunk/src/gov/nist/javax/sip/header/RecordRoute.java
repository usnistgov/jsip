/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.javax.sip.address.*;

/**
 * The Request-Route header is added to a request by any proxy that insists on
 * being in the path of subsequent requests for the same call leg.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class RecordRoute
	extends AddressParametersHeader
	implements javax.sip.header.RecordRouteHeader {

	/**
	 * constructor
	 * @param address address to set
	 */
	public RecordRoute(AddressImpl address) {
		super(NAME);
		this.address = address;
	}

	/**
	 * default constructor
	 */
	public RecordRoute() {
		super(RECORD_ROUTE);

	}

	/** Encode into canonical form.
	 *@return String containing the canonicaly encoded header.
	 */
	public String encodeBody() {
		StringBuffer retval = new StringBuffer();
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval.append(LESS_THAN);
		}
		retval.append(address.encode());
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval.append(GREATER_THAN);
		}

		if (!parameters.isEmpty())
			retval.append(SEMICOLON + this.parameters.encode());
		return retval.toString();
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
