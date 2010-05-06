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
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.lang.reflect.*;

public class SDPObjectList extends GenericObjectList {
    protected static final String SDPFIELDS_PACKAGE =
        PackageNames.SDP_PACKAGE + ".fields";

    /**
     * Do a merge of the GenericObjects contained in this list with the
     * GenericObjects in the mergeList. Note that this does an inplace
     * modification of the given list. This does an object by object
     * merge of the given objects.
     *
     *@param mergeList is the list of Generic objects that we want to do
     * an object by object merge with. Note that no new objects are
     * added to this list.
     *
     */

    public void mergeObjects(GenericObjectList mergeList) {

        Iterator<GenericObject> it1 = this.listIterator();
        Iterator<GenericObject> it2 = mergeList.listIterator();
        while (it1.hasNext()) {
            GenericObject outerObj = (GenericObject) it1.next();
            while (it2.hasNext()) {
                Object innerObj = it2.next();
                outerObj.merge(innerObj);
            }
        }
    }

    /**
     * Add an sdp object to this list.
     */
    public void add(SDPObject s) {
        super.add(s);
    }

    /**
     * Get the input text of the sdp object (from which the object was
     * generated).
     */

    public SDPObjectList(String lname, String classname) {
        super(lname, classname);
    }

    public SDPObjectList() {
        super(null, SDPObject.class);
    }

    public SDPObjectList(String lname) {
        super(lname, SDPFIELDS_PACKAGE + ".SDPObject");
    }

    public GenericObject first() {
        return (SDPObject) super.first();
    }

    public GenericObject next() {
        return (SDPObject) super.next();
    }



    public String encode() {
        StringBuilder retval = new StringBuilder();
        SDPObject sdpObject;
        for (sdpObject = (SDPObject) this.first();
            sdpObject != null;
            sdpObject = (SDPObject) this.next()) {
            retval.append (sdpObject.encode());
        }
        return retval.toString();
    }

    public String toString() {
        return this.encode();
    }
    


}

