/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
/**
* email field in the SDP announce.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*/
public class EmailField extends SDPField implements javax.sdp.EMail {

	protected EmailAddress emailAddress;

	public EmailField() {
		super(SDPFieldNames.EMAIL_FIELD);
		emailAddress = new EmailAddress();
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}
	/**
	 * Set the emailAddress member  
	 */
	public void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 *  Get the string encoded version of this object
	 * @since v1.0
	 */
	public String encode() {
		return EMAIL_FIELD + emailAddress.encode() + Separators.NEWLINE;
	}

	public String toString() {
		return this.encode();
	}

	/** Returns the value.
	 * @throws SdpParseException
	 * @return the value
	 */
	public String getValue() throws SdpParseException {
		if (emailAddress == null)
			return null;
		else {
			return emailAddress.getDisplayName();
		}
	}

	/** Set the value.
	 * @param value to set
	 * @throws SdpException if the value is null
	 */
	public void setValue(String value) throws SdpException {
		if (value == null)
			throw new SdpException("The value is null");
		else {

			emailAddress.setDisplayName(value);
		}
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
