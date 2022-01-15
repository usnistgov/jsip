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
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.LinkedList;


/**
 * SIP Response structure.
 *
 * @version 1.2 $Revision: 1.35 $ $Date: 2010-09-14 14:39:39 $
 * @since 1.1
 *
 * @author M. Ranganathan   <br/>
 *
 *
 */
public class SIPResponse
    extends SIPMessage
    implements javax.sip.message.Response, ResponseExt {
    protected StatusLine statusLine;
    private boolean isRetransmission = true;
   
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

            //Issue 168, Typo fix reported by fre on the retval
            case NOT_ACCEPTABLE_HERE :
                retval = "Not Acceptable here";
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

            case CONDITIONAL_REQUEST_FAILED:
                retval = "Conditional request failed";
                break;

            default :
                retval = "Unknown Status";

        }
        return retval;

    }

    /** set the status code.
     *@param statusCode is the status code to set.
     *@throws IlegalArgumentException if invalid status code.
     */
    public void setStatusCode(int statusCode) throws ParseException {

      // RFC3261 defines statuscode as 3DIGIT, 606 is the highest officially
      // defined code but extensions may add others (in theory up to 999,
      // but in practice up to 699 since the 6xx range is defined as 'final error')
        if (statusCode < 100 || statusCode > 699)
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
        sprint(SIPResponse.class.getCanonicalName());
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
    public void checkHeaders() throws ParseException {
        if (getCSeq() == null) {
            throw new ParseException(CSeq.NAME+ " Is missing ", 0);
        }
        if (getTo() == null) {
            throw new ParseException(To.NAME+ " Is missing ", 0);
        }
        if (getFrom() == null) {
            throw new ParseException(From.NAME+ " Is missing ", 0);
        }
        if (getViaHeaders() == null) {
            throw new ParseException(Via.NAME+ " Is missing ", 0);
        }
        if (getCallId() == null) {
            throw new ParseException(CallID.NAME + " Is missing ", 0);
        }


        if (getStatusCode() > 699) {
            throw new ParseException("Unknown error code!" + getStatusCode(), 0);
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

    /** Encode the message except for the body.
    *
    *@return The string except for the body.
    */

    public StringBuilder encodeMessage(StringBuilder retval) {
//        String retval;
        if (statusLine != null) {
            statusLine.encode(retval);
            super.encodeSIPHeaders(retval);
        } else {
            retval = super.encodeSIPHeaders(retval);
        }
        return retval;
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
        if (this.statusLine != null)
            retval.statusLine = (StatusLine) this.statusLine.clone();
        return retval;
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

        StatusLine rline = that.statusLine;
        if (this.statusLine == null && rline != null)
            return false;
        else if (this.statusLine == rline)
            return super.match(matchObj);
        else {

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

    public byte[] encodeAsBytes( String transport ) {
        byte[] slbytes = null;
        if (statusLine != null) {
            try {
                slbytes = statusLine.encode().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
        byte[] superbytes = super.encodeAsBytes( transport );
        byte[] retval = new byte[slbytes.length + superbytes.length];
        System.arraycopy(slbytes, 0, retval, 0, slbytes.length);
        System.arraycopy(superbytes, 0, retval, slbytes.length,
                superbytes.length);
        return retval;
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
        if (statusLine == null) return  "";
        else return statusLine.encode() + super.encode();
    }
    
    @Override
    public void cleanUp() {
//    	statusLine = null;
    	super.cleanUp();
    }

    /**
     * @param isRetransmission the isRetransmission to set
     */
    public void setRetransmission(boolean isRetransmission) {
        
        this.isRetransmission = isRetransmission;
    }

    /**
     * @return the isRetransmission
     */
    public boolean isRetransmission() {
        return isRetransmission;
    }
}
