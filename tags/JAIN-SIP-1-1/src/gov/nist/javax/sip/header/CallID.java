/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import java.text.ParseException;

/**
 * Call ID SIPHeader.
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:48 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class CallID
	extends SIPHeader
	implements javax.sip.header.CallIdHeader {

	/**
	 * callIdentifier field
	 */
	protected CallIdentifier callIdentifier;

	/**
	 * Default constructor
	 */
	public CallID() {
		super(NAME);
	}

	/**
	 * Compare two call ids for equality.
	 * @param other Object to set
	 * @return true if the two call ids are equals, false otherwise
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass())) {
			return false;
		}
		CallID that = (CallID) other;
		return this.callIdentifier.equals(that.callIdentifier);
	}

	/**
	 * Encode the body part of this header (i.e. leave out the hdrName).
	 *@return String encoded body part of the header.
	 */
	public String encodeBody() {
		if (callIdentifier == null)
			return null;
		else
			return callIdentifier.encode();
	}

	/**
	 * get the CallId field. This does the same thing as
	 * encodeBody 
	 * @return String the encoded body part of the 
	 */
	public String getCallId() {
		return encodeBody();
	}

	/**
	 * get the call Identifer member.
	 * @return CallIdentifier
	 */
	public CallIdentifier getCallIdentifer() {
		return callIdentifier;
	}

	/**
	 * set the CallId field
	 * @param cid String to set. This is the body part of the Call-Id
	 *  header. It must have the form localId@host or localId.
	 * @throws IllegalArgumentException if cid is null, not a token, or is 
	 * not a token@token.
	 */
	public void setCallId(String cid) throws ParseException {
		try {
			callIdentifier = new CallIdentifier(cid);
		} catch (IllegalArgumentException ex) {
			throw new ParseException(cid, 0);
		}
	}

	/**
	 * Set the callIdentifier member.
	 * @param cid CallIdentifier to set (localId@host).
	 */
	public void setCallIdentifier(CallIdentifier cid) {
		callIdentifier = cid;
	}

	/** Constructor given the call Identifier.
	 *@param callId string call identifier (should be localid@host)
	 *@throws IllegalArgumentException if call identifier is bad.
	 */
	public CallID(String callId) throws IllegalArgumentException {
		super(NAME);
		this.callIdentifier = new CallIdentifier(callId);
	}

	public Object clone() {
		CallID retval = (CallID) super.clone();
		if (this.callIdentifier != null)
			retval.callIdentifier = (CallIdentifier) this.callIdentifier.clone();
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
