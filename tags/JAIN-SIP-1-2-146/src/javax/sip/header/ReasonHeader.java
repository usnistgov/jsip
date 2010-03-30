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
 * File Name     : ReasonHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version, optional header to
 *                                          support RFC3326.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;

/**
 * This interface represents the Reason header, as defined by
 * <a href = "http://www.ietf.org/rfc/rfc3326.txt">RFC3326</a>, this header is
 * not part of RFC3261.
 * <p>
 * The ReasonHeader provides information on why a SIP request was issued, often
 * useful when creating services and also used to encapsulate a final status code in
 * a provisional response, which is needed to resolve the "Heterogeneous Error
 * Response Forking Problem".
 * <p>
 * The Reason header field appears to be most useful for BYE and CANCEL requests,
 * but it can appear in any request within a dialog, in any CANCEL request and
 * in 155 (Update Requested) responses. When used in requests, clients and
 * servers are free to ignore this header field. It has no impact on protocol
 * processing.
 * <p>
 * Examples of the ReasonHeader usage are:
 * <ul>
 * <li> A SIP CANCEL request can be issued if the call has completed on another
 * branch or was abandoned before answer. While the protocol and system behavior
 * is the same in both cases, namely, alerting will cease, the user interface
 * may well differ. In the second case, the call may be logged as a missed call,
 * while this would not be appropriate if the call was picked up elsewhere.
 * <li> Third party call controllers sometimes generate a SIP request upon
 * reception of a SIP response from another dialog. Gateways generate SIP
 * requests after receiving messages from a different protocol than SIP. In
 * both cases the client may be interested in knowing what triggered the SIP
 * request.
 * <li> An INVITE can sometimes be rejected not because the session initiation
 * was declined, but because some aspect of the request was not acceptable. If
 * the INVITE forked and resulted in a rejection, the error response may never
 * be forwarded to the client unless all the other branches also reject the
 * request. This problem is known as the "Heterogeneous Error Response Forking
 * Problem". The header field defined in this draft allows encapsulating the
 * final error response in a 155 (Update Requested) provisional response.
 * </ul>
 * A server must ignore Headers that it does not understand. A proxy must not
 * remove or modify Headers that it does not understand.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface ReasonHeader extends Parameters, Header {

    /**
     * Gets the cause value of the ReasonHeader
     *
     * @return the integer value of the cause of the ReasonHeader
     */
    public int getCause();

     /**
      * Sets the cause value of the ReasonHeader. Any SIP status code MAY
      * appear in the Reason header field of a request, assuming the protocol
      * field of the ReasonHeader is SIP.
      *
      * @param cause - the new integer value of the cause of the ReasonHeader
      * @throws InvalidArgumentException if the cause value is less than zero.
      */
     public void setCause(int cause) throws InvalidArgumentException;


     /**
      * Sets the protocol of the ReasonHeader, for example SIP or Q.850.
      *
      * @param protocol - the new string value of the protocol of the ReasonHeader
      * @throws ParseException which signals that an error has been reached
      * unexpectedly while parsing the protocol value.
      */
     public void setProtocol(String protocol) throws ParseException;


     /**
     * Gets the protocol value of the ReasonHeader
     *
     * @return the string value of the protocol of the ReasonHeader
      */
      public String getProtocol();

     /**
      * Sets the text value of the ReasonHeader.
      *
      * @param text - the new string value of the text of the ReasonHeader
      * @throws ParseException which signals that an error has been reached
      * unexpectedly while parsing the text value.
      */
      public void setText(String text) throws ParseException;

     /**
      * Gets the text value of the ReasonHeader
      *
      * @return the string value of the text of the ReasonHeader
      */
      public String getText();

    /**
     * Name of ReasonHeader
     */
    public final static String NAME = "Reason";

}

