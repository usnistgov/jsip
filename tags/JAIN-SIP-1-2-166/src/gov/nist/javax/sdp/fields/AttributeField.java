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
* Attribute Field.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/
public class AttributeField extends SDPField implements javax.sdp.Attribute {
    protected NameValue attribute;

    public NameValue getAttribute() {
        return attribute;
    }

    public AttributeField() {
        super(ATTRIBUTE_FIELD);
    }
    /**
    * Set the attribute member
    */
    public void setAttribute(NameValue a) {
        attribute = a;
        attribute.setSeparator(Separators.COLON);
    }

    /**
    *  Get the string encoded version of this object
    * @since v1.0
    */
    public String encode() {
        String encoded_string = ATTRIBUTE_FIELD;
        if (attribute != null)
            encoded_string += attribute.encode();
        return encoded_string + Separators.NEWLINE;
    }

    public String toString() {
        return this.encode();
    }

    /** Returns the name of this attribute
    * @throws SdpParseException if the name is not well formatted.
    * @return a String identity or null.
    */
    public String getName() throws SdpParseException {
        NameValue nameValue = getAttribute();
        if (nameValue == null)
            return null;
        else {
            String name = nameValue.getName();
            if (name == null)
                return null;
            else
                return name;
        }
    }

    /** Sets the id of this attribute.
     * @param name  the string name/id of the attribute.
     * @throws SdpException if the name is null
     */
    public void setName(String name) throws SdpException {
        if (name == null)
            throw new SdpException("The name is null");
        else {
            NameValue nameValue = getAttribute();
            if (nameValue == null)
                nameValue = new NameValue();
            nameValue.setName(name);
            setAttribute(nameValue);
        }
    }

    /** Determines if this attribute has an associated value.
     * @throws SdpParseException if the value is not well formatted.
     * @return true if the attribute has a value.
     */
    public boolean hasValue() throws SdpParseException {
        NameValue nameValue = getAttribute();
        if (nameValue == null)
            return false;
        else {
            Object value = nameValue.getValueAsObject();
            if (value == null)
                return false;
            else
                return true;
        }
    }

    /** Returns the value of this attribute.
     * @throws SdpParseException if the value is not well formatted.
     * @return the value; null if the attribute has no associated value.
     */
    public String getValue() throws SdpParseException {
        NameValue nameValue = getAttribute();
        if (nameValue == null)
            return null;
        else {
            Object value = nameValue.getValueAsObject();
            if (value == null)
                return null;
            else if (value instanceof String)
                return (String) value;
            else
                return value.toString();
        }
    }

    /** Sets the value of this attribute.
     * @param value the - attribute value
     * @throws SdpException if the value is null.
     */
    public void setValue(String value) throws SdpException {
        if (value == null)
            throw new SdpException("The value is null");
        else {
            NameValue nameValue = getAttribute();
            if (nameValue == null)
                nameValue = new NameValue();
            nameValue.setValueAsObject(value);
            setAttribute(nameValue);
        }
    }

    /**
     * Allow for null value when setting the value.
     *
     * @param value -- can be null.
     */

    public void setValueAllowNull(String value)  {
        NameValue nameValue = getAttribute();
        if (nameValue == null)
            nameValue = new NameValue();
        nameValue.setValueAsObject(value);
        setAttribute(nameValue);
    }

    public Object clone() {
        AttributeField retval = (AttributeField) super.clone();
        if (this.attribute != null)
            retval.attribute = (NameValue) this.attribute.clone();
        return retval;
    }

    public boolean equals(Object that ) {
        if ( ! (that instanceof AttributeField)) return false;
        AttributeField other = (AttributeField) that;
        return other.getAttribute().getName().equalsIgnoreCase(this.getAttribute().getName()) &&
                this.getAttribute().getValueAsObject().equals(other.getAttribute().getValueAsObject());
    }
    
    @Override
    public int hashCode() {
        if (getAttribute() == null ) throw new UnsupportedOperationException("Attribute is null cannot compute hashCode ");
        return this.encode().hashCode();
    }
}
