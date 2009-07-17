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
* Phone Field SDP header
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/
public class PhoneField extends SDPField implements javax.sdp.Phone {
    protected String name;
    protected String phoneNumber;

    public PhoneField() {
        super(PHONE_FIELD);
    }

    public String getName() {
        return name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    /**
    * Set the name member
        *
        *@param name - the name to set.
    */
    public void setName(String name) {
        this.name = name;
    }
    /**
    * Set the phoneNumber member
        *@param phoneNumber - phone number to set.
    */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /** Returns the value.
     * @throws SdpParseException
     * @return the value.
     */
    public String getValue() throws SdpParseException {
        return getName();
    }

    /** Sets the value.
     * @param value the - new information.
     * @throws SdpException if the value is null
     */
    public void setValue(String value) throws SdpException {
        if (value == null)
            throw new SdpException("The value parameter is null");
        else
            setName(value);
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     * Here, we implement only the "name <phoneNumber>" form
     * and not the "phoneNumber (name)" form
     */
    public String encode() {
        String encoded_string;
        encoded_string = PHONE_FIELD;
        if (name != null) {
            encoded_string += name + Separators.LESS_THAN;
        }
        encoded_string += phoneNumber;
        if (name != null) {
            encoded_string += Separators.GREATER_THAN;
        }
        encoded_string += Separators.NEWLINE;
        return encoded_string;
    }

}

