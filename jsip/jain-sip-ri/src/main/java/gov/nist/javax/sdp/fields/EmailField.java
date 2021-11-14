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
* email field in the SDP announce.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*/
public class EmailField extends SDPField implements javax.sdp.EMail {

    protected EmailAddress emailAddress;

    public EmailField() {
        super(SDPFieldNames.EMAIL_FIELD);
        emailAddress = new EmailAddress();
    }

    public EmailAddress getEmailAddress() {
        return emailAddress;
    }
    /**
     * Set the emailAddress member
     */
    public void setEmailAddress(EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        return EMAIL_FIELD + emailAddress.encode() + Separators.NEWLINE;
    }

    public String toString() {
        return this.encode();
    }

    /** Returns the value.
     * @throws SdpParseException
     * @return the value
     */
    public String getValue() throws SdpParseException {
        if (emailAddress == null)
            return null;
        else {
            return emailAddress.getDisplayName();
        }
    }

    /** Set the value.
     * @param value to set
     * @throws SdpException if the value is null
     */
    public void setValue(String value) throws SdpException {
        if (value == null)
            throw new SdpException("The value is null");
        else {

            emailAddress.setDisplayName(value);
        }
    }

    public Object clone() {
        EmailField retval = (EmailField) super.clone();
        if (this.emailAddress != null)
            retval.emailAddress = (EmailAddress) this.emailAddress.clone();
        return retval;
    }

}
