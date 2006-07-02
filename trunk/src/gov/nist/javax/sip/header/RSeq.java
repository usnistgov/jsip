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
package gov.nist.javax.sip.header;

import java.text.ParseException;

import javax.sip.*;
import javax.sip.header.ExtensionHeader;

/**
 *
 * @version 1.2 $Revision: 1.3 $ $Date: 2006-07-02 09:50:42 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public class RSeq extends SIPHeader implements javax.sip.header.RSeqHeader {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 8765762413224043394L;
	protected long sequenceNumber;

	/** Creates a new instance of RSeq */
	public RSeq() {
		super(NAME);
	}

	/** Gets the sequence number of this RSeqHeader.
	 * @deprecated
	 * @return the integer value of the Sequence number of the RSeqHeader
	 */
	public int getSequenceNumber() {
		return (int)this.sequenceNumber;
	}
	
	/*
	 * 
	 */
	public long getSequenceNumberLong() {
		return this.sequenceNumber;
	}
	

	/** Sets the sequence number value of the RSeqHeader of the provisional
	 * response. The sequence number MUST be expressible as a 32-bit unsigned
	 * integer and MUST be less than 2**32 - 1
	 *
	 * @param sequenceNumber - the new Sequence number of this RSeqHeader
	 * @throws InvalidArgumentException if supplied value is less than zero.
	 */
	public void setSequenceNumber(long sequenceNumber)
		throws InvalidArgumentException {
		if (sequenceNumber <= 0 ||sequenceNumber > ((long)1)<<32 - 1)
			throw new InvalidArgumentException(
				"Bad seq number " + sequenceNumber);
		this.sequenceNumber = sequenceNumber;
	}

	/** Encode the body of this header (the stuff that follows headerName).
	 * A.K.A headerValue.
	 */
	protected String encodeBody() {
		return new Long(this.sequenceNumber).toString();
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
