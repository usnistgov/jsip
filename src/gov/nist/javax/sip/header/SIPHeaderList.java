/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/
package gov.nist.javax.sip.header;

import java.util.ListIterator;
import java.util.LinkedList;
import gov.nist.core.*;

/**
 *  This is the root class for all lists of SIP headers.
 *  It imbeds a SIPObjectList object and extends SIPHeader
 *  Lists of ContactSIPObjects etc. derive from this class.
 *  This supports homogeneous  lists (all elements in the list are of
 *  the same class). We use this for building type homogeneous lists of
 *  SIPObjects that appear in SIPHeaders
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:50 $
 */
public class SIPHeaderList extends SIPHeader implements java.util.List {

	protected static final String SIPHEADERS_PACKAGE =
		PackageNames.SIPHEADERS_PACKAGE;
	protected static final String SIP_PACKAGE = PackageNames.SIP_PACKAGE;

	/** hlist field.
	 */
	protected SIPObjectList hlist;

	/** Constructor
	 * @param hl SIPObjectList to set
	 * @param hname String to set
	 */
	public SIPHeaderList(SIPObjectList hl, String hname) {
		super(hname);
		hlist = hl;
	}

	/** Constructor
	 * @param hName String to set
	 */
	public SIPHeaderList(String hName) {
		super(hName);
		hlist = new SIPObjectList(null);
	}

	/**
	 * Constructor
	 * @param objclass Class to set
	 * @param hname String to set
	 */
	public SIPHeaderList(Class objclass, String hname) {
		super(hname);
		hlist = new SIPObjectList(hname, objclass);
	}

	/**
	 * Constructor
	 * @param classname String to set
	 * @param hname String to set
	 */
	public SIPHeaderList(String classname, String hname) {
		super(hname);
		hlist = new SIPObjectList(hname, classname);
	}

	/**
	 * Concatenate the list of stuff that we are keeping around and also
	 * the text corresponding to these structures (that we parsed).
	 * @param objectToAdd GenericObject to set
	 */
	public boolean add(Object objectToAdd) {
		return hlist.add(objectToAdd);
	}

	/**
	 * Concatenate the list of stuff that we are keeping around and also
	 * the text corresponding to these structures (that we parsed).
	 * @param obj Genericobject to set
	 */
	public void addFirst(Object obj) {
		hlist.addFirst(obj);
	}

	/** 
	 * Add to this list.
	 * @param sipheader SIPHeader to add.
	 * @param top is true if we want to add to the top of the list.
	 */
	public void add(SIPHeader sipheader, boolean top) {
		if (top)
			this.addFirst(sipheader);
		else
			this.add(sipheader);
	}

	/**
	 * Concatenate two compatible lists. This appends or prepends the new list
	 * to the end of this list.
	 * @param other SIPHeaderList to set
	 * @param top boolean to set
	 * @throws IllegalArgumentException if the two lists are not compatible
	 */
	public void concatenate(SIPHeaderList other, boolean top)
		throws IllegalArgumentException {
		if (!hlist.getMyClass().equals(other.hlist.getMyClass()))
			throw new IllegalArgumentException(
				"SIPHeaderList concatenation "
					+ hlist.getMyClass().getName()
					+ "/"
					+ other.hlist.getMyClass().getName());
		hlist.concatenate(other.hlist, top);
	}

	/**
	 * Concatenate two compatible lists. This appends  the new list to the end
	 * of this list (which is the most common mode for this operation).
	 * @param other SIPHeaderList
	 * @throws IllegalArgumentException if the two lists are not compatible
	 */
	public void concatenate(SIPHeaderList other)
		throws IllegalArgumentException {
		this.concatenate(other, false);
	}

