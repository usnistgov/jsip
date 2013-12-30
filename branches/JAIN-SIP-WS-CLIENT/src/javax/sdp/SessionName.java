/*
 * SessionName.java
 *
 * Created on January 9, 2002, 10:46 AM
 */

package javax.sdp;

/** A SessionName represents the s= fields contained within a SessionDescription.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface SessionName extends Field {
    
    
    /** Returns the value.
     * @throws SdpParseException
     * @return  the value
     */    
    public String getValue()
    throws SdpParseException;
    
    
    /** Sets the value
     * @param value the - new information.
     * @throws SdpException if the value is null
     */    
    public void setValue(String value)
    throws SdpException;
    
}
