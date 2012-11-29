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
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
 *******************************************************************************/
package gov.nist.javax.sip.message;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.RecordRouteList;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SIPHeaderList;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.ViaList;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.SipException;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ServerHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.TimeStampHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

/*
 * Acknowledgements: Mark Bednarek made a few fixes to this code. Jeff Keyser added two methods
 * that create responses and generate cancel requests from incoming orignial requests without the
 * additional overhead of encoding and decoding messages. Bruno Konik noticed an extraneous
 * newline added to the end of the buffer when encoding it. Incorporates a bug report from Andreas
 * Bystrom. Szabo Barna noticed a contact in a cancel request - this is a pointless header for
 * cancel. Antonis Kyardis contributed bug fixes. Jeroen van Bemmel noted that method names are
 * case sensitive, should use equals() in getting CannonicalName
 * 
 */

/**
 * The SIP Request structure.
 * 
 * @version 1.2 $Revision: 1.57 $ $Date: 2010-09-17 20:06:57 $
 * @since 1.1
 * 
 * @author M. Ranganathan <br/>
 * 
 * 
 * 
 */

public class SIPRequest extends SIPMessage implements javax.sip.message.Request, RequestExt {

    private static final long serialVersionUID = 3360720013577322927L;

    private static final String DEFAULT_USER = "ip";

    private static final String DEFAULT_TRANSPORT = "udp";

    private transient Object transactionPointer;

    protected RequestLine requestLine;

    private transient Object messageChannel;
    
    

    private transient Object inviteTransaction; // The original invite request for a
    // given cancel request

    /**
     * Set of target refresh methods, currently: INVITE, UPDATE, SUBSCRIBE, NOTIFY, REFER
     * 
     * A target refresh request and its response MUST have a Contact
     */
    private static final Set<String> targetRefreshMethods = new HashSet<String>();

    /*
     * A table that maps a name string to its cannonical constant. This is used to speed up
     * parsing of messages .equals reduces to == if we use the constant value.
     * 
     * jeand : Setting the capacity to save on memory since the capacity here will never change
     */
    private static final Map<String, String> nameTable = new ConcurrentHashMap<String, String>(15);
    
    protected static final Set<String> headersToIncludeInResponse = new HashSet<String>(0);

    private static void putName(String name) {
        nameTable.put(name, name);
    }

    static {
        targetRefreshMethods.add(Request.INVITE);
        targetRefreshMethods.add(Request.UPDATE);
        targetRefreshMethods.add(Request.SUBSCRIBE);
        targetRefreshMethods.add(Request.NOTIFY);
        targetRefreshMethods.add(Request.REFER);

        putName(Request.INVITE);
        putName(Request.BYE);
        putName(Request.CANCEL);
        putName(Request.ACK);
        putName(Request.PRACK);
        putName(Request.INFO);
        putName(Request.MESSAGE);
        putName(Request.NOTIFY);
        putName(Request.OPTIONS);
        putName(Request.PRACK);
        putName(Request.PUBLISH);
        putName(Request.REFER);
        putName(Request.REGISTER);
        putName(Request.SUBSCRIBE);
        putName(Request.UPDATE);

        headersToIncludeInResponse.add(FromHeader.NAME.toLowerCase());
        headersToIncludeInResponse.add(ToHeader.NAME.toLowerCase());
        headersToIncludeInResponse.add(ViaHeader.NAME.toLowerCase());
        headersToIncludeInResponse.add(RecordRouteHeader.NAME.toLowerCase());
        headersToIncludeInResponse.add(CallIdHeader.NAME.toLowerCase());
        headersToIncludeInResponse.add(CSeqHeader.NAME.toLowerCase());
        headersToIncludeInResponse.add(TimeStampHeader.NAME.toLowerCase());        
    }

    /**
     * @return true iff the method is a target refresh
     */
    public static boolean isTargetRefresh(String ucaseMethod) {
        return targetRefreshMethods.contains(ucaseMethod);
    }

    /**
     * @return true iff the method is a dialog creating method
     */
    public static boolean isDialogCreating(String ucaseMethod) {
        return SIPTransactionStack.isDialogCreated(ucaseMethod);
    }
    
    /**
     * Set to standard constants to speed up processing. this makes equals comparisons run much
     * faster in the stack because then it is just identity comparision. Character by char
     * comparison is not required. The method returns the String CONSTANT corresponding to the
     * String name.
     * 
     */
    public static String getCannonicalName(String method) {

        if (nameTable.containsKey(method))
            return (String) nameTable.get(method);
        else
            return method;
    }

