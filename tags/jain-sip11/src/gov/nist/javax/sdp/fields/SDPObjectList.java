/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.LinkedList;
import java.lang.reflect.*;

public class SDPObjectList  extends GenericObjectList { 
	protected static final String SDPFIELDS_PACKAGE = 
			PackageNames.SDP_PACKAGE + ".fields";

        /**
         * Make a clone of this header list and return it.
         *For any object in the list (like SIPHeaders) that are cloneable
         *clone the object and add it to the returned List. 
	 *Strings and wrappers of basic types are 
	 *cloned by creating new objects. For other objects, if there is
	 *a clone method, then this is invoked and the cloned object
	 *appears in the result. Otherwise, this just copies the 
	 *object reference over. 
         *@since 1.0
         */
    
    public Object  clone()  {
        SDPObjectList newObject = (SDPObjectList) super.clone();
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
         * Add an sdp object to this list.
         */
    public void add (SDPObject s) { super.add(s); }
    
        /**
         * Get the input text of the sdp object (from which the object was
         * generated).
         */
    
    public SDPObjectList (String lname, String classname) {
        super(lname,classname);
        checkAssignability(SDPFIELDS_PACKAGE+".SDPObject",classname);
    }
    
    public SDPObjectList() {
        super(null, SDPFIELDS_PACKAGE + ".SDPObject");
    }
    
    public SDPObjectList(String lname) {
        super(lname, SDPFIELDS_PACKAGE + ".SDPObject");
    }
    
    public GenericObject first() {
        return (SDPObject) super.first();
    }
    
    public GenericObject next() {
        return (SDPObject) super.next();
    }
    
    public GenericObject next(ListIterator li) {
        return (SDPObject) super.next(li);
    }
	
     public String encode() {
		String retval = "";
		SDPObject sdpObject;
		for (sdpObject = (SDPObject) this.first(); 
		     sdpObject != null; 
		     sdpObject = (SDPObject) this.next() ) {
		     retval += sdpObject.encode();
		}
		return retval;
      }


     public String toString() { return this.encode(); }
    


        /**
         * Do a find and replace of objects in this list.
	 *@param objectText text of the object to find.
         *@param replacementObject object to replace the target with (
	 * in case a target is found).
	 *@param matchSubstring boolean that indicates whether to flag a
         * match when objectText is a substring of a candidate object's 
	 * encoded text.
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
         * Do a find and replace of objects in this list.
         *@since v1.0
	 *@param objectText text of the object to find.
         *@param replacementObject object to replace the target with (in
	 * case a target is found).
	 *@param matchSubstring boolean that indicates whether to flag a
         * match when objectText is a substring of a candidate object's 
	 * encoded text.
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


