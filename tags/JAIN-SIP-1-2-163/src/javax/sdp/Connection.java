/*
 * Connection.java
 *
 * Created on December 18, 2001, 4:20 PM
 */

package javax.sdp;

/** A Connection represents the c= field associated with a SessionDescription or with an individual
 * MediaDescription and is used to identify a network address on which media can be received.
 *
 * The Connection in the SessionDescription applies to all MediaDescriptions unless a
 * MediaDescription specifically overrides it. The Connection identifies the network type (IN for internet),
 * address type (IP4 or IP6), the start of an address range, the time to live of the session and the number of
 * addresses in the range. Both the time to live and number of addresses are optional.
 *
 * A Connection could therefore be of one these forms:
 *
 *     c=IN IP4 myhost.somewhere.com (no ttl and only one address)
 *     c=IN IP4 myhost.somewhere.com/5 (a ttl of 5)
 *     c=IN IP4 myhost.somewhere.com/5/2 (a ttl of 5 and 2 addresses)
 *
 * This implementation does not explicitly support ttl and number of addresses.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface Connection extends Field{
    
     /** The Internet network type, "IN".
      */
    public static final String IN="IN";
    
    /** The IPv4 address type, "IP4".
     */
    public static final String IP4="IP4";
    
    /** The IPv6 address type, "IP6".
     */
    public static final String IP6="IP6";
    
    /** Returns the type of the network for this Connection.
     */
    public String getAddress() throws SdpParseException;
    
    /** Returns the type of the address for this Connection.
     */
     public String getAddressType() throws SdpParseException;
    
    /**Returns the type of the network for this Connection.
     */
    public String getNetworkType() throws SdpParseException;
    
    /** Sets the type of the address for this Connection.
     */
    public void setAddress(String addr) throws SdpException;
    
    /** Returns the type of the network for this Connection.
     */
     public void setAddressType(String type) throws SdpException;
    
     /** Sets the type of the network for this Connection.
     */
      public void setNetworkType(String type) throws SdpException;
    
}

