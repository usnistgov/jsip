/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.*;
import javax.sip.header.*;

/**  
* ProxyRequire Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ProxyRequire extends SIPHeader 
implements ProxyRequireHeader{
    
        /** optiontag field
         */    
	protected String optionTag;
	
         /** Default  Constructor
         * @param s String to set
         */        
	public ProxyRequire() {
		super(PROXY_REQUIRE);
	}
        
        /** Constructor
         * @param s String to set
         */        
	public ProxyRequire( String s) {
		super(PROXY_REQUIRE);
		optionTag = s;
	}
	
        /**
         * Encode in canonical form.
         * @return String
         */
	public String encodeBody() {
		return  optionTag ;
	}
        
    /**
     * Sets the option tag value to the new supplied <var>optionTag</var>
     * parameter.
     *
     * @param optionTag - the new string value of the option tag.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the optionTag value.
     */
    public void setOptionTag(String optionTag) throws ParseException {
        if (optionTag==null) throw new  NullPointerException(
         "JAIN-SIP Exception, ProxyRequire, setOptionTag(), the optionTag parameter is null");
        this.optionTag=optionTag;
    }

    /**
     * Gets the option tag of this OptionTag class.
     *
     * @return the string that identifies the option tag value.
     */
    public String getOptionTag() {
        return optionTag;
    }
		
}
