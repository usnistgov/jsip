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
*   Key field part of an SDP header.
* Acknowledgement. Bug fix contributed by espen@java.net
*
*@version  JSR141-PUBLIC-REVIEW (subject to change)
*
*@author Oliver Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*/
public class KeyField extends SDPField implements javax.sdp.Key {
    protected String type;
    protected String keyData;

    public KeyField() {
        super(KEY_FIELD);
    }

    public String getType() {
        return type;
    }

    public String getKeyData() {
        return keyData;
    }

    /**
    * Set the type member
    */
    public void setType(String t) {
        type = t;
    }
    /**
    * Set the keyData member
    */
    public void setKeyData(String k) {
        keyData = k;
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        String encoded_string;
        encoded_string = KEY_FIELD + type;
        if (keyData != null) {
            encoded_string += Separators.COLON;
            encoded_string += keyData;
        }
        encoded_string += Separators.NEWLINE;
        return encoded_string;
    }

    /** Returns the name of this attribute
     * @throws SdpParseException
     * @return the name of this attribute
     */
    public String getMethod() throws SdpParseException {
        return this.type;
    }

    /** Sets the id of this attribute.
     * @param name to set
     * @throws SdpException if the name is null
     */
    public void setMethod(String name) throws SdpException {
        this.type = name;
    }

    /** Determines if this attribute has an associated value.
     * @throws SdpParseException
     * @return if this attribute has an associated value.
     */
    public boolean hasKey() throws SdpParseException {
        String key = getKeyData();
        return key != null;
    }

    /** Returns the value of this attribute.
     * @throws SdpParseException
     * @return the value of this attribute
     */
    public String getKey() throws SdpParseException {
        return getKeyData();
    }

    /** Sets the value of this attribute.
     * @param key to set
     * @throws SdpException if key is null
     */
    public void setKey(String key) throws SdpException {
        if (key == null)
            throw new SdpException("The key is null");
        else
            setKeyData(key);
    }
}

