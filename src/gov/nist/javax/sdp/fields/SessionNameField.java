/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;

public class SessionNameField  extends SDPField implements SessionName {
	protected String sessionName;

	public SessionNameField() {
		super(SDPFieldNames.SESSION_NAME_FIELD);
	}
	public String getSessionName() { return sessionName; }
	/**
	* Set the sessionName member  
	*/
	public	 void setSessionName(String s) 
 	 	{ sessionName = s ; } 

    /** Returns the value.
     * @throws SdpParseException
     * @return  the value
     */    
    public String getValue()
    throws SdpParseException {
        return getSessionName();
    }
    
    
    /** Sets the value
     * @param value the - new information.
     * @throws SdpException if the value is null
     */    
    public void setValue(String value)
    throws SdpException {
        if (value==null) throw new SdpException("The value is null");
        else {
            setSessionName(value);
        }
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
	return SESSION_NAME_FIELD + sessionName + Separators.NEWLINE;
    }
	
}
