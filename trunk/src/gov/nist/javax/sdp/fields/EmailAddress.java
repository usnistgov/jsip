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
* email address field of the SDP header.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change)
*
*@author Oliver Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/
public class EmailAddress extends SDPObject {
    protected String displayName;
    protected Email email;

    public String getDisplayName() {
        return displayName;
    }
    /**
     * Set the displayName member
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    /**
     * Set the email member
     */
    public void setEmail(Email email) {
        this.email = email;
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     * Here, we implement only the "displayName <email>" form
     * and not the "email (displayName)" form
     */
    public String encode() {
        String encoded_string;

        if (displayName != null) {
            encoded_string = displayName + Separators.LESS_THAN;
        } else {
            encoded_string = "";
        }
        encoded_string += email.encode();
        if (displayName != null) {
            encoded_string += Separators.GREATER_THAN;
        }
        return encoded_string;
    }
    public Object clone() {
        EmailAddress retval = (EmailAddress) super.clone();
        if (this.email != null)
            retval.email = (Email) this.email.clone();
        return retval;
    }

}
