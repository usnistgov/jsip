/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.header;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.reflect.*;
import gov.nist.core.*;
import javax.sip.header.*;

/**
 * Root class for all the collection objects in this list:
 * a wrapper class on the GenericObjectList class for lists of objects
 * that can appear in SIPObjects.
 * IMPORTANT NOTE: SIPObjectList cannot derive from SIPObject.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */

public class SIPObjectList extends GenericObjectList  {
    
    
    /**
     * Construct a SIPObject List given a list name.
     * @param lname String to set
     */
    public SIPObjectList( String lname) {
        super(lname);
    }
    
    /**
     * Construct a SIPObject List given a list name and a class for
     * the objects that go into the list.
     * @param lname String to set
     * @param cname Class to set
     */
    public SIPObjectList(String lname, Class cname) {
        super(lname,cname);
    }
    
    /**
     * Construct a SIPObject List given a list name and a class for
     * the objects that go into the list.
     * @param lname String to set
     * @param cname String to set
     */
    public SIPObjectList(String lname, String cname) {
        super(lname,cname);
    }
    
    /**
     * Construct an empty SIPObjectList.
     */
    public SIPObjectList() {
        super();
    }
    
    /**
     * Add a new object to the list.
     * @param obj SIPObject to set
     */
    public void add(SIPObject obj) {
        super.add((Object)obj);
    }
    
    /**
     * Add a new object to the top of this list.
     * @param obj SIPObject to set
     */
    public void addFirst(SIPObject obj) {
        super.addFirst(obj);
    }
    
