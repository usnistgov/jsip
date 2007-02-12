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
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.GenericObject;
import gov.nist.core.Separators;
import gov.nist.javax.sip.header.ims.PrivacyHeader;

import javax.sip.header.Header;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * 
 * This is the root class for all lists of SIP headers. It imbeds a
 * SIPObjectList object and extends SIPHeader Lists of ContactSIPObjects etc.
 * derive from this class. This supports homogeneous lists (all elements in the
 * list are of the same class). We use this for building type homogeneous lists
 * of SIPObjects that appear in SIPHeaders
 * 
 * @version 1.2 $Revision: 1.7 $ $Date: 2005/10/09 18:47:53
 */
public class SIPHeaderList extends SIPHeader implements java.util.List, Header {

	/**
	 * hlist field.
	 */
	protected List hlist;

	private Class myClass;

	public String getName() {
		return this.headerName;
	}

	/**
	 * Constructor
	 * 
	 * @param hl
	 *            SIPObjectList to set
	 * @param hname
	 *            String to set
	 */
	protected SIPHeaderList(List hl, String hname) {
		this.headerName = hname;
		hlist = hl;
	}

	/**
	 * Constructor
	 * 
	 * @param hName
	 *            String to set
	 */
	protected SIPHeaderList(String hName) {
		this.headerName = hName;
		hlist = new LinkedList();
	}

	/**
	 * Constructor
	 * 
	 * @param objclass
	 *            Class to set
	 * @param hname
	 *            String to set
	 */
	protected SIPHeaderList(Class objclass, String hname) {
		this.headerName = hname;
		hlist = new LinkedList();
		this.myClass = objclass;

	}

	/**
	 * Concatenate the list of stuff that we are keeping around and also the
	 * text corresponding to these structures (that we parsed).
	 * 
	 * @param objectToAdd
	 */
	public boolean add(Object objectToAdd) {
		hlist.add(objectToAdd);
		return true;
	}

	/**
	 * Concatenate the list of stuff that we are keeping around and also the
	 * text corresponding to these structures (that we parsed).
	 * 
	 * @param obj
	 *            Genericobject to set
	 */
	public void addFirst(Object obj) {
		hlist.add(0, obj);
	}

	/**
	 * Add to this list.
	 * 
	 * @param sipheader
	 *            SIPHeader to add.
	 * @param top
	 *            is true if we want to add to the top of the list.
	 */
	public void add(Object sipheader, boolean top) {
		if (top)
			this.addFirst(sipheader);
		else
			this.add(sipheader);
	}

	/**
	 * Concatenate two compatible lists. This appends or prepends the new list
	 * to the end of this list.
	 * 
	 * @param other
	 *            SIPHeaderList to set
	 * @param topFlag
	 *            flag which indicates which end to concatenate
	 *            the lists.
	 * @throws IllegalArgumentException
	 *             if the two lists are not compatible
	 */
	public void concatenate(SIPHeaderList other, boolean topFlag)
			throws IllegalArgumentException {
		if (!topFlag) {
			this.addAll(other);
		} else {
			// add given items to the top end of the list.
			this.addAll(0, other);
		}

	}

	/**
	 * Concatenate two compatible lists. This appends the new list to the end of
	 * this list (which is the most common mode for this operation).
	 * 
	 * @param other
	 *            SIPHeaderList
	 * @throws IllegalArgumentException
	 *             if the two lists are not compatible
	 */
	public void concatenate(SIPHeaderList other)
			throws IllegalArgumentException {
		this.concatenate(other, false);
	}

	/**
	 * Encode a list of sip headers. Headers are returned in cannonical form.
	 * 
	 * @return String encoded string representation of this list of headers.
	 *         (Contains string append of each encoded header).
	 */
	public String encode() {
		return encode(new StringBuffer()).toString();
	}

