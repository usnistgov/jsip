/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 *
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 *
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *
 * .
 *
 */
package gov.nist.javax.sdp;

import javax.sdp.*;
import gov.nist.javax.sdp.fields.*;
import java.util.*;
import gov.nist.core.*;

/*
 * Includes code contributed by
 * Miguel Freitas (IT) PTInovacao -- modifications for IMS.
 */

/**
 * Fieldementation of Media Description interface.
 *
 * @version JSR141-PUBLIC-REVIEW (subject to change).
 *
 * @author Olivier Deruelle <deruelle@antd.nist.gov>
 * @author M. Ranganathan <br/>
 *
 *
 */
public class MediaDescriptionImpl implements javax.sdp.MediaDescription {

    protected MediaField mediaField;

    protected InformationField informationField;

    protected ConnectionField connectionField;

    protected Vector bandwidthFields;

    protected KeyField keyField;

    protected Vector attributeFields;

    /**
     * Encode to a canonical form.
     *
     * @since v1.0
     */
    public String encode() {
        StringBuilder retval = new StringBuilder();

        if (mediaField != null)
            retval.append(mediaField.encode());

        if (informationField != null)
            retval.append(informationField.encode());

        if (connectionField != null)
            retval.append(connectionField.encode());

        if (bandwidthFields != null) {
            for (int i = 0; i < bandwidthFields.size(); i++) {

                // issued by Miguel Freitas (IT) PTInovacao
                retval.append(((SDPField) bandwidthFields.elementAt(i))
                        .encode());

                /*
                 * original code BandwidthField bandwidthField =
                 * (BandwidthField) bandwidthFields.elementAt(i);
                 * retval.append(bandwidthField.encode());
                 */
                // end //
            }
            if (preconditionFields != null) {
                int precondSize = preconditionFields.getPreconditionSize();
                for (int i = 0; i < precondSize; i++) {
                    retval.append(((SDPField) preconditionFields
                            .getPreconditions().elementAt(i)).encode());
                }
            }
        }

        if (keyField != null)
            retval.append(keyField.encode());

        if (attributeFields != null) {
            for (int i = 0; i < attributeFields.size(); i++)
                retval.append(((SDPField) attributeFields.elementAt(i))
                        .encode());
        }

        return retval.toString();
    }

    public String toString() {
        return this.encode();
    }

    public MediaDescriptionImpl() {
        this.bandwidthFields = new Vector();
        this.attributeFields = new Vector();

        // issued by Miguel Freitas (AV) PTInovacao
        this.preconditionFields = new PreconditionFields();
    }

    public MediaField getMediaField() {
        return mediaField;
    }

    public InformationField getInformationField() {
        return informationField;
    }

    public ConnectionField getConnectionField() {
        return connectionField;
    }

    public KeyField getKeyField() {
        return keyField;
    }

    public Vector getAttributeFields() {
        return attributeFields;
    }

    /**
     * Set the mediaField member
     */
    public void setMediaField(MediaField m) {
        mediaField = m;
    }

    /**
     * Set the informationField member
     */
    public void setInformationField(InformationField i) {
        informationField = i;
    }

    /**
     * Set the connectionField member
     */
    public void setConnectionField(ConnectionField c) {
        connectionField = c;
    }

    /**
     * Set the bandwidthField member
     */
    public void addBandwidthField(BandwidthField b) {
        bandwidthFields.add(b);
    }

    /**
     * Set the keyField member
     */
    public void setKeyField(KeyField k) {
        keyField = k;
    }

    /**
     * Set the attributeFields member
     */
    public void setAttributeFields(Vector a) {
        attributeFields = a;
    }

    /**
     * Return the Media field of the description.
     *
     * @return the Media field of the description.
     */
    public Media getMedia() {
        return mediaField;

    }

    // issued by Miguel Freitas //
    public void addAttribute(AttributeField af) {
        // protected void addAttribute(AttributeField af) {
        // end //
        this.attributeFields.add(af);
    }

