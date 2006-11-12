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
 * @author BEA Systems, NIST
 * @version 1.2
 */



public interface TimeStampHeader extends Header {

     /**
     * Sets the timestamp value of this TimeStampHeader to the new timestamp
     * value passed to this method.
     *
     * @param timeStamp - the new float timestamp value
     * @throws InvalidArgumentException if the timestamp value argument is a
     * negative value.
     * @deprecated This method is replaced with {@link #setTimeStamp(float)}.
     */
    public void setTimeStamp(float timeStamp) throws InvalidArgumentException;
    
    /**
     * Gets the timestamp value of this TimeStampHeader.
     *
     * @return the timestamp value of this TimeStampHeader
     * @deprecated This method is replaced with {@link #getTime()}.
     */
    public float getTimeStamp();
    
    /**
     * Gets the timestamp value of this TimeStampHeader.
     * 
     * @since v1.2
     *
     * @return the timestamp value of this TimeStampHeader
     */
    public long getTime();

    /**
     *  Sets the timestamp value of this TimeStampHeader to the new timestamp
     * value passed to this method. This method allows applications to conveniantly
     * use System.currentTimeMillis to set the timeStamp value.
     * 
     * @since v1.2
     *
     * @param timeStamp - the new long timestamp value
     * @throws InvalidArgumentException if the timestamp value argument is a
     * negative value. 
     */
    public void setTime(long timeStamp) throws InvalidArgumentException;    
    
    
    
    /**
     * Gets delay of TimeStampHeader. This method returns <code>-1</code> if the
     * delay parameter is not set.
     * 
     * @return the delay value of this TimeStampHeader
     * @deprecated This method is replaced with {@link #getTimeDelay()}.
     */

    public float getDelay();    

    /**
     * Sets the new delay value of the TimestampHeader to the delay parameter
     * passed to this method
     *
     * @param delay - the new float delay value
     * @throws InvalidArgumentException if the delay value argumenmt is a
     * negative value other than the default value <code>-1</code>.
     * @deprecated This method is replaced with {@link #setTimeDelay(int)}.
     */

    public void setDelay(float delay) throws InvalidArgumentException;

    /**
     * Gets delay of TimeStampHeader. This method returns <code>-1</code> if the
     * delay parameter is not set.
     * 
     * @since v1.2
     * @return the delay value of this TimeStampHeader as an integer.
     */

    public int getTimeDelay();    
    
    /**
     * Sets the new delay value of the TimestampHeader to the delay parameter
     * passed to this method
     *
     * @since v1.2
     * @param delay - the new int delay value
     * @throws InvalidArgumentException if the delay value argumenmt is a
     * negative value other than the default value <code>-1</code>.
     */

    public void setTimeDelay(int delay) throws InvalidArgumentException;
    
   
    /**
     * Name of TimeStampHeader
     */
    public final static String NAME = "Timestamp";
}

