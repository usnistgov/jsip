/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;

/** Information field implementation 
*@version  JSR141-PUBLIC-REVIEW (subject to change)
*
*@author Oliver Deruelle <deruelle@antd.nist.gov> 
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*/

public class InformationField extends SDPField implements javax.sdp.Info {
	protected String information;

	public InformationField() {
		super(INFORMATION_FIELD);
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String info) {
		information = info;
	}

	/**
	 *  Get the string encoded version of this object
	 * @since v1.0
	 */
	public String encode() {
		return INFORMATION_FIELD + information + Separators.NEWLINE;
	}

	/** Returns the value.
	 * @throws SdpParseException
	 * @return the value
	 */
	public String getValue() throws SdpParseException {
		return information;
	}

	/** Set the value.
	 * @param value to set
	 * @throws SdpException if the value is null
	 */
	public void setValue(String value) throws SdpException {
		if (value == null)
			throw new SdpException("The value is null");
		else {
			setInformation(value);
		}
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
