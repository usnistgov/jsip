/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;

/**
* Attribute Field.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class AttributeField extends SDPField  implements javax.sdp.Attribute {
	protected NameValue attribute;

	public NameValue getAttribute() { return attribute; }

	public AttributeField() {
		super(ATTRIBUTE_FIELD);
	}
	/**
	* Set the attribute member  
	*/
	public	 void setAttribute(NameValue a) { 
	    attribute = a ; 
	    attribute.setSeparator(Separators.COLON); 
	} 

	/**
	*  Get the string encoded version of this object
	* @since v1.0
	*/
	public String encode() {
	    String encoded_string = ATTRIBUTE_FIELD;
	    if (attribute != null) encoded_string += attribute.encode();
	    return  encoded_string + Separators.NEWLINE; 
	}

	public String toString() { return this.encode(); }

     /** Returns the name of this attribute
     * @throws SdpParseException if the name is not well formatted.
     * @return a String identity or null.
     */
    public String getName() throws SdpParseException {
       NameValue nameValue= getAttribute();
       if (nameValue==null) return null;
       else {
            String name=nameValue.getName();
            if (name==null) return null; 
            else return name;
       }
    }
    
    /** Sets the id of this attribute.
     * @param name  the string name/id of the attribute.
     * @throws SdpException if the name is null
     */    
    public void setName(String name) throws SdpException {
        if (name==null) throw new SdpException("The name is null"); 
        else {
          NameValue nameValue=getAttribute();
          if (nameValue==null)  nameValue=new NameValue();
          nameValue.setName(name);
          setAttribute(nameValue);
        }
    }
    
    /** Determines if this attribute has an associated value.
     * @throws SdpParseException if the value is not well formatted.
     * @return true if the attribute has a value.
     */    
    public boolean hasValue() throws SdpParseException {
       NameValue nameValue=getAttribute();
       if (nameValue==null) return false;
       else {
            Object value=nameValue.getValue();
            if (value==null ) return false;
            else return true;
       }
    }
    
    /** Returns the value of this attribute.
     * @throws SdpParseException if the value is not well formatted.
     * @return the value; null if the attribute has no associated value.
     */    
    public String getValue() throws SdpParseException {
       NameValue nameValue=getAttribute();
       if (nameValue==null) return null;
       else { 
            Object value=nameValue.getValue();
            if (value==null ) return null;
            else if (value instanceof String) 
                    return (String)value;
                else
                    return value.toString();
       }
    }
    
    /** Sets the value of this attribute.
     * @param value the - attribute value
     * @throws SdpException if the value is null.
     */    
    public void setValue(String value) throws SdpException{ 
      if (value==null) throw new SdpException("The value is null"); 
        else {
          NameValue nameValue=getAttribute();
          if (nameValue==null)  nameValue=new NameValue();
          nameValue.setValue(value);
          setAttribute(nameValue);
        }
    }
    
    

        
}
