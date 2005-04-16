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
	protected Email email;

	public String getDisplayName() {
		return displayName;
	}
	/**
	 * Set the displayName member  
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * Set the email member  
	 */
	public void setEmail(Email email) {
		this.email = email;
	}

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
			encoded_string += Separators.GREATER_THAN;
		}
		return encoded_string;
	}
	public Object clone() {
		EmailAddress retval = (EmailAddress) super.clone();
		if (this.email != null)
			retval.email = (Email) this.email.clone();
		return retval;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:27  sverker
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
