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
import java.util.ListIterator;

/**
* A list of SDP Fields.
*
*@version 1.2
*
*@author M. Ranganathan   <br/>
*
*
*
*/

public abstract class SDPFieldList extends SDPField {
    protected SDPObjectList sdpFields;

    public SDPFieldList() {
    }

    /** Return a list iterator for the embedded field list.
    */
    public ListIterator listIterator() {
        return sdpFields.listIterator();
    }

    public SDPFieldList(String fieldName) {
        super(fieldName);
        sdpFields = new SDPObjectList(fieldName);
    }

    public SDPFieldList(String fieldName, String classname) {
        super(fieldName);
        sdpFields = new SDPObjectList(fieldName, classname);
    }

    /**
    *add a SDP Field to the list of headers that we maintain.
    *@param h is the sdp field to add to our list.
    */
    public void add(SDPField h) {
        sdpFields.add(h);
    }

    public SDPObject first() {
        return (SDPObject) sdpFields.first();
    }

    public SDPObject next() {
        return (SDPObject) sdpFields.next();
    }

    /**
    * Encode into a canonical string.
    */
    public String encode() {
        StringBuilder retval = new StringBuilder();
        ListIterator li = sdpFields.listIterator();
        while (li.hasNext()) {
            SDPField sdphdr = (SDPField) li.next();
            retval.append(sdphdr.encode());
        }
        return retval.toString();
    }
    /**
     * convert to a string representation (for printing).
     * @param indentation int to set
     * @return String string representation of object (for printing).
     */
    public String debugDump(int indentation) {
        stringRepresentation = "";
        String indent = new Indentation(indentation).getIndentation();
        ;
        String className = this.getClass().getName();
        sprint(indent + className);
        sprint(indent + "{");
        sprint(indent + sdpFields.debugDump(indentation));
        sprint(indent + "}");
        return stringRepresentation;
    }

    /** convert to a string representation
     * @return String
     */
    public String debugDump() {
        return debugDump(0);
    }

    /** Return a string representation.
    *
    *@return String representation.
    */
    public String toString() {
        return encode();
    }

    /**
    * Equality checking predicate.
    *@param other is the other object to compare ourselves against.
    */
    public boolean equals(Object other) {
        if (other == null ) return false;
        if (!this.getClass().equals(other.getClass()))
            return false;
        SDPFieldList that = (SDPFieldList) other;
        if (sdpFields == null)
            return that.sdpFields == null;
        return this.sdpFields.equals(that.sdpFields);
    }

    /**
    * Do a template match of fields.
    *@param template is the template to match against.
    */
    public boolean match(Object template) {
        if (template == null)
            return true;
        if (!template.getClass().equals(this.getClass()))
            return false;
        SDPFieldList other = (SDPFieldList) template;
        if (sdpFields == other.sdpFields)
            return true;
        else if (sdpFields == null)
            return false;
        return sdpFields.match(other.sdpFields);
    }

    public Object clone() {
        SDPFieldList retval = (SDPFieldList) super.clone();
        if (this.sdpFields != null)
            retval.sdpFields = (SDPObjectList) this.sdpFields.clone();
        return retval;
    }
}

