/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.InvalidArgumentException;
import java.text.ParseException;

/**
 * RAck SIP Header implementation
 *
 * @version 1.2 $Revision: 1.5 $ $Date: 2006-07-13 09:01:16 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public class RAck extends SIPHeader implements javax.sip.header.RAckHeader {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 743999286077404118L;

	protected long cSeqNumber;

	protected long rSeqNumber;

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
	 * @deprecated 
	 * @return the integer value of the cSeq number of the RAckHeader
	 */
	public int getCSeqNumber() {
		return (int) cSeqNumber;
	}
	/**
	 * Gets the CSeq sequence number of this RAckHeader.
	 *
	* @return the integer value of the cSeq number of the RAckHeader
	 */
	public long getCSeqNumberLong() {
		return  cSeqNumber;
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
	 *@deprecated
	 * @return the integer value of the RSeq number of the RAckHeader
	 */
	public int getRSeqNumber() {
		return (int)rSeqNumber;
	}
	/**
	 * Gets the RSeq sequence number of this RAckHeader.
	 *
	 * @return the integer value of the RSeq number of the RAckHeader
	 */
	public long getRSeqNumberLong() {
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
	public void setCSeqNumber(long cSeqNumber) throws InvalidArgumentException {
		if (cSeqNumber <= 0 || cSeqNumber > ((long)1)<<32 - 1)
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
	public void setRSeqNumber(long rSeqNumber) throws InvalidArgumentException {
		if (rSeqNumber <= 0 || cSeqNumber > ((long)1)<<32 - 1)
			throw new InvalidArgumentException("Bad rSeq # " + rSeqNumber);
		this.rSeqNumber = rSeqNumber;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.4  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.3  2006/05/24 06:14:40  mranga
 * *** empty log message ***
 *
 * Revision 1.2  2006/05/22 08:16:08  mranga
 * Added tests for retransmissionAlert flag
 * Added tests for transaction terminated event
 *
 * Revision 1.1.1.1  2005/10/04 17:12:35  mranga
 *
 * Import
 *
 *
 * Revision 1.3  2004/04/06 11:11:59  mranga
 * Submitted by:  Bruno Konik
 * Reviewed by:   mranga
 * Encoding in the wrong order (rseq first and then cseq - was inverted)
 *
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