	public StringBuffer encode(StringBuffer buffer) {
		if (hlist.isEmpty()) {
			buffer.append(headerName).append(':').append(Separators.NEWLINE);
		}
		else {
			// The following headers do not have comma separated forms for
			// multiple headers. Thus, they must be encoded separately.
			if (this.headerName.equals(SIPHeaderNames.WWW_AUTHENTICATE)
					|| this.headerName.equals(SIPHeaderNames.PROXY_AUTHENTICATE)
					|| this.headerName.equals(SIPHeaderNames.AUTHORIZATION)
					|| this.headerName.equals(SIPHeaderNames.PROXY_AUTHORIZATION)
					|| this instanceof ExtensionHeaderList) {
				ListIterator li = hlist.listIterator();
				while (li.hasNext()) {
					SIPHeader sipheader = (SIPHeader) li.next();
					sipheader.encode(buffer);
				}
			} else {
				// These can be concatenated together in an comma separated
				// list.
				buffer.append(headerName).append(Separators.COLON).append(Separators.SP);
				this.encodeBody(buffer);
				buffer.append(Separators.NEWLINE);
			}
		}
		return buffer;
	}

	/**
	 * Return a list of encoded strings (one for each sipheader).
	 * 
	 * @return LinkedList containing encoded strings in this header list. an
	 *         empty list is returned if this header list contains no sip
	 *         headers.
	 */

	public List getHeadersAsEncodedStrings() {
		List retval = new LinkedList();

		ListIterator li = hlist.listIterator();
		while (li.hasNext()) {
			SIPHeader sipheader = (SIPHeader) li.next();
			retval.add(sipheader.encode());

		}

		return retval;

	}

	/**
	 * Get the first element of this list.
	 * 
	 * @return SIPHeader first element of the list.
	 */
	public SIPHeader getFirst() {
		if (hlist == null || hlist.isEmpty())
			return null;
		else
			return (SIPHeader) hlist.get(0);
	}

	/**
	 * Get the last element of this list.
	 * 
	 * @return SIPHeader last element of the list.
	 */
	public SIPHeader getLast() {
		if (hlist == null || hlist.isEmpty())
			return null;
		return (SIPHeader) hlist.get(hlist.size() - 1);
	}

	/**
	 * Get the class for the headers of this list.
	 * 
	 * @return Class of header supported by this list.
	 */
	public Class getMyClass() {
		return this.myClass;
	}

	/**
	 * Empty check
	 * 
	 * @return boolean true if list is empty
	 */
	public boolean isEmpty() {
		return hlist.isEmpty();
	}

	/**
	 * Get an initialized iterator for my imbedded list
	 * 
	 * @return the generated ListIterator
	 */
	public ListIterator listIterator() {

		return hlist.listIterator(0);
	}

	/**
	 * Get the imbedded linked list.
	 * 
	 * @return the imedded linked list of SIP headers.
	 */
	public List getHeaderList() {
		return this.hlist;
	}

	/**
	 * Get the list iterator for a given position.
	 * 
	 * @param position
	 *            position for the list iterator to return
	 * @return the generated list iterator
	 */
	public ListIterator listIterator(int position) {
		return hlist.listIterator(position);
	}

	/**
	 * Remove the first element of this list.
	 */
	public void removeFirst() {
		if (hlist.size() != 0)
			hlist.remove(0);

	}

	/**
	 * Remove the last element of this list.
	 */
	public void removeLast() {
		if (hlist.size() != 0)
			hlist.remove(hlist.size() - 1);
	}

	/**
	 * Remove a sip header from this list of sip headers.
	 * 
	 * @param obj
	 *            SIPHeader to remove
	 * @return boolean
	 */
	public boolean remove(SIPHeader obj) {
		if (hlist.size() == 0)
			return false;
		else
			return hlist.remove(obj);
	}

	/**
	 * Set the root class for all objects inserted into my list (for assertion
	 * check)
	 * 
	 * @param cl
	 *            class to set
	 */
	protected void setMyClass(Class cl) {
		this.myClass = cl;
	}

	/**
	 * convert to a string representation (for printing).
	 * 
	 * @param indentation
	 *            int to set
	 * @return String string representation of object (for printing).
	 */
	public String debugDump(int indentation) {
		stringRepresentation = "";
		String indent = new Indentation(indentation).getIndentation();

		String className = this.getClass().getName();
		sprint(indent + className);
		sprint(indent + "{");

		for (Iterator it = hlist.iterator(); it.hasNext();) {
			SIPHeader sipHdr = (SIPHeader) it.next();
			sprint(indent + sipHdr.debugDump());
		}
		sprint(indent + "}");
		return stringRepresentation;
	}