	/**
	 * Encode a list of sip headers.
	 * Headers are returned in cannonical form.
	 * @return String encoded string representation of this list of
	 * 	 headers. (Contains string append of each encoded header).
	 */
	public String encode() {
		if (hlist.isEmpty())
			return headerName + ":" + NEWLINE;
		StringBuffer encoding = new StringBuffer();
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
				encoding.append(sipheader.encode());
			}
			return encoding.toString();
		} else {
			// These can be concatenated together in an comma separated
			// list.
			return headerName + COLON + SP + this.encodeBody() + NEWLINE;
		}
	}

	/** Return a list of encoded strings (one for each sipheader).
	 *@return LinkedList containing encoded strings in this header list.
	 *	an empty list is returned if this header list contains no
	 *	sip headers.
	 */
	public LinkedList getHeadersAsEncodedStrings() {
		LinkedList retval = new LinkedList();

		ListIterator li = hlist.listIterator();
		while (li.hasNext()) {
			SIPHeader sipheader = (SIPHeader) li.next();
			retval.add(sipheader.encode());

		}

		return retval;

	}

	/**
	 * Initialize the iterator for a loop
	 * @return SIPObject first element of the list.
	 */
	public SIPObject first() {
		return (SIPObject) this.hlist.first();
	}

	/**
	 * Get the first element of this list.
	 * @return SIPHeader first element of the list.
	 */
	public SIPHeader getFirst() {
		if (hlist == null || hlist.isEmpty())
			return null;
		else
			return (SIPHeader) hlist.getFirst();
	}

	/**
	 * Get the last element of this list.
	 * @return SIPHeader last element of the list.
	 */
	public SIPHeader getLast() {
		if (hlist == null || hlist.isEmpty())
			return null;
		return (SIPHeader) hlist.getLast();
	}

	/**
	 * Get the class for the headers of this list.
	 * @return Class  of header supported by this list.
	 */
	public Class getMyClass() {
		return hlist.getMyClass();
	}

	/**
	 * Empty check
	 * @return boolean true if list is empty
	 */
	public boolean isEmpty() {
		return hlist.isEmpty();
	}

	/**
	 * Get an initialized iterator for my imbedded list
	 * @return the generated ListIterator
	 */
	public ListIterator listIterator() {
		return hlist.listIterator(0);
	}

	/** Get the imbedded linked list.
	 *@return the imedded linked list of SIP headers.
	 */
	public SIPObjectList getHeaderList() {
		return this.hlist;
	}

	/** Get the list iterator for a given position.
	 *@param position position for the list iterator to return
	 *@return the generated list iterator
	 */
	public ListIterator listIterator(int position) {
		return hlist.listIterator(position);
	}

	/**
	 * Get the next element in the list .
	 * This is not thread safe and cannot handle nesting
	 * @return SIPObject next object in this list.
	 */
	public SIPObject next() {
		return (SIPObject) this.hlist.next();
	}

	/**
	 * Get the next item for an iterative scan of the list
	 * @param iterator ListIterator
	 * @return SIPObject next object in this list.
	 */
	public SIPObject next(ListIterator iterator) {
		return (SIPObject) this.hlist.next(iterator);
	}

	/**
	 * Remove all occurances of a given class of SIPObject from
	 * the SIP object list.
	 * @param cl Class to set
	 */
	public void removeAll(Class cl) {
		LinkedList ll = new LinkedList();
		for (SIPHeader sh = (SIPHeader) hlist.first();
			sh != null;
			sh = (SIPHeader) hlist.next()) {
			if (sh.getClass().equals(cl)) {
				ll.add(sh);
			}
		}
		ListIterator li = ll.listIterator();
		while (li.hasNext()) {
			SIPHeader sh = (SIPHeader) li.next();
			hlist.remove(sh);
		}
	}

	/**
	 *Remove the first element of this list.
	 */
	public void removeFirst() {
		if (hlist.size() != 0)
			hlist.removeFirst();

	}

	/**
	 *Remove the last element of this list.
	 */
	public void removeLast() {
		if (hlist.size() != 0)
			hlist.removeLast();
	}

	/**
	 * Remove a sip header from this list of sip headers.
	 * @param obj SIPHeader to set
	 * @return boolean
	 */
	public boolean remove(SIPHeader obj) {
		if (hlist.size() == 0)
			return false;
		else
			return hlist.remove(obj);
	}

	/**
	 * Set the root class for all objects inserted into my list
	 * (for assertion check)
	 * @param cl class to set
	 */
	protected void setMyClass(Class cl) {
		hlist.setMyClass(cl);
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
		sprint(indent + hlist.debugDump(indentation));
		sprint(indent + "}");
		return stringRepresentation;
	}

	/** convert to a string representation
	 * @return String
	 */
	public String debugDump() {
		return debugDump(0);
	}

	/**
	 * Array conversion.
	 * @return SIPHeader []
	 */
	public Object[] toArray() {
		SIPHeader retval[] = new SIPHeader[hlist.size()];
		return (SIPHeader[]) hlist.toArray(retval);
	}

	/** index of an element.
	 *@return index of the given element (-1) if element does not exist.
	 */
	public int indexOf(GenericObject gobj) {
		return hlist.indexOf(gobj);
	}

	/** insert at a location.
	 *@param index location where to add the sipHeader.
	 *@param sipHeader SIPHeader structure to add.
	 */

	public void add(int index, SIPHeader sipHeader)
		throws IndexOutOfBoundsException {
		hlist.add(index, sipHeader);
	}

	/**
	 * Equality comparison operator.
	 *@param other the other object to compare with. true is returned
	 * iff the classes match and list of headers herein is equal to
	 * the list of headers in the target (order of the headers is
	 * not important).
	 */
	public boolean equals(Object other) {

		if (!this.getClass().equals(other.getClass()))
			return false;
		SIPHeaderList that = (SIPHeaderList) other;
		if (this.hlist == that.hlist)
			return true;
		else if (this.hlist == null)
			return false;
		return this.hlist.equals(that.hlist);

	}

	/**
	 * Template match against a template.
	 * null field in template indicates wild card match.
	 */
	public boolean match(Object template) {
		if (template == null)
			return true;
		if (!this.getClass().equals(template.getClass()))
			return false;
		SIPHeaderList that = (SIPHeaderList) template;
		if (this.hlist == that.hlist)
			return true;
		else if (this.hlist == null)
			return false;
		else
			return this.hlist.match(that.hlist);
	}

	/** 
	 *Merge this with a given template.
	 *
	 *@param mergeObject the template to merge with.
	 */

	public void merge(Object mergeObject) {
		if (mergeObject == null)
			return;
		if (!mergeObject.getClass().equals(this.getClass()))
			throw new IllegalArgumentException("Bad class " + this.getClass());
		SIPHeaderList mergeHdrList = (SIPHeaderList) mergeObject;
		hlist.mergeObjects(mergeHdrList.hlist);
	}

	/**
	 * make a clone of this header list.
	 * @return clone of this Header.
	 */
	public Object clone() {
		SIPHeaderList retval = (SIPHeaderList) super.clone();
		if (this.hlist != null)
			retval.hlist = (SIPObjectList) this.hlist.clone();
		return retval;
	}
	/**
	 * Get the number of headers in the list.
	 */
	public int size() {
		return hlist.size();
	}

	/** Return true if this is a header list
	 * (overrides the base class method which returns false).
	 *@return true
	 */
	public boolean isHeaderList() {
		return true;
	}

	/** Encode the body of this header (the stuff that follows headerName).
	 * A.K.A headerValue. This will not give a reasonable result for
	 *WWW-Authenticate, Authorization, Proxy-Authenticate and
	 *Proxy-Authorization and hence this is protected.
	 */
	protected String encodeBody() {

		StringBuffer encoding = new StringBuffer();
		ListIterator iterator = this.listIterator();
		while (true) {
			SIPHeader siphdr = (SIPHeader) iterator.next();
			encoding.append(siphdr.encodeBody());
			if (iterator.hasNext()) {
				encoding.append(COMMA);
				continue;
			} else
				break;
		}
		return encoding.toString();
	}

	/** Encode this to a string representation. 
	 *
	 * This is an alias to the encode function above.
	 */
	public String toString() {
		return this.encode();
	}

	/** Add an element at a specified position.
	 *
	 *@param headerToAdd -- the header to add.
	 */
	public void add(int position, Object headerToAdd) {
		this.hlist.add(position, headerToAdd);

	}

	/** Add a collection of headers.
	 *
	 *@param collection -- a collection containing the headers to add.
	 */
	public boolean addAll(java.util.Collection collection) {
		return this.hlist.add(collection);
	}

	/** Add all the elements of this collection.
	 */
	public boolean addAll(int index, java.util.Collection collection) {
		return this.hlist.addAll(index, collection);
	}

	public void clear() {
		this.hlist.clear();
	}

	public boolean contains(Object header) {
		return this.hlist.contains(header);
	}

	/** Check if the list contains all the headers in this collection.
	 *
	 *@param collection -- the collection of headers to test against.
	 */
	public boolean containsAll(java.util.Collection collection) {
		return this.hlist.containsAll(collection);
	}

	/** Get the object at the specified location.
	 *
	 *@param index -- location from which to get the object.
	 *
	 */
	public Object get(int index) {
		return this.hlist.get(index);
	}

	/** Return the index of a given object.
	 *
	 *@param obj -- object whose index to compute.
	 */
	public int indexOf(Object obj) {
		return this.hlist.indexOf(obj);
	}

	/** Return the iterator to the imbedded list.
	 *
	 *@return iterator to the imbedded list.
	 *
	 */

	public java.util.Iterator iterator() {
		return this.hlist.listIterator();
	}

	/** Get the last index of the given object.
	 *
	 *@param obj -- object whose index to find.
	 */
	public int lastIndexOf(Object obj) {
		if (this.getMyClass() != null
			&& !obj.getClass().equals(this.getMyClass()))
			return -1;
		return this.hlist.lastIndexOf(obj);
	}

	/** Remove the given object.
	 *
	 *@param obj -- object to remove.
	 *
	 */

	public boolean remove(Object obj) {
		if (this.getMyClass() != null
			&& !obj.getClass().equals(this.getMyClass()))
			return false;
		return this.hlist.remove(obj);
	}

	/** Remove the object at a given index.
	 *@param index -- index at which to remove the object
	 */

	public Object remove(int index) {
		return this.hlist.remove(index);
	}

	/** Remove all the elements.
	 */
	public boolean removeAll(java.util.Collection collection) {
		return this.hlist.removeAll(collection);
	}

	public boolean retainAll(java.util.Collection collection) {
		return this.hlist.retainAll(collection);
	}

	public Object set(int index, Object obj) {
		if (this.getMyClass() != null
			&& !obj.getClass().equals(this.getMyClass())) {
			throw new IllegalArgumentException(
				"bad class expecting " + this.getMyClass());
		}
		return this.hlist.set(index, obj);
	}

	public java.util.List subList(int index1, int index2) {
		return this.hlist.subList(index1, index2);

	}

	public Object[] toArray(Object[] obj) {
		return this.hlist.toArray();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:29  sverker
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