    protected boolean hasAttribute(String name) {
        for (int i = 0; i < this.attributeFields.size(); i++) {
            AttributeField af = (AttributeField) this.attributeFields
                    .elementAt(i);
            if (af.getAttribute().getName().equals(name))
                return true;
        }
        return false;
    }

    /**
     * Set the Media field of the description.
     *
     * @param media
     *            to set
     * @throws SdpException
     *             if the media field is null
     */
    public void setMedia(Media media) throws SdpException {
        if (media == null)
            throw new SdpException("The media is null");
        if (media instanceof MediaField) {
            mediaField = (MediaField) media;
        } else
            throw new SdpException("A mediaField parameter is required");
    }

    /**
     * Returns value of the info field (i=) of this object.
     *
     * @return value of the info field (i=) of this object.
     */
    public Info getInfo() {
        InformationField informationField = getInformationField();
        if (informationField == null)
            return null;
        else {
            return informationField;
        }
    }

    /**
     * Sets the i= field of this object.
     *
     * @param i
     *            to set
     * @throws SdpException
     *             if the info is null
     */
    public void setInfo(Info i) throws SdpException {
        if (i == null)
            throw new SdpException("The info is null");
        if (i instanceof InformationField) {
            this.informationField = (InformationField) i;
        } else
            throw new SdpException("A informationField parameter is required");
    }

    /**
     * Returns the connection information associated with this object. This may
     * be null for SessionDescriptions if all Media objects have a connection
     * object and may be null for Media objects if the corresponding session
     * connection is non-null.
     *
     * @return connection
     */
    public Connection getConnection() {

        return connectionField;

    }

    /**
     * Set the connection data for this entity
     *
     * @param conn
     *            to set
     * @throws SdpException
     *             if the connexion is null
     */
    public void setConnection(Connection conn) throws SdpException {
        if (conn == null)
            throw new SdpException("The conn is null");
        if (conn instanceof ConnectionField) {
            connectionField = (ConnectionField) conn;

        } else
            throw new SdpException("bad implementation");
    }

    /**
     * Returns the Bandwidth of the specified type.
     *
     * @param create
     *            type of the Bandwidth to return
     * @return the Bandwidth or null if undefined
     */

    public Vector getBandwidths(boolean create) {
        return bandwidthFields;
    }

    /**
     * set the value of the Bandwidth with the specified type
     *
     * @param bandwidths
     *            type of the Bandwidth object whose value is requested
     * @throws SdpException
     *             if vector is null
     */
    public void setBandwidths(Vector bandwidths) throws SdpException {
        if (bandwidths == null)
            throw new SdpException("The vector bandwidths is null");
        this.bandwidthFields = bandwidths;
    }

    /**
     * Returns the integer value of the specified bandwidth name.
     *
     * @param name
     *            the name of the bandwidth type.
     * @throws SdpParseException
     * @return the value of the named bandwidth
     */
    public int getBandwidth(String name) throws SdpParseException {

        if (name == null)
            throw new NullPointerException("null parameter");
        if (bandwidthFields == null)
            return -1;
        else {
            for (int i = 0; i < bandwidthFields.size(); i++) {
                BandwidthField bandwidthField = (BandwidthField) bandwidthFields
                        .elementAt(i);
                String type = bandwidthField.getBwtype();
                if (type != null && type.equals(name))
                    return bandwidthField.getBandwidth();
            }
            return -1;
        }
    }

    /**
     * Sets the value of the specified bandwidth type.
     *
     * @param name
     *            the name of the bandwidth type.
     * @param value
     *            the value of the named bandwidth type.
     * @throws SdpException
     *             if the name is null
     */
    public void setBandwidth(String name, int value) throws SdpException {
        if (name == null)
            throw new SdpException("The name is null");
        else {
            int i = 0; // issued by Miguel Freitas (IT) PTInovacao
            for (i = 0; i < bandwidthFields.size(); i++) {
                BandwidthField bandwidthField = (BandwidthField) this.bandwidthFields
                        .elementAt(i);
                String type = bandwidthField.getBwtype();
                if (type != null && type.equals(name)) {
                    bandwidthField.setBandwidth(value);

                    break; // issued by Miguel Freitas (IT) PTInovacao
                }
            }

            // issued by Miguel Freitas (IT) PTInovacao
            if (i == this.bandwidthFields.size()) {
                BandwidthField bandwidthField = new BandwidthField();
                bandwidthField.setType(name);
                bandwidthField.setValue(value);
                this.bandwidthFields.add(bandwidthField);
            }
            // end //
        }
    }

