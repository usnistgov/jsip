/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and applicable 
 * provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. Sun, 
 * Sun Microsystems, the Sun logo, Java, Jini and JAIN are trademarks or 
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other 
 * countries.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JAIN SIP Specification
 * File Name     : TimeStampHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import javax.sip.InvalidArgumentException;


/**
 * The Timestamp header field describes when the UAC sent the request to the
 * UAS. When a 100 (Trying) response is generated, any Timestamp header field
 * present in the request MUST be copied into this 100 (Trying) response. If
 * there is a delay in generating the response, the UAS SHOULD add a delay
 * value into the Timestamp value in the response. This value MUST contain the
 * difference between the time of sending of the response and receipt of the
 * request, measured in seconds. Although there is no normative behavior
 * defined here that makes use of the header, it allows for extensions or
 * SIP applications to obtain RTT estimates, that may be used to adjust the
 * timeout value for retransmissions.
 * <p>
 * For Example:<br>
 * <code>Timestamp: 54</code>
 *
 * @version 1.1
 * @author Sun Microsystems
 */



public interface TimeStampHeader extends Header {

    /**
     * Sets the timestamp value of this TimeStampHeader to the new timestamp
     * value passed to this method.
     *
     * @param timeStamp - the new float timestamp value
     * @throws InvalidArgumentException if the timestamp value argument is a
     * negative value.
     */
    public void setTimeStamp(float timeStamp) throws InvalidArgumentException;

    /**
     * Gets the timestamp value of this TimeStampHeader.
     *
     * @return the timestamp value of this TimeStampHeader
     */
    public float getTimeStamp();

    /**
     * Gets delay of TimeStampHeader. This method returns <code>-1</code> if the
     * delay parameter is not set.
     *
     * @return the delay value of this TimeStampHeader
     */

    public float getDelay();

    /**
     * Sets the new delay value of the TimestampHeader to the delay parameter
     * passed to this method
     *
     * @param delay - the new float delay value
     * @throws InvalidArgumentException if the delay value argumenmt is a
     * negative value other than the default value <code>-1</code>.
     */

    public void setDelay(float delay) throws InvalidArgumentException;
    
    /**
     * Name of TimeStampHeader
     */
    public final static String NAME = "Timestamp";

}

