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
package gov.nist.core;

/*
 * Bug reports and fixes: Kirby Kiem, Jeroen van Bemmel.
 */

/**
 * Generic structure for storing name-value pairs.
 * 
 * @version 1.2
 * 
 * @author M. Ranganathan <br/>
 * 
 * 
 * 
 */
public class NameValue extends GenericObject {
	protected boolean isQuotedString;

	protected final boolean isFlagParameter;

	protected String separator;

	protected String quotes;

	protected String name;

	protected Object value;

	public NameValue() {
		name = null;
		value = "";
		separator = Separators.EQUALS;
		this.quotes = "";
		this.isFlagParameter = false;
	}

	/**
	 * New constructor, taking a boolean which is set if the NV pair is a flag
	 * 
	 * @param n
	 * @param v
	 * @param isFlag
	 */
	public NameValue(String n, Object v, boolean isFlag) {

		// assert (v != null ); // I dont think this assertion is correct mranga

		name = n;
		value = v;
		separator = Separators.EQUALS;
		quotes = "";
		this.isFlagParameter = isFlag;
	}

	/**
	 * Original constructor, sets isFlagParameter to 'false'
	 * 
	 * @param n
	 * @param v
	 */
	public NameValue(String n, Object v) {
		this(n, v, false);
	}

	/**
	 * Set the separator for the encoding method below.
	 */
	public void setSeparator(String sep) {
		separator = sep;
	}

	/**
	 * A flag that indicates that doublequotes should be put around the value
	 * when encoded (for example name=value when value is doublequoted).
	 */
	public void setQuotedValue() {
		isQuotedString = true;
		this.quotes = Separators.DOUBLE_QUOTE;
	}

	/**
	 * Return true if the value is quoted in doublequotes.
	 */
	public boolean isValueQuoted() {
		return isQuotedString;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return isFlagParameter ? "" : value; // never return null for flag
												// params
	}

	/**
	 * Set the name member
	 */
	public void setName(String n) {
		name = n;
	}

	/**
	 * Set the value member
	 */
	public void setValue(Object v) {
		value = v;
	}

	/**
	 * Get the encoded representation of this namevalue object. Added
	 * doublequote for encoding doublequoted values.
	 * 
	 * @since 1.0
	 * @return an encoded name value (eg. name=value) string.
	 */
	public String encode() {
		if (name != null && value != null && !isFlagParameter) {
			if (GenericObject.isMySubclass(value.getClass())) {
				GenericObject gv = (GenericObject) value;
				return name + separator + quotes + gv.encode() + quotes;
			} else if (GenericObjectList.isMySubclass(value.getClass())) {
				GenericObjectList gvlist = (GenericObjectList) value;
				return name + separator + gvlist.encode();
			} else if ( value.toString().equals("")) {
				return name;
			} else 
				return name + separator + quotes + value.toString() + quotes;
		} else if (name == null && value != null) {
			if (GenericObject.isMySubclass(value.getClass())) {
				GenericObject gv = (GenericObject) value;
				return gv.encode();
			} else if (GenericObjectList.isMySubclass(value.getClass())) {
				GenericObjectList gvlist = (GenericObjectList) value;
				return gvlist.encode();
			}
			return quotes + value.toString() + quotes;
		} else if (name != null && (value == null || isFlagParameter)) {
			return name;
		} else
			return "";
	}

	public Object clone() {
		NameValue retval = (NameValue) super.clone();
		if (value != null)
			retval.value = makeClone(value);
		return retval;
	}

	/**
	 * Equality comparison predicate.
	 */
	public boolean equals(Object other) {
		if (!other.getClass().equals(this.getClass()))
			return false;
		NameValue that = (NameValue) other;
		if (this == that)
			return true;
		if (this.name == null && that.name != null || this.name != null
				&& that.name == null)
			return false;
		if (this.name != null && that.name != null
				&& this.name.compareToIgnoreCase(that.name) != 0)
			return false;
		if (this.value != null && that.value == null || this.value == null
				&& that.value != null)
			return false;
		if (this.value == that.value)
			return true;
		if (value instanceof String) {
			// Quoted string comparisions are case sensitive.
			if (isQuotedString)
				return this.value.equals(that.value);
			String val = (String) this.value;
			String val1 = (String) that.value;
			return val.compareToIgnoreCase(val1) == 0;
		} else
			return this.value.equals(that.value);
	}

}