    /**
     * Get the Request Line of the SIPRequest.
     * 
     * @return the request line of the SIP Request.
     */

    public RequestLine getRequestLine() {
        return requestLine;
    }

    /**
     * Set the request line of the SIP Request.
     * 
     * @param requestLine is the request line to set in the SIP Request.
     */

    public void setRequestLine(RequestLine requestLine) {
        this.requestLine = requestLine;
    }

    /**
     * Constructor.
     */
    public SIPRequest() {
        super();
    }

    /**
     * Convert to a formatted string for pretty printing. Note that the encode method converts
     * this into a sip message that is suitable for transmission. Note hack here if you want to
     * convert the nice curly brackets into some grotesque XML tag.
     * 
     * @return a string which can be used to examine the message contents.
     * 
     */
    public String debugDump() {
        String superstring = super.debugDump();
        stringRepresentation = "";
        sprint(SIPRequest.class.getName());
        sprint("{");
        if (requestLine != null)
            sprint(requestLine.debugDump());
        sprint(superstring);
        sprint("}");
        return stringRepresentation;
    }

    /**
     * Check header for constraints. (1) Invite options and bye requests can only have SIP URIs in
     * the contact headers. (2) Request must have cseq, to and from and via headers. (3) Method in
     * request URI must match that in CSEQ.
     */
    public void checkHeaders() throws ParseException {
        String prefix = "Missing a required header : ";

        /* Check for required headers */

        if (getCSeq() == null) {
            throw new ParseException(prefix + CSeqHeader.NAME, 0);
        }
        if (getTo() == null) {
            throw new ParseException(prefix + ToHeader.NAME, 0);
        }

        if (this.callIdHeader == null || this.callIdHeader.getCallId() == null
                || callIdHeader.getCallId().equals("")) {
            throw new ParseException(prefix + CallIdHeader.NAME, 0);
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

        if (getTopmostVia() == null)
            throw new ParseException("No via header in request! ", 0);

        if (getMethod().equals(Request.NOTIFY)) {
            if (getHeader(SubscriptionStateHeader.NAME) == null)
                throw new ParseException(prefix + SubscriptionStateHeader.NAME, 0);

            if (getHeader(EventHeader.NAME) == null)
                throw new ParseException(prefix + EventHeader.NAME, 0);

        } else if (getMethod().equals(Request.PUBLISH)) {
            /*
             * For determining the type of the published event state, the EPA MUST include a
             * single Event header field in PUBLISH requests. The value of this header field
             * indicates the event package for which this request is publishing event state.
             */
            if (getHeader(EventHeader.NAME) == null)
                throw new ParseException(prefix + EventHeader.NAME, 0);
        }

        /*
         * RFC 3261 8.1.1.8 The Contact header field MUST be present and contain exactly one SIP
         * or SIPS URI in any request that can result in the establishment of a dialog. For the
         * methods defined in this specification, that includes only the INVITE request. For these
         * requests, the scope of the Contact is global. That is, the Contact header field value
         * contains the URI at which the UA would like to receive requests, and this URI MUST be
         * valid even if used in subsequent requests outside of any dialogs.
         * 
         * If the Request-URI or top Route header field value contains a SIPS URI, the Contact
         * header field MUST contain a SIPS URI as well.
         */
        final String method = requestLine.getMethod();
        if (SIPTransactionStack.isDialogCreated(method)) {
            if (this.getContactHeader() == null) {
                // Make sure this is not a target refresh. If this is a target
                // refresh its ok not to have a contact header. Otherwise
                // contact header is mandatory.
                if (this.getToTag() == null)
                    throw new ParseException(prefix + ContactHeader.NAME, 0);
            }

            /*
          	 * Vladimir Ralev : Removing this check to handle proxy with JSIP.
             * For proxy the contact is not relevent, so the UAC behavior RFC 8.1.1.8 doesn't apply, we remove this check so double-record-routing proxies will work
....
CSeq: 1 INVITE
From: <sip:proxy-tls@sip-servlets.com>;tag=5326009
To: <sip:receiver@sip-servlets.com>
Via: SIP/2.0/TLS 127.0.0.1:5071;branch=z9hG4bKd72cf250-04fc-49a8-bde1-2b9728ab0dc1_8a474751_1317304087784710000
Via: SIP/2.0/TCP 127.0.0.1:5080;branch=z9hG4bK-343139-25f9b919076ced8b4c20506d35036178;rport=60554
Max-Forwards: 69
REM: RRRREM
Contact: <sip:proxy-tls@127.0.0.1:5080>
Record-Route: <sip:127.0.0.1:5071;transport=tls;appname=8a474751;proxy=true;app_id=d72cf250-04fc-49a8-bde1-2b9728ab0dc1;lr>
Record-Route: <sip:127.0.0.1:5070;transport=tcp;appname=8a474751;proxy=true;app_id=d72cf250-04fc-49a8-bde1-2b9728ab0dc1;lr>
Content-Length: 0

Caused by: java.text.ParseException: Scheme for contact should be sips:sip:proxy-tls@127.0.0.1:5080
	at gov.nist.javax.sip.message.SIPRequest.checkHeaders(SIPRequest.java:321)
	at gov.nist.javax.sip.SipProviderImpl.getNewClientTransaction(SipProviderImpl.java:302)
	... 23 more
                     
            if (requestLine.getUri() instanceof SipUri) {
                String scheme = ((SipUri) requestLine.getUri()).getScheme();
                if ("sips".equalsIgnoreCase(scheme)) {
                    RecordRouteHeader rrh = (RecordRouteHeader) getHeader(RecordRouteHeader.NAME);
 
                    if(rrh == null) { // If RRH is not null, then this is proxy so RFC 8.1.1.8 doesn't apply, contact doesnt matter when there is RRH
                    	SipUri sipUri = (SipUri) this.getContactHeader().getAddress().getURI();
                    	if (!sipUri.getScheme().equals("sips")) {
                    		throw new ParseException("Scheme for contact should be sips:" + sipUri, 0);
                    	}
                    }
                }
            } */
        }

        /*
         * Contact header is mandatory for a SIP INVITE request.
         */
        /* emmartins: dupe logic, see above
         if (this.getContactHeader() == null
                && SIPTransactionStack.isDialogCreated(method)) {
            throw new ParseException("Contact Header is Mandatory for a SIP INVITE", 0);
        }*/

        if (requestLine != null && method != null
                && getCSeq().getMethod() != null
                && method.compareTo(getCSeq().getMethod()) != 0) {
            throw new ParseException("CSEQ method mismatch with  Request-Line ", 0);

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
        if (method.compareTo(Request.REGISTER) == 0 || method.compareTo(Request.INVITE) == 0) {
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
                method = getCannonicalName(cseq.getMethod());
                requestLine.setMethod(method);
            }
        }
    }

    /**
     * A conveniance function to access the Request URI.
     * 
     * @return the requestURI if it exists.
     */
    public javax.sip.address.URI getRequestURI() {
        if (this.requestLine == null)
            return null;
        else
            return (javax.sip.address.URI) this.requestLine.getUri();
    }

    /**
     * Sets the RequestURI of Request. The Request-URI is a SIP or SIPS URI or a general URI. It
     * indicates the user or service to which this request is being addressed. SIP elements MAY
     * support Request-URIs with schemes other than "sip" and "sips", for example the "tel" URI
     * scheme. SIP elements MAY translate non-SIP URIs using any mechanism at their disposal,
     * resulting in SIP URI, SIPS URI, or some other scheme.
     * 
     * @param uri the new Request URI of this request message
     */
    public void setRequestURI(URI uri) {
        if ( uri == null ) {
            throw new NullPointerException("Null request URI");
        }
        if (this.requestLine == null) {
            this.requestLine = new RequestLine();
        }
        this.requestLine.setUri((GenericURI) uri);
        this.nullRequest = false;
    }

    /**
     * Set the method.
     * 
     * @param method is the method to set.
     * @throws IllegalArgumentException if the method is null
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
                this.cSeqHeader.setMethod(meth);
            } catch (ParseException e) {
            }
        }
    }

    /**
     * Get the method from the request line.
     * 
     * @return the method from the request line if the method exits and null if the request line
     *         or the method does not exist.
     */
    public String getMethod() {
        if (requestLine == null)
            return null;
        else
            return requestLine.getMethod();
    }

    /**
     * Encode the SIP Request as a string.
     * 
     * @return an encoded String containing the encoded SIP Message.
     */

    public String encode() {
        String retval;
        if (requestLine != null) {
            this.setRequestLineDefaults();
            retval = requestLine.encode() + super.encode();
        } else if (this.isNullRequest()) {
            retval = "\r\n\r\n";
        } else {       
            retval = super.encode();
        }
        return retval;
    }

    /**
     * Encode only the headers and not the content.
     */
    public StringBuilder encodeMessage(StringBuilder retval) {    	       
        if (requestLine != null) {
            this.setRequestLineDefaults();
            requestLine.encode(retval);
            encodeSIPHeaders(retval);
        } else if (this.isNullRequest()) {
            retval.append("\r\n\r\n");
        } else
            retval = encodeSIPHeaders(retval);
        return retval;

    }

    /**
     * ALias for encode above.
     */
    public String toString() {
        return this.encode();
    }

    /**
     * Make a clone (deep copy) of this object. You can use this if you want to modify a request
     * while preserving the original
     * 
     * @return a deep copy of this object.
     */

    public Object clone() {
        SIPRequest retval = (SIPRequest) super.clone();
        // Do not copy over the tx pointer -- this is only for internal
        // tracking.
        retval.transactionPointer = null;
        if (this.requestLine != null)
            retval.requestLine = (RequestLine) this.requestLine.clone();

        return retval;
    }

    /**
     * Compare for equality.
     * 
     * @param other object to compare ourselves with.
     */
    public boolean equals(Object other) {
        if (!this.getClass().equals(other.getClass()))
            return false;
        SIPRequest that = (SIPRequest) other;
        return requestLine.equals(that.requestLine) && super.equals(other);
    }

    /**
     * Get the message as a linked list of strings. Use this if you want to iterate through the
     * message.
     * 
     * @return a linked list containing the request line and headers encoded as strings.
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
     * Match with a template. You can use this if you want to match incoming messages with a
     * pattern and do something when you find a match. This is useful for building filters/pattern
     * matching responders etc.
     * 
     * @param matchObj object to match ourselves with (null matches wildcard)
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

    /**
     * Encode this into a byte array. This is used when the body has been set as a binary array
     * and you want to encode the body as a byte array for transmission.
     * 
     * @return a byte array containing the SIPRequest encoded as a byte array.
     */

    public byte[] encodeAsBytes(String transport) {
        if (this.isNullRequest()) {
            // Encoding a null message for keepalive.
            return "\r\n\r\n".getBytes();
        } else if ( this.requestLine == null ) {
            return new byte[0];
        }

        byte[] rlbytes = null;
        if (requestLine != null) {
            try {
                rlbytes = requestLine.encode().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes(transport);
        byte[] retval = new byte[rlbytes.length + superbytes.length];
        System.arraycopy(rlbytes, 0, retval, 0, rlbytes.length);
        System.arraycopy(superbytes, 0, retval, rlbytes.length, superbytes.length);
        return retval;
    }

    /**
     * Creates a default SIPResponse message for this request. Note You must add the necessary
     * tags to outgoing responses if need be. For efficiency, this method does not clone the
     * incoming request. If you want to modify the outgoing response, be sure to clone the
     * incoming request as the headers are shared and any modification to the headers of the
     * outgoing response will result in a modification of the incoming request. Tag fields are
     * just copied from the incoming request. Contact headers are removed from the incoming
     * request. Added by Jeff Keyser.
     * 
     * @param statusCode Status code for the response. Reason phrase is generated.
     * 
     * @return A SIPResponse with the status and reason supplied, and a copy of all the original
     *         headers from this request.
     */

    public SIPResponse createResponse(int statusCode) {

        String reasonPhrase = SIPResponse.getReasonPhrase(statusCode);
        return this.createResponse(statusCode, reasonPhrase);

    }

    /**
     * Creates a default SIPResponse message for this request. Note You must add the necessary
     * tags to outgoing responses if need be. For efficiency, this method does not clone the
     * incoming request. If you want to modify the outgoing response, be sure to clone the
     * incoming request as the headers are shared and any modification to the headers of the
     * outgoing response will result in a modification of the incoming request. Tag fields are
     * just copied from the incoming request. Contact headers are removed from the incoming
     * request. Added by Jeff Keyser. Route headers are not added to the response.
     * 
     * @param statusCode Status code for the response.
     * @param reasonPhrase Reason phrase for this response.
     * 
     * @return A SIPResponse with the status and reason supplied, and a copy of all the original
     *         headers from this request except the ones that are not supposed to be part of the
     *         response .
     */

    public SIPResponse createResponse(int statusCode, String reasonPhrase) {
        SIPResponse newResponse;
//        Iterator headerIterator;
//        SIPHeader nextHeader;

        newResponse = new SIPResponse();
        try {
            newResponse.setStatusCode(statusCode);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Bad code " + statusCode);
        }
        if (reasonPhrase != null)
            newResponse.setReasonPhrase(reasonPhrase);
        else
            newResponse.setReasonPhrase(SIPResponse.getReasonPhrase(statusCode));
        
//        headerIterator = getHeaders();
//        while (headerIterator.hasNext()) {
//            nextHeader = (SIPHeader) headerIterator.next();
//            if (nextHeader instanceof From
//                    || nextHeader instanceof To
//                    || nextHeader instanceof ViaList
//                    || nextHeader instanceof CallID
//                    || (nextHeader instanceof RecordRouteList && mustCopyRR(statusCode))
//                    || nextHeader instanceof CSeq
//                    // We just copy TimeStamp for all headers (not just 100).
//                    || nextHeader instanceof TimeStamp) {
//
//                try {
//
//                    newResponse.attachHeader((SIPHeader) nextHeader.clone(), false);
//                } catch (SIPDuplicateHeaderException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        
        // no need to iterate through all headers to create the response since we know which headers
        // we only want to keep and helps the lazy parsing to avoid going through all headers
        for(String headerName : headersToIncludeInResponse) {                	
        	SIPHeader nextHeader = headerTable.get(headerName);
        	if(nextHeader != null) {
        		if(!(nextHeader instanceof RecordRouteList) || (nextHeader instanceof RecordRouteList && mustCopyRR(statusCode))) {
        			try {
        				newResponse.attachHeader((SIPHeader) nextHeader.clone(), false);
        			} catch (SIPDuplicateHeaderException e) {
                      e.printStackTrace();
                  }
        		}
        	}
        }
        
        if (MessageFactoryImpl.getDefaultServerHeader() != null) {
            newResponse.setHeader(MessageFactoryImpl.getDefaultServerHeader());

        }
        // Commented out for Issue 305 : 
        // tag parameter in the To header in 100 Trying response for re-INVITE 
        // is needed
        // Fix by Tomasz Zieleniewski
//        if (newResponse.getStatusCode() == 100) {
//            // Trying is never supposed to have the tag parameter set.
//            newResponse.getTo().removeParameter("tag");
//        }
        ServerHeader server = MessageFactoryImpl.getDefaultServerHeader();
        if (server != null) {
            newResponse.setHeader(server);
        }
        return newResponse;
    }

    // Helper method for createResponse, to avoid copying Record-Route unless needed
    protected final boolean mustCopyRR( int code ) {
    	// Only for 1xx-2xx, not for 100 or errors
    	if ( code>100 && code<300 ) {
    		return isDialogCreating( this.getMethod() ) && getToTag() == null;
    	} else return false;
    }
    
    /**
     * Creates a default SIPResquest message that would cancel this request. Note that tag
     * assignment and removal of is left to the caller (we use whatever tags are present in the
     * original request).
     * 
     * @return A CANCEL SIPRequest constructed according to RFC3261 section 9.1
     * 
     * @throws SipException
     * @throws ParseException
     */
    public SIPRequest createCancelRequest() throws SipException {

        // see RFC3261 9.1

        // A CANCEL request SHOULD NOT be sent to cancel a request other than
        // INVITE

        if (!this.getMethod().equals(Request.INVITE))
            throw new SipException("Attempt to create CANCEL for " + this.getMethod());

        /*
         * The following procedures are used to construct a CANCEL request. The Request-URI,
         * Call-ID, To, the numeric part of CSeq, and From header fields in the CANCEL request
         * MUST be identical to those in the request being cancelled, including tags. A CANCEL
         * constructed by a client MUST have only a single Via header field value matching the top
         * Via value in the request being cancelled. Using the same values for these header fields
         * allows the CANCEL to be matched with the request it cancels (Section 9.2 indicates how
         * such matching occurs). However, the method part of the CSeq header field MUST have a
         * value of CANCEL. This allows it to be identified and processed as a transaction in its
         * own right (See Section 17).
         */
        SIPRequest cancel = new SIPRequest();
        cancel.setRequestLine((RequestLine) this.requestLine.clone());
        cancel.setMethod(Request.CANCEL);
        cancel.setHeader((Header) this.callIdHeader.clone());
        cancel.setHeader((Header) this.toHeader.clone());
        cancel.setHeader((Header) cSeqHeader.clone());
        try {
            cancel.getCSeq().setMethod(Request.CANCEL);
        } catch (ParseException e) {
            e.printStackTrace(); // should not happen
        }
        cancel.setHeader((Header) this.fromHeader.clone());

        cancel.addFirst((Header) this.getTopmostVia().clone());
        cancel.setHeader((Header) this.maxForwardsHeader.clone());

        /*
         * If the request being cancelled contains a Route header field, the CANCEL request MUST
         * include that Route header field's values.
         */
        if (this.getRouteHeaders() != null) {
            cancel.setHeader((SIPHeaderList< ? >) this.getRouteHeaders().clone());
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            cancel.setHeader(MessageFactoryImpl.getDefaultUserAgentHeader());

        }
        return cancel;
    }

    /**
     * Creates a default ACK SIPRequest message for this original request. Note that the
     * defaultACK SIPRequest does not include the content of the original SIPRequest. If
     * responseToHeader is null then the toHeader of this request is used to construct the ACK.
     * Note that tag fields are just copied from the original SIP Request. Added by Jeff Keyser.
     * 
     * @param responseToHeader To header to use for this request.
     * 
     * @return A SIPRequest with an ACK method.
     */
    public SIPRequest createAckRequest(To responseToHeader) {
//    	SIPRequest newRequest;
//        Iterator headerIterator;
//        SIPHeader nextHeader;

    	// cloning instead of iterating through headers so that lazy parsers don't have to parse the messages fully
    	// to create ACK requests
        SIPRequest newRequest = (SIPRequest) this.clone();
//        newRequest = new SIPRequest();
//        newRequest.setRequestLine((RequestLine) this.requestLine.clone());
        newRequest.setMethod(Request.ACK);
        // Ack and cancel do not get ROUTE headers.
        // Route header for ACK is assigned by the
        // Dialog if necessary.
        newRequest.removeHeader(RouteHeader.NAME);
        // Remove proxy auth header.
        // Assigned by the Dialog if necessary.
        newRequest.removeHeader(ProxyAuthorizationHeader.NAME);
        // Adding content is responsibility of user.
        newRequest.removeContent();
        // Content type header is removed since
        // content length is 0.
        newRequest.removeHeader(ContentTypeHeader.NAME);
        // The CSeq header field in the
        // ACK MUST contain the same value for the
        // sequence number as was present in the
        // original request, but the method parameter
        // MUST be equal to "ACK".
        try{
        	newRequest.getCSeq().setMethod(Request.ACK);
        } catch (ParseException e) {
        }
        if (responseToHeader != null) {
            newRequest.setTo(responseToHeader);
        }
        // CONTACT header does not apply for ACK requests.
        newRequest.removeHeader(ContactHeader.NAME);
        newRequest.removeHeader(ExpiresHeader.NAME);
        ViaList via = newRequest.getViaHeaders();
        // Bug reported by Gianluca Martinello
        // The ACK MUST contain a single Via header field,
        // and this MUST be equal to the top Via header
        // field of the original
        // request.
        if(via != null && via.size() > 1) {
        	for(int i = 2; i < via.size(); i++) {
        		via.remove(i);
        	}
        }
        
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            newRequest.setHeader(MessageFactoryImpl.getDefaultUserAgentHeader());

        }
        return newRequest;
    }

    /**
     * Creates an ACK for non-2xx responses according to RFC3261 17.1.1.3
     * 
     * @return A SIPRequest with an ACK method.
     * @throws SipException
     * @throws NullPointerException
     * @throws ParseException
     * 
     * @author jvb
     */
    public final SIPRequest createErrorAck(To responseToHeader) throws SipException,
            ParseException {

        /*
         * The ACK request constructed by the client transaction MUST contain values for the
         * Call-ID, From, and Request-URI that are equal to the values of those header fields in
         * the request passed to the transport by the client transaction (call this the "original
         * request"). The To header field in the ACK MUST equal the To header field in the
         * response being acknowledged, and therefore will usually differ from the To header field
         * in the original request by the addition of the tag parameter. The ACK MUST contain a
         * single Via header field, and this MUST be equal to the top Via header field of the
         * original request. The CSeq header field in the ACK MUST contain the same value for the
         * sequence number as was present in the original request, but the method parameter MUST
         * be equal to "ACK".
         */
        SIPRequest newRequest = new SIPRequest();
        newRequest.setRequestLine((RequestLine) this.requestLine.clone());
        newRequest.setMethod(Request.ACK);
        newRequest.setHeader((Header) this.callIdHeader.clone());
        newRequest.setHeader((Header) this.maxForwardsHeader.clone()); // ISSUE
        // 130
        // fix
        newRequest.setHeader((Header) this.fromHeader.clone());
        newRequest.setHeader((Header) responseToHeader.clone());
        newRequest.addFirst((Header) this.getTopmostVia().clone());
        newRequest.setHeader((Header) cSeqHeader.clone());
        newRequest.getCSeq().setMethod(Request.ACK);

        /*
         * If the INVITE request whose response is being acknowledged had Route header fields,
         * those header fields MUST appear in the ACK. This is to ensure that the ACK can be
         * routed properly through any downstream stateless proxies.
         */
        if (this.getRouteHeaders() != null) {
            newRequest.setHeader((SIPHeaderList) this.getRouteHeaders().clone());
        }
        if (MessageFactoryImpl.getDefaultUserAgentHeader() != null) {
            newRequest.setHeader(MessageFactoryImpl.getDefaultUserAgentHeader());

        }
        return newRequest;
    }

     /**
     * Get the host from the topmost via header.
     * 
     * @return the string representation of the host from the topmost via header.
     */
    public String getViaHost() {
        Via via = (Via) this.getViaHeaders().getFirst();
        return via.getHost();

    }

    /**
     * Get the port from the topmost via header.
     * 
     * @return the port from the topmost via header (5060 if there is no port indicated).
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
        if (sipVersion == null || !sipVersion.equalsIgnoreCase("SIP/2.0"))
            throw new ParseException("sipVersion", 0);
        this.requestLine.setSipVersion(sipVersion);
    }

    /**
     * Get the SIP version.
     * 
     * @return the SIP version from the request line.
     */
    public String getSIPVersion() {
        return this.requestLine.getSipVersion();
    }

    /**
     * Book keeping method to return the current tx for the request if one exists.
     * 
     * @return the assigned tx.
     */
    public Object getTransaction() {
        // Return an opaque pointer to the transaction object.
        // This is for consistency checking and quick lookup.
        return this.transactionPointer;
    }

    /**
     * Book keeping field to set the current tx for the request.
     * 
     * @param transaction
     */
    public void setTransaction(Object transaction) {
        this.transactionPointer = transaction;
    }

    /**
     * Book keeping method to get the messasge channel for the request.
     * 
     * @return the message channel for the request.
     */

    public Object getMessageChannel() {
        // return opaque ptr to the message chanel on
        // which the message was recieved. For consistency
        // checking and lookup.
        return this.messageChannel;
    }

    /**
     * Set the message channel for the request ( bookkeeping field ).
     * 
     * @param messageChannel
     */

    public void setMessageChannel(Object messageChannel) {
        this.messageChannel = messageChannel;
    }

    /**
     * Generates an Id for checking potentially merged requests.
     * 
     * @return String to check for merged requests
     */
    public String getMergeId() {
        /*
         * generate an identifier from the From tag, Call-ID, and CSeq
         */
        String fromTag = this.getFromTag();
        String cseq = this.cSeqHeader.toString();
        String callId = this.callIdHeader.getCallId();
        /* NOTE : The RFC does NOT specify you need to include a Request URI 
         * This is added here for the case of Back to Back User Agents.
         */
        String requestUri = this.getRequestURI().toString();

        if (fromTag != null) {
            return new StringBuilder().append(requestUri).append(":").append(fromTag).append(":").append(cseq).append(":")
                    .append(callId).toString();
        } else
            return null;

    }

    /**
     * @param inviteTransaction the inviteTransaction to set
     */
    public void setInviteTransaction(Object inviteTransaction) {
        this.inviteTransaction = inviteTransaction;
    }

    /**
     * @return the inviteTransaction
     */
    public Object getInviteTransaction() {
        return inviteTransaction;
    }
    
    @Override
    public void cleanUp() {
    	super.cleanUp();
    }
}
