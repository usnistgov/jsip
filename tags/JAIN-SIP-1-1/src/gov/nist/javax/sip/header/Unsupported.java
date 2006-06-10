/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import java.text.ParseException;

/**
 * the Unsupported header. 
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:30 $
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Unsupported
	extends SIPHeader
	implements javax.sip.header.UnsupportedHeader {

	/** option-Tag field.
	 */
	protected String optionTag;

	/** Default Constructor.
	 */
	public Unsupported() {
		super(NAME);
	}

	/** Constructor
	 * @param ot String to set
	 */
	public Unsupported(String ot) {
		super(NAME);
		optionTag = ot;
	}

	/**
	 * Return a canonical value.
	 * @return String.
	 */
	public String encodeBody() {
		return optionTag;
	}

	/** get the option tag field
	 * @return option Tag field
	 */
	public String getOptionTag() {
		return optionTag;
	}

	/**
	 * Set the option member
	 * @param o String to set
	 */
	public void setOptionTag(String o) throws ParseException {
		if (o == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ " Unsupported, setOptionTag(), The option tag parameter is null");
		optionTag = o;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
