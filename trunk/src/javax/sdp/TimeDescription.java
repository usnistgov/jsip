/*
 * TimeDescription.java
 *
 * Created on January 9, 2002, 11:13 AM
 */

package javax.sdp;

import java.io.*;
import java.util.*;

/** A TimeDescription represents the fields present within a SDP time description.
 *
 * Quoting from RFC 2327:
 *
 *     Multiple "t=" fields may be used if a session is active at multiple 
 *     irregularly spaced times; each additional
 *     "t=" field specifies an additional period of time for which the session 
 *     will be active. If the session is active at
 *     regular times, an "r=" field (see below) should be used in addition to and
 *     following a "t=" field - in which
 *     case the "t=" field specifies the start and stop times of the repeat sequence.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface TimeDescription extends Serializable, Cloneable {

/** Constant used to translate between NTP time used in SDP and "native" Java time.
 * NTP time is defined as the
 *     number of seconds relative to midnight, January 1, 1900 and Java time is 
 * measured in number of milliseconds
 *     since midnight, January 1, 1970 UTC (see System#currentTimeMillis()}).
 *
 *     The value of this constant is 2208988800L. It can be used to convert between
 * NTP and Java time using the
 *     following formulas:
 *
 *        ntpTime = javaTime/1000 * SdpConstants.NTP_CONST;
 *        javaTime = (ntpTime - SdpConstants.NTP_CONST) * 1000;
 *
 *
 *     The Network Time Protocol (NTP) is defined in RFC 1305.
 */    
    public static final long NTP_CONST=2208988800L;
    
    /** Returns the Time field.
     * @return Time
     */    
    public Time getTime()throws SdpParseException;
    
    /** Sets the Time field.
     * @param t Time to set
     * @throws SdpException if the time is null
     */    
    public void setTime(Time t)
             throws SdpException;
    
    /** Returns the list of repeat times (r= fields) specified in the SessionDescription.
     * @param create boolean to set
     * @return Vector
     */    
    public Vector getRepeatTimes(boolean create);
    
    /** Returns the list of repeat times (r= fields) specified in the SessionDescription.
     * @param repeatTimes Vector to set
     * @throws SdpException if the parameter is null
     */    
    public void setRepeatTimes(Vector repeatTimes)
                    throws SdpException;
    
}

