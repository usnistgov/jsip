/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.core;
/**
*  Generic structure for storing name-value pairs.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class NameValue  extends GenericObject {
	protected boolean isQuotedString;
	protected String separator;
	protected String quotes;
        protected String name;
        protected Object value;
	public NameValue() { 
		name = null; value = null;  
		separator = Separators.EQUALS; 
		this.quotes = ""; 
	}
	public NameValue( String n, Object v) { 
		name = n; 
		value = v;  
		separator = Separators.EQUALS; 
		quotes = ""; 
	}
	/**
	* Set the separator for the encoding method below.
	*/
	public void setSeparator( String sep) {
		separator = sep;
	}

	/** A flag that indicates that doublequotes should be put around the
	* value when encoded 
	*(for example name=value when value is doublequoted).
	*/
	public void setQuotedValue() {
		isQuotedString = true;
		this.quotes = Separators.DOUBLE_QUOTE;
	}

	/** Return true if the value is quoted in doublequotes.
	*/
	public boolean isValueQuoted() { return isQuotedString; }

	public	 String getName() 
 	 	{ return name ; } 
	public	 Object getValue() 
 	 	{ return value ; } 
	/**
	* Set the name member  
	*/
	public	 void setName(String n) 
 	 	{ name = n ; } 
	/**
	* Set the value member  
	*/
	public	 void setValue(Object v) 
 	 	{ value = v ; } 

	/**
	* Get the encoded representation of this namevalue object.
        * Added doublequote for encoding doublequoted values 
	* (bug reported by Kirby Kiem).
	*@since 1.0
	*@return an encoded name value (eg. name=value) string.
	*/
	public String encode() { 
		if ( name != null && value != null) {
		    if (GenericObject.isMySubclass(value.getClass())) {
		        GenericObject gv = (GenericObject) value;
		       return name + separator + quotes+ gv.encode() + quotes;
		    } else 
		      if (GenericObjectList.isMySubclass(value.getClass())) {
		        GenericObjectList gvlist = (GenericObjectList) value;
		        return name + separator + gvlist.encode();
		    } else return 
			name + separator + quotes + value.toString() +  quotes;
		 } else if (name == null && value != null ) {
		    if (GenericObject.isMySubclass(value.getClass())) {
		     GenericObject gv = (GenericObject) value;
		     return  gv.encode();
		    } else 
		      if (GenericObjectList.isMySubclass(value.getClass())) {
		        GenericObjectList gvlist = (GenericObjectList) value;
		        return gvlist.encode();
		      } return quotes + value.toString() + quotes;
		} else if (name != null && value == null) {
		     return name;
		} else return "";
	}

	public Object clone() {
		NameValue retval = new NameValue();
		retval.separator = this.separator;
		retval.isQuotedString = this.isQuotedString;
		retval.quotes = this.quotes;
		retval.name = this.name;
		if (value != null) retval.value = makeClone(this.value);
		return retval;
	}

	/** 
	* Equality comparison predicate.
	*/
	public boolean equals( Object other) {
		if (! other.getClass().equals(this.getClass()))  return false;
	        NameValue that = (NameValue) other;
		if (this == that) return true;
		if (this.name  == null && that.name != null ||
		   this.name != null && that.name == null) return false;
		if (this.name != null && that.name != null &&
			this.name.compareToIgnoreCase(that.name) != 0) 
				return false;
		if ( this.value != null && that.value == null ||
		     this.value == null && that.value != null) return false;
		if (this.value == that.value) return true;
		if (value instanceof String) {
			// Quoted string comparisions are case sensitive.
		        if (isQuotedString)  
				return this.value.equals(that.value);
			String val = (String) this.value;
			String val1 = (String) that.value;
			return val.compareToIgnoreCase(val1) == 0 ;
		} else return this.value.equals(that.value);
	}

}
