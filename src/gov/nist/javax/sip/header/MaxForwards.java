/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;
import javax.sip.InvalidArgumentException;

/**
 * MaxForwards SIPHeader
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2004-12-13 01:28:26 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class MaxForwards extends SIPHeader implements MaxForwardsHeader {

	/** maxForwards field.
	 */
	protected int maxForwards;

	/** Default constructor.
	 */
	public MaxForwards() {
		super(NAME);
	}

	/** get the MaxForwards field.
	 * @return the maxForwards member.
	 */
	public int getMaxForwards() {
		return maxForwards;
	}

	/**
	     * Set the maxForwards member
	     * @param maxForwards maxForwards parameter to set
	     */
	public void setMaxForwards(int maxForwards)
		throws InvalidArgumentException {
		if (maxForwards < 0 || maxForwards > 255)
			throw new InvalidArgumentException(
				"bad max forwards value " + maxForwards);
		this.maxForwards = maxForwards;
	}

	/**
	     * Encode into a string.
	     * @return encoded string.
	     *
	     */
	public String encodeBody() {
		return new Integer(maxForwards).toString();
	}

	/** Boolean function
	 * @return true if MaxForwards field reached zero.
	 */
	public boolean hasReachedZero() {
		return maxForwards == 0;
	}

	/** decrement MaxForwards field one by one.
	 */
	public void decrementMaxForwards() {
		if (maxForwards > 0)
			maxForwards--;
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
