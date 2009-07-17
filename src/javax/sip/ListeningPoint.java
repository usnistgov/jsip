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
 * File Name     : ListeningPoint.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import java.io.*;
import java.text.ParseException;

/**
 * This interface represents a unique IP network listening point,
 * which consists of port transport and IP. A ListeningPoint is a Java
 * representation of the socket that a SipProvider messaging entity uses to send
 * and receive messages.
 * <p>
 * The ListeningPoint also includes an optional sent-by string parameter.
 * If set, this string will be placed in the sent-by parameter of the
 * top most Via header of outgoing requests.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface ListeningPoint extends Cloneable, Serializable {


    /**
     * Gets the port of the ListeningPoint. The default port of a ListeningPoint
     * is dependent on the scheme and transport.  For example:
     * <ul>
     * <li>The default port is 5060, if the transport UDP and the scheme is
     * <i>sip:</i>.
     * <li>The default port is 5060, if the transport is TCP and the scheme
     * is <i>sip:</i>.
     * <li>The default port is 5060, if the transport is SCTP and the scheme
     * is <i>sip:</i>.
     * <li>The default port is 5061, if the transport is TLS over TCP and the
     * scheme is <i>sip:</i>.
     * <li>The default port is 5061, if the transport is TCP and the scheme
     * is <i>sips:</i>.
     * </ul>
     *
     * @return the integer value of the port.
     */
    public int getPort();

    /**
     * Gets the transport of the ListeningPoint.
     *
     * @return the string value of the transport.
     */
    public String getTransport();

    /**
     * Gets the IP of the ListeningPoint.
     *
     * @since v1.2
     * @return the string value of the IP address.
     */
    public String getIPAddress();

    /**
     * Sets the sentBy string for this ListeningPoint. The sentBy String is
     * placed in the top most Via header of outgoing requests. This parameter
     * is optional and if it is not set, the top most Via header will use the
     * IP address and port assigned to the listening point for the sentBy field.
     *
     * @param sentBy the sentBy string to be set in requests top most Via
     * headers.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the sentBy value.
     * @since v1.2
     */
    public void setSentBy(String sentBy) throws ParseException;

    /**
     * Gets the sentBy attribute of the ListeningPoint.
     *
     * @return the string value of the sentBy attribute.
     * @since v1.2
     */
    public String getSentBy();

    /**
     * This method indicates whether the specified object is equal to this
     * Listening Point. The specified object is equal to this ListeningPoint
     * if the specified object is a ListeningPoint and the transport and port
     * in the specified Listening Point is the same as this Listening Point.
     *
     * @param obj the object with which to compare this ListeningPoint.
     * @return true if this ListeningPoint is "equal to" the obj argument;
     * false otherwise.
     */
    public boolean equals(Object obj);


//Constants

    /**
     * Transport constant: TCP
     */
    public static final String TCP = "TCP";

    /**
     * Transport constant: UDP
     */
    public static final String UDP = "UDP";

    /**
     * Transport constant: SCTP
     *
     */
    public static final String SCTP = "SCTP";

    /**
     * Transport constant: TLS over TCP
     */
    public static final String TLS = "TLS";

    /**
     * Port Constant: Default port 5060. This constant should only be used
     * when the transport of the ListeningPoint is set to UDP, TCP or SCTP.
     */
    public static final int PORT_5060 = 5060;

    /**
     * Port Constant: Default port 5061. This constant should only be used
     * when the transport of the Listening Point is set to TLS over TCP or TCP
     * assuming the scheme is "sips".
     */
    public static final int PORT_5061 = 5061;

}

