package gov.nist.javax.sip.header;

import javax.sip.address.*;
import gov.nist.javax.sip.address.*;

/** An abstract class for headers that take an address and parameters. 
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public abstract class AddressParametersHeader extends ParametersHeader {

	protected AddressImpl address;

	/**
	 * get the Address field
	 * @return the imbedded  Address
	 */
	public Address getAddress() {
		return address;
	}

	/**
	 * set the Address field
	 * @param address Address to set
	 */
	public void setAddress(Address address) {
		this.address = (AddressImpl) address;
	}

	/**
	 * Constructor given the name of the header.
	 */
	protected AddressParametersHeader(String name) {
		super(name);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
