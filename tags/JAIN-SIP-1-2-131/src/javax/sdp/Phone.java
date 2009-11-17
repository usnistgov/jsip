/*
 * Phone.java
 *
 * Created on December 20, 2001, 2:57 PM
 */

package javax.sdp;

/** A Phone represents a p= field contained within a SessionDescription.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface Phone extends Field {

    /** Returns the value.
     * @throws SdpParseException
     * @return the value.
     */    
    public String getValue()
                throws SdpParseException;
    
    /** Sets the value.
     * @param value the - new information.
     * @throws SdpException if the value is null
     */    
    public void setValue(String value)
              throws SdpException;
    
}