	/**
	 * convert to a string representation
	 * 
	 * @return String
	 */
	public String debugDump() {
		return debugDump(0);
	}

	/**
	 * Array conversion.
	 * 
	 * @return SIPHeader []
	 */
	public Object[] toArray() {
		return hlist.toArray();

	}

	/**
	 * index of an element.
	 * 
	 * @return index of the given element (-1) if element does not exist.
	 */
	public int indexOf(GenericObject gobj) {
		return hlist.indexOf(gobj);
	}

	/**
	 * insert at a location.
	 * 
	 * @param index
	 *            location where to add the sipHeader.
	 * @param sipHeader
	 *            SIPHeader structure to add.
	 */

	public void add(int index, Object sipHeader)
			throws IndexOutOfBoundsException {
		hlist.add(index, sipHeader);
	}

	/**
	 * Equality comparison operator.
	 * 
	 * @param other
	 *            the other object to compare with. true is returned iff the
	 *            classes match and list of headers herein is equal to the list
	 *            of headers in the target (order of the headers is not
	 *            important).
	 */
	public boolean equals(Object other) {

		if (other == this)
			return true;

		if (other instanceof SIPHeaderList) {
			SIPHeaderList that = (SIPHeaderList) other;
			if (this.hlist == that.hlist)
				return true;
			else if (this.hlist == null)
				return that.hlist == null || that.hlist.size() == 0;
			return this.hlist.equals(that.hlist);
		}
		return false;
	}

	/**
	 * Template match against a template. null field in template indicates wild
	 * card match.
	 */
	public boolean match(SIPHeaderList template) {
		if (template == null)
			return true;
		if (!this.getClass().equals(template.getClass()))
			return false;
		SIPHeaderList that = (SIPHeaderList) template;
		if (this.hlist == that.hlist)
			return true;
		else if (this.hlist == null)
			return false;
		else {

			for (Iterator it = template.hlist.iterator(); it.hasNext();) {
				SIPHeader sipHdr = (SIPHeader) it.next();

				boolean found = false;
				for (Iterator it1 = this.hlist.iterator(); it1.hasNext()
						&& !found;) {
					SIPHeader sipHdr1 = (SIPHeader) it1.next();
					found = sipHdr1.match(sipHdr);
				}
				if (!found)
					return false;
			}
			return true;
		}
	}

	/**
	 * Merge this with a given template.
	 * 
	 * @param mergeHdrList
	 *            the template to merge with.
	 */

	public void merge(SIPHeaderList mergeHdrList) {
		if (mergeHdrList == null)
			return;

		Iterator it1 = this.listIterator();
		Iterator it2 = mergeHdrList.listIterator();
		while (it1.hasNext()) {
			GenericObject outerObj = (GenericObject) it1.next();
			while (it2.hasNext()) {
				Object innerObj = it2.next();
				outerObj.merge(innerObj);
			}
		}
	}

	/**
	 * make a clone of this header list.
	 * 
	 * @return clone of this Header.
	 */
	public Object clone() {
		try {
			Class clazz = this.getClass();

			Constructor cons = clazz.getConstructor((Class[])null);

			SIPHeaderList retval = (SIPHeaderList) cons.newInstance((Object[])null);

			return retval.clonehlist(this.hlist);
		} catch (Exception ex) {
			throw new RuntimeException("Could not clone!", ex);
		}
	}

	protected final SIPHeaderList clonehlist(List hlistToClone) {
		if (hlistToClone != null) {
			for (Iterator it = hlistToClone.iterator(); it.hasNext();) {
				SIPHeader h = (SIPHeader) it.next();
				this.hlist.add(h.clone());
			}
		}
		return this;
	}

	/**
	 * Get the number of headers in the list.
	 */
	public int size() {
		return hlist.size();
	}

	/**
	 * Return true if this is a header list (overrides the base class method
	 * which returns false).
	 * 
	 * @return true
	 */
	public boolean isHeaderList() {
		return true;
	}

