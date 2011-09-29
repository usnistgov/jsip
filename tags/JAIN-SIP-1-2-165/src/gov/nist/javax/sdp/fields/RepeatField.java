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
import java.util.LinkedList;
import java.util.ListIterator;
import javax.sdp.*;
/**
* Repeat SDP Field (part of the time field).
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/
public class RepeatField extends SDPField implements javax.sdp.RepeatTime {

    private static final long serialVersionUID = -6415338212212641819L;
    protected TypedTime repeatInterval;
    protected TypedTime activeDuration;
    protected SDPObjectList offsets;

    public RepeatField() {
        super(REPEAT_FIELD);
        offsets = new SDPObjectList();
    }

    public void setRepeatInterval(TypedTime interval) {
        repeatInterval = interval;
    }

    public void setActiveDuration(TypedTime duration) {
        activeDuration = duration;
    }

    public void addOffset(TypedTime offset) {
        offsets.add(offset);
    }

    public LinkedList getOffsets() {
        return offsets;
    }

    /** Returns the "repeat interval" in seconds.
    * @throws SdpParseException
    * @return the "repeat interval" in seconds.
    */
    public int getRepeatInterval() throws SdpParseException {
        if (repeatInterval == null)
            return -1;
        else {
            return repeatInterval.getTime();
        }
    }

    /** Set the "repeat interval" in seconds.
     * @param repeatInterval the "repeat interval" in seconds.
     * @throws SdpException if repeatInterval is <0
     */
    public void setRepeatInterval(int repeatInterval) throws SdpException {
        if (repeatInterval < 0)
            throw new SdpException("The repeat interval is <0");
        else {
            if (this.repeatInterval == null)
                this.repeatInterval = new TypedTime();
            this.repeatInterval.setTime(repeatInterval);
        }
    }

    /** Returns the "active duration" in seconds.
     * @throws SdpParseException
     * @return the "active duration" in seconds.
     */
    public int getActiveDuration() throws SdpParseException {
        if (activeDuration == null)
            return -1;
        else {
            return activeDuration.getTime();
        }
    }

    /** Sets the "active duration" in seconds.
     * @param activeDuration the "active duration" in seconds.
     * @throws SdpException if the active duration is <0
     */
    public void setActiveDuration(int activeDuration) throws SdpException {
        if (activeDuration < 0)
            throw new SdpException("The active Duration is <0");
        else {
            if (this.activeDuration == null)
                this.activeDuration = new TypedTime();
            this.activeDuration.setTime(activeDuration);
        }
    }

    /** Returns the list of offsets. These are relative to the start-time given
     * in the Time object (t=
     *     field) with which this RepeatTime is associated.
     * @throws SdpParseException
     * @return the list of offsets
     */
    public int[] getOffsetArray() throws SdpParseException {
        LinkedList linkedList = getOffsets();
        int[] result = new int[linkedList.size()];
        for (int i = 0; i < linkedList.size(); i++) {
            TypedTime typedTime = (TypedTime) linkedList.get(i);
            result[i] = typedTime.getTime();
        }
        return result;
    }

    /** Set the list of offsets. These are relative to the start-time given in the
     * Time object (t=
     *     field) with which this RepeatTime is associated.
     * @param offsets array of repeat time offsets
     * @throws SdpException
     */
    public void setOffsetArray(int[] offsets) throws SdpException {
        for (int i = 0; i < offsets.length; i++) {
            TypedTime typedTime = new TypedTime();
            typedTime.setTime(offsets[i]);
            addOffset(typedTime);
        }

    }

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
    public boolean getTypedTime() throws SdpParseException {
        return true;
    }

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
    public void setTypedTime(boolean typedTime) {

    }

    public String encode() {
        StringBuilder retval = new StringBuilder();
    retval.append(REPEAT_FIELD)
      .append(repeatInterval.encode())
            .append(Separators.SP)
          .append(activeDuration.encode());
        ListIterator li = offsets.listIterator();
        while (li.hasNext()) {
            TypedTime off = (TypedTime) li.next();
            retval.append (Separators.SP).append (off.encode());
        }
        retval.append (Separators.NEWLINE);
        return retval.toString ();
    }

    public Object clone() {
        RepeatField retval = (RepeatField) super.clone();
        if (this.repeatInterval != null)
            retval.repeatInterval = (TypedTime) this.repeatInterval.clone();
        if (this.activeDuration != null)
            retval.activeDuration = (TypedTime) this.activeDuration.clone();
        if (this.offsets != null)
            retval.offsets = (SDPObjectList) this.offsets.clone();
        return retval;
    }

}

