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

/** Information field implementation
*@version  JSR141-PUBLIC-REVIEW (subject to change)
*
*@author Oliver Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*/

public class InformationField extends SDPField implements javax.sdp.Info {
    protected String information;

    public InformationField() {
        super(INFORMATION_FIELD);
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String info) {
        information = info;
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        return INFORMATION_FIELD + information + Separators.NEWLINE;
    }

    /** Returns the value.
     * @throws SdpParseException
     * @return the value
     */
    public String getValue() throws SdpParseException {
        return information;
    }

    /** Set the value.
     * @param value to set
     * @throws SdpException if the value is null
     */
    public void setValue(String value) throws SdpException {
        if (value == null)
            throw new SdpException("The value is null");
        else {
            setInformation(value);
        }
    }

}

