/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)        *
 ******************************************************************************/
package gov.nist.javax.sip.message;

import java.io.UnsupportedEncodingException;
import gov.nist.javax.sip.*;
import java.util.*;
import java.lang.reflect.*;
import gov.nist.core.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.parser.*;
import javax.sip.header.*;
import javax.sip.*;
import java.text.ParseException;

/**
 * This is the main SIP Message structure.
 *
 * @see StringMsgParser
 * @see PipelinedMsgParser
 *
 * @version JAIN-SIP-1.1 $Revision: 1.11 $ $Date: 2005-01-25 22:51:17 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public abstract class SIPMessage
	extends MessageObject
	implements javax.sip.message.Message {

	protected static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * unparsed headers
	 */
	protected LinkedList unrecognizedHeaders;

	/**
	 * List of parsed headers (in the order they were added)
	 */
	protected LinkedList headers;

	/**
	 * Direct accessors for frequently accessed headers
	 */
	protected From fromHeader;
	protected To toHeader;
	protected CSeq cSeqHeader;
	protected CallID callIdHeader;
	protected ContentLength contentLengthHeader;
	protected MaxForwards maxForwardsHeader;

	// Cumulative size of all the headers.
	protected int size;

	// Payload
	private String messageContent;
	private byte[] messageContentBytes;
	private Object messageContentObject;

	// Table of headers indexed by name.
	private Hashtable nameTable;

	/**
	 * Return true if the header belongs only in a Request.
	 *
	 * @param sipHeader is the header to test.
	 */
	public static boolean isRequestHeader(SIPHeader sipHeader) {
		return sipHeader instanceof AlertInfo
			|| sipHeader instanceof InReplyTo
			|| sipHeader instanceof Authorization
			|| sipHeader instanceof MaxForwards
			|| sipHeader instanceof UserAgent
			|| sipHeader instanceof Priority
			|| sipHeader instanceof ProxyAuthorization
			|| sipHeader instanceof ProxyRequire
			|| sipHeader instanceof ProxyRequireList
			|| sipHeader instanceof Route
			|| sipHeader instanceof RouteList
			|| sipHeader instanceof Subject;

	}

	/**
	 * Return true if the header belongs only in a response.
	 *
	 * @param sipHeader is the header to test.
	 */
	public static boolean isResponseHeader(SIPHeader sipHeader) {
		return sipHeader instanceof ErrorInfo
			|| sipHeader instanceof ProxyAuthenticate
			|| sipHeader instanceof Server
			|| sipHeader instanceof Unsupported
			|| sipHeader instanceof RetryAfter
			|| sipHeader instanceof Warning
			|| sipHeader instanceof WWWAuthenticate;

	}

	/**
	 * Get the headers as a linked list of encoded Strings
	 * @return a linked list with each element of the list containing a
	 * string encoded header in canonical form.
	 */
	public LinkedList getMessageAsEncodedStrings() {
		LinkedList retval = new LinkedList();
		synchronized (headers) {
			ListIterator li = headers.listIterator();
			while (li.hasNext()) {
				SIPHeader sipHeader = (SIPHeader) li.next();
				if (sipHeader instanceof SIPHeaderList) {
					SIPHeaderList shl = (SIPHeaderList) sipHeader;
					retval.addAll(shl.getHeadersAsEncodedStrings());
				} else {
					retval.add(sipHeader.encode());
				}
			}
		}
		return retval;
	}

	/** Encode only the message and exclude the contents (for debugging);
	*@return a string with all the headers encoded.
	*/
	protected String encodeSIPHeaders()  {
		StringBuffer encoding = new StringBuffer();
		synchronized (this.headers) {
			ListIterator it = this.headers.listIterator();

			while (it.hasNext()) {
				SIPHeader siphdr = (SIPHeader) it.next();
				if (!(siphdr instanceof ContentLength))
					encoding.append(siphdr.encode());
			}
		}

		return 
		encoding.append(contentLengthHeader.encode()).append(NEWLINE).toString();
	}

	/** Encode all the headers except the contents. For debug logging.
	*/
	public abstract String encodeMessage();

	/**
	 * Get A dialog identifier constructed from this messsage.
	 * This is an id that can be used to identify dialogs.
	 * @param isServerTransaction is a flag that indicates whether this is
	 * a server transaction.
	 */
	public abstract String getDialogId(boolean isServerTransaction);

	/**
	 * Template match for SIP messages.
	 * The matchObj is a SIPMessage template to match against.
	 * This method allows you to do pattern matching with incoming
	 * SIP messages.
	 * Null matches wild card.
	 *@param other is the match template to match against.
	 *@return true if a match occured and false otherwise.
	 */
	public boolean match(Object other) {
		if (other == null)
			return true;
		if (!other.getClass().equals(this.getClass()))
			return false;
		SIPMessage matchObj = (SIPMessage) other;
		ListIterator li = matchObj.getHeaders();
		while (li.hasNext()) {
			SIPHeader hisHeaders = (SIPHeader) li.next();
			LinkedList myHeaders =
				this.getHeaderList(hisHeaders.getHeaderName());

			// Could not find a header to match his header.
			if (myHeaders == null || myHeaders.size() == 0)
				return false;

			if (hisHeaders instanceof SIPHeaderList) {
				ListIterator outerIterator =
					((SIPHeaderList) hisHeaders).listIterator();
				while (outerIterator.hasNext()) {
					SIPHeader hisHeader = (SIPHeader) outerIterator.next();
					ListIterator innerIterator = myHeaders.listIterator();
					boolean found  = false;
					while (innerIterator.hasNext()) {
						SIPHeader myHeader = (SIPHeader) innerIterator.next();
						if (myHeader.match(hisHeader)) {
							found = true;
							break;
						}
					}
					if (! found)  return false;
				}
			} else {
				SIPHeader hisHeader = hisHeaders;
				ListIterator innerIterator = myHeaders.listIterator();
				boolean found = false;
				while (innerIterator.hasNext()) {
					SIPHeader myHeader = (SIPHeader) innerIterator.next();
					if (myHeader.match(hisHeader)) {
						found = true;
						break;
					}
				}
				if (! found)  return false;
			}
		}
		return true;

	}

	/**
	 * Recursively replace a portion of this object with a new Object.
	 * You cannot use this function for replacing sipheaders in
	 * a message (for that, use the remove and attach functions).
	 * Its intended use is for global find and replace of poritons of
	 * headers such as addresses.
	 * @param cText canonical representation of object that has to be
	 * 	replaced.
	 * @param newObject object that replaces the object that has the
	 * 	text cText
	 * @param matchSubstring if true then if cText is a substring of the
	 * encoded text of the Object then a match is flagged.
	 * @exception IllegalArgumentException on null args and if
	 * replacementObject does not derive from GenericObject or
	 * GenericObjectList
	 */
	public void replace(
		String cText,
		GenericObject newObject,
		boolean matchSubstring)
		throws IllegalArgumentException {
		SIPHeader siphdr;
		if (cText == null || newObject == null) {
			throw new IllegalArgumentException("null arguments");
		}
		if (SIPHeader.class.isAssignableFrom(newObject.getClass())) {
			throw new IllegalArgumentException(
				"Cannot replace object of class" + newObject.getClass());
		} else if (
			SIPHeaderList.class.isAssignableFrom(newObject.getClass())) {
			throw new IllegalArgumentException(
				"Cannot replace object of class " + newObject.getClass());
		} else {
			// not a sipheader or a sipheaderlist so do a find and replace.
			synchronized (this.headers) {
				// Concurrent modification exception noticed by Lamine Brahimi
				ListIterator li = this.headers.listIterator();
				while (li.hasNext()) {
					siphdr = (SIPHeader) li.next();
					siphdr.replace(cText, newObject, matchSubstring);
				}
			}
		}
	}

	/**
	 * Recursively replace a portion of this object with a new  Object.
	 * You cannot use this function for replacing sipheaders in
	 * a message (for that, use the remove and attach functions).
	 * Its intended use is for global find and replace of poritons of
	 * headers such as addresses.
	 * @param cText canonical representation of object that has to be
	 * 	replaced.
	 * @param newObject object that replaces the object that has the
	 * 	text cText
	 * @param matchSubstring if true then flag a match if cText is a
	 * substring of the encoded text of the object.
	 * @exception IllegalArgumentException on null args and if
	 *  replacementObject does not derive from GenericObject or
	 *  GenericObjectList
	 */
	public void replace(
		String cText,
		GenericObjectList newObject,
		boolean matchSubstring)
		throws IllegalArgumentException {
		SIPHeader siphdr;
		if (cText == null || newObject == null) {
			throw new IllegalArgumentException("null arguments");
		}
		if (SIPHeaderList.class.isAssignableFrom(newObject.getClass())) {
			throw new IllegalArgumentException(
				"Cannot replace object of class " + newObject.getClass());
		} else if (SIPHeader.class.isAssignableFrom(newObject.getClass())) {
			throw new IllegalArgumentException(
				"Cannot replace object of class " + newObject.getClass());
		} else {
			synchronized (this.headers) {
				// not a sipheader.
				ListIterator li = this.headers.listIterator();
				while (li.hasNext()) {
					siphdr = (SIPHeader) li.next();
					siphdr.replace(cText, newObject, matchSubstring);
				}
			}
		}
	}

	/**
	 * Merge a request with a template
 	 *
	 *@param template -- template to merge with.
	 *
	 */
	public void merge(Object template) {
		if (!template.getClass().equals(this.getClass()))
			throw new IllegalArgumentException(
				"Bad class " + template.getClass());
		SIPMessage templateMessage = (SIPMessage) template;
		Object[] templateHeaders = templateMessage.headers.toArray();
		for (int i = 0; i < templateHeaders.length; i++) {
			SIPHeader hdr = (SIPHeader) templateHeaders[i];
			String hdrName = hdr.getHeaderName();
			LinkedList myHdrs = this.getHeaderList(hdrName);
			if (myHdrs == null) {
				this.attachHeader(hdr);
			} else {
				ListIterator it = myHdrs.listIterator();
				while (it.hasNext()) {
					SIPHeader sipHdr = (SIPHeader) it.next();
					sipHdr.merge(hdr);
				}
			}
		}

	}

	/**
	 * Encode this message as a string. This is more efficient when
	 * the payload is a string (rather than a binary array of bytes).
	 * If the payload cannot be encoded as a UTF-8 string then it is
	 * simply ignored (will not appear in the encoded message).
	 *
	 * @return The Canonical String representation of the message
	 * (including the canonical string representation of
	 * the SDP payload if it exists).
	 */
	public String encode() {
		StringBuffer encoding = new StringBuffer();
		// Synchronization added because of 
		// concurrent modification exception
		// noticed by Lamine Brahimi.
		synchronized (this.headers) {
			ListIterator it = this.headers.listIterator();

			while (it.hasNext()) {
				SIPHeader siphdr = (SIPHeader) it.next();
				if (!(siphdr instanceof ContentLength))
					encoding.append(siphdr.encode());
			}
		}

		encoding.append(contentLengthHeader.encode()).append(NEWLINE);

		if (this.messageContentObject != null) {
			String mbody = this.getContent().toString();

			encoding.append(mbody);
		} else if (
			this.messageContent != null || this.messageContentBytes != null) {

			String content = null;
			try {
				if (messageContent != null)
					content = messageContent;
				else
					content = new String(messageContentBytes, DEFAULT_ENCODING);
			} catch (UnsupportedEncodingException ex) {
				content = "";
			}

			encoding.append(content);
		} 
		return encoding.toString();
	}

	/**
	 * Encode the message as a byte array.
	 * Use this when the message payload is a binary byte array.
	 *
	 * @return The Canonical byte array representation of the message
	 * (including the canonical byte array representation of
	 * the SDP payload if it exists all in one contiguous byte array).
	 */
	public byte[] encodeAsBytes() {
		StringBuffer encoding = new StringBuffer();
		ListIterator it = this.headers.listIterator();

		while (it.hasNext()) {
			SIPHeader siphdr = (SIPHeader) it.next();
			if (!(siphdr instanceof ContentLength))
				encoding.append(siphdr.encode());

		}
		encoding.append(contentLengthHeader.encode()).append(NEWLINE);

		byte[] retval = null;
		byte[] content = this.getRawContent();
		if (content != null) {
			// Append the content

			byte[] msgarray = null;
			try {
				msgarray = encoding.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				InternalErrorHandler.handleException(ex);
			}

			retval = new byte[msgarray.length + content.length];
			System.arraycopy(msgarray, 0, retval, 0, msgarray.length);
			System.arraycopy(
				content,
				0,
				retval,
				msgarray.length,
				content.length);
		} else {
			// Message content does not exist.

			try {
				retval = encoding.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException ex) {
				InternalErrorHandler.handleException(ex);
			}
		}
		return retval;
	}

	/**
	 * clone this message (create a new deep physical copy).
	 * All headers in the message are cloned.
	 * You can modify the cloned copy without affecting
	 * the original. The content is handled as follows:
	 * If the content is a String, or a byte array, a
	 * new copy of the content is allocated and copied over. If the
	 * content is an Object that supports the clone method, then the
	 * clone method is invoked and the cloned content is the new content.
	 * Otherwise, the content of the new message is set equal to null.
	 *
	 * @return A cloned copy of this object.
	 */
	public Object clone() {
		SIPMessage retval = null;
		try {
			retval = (SIPMessage) this.getClass().newInstance();
		} catch (IllegalAccessException ex) {
			InternalErrorHandler.handleException(ex);
		} catch (InstantiationException ex) {
			InternalErrorHandler.handleException(ex);
		}
		ListIterator li = headers.listIterator();
		while (li.hasNext()) {
			SIPHeader sipHeader = (SIPHeader) ((SIPHeader) li.next()).clone();
			retval.attachHeader(sipHeader);
		}
		if (retval instanceof SIPRequest) {
			SIPRequest thisRequest = (SIPRequest) this;
			RequestLine rl =
				(RequestLine) (thisRequest.getRequestLine()).clone();
			((SIPRequest) retval).setRequestLine(rl);
		} else {
			SIPResponse thisResponse = (SIPResponse) this;
			StatusLine sl = (StatusLine) (thisResponse.getStatusLine()).clone();
			((SIPResponse) retval).setStatusLine(sl);
		}

		if (this.getContent() != null) {
			try {
				Object newContent = null;
				Object currentContent = this.getContent();
				// Check the type of the returned content.
				if (currentContent instanceof String) {
					// If it is a string allocate a new string for the body
					newContent = new String(currentContent.toString());
				} else if (currentContent instanceof byte[]) {
					// If it is raw bytes allocate a new array of bytes
					// and copy over the content.
					int cl = ((byte[]) currentContent).length;
					byte[] nc = new byte[cl];
					System.arraycopy((byte[]) currentContent, 0, nc, 0, cl);
					newContent = nc;
				} else {
					// See if the object has a clone method that is public
					// If so invoke the clone method for the new content.
					Class cl = currentContent.getClass();
					try {
						Method meth = cl.getMethod("clone", null);
						if (Modifier.isPublic(meth.getModifiers())) {
							newContent = meth.invoke(currentContent, null);
						} else {
							newContent = currentContent;
						}
					} catch (Exception ex) {
						newContent = null;
					}
				}
				if (newContent != null)
					retval.setContent(newContent, this.getContentTypeHeader());
			} catch (ParseException ex) { /** Ignore **/
			}
		}

		return retval;
	}

	/**
	 * Get the string representation of this header (for pretty printing the
	 * generated structure).
	 *
	 * @return Formatted string representation of the object. Note that
	 * 	this is NOT the same as encode(). This is used mainly for
	 *	debugging purposes.
	 */
	public String debugDump() {
		stringRepresentation = "";
		sprint("SIPMessage:");
		sprint("{");
		try {

			Field[] fields = this.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				Class fieldType = f.getType();
				String fieldName = f.getName();
				if (f.get(this) != null
					&& Class.forName(
						SIPHEADERS_PACKAGE + ".SIPHeader").isAssignableFrom(
						fieldType)
					&& fieldName.compareTo("headers") != 0) {
					sprint(fieldName + "=");
					sprint(((SIPHeader) f.get(this)).debugDump());
				}
			}
		} catch (Exception ex) {
			InternalErrorHandler.handleException(ex);
		}

		sprint("List of headers : ");
		sprint(headers.toString());
		sprint("messageContent = ");
		sprint("{");
		sprint(messageContent);
		sprint("}");
		if (this.getContent() != null) {
			sprint(this.getContent().toString());
		}
		sprint("}");
		return stringRepresentation;
	}

	/**
	 * Constructor: Initializes lists and list headers.
	 * All the headers for which there can be multiple occurances in
	 * a message are  derived from the SIPHeaderListClass. All singleton
	 * headers are derived from SIPHeader class.
	 */
	public SIPMessage() {
		this.unrecognizedHeaders = new LinkedList();
		this.headers = new LinkedList();
		nameTable = new Hashtable();
		try {
			this.attachHeader(new ContentLength(0), false);
		} catch (Exception ex) {
		}
	}

	/**
	 * Attach a header and die if you get a duplicate header exception.
	 *
	 * @param h SIPHeader to attach.
	 */
	private void attachHeader(SIPHeader h) {
		if (h == null)
			throw new IllegalArgumentException("null header!");
		try {
			if (h instanceof SIPHeaderList) {
				SIPHeaderList hl = (SIPHeaderList) h;
				if (hl.isEmpty()) {
					return;
				}
			}
			attachHeader(h, false, false);
		} catch (SIPDuplicateHeaderException ex) {
			// InternalErrorHandler.handleException(ex);
		}
	}

	/**
	 * Attach a header (replacing the original header).
	 *
	 * @param sipHeader SIPHeader that replaces a header of the same type.
	 */
	public void setHeader(Header sipHeader) {
		SIPHeader header = (SIPHeader) sipHeader;
		if (header == null)
			throw new IllegalArgumentException("null header!");
		try {
			if (header instanceof SIPHeaderList) {
				SIPHeaderList hl = (SIPHeaderList) header;
				// Ignore empty lists.
				if (hl.isEmpty())
					return;
			}
			this.removeHeader(header.getHeaderName());
			attachHeader(header, true, false);
		} catch (SIPDuplicateHeaderException ex) {
			InternalErrorHandler.handleException(ex);
		}
	}

	/**
	 * Set a header from a linked list of headers.
	 *
	 * @param headers -- a list of headers to set.
	 */
	public void setHeaders(java.util.List headers) {
		ListIterator listIterator = headers.listIterator();
		while (listIterator.hasNext()) {
			SIPHeader sipHeader = (SIPHeader) listIterator.next();
			try {
				this.attachHeader(sipHeader, false);
			} catch (SIPDuplicateHeaderException ex) {
			}
		}
	}

	/**
	 * Attach a header to the end of the existing headers in
	 * this SIPMessage structure.
	 * This is equivalent to the attachHeader(SIPHeader,replaceflag,false);
	 * which is the normal way in which headers are attached.
	 * This was added in support of JAIN-SIP.
	 *
	 * @param h header to attach.
	 * @param replaceflag if true then replace a header if it exists.
	 * @throws SIPDuplicateHeaderException If replaceFlag is false and
	 * only a singleton header is allowed (fpr example CSeq).
	 */
	public void attachHeader(SIPHeader h, boolean replaceflag)
		throws SIPDuplicateHeaderException {
		this.attachHeader(h, replaceflag, false);
	}

	/**
	 * Attach the header to the SIP Message structure at a specified
	 * position in its list of headers.
	 *
	 * @param header Header to attach.
	 * @param replaceFlag If true then replace the existing header.
	 * @param top Location in the header list to insert the header.
	 * @exception SIPDuplicateHeaderException if the header is of a type
	 * that cannot tolerate duplicates and one of this type already exists
	 * (e.g. CSeq header).
	 * @throws IndexOutOfBoundsException If the index specified is
	 * greater than the number of headers that are in this message.
	 */

	public void attachHeader(
		SIPHeader header,
		boolean replaceFlag,
		boolean top)
		throws SIPDuplicateHeaderException {
		if (header == null) {
			throw new NullPointerException("null header");
		}

		SIPHeader h;

		if (ListMap.hasList(header)
			&& !SIPHeaderList.class.isAssignableFrom(header.getClass())) {
			SIPHeaderList hdrList = ListMap.getList(header);
			hdrList.add(header);
			h = hdrList;
		} else {
			h = header;
		}

		if (replaceFlag) {
			nameTable.remove(header.getName().toLowerCase());
		} else if (
			nameTable.containsKey(header.getName().toLowerCase())
				&& !(h instanceof SIPHeaderList)) {
			if (h instanceof ContentLength) {
				try {
					ContentLength cl = (ContentLength) header;
					contentLengthHeader.setContentLength(cl.getContentLength());
				} catch (InvalidArgumentException e) {
				}
			}
			// Just ignore duplicate header.
			return;
		}

		SIPHeader originalHeader = (SIPHeader) getHeader(header.getName());

		// Delete the original header from our list structure.
		if (originalHeader != null) {
			ListIterator li = headers.listIterator();

			while (li.hasNext()) {
				SIPHeader next = (SIPHeader) li.next();
				if (next.equals(originalHeader)) {
					li.remove();
				}
			}
		}

		if (getHeader(header.getName()) == null) {
			nameTable.put(header.getName().toLowerCase(), h);
			headers.add(h);
		} else {
			if (h instanceof SIPHeaderList) {
				SIPHeaderList hdrlist =
					(SIPHeaderList) nameTable.get(
						header.getName().toLowerCase());
				if (hdrlist != null)
					hdrlist.concatenate((SIPHeaderList) h, top);
				else
					nameTable.put(h.getName().toLowerCase(), h);
			} else {
				nameTable.put(h.getName().toLowerCase(), h);
			}
		}

		// Direct accessor fields for frequently accessed headers.
		if (h instanceof From) {
			this.fromHeader = (From) h;
		} else if (h instanceof ContentLength) {
			this.contentLengthHeader = (ContentLength) h;
		} else if (h instanceof To) {
			this.toHeader = (To) h;
		} else if (h instanceof CSeq) {
			this.cSeqHeader = (CSeq) h;
		} else if (h instanceof CallID) {
			this.callIdHeader = (CallID) h;
		} else if (h instanceof MaxForwards) {
			this.maxForwardsHeader = (MaxForwards) h;
		}

	}

	/**
	 * Remove a header given its name. If multiple headers of a given name
	 * are present then the top flag determines which end to remove headers
	 * from.
	 *
	 * @param headerName is the name of the header to remove.
	 * @param top -- flag that indicates which end of header list to process.
	 */
	public void removeHeader(String headerName, boolean top) {

		SIPHeader toRemove =
			(SIPHeader) nameTable.get(headerName.toLowerCase());
		// nothing to do then we are done.
		if (toRemove == null)
			return;
		if (toRemove instanceof SIPHeaderList) {
			SIPHeaderList hdrList = (SIPHeaderList) toRemove;
			if (top)
				hdrList.removeFirst();
			else
				hdrList.removeLast();
			// Clean up empty list
			if (hdrList.isEmpty()) {
				ListIterator li = this.headers.listIterator();
				while (li.hasNext()) {
					SIPHeader sipHeader = (SIPHeader) li.next();
					if (sipHeader.getName().equalsIgnoreCase(headerName))
						li.remove();
				}
			}
		} else {
			this.nameTable.remove(headerName.toLowerCase());
			if (toRemove instanceof From) {
				this.fromHeader = null;
			} else if (toRemove instanceof To) {
				this.toHeader = null;
			} else if (toRemove instanceof CSeq) {
				this.cSeqHeader = null;
			} else if (toRemove instanceof CallID) {
				this.callIdHeader = null;
			} else if (toRemove instanceof MaxForwards) {
				this.maxForwardsHeader = null;
			} else if (toRemove instanceof ContentLength) {
				this.contentLengthHeader = null;
			}
			ListIterator li = this.headers.listIterator();
			while (li.hasNext()) {
				SIPHeader sipHeader = (SIPHeader) li.next();
				if (sipHeader.getName().equalsIgnoreCase(headerName))
					li.remove();
			}
		}

	}

	/**
	 * Remove all headers given its name.
	 *
	 * @param headerName is the name of the header to remove.
	 */
	public void removeHeader(String headerName) {

		if (headerName == null)
			throw new NullPointerException("null arg");
		SIPHeader toRemove =
			(SIPHeader) nameTable.get(headerName.toLowerCase());
		// nothing to do then we are done.
		if (toRemove == null)
			return;
		nameTable.remove(headerName.toLowerCase());
		// Remove the fast accessor fields.
		if (toRemove instanceof From) {
			this.fromHeader = null;
		} else if (toRemove instanceof To) {
			this.toHeader = null;
		} else if (toRemove instanceof CSeq) {
			this.cSeqHeader = null;
		} else if (toRemove instanceof CallID) {
			this.callIdHeader = null;
		} else if (toRemove instanceof MaxForwards) {
			this.maxForwardsHeader = null;
		} else if (toRemove instanceof ContentLength) {
			this.contentLengthHeader = null;
		}

		ListIterator li = this.headers.listIterator();
		while (li.hasNext()) {
			SIPHeader sipHeader = (SIPHeader) li.next();
			if (sipHeader.getName().equalsIgnoreCase(headerName))
				li.remove();

		}
	}

	/**
	 * Generate (compute) a transaction ID for this SIP message.
	 * @return A string containing the concatenation of various
	 * portions of the From,To,Via and RequestURI portions
	 * of this message as specified in RFC 2543:
	 * All responses to a request contain the same values in
	 * the Call-ID, CSeq, To, and From fields
	 * (with the possible addition of  a tag in the To field
	 * (section 10.43)). This allows responses to be matched with requests.
	 * Incorporates a bug fix  for a bug sent in by Gordon Ledgard of
	 * IPera for generating transactionIDs when no port is present in the
	 * via header.
	 * Incorporates a bug fix for a bug report sent in by Chris Mills
	 * of Nortel Networks (converts to lower case when returning the
	 * transaction identifier).
	 *
	 *@return a string that can be used as a transaction identifier
	 *  for this message. This can be used for matching responses and
	 *  requests (i.e. an outgoing request and its matching response have
	 *	the same computed transaction identifier).
	 */
	public String getTransactionId() {
		Via topVia = null;
		if (!this.getViaHeaders().isEmpty()) {
			topVia = (Via) this.getViaHeaders().first();
		}
		// Have specified a branch Identifier so we can use it to identify
		// the transaction. BranchId is not case sensitive.
		// Branch Id prefix is not case sensitive.
		if (topVia.getBranch() != null
			&& topVia.getBranch().toUpperCase().startsWith(
				SIPConstants.BRANCH_MAGIC_COOKIE.toUpperCase())) {
			// Bis 09 compatible branch assignment algorithm.
			// implies that the branch id can be used as a transaction
			// identifier.
			return topVia.getBranch().toLowerCase();
		} else {
			// Old style client so construct the transaction identifier
			// from various fields of the request.
			StringBuffer retval = new StringBuffer();
			From from = (From) this.getFrom();
			To to = (To) this.getTo();
			String hpFrom = from.getUserAtHostPort();
			retval.append(hpFrom).append(":");
			if (from.hasTag())
				retval.append(from.getTag()).append(":");
			String hpTo = to.getUserAtHostPort();
			retval.append(hpTo).append(":");
			String cid = this.callIdHeader.getCallId();
			retval.append(cid).append(":");
			retval.append(this.cSeqHeader.getSequenceNumber()).append(
				":").append(
				this.cSeqHeader.getMethod());
			if (topVia != null) {
				retval.append(":").append(topVia.getSentBy().encode());
				if (!topVia.getSentBy().hasPort()) {
					retval.append(":").append(5060);
				}
			}
			String hc =
				Utils.toHexString(retval.toString().toLowerCase().getBytes());

			return new Integer( hc.hashCode() ).toString();
			/*
			if (hc.length() < 32)
				return hc;
			else
				return hc.substring(hc.length() - 32, hc.length() - 1);
			*/
		}
		// Convert to lower case -- bug fix as a result of a bug report
		// from Chris Mills of Nortel Networks.
	}

	/**
	 * Return true if this message has a body.
	 */
	public boolean hasContent() {
		return messageContent != null || messageContentBytes != null;
	}

	/**
	 * Return an iterator for the list of headers in this message.
	 * @return an Iterator for the headers of this message.
	 */
	public ListIterator getHeaders() {
		return headers.listIterator();
	}

	/**
	 * Get the first header of the given name.
	 *
	 * @return header -- the first header of the given name.
	 */
	public Header getHeader(String headerName) {
		if (headerName == null)
			throw new NullPointerException("bad name");
		SIPHeader sipHeader =
			(SIPHeader) this.nameTable.get(headerName.toLowerCase());
		if (sipHeader instanceof SIPHeaderList)
			return (Header) ((SIPHeaderList) sipHeader).getFirst();
		else
			return (Header) sipHeader;
	}

	/**
	 * Get the contentType header (null if one does not exist).
	 * @return contentType header
	 */
	public ContentType getContentTypeHeader() {
		return (ContentType) this.getHeader(ContentTypeHeader.NAME);
	}

	/**
	 * Get the from header.
	 * @return -- the from header.
	 */
	public FromHeader getFrom() {
		return (FromHeader) fromHeader;
	}

	/**
	 * Get the ErrorInfo list of headers (null if one does not exist).
	 * @return List containing ErrorInfo headers.
	 */
	public ErrorInfoList getErrorInfoHeaders() {
		return (ErrorInfoList) this.getSIPHeaderList(ErrorInfo.NAME);
	}

	/**
	 * Get the Contact list of headers (null if one does not exist).
	 * @return List containing Contact headers.
	 */
	public ContactList getContactHeaders() {
		return (ContactList) this.getSIPHeaderList(ContactHeader.NAME);
	}

	/**
	 * Get the Via list of headers (null if one does not exist).
	 * @return List containing Via headers.
	 */
	public ViaList getViaHeaders() {
		return (ViaList) getSIPHeaderList(ViaHeader.NAME);
	}


	/**
	 * Set A list of via headers.
	 * @param viaList a list of via headers to add.
	 */
	public void setVia(java.util.List viaList) {
		ViaList vList = new ViaList();
		ListIterator it = viaList.listIterator();
		while (it.hasNext()) {
			Via via = (Via) it.next();
			vList.add(via);
		}
		this.setHeader(vList);
	}

	/**
	 * Set the header given a list of headers.
	 *
	 * @param sipHeaderList a headerList to set
	 */

	public void setHeader(SIPHeaderList sipHeaderList) {
		this.setHeader((Header) sipHeaderList);
	}

	/**
	 * Get the topmost via header.
	 * @return the top most via header if one exists or null if none exists.
	 */
	public Via getTopmostVia() {
		if (this.getViaHeaders() == null)
			return null;
		else
			return (Via) (getViaHeaders().getFirst());
	}

	/**
	 * Get the CSeq list of header (null if one does not exist).
	 * @return CSeq header
	 */
	public CSeqHeader getCSeq() {
		return (CSeqHeader) cSeqHeader;
	}


	/**
	 * Get the Authorization header (null if one does not exist).
	 * @return Authorization header.
	 */
	public Authorization getAuthorization() {
		return (Authorization) this.getHeader(AuthorizationHeader.NAME);
	}

	/**
	 * Get the MaxForwards header (null if one does not exist).
         * @return Max-Forwards header
	 */
	
	public MaxForwardsHeader getMaxForwards() {
		return maxForwardsHeader;
	}

	/**
	 * Set the max forwards header.
	 * @param maxForwards is the MaxForwardsHeader to set.
	 */
	public void setMaxForwards(MaxForwardsHeader maxForwards) {
		this.setHeader(maxForwards);
	}
	


	/**
	 * Get the Route List of headers (null if one does not exist).
	 * @return List containing Route headers
	 */
	public RouteList getRouteHeaders() {
		return (RouteList) getSIPHeaderList(RouteHeader.NAME);
	}

	/**
	 * Get the CallID header (null if one does not exist)
	 *
	 * @return Call-ID header .
	 */
	public CallIdHeader getCallId() {
		return callIdHeader;
	}

	/**
	 * Set the call id header.
	 *
	 * @param callId call idHeader (what else could it be?)
	 */
	public void setCallId(CallIdHeader callId) {
		this.setHeader(callId);
	}

	/**
	 * Get the CallID header (null if one does not exist)
	 *
	 * @param callId -- the call identifier to be assigned to the call id header
	 */
	public void setCallId(String callId) throws java.text.ParseException {
		if (callIdHeader == null) {
			this.setHeader(new CallID());
		}
		callIdHeader.setCallId(callId);
	}


	/**
	 * Get the RecordRoute header list (null if one does not exist).
	 *
	 * @return Record-Route header
	 */
	public RecordRouteList getRecordRouteHeaders() {
		return (RecordRouteList) this.getSIPHeaderList(RecordRouteHeader.NAME);
	}

	/**
	 * Get the To header (null if one does not exist).
	 * @return To header
	 */
	public ToHeader getTo() {
		return (ToHeader) toHeader;
	}

	public void setTo(ToHeader to) {
		this.setHeader(to);
	}

	public void setFrom(FromHeader from) {
		this.setHeader(from);

	}

	/**
	 * Get the ContentLength header (null if one does not exist).
	 *
	 * @return content-length header.
	 */
	public ContentLengthHeader getContentLength() {
		return this.contentLengthHeader;
	}

	/**
	 * Get the message body as a string.
	 * If the message contains a content type header with a specified
	 * charset, and if the payload has been read as a byte array, then
	 * it is returned encoded into this charset.
	 *
	 * @return Message body (as a string)
	 * @throws UnsupportedEncodingException if the platform does not
	 *  support the charset specified in the content type header.
	 *
	 */
	public String getMessageContent() throws UnsupportedEncodingException {
		if (this.messageContent == null && this.messageContentBytes == null)
			return null;
		else if (this.messageContent == null) {
			ContentType contentTypeHeader =
				(ContentType) this.nameTable.get(
					ContentType.NAME.toLowerCase());
			if (contentTypeHeader != null) {
				String charset = contentTypeHeader.getCharset();
				if (charset != null) {
					this.messageContent =
						new String(messageContentBytes, charset);
				} else {
					this.messageContent =
						new String(messageContentBytes, DEFAULT_ENCODING);
				}
			} else
				this.messageContent =
					new String(messageContentBytes, DEFAULT_ENCODING);
		}
		return this.messageContent;
	}

	/**
	 * Get the message content as an array of bytes.
	 * If the payload has been read as a String then it is decoded using
	 * the charset specified in the content type header if it exists.
	 * Otherwise, it is encoded using the default encoding which is
	 * UTF-8.
	 *
	 * @return an array of bytes that is the message payload.
	 */
	public byte[] getRawContent() {
		try {
			if (this.messageContent == null
				&& this.messageContentBytes == null
				&& this.messageContentObject == null) {
				return null;
			} else if (this.messageContentObject != null) {
				String messageContent = this.messageContentObject.toString();
				byte[] messageContentBytes;
				ContentType contentTypeHeader =
					(ContentType) this.nameTable.get(
						ContentTypeHeader.NAME.toLowerCase());
				if (contentTypeHeader != null) {
					String charset = contentTypeHeader.getCharset();
					if (charset != null) {
						messageContentBytes = messageContent.getBytes(charset);
					} else {
						messageContentBytes =
							messageContent.getBytes(DEFAULT_ENCODING);
					}
				} else
					messageContentBytes =
						messageContent.getBytes(DEFAULT_ENCODING);
				return messageContentBytes;
			} else if (this.messageContent != null) {
				byte[] messageContentBytes;
				ContentType contentTypeHeader =
					(ContentType) this.nameTable.get(
						ContentTypeHeader.NAME.toLowerCase());
				if (contentTypeHeader != null) {
					String charset = contentTypeHeader.getCharset();
					if (charset != null) {
						messageContentBytes =
							this.messageContent.getBytes(charset);
					} else {
						messageContentBytes =
							this.messageContent.getBytes(DEFAULT_ENCODING);
					}
				} else
					messageContentBytes =
						this.messageContent.getBytes(DEFAULT_ENCODING);
				return messageContentBytes;
			} else {
				return messageContentBytes;
			}
		} catch (UnsupportedEncodingException ex) {
			InternalErrorHandler.handleException(ex);
			return null;
		}
	}

	/**
	 * Set the message content given type and subtype.
	 *
	 * @param type is the message type (eg. application)
	 * @param subType is the message sybtype (eg. sdp)
	 * @param messageContent is the messge content as a string.
	 */
	public void setMessageContent(
		String type,
		String subType,
		String messageContent) {
		if (messageContent == null)
			throw new IllegalArgumentException("messgeContent is null");
		ContentType ct = new ContentType(type, subType);
		this.setHeader(ct);
		this.messageContent = messageContent;
		this.messageContentBytes = null;
		this.messageContentObject = null;
		// Could be  double byte so we need to compute length
		// after converting to byte[]
		try {
			this.contentLengthHeader.setContentLength(messageContent.getBytes().length);
		} catch (InvalidArgumentException ex) {
		}

	}

	/**
	 * Set the message content after converting the given object to a
	 * String.
	 *
	 * @param content -- content to set.
	 * @param contentTypeHeader -- content type header corresponding to
	 *	content.
	 */
	public void setContent(Object content, ContentTypeHeader contentTypeHeader)
		throws ParseException {
		if (content == null)
			throw new NullPointerException("null content");
		this.setHeader(contentTypeHeader);
		if (content instanceof String) {
			this.messageContent = (String) content;
		} else if (content instanceof byte[]) {
			this.messageContentBytes = (byte[]) content;
		} else
			this.messageContentObject = content;

		try {
			int length = -1;
			if (content instanceof String)
				length = ((String) content).length();
			else if (content instanceof byte[])
				length = ((byte[]) content).length;
			else
				length = content.toString().length();

			if (length != -1) {
				this.contentLengthHeader.setContentLength(length);
			}
		} catch (InvalidArgumentException ex) {
		}

	}

	/**
	 * Get the content of the header.
	 *
	 * @return the content of the sip message.
	 */
	public Object getContent() {
		if (this.messageContentObject != null)
			return messageContentObject;
		else if (this.messageContentBytes != null)
			return this.messageContentBytes;
		else if (this.messageContent != null)
			return this.messageContent;
		else
			return null;
	}

	/**
	 * Set the message content for a given type and subtype.
	 *
	 * @param type is the messge type.
	 * @param subType is the message subType.
	 * @param messageContent is the message content as a byte array.
	 */
	public void setMessageContent(
		String type,
		String subType,
		byte[] messageContent) {
		ContentType ct = new ContentType(type, subType);
		this.setHeader(ct);
		this.setMessageContent(messageContent);
		try {
			this.contentLengthHeader.setContentLength(messageContent.length);
		} catch (InvalidArgumentException ex) {
		}

	}

	/**
	 * Set the message content for this message.
	 *
	 * @param content Message body as a string.
	 */
	public void setMessageContent(String content) {
		// Note that that this could be a double byte character
		// set - bug report by Masafumi Watanabe
		int clength = (content == null ? 0 : content.getBytes().length);
		try {
			this.contentLengthHeader.setContentLength(clength);
		} catch (InvalidArgumentException ex) {
		}
		messageContent = content;
		messageContentBytes = null;
		messageContentObject = null;
	}

	/**
	 * Set the message content as an array of bytes.
	 *
	 * @param content is the content of the message as an array of bytes.
	 */
	public void setMessageContent(byte[] content) {
		try {
			this.contentLengthHeader.setContentLength(content.length);
		} catch (InvalidArgumentException ex) {
		}

		messageContentBytes = content;
		messageContent = null;
		messageContentObject = null;
	}

	/**
	 * Remove the message content if it exists.
	 */
	public void removeContent() {
		messageContent = null;
		messageContentBytes = null;
		messageContentObject = null;
		try {
			this.contentLengthHeader.setContentLength(0);
		} catch (InvalidArgumentException ex) {
		}
	}

	/**
	 * Get a SIP header or Header list given its name.
	 * @param headerName is the name of the header to get.
	 * @return a header or header list that contians the retrieved header.
	 */
	public ListIterator getHeaders(String headerName) {
		if (headerName == null)
			throw new NullPointerException("null headerName");
		SIPHeader sipHeader =
			(SIPHeader) nameTable.get(headerName.toLowerCase());
		// empty iterator
		if (sipHeader == null)
			return new LinkedList().listIterator();
		if (sipHeader instanceof SIPHeaderList) {
			return ((SIPHeaderList) sipHeader).listIterator();
		} else {
			return new HeaderIterator(this, sipHeader);
		}
	}

	private SIPHeaderList getSIPHeaderList(String headerName) {
		return (SIPHeaderList) nameTable.get(headerName.toLowerCase());
	}

	private LinkedList getHeaderList(String headerName) {
		SIPHeader sipHeader =
			(SIPHeader) nameTable.get(headerName.toLowerCase());
		if (sipHeader == null)
			return null;
		else if (sipHeader instanceof SIPHeaderList)
			return (LinkedList) (((SIPHeaderList) sipHeader).getHeaderList());
		else {
			LinkedList ll = new LinkedList();
			ll.add(sipHeader);
			return ll;
		}
	}

	/**
	 * Return true if the SIPMessage has a header of the given name.
	 *
	 * @param headerName is the header name for which we are testing.
	 * @return true if the header is present in the message
	 */
	public boolean hasHeader(String headerName) {
		return nameTable.containsKey(headerName.toLowerCase());
	}

	/**
	 * Return true if the message has a From header tag.
	 *
	 * @return true if the message has a from header and that header has
	 * 		a tag.
	 */
	public boolean hasFromTag() {
		return fromHeader != null && fromHeader.getTag() != null;
	}

	/**
	 * Return true if the message has a To header tag.
	 *
	 * @return true if the message has a to header and that header has
	 * 		a tag.
	 */
	public boolean hasToTag() {
		return toHeader != null && toHeader.getTag() != null;
	}

	/**
	 * Return the from tag.
	 *
	 * @return the tag from the from header.
	 *
	 */
	public String getFromTag() {
		return fromHeader == null ? null : fromHeader.getTag();
	}

	/**
	 * Set the From Tag.
	 *
	 *@param tag -- tag to set in the from header.
	 */
	public void setFromTag(String tag) {
		try {
			fromHeader.setTag(tag);
		} catch (ParseException e) {
		}
	}

	/**
	 * Set the to tag.
	 *
	 *@param tag -- tag to set.
	 */
	public void setToTag(String tag) {
		try {
			toHeader.setTag(tag);
		} catch (ParseException e) {
		}
	}

	/**
	 * Return the to tag.
	 */
	public String getToTag() {
		return toHeader == null ? null : toHeader.getTag();
	}

	/**
	 * Return the encoded first line.
	 */
	public abstract String getFirstLine();

	/**
	 * Add a SIP header.
	 * @param sipHeader -- sip header to add.
	 */
	public void addHeader(javax.sip.header.Header sipHeader) {
		// Content length is never stored. Just computed.
		SIPHeader sh = (SIPHeader) sipHeader;
		try {
			if (sipHeader instanceof ViaHeader) {
				attachHeader(sh, false, true);
			} else {
				attachHeader(sh, false, false);
			}
		} catch (SIPDuplicateHeaderException ex) {
			try {
				if (sipHeader instanceof ContentLength) {
					ContentLength cl = (ContentLength) sipHeader;
					contentLengthHeader.setContentLength(cl.getContentLength());
				}
			} catch (InvalidArgumentException e) {
			}
		}
	}

	/**
	 * Add a header to the unparsed list of headers.
	 *
	 * @param unparsed -- unparsed header to add to the list.
	 */
	public void addUnparsed(String unparsed) {
		this.unrecognizedHeaders.add(unparsed);
	}

	/**
	 * Add a SIP header.
	 * @param sipHeader -- string version of SIP header to add.
	 */

	public void addHeader(String sipHeader) {
		String hdrString = sipHeader.trim() + "\n";
		try {
			HeaderParser parser = ParserFactory.createParser(sipHeader);
			SIPHeader sh = parser.parse();
			this.attachHeader(sh, false);
		} catch (ParseException ex) {
			this.unrecognizedHeaders.add(hdrString);
		}
	}

	/**
	 * Get a list containing the unrecognized headers.
	 * @return a linked list containing unrecongnized headers.
	 */
	public ListIterator getUnrecognizedHeaders() {
		return this.unrecognizedHeaders.listIterator();
	}

	/**
	 * Get the header names.
	 *
	 * @return a list iterator to a list of header names. These are ordered
	 * in the same order as are present in the message.
	 */
	public ListIterator getHeaderNames() {
		ListIterator li = this.headers.listIterator();
		LinkedList retval = new LinkedList();
		while (li.hasNext()) {
			SIPHeader sipHeader = (SIPHeader) li.next();
			String name = sipHeader.getName();
			retval.add(name);
		}
		return retval.listIterator();
	}

	/**
	 * Compare for equality.
	 *
	 * @param other -- the other object to compare with.
	 */
	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass())) {
			return false;
		}
		SIPMessage otherMessage = (SIPMessage) other;
		Collection values = this.nameTable.values();
		Iterator it = values.iterator();
		if (nameTable.size() != otherMessage.nameTable.size()) {
			return false;
		}

		while (it.hasNext()) {
			SIPHeader mine = (SIPHeader) it.next();
			SIPHeader his =
				(SIPHeader) (otherMessage
					.nameTable
					.get(mine.getName().toLowerCase()));
			if (his == null) {
				return false;
			} else if (!his.equals(mine)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * get content disposition header or null if no such header exists.
	 *
	 * @return the contentDisposition header
	 */
	public javax.sip.header.ContentDispositionHeader getContentDisposition() {
		return (ContentDispositionHeader) this.getHeader(
			ContentDispositionHeader.NAME);
	}

	/**
	 * get the content encoding header.
	 *
	 * @return the contentEncoding header.
	 */
	public javax.sip.header.ContentEncodingHeader getContentEncoding() {
		return (ContentEncodingHeader) this.getHeader(
			ContentEncodingHeader.NAME);
	}
	/**
	 * Get the contentLanguage header.
	 *
	 * @return the content language header.
	 */
	public javax.sip.header.ContentLanguageHeader getContentLanguage() {
		return (ContentLanguageHeader) getHeader(ContentLanguageHeader.NAME);

	}

	/**
	 * Get the exipres header.
	 *
	 * @return the expires header or null if one does not exist.
	 */
	public javax.sip.header.ExpiresHeader getExpires() {
		return (ExpiresHeader) getHeader(ExpiresHeader.NAME);

	}

	/**
	 * Set the expiresHeader
	 *
	 *@param expiresHeader -- the expires header to set.
	 */

	public void setExpires(ExpiresHeader expiresHeader) {
		this.setHeader(expiresHeader);
	}

	/**
	 * Set the content disposition header.
	 *
	 *@param contentDispositionHeader -- content disposition header.
	 */

	public void setContentDisposition(ContentDispositionHeader contentDispositionHeader) {
		this.setHeader(contentDispositionHeader);

	}

	public void setContentEncoding(ContentEncodingHeader contentEncodingHeader) {
		this.setHeader(contentEncodingHeader);

	}

	public void setContentLanguage(ContentLanguageHeader contentLanguageHeader) {
		this.setHeader(contentLanguageHeader);
	}

	/**
	 * Set the content length header.
	 *
	 *@param contentLength -- content length header.
	 */
	public void setContentLength(ContentLengthHeader contentLength) {
		try {
			this.contentLengthHeader.setContentLength(
				contentLength.getContentLength());
		} catch (InvalidArgumentException ex) {
		}

	}

	/** Set the size of all the headers. This is for book keeping.
	* Called by the parser.
	*@param size -- size of the headers.
	*/
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return this.size;
	}

	/**
	 * Set the CSeq header.
	 *
	 *@param cseqHeader -- CSeq Header.
	 */

	public void setCSeq(CSeqHeader cseqHeader) {
		this.setHeader(cseqHeader);
	}

	public abstract void setSIPVersion(String sipVersion)
		throws ParseException;

	public abstract String getSIPVersion();

	public abstract String toString();

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.10  2004/09/10 18:26:08  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * added match examples for the benifit of those building test frameworks.
 *
 * Revision 1.9  2004/07/25 19:26:44  mranga
 * Reviewed by:   mranga
 * Allows multiple Authorization headers in a message. Some minor cleanup.
 *
 * Revision 1.8  2004/03/25 15:15:04  mranga
 * Reviewed by:   mranga
 * option to log message content added.
 *
 * Revision 1.7  2004/03/01 12:37:27  mranga
 * Submitted by:  Watanabe Masafumi
 * Reviewed by:   mranga
 * Allow for double byte characters when setting content length.
 *
 * Revision 1.6  2004/02/29 00:46:33  mranga
 * Reviewed by:   mranga
 * Added new configuration property to limit max message size for TCP transport.
 * The property is gov.nist.javax.sip.MAX_MESSAGE_SIZE
 *
 * Revision 1.5  2004/02/18 14:33:02  mranga
 * Submitted by:  Bruno Konik
 * Reviewed by:   mranga
 * Remove extraneous newline in encoding messages. Test for empty sdp announce
 * rather than die with null when null is passed to sdp announce parser.
 * Fixed bug in checking for \n\n when looking for message end.
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
