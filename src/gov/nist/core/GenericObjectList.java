/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.core;
import java.util.*;
import java.lang.reflect.*;
import java.io.Serializable;

/**
* Implements a homogenous consistent linked list.   All the objects in
* the linked list  must derive from the same root class. This is a useful
* constraint to place on  our code as this property is invariant.The
* list is created with the superclass which can be specified as either
* a class name or a Class.  
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public abstract class GenericObjectList extends  
LinkedList implements Serializable { 
    // Useful constants.
    protected static final String SEMICOLON= Separators.SEMICOLON;
    protected static final String COLON= Separators.COLON;
    protected static final String COMMA= Separators.COMMA;
    protected static final String SLASH= Separators.SLASH;
    protected static final String SP=   Separators.SP;
    protected static final String EQUALS= Separators.EQUALS;
    protected static final String STAR= Separators.STAR;
    protected static final String NEWLINE = Separators.NEWLINE;
    protected static final String RETURN= Separators.RETURN;
    protected static final String LESS_THAN = Separators.LESS_THAN;
    protected static final String GREATER_THAN = Separators.GREATER_THAN;
    protected static final String AT =  Separators.AT;
    protected static final String DOT = Separators.DOT;
    protected static final String QUESTION = Separators.QUESTION;
    protected static final String POUND = Separators.POUND;
    protected static final String AND = Separators.AND;
    protected static final String LPAREN = Separators.LPAREN;
    protected static final String RPAREN = Separators.RPAREN;
    protected static final String DOUBLE_QUOTE = Separators.DOUBLE_QUOTE;
    protected static final String QUOTE = Separators.QUOTE;
    protected static final String HT = Separators.HT;
    protected static final String PERCENT = Separators.PERCENT;
    
    protected int  indentation;
    protected String listName; // For debugging
    private ListIterator myListIterator;
    private String stringRep;
    protected Class myClass;
    protected String separator;
    
    protected String getIndentation() {
        String retval = "";
        for (int i = 0; i < indentation; i++) retval += " ";
        return retval;
    }

    /** Return true if this supports reflection based cloning.
     */
    protected static boolean isCloneable(Object obj) {
        return obj instanceof GenericObject ||
        obj instanceof GenericObjectList;
    }
    
    
    
        public static boolean isMySubclass(Class other) {
            try {
               return GenericObjectList.class.isAssignableFrom(other);
            } catch ( Exception ex) {
                InternalErrorHandler.handleException(ex);
            }
	    return false;
	}
    
        /**
         *Make a clone of the given object.
         *Cloning rules are as follows:
         *Strings and wrapped basic types are cloned.
         *If the object supports a clone method then it is called
         *Otherwise the original object is returned.
         */
    protected static Object makeClone( Object obj) {
        Object clone_obj = obj;
        if (obj instanceof String ) {
            String string = (String) obj;
            clone_obj = (Object) new String(string);
        } else if (obj instanceof Integer) {
            clone_obj = new Integer(((Integer)obj).intValue());
        } else if ( obj instanceof Float ) {
            clone_obj = new Float(((Float) obj).floatValue());
        } else if (obj instanceof Double) {
            clone_obj = new Double(((Double) obj).doubleValue());
        } else if (obj instanceof Long) {
            clone_obj = new Long(((Long)obj).longValue());
        } else {
            // If a clone method exists for the object, then
            // invoke it
            try {
                Class cl = obj.getClass();
                Method meth = cl.getMethod("clone",null);
                clone_obj = meth.invoke(obj,null);
            } catch (SecurityException ex) {
                clone_obj = obj;
            } catch (IllegalArgumentException ex) {
                InternalErrorHandler.handleException(ex);
            } catch (IllegalAccessException ex) {
                clone_obj = obj;
            } catch (InvocationTargetException ex) {
                clone_obj = obj;
            } catch (NoSuchMethodException ex) {
                clone_obj = obj;
            }
        }
        return clone_obj;
    }
    
    
        /**
         * Implement the clone method.
         */
    public Object clone() {
        Class myclass = this.getClass();
        Object newObject = null;
        try {
            newObject = myclass.newInstance();
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
        GenericObjectList gobj = (GenericObjectList) newObject;
        gobj.clear();
        gobj.separator = this.separator;
        gobj.myClass = this.myClass;
        gobj.listName = new String(this.listName);
        return newObject;
    }
    
    
    
        /**
         * Sets the class that all our elements derive from.
         */
    public Class getMyClass() {
        return myClass;
    }
    
    public void setMyClass( Class cl ) {
        myClass = cl;
    }
    
    protected GenericObjectList() {
        super();
        listName = null;
        stringRep = "";
        separator = ";";
    }
    
    
    protected GenericObjectList (String lname ) {
	this();
        listName = lname;
    }
    
        /**
         * A Constructor which takes a list name and a
         * class name (for assertion checking).
         */
    
    protected GenericObjectList (String lname, String classname) {
	this(lname);
        try  {
            myClass = Class.forName(classname);
        } catch (ClassNotFoundException ex) {
            InternalErrorHandler.handleException(ex);
        }
        
    }
    
        /**
         * A Constructor which takes a list name and a class
         * (for assertion checking).
         */
    
    protected GenericObjectList (String lname, Class objclass) {
	this(lname);
        myClass = objclass;
    }
    
    
    
    
    
        /**
         *  Traverse the list given a list iterator
         */
    protected GenericObject next( ListIterator iterator) {
        try {
            return  (GenericObject) iterator.next();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
    
    
        /**
         *  This is the default list iterator.This will not handle
         * nested list traversal.
         */
    protected GenericObject first() {
        myListIterator = this.listIterator(0);
        try {
            return (GenericObject) myListIterator.next();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
    
        /**
         *  Fetch the next object from the list based on
         *  the default list iterator
         */
    protected GenericObject next()
    {
        if (myListIterator == null) {
            myListIterator = this.listIterator(0);
        }
        try {
            return  (GenericObject) myListIterator.next();
        } catch (NoSuchElementException ex) {
            myListIterator = null;
            return null;
        }
    }
    
        /**
         * Concatenate two compatible header lists, adding the argument to the
         * tail end of this list.
         * @param <var> topFlag </var> set to true to add items to top of list
         */
    protected void concatenate( GenericObjectList objList ) {
        concatenate(objList,false);
    }
    
        /**
         * Concatenate two compatible header lists, adding the argument 
	 * either to the beginning
         * or the tail end of this list.
         * A type check is done before concatenation.
         * @param <var> topFlag </var> set to true to add items to top of 
	 *  	list else add them to the tail end of the list.
         */
    protected void concatenate( GenericObjectList objList, boolean topFlag ) {
        if (! topFlag ) {
            this.addAll(objList);
        } else {
            // add given items to the top end of the list.
            this.addAll(0,objList);
        }
    }
    
    
    
    /**
     *Get the list iterator for this list.
     */
    public Iterator getIterator() { return this.listIterator(); }
    
        /**
         * string formatting function.
         */
    
    private void sprint( String s ) {
        if (s == null) {
            stringRep += getIndentation();
            stringRep += "<null>\n";
            return;
        }
        
        if (s.compareTo("}") == 0 || s.compareTo("]") == 0 ) {
            indentation--;
        }
        stringRep += getIndentation();
        stringRep += s;
        stringRep += "\n";
        if (s.compareTo("{") == 0 || s.compareTo("[") == 0 ) {
            indentation++;
        }
    }
    
        /**
         * Convert this list of headers to a formatted string.
         */
    
    public String debugDump() {
        stringRep = "";
        Object obj = this.first();
        if (obj == null) return "<null>";
        sprint("listName:");
        sprint(listName);
        sprint("{");
        while(obj != null) {
            sprint("[");
            try {
                if ( Class.forName
                (PackageNames.CORE_PACKAGE + ".GenericObjectList").
                isAssignableFrom(obj.getClass()) ) {
                    sprint(((GenericObjectList) obj).
                    debugDump(this.indentation));
                } else
                    if ( Class.forName(PackageNames.CORE_PACKAGE+
		    ".GenericObject").isAssignableFrom(obj.getClass()))  {
                        sprint(((GenericObject) obj).
                        debugDump(this.indentation));
                    }
            } catch ( ClassNotFoundException ex) {
                InternalErrorHandler.handleException(ex);
            }
            obj = next();
            sprint("]");
        }
        sprint("}");
        return stringRep;
    }
    
    
        /**
         * Convert this list of headers to a string
         * (for printing) with an indentation given.
         */
    
    public  String debugDump( int indent) {
        int save = indentation;
        indentation = indent;
        String retval =  this.debugDump();
        indentation = save;
        return retval;
    }
    
    public  boolean add (Object obj) {
        if (myClass == null) {
            myClass = obj.getClass();
            return super.add(obj);
        } else {
            Class newclass = obj.getClass();
            if ( ! myClass.isAssignableFrom(newclass)) {
		throw new IllegalArgumentException
                ("Class mismatch list insertion  " +
                listName + " " +
                newclass.getName() + "/" + myClass.getName());
            }
            return super.add(obj);
        }
    }


	public void addFirst(Object objToAdd) {
	     if (myClass == null)  {
		myClass = objToAdd.getClass();
            } else {
               Class newclass = objToAdd.getClass();
               if ( ! myClass.isAssignableFrom(newclass)) {
		  throw new IllegalArgumentException
                  ("Class mismatch list insertion  " +
                  listName + " " +
                  newclass.getName() + "/" + myClass.getName());
               }
               super.addFirst(objToAdd);
	   }
	}

    
        /**
         *  Type checked add operation.
         *  All objects in this list are assignable from a common
         *  superclass.  If the class is already set, then the new one
         *  is just compared with the existing class objects. Otherwise
         *  the first object that is added determines the class of the
         *  objects in the list.
         */
    
    protected  void add (GenericObject obj) {
        if (myClass == null) {
            myClass = obj.getClass();
            super.add(obj);
        } else {
            Class newclass = obj.getClass();
            if ( ! myClass.isAssignableFrom(newclass)) {
		 throw new IllegalArgumentException
                ("Class mismatch list insertion  " +
                listName + " " +
                newclass.getName() + "/" + myClass.getName());
            }
            super.add(obj);
        }
    }
    
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

        /**
         * Do a find and replace of objects in this list.
	 *@param regexp regular expression to match with the canonical
	 *		text we want to replace.
         *@param replacementObject object to replace the target with (in
	 * case a target is found).
         */
    public void replace(Match regexp,
    	GenericObjectList replacementObject) 
        throws IllegalArgumentException {
	if (regexp == null || replacementObject == null) {
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
		    if (regexp.match(gobj.encode())) {
			 ll.add(obj);
		    } else {
                        gobj.replace(regexp,replacementObject);
                    }   
		}
            } else if (GenericObjectList.isMySubclass(obj.getClass())) {
                GenericObjectList gobj = (GenericObjectList)obj;
                if (gobj.getClass().equals
                (replacementObject.getClass())) {
		    if (regexp.match(gobj.encode())) {
			 ll.add(obj);
		    } else {
                        gobj.replace(regexp,replacementObject);
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
	 *@param regexp match regular expression of the object to find.
	 * this is generated using the org.apache.regexp package.
         *@param replacementObject object to replace the target with (
	 * in case a target is found).
         */
    public void replace(Match regexp, GenericObject replacementObject)
	throws IllegalArgumentException {
        
	if (regexp == null || replacementObject == null) {
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
		    if (regexp.match(gobj.encode())) {
			 ll.add(obj);
		    } else {
                        gobj.replace(regexp,replacementObject);
                    }   
                }
            } else if (GenericObjectList.isMySubclass(obj.getClass())) {
                GenericObjectList gobj = (GenericObjectList)obj;
                if (gobj.getClass().equals
                (replacementObject.getClass())) {
		    if (regexp.match(gobj.encode())) {
		        ll.add(obj);
                    } else {
                         gobj.replace(regexp,replacementObject);
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
	
	    if (mergeList == null) return;
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
         * Encode the list in semicolon separated form.
	 * @return an encoded string containing the objects in this list.
         * @since v1.0
         */
    public String encode() {
        if (this.isEmpty()) return "";
        StringBuffer encoding = new StringBuffer();
        ListIterator iterator = this.listIterator();
        if (iterator.hasNext()) {
            while(true) {
                Object obj = iterator.next();
                if (obj instanceof GenericObject) {
                    GenericObject gobj = (GenericObject) obj;
                    encoding.append(gobj.encode());
                } else {
                    encoding.append(obj.toString());
                }
                if (iterator.hasNext()) encoding.append(separator);
                else break;
            }
        }
        return encoding.toString();
    }

	/** Alias for the encode function above.
	*/
 	public String toString() { 
		return this.encode(); 
	}
    
        /**
         *  Set the separator (for encoding the list)
         * @since v1.0
         * @param sep is the new seperator (default is semicolon)
         */
    public void setSeparator (String sep ) {
        separator = sep;
    }


	/**
	* Equality checking predicate.
	*@param that is the object to compare ourselves to.
	*/
     public boolean equals(Object other) {
	if ( ! this.getClass().equals(other.getClass()) ) return false;
	GenericObjectList that = (GenericObjectList) other;
	if (  this.size() != that.size()) return false;
	ListIterator myIterator = this.listIterator();
	while (myIterator.hasNext()) {
	    Object myobj = myIterator.next();
	    ListIterator hisIterator = that.listIterator();
	    try {
	      while(true) {
		Object hisobj = hisIterator.next();
		if (myobj.equals(hisobj)) break;
	      }
	    } catch (NoSuchElementException ex) {
		return false;
	    }
	}
	ListIterator hisIterator = that.listIterator();
	while (hisIterator.hasNext()) {
	    Object hisobj =  hisIterator.next();
	    myIterator = this.listIterator();
	    try {
	      while(true) {
		Object myobj =  myIterator.next();
		if (hisobj.equals(myobj)) break;
	      }
	    } catch (NoSuchElementException ex) {
		return false;
	    }
	}
	return true;
     }


	/** Match with a template (return true if we have a superset of the
	* given template. This can be used for partial match 
	* (template matching of SIP objects). Note -- this implementation is
	* not unnecessarily efficient  :-) 
	* @param other template object to compare against.
	*/

     public boolean match(Object other) {
	if ( ! this.getClass().equals(other.getClass()) ) return false;
	GenericObjectList that = (GenericObjectList) other;
	ListIterator hisIterator = that.listIterator();
	outer:
	while (hisIterator.hasNext()) {
	    Object hisobj =  hisIterator.next();
	    Object myobj = null;
	    ListIterator myIterator = this.listIterator();
	    while(myIterator.hasNext()) {
		myobj =  myIterator.next();
		if (myobj instanceof GenericObject)
	       System.out.println("Trying to match  = " +
			((GenericObject)myobj).encode());
		if ( GenericObject.isMySubclass(myobj.getClass()) &&
		    ((GenericObject) myobj).match(hisobj) ) 
			break outer;
		else if (GenericObjectList.isMySubclass(myobj.getClass()) &&
			((GenericObjectList) myobj).match(hisobj) ) 
			break outer;
	   }
	   System.out.println(((GenericObject)hisobj).encode());
	   return false;
	}
	return true;
	
     }

  
    

}
		
