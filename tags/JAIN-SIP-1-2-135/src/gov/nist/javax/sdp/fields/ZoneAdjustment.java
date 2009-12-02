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

/**
* Zone adjustment class.
*
*@version 1.2
*
*@author M. Ranganathan   <br/>
*
*
*
*/
public class ZoneAdjustment extends SDPObject {
    protected long time;
    protected String sign;
    protected TypedTime offset;

    /**
    * Set the time.
    *@param t time to set.
    */
    public void setTime(long t) {
        time = t;
    }

    /**
    * Get the time.
    */
    public long getTime() {
        return time;
    }

    /**
    * get the offset.
    */
    public TypedTime getOffset() {
        return offset;
    }

    /**
    * Set the offset.
    *@param off typed time offset to set.
    */
    public void setOffset(TypedTime off) {
        offset = off;
    }

    /**
    * Set the sign.
    *@param s sign for the offset.
    */
    public void setSign(String s) {
        sign = s;
    }

    /**
    * Encode this structure into canonical form.
    *@return encoded form of the header.
    */
    public String encode() {
        String retval = Long.toString(time);
        retval += Separators.SP;
        if (sign != null)
            retval += sign;
        retval += offset.encode();
        return retval;
    }

    public Object clone() {
        ZoneAdjustment retval = (ZoneAdjustment) super.clone();
        if (this.offset != null)
            retval.offset = (TypedTime) this.offset.clone();
        return retval;
    }

}
