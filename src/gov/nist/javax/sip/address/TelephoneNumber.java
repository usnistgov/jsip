package gov.nist.javax.sip.address;

import gov.nist.core.*;
import java.util.Iterator;

/**
 * Telephone number class.
 * @version  JAIN-SIP-1.1
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:28 $
 *
 * @author M. Ranganathan <mranga@nist.gov>
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class TelephoneNumber extends NetObject {
	public static final String POSTDIAL = ParameterNames.POSTDIAL;
	public static final String PHONE_CONTEXT_TAG =
		ParameterNames.PHONE_CONTEXT_TAG;
	public static final String ISUB = ParameterNames.ISUB;
	public static final String PROVIDER_TAG = ParameterNames.PROVIDER_TAG;

	/** isglobal field
	 */
	protected boolean isglobal;

	/** phoneNumber field
	 */
	protected String phoneNumber;

	/** parmeters list
	 */
	protected NameValueList parms;

	/** Creates new TelephoneNumber */
	public TelephoneNumber() {
		parms = new NameValueList("telparms");
	}

	/** delete the specified parameter.
	 * @param name String to set
	 */
	public void deleteParm(String name) {
		parms.delete(name);
	}

	/** get the PhoneNumber field
	 * @return String
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/** get the PostDial field
	 * @return String
	 */
	public String getPostDial() {
		return (String) parms.getValue(POSTDIAL);
	}

	/**
	 * Get the isdn subaddress for this number.
	 * @return String
	 */
	public String getIsdnSubaddress() {
		return (String) parms.getValue(ISUB);
	}

	/** returns true if th PostDial field exists
	 * @return boolean
	 */
	public boolean hasPostDial() {
		return parms.getValue(POSTDIAL) != null;
	}

	/** return true if this header has parameters.
	 * @param pname String to set
	 * @return boolean
	 */
	public boolean hasParm(String pname) {
		return parms.hasNameValue(pname);
	}

	/**
	 * return true if the isdn subaddress exists.
	 * @return boolean
	 */
	public boolean hasIsdnSubaddress() {
		return hasParm(ISUB);
	}

	/**
	 * is a global telephone number.
	 * @return boolean
	 */
	public boolean isGlobal() {
		return isglobal;
	}

	/** remove the PostDial field
	 */
	public void removePostDial() {
		parms.delete(POSTDIAL);
	}

	/**
	 * Remove the isdn subaddress (if it exists).
	 */
	public void removeIsdnSubaddress() {
		deleteParm(ISUB);
	}

	/**
	 * Set the list of parameters.
	 * @param p NameValueList to set
	 */
	public void setParameters(NameValueList p) {
		parms = p;
	}

	/** set the Global field
	 * @param g boolean to set
	 */
	public void setGlobal(boolean g) {
		isglobal = g;
	}

	/** set the PostDial field
	 * @param p String to set
	 */
	public void setPostDial(String p) {
		NameValue nv = new NameValue(POSTDIAL, p);
		parms.add(nv);
	}

	/** set the specified parameter
	 * @param name String to set
	 * @param value Object to set
	 */
	public void setParm(String name, Object value) {
		NameValue nv = new NameValue(name, value);
		parms.add(nv);
	}

	/**
	 * set the isdn subaddress for this structure.
	 * @param isub String to set
	 */
	public void setIsdnSubaddress(String isub) {
		setParm(ISUB, isub);
	}

	/** set the PhoneNumber field
	 * @param num String to set
	 */
	public void setPhoneNumber(String num) {
		phoneNumber = num;
	}

	public String encode() {
		String retval = "";
		if (isglobal)
			retval += "+";
		retval += phoneNumber;
		if (!parms.isEmpty()) {
			retval += SEMICOLON;
			retval += parms.encode();
		}
		return retval;
	}

	/**
	 * Returns the value of the named parameter, or null if it is not set. A
	 * zero-length String indicates flag parameter.
	 *
	 * @param name name of parameter to retrieve
	 *
	 * @return the value of specified parameter
	 *
	 */
	public String getParameter(String name) {
		Object val = parms.getValue(name);
		if (val == null)
			return null;
		if (val instanceof GenericObject)
			return ((GenericObject) val).encode();
		else
			return val.toString();
	}

	/**
	 *
	 * Returns an Iterator over the names (Strings) of all parameters.
	 *
	 * @return an Iterator over all the parameter names
	 *
	 */
	public Iterator getParameterNames() {
		return this.parms.getNames();
	}

	public void removeParameter(String parameter) {
		this.parms.delete(parameter);
	}

	public void setParameter(String name, String value) {
		NameValue nv = new NameValue(name, value);
		this.parms.add(nv);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
