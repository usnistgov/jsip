/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import javax.sip.address.*;

/**
* AlertInfo SIP Header.
* 
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class AlertInfo  extends ParametersHeader implements javax.sip.header.AlertInfoHeader {
    
    /** URI field
     */
    protected GenericURI uri;

    
     
    /** Constructor
     */
    public AlertInfo() {
        super(NAME);
    }
    
        /**
         * Return value encoding in canonical form.
         * @return The value of the header in canonical encoding.
         */
    protected String encodeBody() {
        StringBuffer encoding = new StringBuffer();
        encoding.append(LESS_THAN ).append(uri.encode()).append(GREATER_THAN);
        if (!parameters.isEmpty()) {
            encoding.append(SEMICOLON).append(parameters.encode());
        }
        return encoding.toString();
    }
   
    
        /**
         * Set the uri member
         * @param u URI to set
         */
    public void setAlertInfo(URI uri) {
        this.uri = (GenericURI)uri ;
    }
    
    
    /**
     * Returns the AlertInfo value of this AlertInfoHeader.
     *
     *
     *
     * @return the URI representing the AlertInfo.
     *
     * @since JAIN SIP v1.1
     *
     */
    public URI getAlertInfo() {
	return (URI)this.uri;
    }
    
    
}
