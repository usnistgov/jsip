/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
/**
* email address field of the SDP header.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change)
*
*@author Oliver Deruelle <deruelle@antd.nist.gov> 
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class EmailAddress extends SDPObject {
    protected String displayName;
    protected Email  email;

    public String getDisplayName() 
    { return displayName ; } 
    /**
     * Set the displayName member  
     */
    public void setDisplayName(String displayName) 
    { this.displayName = displayName ; } 
    /**
     * Set the email member  
     */
    public void setEmail(Email email) 
    { this.email = email ; } 

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     * Here, we implement only the "displayName <email>" form
     * and not the "email (displayName)" form
     */
    public String encode() {
	String encoded_string;
    
	if (displayName != null) {
	    encoded_string = displayName + Separators.LESS_THAN;
	} else {
	    encoded_string = "";
	}
	encoded_string += email.encode();
	if (displayName != null) {
	    encoded_string +=  Separators.GREATER_THAN;
	}
	return encoded_string;
    }

}
