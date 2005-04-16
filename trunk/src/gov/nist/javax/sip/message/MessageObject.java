/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)        *
 ******************************************************************************/
package gov.nist.javax.sip.message;

import gov.nist.core.*;
import java.lang.reflect.*;

/**
 * This is the root object from which all other objects in this package
 * are derived. This class is never directly instantiated (and hence it
 * is abstract).
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:51 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public abstract class MessageObject extends GenericObject {
	public abstract String encode();

	protected static final String MESSAGE_PACKAGE =
		PackageNames.MESSAGE_PACKAGE;
	protected static final String SIP_PACKAGE = PackageNames.SIP_PACKAGE;
	protected static final String PARSER_PACKAGE = PackageNames.PARSER_PACKAGE;
	protected static final String SIPHEADERS_PACKAGE =
		PackageNames.SIPHEADERS_PACKAGE;

	public void dbgPrint() {
		super.dbgPrint();
	}
	
	/**
	 * An introspection based string formatting method. We need this because
	 * in this package (although it is an exact duplicate of the one in
	 * the superclass) because it needs to access the protected members
	 * of the other objects in this class.
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
					getClassFromName(
						SIP_PACKAGE + ".GenericObject").isAssignableFrom(
						fieldType)) {
					if (f.get(this) != null) {
						sprint(
							((GenericObject) f.get(this)).debugDump(
								this.indentation + 1));
					} else {
						sprint("<null>");
					}

				} else if (
					getClassFromName(
						SIP_PACKAGE + ".GenericObjectList").isAssignableFrom(
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
			}
		}
		sprint("}");
		return stringRepresentation;
	}
	
	/** 
	 * Recursively override the fields of this object with the fields
	 * of a new object. This is useful when you want to genrate a template
	 * and override the fields of an incoming SIPMessage with another
	 * SIP message that you have already generated.
	 *
	 * @param mergeObject is the replacement object.  The override
	 * obect must be of the same class as this object.
	 * Set any fields that you do not want to override as null in the
	 * mergeOject object.
	 */
	public void merge(Object mergeObject) {
		if (!mergeObject.getClass().equals(this.getClass()))
			throw new IllegalArgumentException("Bad override object");
		// Base case.
		if (mergeObject == null)
			return;
		Class myclass = this.getClass();
		Field[] fields = myclass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			int modifier = f.getModifiers();
			if (Modifier.isPrivate(modifier)) {
				continue;
			} else if (Modifier.isStatic(modifier)) {
				continue;
			} else if (Modifier.isInterface(modifier)) {
				continue;
			}
			Class fieldType = f.getType();
			String fieldName = f.getName();
			String fname = fieldType.toString();
			try {
				// Primitive fields are printed with type: value 
				if (fieldType.isPrimitive()) {
					if (fname.compareTo("int") == 0) {
						int intfield = f.getInt(mergeObject);
						f.setInt(this, intfield);
					} else if (fname.compareTo("short") == 0) {
						short shortField = f.getShort(mergeObject);
						f.setShort(this, shortField);
					} else if (fname.compareTo("char") == 0) {
						char charField = f.getChar(mergeObject);
						f.setChar(this, charField);
					} else if (fname.compareTo("long") == 0) {
						long longField = f.getLong(mergeObject);
						f.setLong(this, longField);
					} else if (fname.compareTo("boolean") == 0) {
						boolean booleanField = f.getBoolean(mergeObject);
						f.setBoolean(this, booleanField);
					} else if (fname.compareTo("double") == 0) {
						double doubleField = f.getDouble(mergeObject);
						f.setDouble(this, doubleField);
					} else if (fname.compareTo("float") == 0) {
						float floatField = f.getFloat(mergeObject);
						f.setFloat(this, floatField);
					}
				} else {
					Object obj = f.get(this);
					Object mobj = f.get(mergeObject);
					if (mobj == null)
						continue;
					if (obj == null) {
						f.set(this, mobj);
						continue;
					}
					if (obj instanceof GenericObject) {
						GenericObject gobj = (GenericObject) obj;
						gobj.merge(mobj);
					} else if (obj instanceof GenericObjectList) {
						GenericObjectList gobjList = (GenericObjectList) obj;
						gobjList.mergeObjects((GenericObjectList) mobj);
					} else {
						f.set(this, mobj);
					}
				}
			} catch (IllegalAccessException ex1) {
				ex1.printStackTrace();
				continue; // we are accessing a private field...
			}
		}
	}

	protected MessageObject() {
		super();
	}

	/**
	 * Formatter with a given starting indentation (for nested structs).
	 */
	public String dbgPrint(int indent) {
		int save = indentation;
		indentation = indent;
		String retval = this.toString();
		indentation = save;
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:31  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
