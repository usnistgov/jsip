/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import java.text.ParseException;

/**  
 * InReplyTo SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class InReplyTo extends SIPHeader implements InReplyToHeader {

	protected CallIdentifier callId;

	/** Default constructor
	 */
	public InReplyTo() {
		super(IN_REPLY_TO);
	}

	/** constructor
	 * @param cid CallIdentifier to set
	 */
	public InReplyTo(CallIdentifier cid) {
		super(IN_REPLY_TO);
		callId = cid;
	}

	/**
	 * Sets the Call-Id of the InReplyToHeader. The CallId parameter uniquely
	 * identifies a serious of messages within a dialogue.
	 *
	 * @param callId - the string value of the Call-Id of this InReplyToHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the callId value.
	 */
	public void setCallId(String callId) throws ParseException {
		try {
			this.callId = new CallIdentifier(callId);
		} catch (Exception e) {
			throw new ParseException(e.getMessage(), 0);
		}
	}

	/**
	 * Returns the Call-Id of InReplyToHeader. The CallId parameter uniquely
	 * identifies a series of messages within a dialogue.
	 *
	 * @return the String value of the Call-Id of this InReplyToHeader
	 */
	public String getCallId() {
		if (callId == null)
			return null;
		return callId.encode();
	}

	/**
	     * Generate canonical form of the header.
	     * @return String
	     */
	public String encodeBody() {
		return callId.encode();
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
