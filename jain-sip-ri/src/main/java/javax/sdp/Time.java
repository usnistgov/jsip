/*
 * Time.java
 *
 * Created on January 9, 2002, 10:53 AM
 */

package javax.sdp;

import java.util.*;

/** A RepeatTime represents a t= field contained within a TimeDescription.
 *
 * A RepeatTime specifies the start and stop times for a SessionDescription.
 *
 * Note: this class uses java.util.Date objects. SDP messages encode time in NTP
 * format.
 *
 * To convert between them use SdpFactory.getDateFromNtp(long) and 
 * SdpFactory.getNtpTime(Date).
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
public interface Time extends Field {

    /** Returns the start time of the conference/session.
     * @throws SdpParseException
     * @return the date
     */    
    public Date getStart()
              throws SdpParseException;
    
    /** Sets the start time of the conference/session.
     * @param start start - the start time for the session.
     * @throws SdpException if the date is null
     */    
    public void setStart(Date start)
              throws SdpException;
    
    /** Returns the stop time of the session
     * @throws SdpParseException
     * @return the stop time of the session.
     */    
    public Date getStop()
             throws SdpParseException;
    
    /** Sets the stop time of the session.
     * @param stop start - the start time
     * @throws SdpException if the date is null
     */    
    public void setStop(Date stop)
             throws SdpException;
    
    /** Returns whether the start and stop times were set to zero (in NTP).
     * @return boolean
     */    
    public boolean isZero();
    
    /** Sets the start and stop times to zero (in NTP).
     */    
    public void setZero();
    
    /** Returns whether the field will be output as a typed time or a integer value.
     *
     *     Typed time is formatted as an integer followed by a unit character.
     * The unit indicates an appropriate multiplier for
     *     the integer.
     *
     *     The following unit types are allowed.
     *          d - days (86400 seconds)
     *          h - hours (3600 seconds)
     *          m - minutes (60 seconds)
     *          s - seconds ( 1 seconds)
     * @return true, if the field will be output as a typed time; false, if as an
     * integer value.
     */    
    public boolean getTypedTime();
    
    /** Sets whether the field will be output as a typed time or a integer value.
     *
     *     Typed time is formatted as an integer followed by a unit character. 
     * The unit indicates an appropriate multiplier for
     *     the integer.
     *
     *     The following unit types are allowed.
     *          d - days (86400 seconds)
     *          h - hours (3600 seconds)
     *          m - minutes (60 seconds)
     *          s - seconds ( 1 seconds)
     * @param typedTime typedTime - if set true, the start and stop times will
     * be output in an optimal typed time format; if false, the
     *          times will be output as integers.
     */    
    public void setTypedTime(boolean typedTime);
    
}

