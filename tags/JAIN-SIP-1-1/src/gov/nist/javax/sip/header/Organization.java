/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.*;

/**
 * Organization SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Organization extends SIPHeader implements OrganizationHeader {

	/**
	 * Organization field
	 */
	protected String organization;

	/**
	 * Return encoding of value of the header.
	 * @return String
	 */
	public String encodeBody() {
		return organization;
	}

	/**
	 * Default constructor
	 */
	public Organization() {
		super(ORGANIZATION);
	}

	/**
	 * Get the organization field.
	 * @return String
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Set the organization member
	 * @param o String to set
	 */
	public void setOrganization(String o) throws ParseException {
		if (o == null)
			throw new NullPointerException(
				"JAIN-SIP Exception,"
					+ " Organization, setOrganization(), the organization parameter is null");
		organization = o;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