    /**
     * Removes the specified bandwidth type.
     *
     * @param name
     *            the name of the bandwidth type.
     */
    public void removeBandwidth(String name) {
        if (name == null) {
            throw new NullPointerException("null bandwidth type");
        } else {
            int i = 0;
            for (i = 0; i < bandwidthFields.size(); i++) {
                BandwidthField bandwidthField = (BandwidthField) bandwidthFields
                        .elementAt(i);
                String type = bandwidthField.getBwtype();
                if (type != null && type.equals(name))
                    break;

            }
            if (i < bandwidthFields.size())
                bandwidthFields.removeElementAt(i);
        }
    }

    /**
     * Returns the key data.
     *
     * @return the key data.
     */
    public Key getKey() {
        if (keyField == null)
            return null;
        else {
            return keyField;
        }
    }

    /**
     * Sets encryption key information. This consists of a method and an
     * encryption key included inline.
     *
     * @param key
     *            the encryption key data; depending on method may be null
     * @throws SdpException
     *             if the key is null
     */
    public void setKey(Key key) throws SdpException {
        if (key == null)
            throw new SdpException("The key is null");
        if (key instanceof KeyField) {
            KeyField keyField = (KeyField) key;
            setKeyField(keyField);
        } else
            throw new SdpException("A keyField parameter is required");
    }

    /**
     * Returns the set of attributes for this Description as a Vector of
     * Attribute objects in the order they were parsed.
     *
     * @param create
     *            specifies whether to return null or a new empty Vector in case
     *            no attributes exists for this Description
     * @return attributes for this Description
     */
    public Vector getAttributes(boolean create) {
        return attributeFields;
    }

    /**
     * Adds the specified Attribute to this Description object.
     *
     * @param attributes --
     *            the attribute to add
     * @throws SdpException --
     *             if the attributes is null
     */
    public void setAttributes(Vector attributes) throws SdpException {
        this.attributeFields = attributes;
    }

    /**
     * Returns the value of the specified attribute.
     *
     * @param name
     *            the name of the attribute.
     * @throws SdpParseException
     * @return the value of the named attribute
     */
    public String getAttribute(String name) throws SdpParseException {
        if (name != null) {
            for (int i = 0; i < this.attributeFields.size(); i++) {
                AttributeField af = (AttributeField) this.attributeFields
                        .elementAt(i);
                if (name.equals(af.getAttribute().getName()))
                    return (String) af.getAttribute().getValueAsObject();
            }
            return null;
        } else
            throw new NullPointerException("null arg!");
    }

    /**
     * Sets the value of the specified attribute
     *
     * @param name
     *            the name of the attribute.
     * @param value
     *            the value of the named attribute.
     * @throws SdpException
     *             if the parameters are null
     */
    public void setAttribute(String name, String value) throws SdpException {
        if (name == null)
            throw new SdpException("The parameters are null");
        else {

            int i = 0;
            for (i = 0; i < this.attributeFields.size(); i++) {
                AttributeField af = (AttributeField) this.attributeFields
                        .elementAt(i);
                if (af.getAttribute().getName().equals(name)) {
                    NameValue nv = af.getAttribute();
                    nv.setValueAsObject(value);
                    break;
                }

            }

            if (i == this.attributeFields.size()) {
                AttributeField af = new AttributeField();
                NameValue nv = new NameValue(name, value);
                af.setAttribute(nv);
                // Bug fix by Emil Ivov.
                this.attributeFields.add(af);
            }

        }
    }

