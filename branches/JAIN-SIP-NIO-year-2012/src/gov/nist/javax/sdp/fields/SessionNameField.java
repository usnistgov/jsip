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

public class SessionNameField extends SDPField implements SessionName {
    protected String sessionName;

    public SessionNameField() {
        super(SDPFieldNames.SESSION_NAME_FIELD);
    }
    public String getSessionName() {
        return sessionName;
    }
    /**
    * Set the sessionName member
    */
    public void setSessionName(String s) {
        sessionName = s;
    }

    /** Returns the value.
     * @throws SdpParseException
     * @return  the value
     */
    public String getValue() throws SdpParseException {
        return getSessionName();
    }

    /** Sets the value
     * @param value the - new information.
     * @throws SdpException if the value is null
     */
    public void setValue(String value) throws SdpException {
        if (value == null)
            throw new SdpException("The value is null");
        else {
            setSessionName(value);
        }
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        return SESSION_NAME_FIELD + sessionName + Separators.NEWLINE;
    }

}
