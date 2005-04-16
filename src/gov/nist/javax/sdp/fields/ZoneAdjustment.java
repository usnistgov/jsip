/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;

/**
* Zone adjustment class.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ZoneAdjustment extends SDPObject {
	protected long time;
	protected String sign;
	protected TypedTime offset;

	/**
	* Set the time.
	*@param t time to set.
	*/
	public void setTime(long t) {
		time = t;
	}

	/**
	* Get the time.
	*/
	public long getTime() {
		return time;
	}

	/**
	* get the offset.
	*/
	public TypedTime getOffset() {
		return offset;
	}

	/**
	* Set the offset.
	*@param off typed time offset to set.
	*/
	public void setOffset(TypedTime off) {
		offset = off;
	}

	/**
	* Set the sign.
	*@param s sign for the offset.
	*/
	public void setSign(String s) {
		sign = s;
	}

	/**
	* Encode this structure into canonical form.
	*@return encoded form of the header.
	*/
	public String encode() {
		String retval = new Long(time).toString();
		retval += Separators.SP;
		if (sign != null)
			retval += sign;
		retval += offset.encode();
		return retval;
	}

	public Object clone() {
		ZoneAdjustment retval = (ZoneAdjustment) super.clone();
		if (this.offset != null)
			retval.offset = (TypedTime) this.offset.clone();
		return retval;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
