package gov.nist.javax.sip.header;

import javax.sip.address.*;
import gov.nist.javax.sip.address.*;

/** An abstract class for headers that take an address and parameters. 
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:48 $
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

	public Object clone() {
		AddressParametersHeader retval = (AddressParametersHeader) super.clone();
		if (this.address != null)
			retval.address = (AddressImpl) this.address.clone();
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:29  sverker
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
