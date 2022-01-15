/*
 * Attribute.java
 *
 * Created on December 18, 2001, 10:55 AM
 */

package javax.sdp;

/**
 * An Attribute represents an a= fields contained within either a MediaDescription or a
 * SessionDescription.
 *
 * An Attribute can be just an identity/name or a name-value pair.
 *
 * Here are some examples:
 *
 * a=recvonly
 *     identifies a rcvonly attribute with just a name
 * a=rtpmap:0 PCMU/8000
 *     identifies the media format 0 has having the value PCMU/8000.
 *
 * If a value is present, it must be preceeded by the : character.
 * @author  deruelle
 * @version 1.0
 */
public interface Attribute extends Field {
    
    /** Returns the name of this attribute
     * @throws SdpParseException if the name is not well formatted.
     * @return a String identity.
     */
    public String getName() throws SdpParseException;
    
    /** Sets the id of this attribute.
     * @param name  the string name/id of the attribute.
     * @throws SdpException if the name is null
     */    
    public void setName(String name) throws SdpException;
    
    /** Determines if this attribute has an associated value.
     * @throws SdpParseException if the value is not well formatted.
     * @return true if the attribute has a value.
     */    
    public boolean hasValue() throws SdpParseException;
    
    /** Returns the value of this attribute.
     * @throws SdpParseException if the value is not well formatted.
     * @return the value; null if the attribute has no associated value.
     */    
    public String getValue() throws SdpParseException;
    
    /** Sets the value of this attribute.
     * @param value the - attribute value
     * @throws SdpException if the value is null.
     */    
    public void setValue(String value) throws SdpException;
    
}

