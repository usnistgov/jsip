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

/** Proto version field of SDP announce.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*
*/
public class ProtoVersionField extends SDPField implements javax.sdp.Version {
    protected int protoVersion;

    public ProtoVersionField() {
        super(PROTO_VERSION_FIELD);
    }

    public int getProtoVersion() {
        return protoVersion;
    }

    /**
    * Set the protoVersion member
    */
    public void setProtoVersion(int pv) {
        protoVersion = pv;
    }

    /** Returns the version number.
     * @throws SdpParseException
     * @return int
     */
    public int getVersion() throws SdpParseException {
        return getProtoVersion();
    }

    /** Sets the version.
     * @param value the - new version value.
     * @throws SdpException if the value is <=0
     */
    public void setVersion(int value) throws SdpException {
        if (value < 0)
            throw new SdpException("The value is <0");
        else
            setProtoVersion(value);
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        return PROTO_VERSION_FIELD + protoVersion + Separators.NEWLINE;
    }

}

