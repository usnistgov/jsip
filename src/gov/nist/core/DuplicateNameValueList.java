package gov.nist.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * 
 * @author aayush.bhatnagar
 * 
 * This is a Duplicate Name Value List that will allow multiple values map
 * to the same key. 
 * 
 * The parsing and encoding logic for it is the 
 * same as that of NameValueList. Only the HashMap container is different.
 *
 */
public class DuplicateNameValueList implements Serializable{

	MultiHashMap m = new MultiHashMap();
	 private String separator;
	
	private static final long serialVersionUID = -5611332957903796952L;

	public DuplicateNameValueList()
	
	{
		this.separator = ";";
	}
	
	//------------------
	
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
	        if (!m.isEmpty()) {
	            Iterator<NameValue> iterator = m.values().iterator();
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
	        this.m.put(nv.getName().toLowerCase(), nv);
	    }

	    /**
	     * Set a namevalue object in this list.
	     */
	    public void set(String name, Object value) {
	        NameValue nameValue = new NameValue(name, value);
	        m.put(name.toLowerCase(), nameValue);

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
	        DuplicateNameValueList other = (DuplicateNameValueList) otherObject;

	        if (m.size() != other.m.size()) {
	            return false;
	        }
	        Iterator<String> li = this.m.keySet().iterator();

	        while (li.hasNext()) {
	            String key = (String) li.next();
	            Collection nv1 = this.getNameValue(key);
	            Collection nv2 = (Collection) other.m.get(key);
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
	    	Collection nv = this.getNameValue(name.toLowerCase());
	        if (nv != null)
	            return nv;
	        else
	            return null;
	    }

	    /**
	     * Get the NameValue record given a name.
	     *
	     * @since 1.0
	     */
	    public Collection getNameValue(String name) {
	        return (Collection) this.m.get(name.toLowerCase());
	    }

	    /**
	     * Returns a boolean telling if this NameValueList has a record with this
	     * name
	     */
	    public boolean hasNameValue(String name) {
	        return m.containsKey(name.toLowerCase());
	    }

	    /**
	     * Remove the element corresponding to this name.
	     */
	    public boolean delete(String name) {
	        String lcName = name.toLowerCase();
	        if (this.m.containsKey(lcName)) {
	            this.m.remove(lcName);
	            return true;
	        } else {
	            return false;
	        }

	    }

	    public Object clone() {
	        DuplicateNameValueList retval = new DuplicateNameValueList();
	        retval.setSeparator(this.separator);
	        Iterator<NameValue> it = this.m.values().iterator();
	        while (it.hasNext()) {
	            retval.set((NameValue) ((NameValue) it.next()).clone());
	        }
	        return retval;
	    }
	    
	    /**
	     * Return an iterator for the name-value pairs of this list.
	     *
	     * @return the iterator.
	     */
	    public Iterator<NameValue> iterator() {
	        return this.m.values().iterator();
	    }

	    /**
	     * Get a list of parameter names.
	     *
	     * @return a list iterator that has the names of the parameters.
	     */
	    public Iterator<String> getNames() {
	        return this.m.keySet().iterator();

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

	public void clear() {
		m.clear();
		
	}

	public boolean containsKey(Object key) {
		return m.containsKey(key.toString().toLowerCase());
	}

	public boolean containsValue(Object value) {
		return m.containsValue(value);
	}

	public NameValue get(Object key) {
		return (NameValue) this.m.get(key);
	}

	public boolean isEmpty() {
		return this.m.isEmpty();
	}

	public Set<String> keySet() {
		return this.m.keySet();
	}

	public NameValue put(String key, NameValue value) {
		return (NameValue) this.m.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends NameValue> t) {
		this.m.putAll(t);
		
	}

	public NameValue remove(Object key) {
		return (NameValue) this.m.remove(key);
	}

	public int size() {
		return this.m.size();
	}

	public Collection<NameValue> values() {
		return this.m.values();
	}

}
