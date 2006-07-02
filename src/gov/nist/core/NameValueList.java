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
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;

/**
* Implements a simple NameValue association with a quick lookup 
* function (via a hash table) this class is not thread safe 
* because it uses HashTables.
*
*@version 1.2
*
*@author M. Ranganathan   <br/>
*
*
*
*/

public class NameValueList extends GenericObjectList {

	

	public NameValueList(String listName) {
		super(listName, NameValue.class);
	}

	public void add(NameValue nv) {
		if (nv == null)
			throw new NullPointerException("null nv");
		super.add((GenericObject) nv);
	}

	/**
	* Set a namevalue object in this list.
	*/

	public void set(NameValue nv) {
		this.delete(nv.name);
		this.add(nv);
	}

	/**
	* Set a namevalue object in this list.
	*/
	public void set(String name, Object value) {
		NameValue nv = new NameValue(name, value);
		this.set(nv);
	}

	/**
	* Add a name value record to this list.
	*/

	public void add(String name, Object obj) {
		NameValue nv = new NameValue(name, obj);
		add(nv);
	}

	/**
	     *  Compare if two NameValue lists are equal.
	 *@param otherObject  is the object to compare to.
	 *@return true if the two objects compare for equality.
	     */
	public boolean equals(Object otherObject) {
		if (!otherObject.getClass().equals(this.getClass())) {
			return false;
		}
		NameValueList other = (NameValueList) otherObject;

		if (this.size() != other.size()) {
			return false;
		}
		ListIterator li = this.listIterator();

		while (li.hasNext()) {
			NameValue nv = (NameValue) li.next();
			boolean found = false;
			ListIterator li1 = other.listIterator();
			while (li1.hasNext()) {
				NameValue nv1 = (NameValue) li1.next();
				// found a match so break;
				if (nv.equals(nv1)) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	/**
	*  Do a lookup on a given name and return value associated with it.
	*/
	public Object getValue(String name) {
		NameValue nv = this.getNameValue(name);
		if (nv != null)
			return nv.value;
		else
			return null;
	}

	/**
	* Get the NameValue record given a name.
	* @since 1.0
	*/
	public NameValue getNameValue(String name) {
		ListIterator li = this.listIterator();

		NameValue retval = null;
		while (li.hasNext()) {
			NameValue nv = (NameValue) li.next();
			if (nv.getName().equalsIgnoreCase(name)) {
				retval = nv;
				break;
			}
		}
		return retval;
	}

	/**
	* Returns a boolean telling if this NameValueList
	* has a record with this name  
	* @since 1.0
	*/
	public boolean hasNameValue(String name) {
		return getNameValue(name) != null;
	}

	/**
	* Remove the element corresponding to this name.
	* @since 1.0
	*/
	public boolean delete(String name) {
		ListIterator li = this.listIterator();
		NameValue nv;
		boolean removed = false;
		while (li.hasNext()) {
			nv = (NameValue) li.next();
			if (nv.getName().equalsIgnoreCase(name)) {
				li.remove();
				removed = true;
			}
		}
		return removed;

	}

	
	/**
	 *Get a list of parameter names.
	 *@return a list iterator that has the names of the parameters.
	 */
	public Iterator getNames() {
		LinkedList ll = new LinkedList();
		Iterator iterator =  super.iterator();
		while (iterator.hasNext()) {
			String name = ((NameValue) iterator.next()).name;
			ll.add(name);
		}
		return ll.listIterator();
	}

	/**
	 *default constructor.
	 */
	public NameValueList() {
	}

	/** Get the parameter as a String.
	 *@return the parameter as a string.
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
