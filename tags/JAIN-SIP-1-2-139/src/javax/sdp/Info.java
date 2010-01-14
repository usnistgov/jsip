/*
 * Info.java
 *
 * Created on December 18, 2001, 5:12 PM
 */

package javax.sdp;

/**An Info represents the i= fields contained within either a MediaDescription
 * or a SessionDescription. 
 *
 *  Please refer to IETF RFC 2327 for a description of SDP. 
 *
 * @author  deruelle
 * @version 1.0
 */
public interface Info extends Field {

    /** Returns the value.
     */
    public String getValue() throws SdpParseException;
    
    /** Set the value.
     */
    public void setValue(String value) throws SdpException;

}


