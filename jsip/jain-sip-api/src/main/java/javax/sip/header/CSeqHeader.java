/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. 
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : CSeqHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty
 *  1.2     11/06/2006  Phelim O'Doherty    Changed Cseq to type long
 *    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.InvalidArgumentException;
import java.text.ParseException;

/**
 * A CSeq header field in a request contains a single decimal sequence number
 * and the request method. The CSeq header field serves to identify and order
 * transactions within a dialog, to provide a means to uniquely identify
 * transactions, and to differentiate between new requests and request
 * retransmissions.  Two CSeq header fields are considered equal if the
 * sequence number and the request method are identical.
 * <ul>
 * <li>Method - The method part of CSeq is case-sensitive and MUST match that
 * of the request.
 * <li>Sequence number - The sequence number is chosen by the requesting
 * client and is unique within a single value of Call-ID. The sequence number
 * MUST be expressible as a 32-bit unsigned integer and MUST be less than
 * 2**31. For non-REGISTER requests outside of a dialog, the sequence number
 * value is arbitrary. Consecutive Requests that differ in method, headers or
 * body, but have the same CallIdHeader must contain strictly monotonically
 * increasing and contiguous sequence numbers; sequence numbers do not wrap
 * around. Retransmissions of the same Request carry the same sequence number,
 * but an INVITE Request with a different message body or different headers
 * (a "re-invitation") acquires a new, higher sequence number. A server must
 * echo the CSeqHeader from the Request in its Response. If the method value is
 * missing in the received CSeqHeader, the server fills it in appropriately.
 * ACK and CANCEL Requests must contain the same CSeqHeader sequence number
 * (but not method) as the INVITE Request they refer to, while a BYE Request
 * cancelling an invitation must have a higher sequence number. An user agent
 * server must remember the highest sequence number for any INVITE Request
 * with the same CallIdHeader. The server must respond to, and then discard,
 * any INVITE Request with a lower sequence number.
 * </ul>
 * As long as a client follows the above guidelines, it may use any mechanism
 * it would like to select CSeq header field values.
 * <p>
 * <b>Forked Requests:</b><br>
 * Forked Requests must have the same CSeqHeader as there would be ambiguity
 * otherwise between these forked Requests and later BYE Requests issued by the
 * client user agent.
 * <p>
 * For Example:<br>
 * <code>CSeq: 4711 INVITE</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 *
 */

public interface CSeqHeader extends Header {

    /**
     * Sets the method of CSeqHeader
     *
     * @param method - the method of the Request of this CSeqHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     */
    public void setMethod(String method) throws ParseException;

    /**
     * Gets the method of CSeqHeader
     *
     * @return method of CSeqHeader
     */
    public String getMethod();

    /**
     * Sets the sequence number value of the CSeqHeader. The sequence number
     * MUST be expressible as a 32-bit unsigned integer and MUST be less than
     * 2**32 -1
     *
     * @param sequenceNumber - the new sequence number of this CSeqHeader
     * @throws InvalidArgumentException if supplied value is less than zero.
     * @since v1.1
     * 
     * @deprecated This method is replaced with {@link #setSeqNumber(long)} with 
     * type long. 
     */
    public void setSequenceNumber( int sequenceNumber) 
                                        throws InvalidArgumentException;
    
    /**
     * Gets the sequence number of this CSeqHeader.
     *
     * @deprecated This method is replaced with {@link #getSeqNumber()} with 
     * type long.
     * @return sequence number of the CSeqHeader
     * @since v1.1
     */
    public int getSequenceNumber();

    /**
     * Sets the sequence number value of the CSeqHeader. 
     *
     * @param sequenceNumber - the new sequence number of this CSeqHeader
     * @throws InvalidArgumentException if supplied value is less than zero.
     * @since v1.2
     */
    public void setSeqNumber(long sequenceNumber) 
                                        throws InvalidArgumentException;    
    
    /**
     * Gets the sequence number of this CSeqHeader.
     *
     * @return sequence number of the CSeqHeader
     * @since v1.2
     */
    public long getSeqNumber();

    
    /**
     * Compare this CSeqHeader for equality with another. This method 
     * overrides the equals method in javax.sip.Header. This method specifies 
     * object equality as outlined by  
     * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>. 
     * Two CSeq header fields are considered equal if the sequence number and 
     * the request method are identical. When comparing header fields, field names 
     * are always case-insensitive. Unless otherwise stated in the
     * definition of a particular header field, field values, parameter names,
     * and parameter values are case-insensitive. Tokens are always
     * case-insensitive. Unless specified otherwise, values expressed as quoted
     * strings are case-sensitive.
     *
     * @param obj the object to compare this CSeqHeader with.
     * @return <code>true</code> if <code>obj</code> is an instance of this class
     * representing the same CseqHeader as this, <code>false</code> otherwise.
     * @since v1.2
     */
    public boolean equals(Object obj);    
    
    
     /**
     * Name of the CSeqHeader
     */
    public final static String NAME = "CSeq";

}