    public String getDuplexity() {
    	  int i = 0;
          for (i = 0; i < this.attributeFields.size(); i++) {
              AttributeField af = (AttributeField) this.attributeFields
                      .elementAt(i);
              if (af.getAttribute().getName().equalsIgnoreCase("sendrecv") ||
                      af.getAttribute().getName().equalsIgnoreCase("recvonly") ||
                      af.getAttribute().getName().equalsIgnoreCase("sendonly") ||
                      af.getAttribute().getName().equalsIgnoreCase("inactive")) {
            	  return af.getAttribute().getName();
              }
          }
          return null;
         
    }
    
    
    public void setDuplexity(String duplexity) {
        if ( duplexity == null ) throw new NullPointerException("Null arg");
        int i = 0;
        for (i = 0; i < this.attributeFields.size(); i++) {
            AttributeField af = (AttributeField) this.attributeFields
                    .elementAt(i);
            if (af.getAttribute().getName().equalsIgnoreCase("sendrecv") ||
                    af.getAttribute().getName().equalsIgnoreCase("recvonly") ||
                    af.getAttribute().getName().equalsIgnoreCase("sendonly") ||
                    af.getAttribute().getName().equalsIgnoreCase("inactive")) {
                NameValue nv = new NameValue(duplexity, null);
                af.setAttribute(nv);
                return;
            }

        }

        if (i == this.attributeFields.size()) {
            AttributeField af = new AttributeField();
            NameValue nv = new NameValue(duplexity, null);
            af.setAttribute(nv);
            // Bug fix by Emil Ivov.
            this.attributeFields.add(af);
        }


    }

    /**
     * Removes the attribute specified by the value parameter.
     *
     * @param name
     *            the name of the attribute.
     */
    public void removeAttribute(String name) {
        if (name == null)
            throw new NullPointerException("null arg!");
        if (name != null) {
            int i = 0;
            for (i = 0; i < this.attributeFields.size(); i++) {
                AttributeField af = (AttributeField) this.attributeFields
                        .elementAt(i);
                if (af.getAttribute().getName().equals(name))
                    break;
            }
            if (i < attributeFields.size())
                attributeFields.removeElementAt(i);

        }
    }

    /**
     * Returns a Vector containing a string indicating the MIME type for each of
     * the codecs in this description.
     *
     * A MIME value is computed for each codec in the media description.
     *
     * The MIME type is computed in the following fashion: The type is the
     * mediaType from the media field. The subType is determined by the
     * protocol.
     *
     * The result is computed as the string of the form:
     *
     * type + '/' + subType
     *
     * The subType portion is computed in the following fashion. RTP/AVP the
     * subType is returned as the codec name. This will either be extracted from
     * the rtpmap attribute or computed. other the protocol is returned as the
     * subType.
     *
     * If the protocol is RTP/AVP and the rtpmap attribute for a codec is
     * absent, then the codec name will be computed in the following fashion.
     * String indexed in table SdpConstants.avpTypeNames if the value is an int
     * greater than or equal to 0 and less than AVP_DEFINED_STATIC_MAX, and has
     * been assigned a value. SdpConstant.RESERVED if the value is an int
     * greater than or equal to 0 and less than AVP_DEFINED_STATIC_MAX, and has
     * not been assigned a value. SdpConstant.UNASSIGNED An int greater than or
     * equal to AVP_DEFINED_STATIC_MAX and less than AVP_DYNAMIC_MIN - currently
     * unassigned. SdpConstant.DYNAMIC Any int less than 0 or greater than or
     * equal to AVP_DYNAMIC_MIN
     *
     * @throws SdpException
     *             if there is a problem extracting the parameters.
     * @return a Vector containing a string indicating the MIME type for each of
     *         the codecs in this description
     */
    public Vector getMimeTypes() throws SdpException {
        MediaField mediaField = (MediaField) getMedia();
        String type = mediaField.getMediaType();
        String protocol = mediaField.getProtocol();
        Vector formats = mediaField.getMediaFormats(false);

        Vector v = new Vector();
        for (int i = 0; i < formats.size(); i++) {
            String result = null;
            if (protocol.equals("RTP/AVP")) {
                if (getAttribute(SdpConstants.RTPMAP) != null)
                    result = type + "/" + protocol;
                else {

                }
            } else
                result = type + "/" + protocol;
            v.addElement(result);
        }
        return v;
    }

