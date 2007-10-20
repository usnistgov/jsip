/*
 * Media.java
 *
 * Created on December 19, 2001, 10:28 AM
 */

package javax.sdp;

import java.util.*;
/** A Media represents an m= field contained within a MediaDescription. The Media
 * identifies information about the format(s) of the
 * media associated with the MediaDescription.
 *
 * The Media field includes:
 *
 *     a mediaType (e.g. audio, video, etc.)
 *     a port number (or set of ports)
 *     a protocol to be used (e.g. RTP/AVP)
 *     a set of media formats which correspond to Attributes associated with the
 * media description.
 *
 * Here is an example:
 *
 * m=audio 60000 RTP/AVP 0
 * a=rtpmap:0 PCMU/8000
 *
 * This example identifies that the client can receive audio on port 60000 in 
 * format 0 which corresponds to PCMU/8000.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface Media extends Field {
    
    /** Returns the type (audio,video etc) of the media defined by this description.
     * @throws SdpParseException
     * @return the string media type.
     */    
    public String getMediaType()
    throws SdpParseException;
    
    /** Sets the type (audio,video etc) of the media defined by this description.
     * @param mediaType to set
     * @throws SdpException if mediaType is null
     */    
    public void setMediaType(String mediaType)
    throws SdpException;
    
    /** Returns the port of the media defined by this description
     * @throws SdpParseException
     * @return the integer media port.
     */    
    public int getMediaPort()
    throws SdpParseException;
    
    /** Sets the port of the media defined by this description
     * @param port to set
     * @throws SdpException
     */    
    public void setMediaPort(int port)
    throws SdpException;
    
    /** Returns the number of ports associated with this media description
     * @throws SdpParseException
     * @return the integer port count.
     */    
    public int getPortCount()
    throws SdpParseException;
    
    /** Sets the number of ports associated with this media description.
     * @param portCount portCount - the integer port count.
     * @throws SdpException
     */    
    public void setPortCount(int portCount)
    throws SdpException;
    
    /** Returns the protocol over which this media should be transmitted.
     * @throws SdpParseException
     * @return the String protocol, e.g. RTP/AVP.
     */    
    public String getProtocol()
    throws SdpParseException;
    
    /** Sets the protocol over which this media should be transmitted.
     * @param protocol  - the String protocol, e.g. RTP/AVP.
     * @throws SdpException if the protocol is null
     */    
    public void setProtocol(String protocol)
    throws SdpException;
    
    /** Returns an Vector of the media formats supported by this description.
     * Each element in this Vector will be an String value which matches one of
     * the a=rtpmap: attribute fields of the media description.
     * @param create to set
     * @throws SdpException
     * @return the Vector.
     */    
    public Vector getMediaFormats(boolean create)
    throws SdpParseException;
    
    /** Adds a media format to the media description.
     * Each element in this Vector should be an String value which matches one of the
     * a=rtpmap: attribute fields of the media description.
     * @param mediaFormats the format to add.
     * @throws SdpException if the vector is null
     */    
    public void setMediaFormats(Vector mediaFormats)
    throws SdpException;
    
    /** Generates a string description of this object.
     * @return the description.
     */    
    public String toString();
    
}

