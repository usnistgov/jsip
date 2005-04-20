/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip.message;

import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.util.LinkedList;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import javax.sip.address.URI;
import javax.sip.message.*;
import java.text.ParseException;
import javax.sip.*;
import javax.sip.header.*;
import gov.nist.javax.sip.header.*;

/**   Acknowledgements: Mark Bednarek made a few fixes to this code.
 **   Jeff Keyser added two methods that create responses and generate
 **   cancel requests from incoming orignial  requests without 
 **   the additional overhead  of encoding and decoding messages.
 **   Bruno Konik noticed an extraneous newline added to the end of the
 **   buffer when encoding it. 
 */

/**
 * The SIP Request structure.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.13 $ $Date: 2005-04-20 20:01:12 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */



public final class SIPRequest
	extends SIPMessage
	implements javax.sip.message.Request {
	private static final String DEFAULT_USER = "ip";
	private static final int DEFAULT_TTL = 1;
	private static final String DEFAULT_TRANSPORT = "udp";
	private static final String DEFAULT_METHOD = Request.INVITE;

	private Object transactionPointer;

	protected RequestLine requestLine;

	/** Set to standard constants to speed up processing.
	* this makes equals comparisons run much faster in the
	* stack because then it is just identity comparision.
	* Character by char comparison is not required.
	*/
	public static String getCannonicalName(String method) {
		if (method.equalsIgnoreCase(Request.INVITE)) return(Request.INVITE);
		else if (method.equalsIgnoreCase(Request.BYE)) return(Request.BYE);
		else if (method.equalsIgnoreCase(Request.ACK)) return Request.ACK;
		else if (method.equalsIgnoreCase(Request.PRACK)) return Request.PRACK;
		else if (method.equalsIgnoreCase(Request.INFO)) return Request.INFO;
		else if (method.equalsIgnoreCase(Request.UPDATE)) return Request.UPDATE;
		else if (method.equalsIgnoreCase(Request.REFER)) return Request.REFER;
		else if (method.equalsIgnoreCase(Request.MESSAGE)) return Request.MESSAGE;
		else if (method.equalsIgnoreCase(Request.SUBSCRIBE)) return Request.SUBSCRIBE;
		else if (method.equalsIgnoreCase(Request.NOTIFY)) return Request.NOTIFY;
		else if (method.equalsIgnoreCase(Request.REGISTER)) return Request.REGISTER;
		else if (method.equalsIgnoreCase(Request.OPTIONS)) return Request.OPTIONS;
		else return method;
	}

	/**
	* Replace a portion of this response with a new structure (given by
	* newObj). This method finds a sub-structure that encodes to cText
	* and has the same type as the second arguement and replaces this
	* portion with the second argument.
	* @param ctext is the text that we want to replace.
	* @param newObject is the new object that we want to put in place of
	* 	ctext.
	* @param matchSubstring
	*/
	public void replace(
		String ctext,
		GenericObject newObject,
		boolean matchSubstring) {
		if (ctext == null || newObject == null) {
			throw new IllegalArgumentException("Illegal argument null");
		}

		requestLine.replace(ctext, newObject, matchSubstring);
		super.replace(ctext, newObject, matchSubstring);

	}

	/** Get the Request Line of the SIPRequest.
	*@return the request line of the SIP Request.
	*/

	public RequestLine getRequestLine() {
		return requestLine;
	}

	/** Set the request line of the SIP Request.
	*@param requestLine is the request line to set in the SIP Request.
	*/

	public void setRequestLine(RequestLine requestLine) {
		this.requestLine = requestLine;
	}

	/** Constructor.
	*/
	public SIPRequest() {
		super();
	}

	/** Convert to a formatted string for pretty printing. Note that
	* the encode method converts this into a sip message that is suitable
	* for transmission. Note hack here if you want to convert the nice
	* curly brackets into some grotesque XML tag.
	*
	*@return a string which can be used to examine the message contents.
	*
	*/
	public String debugDump() {
		String superstring = super.debugDump();
		stringRepresentation = "";
		sprint(PackageNames.MESSAGE_PACKAGE + ".SIPRequest");
		sprint("{");
		if (requestLine != null)
			sprint(requestLine.debugDump());
		sprint(superstring);
		sprint("}");
		return stringRepresentation;
	}

	/**
	* Check header for constraints. 
	* (1) Invite options and bye requests can only have SIP URIs in the
	* contact headers. 
	* (2) Request must have cseq, to and from and via headers.
	* (3) Method in request URI must match that in CSEQ.
	*/
	protected void checkHeaders() throws ParseException {
		String prefix = "Missing Header ";

		/* Check for required headers */

		if (getCSeq() == null) {
			throw new ParseException(prefix + CSeqHeader.NAME, 0);
		}
		if (getTo() == null) {
			throw new ParseException(prefix + ToHeader.NAME, 0);
		}
		if (getFrom() == null) {
			throw new ParseException(prefix + FromHeader.NAME, 0);
		}
		if (getViaHeaders() == null) {
			throw new ParseException(prefix + ViaHeader.NAME, 0);
		}
		if (getMaxForwards() == null) {
			throw new ParseException(prefix + MaxForwardsHeader.NAME, 0);
		}


		if (requestLine != null
			&& requestLine.getMethod() != null
			&& getCSeq().getMethod() != null
			&& requestLine.getMethod().compareToIgnoreCase(getCSeq().getMethod())
				!= 0) {
			throw new ParseException(
				"CSEQ method mismatch with  Request-Line ",
				0);

		}

	}

	/**
	* Set the default values in the request URI if necessary.
	*/
	protected void setDefaults() {
		// The request line may be unparseable (set to null by the
		// exception handler.
		if (requestLine == null)
			return;
		String method = requestLine.getMethod();
		// The requestLine may be malformed!
		if (method == null)
			return;
		GenericURI u = requestLine.getUri();
		if (u == null)
			return;
		if (method.compareTo(Request.REGISTER) == 0
			|| method.compareTo(Request.INVITE) == 0) {
			if (u instanceof SipUri) {
				SipUri sipUri = (SipUri) u;
				sipUri.setUserParam(DEFAULT_USER);
				try {
					sipUri.setTransportParam(DEFAULT_TRANSPORT);
				} catch (ParseException ex) {
				}
			}
		}
	}

	/**
	* Patch up the request line as necessary.
	*/
	protected void setRequestLineDefaults() {
		String method = requestLine.getMethod();
		if (method == null) {
			CSeq cseq = (CSeq) this.getCSeq();
			if (cseq != null) {
				method = cseq.getMethod();
				requestLine.setMethod(method);
			}
		}
	}

	/**
	* A conveniance function to access the Request URI.
	*@return the requestURI if it exists.
	*/
	public javax.sip.address.URI getRequestURI() {
		if (this.requestLine == null)
			return null;
		else
			return (javax.sip.address.URI) this.requestLine.getUri();
	}

	/** Sets the RequestURI of Request. The Request-URI is a SIP or
	 * SIPS URI or a general URI. It indicates the user or service to which
	 * this request  is being addressed. SIP elements MAY support
	 * Request-URIs with schemes  other than "sip" and "sips", for
	 * example the "tel" URI scheme. SIP  elements MAY translate
	 * non-SIP URIs using any mechanism at their disposal,  resulting
	 * in SIP URI, SIPS URI, or some other scheme.
	 *
	 * @param uri the new Request URI of this request message
	 */
	public void setRequestURI(URI uri) {
		if (this.requestLine == null) {
			this.requestLine = new RequestLine();
		}
		this.requestLine.setUri((GenericURI) uri);
	}

	/** Set the method.
	*@param method is the method to set.
	*@throws IllegalArgumentException if the method is null
	*/
	public void setMethod(String method) {
		if (method == null)
			throw new IllegalArgumentException("null method");
		if (this.requestLine == null) {
			this.requestLine = new RequestLine();
		}

		// Set to standard constants to speed up processing.
		// this makes equals compares run much faster in the
		// stack because then it is just identity comparision

		String meth = getCannonicalName(method);
		this.requestLine.setMethod(meth);

		if (this.cSeqHeader != null) {
			try {
				this.cSeqHeader.setMethod(method);
			} catch (ParseException e) {
			}
		}
	}

	/** Get the method from the request line.
	*@return the method from the request line if the method exits and
	* null if the request line or the method does not exist.
	*/
	public String getMethod() {
		if (requestLine == null)
			return null;
		else
			return requestLine.getMethod();
	}

	/**
	*  Encode the SIP Request as a string. 
	*
	*@return an encoded String containing the encoded SIP Message.
	*/

	public String encode() {
		String retval;
		if (requestLine != null) {
			this.setRequestLineDefaults();
			retval = requestLine.encode() + super.encode();
		} else
			retval = super.encode();
		return retval;
	}

	/** Encode only the headers and not the content.
	*/
	public String encodeMessage() {
		String retval;
		if (requestLine != null) {
			this.setRequestLineDefaults();
			retval = requestLine.encode() + super.encodeSIPHeaders();
		} else
			retval = super.encodeSIPHeaders();
		return retval;

	}

	/** ALias for encode above.
	*/
	public String toString() {
		return this.encode();
	}

	/**
	* Make a clone (deep copy) of this object.
	* You can use this if you
	* want to modify a request while preserving the original 
	*
	*@return a deep copy of this object. 
	*/

	public Object clone() {
		SIPRequest retval = (SIPRequest) super.clone();
		// Do not copy over the tx pointer -- this is only for internal tracking.
		retval.transactionPointer = null;
		if (this.requestLine != null)
			retval.requestLine = (RequestLine) this.requestLine.clone();
		
		return retval;
	}

	/**
	* Compare for equality.
	*
	*@param other object to compare ourselves with.
	*/
	public boolean equals(Object other) {
		if (!this.getClass().equals(other.getClass()))
			return false;
		SIPRequest that = (SIPRequest) other;

		return requestLine.equals(that.requestLine) && super.equals(other);
	}

	/**
	* Get the message as a linked list of strings.
	* Use this if you want to iterate through the message.
	*
	*@return a linked list containing the request line and 
	* headers encoded as strings. 
	*/
	public LinkedList getMessageAsEncodedStrings() {
		LinkedList retval = super.getMessageAsEncodedStrings();
		if (requestLine != null) {
			this.setRequestLineDefaults();
			retval.addFirst(requestLine.encode());
		}
		return retval;

	}

	/**
	* Match with a template. You can use this if you want to match
	* incoming messages with a pattern and do something when you find
	* a match. This is useful for building filters/pattern matching
	* responders etc.
	*
	*@param matchObj object to match ourselves with (null matches wildcard)
	*
	*/
	public boolean match(Object matchObj) {
		if (matchObj == null)
			return true;
		else if (!matchObj.getClass().equals(this.getClass()))
			return false;
		else if (matchObj == this)
			return true;
		SIPRequest that = (SIPRequest) matchObj;
		RequestLine rline = that.requestLine;
		if (this.requestLine == null && rline != null)
			return false;
		else if (this.requestLine == rline)
			return super.match(matchObj);
		return requestLine.match(that.requestLine) && super.match(matchObj);

	}

	/** Get a dialog identifier. 
	* Generates a string that can be used as a dialog identifier.
	    *
	*@param isServer is set to true if this is the UAS
	*	and set to false if this is the UAC
	*/
	public String getDialogId(boolean isServer) {
		CallID cid = (CallID) this.getCallId();
		StringBuffer retval = new StringBuffer(cid.getCallId());
		From from = (From) this.getFrom();
		To to = (To) this.getTo();
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

	/** Get a dialog id given the remote tag.
	*/
	public String getDialogId(boolean isServer, String toTag) {
		From from = (From) this.getFrom();
		To to = (To) this.getTo();
		CallID cid = (CallID) this.getCallId();
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

	/** Encode this into a byte array.
	* This is used when the body has been set as a binary array 
	* and you want to encode the body as a byte array for transmission.
	*
	*@return a byte array containing the SIPRequest encoded as a byte
	*  array.
	*/

	public byte[] encodeAsBytes() {
		byte[] rlbytes = null;
		if (requestLine != null) {
			try {
				rlbytes = requestLine.encode().getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				InternalErrorHandler.handleException(ex);
			}
		}
		byte[] superbytes = super.encodeAsBytes();
		byte[] retval = new byte[rlbytes.length + superbytes.length];
		int i = 0;
		System.arraycopy(rlbytes, 0, retval, 0, rlbytes.length);
		System.arraycopy(
			superbytes,
			0,
			retval,
			rlbytes.length,
			superbytes.length);
		return retval;
	}

	/** Creates a default SIPResponse message for this request. Note
	    * You must add the necessary tags to outgoing responses if need
	* be. For efficiency, this method does not clone the incoming
	* request. If you want to modify the outgoing response, be sure
	* to clone the incoming request as the headers are shared and
	* any modification to the headers of the outgoing response will
	* result in a modification of the incoming request. 
	* Tag fields are just copied from the incoming request. 
	* Contact headers are removed from the incoming request.
	* Added by Jeff Keyser.
	*
	*@param statusCode Status code for the response. 
	* Reason phrase is generated.
	*
	*@return A SIPResponse with the status and reason supplied, and a copy
	*of all the original headers from this request.
	*/

	public SIPResponse createResponse(int statusCode) {
		SIPResponse newResponse;
		Iterator headerIterator;
		SIPHeader nextHeader;

		String reasonPhrase = SIPResponse.getReasonPhrase(statusCode);
		return this.createResponse(statusCode, reasonPhrase);

	}

	/** Creates a default SIPResponse message for this request. Note
	    * You must add the necessary tags to outgoing responses if need
	* be. For efficiency, this method does not clone the incoming
	* request. If you want to modify the outgoing response, be sure
	* to clone the incoming request as the headers are shared and
	* any modification to the headers of the outgoing response will
	* result in a modification of the incoming request. 
	* Tag fields are just copied from the incoming request. 
	* Contact headers are removed from the incoming request.
	* Added by Jeff Keyser. Route headers are not added to the
	* response.
	*
	*@param statusCode Status code for the response.
	*@param reasonPhrase Reason phrase for this response.
	*
	*@return A SIPResponse with the status and reason supplied, and a copy
	*of all the original headers from this request.
	*/

	public SIPResponse createResponse(int statusCode, String reasonPhrase) {
		SIPResponse newResponse;
		Iterator headerIterator;
		SIPHeader nextHeader;

		newResponse = new SIPResponse();
		try {
			newResponse.setStatusCode(statusCode);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad code " + statusCode);
		}
		if (reasonPhrase != null)
			newResponse.setReasonPhrase(reasonPhrase);
		else
			newResponse.setReasonPhrase(
				SIPResponse.getReasonPhrase(statusCode));
		headerIterator = getHeaders();
		while (headerIterator.hasNext()) {
			nextHeader = (SIPHeader) headerIterator.next();
			if (nextHeader instanceof From
				|| nextHeader instanceof To
				|| nextHeader instanceof ViaList
				|| nextHeader instanceof CallID
				|| nextHeader instanceof RecordRouteList
				|| nextHeader instanceof CSeq
				|| nextHeader instanceof MaxForwards
				|| nextHeader instanceof TimeStamp) {
				/**
				if (SIPMessage.isRequestHeader(nextHeader)) {
					continue;
				} else if (nextHeader instanceof ContentLength) {
					// content length added when content is
					// added...
					continue;
				} else if ( nextHeader instanceof ContactList)  {
				       // contacts are stripped from the response.
					continue;
				}
				**/
				try {
					newResponse.attachHeader(nextHeader, false);
				} catch (SIPDuplicateHeaderException e) {
					e.printStackTrace();
				}
			}
		}
		return newResponse;
	}

	/** Creates a default SIPResquest message that would cancel 
	* this request. Note that tag assignment and removal of
	* is left to the caller (we use whatever tags are present in the
	* original request).  Acknowledgement: Added by Jeff Keyser.
	* Incorporates a bug report from Andreas Byström.
	*
	*@return A CANCEL SIPRequest with a copy all the original headers 
	* from this request except for Require, ProxyRequire.
	*/

	public SIPRequest createCancelRequest() {
		SIPRequest newRequest;
		Iterator headerIterator;
		SIPHeader nextHeader;

		newRequest = new SIPRequest();
		newRequest.setRequestLine((RequestLine) this.requestLine.clone());
		newRequest.setMethod(Request.CANCEL);
		headerIterator = getHeaders();
		while (headerIterator.hasNext()) {
			nextHeader = (SIPHeader) headerIterator.next();
			if (nextHeader instanceof RequireList)
				continue;
			else if (nextHeader instanceof ProxyRequireList)
				continue;
			else if (nextHeader instanceof ContentLength)
				continue;
			else if (nextHeader instanceof ContentType)
				continue;

			if (nextHeader instanceof ViaList) {
				/**
				   SIPHeader sipHeader =  
					(SIPHeader) 
				       ((ViaList) nextHeader).getFirst().clone() ;
				   nextHeader = new ViaList();
				   ((ViaList)nextHeader).add(sipHeader);
				 **/
				nextHeader = (ViaList) ((ViaList) nextHeader).clone();
			}

			// CSeq method for a cancel request must be cancel.
			if (nextHeader instanceof CSeq) {
				CSeq cseq = (CSeq) nextHeader.clone();
				try {
					cseq.setMethod(Request.CANCEL);
				} catch (ParseException e) {
				}
				nextHeader = cseq;
			}
			try {
				newRequest.attachHeader(nextHeader, false);
			} catch (SIPDuplicateHeaderException e) {
				e.printStackTrace();
			}
		}
		return newRequest;
	}

	/** Creates a default ACK SIPRequest message for this original request.
	* Note that the defaultACK SIPRequest does not include the 
	* content of the original SIPRequest. If responseToHeader
	* is null then the toHeader of this request is used to
	* construct the ACK.  Note that tag fields are just copied
	* from the original SIP Request.  Added by Jeff Keyser.
	*
	*@param responseToHeader To header to use for this request.
	*
	*@return A SIPRequest with an ACK method.
	*/
	public SIPRequest createAckRequest(To responseToHeader) {
		SIPRequest newRequest;
		Iterator headerIterator;
		SIPHeader nextHeader;

		newRequest = new SIPRequest();
		newRequest.setRequestLine((RequestLine) this.requestLine.clone());
		newRequest.setMethod(Request.ACK);
		headerIterator = getHeaders();
		while (headerIterator.hasNext()) {
			nextHeader = (SIPHeader) headerIterator.next();
			if (nextHeader instanceof RouteList) {
				// Ack and cancel do not get ROUTE headers.
				// Route header for ACK is assigned by the 
				// Dialog if necessary.
				continue;
			} else if (nextHeader instanceof ProxyAuthorization) {
				// Remove proxy auth header. 
				// Assigned by the Dialog if necessary.
				continue;
			} else if (nextHeader instanceof ContentLength) {
				// Adding content is responsibility of user.
				nextHeader = (SIPHeader) nextHeader.clone();
				try {
					((ContentLength) nextHeader).setContentLength(0);
				} catch (InvalidArgumentException e) {
				}
			} else if (nextHeader instanceof ContentType) {
				// Content type header is removed since 
				// content length is 0. Bug fix from
				// Antonis Kyardis.
				continue;
			} else if (nextHeader instanceof CSeq) {
				// The CSeq header field in the 
				// ACK MUST contain the same value for the 
				// sequence number as was present in the 
				// original request, but the method parameter 
				// MUST be equal to "ACK".
				CSeq cseq = (CSeq) nextHeader.clone();
				try {
					cseq.setMethod(Request.ACK);
				} catch (ParseException e) {
				}
				nextHeader = cseq;
			} else if (nextHeader instanceof To) {
				if (responseToHeader != null) {
					nextHeader = responseToHeader;
				} else {
					nextHeader = (SIPHeader) nextHeader.clone();
				}
			} else if (nextHeader instanceof ContactList ) {
				// CONTACT header does not apply for ACK requests.
				continue;
			} else if (nextHeader instanceof ViaList) {
				// Bug reported by Gianluca Martinello
				//The ACK MUST contain a single Via header field, 
				// and this MUST be equal to the top Via header 
				// field of the original
				// request.  

				nextHeader =
					(SIPHeader) ((ViaList) nextHeader).getFirst().clone();
			} else {
				nextHeader = (SIPHeader) nextHeader.clone();
			}

			try {
				newRequest.attachHeader(nextHeader, false);
			} catch (SIPDuplicateHeaderException e) {
				e.printStackTrace();
			}
		}
		return newRequest;
	}

	/**
	* Create a new default SIPRequest from the original request. Warning:
	* the newly created SIPRequest, shares the headers of 
	* this request but we generate any new headers that we need to modify
	* so  the original request is umodified. However, if you modify the
	* shared headers after this request is created, then the newly
	* created request will also be modified.
	* If you want to modify the original request
	* without affecting the returned Request
	* make sure you clone it before calling this method.
	*
	* Only required headers are copied.
	* <ul>
	* <li>
	* Contact headers are not included in the newly created request.
	* Setting the appropriate sequence number is the responsibility of
	* the caller. </li>
	* <li> RouteList is not copied for ACK and CANCEL </li>
	* <li> Note that we DO NOT copy the body of the 
	* argument into the returned header. We do not copy the content
	* type header from the original request either. These have to be 
	* added seperately and the content length has to be correctly set 
	* if necessary the content length is set to 0 in the returned header.
	* </li>
	* <li>Contact List is not copied from the original request.</li>
	* <li>RecordRoute List is not included from original request. </li>
	* <li>Via header is not included from the original request. </li>
	* </ul>
	*
	*@param requestLine is the new request line.
	*
	*@param switchHeaders is a boolean flag that causes to and from
	* 	headers to switch (set this to true if you are the 
	*	server of the transaction and are generating a BYE
	*	request). If the headers are switched, we generate 
	*	new From and To headers otherwise we just use the
	*	incoming headers.
	*
	*@return a new Default SIP Request which has the requestLine specified.
	*
	*/
	public SIPRequest createSIPRequest(
		RequestLine requestLine,
		boolean switchHeaders) {
		SIPRequest newRequest = new SIPRequest();
		newRequest.requestLine = requestLine;
		Iterator headerIterator = this.getHeaders();
		while (headerIterator.hasNext()) {
			SIPHeader nextHeader = (SIPHeader) headerIterator.next();
			// For BYE and cancel set the CSeq header to the
			// appropriate method.
			if (nextHeader instanceof CSeq) {
				CSeq newCseq = (CSeq) nextHeader.clone();
				nextHeader = newCseq;
				try {
					newCseq.setMethod(requestLine.getMethod());
				} catch (ParseException e) {
				}
			} else if (nextHeader instanceof ViaList) {
				Via via = (Via) (((ViaList) nextHeader).getFirst().clone());
				via.removeParameter("branch");
				nextHeader = via;
				// Cancel and ACK preserve the branch ID.
			} else if (nextHeader instanceof To) {
				To to = (To) nextHeader;
				if (switchHeaders) {
					nextHeader = new From(to);
					((From) nextHeader).removeTag();
				} else {
					nextHeader = (SIPHeader) to.clone();
					((To) nextHeader).removeTag();
				}
			} else if (nextHeader instanceof From) {
				From from = (From) nextHeader;
				if (switchHeaders) {
					nextHeader = new To(from);
					((To) nextHeader).removeTag();
				} else {
					nextHeader = (SIPHeader) from.clone();
					((From) nextHeader).removeTag();
				}
			} else if (nextHeader instanceof ContentLength) {
				ContentLength cl = (ContentLength) nextHeader.clone();
				try {
					cl.setContentLength(0);
				} catch (InvalidArgumentException e) {
				}
				nextHeader = cl;
			} else if (
				!(nextHeader instanceof CallID)
					&& !(nextHeader instanceof MaxForwards)) {
				// Route is kept by dialog.
				// RR is added by the caller.
				// Contact is added by the Caller
				// Any extension headers must be added 
				// by the caller.
				continue;
			}
			try {
				newRequest.attachHeader(nextHeader, false);
			} catch (SIPDuplicateHeaderException e) {
				e.printStackTrace();
			}
		}
		return newRequest;

	}

	/**
	 * Create a BYE request from this request.
	 *
	 * @param switchHeaders is a boolean flag that causes from and
	 *	isServerTransaction to headers to be swapped. Set this
	 *	to true if you are the server of the dialog and are generating
	 *      a BYE request for the dialog.
	 * @return a new default BYE request.
	 */
	public SIPRequest createBYERequest(boolean switchHeaders) {
		RequestLine requestLine = (RequestLine) this.requestLine.clone();
		requestLine.setMethod("BYE");
		return this.createSIPRequest(requestLine, switchHeaders);
	}

	/**
	 * Create an ACK request from this request. This is suitable for
	 * generating an ACK for an INVITE  client transaction.
	 *
	 * @return an ACK request that is generated from this request.
	 */
	public SIPRequest createACKRequest() {
		RequestLine requestLine = (RequestLine) this.requestLine.clone();
		requestLine.setMethod(Request.ACK);
		return this.createSIPRequest(requestLine, false);
	}

	/** 
	 * Get the host from the topmost via header.
	 *
	 * @return the string representation of the host from the topmost via
	 * header.
	 */
	public String getViaHost() {
		Via via = (Via) this.getViaHeaders().getFirst();
		return via.getHost();

	}

	/** 
	 * Get the port from the topmost via header.
	 *
	 * @return the port from the topmost via header (5060 if there is
	 *  no port indicated).
	 */
	public int getViaPort() {
		Via via = (Via) this.getViaHeaders().getFirst();
		if (via.hasPort())
			return via.getPort();
		else
			return 5060;
	}

	/**
	 * Get the first line encoded.
	 *
	 * @return a string containing the encoded request line.
	 */
	public String getFirstLine() {
		if (requestLine == null)
			return null;
		else
			return this.requestLine.encode();
	}

	/**
	 * Set the sip version.
	 *
	 * @param sipVersion the sip version to set.
	 */
	public void setSIPVersion(String sipVersion) throws ParseException {
		if (sipVersion == null || !sipVersion.equals("SIP/2.0"))
			throw new ParseException("sipVersion", 0);
		this.requestLine.setSIPVersion(sipVersion);
	}

	/**
	 * Get the SIP version.
	 *
	 * @return the SIP version from the request line.
	 */
	public String getSIPVersion() {
		return this.requestLine.getSipVersion();
	}

	public Object getTransaction() {
		// Return an opaque pointer to the transaction object.
		// This is for consistency checking and quick lookup.
		return this.transactionPointer;
	}

	public void setTransaction(Object transaction) {
		this.transactionPointer = transaction;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.12  2005/04/19 03:09:54  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * Fixes clone problem ( should allocate a new request ).
 *
 * Revision 1.11  2005/04/16 20:38:52  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.10  2004/09/13 15:12:26  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  Ben Evans (Open Cloud)
 * Reviewed by:   M. Ranganathan (NIST)
 *
 * Fixes numerous TCK problems
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
 * Revision 1.9  2004/06/16 02:53:19  mranga
 * Submitted by:  mranga
 * Reviewed by:   implement re-entrant multithreaded listener model.
 *
 * Revision 1.8  2004/06/15 09:54:43  mranga
 * Reviewed by:   mranga
 * re-entrant listener model added.
 * (see configuration property gov.nist.javax.sip.REENTRANT_LISTENER)
 *
 * Revision 1.7  2004/03/25 15:15:04  mranga
 * Reviewed by:   mranga
 * option to log message content added.
 *
 * Revision 1.6  2004/02/18 14:33:02  mranga
 * Submitted by:  Bruno Konik
 * Reviewed by:   mranga
 * Remove extraneous newline in encoding messages. Test for empty sdp announce
 * rather than die with null when null is passed to sdp announce parser.
 * Fixed bug in checking for \n\n when looking for message end.
 *
 * Revision 1.5  2004/02/05 14:43:21  mranga
 * Reviewed by:   mranga
 * Fixed for correct reporting of transaction state.
 * Remove contact headers from ack
 *
 * Revision 1.4  2004/01/22 13:26:31  sverker
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
