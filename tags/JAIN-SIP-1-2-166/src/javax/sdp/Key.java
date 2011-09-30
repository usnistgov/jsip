/*
 * Key.java
 *
 * Created on December 19, 2001, 10:10 AM
 */

package javax.sdp;

/** A Key represents the k= field contained within either a MediaDescription or a SessionDescription.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface Key extends Field {
    
    /** Returns the name of this attribute
     * @throws SdpParseException
     * @return the name of this attribute
     */
    public String getMethod()
    throws SdpParseException;
    
    /** Sets the id of this attribute.
     * @param name to set
     * @throws SdpException if the name is null
     */
    public void setMethod(String name)
    throws SdpException;
    
    /** Determines if this attribute has an associated value.
     * @throws SdpParseException
     * @return if this attribute has an associated value.
     */
    public boolean hasKey()
    throws SdpParseException;
    
    /** Returns the value of this attribute.
     * @throws SdpParseException
     * @return the value of this attribute
     */    
    public String getKey()
    throws SdpParseException;
    
    /** Sets the value of this attribute.
     * @param key to set
     * @throws SdpException if key is null
     */    
    public void setKey(String key)
    throws SdpException;
}

