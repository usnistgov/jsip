/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import java.net.*;
import javax.sdp.*;
/**
*   Key field part of an SDP header
*
*@version  JSR141-PUBLIC-REVIEW (subject to change)
*
*@author Oliver Deruelle <deruelle@antd.nist.gov> 
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*/
public class KeyField  extends SDPField   implements javax.sdp.Key {
	protected String 	   type;
	protected String 	   keyData;
	
	public KeyField() { super(KEY_FIELD); }


	public String getType()    
		{ return type; }

	public String getKeyData()     
        { 
	  return keyData;
        }

	/**
	* Set the type member  
	*/
	public	 void setType(String t) 
 	 	{ type = t ; } 
	/**
	* Set the keyData member  
	*/
	public	 void setKeyData(String k) 
 	 	{ keyData = k ; } 

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        String encoded_string; 
	encoded_string = KEY_FIELD + type;
	if (type.compareToIgnoreCase(SDPKeywords.PROMPT) == 0) {
	    if (type.compareToIgnoreCase(SDPKeywords.URI) == 0) {
	        encoded_string += Separators.COLON;
		encoded_string += keyData;
	    } else {
		if (keyData != null) {
	           encoded_string += Separators.COLON;
		   encoded_string += keyData;
		}
	    }
	}
	encoded_string += Separators.NEWLINE;
	return encoded_string;
    }

    /** Returns the name of this attribute
     * @throws SdpParseException
     * @return the name of this attribute
     */
    public String getMethod()
    throws SdpParseException {
	return this.type;
    }
    
    /** Sets the id of this attribute.
     * @param name to set
     * @throws SdpException if the name is null
     */
    public void setMethod(String name)
    throws SdpException {
	this.type = name;
    }
    
    /** Determines if this attribute has an associated value.
     * @throws SdpParseException
     * @return if this attribute has an associated value.
     */
    public boolean hasKey()
    throws SdpParseException {
        String key=getKeyData();
        return key!=null;
    }
    
    /** Returns the value of this attribute.
     * @throws SdpParseException
     * @return the value of this attribute
     */    
    public String getKey()
    throws SdpParseException {
        return getKeyData();
    }
    
    /** Sets the value of this attribute.
     * @param key to set
     * @throws SdpException if key is null
     */    
    public void setKey(String key)
    throws SdpException {
        if (key==null) throw new SdpException("The key is null");
        else setKeyData(key); 
    }
}
