/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.address;
import gov.nist.core.*;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.reflect.*;

/**
* Root class for all the collection objects in this list:
* a wrapper class on the GenericObjectList class for lists of objects
* that can appear in NetObjects.
* IMPORTANT NOTE: NetObjectList cannot derive from NetObject as this 
* will screw up the way in which we attach objects to headers.
*
*@version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:47 $
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class NetObjectList extends GenericObjectList {

	/**
	 * Construct a NetObject List given a list name.
	 * @param lname String to set
	 */
	public NetObjectList(String lname) {
		super(lname);
	}

	/**
	 * Construct a NetObject List given a list name and a class for
	 * the objects that go into the list.
	 * @param lname String to set
	 * @param cname Class to set
	 */
	public NetObjectList(String lname, Class cname) {
		super(lname, cname);
	}

	/**
	 * Construct a NetObject List given a list name and a class for
	 * the objects that go into the list.
	 * @param lname String to set
	 * @param cname String to set
	 */
	public NetObjectList(String lname, String cname) {
		super(lname, cname);
	}

	/**
	 * Construct an empty NetObjectList.
	 */
	public NetObjectList() {
		super();
	}

	/**
	 * Add a new object to the list.
	 * @param obj NetObject to set
	 */
	public void add(NetObject obj) {
		super.add(obj);
	}

	/** concatenate the two Lists
	 * @param net_obj_list NetObjectList to set
	 */
	public void concatenate(NetObjectList net_obj_list) {
		super.concatenate(net_obj_list);
	}

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
		if (!mergeList.getMyClass().equals(this.getMyClass()))
			throw new IllegalArgumentException("class mismatch");
		Iterator it1 = this.listIterator();
		Iterator it2 = mergeList.listIterator();
		while (it1.hasNext()) {
			GenericObject outerObj = (GenericObject) it1.next();
			while (it2.hasNext()) {
				Object innerObj = it2.next();
				outerObj.merge(innerObj);
			}
		}
	}

	/** returns the first element
	 * @return GenericObject
	 */
	public GenericObject first() {
		return (NetObject) super.first();
	}

	/**
	 * Get the class for all objects in my list.
	 * @return Class
	 */
	public Class getMyClass() {
		return super.getMyClass();
	}

	/** returns the next element
	 * @return GenericObject
	 */
	public GenericObject next() {
		return (NetObject) super.next();
	}

	/** returns the next element
	 * @param li ListIterator to set
	 * @return GenericObject
	 */
	public GenericObject next(ListIterator li) {
		return (NetObject) super.next(li);
	}

	/**
	 * Do a recursive find and replace of objects.
	*@param objectText text of the object to find.
	 *@param replacementObject object to replace the target with (
	* in case a target is found).
	*@param matchSubstring boolean that indicates whether to flag a
	 * match when objectText is a substring of a candidate object's 
	* encoded text.
	*@exception IllegalArgumentException on null args and if replacementObject
	* does not derive from GenericObject or GenericObjectList
	 */
	public void replace(
		String objectText,
		GenericObject replacementObject,
		boolean matchSubstring)
		throws IllegalArgumentException {

		if (objectText == null || replacementObject == null) {
			throw new IllegalArgumentException("null argument");
		}
		ListIterator listIterator = this.listIterator();
		LinkedList ll = new LinkedList();

		while (listIterator.hasNext()) {
			Object obj = listIterator.next();
			if (GenericObject.isMySubclass(obj.getClass())) {
				GenericObject gobj = (GenericObject) obj;
				if (gobj.getClass().equals(replacementObject.getClass())) {
					if ((!matchSubstring)
						&& gobj.encode().compareTo(objectText) == 0) {
						// Found the object that we want,
						ll.add(obj);
					} else if (
						matchSubstring
							&& gobj.encode().indexOf(objectText) >= 0) {
						ll.add(obj);
					} else {
						gobj.replace(
							objectText,
							replacementObject,
							matchSubstring);
					}
				}
			} else if (GenericObjectList.isMySubclass(obj.getClass())) {
				GenericObjectList gobj = (GenericObjectList) obj;
				if (gobj.getClass().equals(replacementObject.getClass())) {
					if ((!matchSubstring)
						&& gobj.encode().compareTo(objectText) == 0) {
						// Found the object that we want,
						ll.add(obj);
					} else if (
						matchSubstring
							&& gobj.encode().indexOf(objectText) >= 0) {
						ll.add(obj);
					} else {
						gobj.replace(
							objectText,
							replacementObject,
							matchSubstring);
					}
				}
			}
		}
		for (int i = 0; i < ll.size(); i++) {
			Object obj = ll.get(i);
			this.remove(obj);
			this.add(i, (Object) replacementObject);
		}

	}

	/**
	 * Do a recursive find and replace of objects in this list.
	 *@since v1.0
	*@param objectText text of the object to find.
	 *@param replacementObject object to replace the target with (in
	* case a target is found).
	*@param matchSubstring boolean that indicates whether to flag a
	 * match when objectText is a substring of a candidate object's 
	* encoded text.
	*@exception IllegalArgumentException on null args and if replacementObject
	* does not derive from GenericObject or GenericObjectList
	 */
	public void replace(
		String objectText,
		GenericObjectList replacementObject,
		boolean matchSubstring)
		throws IllegalArgumentException {
		if (objectText == null || replacementObject == null) {
			throw new IllegalArgumentException("null argument");
		}

		ListIterator listIterator = this.listIterator();
		LinkedList ll = new LinkedList();

		while (listIterator.hasNext()) {
			Object obj = listIterator.next();
			if (GenericObject.isMySubclass(obj.getClass())) {
				GenericObject gobj = (GenericObject) obj;
				if (gobj.getClass().equals(replacementObject.getClass())) {
					if ((!matchSubstring)
						&& gobj.encode().compareTo(objectText) == 0) {
						// Found the object that we want,
						ll.add(obj);
					} else if (
						matchSubstring
							&& gobj.encode().indexOf(objectText) >= 0) {
						ll.add(obj);
					} else {
						gobj.replace(
							objectText,
							replacementObject,
							matchSubstring);
					}
				}
			} else if (GenericObjectList.isMySubclass(obj.getClass())) {
				GenericObjectList gobj = (GenericObjectList) obj;
				if (gobj.getClass().equals(replacementObject.getClass())) {
					if ((!matchSubstring)
						&& gobj.encode().compareTo(objectText) == 0) {
						// Found the object that we want,
						ll.add(obj);
					} else if (
						matchSubstring
							&& gobj.encode().indexOf(objectText) >= 0) {
						ll.add(obj);
					} else {
						gobj.replace(
							objectText,
							replacementObject,
							matchSubstring);
					}

				}
			}
		}
		for (int i = 0; i < ll.size(); i++) {
			Object obj = ll.get(i);
			this.remove(obj);
			this.add(i, (Object) replacementObject);
		}
	}

	/** set the class
	 * @param cl Class to set
	 */
	public void setMyClass(Class cl) {
		super.setMyClass(cl);
	}

	/**
	 * Convert to a string given an indentation(for pretty printing).
	 * @param indent int to set
	 * @return String
	 */
	public String debugDump(int indent) {
		return super.debugDump(indent);
	}

	/** 
	* Encode this to a string.
	*
	*@return a string representation for this object.
	*/
	public String toString() {
		return this.encode();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
