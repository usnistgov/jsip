/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import java.util.*;
import javax.sip.header.*;

/**
* Date Header.
*
*@version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:50 $
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class SIPDateHeader extends SIPHeader implements DateHeader {

	/** date field
	 */
	protected SIPDate date;

	/** Default constructor.
	 */
	public SIPDateHeader() {
		super(DATE);
	}

	/** Encode the header into a String.
	 * @return String
	 */
	public String encodeBody() {
		return date.encode();
	}

	/**
	 * Set the date member
	 * @param d SIPDate to set
	 */
	public void setDate(SIPDate d) {
		date = d;

	}

	/**
	 * Sets date of DateHeader. The date is repesented by the Calendar object.
	 *
	 * @param dat the Calendar object date of this header.
	 */
	public void setDate(Calendar dat) {
		if (dat != null)
			date = new SIPDate(dat.getTime().getTime());
	}

	/**
	 * Gets the date of DateHeader. The date is repesented by the Calender
	 * object.
	 *
	 * @return the Calendar object representing the date of DateHeader
	 */
	public Calendar getDate() {
		if (date == null)
			return null;
		return date.getJavaCal();
	}

	public Object clone() {
		SIPDateHeader retval = (SIPDateHeader) super.clone();
		if (this.date != null)
			retval.date = (SIPDate) this.date.clone();
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
