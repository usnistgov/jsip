/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.InvalidArgumentException;
import java.text.ParseException;

/**
 * RAck SIP Header implementation
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2004-04-06 11:11:59 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class RAck extends SIPHeader implements javax.sip.header.RAckHeader {

	protected int cSeqNumber;

	protected int rSeqNumber;

	protected String method;

	/** Creates a new instance of RAck */
	public RAck() {
		super(NAME);
	}

	/**
	 * Encode the body of this header (the stuff that follows headerName).
	 * A.K.A headerValue.
	 * 
	 */
	protected String encodeBody() {
		// Bug reported by Bruno Konik - was encoded in
		// the wrong order.
		return new StringBuffer()
			.append(rSeqNumber)
			.append(SP)
			.append(cSeqNumber)
			.append(SP)
			.append(method)
			.toString();

	}

	/**
	 * Gets the CSeq sequence number of this RAckHeader.
	 *
	 * @return the integer value of the cSeq number of the RAckHeader
	 */
	public int getCSeqNumber() {
		return cSeqNumber;
	}

	/**
	 * Gets the method of RAckHeader
	 *
	 * @return method of RAckHeader
	 */
	public String getMethod() {
		return this.method;
	}

	/**
	 * Gets the RSeq sequence number of this RAckHeader.
	 *
	 * @return the integer value of the RSeq number of the RAckHeader
	 */
	public int getRSeqNumber() {
		return rSeqNumber;
	}

	/**
	 * Sets the sequence number value of the CSeqHeader of the provisional
	 * response being acknowledged. The sequence number MUST be expressible as
	 * a 32-bit unsigned integer and MUST be less than 2**31.
	 *
	 * @param cSeqNumber - the new cSeq number of this RAckHeader
	 * @throws InvalidArgumentException if supplied value is less than zero.
	 */
	public void setCSeqNumber(int cSeqNumber) throws InvalidArgumentException {
		if (cSeqNumber <= 0)
			throw new InvalidArgumentException("Bad CSeq # " + cSeqNumber);
		this.cSeqNumber = cSeqNumber;
	}

	/**
	 * Sets the method of RAckHeader, which correlates to the method of the
	 * CSeqHeader of the provisional response being acknowledged.
	 *
	 * @param method - the new string value of the method of the RAckHeader
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the method value.
	 */
	public void setMethod(String method) throws ParseException {
		this.method = method;
	}

	/**
	 * Sets the sequence number value of the RSeqHeader of the provisional
	 * response being acknowledged. The sequence number MUST be expressible as
	 * a 32-bit unsigned integer and MUST be less than 2**31.
	 *
	 * @param rSeqNumber - the new rSeq number of this RAckHeader
	 * @throws InvalidArgumentException if supplied value is less than zero.
	 */
	public void setRSeqNumber(int rSeqNumber) throws InvalidArgumentException {
		if (rSeqNumber <= 0)
			throw new InvalidArgumentException("Bad rSeq # " + rSeqNumber);
		this.rSeqNumber = rSeqNumber;
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
