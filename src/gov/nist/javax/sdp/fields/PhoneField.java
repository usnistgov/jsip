/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
/**
* Phone Field SDP header
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class PhoneField extends SDPField  implements javax.sdp.Phone {
	protected String name;
	protected String phoneNumber;

	public PhoneField() {
		super(PHONE_FIELD);
	}

	public	 String getName() 
 	 	{ return name ; } 
	public	 String getPhoneNumber() 
 	 	{ return phoneNumber ; } 
	/**
	* Set the name member  
        *
        *@param name - the name to set.
	*/
	public	 void setName(String name) 
 	 	{ this.name = name ; } 
	/**
	* Set the phoneNumber member 
        *@param phoneNumber - phone number to set. 
	*/
	public	 void setPhoneNumber(String phoneNumber) 
 	 	{ this.phoneNumber = phoneNumber ; } 

    /** Returns the value.
     * @throws SdpParseException
     * @return the value.
     */    
    public String getValue()
    throws SdpParseException {
        return getName();
    }
    
    /** Sets the value.
     * @param value the - new information.
     * @throws SdpException if the value is null
     */    
    public void setValue(String value)
    throws SdpException{
        if (value==null) throw new SdpException("The value parameter is null");
        else setName(value);    
    }
    

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     * Here, we implement only the "name <phoneNumber>" form
     * and not the "phoneNumber (name)" form
     */
    public String encode() {
        String encoded_string;
	encoded_string = PHONE_FIELD;
	if (name != null) {
	    encoded_string += name + Separators.LESS_THAN;
	}
	encoded_string += phoneNumber;
	if (name != null) {
	    encoded_string +=  Separators.GREATER_THAN;
	}
	encoded_string += Separators.NEWLINE;
	return encoded_string;
    }

}
