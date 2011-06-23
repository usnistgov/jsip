/*
 * URI.java
 *
 * Created on January 9, 2002, 11:26 AM
 */

package javax.sdp;

import java.net.*;

/** An URI represents the u= field within a SessionDescription.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface URI extends Field {

    /** Returns the value.
     * @throws SdpParseException
     * @return the value
     */    
    public URL get()
        throws SdpParseException;
    
    /** Sets the value.
     * @param value the new information
     * @throws SdpException if the parameter is null
     */    
    public void set(URL value)
         throws SdpException;
    
}

