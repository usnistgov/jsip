/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;

/** Proto version field of SDP announce.
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ProtoVersionField extends SDPField implements javax.sdp.Version{
	protected int protoVersion;
	
	public ProtoVersionField() {
		super(PROTO_VERSION_FIELD); 
	}

	public int getProtoVersion() {
		return protoVersion;
	}

	/**
	* Set the protoVersion member  
	*/
	public void setProtoVersion( int pv ) {
		protoVersion = pv;
	}

    /** Returns the version number.
     * @throws SdpParseException
     * @return int
     */    
    public int getVersion()
    throws SdpParseException {
        return getProtoVersion();
    }
    
    /** Sets the version.
     * @param value the - new version value.
     * @throws SdpException if the value is <=0
     */    
    public void setVersion(int value)
    throws SdpException {
        if (value <0) throw new SdpException("The value is <0");
        else setProtoVersion(value); 
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
	return PROTO_VERSION_FIELD + protoVersion + Separators.NEWLINE;
    }
	
}