	/**
	 * Encode the body of this header (the stuff that follows headerName). A.K.A
	 * headerValue. This will not give a reasonable result for WWW-Authenticate,
	 * Authorization, Proxy-Authenticate and Proxy-Authorization and hence this
	 * is protected.
	 */
	protected String encodeBody() {
		return encodeBody(new StringBuffer()).toString();
	}

	protected StringBuffer encodeBody(StringBuffer buffer) {
		ListIterator iterator = this.listIterator();
		while (true) {
			SIPHeader siphdr = (SIPHeader) iterator.next();
			if ( siphdr == this ) throw new RuntimeException ("Unexpected circularity in SipHeaderList");
			siphdr.encodeBody(buffer);
			// if (body.equals("")) System.out.println("BODY == ");
			if (iterator.hasNext()) {
				if (!this.headerName.equals(PrivacyHeader.NAME))
					buffer.append(Separators.COMMA);
				else 
					buffer.append(Separators.SEMICOLON);
				continue;
			} else
				break;

		}
		return buffer;
	}

	/**
	 * Add a collection of headers.
	 * 
	 * @param collection --
	 *            a collection containing the headers to add.
	 * 
	 */
	public boolean addAll(Collection collection) {
		return this.hlist.addAll(collection);
	}

	/**
	 * Add all the elements of this collection.
	 */
	public boolean addAll(int index, Collection collection) {
		return this.hlist.addAll(index, collection);
	}

	public void clear() {
		this.hlist.clear();
	}

	public boolean contains(Object header) {
		return this.hlist.contains(header);
	}

	/**
	 * Check if the list contains all the headers in this collection.
	 * 
	 * @param collection --
	 *            the collection of headers to test against.
	 */
	public boolean containsAll(Collection collection) {
		return this.hlist.containsAll(collection);
	}

	/**
	 * Get the object at the specified location.
	 * 
	 * @param index --
	 *            location from which to get the object.
	 * 
	 */
	public Object get(int index) {
		return this.hlist.get(index);
	}

	/**
	 * Return the index of a given object.
	 * 
	 * @param obj --
	 *            object whose index to compute.
	 */
	public int indexOf(Object obj) {
		return this.hlist.indexOf(obj);
	}

	/**
	 * Return the iterator to the imbedded list.
	 * 
	 * @return iterator to the imbedded list.
	 * 
	 */

	public java.util.Iterator iterator() {
		return this.hlist.listIterator();
	}

	/**
	 * Get the last index of the given object.
	 * 
	 * @param obj --
	 *            object whose index to find.
	 */
	public int lastIndexOf(Object obj) {

		return this.hlist.lastIndexOf(obj);
	}

	/**
	 * Remove the given object.
	 * 
	 * @param obj --
	 *            object to remove.
	 * 
	 */

	public boolean remove(Object obj) {

		return this.hlist.remove(obj);
	}

	/**
	 * Remove the object at a given index.
	 * 
	 * @param index --
	 *            index at which to remove the object
	 */

	public Object remove(int index) {
		return this.hlist.remove(index);
	}

	/**
	 * Remove all the elements.
	 * @see List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(java.util.Collection collection) {
		return this.hlist.removeAll(collection);
	}

	
	/**
	 * @see List#retainAll(java.util.Collection)
	 * @param collection
	 */
	public boolean retainAll(java.util.Collection collection) {
		return this.hlist.retainAll(collection);
	}

	/**
	 * Get a sublist of the list.
	 * 
	 * @see List#subList(int, int)
	 */
	public java.util.List subList(int index1, int index2) {
		return this.hlist.subList(index1, index2);

	}

	/**
	 * @see Object#hashCode()
	 * @return -- the computed hashcode.
	 */
	public int hashCode() {

		return this.headerName.hashCode();
	}

	/**
	 * Set a SIPHeader at a particular position in the list.
	 * 
	 * @see List#set(int, java.lang.Object)
	 */
	public Object set(int position, Object sipHdr) {

		return hlist.set(position, sipHdr);

	}
	
	/**
	 * Convert a list of headers to an array.
	 * 
	 * @see List#toArray(java.lang.Object[])
	 */

	public Object[] toArray(Object[] array) {

		return hlist.toArray(array);
	}

}
