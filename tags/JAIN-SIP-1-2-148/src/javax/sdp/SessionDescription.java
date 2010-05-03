/*
 * SessionDescription.java
 *
 * Created on January 10, 2002, 2:38 PM
 */

package javax.sdp;

import java.io.*;
import java.util.*;

/** A SessionDescription represents the data defined by the Session Description 
 * Protocol (see
 * IETF RFC 2327) and holds information about the originitor of a session, 
 * the media types that a
 * client can support and the host and port on which the client will listen 
 * for that media.
 *
 * The SessionDescription also holds timing information for the session (e.g. start, end,
 * repeat, time zone) and bandwidth supported for the session.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface SessionDescription extends Serializable, Cloneable{

    /** Public clone declaration.
     * @throws CloneNotSupportedException if clone method is not supported
     * @return Object
     */    
    public Object clone()
             throws CloneNotSupportedException;
    
    /** Returns the version of SDP in use. This corresponds to the v= field of the SDP data.
     * @return the integer version (-1 if not set).
     */    
    public Version getVersion();
    
    /** Sets the version of SDP in use. This corresponds to the v= field of the SDP data.
     * @param v version - the integer version.
     * @throws SdpException if the version is null
     */    
    public void setVersion(Version v)
                throws SdpException;
    
    /** Returns information about the originator of the session. This corresponds
     * to the o= field
     *     of the SDP data.
     * @return the originator data.
     */    
    public Origin getOrigin();
    
    /** Sets information about the originator of the session. This corresponds 
     * to the o= field of
     *     the SDP data.
     * @param origin origin - the originator data.
     * @throws SdpException if the origin is null
     */    
    public void setOrigin(Origin origin)
               throws SdpException;
    
    /** Returns the name of the session. This corresponds to the s= field of the SDP data.
     * @return the session name.
     */    
    public SessionName getSessionName();
    
    
    /** Sets the name of the session. This corresponds to the s= field of the SDP data.
     * @param sessionName name - the session name.
     * @throws SdpException if the sessionName is null
     */    
    public void setSessionName(SessionName sessionName)
                    throws SdpException;
    
    /** Returns value of the info field (i=) of this object.
     * @return info
     */    
    public Info getInfo();
    
    /** Sets the i= field of this object.
     * @param i s - new i= value; if null removes the field
     * @throws SdpException if the info is null
     */    
    public void setInfo(Info i)
             throws SdpException;
    
    /** Returns a uri to the location of more details about the session. 
     * This corresponds to the u=
     *     field of the SDP data.
     * @return the uri.
     */    
    public URI getURI();
    
    /** Sets the uri to the location of more details about the session. This
     * corresponds to the u=
     *     field of the SDP data.
     * @param uri uri - the uri.
     * @throws SdpException if the uri is null
     */    
    public void setURI(URI uri)
            throws SdpException;
    
    /** Returns an email address to contact for further information about the session. 
     * This corresponds to the e= field of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return the email address.
     */    
    public Vector getEmails(boolean create)
                 throws SdpParseException;
    
    /** Sets a an email address to contact for further information about the session.
     * This corresponds to the e= field of the SDP data.
     * @param emails email - the email address.
     * @throws SdpException if the vector is null
     */    
    public void setEmails(Vector emails)
               throws SdpException;
    
    /** Returns a phone number to contact for further information about the session. This
     *     corresponds to the p= field of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return the phone number.
     */    
    public Vector getPhones(boolean create)
                 throws SdpException;
    
    /** Sets a phone number to contact for further information about the session. This
     *     corresponds to the p= field of the SDP data.
     * @param phones phone - the phone number.
     * @throws SdpException if the vector is null
     */    
    public void setPhones(Vector phones)
               throws SdpException;
    
    /** Returns a TimeField indicating the start, stop, repetition and time zone
     * information of the
     *     session. This corresponds to the t= field of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return the Time Field.
     */    
    public Vector getTimeDescriptions(boolean create)
                           throws SdpException;
    
    /** Sets a TimeField indicating the start, stop, repetition and time zone 
     * information of the
     *     session. This corresponds to the t= field of the SDP data.
     * @param times time - the TimeField.
     * @throws SdpException if the vector is null
     */    
    public void setTimeDescriptions(Vector times)
                         throws SdpException;
    
    /** Returns the time zone adjustments for the Session
     * @param create boolean to set
     * @throws SdpException
     * @return a Hashtable containing the zone adjustments, where the key is the 
     * Adjusted Time
     *          Zone and the value is the offset.
     */    
    public Vector getZoneAdjustments(boolean create)
                          throws SdpException;
    
    /** Sets the time zone adjustment for the TimeField.
     * @param zoneAdjustments zoneAdjustments - a Hashtable containing the zone
     * adjustments, where the key
     *          is the Adjusted Time Zone and the value is the offset.
     * @throws SdpException if the vector is null
     */    
    public void setZoneAdjustments(Vector zoneAdjustments)
                        throws SdpException;
    
    /** Returns the connection information associated with this object. This may 
     * be null for SessionDescriptions if all Media objects have a connection 
     * object and may be null
     *     for Media objects if the corresponding session connection is non-null.
     * @return connection
     */    
    public Connection getConnection();
    
    /** Set the connection data for this entity.
     * @param conn to set
     * @throws SdpException if the parameter is null
     */    
    public void setConnection(Connection conn)
                   throws SdpException;
    
    /** Returns the Bandwidth of the specified type.
     * @param create type - type of the Bandwidth to return
     * @return the Bandwidth or null if undefined
     */    
    public Vector getBandwidths(boolean create);
    
    /** set the value of the Bandwidth with the specified type.
     * @param bandwidths to set
     * @throws SdpException if the vector is null
     */    
    public void setBandwidths(Vector bandwidths)
                   throws SdpException;
    
    /** Returns the integer value of the specified bandwidth name.
     * @param name name - the name of the bandwidth type
     * @throws SdpParseException
     * @return the value of the named bandwidth
     */    
    public int getBandwidth(String name)
                 throws SdpParseException;
    
    /** Sets the value of the specified bandwidth type.
     * @param name name - the name of the bandwidth type.
     * @param value value - the value of the named bandwidth type.
     * @throws SdpException if the name is null
     */    
    public void setBandwidth(String name,
                         int value)
                  throws SdpException;
    
    /** Removes the specified bandwidth type.
     * @param name name - the name of the bandwidth type
     */    
    public void removeBandwidth(String name);
    
    /** Returns the key data.
     * @return key
     */    
    public Key getKey();
    
    /** Sets encryption key information. This consists of a method and an encryption key
     *     included inline.
     * @param key key - the encryption key data; depending on method may be null
     * @throws SdpException if the parameter is null
     */    
    public void setKey(Key key)
            throws SdpException;
    
    /** Returns the value of the specified attribute.   
     * @param name name - the name of the attribute
     * @throws SdpParseException
     * @return the value of the named attribute
     */    
    public String getAttribute(String name)
                    throws SdpParseException;
    
    /** Returns the set of attributes for this Description as a Vector of Attribute
     * objects in the
     *     order they were parsed.
     * @param create create - specifies whether to return null or a new empty 
     * Vector in case no
     *          attributes exists for this Description
     * @return attributes for this Description
     */ 
    public Vector getAttributes(boolean create);
    
    /** Removes the attribute specified by the value parameter.
     * @param name name - the name of the attribute
     */    
    public void removeAttribute(String name);
    
    /** Sets the value of the specified attribute.
     * @param name name - the name of the attribute.
     * @param value value - the value of the named attribute.
     * @throws SdpException if the name or the value is null
     */    
    public void setAttribute(String name,
                         String value)
                  throws SdpException;
    
    /** Adds the specified Attribute to this Description object.
     * @param Attributes attribute - the attribute to add
     * @throws SdpException if the vector is null
     */    
    public void setAttributes(Vector Attributes)
                   throws SdpException;
    
    /** Adds a MediaDescription to the session description. These correspond to the m=
     *    fields of the SDP data.
     * @param create boolean to set
     * @throws SdpException
     * @return media - the field to add.
     */    
    public Vector getMediaDescriptions(boolean create)
                            throws SdpException;
    
    /** Removes all MediaDescriptions from the session description.
     * @param mediaDescriptions to set
     * @throws SdpException if the parameter is null
     */    
    public void setMediaDescriptions(Vector mediaDescriptions)
                          throws SdpException;
}

