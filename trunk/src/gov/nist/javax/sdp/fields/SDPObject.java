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
import java.lang.reflect.*;

/**
* Root class for everything in this package.
*/
public abstract class SDPObject
    extends GenericObject
    implements SDPFieldNames {

    protected static final String SDPFIELDS_PACKAGE =
        PackageNames.SDP_PACKAGE + ".fields";
    protected static final String CORE_PACKAGE = PackageNames.CORE_PACKAGE;

    // should make this a property during the compile.

    public abstract String encode();

    protected void sprint(String s) {
        super.sprint(s);
    }

    protected void initSprint() {
        super.stringRepresentation = "";
    }

    public String toString() {
        return encode();
    }

    protected String getStringRepresentation() {
        return super.stringRepresentation;
    }

    /**
    * An introspection based equality predicate for SDPObjects.
    *@param  that is the other object to test against.
    *@return true if the objects are equal.
    */
    public boolean equals(Object that) {
        if (!this.getClass().equals(that.getClass()))
            return false;
        Class myclass = this.getClass();
        Field[] fields = myclass.getDeclaredFields();
        Class hisclass = that.getClass();
        Field[] hisfields = hisclass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            Field g = hisfields[i];
            // Only print protected and public members.
            int modifier = f.getModifiers();
            if (modifier == Modifier.PRIVATE)
                continue;
            Class fieldType = f.getType();
            String fieldName = f.getName();
            if (fieldName.compareTo("stringRepresentation") == 0) {
                continue;
            }
            if (fieldName.compareTo("indentation") == 0) {
                continue;
            }
            try {
                // Primitive fields are printed with type: value
                if (fieldType.isPrimitive()) {
                    String fname = fieldType.toString();
                    if (fname.compareTo("int") == 0) {
                        if (f.getInt(this) != g.getInt(that))
                            return false;
                    } else if (fname.compareTo("short") == 0) {
                        if (f.getShort(this) != g.getShort(that))
                            return false;
                    } else if (fname.compareTo("char") == 0) {
                        if (f.getChar(this) != g.getChar(that))
                            return false;
                    } else if (fname.compareTo("long") == 0) {
                        if (f.getLong(this) != g.getLong(that))
                            return false;
                    } else if (fname.compareTo("boolean") == 0) {
                        if (f.getBoolean(this) != g.getBoolean(that))
                            return false;
                    } else if (fname.compareTo("double") == 0) {
                        if (f.getDouble(this) != g.getDouble(that))
                            return false;
                    } else if (fname.compareTo("float") == 0) {
                        if (f.getFloat(this) != g.getFloat(that))
                            return false;
                    }
                } else if (g.get(that) == f.get(this))
                    continue;
                else if (f.get(this) == null && g.get(that) != null)
                    return false;
                else if (g.get(that) == null && f.get(that) != null)
                    return false;
                else if (!f.get(this).equals(g.get(that)))
                    return false;
            } catch (IllegalAccessException ex1) {
                InternalErrorHandler.handleException(ex1);
            }
        }
        return true;
    }

    /**
    * Generic introspection based printing funciton for this package.
    * Because we use introspection, this must reside in this package
    * and we cannot use the superclass defintion (even though it
    * is virtually identical.
    */
    public String debugDump() {
        stringRepresentation = "";
        Class myclass = getClass();
        sprint(myclass.getName());
        sprint("{");
        Field[] fields = myclass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            // Only print protected and public members.
            int modifier = f.getModifiers();
            if (modifier == Modifier.PRIVATE)
                continue;
            Class fieldType = f.getType();
            String fieldName = f.getName();
            if (fieldName.compareTo("stringRepresentation") == 0) {
                // avoid nasty recursions...
                continue;
            }
            if (fieldName.compareTo("indentation") == 0) {
                // formatting stuff - not relevant here.
                continue;
            }
            sprint(fieldName + ":");
            try {
                // Primitive fields are printed with type: value
                if (fieldType.isPrimitive()) {
                    String fname = fieldType.toString();
                    sprint(fname + ":");
                    if (fname.compareTo("int") == 0) {
                        int intfield = f.getInt(this);
                        sprint(intfield);
                    } else if (fname.compareTo("short") == 0) {
                        short shortField = f.getShort(this);
                        sprint(shortField);
                    } else if (fname.compareTo("char") == 0) {
                        char charField = f.getChar(this);
                        sprint(charField);
                    } else if (fname.compareTo("long") == 0) {
                        long longField = f.getLong(this);
                        sprint(longField);
                    } else if (fname.compareTo("boolean") == 0) {
                        boolean booleanField = f.getBoolean(this);
                        sprint(booleanField);
                    } else if (fname.compareTo("double") == 0) {
                        double doubleField = f.getDouble(this);
                        sprint(doubleField);
                    } else if (fname.compareTo("float") == 0) {
                        float floatField = f.getFloat(this);
                        sprint(floatField);
                    }
                } else if (
                    Class.forName(
                        CORE_PACKAGE + ".GenericObject").isAssignableFrom(
                        fieldType)) {
                    if (f.get(this) != null) {
                        sprint(
                            ((GenericObject) f.get(this)).debugDump(
                                indentation + 1));
                    } else {
                        sprint("<null>");
                    }

                } else if (
                    Class.forName(
                        CORE_PACKAGE + ".GenericObjectList").isAssignableFrom(
                        fieldType)) {
                    if (f.get(this) != null) {
                        sprint(
                            ((GenericObjectList) f.get(this)).debugDump(
                                indentation + 1));
                    } else {
                        sprint("<null>");
                    }

                } else {
                    // Dont do recursion on things that are not
                    // of our header type...
                    if (f.get(this) != null) {
                        sprint(f.get(this).getClass().getName() + ":");
                    } else {
                        sprint(fieldType.getName() + ":");
                    }

                    sprint("{");
                    if (f.get(this) != null) {
                        sprint(f.get(this).toString());
                    } else {
                        sprint("<null>");
                    }
                    sprint("}");
                }
            } catch (IllegalAccessException ex1) {
                continue; // we are accessing a private field...
            } catch (ClassNotFoundException ex) {
                System.out.println("Cound not find " + ex.getMessage());
                ex.printStackTrace();
                System.exit(0);
            }
        }
        sprint("}");
        return stringRepresentation;
    }

    /** An introspection based predicate matching using a template
    * object. Allows for partial match of two protocl Objects.
    *@param other the match pattern to test against. The match object
    * has to be of the same type (class). Primitive types
    * and non-sip fields that are non null are matched for equality.
    * Null in any field  matches anything. Some book-keeping fields
    * are ignored when making the comparison.
    */

    public boolean match(Object other) {
        if (other == null)
            return true;
        if (!this.getClass().equals(other.getClass()))
            return false;
        GenericObject that = (GenericObject) other;
        Class myclass = this.getClass();
        Field[] fields = myclass.getDeclaredFields();
        Class hisclass = other.getClass();
        Field[] hisfields = hisclass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            Field g = hisfields[i];
            // Only print protected and public members.
            int modifier = f.getModifiers();
            if (modifier == Modifier.PRIVATE)
                continue;
            Class fieldType = f.getType();
            String fieldName = f.getName();
            if (fieldName.compareTo("stringRepresentation") == 0) {
                continue;
            }
            if (fieldName.compareTo("indentation") == 0) {
                continue;
            }
            try {
                // Primitive fields are printed with type: value
                if (fieldType.isPrimitive()) {
                    String fname = fieldType.toString();
                    if (fname.compareTo("int") == 0) {
                        if (f.getInt(this) != g.getInt(that))
                            return false;
                    } else if (fname.compareTo("short") == 0) {
                        if (f.getShort(this) != g.getShort(that))
                            return false;
                    } else if (fname.compareTo("char") == 0) {
                        if (f.getChar(this) != g.getChar(that))
                            return false;
                    } else if (fname.compareTo("long") == 0) {
                        if (f.getLong(this) != g.getLong(that))
                            return false;
                    } else if (fname.compareTo("boolean") == 0) {
                        if (f.getBoolean(this) != g.getBoolean(that))
                            return false;
                    } else if (fname.compareTo("double") == 0) {
                        if (f.getDouble(this) != g.getDouble(that))
                            return false;
                    } else if (fname.compareTo("float") == 0) {
                        if (f.getFloat(this) != g.getFloat(that))
                            return false;
                    }
                } else {
                    Object myObj = f.get(this);
                    Object hisObj = g.get(that);
                    if (hisObj == myObj)
                        return true;
                    else if (hisObj != null && myObj == null)
                        return false;
                    else if (
                        hisObj instanceof java.lang.String
                            && myObj instanceof java.lang.String) {
                        if (((String) myObj)
                            .compareToIgnoreCase((String) hisObj)
                            != 0)
                            return false;
                    } else if (
                        hisObj != null
                            && GenericObject.isMySubclass(myObj.getClass())
                            && GenericObject.isMySubclass(hisObj.getClass())
                            && myObj.getClass().equals(hisObj.getClass())
                            && ((GenericObject) hisObj).getMatcher() != null) {
                        String myObjEncoded = ((GenericObject) myObj).encode();
                        return ((GenericObject) hisObj).getMatcher().match(
                            myObjEncoded);
                    } else if (
                        GenericObject.isMySubclass(myObj.getClass())
                            && !((GenericObject) myObj).match(hisObj))
                        return false;
                    else if (
                        GenericObjectList.isMySubclass(myObj.getClass())
                            && !((GenericObjectList) myObj).match(hisObj))
                        return false;
                }
            } catch (IllegalAccessException ex1) {
                InternalErrorHandler.handleException(ex1);
            }
        }
        return true;
    }


    /**
    * Formatter with a given starting indentation.
    */
    public String dbgPrint(int indent) {
        int save = indentation;
        indentation = indent;
        String retval = this.toString();
        indentation = save;
        return retval;
    }


}

