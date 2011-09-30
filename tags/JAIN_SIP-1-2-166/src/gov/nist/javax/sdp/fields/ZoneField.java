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
import java.util.*;
import javax.sdp.*;

/**
* Z= SDP field.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/

public class ZoneField
    extends SDPField
    implements javax.sdp.TimeZoneAdjustment {

    protected SDPObjectList zoneAdjustments;

    /**
    * Constructor.
    */
    public ZoneField() {
        super(ZONE_FIELD);
        zoneAdjustments = new SDPObjectList();
    }

    /**
    * Add an element to the zone adjustment list.
    *@param za zone adjustment to add.
    */
    public void addZoneAdjustment(ZoneAdjustment za) {
        zoneAdjustments.add(za);
    }

    /**
    * Get the zone adjustment list.
    *@return the list of zone adjustments.
    */

    public SDPObjectList getZoneAdjustments() {
        return zoneAdjustments;
    }

    /**
    * Encode this structure into a canonical form.
    */
    public String encode() {
        StringBuilder retval = new StringBuilder(ZONE_FIELD);
        ListIterator li = zoneAdjustments.listIterator();
        int num = 0;
        while (li.hasNext()) {
            ZoneAdjustment za = (ZoneAdjustment) li.next();
            if (num > 0)
                retval.append(Separators.SP);
            retval.append(za.encode());
            num++;
        }
        retval.append(Separators.NEWLINE);
        return retval.toString();
    }

    /** Returns a Hashtable of adjustment times, where:
     *        key = Date. This is the equivalent of the decimal NTP time value.
     *        value = Int Adjustment. This is a relative time value in seconds.
     * @param create to set
     * @throws SdpParseException
     * @return create - when true, an empty Hashtable is created, if it is null.
     */
    public Hashtable getZoneAdjustments(boolean create)
        throws SdpParseException {
        Hashtable result = new Hashtable();
        SDPObjectList zoneAdjustments = getZoneAdjustments();
        ZoneAdjustment zone;
        if (zoneAdjustments == null)
            if (create)
                return new Hashtable();
            else
                return null;
        else {
            while ((zone = (ZoneAdjustment) zoneAdjustments.next()) != null) {
                Long l = Long.valueOf(zone.getTime());
                Integer time = Integer.valueOf(l.toString());
                Date date = new Date(zone.getTime());
                result.put(date, time);
            }
            return result;
        }
    }

    /** Sets the Hashtable of adjustment times, where:
     *          key = Date. This is the equivalent of the decimal NTP time value.
     *          value = Int Adjustment. This is a relative time value in seconds.
     * @param map Hashtable to set
     * @throws SdpException if the parameter is null
     */
    public void setZoneAdjustments(Hashtable map) throws SdpException {
        if (map == null)
            throw new SdpException("The map is null");
        else {
            for (Enumeration e = map.keys(); e.hasMoreElements();) {
                Object o = e.nextElement();
                if (o instanceof Date) {
                    Date date = (Date) o;
                    ZoneAdjustment zone = new ZoneAdjustment();
                    zone.setTime(date.getTime());
                    addZoneAdjustment(zone);
                } else
                    throw new SdpException("The map is not well-formated ");
            }
        }
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
     * @param typedTime typedTime - if set true, the start and stop times will be
     * output in an optimal typed time format; if false, the
     *          times will be output as integers.
     */
    public void setTypedTime(boolean typedTime) {
        // Dummy -- feature not implemented.
    }

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
     * @return true, if the field will be output as a typed time; false, if as an integer value.
     */
    public boolean getTypedTime() {
        return false;
    }

    public Object clone() {
        ZoneField retval = (ZoneField) super.clone();
        if (this.zoneAdjustments != null)
            retval.zoneAdjustments = (SDPObjectList) this.zoneAdjustments.clone();
        return retval;
    }
}