    /**
     * Returns a Vector containing a string of parameters for each of the codecs
     * in this description.
     *
     * A parameter string is computed for each codec.
     *
     * The parameter string is computed in the following fashion.
     *
     * The rate is extracted from the rtpmap or static data.
     *
     * The number of channels is extracted from the rtpmap or static data.
     *
     * The ptime is extracted from the ptime attribute.
     *
     * The maxptime is extracted from the maxptime attribute.
     *
     * Any additional parameters are extracted from the ftmp attribute.
     *
     * @throws SdpException
     *             if there is a problem extracting the parameters.
     * @return a Vector containing a string of parameters for each of the codecs
     *         in this description.
     */
    public Vector getMimeParameters() throws SdpException {
        String rate = getAttribute("rate");
        String ptime = getAttribute("ptime");
        String maxptime = getAttribute("maxptime");
        String ftmp = getAttribute("ftmp");
        Vector result = new Vector();
        result.addElement(rate);
        result.addElement(ptime);
        result.addElement(maxptime);
        result.addElement(ftmp);
        return result;
    }

    /**
     * Adds dynamic media types to the description.
     *
     * @param payloadNames
     *            a Vector of String - each one the name of a dynamic payload to
     *            be added (usually an integer larger than
     *            SdpConstants.AVP_DYNAMIC_MIN).
     * @param payloadValues
     *            a Vector of String - each contains the value describing the
     *            correlated dynamic payloads to be added
     * @throws SdpException
     *             if either vector is null or empty. if the vector sizes are
     *             unequal.
     */
    public void addDynamicPayloads(Vector payloadNames, Vector payloadValues)
            throws SdpException {

        if (payloadNames == null || payloadValues == null)
            throw new SdpException(" The vectors are null");
        else {
            if (payloadNames.isEmpty() || payloadValues.isEmpty())
                throw new SdpException(" The vectors are empty");
            else {
                if (payloadNames.size() != payloadValues.size())
                    throw new SdpException(" The vector sizes are unequal");
                else {
                    for (int i = 0; i < payloadNames.size(); i++) {
                        String name = (String) payloadNames.elementAt(i);
                        String value = (String) payloadValues.elementAt(i);
                        setAttribute(name, value);
                    }
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // Precondition Mechanism
    // based in 3GPP TS 24.229 and precondition mechanism (RFC 3312)
    // issued by Miguel Freitas (IT) PTinovacao
    // /////////////////////////////////////////////////////////////////

    /**
     * Precondition Mechanism - precondition fields for the media description
     */
    // Precondition Attribute Fields
    protected PreconditionFields preconditionFields;

    /**
     * <p>
     * Set the Media Description's Precondition Fields
     * </p>
     * <p>
     * issued by Miguel Freitas (IT) PTInovacao
     * </p>
     *
     * @param precondition
     *            Vector containing PreconditionFields
     * @throws SdpException
     */
    public void setPreconditionFields(Vector precondition) throws SdpException {
        this.preconditionFields.setPreconditions(precondition);
    }

    /**
     * <p>
     * Set the Media Description's Precondition Fields
     * </p>
     * <p>
     * issued by Miguel Freitas (IT) PTInovacao
     * </p>
     *
     * @param precondition
     *            PreconditionFields parameter
     */
    public void setPreconditions(PreconditionFields precondition) {
        this.preconditionFields = precondition;
    }

    /**
     * <p>
     * Get attribute fields of segmented precondition
     * </p>
     * <p>
     * issued by Miguel Freitas (IT) PTInovacao
     * </p>
     *
     * @return Vector of attribute fields (segmented precondition)
     */
    public Vector getPreconditionFields() {
        return this.preconditionFields.getPreconditions();
    }

    // end //

}
