/*
 * BandWidth.java
 *
 * Created on December 18, 2001, 3:59 PM
 */

package javax.sdp;

/** A Bandwidth represents the b= fields contained within either a MediaDescription or a
 * SessionDescription.
 *
 * This specifies the proposed bandwidth to be used by the session or media, and is optional. Multiple
 * bandwidth specifiers of different types may be associated with the same SessionDescription. Each
 * consists of a token type and an integer value measuring bandwidth in kilobits per second.
 *
 * RFC 2327 defines two bandwidth types (or modifiers):
 *
 * CT
 *     Conference Total: An implicit maximum bandwidth is associated with each TTL on the Mbone or
 *     within a particular multicast administrative scope region (the Mbone bandwidth vs. TTL limits are given
 *     in the MBone FAQ). If the bandwidth of a session or media in a session is different from the
 *     bandwidth implicit from the scope, a 'b=CT:...' line should be supplied for the session giving the
 *     proposed upper limit to the bandwidth used. The primary purpose of this is to give an approximate
 *     idea as to whether two or more conferences can co-exist simultaneously.
 * AS
 *     Application-Specific Maximum: The bandwidth is interpreted to be application-specific, i.e., will be the
 *     application's concept of maximum bandwidth. Normally this will coincide with what is set on the
 *     application's "maximum bandwidth" control if applicable.
 *
 * Note that CT gives a total bandwidth figure for all the media at all sites. AS gives a bandwidth figure for a
 * single media at a single site, although there may be many sites sending simultaneously.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 *
 * @author deruelle
 * @version 1.0
 */ 
public interface BandWidth extends Field{
    
     /**  "Conference Total" bandwidth modifier, "CT".
     */
    public static final String CT="CT";
    
   /** "Application Specific" bandwidth modifier, "AS".
     */
    public static final String AS="AS";
    
    /** Returns the bandwidth type.
     */
    public String getType() throws SdpParseException;
    
    /** Sets the bandwidth type.
     */
    public void setType(String type) throws SdpException;
    
    /** Returns the bandwidth value measured in kilobits per second.
     */
    public int getValue() throws SdpParseException;
    
    /** Sets the bandwidth value.
     */
     public void setValue(int value) throws SdpException;
    
}

