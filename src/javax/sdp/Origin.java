/*
 * Origin.java
 *
 * Created on December 20, 2001, 2:30 PM
 */

package javax.sdp;


/**
 * An Origin represents the o= fields contained within a SessionDescription.
 *
 * The Origin field identifies the originator of the session.
 *
 * This is not necessarily the same entity who is involved in the session.
 *
 * The Origin contains:
 *
 *     the name of the user originating the session,
 *     a unique session identifier, and
 *     a unique version for the session.
 *
 * These fields should uniquely identify the session.
 *
 * The Origin also includes:
 *
 *     the network type,
 *     address type, and
 *     address of the originator.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 * @author deruelle
 * @version 1.0
 */
public interface Origin extends Field {

    /** Returns the name of the session originator.
     * @throws SdpParseException
     * @return the string username.
     */    
    public String getUsername()
                   throws SdpParseException;
    
    /** Sets the name of the session originator.
     * @param user the string username.
     * @throws SdpException if the parameter is null
     */    
    public void setUsername(String user)
                 throws SdpException;
    
    /** Returns the unique identity of the session.
     * @throws SdpParseException
     * @return the session id.
     */    
    public long getSessionId()
                  throws SdpParseException;
    
    /** Sets the unique identity of the session.
     * @param id  the session id.
     * @throws SdpException if the id is <0
     */    
    public void setSessionId(long id)
                  throws SdpException;
    
    /** Returns the unique version of the session.
     * @throws SdpException
     * @return the session version.
     */    
    public long getSessionVersion()
                       throws SdpParseException;
    
    /** Sets the unique version of the session.
     * @param version  the session version.
     * @throws SdpException if the version is <0
     */    
    public void setSessionVersion(long version)
                       throws SdpException;
    
    /** Returns the type of the network for this Connection.
     * @throws SdpParseException
     * @return the string network type.
     */    
    public String getAddress()
                  throws SdpParseException;
    
    /** Returns the type of the address for this Connection.
     * @throws SdpParseException
     * @return the string address type.
     */    
    public String getAddressType()
                      throws SdpParseException;
    
    /** Returns the type of the network for this Connection
    * @throws SdpParseException
    * @return the string network type.
    */    
    public String getNetworkType()
                      throws SdpParseException;
    
    /** Sets the type of the address for this Connection.
     * @param addr  string address type.
     * @throws SdpException if the addr is null
     */    
    public void setAddress(String addr)
                throws SdpException;
    
    /** Returns the type of the network for this Connection.
     * @param type the string network type.
     * @throws SdpException if the type is null
     */    
    public void setAddressType(String type)
                    throws SdpException;
    
    /** Sets the type of the network for this Connection.
     * @param type  the string network type.
     * @throws SdpException if the type is null
     */    
    public void setNetworkType(String type)
                    throws SdpException;
}

