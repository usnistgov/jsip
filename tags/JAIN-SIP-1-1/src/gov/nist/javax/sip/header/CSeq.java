/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import java.text.ParseException;
import gov.nist.javax.sip.message.SIPRequest;

/**
 *  CSeq SIP Header.
 *
 * @author M. Ranganathan <mranga@nist.gov>  NIST/ITL/ANTD <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2004-06-16 02:53:18 $
 *
 */

public class CSeq extends SIPHeader implements javax.sip.header.CSeqHeader {

	/**
	 * seqno field
	 */
	protected Integer seqno;

	/**
	 * method field
	 */
	protected String method;

	/**
	 * Constructor.
	 */
	public CSeq() {
		super(CSEQ);
	}

	/**
	 * Constructor given the sequence number and method.
	 *
	 * @param seqno is the sequence number to assign.
	 * @param method is the method string.
	 */
	public CSeq(int seqno, String method) {
		this();
		this.seqno = new Integer(seqno);
		this.method = SIPRequest.getCannonicalName(method);
	}

	/**
	 * Compare two cseq headers for equality.
	 * @param other Object to compare against.
	 * @return true if the two cseq headers are equals, false
	 * otherwise.
	 */
	public boolean equals(Object other) {
		try {
			CSeq that = (CSeq) other;
			if (!this.seqno.equals(that.seqno)) {
				return false;
			}
			if (this.method.compareToIgnoreCase(that.method) != 0) {
				return false;
			}
			return true;
		} catch (ClassCastException ex) {
			return false;
		}
	}

	/**
	 * Return canonical encoded header.
	 * @return String with canonical encoded header.
	 */
	public String encode() {
		return headerName + COLON + SP + encodeBody() + NEWLINE;
	}

	/**
	 * Return canonical header content. (encoded header except headerName:)
	 *
	 * @return encoded string.
	 */
	public String encodeBody() {
		return seqno + SP + method.toUpperCase();
	}

	/**
	 * Get the method.
	 * @return String the method.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets the sequence number of this CSeqHeader. The sequence number
	 * MUST be expressible as a 32-bit unsigned integer and MUST be less than
	 * 2**31.
	 *
	 * @param sequenceNumber - the sequence number to set.
	 * @throws InvalidArgumentException -- if the seq number is <= 0
	 */
	public void setSequenceNumber(int sequenceNumber)
		throws InvalidArgumentException {
		if (sequenceNumber < 0)
			throw new InvalidArgumentException(
				"JAIN-SIP Exception, CSeq, setSequenceNumber(), "
					+ "the sequence number parameter is < 0");
		seqno = new Integer(sequenceNumber);
	}

	/**
	 * Set the method member
	 *
	 * @param meth -- String to set
	 */
	public void setMethod(String meth) throws ParseException {
		if (meth == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, CSeq"
					+ ", setMethod(), the meth parameter is null");
		this.method = SIPRequest.getCannonicalName(meth);
	}

	/**
	 * Gets the sequence number of this CSeqHeader.
	 *
	 * @return sequence number of the CSeqHeader 
	 */
	public int getSequenceNumber() {
		if (this.seqno == null)
			return 0;
		else
			return this.seqno.intValue();
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
