/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.*;

/**  
* MimeVersion SIP Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class MimeVersion extends SIPHeader
implements MimeVersionHeader{
    
        /** mimeVersion field
         */    
	protected int minorVersion;
        
        /** majorVersion field
         */    
	protected int majorVersion;
        
        /** Default constructor
         */        
	public MimeVersion() {
            super(MIME_VERSION) ;
        }

    /**
     * Gets the Minor version value of this MimeVersionHeader.
     *
     * @return the Minor version of this MimeVersionHeader
     */
    public int getMinorVersion() {
        return minorVersion;
    }
    
     /**
     * Gets the Major version value of this MimeVersionHeader.
     *
     * @return the Major version of this MimeVersionHeader
     */
    public int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Sets the Minor-Version argument of this MimeVersionHeader to the supplied
     * <var>minorVersion</var> value.
     *
     * @param minorVersion - the new integer Minor version
     * @throws InvalidArgumentException
     */
    public void setMinorVersion(int minorVersion) throws InvalidArgumentException {
        if (minorVersion<0) throw new InvalidArgumentException("JAIN-SIP Exception"+
        ", MimeVersion, setMinorVersion(), the minorVersion parameter is null");
        this.minorVersion=minorVersion;
    }
    
    /**
     * Sets the Major-Version argument of this MimeVersionHeader to the supplied
     * <var>majorVersion</var> value.
     *
     * @param majorVersion - the new integer Major version
     * @throws InvalidArgumentException
     */
    public void setMajorVersion(int majorVersion) throws InvalidArgumentException {
        if (majorVersion<0) throw new InvalidArgumentException("JAIN-SIP Exception"+
        ", MimeVersion, setMajorVersion(), the majorVersion parameter is null");
        this.majorVersion=majorVersion;
    }

        /**
         * Return canonical form.
         * @return String
         */                  
	public String encodeBody() {
            return new Integer(majorVersion).toString()+DOT+
                   new Integer(minorVersion).toString();
        }
		
}