    /**
     * Make a clone of this header list and return it.
     * For any object in the list (like SIPHeaders) that inherits from
     * GenericObject or GenericObjectList, clone the object and add it
     * to the returned List. For objects that do  not,
     * this just copies the reference over. WARNING.. NO CIRCULAR
     * REFERENCES that are accessible are tolerated (will throw this
     * method into an infinite loop). However, we dont (shouldnt) have such
     * worries because the parser generates tree sturctures.
     * This belongs here and not in the superclass because we need to
     * directly access protected fields.
     * @since 1.0
     * @return clone of this header
     */
    public Object  clone()  {
        SIPObjectList newObject = (SIPObjectList) super.clone();
        ListIterator li = this.listIterator();
        while(li.hasNext()) {
            Object listObj = li.next();
            Object clone_obj = makeClone(listObj);
            newObject.add(clone_obj);
            
        }
        Class myclass = this.getClass();
        Field[] fields = myclass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields [i];
            int modifier =  f.getModifiers();
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
                        int intfield = f.getInt(this);
                        f.setInt(newObject,intfield);
                    } else if (fname.compareTo("short") == 0) {
                        short shortField = f.getShort(this);
                        f.setShort(newObject,shortField);
                    } else if (fname.compareTo("char") == 0) {
                        char charField = f.getChar(this);
                        f.setChar(newObject,charField);
                    } else if (fname.compareTo("long") == 0) {
                        long longField = f.getLong(this);
                        f.setLong(newObject,longField);
                    } else if (fname.compareTo("boolean") == 0) {
                        boolean booleanField = f.getBoolean(this);
                        f.setBoolean(newObject,booleanField);
                    } else if (fname.compareTo("double") == 0) {
                        double doubleField = f.getDouble(this);
                        f.setDouble(newObject,doubleField);
                    } else if (fname.compareTo("float") == 0) {
                        float floatField = f.getFloat(this);
                        f.setFloat(newObject,floatField);
                    }
                } else {
                    Object obj = f.get(this);
                    if (obj == null) continue;
                    Object clone_obj = makeClone(obj);
                    f.set(newObject,clone_obj);
                }
            }  catch (IllegalAccessException ex1 ) {
                ex1.printStackTrace();
                continue; // we are accessing a private field...
            }
        }
        return (Object) newObject;
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
            while(it2.hasNext()) {
                Object innerObj = it2.next();
                outerObj.merge(innerObj);
            }
        }
    }
    
    /**
     * Append a given list to the end of this list.
     * @param otherList SIPObjectList to set
     */
    public void concatenate( SIPObjectList otherList) {
        super.concatenate(otherList);
    }
    
    /**
     * Append or prepend a given list to this list.
     * @param otherList SIPObjectList to set
     * @param topFlag boolean to set
     */
    public void concatenate(SIPObjectList otherList, boolean topFlag) {
        super.concatenate(otherList,topFlag);
    }
    
    /**
     * Get the first object of this list.
     * @return GenericObject
     */
    public GenericObject first() {
        return (SIPObject) super.first();
    }
    
    /**
     * Get the class of the supported objects of this list.
     * @return Class
     */
    public Class getMyClass() {
        return super.getMyClass();
    }
    
    
    /**
     * Get the next object of this list (assumes that first() has been
     * called prior to calling this method.)
     * @return GenericObject
     */
    public GenericObject next() {
        return (SIPObject) super.next();
    }
    
    /**
     * Get the next object of this list.
     * @param li ListIterator to set
     * @return GenericObject
     */
    public GenericObject next(ListIterator li) {
        return (SIPObject) super.next(li);
    }
    
    /**
     * Remove the first object of this list.
     * @return Object removed
     */
    public Object removeFirst() {
        return super.removeFirst();
    }
    
    /**
     * Remove the last object from this list.
     * @return Object removed
     */
    public Object removeLast() {
        return super.removeLast();
    }
    
    /**
     * Convert to a string given an indentation(for pretty printing).
     * This is useful for debugging the system in lieu of a debugger.
     *
     * @param indent int to set
     * @return an indentation
     */
    public String debugDump(int indent) {
        return super.debugDump(indent);
    }
    
    /**
     * Set the class of the supported objects of this list.
     *
     * @param cl Class to set
     */
    public void setMyClass( Class cl) {
        super.setMyClass(cl);
    }
    
    /**
     * Do a recursive find and replace of objects pointed to by this
     * object. You might find this handy if you are writing a proxy
     * server.
     *
     * @param objectText is the canonical string representation of
     *		the object that we want to replace.
     * @param replacementObject is the object that we want to replace it
     *	with (must be a subclass of GenericObject or GenericObjectList).
     * @param matchSubstring a boolean which tells if we should match
     * 		a substring of the target object
     * A replacement will occur if a portion of the structure is found
     * with matching encoded text as objectText and with the same class
     * as replacement.
     * (i.e. if matchSubstring is true an object is a  candidate for
     *  replacement if objectText is a substring of
     *  candidate.encode() && candidate.class.equals(replacement.class)
     * otherwise the match test is an equality test.)
     *@exception IllegalArgumentException on null args or if
     * replacementObject does not derive from GenericObject or
     * GenericObjectList
     */
    public void replace(String objectText,
    GenericObject replacementObject,
    boolean matchSubstring )
    throws IllegalArgumentException {
        
        if (objectText == null || replacementObject == null) {
            throw new IllegalArgumentException("null argument");
        }
        ListIterator listIterator = this.listIterator();
        LinkedList ll = new LinkedList();
        
        while(listIterator.hasNext()) {
            Object obj = listIterator.next();
            if (GenericObject.isMySubclass(obj.getClass())) {
                GenericObject gobj = (GenericObject)obj;
                if (gobj.getClass().equals
                (replacementObject.getClass())) {
                    if ( (!matchSubstring) &&
                    gobj.encode().compareTo(objectText) == 0 ) {
                        // Found the object that we want,
                        ll.add(obj);
                    } else if (
                    matchSubstring && gobj.encode().indexOf(objectText)
                    >= 0 ) {
                        ll.add(obj);
                    } else {
                        gobj.replace(objectText,replacementObject,
                        matchSubstring);
                    }
                }
            } else if (GenericObjectList.isMySubclass(obj.getClass())) {
                GenericObjectList gobj = (GenericObjectList)obj;
                if (gobj.getClass().equals
                (replacementObject.getClass())) {
                    if ((!matchSubstring) &&
                    gobj.encode().compareTo(objectText) == 0 ) {
                        // Found the object that we want,
                        ll.add(obj);
                    } else if (matchSubstring &&
                    gobj.encode().indexOf(objectText) >= 0 ) {
                        ll.add(obj);
                    } else {
                        gobj.replace
                        (objectText,replacementObject,matchSubstring);
                    }
                }
            }
        }
        for (int i = 0; i < ll.size(); i++ ) {
            Object obj = ll.get(i);
            this.remove(obj);
            this.add(i,(Object)replacementObject);
        }
        
    }
    
    /**
     * Do a recursive find and replace of objects pointed to by this
     * object.
     *
     * @param objectText is the canonical string representation of
     *		the object that we want to replace.
     * @param replacementObject is the object that we want to replace it
     *	with (must be a subclass of GenericObject or GenericObjectList).
     * @param matchSubstring a boolean which tells if we should match
     * 		a substring of the target object
     * A replacement will occur if a portion of the structure is found
     * with matching encoded text as objectText and with the same class
     * as replacement.
     * (i.e. if matchSubstring is true an object is a  candidate for
     *  replacement if objectText is a substring of
     *  candidate.encode() && candidate.class.equals(replacement.class)
     * otherwise the match test is an equality test.)
     *@exception IllegalArgumentException on null args or if
     * replacementObject does not derive from GenericObject or
     * GenericObjectList
     */
    public void replace(String objectText,
    GenericObjectList replacementObject,
    boolean matchSubstring )
    throws IllegalArgumentException {
        if (objectText == null || replacementObject == null) {
            throw new IllegalArgumentException("null argument");
        }
        
        ListIterator listIterator = this.listIterator();
        LinkedList ll = new LinkedList();
        
        while(listIterator.hasNext()) {
            Object obj = listIterator.next();
            if (GenericObject.isMySubclass(obj.getClass())) {
                GenericObject gobj = (GenericObject)obj;
                if (gobj.getClass().equals
                (replacementObject.getClass())) {
                    if ( (!matchSubstring) &&
                    gobj.encode().compareTo(objectText) == 0 ) {
                        // Found the object that we want,
                        ll.add(obj);
                    } else if (matchSubstring &&
                    gobj.encode().indexOf(objectText) >= 0) {
                        ll.add(obj);
                    } else {
                        gobj.replace
                        (objectText,replacementObject,matchSubstring);
                    }
                }
            } else if (GenericObjectList.isMySubclass(obj.getClass())) {
                GenericObjectList gobj = (GenericObjectList)obj;
                if (gobj.getClass().equals
                (replacementObject.getClass())) {
                    if ( (!matchSubstring) &&
                    gobj.encode().compareTo(objectText) == 0 ) {
                        // Found the object that we want,
                        ll.add(obj);
                    } else if (matchSubstring &&
                    gobj.encode().indexOf(objectText) >= 0) {
                        ll.add(obj);
                    } else {
                        gobj.replace
                        (objectText,replacementObject,matchSubstring);
                    }
                    
                }
            }
        }
        for (int i = 0; i < ll.size(); i++ ) {
            Object obj = ll.get(i);
            this.remove(obj);
            this.add(i,(Object)replacementObject);
        }
        
    }
    
}
