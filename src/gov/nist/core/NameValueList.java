/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.core;
import java.util.*;

/**
* Implements a simple NameValue association with a quick lookup 
* function (via a hash table) this class is not thread safe 
* because it uses HashTables.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class NameValueList extends GenericObjectList  {
	private  Hashtable nvHash;

	public NameValueList( String listName ) {
	    super(listName,NameValue.class);
	    nvHash = new Hashtable();
	}

	public void add (NameValue nv ) {
		if (nv == null) throw new NullPointerException("null nv");
		if (nv.name != null )  {
		  nvHash.put(nv.name.toLowerCase(),nv);
		}
		super.add((GenericObject)nv);
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
		NameValue nv = new NameValue(name,value);
		this.set(nv);
	}

	
        
        
        /**
         *Generic add method (throws a class cast exception if anything
         *but NameValue is added.
         */
        public boolean add (Object obj) { 
            add((NameValue) obj);
            return true;
        }

	/**
	* Add a name value record to this list.
	*/

	public void add(String name, Object obj) {
		NameValue nv = new NameValue(name,obj);
		add(nv);
	}
        
	
	
		       
		

	/**
         *  Compare if two NameValue lists are equal.
	 *@param otherObject  is the object to compare to.
	 *@return true if the two objects compare for equality.
         */
        public boolean equals(Object otherObject) {
	   int exit = 0;
	    try {
            if (!otherObject.getClass().equals
                (this.getClass())) {
		exit = 1;
                return false;
            }
            NameValueList other = (NameValueList) otherObject;

            if (this.nvHash.size() != other.nvHash.size()) {
		exit = 2;
		return false;
	    }
            for (Enumeration keys = this.nvHash.keys();
            keys.hasMoreElements(); ) {
                String name     = (String) keys.nextElement();
                Object obj = this.nvHash.get(name.toLowerCase());
                NameValue nv = (NameValue) obj;
                Object myvalue = nv.value;
                Object obj1 =  other.nvHash.get(name.toLowerCase());
                if (obj1 == null)  {
		        exit = 3;
			return false;
		}
                NameValue nv1 = (NameValue) obj1;
                Object hisvalue =  nv1.value;
                if (hisvalue == null && myvalue != null ||
		    hisvalue != null && myvalue == null )  {
		        exit = 4;
			return false;
		}
                if (! myvalue.equals(hisvalue) ) {
		        exit = 5;
		        return false;
		}
            }
	    exit = 6;
            return true;
	    } finally {
		// System.out.println("NameValueList.equals() " + exit);
	    }
	}
	

	/**
	*  Do a lookup on a given name and return value associated with it.
	*/
	public Object  getValue(String name) {
		NameValue nv = (NameValue) nvHash.get(name.toLowerCase());
		if (nv != null ) return nv.value;
		else return null;
	}

	/**
	* Get the NameValue record given a name.
	* @since 1.0
	*/
	public NameValue getNameValue (String name) {
		return (NameValue) nvHash.get(name.toLowerCase());
	}
	
	/**
	* Returns a boolean telling if this NameValueList
	* has a record with this name  
	* @since 1.0
	*/
	public boolean hasNameValue (String name) {
		NameValue nv = (NameValue) nvHash.get(name.toLowerCase()) ;
		return nv != null ;
	}

	/**
	* Remove the element corresponding to this name.
	* @since 1.0
	*/
	public boolean delete( String name) {
		NameValue nv = (NameValue) nvHash.get(name.toLowerCase());
		if (nv == null) return false;
		this.remove(nv);
		nvHash.remove(name.toLowerCase());
		return true;
	}
        
        /**
         *Get the list iterator for this list.
         */
        public Iterator getIterator() { return super.getIterator(); }

	 /**
	  *Get a list of parameter names.
	  *@return a list iterator that has the names of the parameters.
	  */
	 public Iterator getNames() {
		LinkedList ll = new LinkedList();
		Iterator iterator = this.getIterator();
		while(iterator.hasNext()) {
		   String name = ((NameValue) iterator.next()).name;
		   ll.add(name);
		}
		return ll.listIterator();
	 }

	
        /**
         *default constructor.
         */
        public NameValueList() { nvHash = new Hashtable(); }
            
        
        public Object clone()   {
            NameValueList retval = new NameValueList();
	    retval.setMyClass(this.getMyClass());
	    retval.listName = listName;
	    retval.separator = this.separator;
            ListIterator li = this.listIterator();
            while (li.hasNext()) {
                NameValue nv = (NameValue) li.next();
                NameValue nnv = (NameValue) nv.clone();
                retval.add(nnv);
            }
            return retval;
        
        }
        
        /** Get the parameter as a String.
         *@return the parameter as a string.
         */
        public String getParameter(String name) {
            Object val = this.getValue(name);
            if (val == null) return null;
            if (val instanceof GenericObject) 
                return ((GenericObject)val).encode();
            else return val.toString();
        }

	/**
	* Get the first element of the list.
	*/
	public GenericObject first() { return (NameValue) super.first(); }


}
