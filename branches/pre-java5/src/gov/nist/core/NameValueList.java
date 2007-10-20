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

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements a simple NameValue association with a quick lookup function (via a
 * hash map) the default behavior for this class is not thread safe.
 * specify a constructor with boolean true to make this thread safe.
 * 
 * @version 1.2
 * 
 * @author M. Ranganathan <br/>
 * 
 * 
 * 
 */

public class NameValueList implements Serializable {

	private Map hmap;

	private String separator;

	/**
	 * default constructor.
	 */
	public NameValueList() {
		this.separator = ";";
		this.hmap = new HashMap();
	}

	public NameValueList(boolean sync) {
		this.separator = ";";
		if (sync)
			this.hmap = new ConcurrentHashMap();
		else
			this.hmap = new HashMap();
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * Encode the list in semicolon separated form.
	 * 
	 * @return an encoded string containing the objects in this list.
	 * @since v1.0
	 */
	public String encode() {
		return encode(new StringBuffer()).toString();
	}
	
	public StringBuffer encode(StringBuffer buffer) {
		if (!hmap.isEmpty()) {
			Iterator iterator = hmap.values().iterator();
			if (iterator.hasNext()) {
				while (true) {
					Object obj = iterator.next();
					if (obj instanceof GenericObject) {
						GenericObject gobj = (GenericObject) obj;
						gobj.encode(buffer);
					} else {
						buffer.append(obj.toString());
					}
					if (iterator.hasNext())
						buffer.append(separator);
					else
						break;
				}
			}
		}
		return buffer;
	}

	public String toString() {
		return this.encode();
	}

	/**
	 * Set a namevalue object in this list.
	 */

	public void set(NameValue nv) {
		this.hmap.put(nv.name.toLowerCase(), nv);
	}

	/**
	 * Set a namevalue object in this list.
	 */
	public void set(String name, Object value) {
		NameValue nameValue = new NameValue(name, value);
		hmap.put(name.toLowerCase(), nameValue);

	}

	/**
	 * Compare if two NameValue lists are equal.
	 * 
	 * @param otherObject
	 *            is the object to compare to.
	 * @return true if the two objects compare for equality.
	 */
	public boolean equals(Object otherObject) {
		if (!otherObject.getClass().equals(this.getClass())) {
			return false;
		}
		NameValueList other = (NameValueList) otherObject;

		if (hmap.size() != other.hmap.size()) {
			return false;
		}
		Iterator li = this.hmap.keySet().iterator();

		while (li.hasNext()) {
			String key = (String) li.next();
			NameValue nv1 = this.getNameValue(key);
			NameValue nv2 = (NameValue) other.hmap.get(key);
			if (nv2 == null)
				return false;
			else if (!nv2.equals(nv1))
				return false;
		}
		return true;
	}

	/**
	 * Do a lookup on a given name and return value associated with it.
	 */
	public Object getValue(String name) {
		NameValue nv = this.getNameValue(name.toLowerCase());
		if (nv != null)
			return nv.value;
		else
			return null;
	}

	/**
	 * Get the NameValue record given a name.
	 * 
	 * @since 1.0
	 */
	public NameValue getNameValue(String name) {
		return (NameValue) this.hmap.get(name.toLowerCase());
	}

	/**
	 * Returns a boolean telling if this NameValueList has a record with this
	 * name
	 * 
	 * @since 1.0
	 */
	public boolean hasNameValue(String name) {
		return hmap.containsKey(name.toLowerCase());
	}

	/**
	 * Remove the element corresponding to this name.
	 * 
	 * @since 1.0
	 */
	public boolean delete(String name) {
		String lcName = name.toLowerCase();
		if (this.hmap.containsKey(lcName)) {
			this.hmap.remove(lcName);
			return true;
		} else {
			return false;
		}

	}

	public Object clone() {
		NameValueList retval = new NameValueList();
		retval.setSeparator(this.separator);
		Iterator it = this.hmap.values().iterator();
		while (it.hasNext()) {
			retval.set((NameValue) ((NameValue) it.next()).clone());
		}
		return retval;
	}

	/**
	 * Return the size of the embedded map
	 */
	public int size() {
		return this.hmap.size();
	}

	/**
	 * Return true if empty.
	 */
	public boolean isEmpty() {
		return hmap.isEmpty();
	}

	/**
	 * Return an iterator for the name-value pairs of this list.
	 * 
	 * @return the iterator.
	 */
	public Iterator iterator() {
		return this.hmap.values().iterator();
	}

	/**
	 * Get a list of parameter names.
	 * 
	 * @return a list iterator that has the names of the parameters.
	 */
	public Iterator getNames() {
		return this.hmap.keySet().iterator();

	}

	/**
	 * Get the parameter as a String.
	 * 
	 * @return the parameter as a string.
	 */
	public String getParameter(String name) {
		Object val = this.getValue(name);
		if (val == null)
			return null;
		if (val instanceof GenericObject)
			return ((GenericObject) val).encode();
		else
			return val.toString();
	}
}
