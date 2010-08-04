/*
 * RepeatTime.java
 *
 * Created on December 20, 2001, 3:09 PM
 */

package javax.sdp;

/** A RepeatTime represents a r= field contained within a TimeDescription.
 *
 * A RepeatTime specifies the repeat times for a SessionDescription.
 *
 * This consists of a "repeat interval", an "active duration", and a list of offsets relative to the t=
 * start-time (see Time.getStart()).
 *
 * Quoting from RFC 2327:
 *
 *     For example, if a session is active at 10am on Monday and 11am on Tuesday for
 *     one hour each week for three months, then the <start time> in the corresponding
 *     "t=" field would be the NTP representation of 10am on the first Monday, the
 *     <repeat interval> would be 1 week, the <active duration> would be 1 hour, and
 *     the offsets would be zero and 25 hours. The corresponding "t=" field stop time
 *     would be the NTP representation of the end of the last session three months later.
 *     By default all fields are in seconds, so the "r=" and "t=" fields might be:
 *
 *           t=3034423619 3042462419
 *           r=604800 3600 0 90000
 *
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface RepeatTime extends Field {

    /** Returns the "repeat interval" in seconds.
     * @throws SdpParseException
     * @return the "repeat interval" in seconds.
     */    
    public int getRepeatInterval()
                      throws SdpParseException;
    
    /** Set the "repeat interval" in seconds.
     * @param repeatInterval the "repeat interval" in seconds.
     * @throws SdpException if repeatInterval is <0
     */    
    public void setRepeatInterval(int repeatInterval)
                       throws SdpException;
    
    /** Returns the "active duration" in seconds.
     * @throws SdpParseException
     * @return the "active duration" in seconds.
     */    
    public int getActiveDuration()
                      throws SdpParseException;
    
    /** Sets the "active duration" in seconds.
     * @param activeDuration the "active duration" in seconds.
     * @throws SdpException if the active duration is <0
     */    
    public void setActiveDuration(int activeDuration)
                       throws SdpException;
    
    /** Returns the list of offsets. These are relative to the start-time given
     * in the Time object (t=
     *     field) with which this RepeatTime is associated.
     * @throws SdpParseException
     * @return the list of offsets
     */    
    public int[] getOffsetArray()
                     throws SdpParseException;
    
    /** Set the list of offsets. These are relative to the start-time given in the 
     * Time object (t=
     *     field) with which this RepeatTime is associated.
     * @param offsets array of repeat time offsets
     * @throws SdpException
     */    
    public void setOffsetArray(int[] offsets)
                    throws SdpException;
    
/** Returns whether the field will be output as a typed time or a integer value.
 *
 *     Typed time is formatted as an integer followed by a unit character. The unit indicates an
 *     appropriate multiplier for the integer.
 *
 *     The following unit types are allowed.
 *          d - days (86400 seconds)
 *          h - hours (3600 seconds)
 *          m - minutes (60 seconds)
 *          s - seconds ( 1 seconds)
 * @throws SdpParseException
 * @return true, if the field will be output as a typed time; false, if as an integer value.
 */    
    public boolean getTypedTime()
                     throws SdpParseException;
    
/** Sets whether the field will be output as a typed time or a integer value.
 *
 *     Typed time is formatted as an integer followed by a unit character. The unit indicates an
 *     appropriate multiplier for the integer.
 *
 *     The following unit types are allowed.
 *          d - days (86400 seconds)
 *          h - hours (3600 seconds)
 *          m - minutes (60 seconds)
 *          s - seconds ( 1 seconds)
 * @param typedTime typedTime - if set true, the start and stop times will be output in an optimal typed
 *          time format; if false, the times will be output as integers.
 */    
    public void setTypedTime(boolean typedTime);
}

