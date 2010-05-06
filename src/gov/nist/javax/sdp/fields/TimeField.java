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
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
import java.util.*;
/**
* Time Field.
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan
*
*Bug Report contributed by Brian J. Collins.
*
*/
public class TimeField extends SDPField implements Time {
    protected long startTime;
    protected long stopTime;
    public TimeField() {
        super(TIME_FIELD);
    }
    public long getStartTime() {
        return startTime;
    }
    public long getStopTime() {
        return stopTime;
    }
    /**
    * Set the startTime member
    */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    /**
    * Set the stopTime member
    */
    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    /** Returns the start time of the conference/session.
    * @throws SdpParseException
    * @return the date
    */
    public Date getStart() throws SdpParseException {
        return SdpFactory.getDateFromNtp(startTime);
    }

    /** Returns the stop time of the session
     * @throws SdpParseException
     * @return the stop time of the session.
     */
    public Date getStop() throws SdpParseException {
        return SdpFactory.getDateFromNtp(stopTime);
    }

    /** Sets the stop time of the session.
     * @param stop start - the start time
     * @throws SdpException if the date is null
     */
    public void setStop(Date stop) throws SdpException {
        if (stop == null)
            throw new SdpException("The date is null");
        else {
            this.stopTime = SdpFactory.getNtpTime(stop);
        }
    }

    /** Sets the start time of the conference/session.
     * @param start start - the start time for the session.
     * @throws SdpException if the date is null
     */
    public void setStart(Date start) throws SdpException {
        if (start == null)
            throw new SdpException("The date is null");
        else {
            this.startTime = SdpFactory.getNtpTime(start);
        }
    }

    /** Returns whether the field will be output as a typed time
     * or a integer value.
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
     * @return true, if the field will be output as a
     * typed time; false, if as an integer value.
     */
    public boolean getTypedTime() {
        return false;
    }

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
    public void setTypedTime(boolean typedTime) {

    }

    /** Returns whether the start and stop times were set to zero (in NTP).
     * @return boolean
     */
    public boolean isZero() {
        return getStartTime()==0 && getStopTime()==0;
    }

    /** Sets the start and stop times to zero (in NTP).
     */
    public void setZero() {
        setStopTime(0);
        setStartTime(0);
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        return new StringBuilder()
            .append(TIME_FIELD)
            .append(startTime)
            .append(Separators.SP)
            .append(stopTime)
            .append(Separators.NEWLINE)
            .toString();
    }

}

