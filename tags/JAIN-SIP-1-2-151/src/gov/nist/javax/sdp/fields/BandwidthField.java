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
/**
* Bandwidth field of a SDP header.
*
*@version  JSR141-PUBLIC-REVIEW (Subject to change)
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/

public class BandwidthField extends SDPField implements javax.sdp.BandWidth {
    protected String bwtype;
    protected int bandwidth;
    public BandwidthField() {
        super(SDPFieldNames.BANDWIDTH_FIELD);
    }
    public String getBwtype() {
        return bwtype;
    }
    public int getBandwidth() {
        return bandwidth;
    }
    /**
    * Set the bwtype member
    */
    public void setBwtype(String b) {
        bwtype = b;
    }
    /**
    * Set the bandwidth member
    */
    public void setBandwidth(int b) {
        bandwidth = b;
    }

    /**
    *  Get the string encoded version of this object
    * @since v1.0
    */
    public String encode() {
        String encoded_string = BANDWIDTH_FIELD;

        if (bwtype != null)
            encoded_string += bwtype + Separators.COLON;
        return encoded_string + bandwidth + Separators.NEWLINE;
    }

    /** Returns the bandwidth type.
     * @throws SdpParseException
     * @return type
     */
    public String getType() throws SdpParseException {
        return getBwtype();
    }

    /** Sets the bandwidth type.
     * @param type to set
     * @throws SdpException if the type is null
     */
    public void setType(String type) throws SdpException {
        if (type == null)
            throw new SdpException("The type is null");
        else
            setBwtype(type);
    }

    /** Returns the bandwidth value measured in kilobits per second.
     * @throws SdpParseException
     * @return the bandwidth value
     */
    public int getValue() throws SdpParseException {
        return getBandwidth();
    }

    /** Sets the bandwidth value.
     * @param value to set
     * @throws SdpException
     */
    public void setValue(int value) throws SdpException {
        setBandwidth(value);
    }

}
