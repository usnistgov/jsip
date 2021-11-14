/*
 * MediaDescription.java
 *
 * Created on December 19, 2001, 11:17 AM
 */

package javax.sdp;

import java.io.*;
import java.util.*;

// issued by Miguel Freitas - work-around
import gov.nist.javax.sdp.fields.PreconditionFields;
import gov.nist.javax.sdp.fields.AttributeField;
// end //


/** A MediaDescription identifies the set of medias that may be received on a specific port or set of ports. It includes:
 *
 *     a mediaType (e.g., audio, video, etc.)
 *     a port number (or set of ports)
 *     a protocol to be used (e.g., RTP/AVP)
 *     a set of media formats which correspond to Attributes associated with the media description.
 *
 * The following is an example
 *
 * m=audio 60000 RTP/AVP 0
 * a=rtpmap:0 PCMU/8000
 *
 * This example identifies that the client can receive audio on port 60000 in format 0 which corresponds to PCMU/8000.
 *
 * Please refer to IETF RFC 2327 for a description of SDP.
 *
 * @author deruelle
 * @version 1.0
 */
public interface MediaDescription extends Serializable, Cloneable {

/** Return the Media field of the description.
 * @return the Media field of the description.
 */    
public Media getMedia();

/** Set the Media field of the description.
 * @param media to set
 * @throws SdpException if the media field is null
 */
public void setMedia(Media media)
              throws SdpException;

/** Returns value of the info field (i=) of this object.
 * @return value of the info field (i=) of this object.
 */
public Info getInfo();

/** Sets the i= field of this object.
 * @param i to set
 * @throws SdpException if the info is null
 */
public void setInfo(Info i)
             throws SdpException;

/** Returns the connection information associated with this object. This may be null for SessionDescriptions if all Media
 *     objects have a connection object and may be null for Media objects if the corresponding session connection is non-null.
 * @return connection
 */
public Connection getConnection();


/** Set the connection data for this entity
 * @param conn to set
 * @throws SdpException if the connexion is null
 */
public void setConnection(Connection conn)
                   throws SdpException;

/** Returns the Bandwidth of the specified type.
 * @param create type of the Bandwidth to return
 * @return the Bandwidth or null if undefined
 */
public Vector getBandwidths(boolean create);

/** set the value of the Bandwidth with the specified type
 * @param bandwidths type of the Bandwidth object whose value is requested
 * @throws SdpException if vector is null
 */
public void setBandwidths(Vector bandwidths)
                   throws SdpException;

/** Returns the integer value of the specified bandwidth name.
 * @param name the name of the bandwidth type.
 * @throws SdpParseException
 * @return the value of the named bandwidth
 */
public int getBandwidth(String name)
                 throws SdpParseException;

/** Sets the value of the specified bandwidth type.
 * @param name the name of the bandwidth type.
 * @param value  the value of the named bandwidth type.
 * @throws SdpException if the name is null
 */
public void setBandwidth(String name,
                         int value)
                  throws SdpException;

/** Removes the specified bandwidth type.
 * @param name the name of the bandwidth type.
 */
public void removeBandwidth(String name);

/** Returns the key data.
 * @return the key data.
 */
public Key getKey();

/** Sets encryption key information. This consists of a method and an encryption key included inline.
 * @param key  the encryption key data; depending on method may be null
 * @throws SdpException if the key is null
 */
public void setKey(Key key)
            throws SdpException;

/** Returns the set of attributes for this Description as a Vector of Attribute objects in the order they were parsed.
 * @param create specifies whether to return null or a new empty Vector in case no attributes exists for this Description
 * @return attributes for this Description
 */
public Vector getAttributes(boolean create);

/** Adds the specified Attribute to this Description object.
 * @param Attributes  the attribute to add
 * @throws SdpException if the attribute is null
 */
public void setAttributes(Vector Attributes)
                   throws SdpException;

/** Returns the value of the specified attribute.
 * @param name the name of the attribute.
 * @throws SdpParseException
 * @return the value of the named attribute
 */
public String getAttribute(String name)
                    throws SdpParseException;

/** Sets the value of the specified attribute
 * @param name the name of the attribute.
 * @param value the value of the named attribute.
 * @throws SdpException if the parameters are null
 */
public void setAttribute(String name,
                         String value)
                  throws SdpException;

/** Removes the attribute specified by the value parameter.
 * @param name the name of the attribute.
 */
public void removeAttribute(String name);

/** Returns a Vector containing a string indicating the MIME type for each of the codecs in this description.
 *
 *     A MIME value is computed for each codec in the media description.
 *
 *     The MIME type is computed in the following fashion:
 *          The type is the mediaType from the media field.
 *          The subType is determined by the protocol.
 *
 *     The result is computed as the string of the form:
 *
 *     type + '/' + subType
 *
 *     The subType portion is computed in the following fashion.
 *     RTP/AVP
 *          the subType is returned as the codec name. This will either be extracted from the rtpmap attribute or computed.
 *     other
 *          the protocol is returned as the subType.
 *
 *     If the protocol is RTP/AVP and the rtpmap attribute for a codec is absent, then the codec name will be computed in the
 *     following fashion.
 *     String indexed in table SdpConstants.avpTypeNames
 *          if the value is an int greater than or equal to 0 and less than AVP_DEFINED_STATIC_MAX, and has been assigned a
 *          value.
 *     SdpConstant.RESERVED
 *          if the value is an int greater than or equal to 0 and less than AVP_DEFINED_STATIC_MAX, and has not been
 *          assigned a value.
 *     SdpConstant.UNASSIGNED
 *          An int greater than or equal to AVP_DEFINED_STATIC_MAX and less than AVP_DYNAMIC_MIN - currently
 *          unassigned.
 *     SdpConstant.DYNAMIC
 *          Any int less than 0 or greater than or equal to AVP_DYNAMIC_MIN
 * @throws SdpException if there is a problem extracting the parameters.
 * @return a Vector containing a string indicating the MIME type for each of the codecs in this description
 */
public Vector getMimeTypes()
                    throws SdpException;

/** Returns a Vector containing a string of parameters for each of the codecs in this description.
 *
 *     A parameter string is computed for each codec.
 *
 *     The parameter string is computed in the following fashion.
 *
 *     The rate is extracted from the rtpmap or static data.
 *
 *     The number of channels is extracted from the rtpmap or static data.
 *
 *     The ptime is extracted from the ptime attribute.
 *
 *     The maxptime is extracted from the maxptime attribute.
 *
 *     Any additional parameters are extracted from the ftmp attribute.
 * @throws SdpException if there is a problem extracting the parameters.
 * @return a Vector containing a string of parameters for each of the codecs in this description.
 */
public Vector getMimeParameters()
                         throws SdpException;

/** Adds dynamic media types to the description.
 * @param payloadNames a Vector of String - each one the name of a dynamic payload to be added (usually an integer larger
 *          than SdpConstants.AVP_DYNAMIC_MIN).
 * @param payloadValues a Vector of String - each contains the value describing the correlated dynamic payloads to be added
 * @throws SdpException  if either vector is null or empty.
 * if the vector sizes are unequal.
 */
public void addDynamicPayloads(Vector payloadNames,
                               Vector payloadValues)
                        throws SdpException;





//////////////////////////////////////////////
// changes made by PT-INOVACAO
//////////////////////////////////////////////

/**
 * <p>Set PreconditionFields for the Media Description</p>
 * 
 * issued by Miguel Freitas (IT) PTInovacao
 * @param segPrecondition Vector with values to ser
 * @throws SdpException
 */
public void setPreconditionFields(Vector segPrecondition) throws SdpException;

/**
 * <p>Set PreconditionFields for the Media Description</p>
 * 
 * issued by Miguel Freitas (IT) PTInovacao
 * @param segPrecondition PreconditionFields with values to set
 */
public void setPreconditions(PreconditionFields segPrecondition);

/**
 * <p>Get all Precondition Fields in the Media Descritpion</p> 
 * 
 * issued by Miguel Freitas (IT) PTInovacao
 * @return Vector precondition fields
 */
public Vector getPreconditionFields();

/**
 * <p>Add Media Attribute based on an AttributeField value</p>
 * 
 * issued by Miguel Freitas (IT) PTInovacao
 * @param at AttributeField
 */
public void addAttribute(AttributeField at);


}

