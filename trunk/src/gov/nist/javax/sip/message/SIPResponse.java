/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
 *******************************************************************************/
package gov.nist.javax.sip.message;

import gov.nist.javax.sip.address.*;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import gov.nist.core.*;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;


/**
 * SIP Response structure.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2004-02-18 14:33:02 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class SIPResponse
	extends SIPMessage
	implements javax.sip.message.Response {
	protected StatusLine statusLine;

	public static String getReasonPhrase(int rc) {
		String retval = null;
		switch (rc) {

			case TRYING :
				retval = "Trying";
				break;

			case RINGING :
				retval = "Ringing";
				break;

			case CALL_IS_BEING_FORWARDED :
				retval = "Call is being forwarded";
				break;

			case QUEUED :
				retval = "Queued";
				break;

			case SESSION_PROGRESS :
				retval = "Session progress";
				break;

			case OK :
				retval = "OK";
				break;

			case ACCEPTED :
				retval = "Accepted";
				break;

			case MULTIPLE_CHOICES :
				retval = "Multiple choices";
				break;

			case MOVED_PERMANENTLY :
				retval = "Moved permanently";
				break;

			case MOVED_TEMPORARILY :
				retval = "Moved Temporarily";
				break;

			case USE_PROXY :
				retval = "Use proxy";
				break;

			case ALTERNATIVE_SERVICE :
				retval = "Alternative service";
				break;

			case BAD_REQUEST :
				retval = "Bad request";
				break;

			case UNAUTHORIZED :
				retval = "Unauthorized";
				break;

			case PAYMENT_REQUIRED :
				retval = "Payment required";
				break;

			case FORBIDDEN :
				retval = "Forbidden";
				break;

			case NOT_FOUND :
				retval = "Not found";
				break;

			case METHOD_NOT_ALLOWED :
				retval = "Method not allowed";
				break;

			case NOT_ACCEPTABLE :
				retval = "Not acceptable";
				break;

			case PROXY_AUTHENTICATION_REQUIRED :
				retval = "Proxy Authentication required";
				break;

			case REQUEST_TIMEOUT :
				retval = "Request timeout";
				break;

			case GONE :
				retval = "Gone";
				break;

			case TEMPORARILY_UNAVAILABLE :
				retval = "Temporarily Unavailable";
				break;

			case REQUEST_ENTITY_TOO_LARGE :
				retval = "Request entity too large";
				break;

			case REQUEST_URI_TOO_LONG :
				retval = "Request-URI too large";
				break;

			case UNSUPPORTED_MEDIA_TYPE :
				retval = "Unsupported media type";
				break;

			case UNSUPPORTED_URI_SCHEME :
				retval = "Unsupported URI Scheme";
				break;

			case BAD_EXTENSION :
				retval = "Bad extension";
				break;

			case EXTENSION_REQUIRED :
				retval = "Etension Required";
				break;

			case INTERVAL_TOO_BRIEF :
				retval = "Interval too brief";
				break;

			case CALL_OR_TRANSACTION_DOES_NOT_EXIST :
				retval = "Call leg/Transaction does not exist";
				break;

			case LOOP_DETECTED :
				retval = "Loop detected";
				break;

			case TOO_MANY_HOPS :
				retval = "Too many hops";
				break;

			case ADDRESS_INCOMPLETE :
				retval = "Address incomplete";
				break;

			case AMBIGUOUS :
				retval = "Ambiguous";
				break;

			case BUSY_HERE :
				retval = "Busy here";
				break;

			case REQUEST_TERMINATED :
				retval = "Request Terminated";
				break;

			case NOT_ACCEPTABLE_HERE :
				retval = "Not Accpetable here";
				break;

			case BAD_EVENT :
				retval = "Bad Event";
				break;

			case REQUEST_PENDING :
				retval = "Request Pending";
				break;

			case SERVER_INTERNAL_ERROR :
				retval = "Server Internal Error";
				break;

			case UNDECIPHERABLE :
				retval = "Undecipherable";
				break;

			case NOT_IMPLEMENTED :
				retval = "Not implemented";
				break;

			case BAD_GATEWAY :
				retval = "Bad gateway";
				break;

			case SERVICE_UNAVAILABLE :
				retval = "Service unavailable";
				break;

			case SERVER_TIMEOUT :
				retval = "Gateway timeout";
				break;

			case VERSION_NOT_SUPPORTED :
				retval = "SIP version not supported";
				break;

			case MESSAGE_TOO_LARGE :
				retval = "Message Too Large";
				break;

			case BUSY_EVERYWHERE :
				retval = "Busy everywhere";
				break;

			case DECLINE :
				retval = "Decline";
				break;

			case DOES_NOT_EXIST_ANYWHERE :
				retval = "Does not exist anywhere";
				break;

			case SESSION_NOT_ACCEPTABLE :
				retval = "Session Not acceptable";
				break;

			default :
				retval = null;

		}
		return retval;

	}

	/** set the status code.
	 *@param statusCode is the status code to set.
	 *@throws IlegalArgumentException if invalid status code.
	 */
	public void setStatusCode(int statusCode) throws ParseException {
		if (statusCode < 100 || statusCode > 800)
			throw new ParseException("bad status code", 0);
		if (this.statusLine == null)
			this.statusLine = new StatusLine();
		this.statusLine.setStatusCode(statusCode);
	}

	/**
	 * Get the status line of the response.
	 *@return StatusLine
	 */
	public StatusLine getStatusLine() {
		return statusLine;
	}

	/** Get the staus code (conveniance function).
	 *@return the status code of the status line.
	 */
	public int getStatusCode() {
		return statusLine.getStatusCode();
	}

	/** Set the reason phrase.
	 *@param reasonPhrase the reason phrase.
	 *@throws IllegalArgumentException if null string
	 */
	public void setReasonPhrase(String reasonPhrase) {
		if (reasonPhrase == null)
			throw new IllegalArgumentException("Bad reason phrase");
		if (this.statusLine == null)
			this.statusLine = new StatusLine();
		this.statusLine.setReasonPhrase(reasonPhrase);
	}

	/** Get the reason phrase.
	 *@return the reason phrase.
	 */
	public String getReasonPhrase() {
		if (statusLine == null || statusLine.getReasonPhrase() == null)
			return "";
		else
			return statusLine.getReasonPhrase();
	}

	/** Return true if the response is a final response.
	 *@param rc is the return code.
	 *@return true if the parameter is between the range 200 and 700.
	 */
	public static boolean isFinalResponse(int rc) {
		return rc >= 200 && rc < 700;
	}

	/** Is this a final response?
	 *@return true if this is a final response.
	 */
	public boolean isFinalResponse() {
		return isFinalResponse(statusLine.getStatusCode());
	}

	/**
	 * Set the status line field.
	 *@param sl Status line to set.
	 */
	public void setStatusLine(StatusLine sl) {
		statusLine = sl;
	}

	/** Constructor.
	 */
	public SIPResponse() {
		super();
	}
	/**
	 * Print formatting function.
	 *Indent and parenthesize for pretty printing.
	 * Note -- use the encode method for formatting the message.
	 * Hack here to XMLize.
	 *
	 *@return a string for pretty printing.
	 */
	public String debugDump() {
		String superstring = super.debugDump();
		stringRepresentation = "";
		sprint(MESSAGE_PACKAGE + ".SIPResponse");
		sprint("{");
		if (statusLine != null) {
			sprint(statusLine.debugDump());
		}
		sprint(superstring);
		sprint("}");
		return stringRepresentation;
	}

	/**
	 * Check the response structure. Must have from, to CSEQ and VIA
	 * headers.
	 */
	protected void checkHeaders() throws ParseException {
		if (getCSeq() == null) {
			throw new ParseException(CSeq.NAME, 0);
		}
		if (getTo() == null) {
			throw new ParseException(To.NAME, 0);
		}
		if (getFrom() == null) {
			throw new ParseException(From.NAME, 0);
		}
		if (getViaHeaders() == null) {
			throw new ParseException(Via.NAME, 0);
		}
	}

	/**
	 *  Encode the SIP Request as a string.
	 *@return The string encoded canonical form of the message.
	 */

	public String encode() {
		String retval;
		if (statusLine != null)
			retval = statusLine.encode() + super.encode();
		else
			retval = super.encode();
		return retval ;
	}

	/** Get this message as a list of encoded strings.
	 *@return LinkedList containing encoded strings for each header in
	 *   the message.
	 */

	public LinkedList getMessageAsEncodedStrings() {
		LinkedList retval = super.getMessageAsEncodedStrings();

		if (statusLine != null)
			retval.addFirst(statusLine.encode());
		return retval;

	}

	/**
	 * Make a clone (deep copy) of this object.
	 *@return a deep copy of this object.
	 */

	public Object clone() {
		SIPResponse retval = (SIPResponse) super.clone();
		retval.statusLine = (StatusLine) this.statusLine.clone();
		return retval;
	}
	/**
	 * Replace a portion of this response with a new structure (given by
	 * newObj). This method finds a sub-structure that encodes to cText
	 * and has the same type as the second arguement and replaces this
	 * portion with the second argument.
	 * @param cText is the text that we want to replace.
	 * @param newObj is the new object that we want to put in place of
	 * 	cText.
	 * @param matchSubstring boolean to indicate whether to match on
	 *   substrings when searching for a replacement.
	 */
	public void replace(
		String cText,
		GenericObject newObj,
		boolean matchSubstring) {
		if (cText == null || newObj == null)
			throw new IllegalArgumentException("null args!");
		if (newObj instanceof SIPHeader)
			throw new IllegalArgumentException(
				"Bad replacement class " + newObj.getClass().getName());

		if (statusLine != null)
			statusLine.replace(cText, newObj, matchSubstring);
		super.replace(cText, newObj, matchSubstring);
	}

	/**
	 * Compare for equality.
	 *@param other other object to compare with.
	 */
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass()))
			return false;
		SIPResponse that = (SIPResponse) other;
		return statusLine.equals(that.statusLine) && super.equals(other);
	}

	/**
	 * Match with a template.
	 *@param matchObj template object to match ourselves with (null
	 * in any position in the template object matches wildcard)
	 */
	public boolean match(Object matchObj) {
		if (matchObj == null)
			return true;
		else if (!matchObj.getClass().equals(this.getClass())) {
			return false;
		} else if (matchObj == this)
			return true;
		SIPResponse that = (SIPResponse) matchObj;
		// System.out.println("---------------------------------------");
		// System.out.println("matching " + this.encode());
		// System.out.println("matchObj " + that.encode());
		StatusLine rline = that.statusLine;
		if (this.statusLine == null && rline != null)
			return false;
		else if (this.statusLine == rline)
			return super.match(matchObj);
		else {
			// System.out.println(statusLine.match(that.statusLine));
			// System.out.println(super.match(matchObj));
			// System.out.println("---------------------------------------");
			return statusLine.match(that.statusLine) && super.match(matchObj);
		}

	}

	/** Encode this into a byte array.
	 * This is used when the body has been set as a binary array
	 * and you want to encode the body as a byte array for transmission.
	 *
	 *@return a byte array containing the SIPRequest encoded as a byte
	 *  array.
	 */

	public byte[] encodeAsBytes() {
		byte[] slbytes = null;
		if (statusLine != null) {
			try {
				slbytes = statusLine.encode().getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				InternalErrorHandler.handleException(ex);
			}
		}
		byte[] superbytes = super.encodeAsBytes();
		byte[] retval = new byte[slbytes.length + superbytes.length];
		int i = 0;
		if (slbytes != null) {
			for (i = 0; i < slbytes.length; i++) {
				retval[i] = slbytes[i];
			}
		}

		for (int j = 0; j < superbytes.length; j++, i++) {
			retval[i] = superbytes[j];
		}
		return retval;
	}

	/** Get the dialog identifier. Assume the incoming response
	 * corresponds to a client dialog for an outgoing request.
	 * Acknowledgement -- this was contributed by Lamine Brahimi.
	 *
	 *@return a string that can be used to identify the dialog.
	public String getDialogId()  {
	    CallID cid = (CallID)this.getCallId();
	    From from = (From) this.getFrom();
	    String retval = cid.getCallId();
	    retval += COLON + from.getUserAtHostPort();
	    retval += COLON;
	    if (from.getTag() != null)
	        retval +=  from.getTag();
	    
	    
	    return retval.toLowerCase();
	}
	 */

	/** Get a dialog identifier.
	 * Generates a string that can be used as a dialog identifier.
	 *
	 * @param isServer is set to true if this is the UAS
	 * and set to false if this is the UAC
	 */
	public String getDialogId(boolean isServer) {
		CallID cid = (CallID) this.getCallId();
		From from = (From) this.getFrom();
		To to = (To) this.getTo();
		StringBuffer retval = new StringBuffer(cid.getCallId());
		if (!isServer) {
			retval.append(COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(COLON);
				retval.append(from.getTag());
			}
			retval.append(COLON).append(to.getUserAtHostPort());
			if (to.getTag() != null) {
				retval.append(COLON);
				retval.append(to.getTag());
			}
		} else {
			retval.append(COLON).append(to.getUserAtHostPort());
			if (to.getTag() != null) {
				retval.append(COLON);
				retval.append(to.getTag());
			}
			retval.append(COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(COLON);
				retval.append(from.getTag());
			}
		}
		return retval.toString().toLowerCase();
	}

	public String getDialogId(boolean isServer, String toTag) {
		CallID cid = (CallID) this.getCallId();
		From from = (From) this.getFrom();
		To to = (To) this.getTo();
		StringBuffer retval = new StringBuffer(cid.getCallId());
		if (!isServer) {
			retval.append(COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(COLON);
				retval.append(from.getTag());
			}
			retval.append(COLON).append(to.getUserAtHostPort());
			if (toTag != null) {
				retval.append(COLON);
				retval.append(toTag);
			}
		} else {
			retval.append(COLON).append(to.getUserAtHostPort());
			if (toTag != null) {
				retval.append(COLON);
				retval.append(toTag);
			}
			retval.append(COLON).append(from.getUserAtHostPort());
			if (from.getTag() != null) {
				retval.append(COLON);
				retval.append(from.getTag());
			}
		}
		return retval.toString().toLowerCase();
	}

	/**
	 * Create a new SIPRequest from the given response. Note that the
	 * RecordRoute Via and CSeq headers are not copied from the response.
	 * These have to be added by the caller.
	 * This method is useful for generating ACK messages from final
	 * responses.
	 *
	 *@param requestURI is the request URI to use.
	 *@param via is the via header to use.
	 *@param cseq is the cseq header to use in the generated
	 * request.
	 */

	public SIPRequest createRequest(SipUri requestURI, Via via, CSeq cseq) {
		SIPRequest newRequest = new SIPRequest();
		String method = cseq.getMethod();
		newRequest.setMethod(method);
		newRequest.setRequestURI(requestURI);
		if ((method.equalsIgnoreCase("ACK")
			|| method.equalsIgnoreCase("CANCEL"))
			&& this.getTopmostVia().getBranch() != null) {
			// Use the branch id from the OK.
			try {
				via.setBranch(this.getTopmostVia().getBranch());
			} catch (ParseException ex) {
			}
		}
		newRequest.setHeader(via);
		newRequest.setHeader(cseq);
		Iterator headerIterator = getHeaders();
		while (headerIterator.hasNext()) {
			SIPHeader nextHeader = (SIPHeader) headerIterator.next();
			// Some headers do not belong in a Request ....
			if (SIPMessage.isResponseHeader(nextHeader)
				|| nextHeader instanceof ViaList
				|| nextHeader instanceof CSeq
				|| nextHeader instanceof ContentType
				|| nextHeader instanceof RecordRouteList) {
				continue;
			}
			if (nextHeader instanceof To)
				nextHeader = (SIPHeader) nextHeader.clone();
			else if (nextHeader instanceof From)
				nextHeader = (SIPHeader) nextHeader.clone();
			try {
				newRequest.attachHeader(nextHeader, false);
			} catch (SIPDuplicateHeaderException e) {
				e.printStackTrace();
			}
		}
		return newRequest;
	}

	/**
	 * Get the encoded first line.
	 *
	 *@return the status line encoded.
	 *
	 */
	public String getFirstLine() {
		if (this.statusLine == null)
			return null;
		else
			return this.statusLine.encode();
	}

	public void setSIPVersion(String sipVersion) {
		this.statusLine.setSipVersion(sipVersion);
	}

	public String getSIPVersion() {
		return this.statusLine.getSipVersion();
	}

	public String toString() {
		return statusLine.encode() + super.encode();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:31  sverker
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
