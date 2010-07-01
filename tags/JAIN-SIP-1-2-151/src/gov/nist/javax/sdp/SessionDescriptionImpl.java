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
/*
 * SessionDescriptionImpl.java
 *
 * Created on January 10, 2002, 3:11 PM
 */
package gov.nist.javax.sdp;

import java.util.*;
import javax.sdp.*;

import gov.nist.javax.sdp.fields.*;

import java.text.ParseException;

/**
 * Implementation of the SessionDescription interface.
 *
 * @version JSR141-PUBLIC-REVIEW
 *
 *
 * @author Olivier Deruelle
 * @author M. Ranganathan  <br/>
 *
 *
 *
 */
public class SessionDescriptionImpl implements SessionDescription {
    private TimeDescriptionImpl currentTimeDescription;

    private MediaDescriptionImpl currentMediaDescription;

    protected ProtoVersionField versionImpl;

    protected OriginField originImpl;

    protected SessionNameField sessionNameImpl;

    protected InformationField infoImpl;

    protected URIField uriImpl;

    protected ConnectionField connectionImpl;

    protected KeyField keyImpl;

    protected Vector timeDescriptions;

    protected Vector mediaDescriptions;

    protected Vector zoneAdjustments;

    protected Vector emailList;

    protected Vector phoneList;

    protected Vector bandwidthList;

    protected Vector attributesList;

    /** Creates new SessionDescriptionImpl */
    public SessionDescriptionImpl() {
    }

    /**
     * Copy constructor, creates a deep copy of another SessionDescription.
     *
     * @param otherSessionDescription - the SessionDescription to copy from.
     * @throws SdpException - if there is a problem constructing the SessionDescription.
     */
    public SessionDescriptionImpl(SessionDescription otherSessionDescription)
            throws SdpException
    {
        // If the other session description is null there's nothing to initialize
        if (otherSessionDescription == null) return;

        // OK to clone the version field, no deep copy required
        Version otherVersion = otherSessionDescription.getVersion();
        if (otherVersion != null) {
            this.setVersion((Version) otherVersion.clone());
        }

        // OK to clone the origin field, class already does a deep copy
        Origin otherOrigin = otherSessionDescription.getOrigin();
        if (otherOrigin != null) {
            this.setOrigin((Origin) otherOrigin.clone());
        }

        // OK to clone the session name, no deep copy required
        SessionName otherSessionName = otherSessionDescription.getSessionName();
        if (otherSessionName != null) {
            this.setSessionName((SessionName) otherSessionName.clone());
        }

        // OK to clone the information field, just a string, no deep copy required
        Info otherInfo = otherSessionDescription.getInfo();
        if (otherInfo != null) {
            this.setInfo((Info) otherInfo.clone());
        }

        // URI field requires deep copy
        URIField otherUriField = (URIField) otherSessionDescription.getURI();
        if (otherUriField != null) {
            URIField newUF = new URIField();
            newUF.setURI(otherUriField.toString());
            this.setURI(newUF);
        }

        // OK to clone the connection field, class already does a deep copy
        Connection otherConnection = (Connection) otherSessionDescription.getConnection();
        if (otherConnection != null) {
            this.setConnection((Connection) otherConnection.clone());
        }

        // OK to clone the key field, just a couple of strings
        Key otherKey = (Key) otherSessionDescription.getKey();
        if (otherKey != null) {
            this.setKey((Key) otherKey.clone());
        }

        // Deep copy each vector, starting with time descriptions
        Vector otherTimeDescriptions = otherSessionDescription.getTimeDescriptions(false);
        if (otherTimeDescriptions != null) {
            Vector newTDs = new Vector();
            Iterator itTimeDescriptions = otherTimeDescriptions.iterator();
            while (itTimeDescriptions.hasNext()) {
                TimeDescriptionImpl otherTimeDescription = (TimeDescriptionImpl) itTimeDescriptions.next();
                if (otherTimeDescription != null) {
                    TimeField otherTimeField = (TimeField) otherTimeDescription.getTime().clone();
                    TimeDescriptionImpl newTD = new TimeDescriptionImpl(otherTimeField);
                    Vector otherRepeatTimes = otherTimeDescription.getRepeatTimes(false);
                    if (otherRepeatTimes != null) {
                        Iterator itRepeatTimes = otherRepeatTimes.iterator();
                        while (itRepeatTimes.hasNext()) {
                            RepeatField otherRepeatField = (RepeatField) itRepeatTimes.next();
                            if (otherRepeatField != null) {
                                // RepeatField clone is a deep copy
                                RepeatField newRF = (RepeatField) otherRepeatField.clone();
                                newTD.addRepeatField(newRF);
                            }
                        }
                    }
                    newTDs.add(newTD);
                }
            }
            this.setTimeDescriptions(newTDs);
        }

        // Deep copy the email list
        Vector otherEmails = otherSessionDescription.getEmails(false);
        if (otherEmails != null) {
            Vector newEmails = new Vector();
            Iterator itEmails = otherEmails.iterator();
            while (itEmails.hasNext()) {
                EmailField otherEmailField = (EmailField) itEmails.next();
                if (otherEmailField != null) {
                    // Email field clone is a deep copy
                    EmailField newEF = (EmailField) otherEmailField.clone();
                    newEmails.add(newEF);
                }
            }
            this.setEmails(newEmails);
        }

        // Deep copy the phone list
        Vector otherPhones = otherSessionDescription.getPhones(false);
        if (otherPhones != null) {
            Vector newPhones = new Vector();
            Iterator itPhones = otherPhones.iterator();
            while (itPhones.hasNext()) {
                PhoneField otherPhoneField = (PhoneField) itPhones.next();
                if (otherPhoneField != null) {
                    // Phone field clone is a deep copy
                    PhoneField newPF = (PhoneField) otherPhoneField.clone();
                    newPhones.add(newPF);
                }
            }
            this.setPhones(newPhones);
        }

        // Deep copy the zone adjustments list
        Vector otherZAs = otherSessionDescription.getZoneAdjustments(false);
        if (otherZAs != null) {
            Vector newZAs = new Vector();
            Iterator itZAs = otherZAs.iterator();
            while (itZAs.hasNext()) {
                ZoneField otherZoneField = (ZoneField) itZAs.next();
                if (otherZoneField != null) {
                    // Zone field clone is a deep copy
                    ZoneField newPF = (ZoneField) otherZoneField.clone();
                    newZAs.add(newPF);
                }
            }
            this.setZoneAdjustments(newZAs);
        }

        // Deep copy the bandwidth list
        Vector otherBandwidths = otherSessionDescription.getBandwidths(false);
        if (otherBandwidths != null) {
            Vector newBandwidths = new Vector();
            Iterator itBandwidths = otherBandwidths.iterator();
            while (itBandwidths.hasNext()) {
                BandwidthField otherBandwidthField = (BandwidthField) itBandwidths.next();
                if (otherBandwidthField != null) {
                    // Bandwidth field clone() is a shallow copy but object is not deep
                    BandwidthField newBF = (BandwidthField) otherBandwidthField.clone();
                    newBandwidths.add(newBF);
                }
            }
            this.setBandwidths(newBandwidths);
        }

        // Deep copy the attribute list
        Vector otherAttributes = otherSessionDescription.getAttributes(false);
        if (otherAttributes != null) {
            Vector newAttributes = new Vector();
            Iterator itAttributes = otherAttributes.iterator();
            while (itAttributes.hasNext()) {
                AttributeField otherAttributeField = (AttributeField) itAttributes.next();
                if (otherAttributeField != null) {
                    // Attribute field clone() makes a deep copy but be careful: it may use reflection to copy one of its members
                    AttributeField newBF = (AttributeField) otherAttributeField.clone();
                    newAttributes.add(newBF);
                }
            }
            this.setAttributes(newAttributes);
        }

        // Deep copy the media descriptions
        Vector otherMediaDescriptions = otherSessionDescription.getMediaDescriptions(false);
        if (otherMediaDescriptions != null) {
            Vector newMDs = new Vector();
            Iterator itMediaDescriptions = otherMediaDescriptions.iterator();
            while (itMediaDescriptions.hasNext()) {
                MediaDescriptionImpl otherMediaDescription = (MediaDescriptionImpl) itMediaDescriptions.next();
                if (otherMediaDescription != null) {
                    MediaDescriptionImpl newMD = new MediaDescriptionImpl();

                    // Copy the media field
                    MediaField otherMediaField = otherMediaDescription.getMediaField();
                    if (otherMediaField != null) {
                        // Media field clone() makes a shallow copy, so don't use clone()
                        MediaField newMF = new MediaField();
                        newMF.setMedia(otherMediaField.getMedia());
                        newMF.setPort(otherMediaField.getPort());
                        newMF.setNports(otherMediaField.getNports());
                        newMF.setProto(otherMediaField.getProto());
                        Vector otherFormats = otherMediaField.getFormats();
                        if (otherFormats != null) {
                            Vector newFormats = new Vector();
                            Iterator itFormats = otherFormats.iterator();
                            while (itFormats.hasNext()) {
                                Object otherFormat = itFormats.next();
                                if (otherFormat != null) {
                                    // Convert all format objects to strings in order to avoid reflection
                                    newFormats.add(String.valueOf(otherFormat));
                                }
                            }
                            newMF.setFormats(newFormats);
                        }
                        newMD.setMedia(newMF);
                    }

                    // Copy the information field (it's a shallow object, ok to clone)
                    InformationField otherInfoField = otherMediaDescription.getInformationField();
                    if (otherInfoField != null) {
                        newMD.setInformationField((InformationField) otherInfoField.clone());
                    }

                    // Copy the connection field. OK to use clone(), already does a deep copy.
                    ConnectionField otherConnectionField = otherMediaDescription.getConnectionField();
                    if (otherConnectionField != null) {
                        newMD.setConnectionField((ConnectionField) otherConnectionField.clone());
                    }

                    // Copy the bandwidth fields
                    Vector otherBFs = otherMediaDescription.getBandwidths(false);
                    if (otherBFs != null) {
                        Vector newBFs = new Vector();
                        Iterator itBFs = otherBFs.iterator();
                        while (itBFs.hasNext()) {
                            BandwidthField otherBF = (BandwidthField) itBFs.next();
                            if (otherBF != null) {
                                // BandwidthField is a shallow object, ok to use clone
                                newBFs.add((BandwidthField) otherBF.clone());
                            }
                        }
                        newMD.setBandwidths(newBFs);
                    }

                    // Copy the key field (shallow object)
                    KeyField otherKeyField = otherMediaDescription.getKeyField();
                    if (otherKeyField != null) {
                        newMD.setKeyField((KeyField) otherKeyField.clone());
                    }

                    // Copy the attributes
                    Vector otherAFs = otherMediaDescription.getAttributeFields();
                    if (otherAFs != null) {
                        Vector newAFs = new Vector();
                        Iterator itAFs = otherAFs.iterator();
                        while (itAFs.hasNext()) {
                            AttributeField otherAF = (AttributeField) itAFs.next();
                            if (otherAF != null) {
                                // AttributeField clone() already makes a deep copy, but be careful. It will use reflection
                                // unless the attribute is a String or any other immutable object.
                                newAFs.add((AttributeField) otherAF.clone());
                            }
                        }
                        newMD.setAttributeFields(newAFs);
                    }
                    newMDs.add(newMD);
                }
            }
            this.setMediaDescriptions(newMDs);
        }
    }

    public void addField(SDPField sdpField) throws ParseException {
        try {
            if (sdpField instanceof ProtoVersionField) {
                versionImpl = (ProtoVersionField) sdpField;
            } else if (sdpField instanceof OriginField) {
                originImpl = (OriginField) sdpField;
            } else if (sdpField instanceof SessionNameField) {
                sessionNameImpl = (SessionNameField) sdpField;
            } else if (sdpField instanceof InformationField) {
                if (currentMediaDescription != null)
                    currentMediaDescription
                            .setInformationField((InformationField) sdpField);
                else
                    this.infoImpl = (InformationField) sdpField;
            } else if (sdpField instanceof URIField) {
                uriImpl = (URIField) sdpField;
            } else if (sdpField instanceof ConnectionField) {
                if (currentMediaDescription != null)
                    currentMediaDescription
                            .setConnectionField((ConnectionField) sdpField);
                else
                    this.connectionImpl = (ConnectionField) sdpField;
            } else if (sdpField instanceof KeyField) {
                if (currentMediaDescription != null)
                    currentMediaDescription.setKey((KeyField) sdpField);
                else
                    keyImpl = (KeyField) sdpField;
            } else if (sdpField instanceof EmailField) {
                getEmails(true).add(sdpField);
            } else if (sdpField instanceof PhoneField) {
                getPhones(true).add(sdpField);
            } else if (sdpField instanceof TimeField) {
                currentTimeDescription = new TimeDescriptionImpl(
                        (TimeField) sdpField);
                getTimeDescriptions(true).add(currentTimeDescription);
            } else if (sdpField instanceof RepeatField) {
                if (currentTimeDescription == null) {
                    throw new ParseException("no time specified", 0);
                } else {
                    currentTimeDescription
                            .addRepeatField((RepeatField) sdpField);
                }
            } else if (sdpField instanceof ZoneField) {
                getZoneAdjustments(true).add(sdpField);
            } else if (sdpField instanceof BandwidthField) {
                if (currentMediaDescription != null)
                    currentMediaDescription
                            .addBandwidthField((BandwidthField) sdpField);
                else
                    getBandwidths(true).add(sdpField);
            } else if (sdpField instanceof AttributeField) {
                if (currentMediaDescription != null) {
                    AttributeField af = (AttributeField) sdpField;
                    String s = af.getName();
                    // Bug report from Andreas Bystrom
                    currentMediaDescription
                            .addAttribute((AttributeField) sdpField);
                } else {
                    getAttributes(true).add(sdpField);
                }

            } else if (sdpField instanceof MediaField) {
                currentMediaDescription = new MediaDescriptionImpl();
                getMediaDescriptions(true).add(currentMediaDescription);
                // Bug report from Andreas Bystrom
                currentMediaDescription.setMediaField((MediaField) sdpField);
            }
        } catch (SdpException ex) {
            throw new ParseException(sdpField.encode(), 0);
        }
    }

    /**
     * Creates and returns a deep copy of this object
     *
     * @return     a clone of this instance.
     * @exception  CloneNotSupportedException  if this instance cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        try {
            return new SessionDescriptionImpl(this);
        } catch (SdpException e) {
            // throw this exception to indicate that this instance cannot be cloned
            throw new CloneNotSupportedException();
        }
    }

    /**
     * Returns the version of SDP in use. This corresponds to the v= field of
     * the SDP data.
     *
     * @return the integer version (-1 if not set).
     */
    public Version getVersion() {
        return versionImpl;
    }

    /**
     * Sets the version of SDP in use. This corresponds to the v= field of the
     * SDP data.
     *
     * @param v
     *            version - the integer version.
     * @throws SdpException
     *             if the version is null
     */
    public void setVersion(Version v) throws SdpException {
        if (v == null)
            throw new SdpException("The parameter is null");
        if (v instanceof ProtoVersionField) {
            versionImpl = (ProtoVersionField) v;
        } else
            throw new SdpException(
                    "The parameter must be an instance of VersionField");
    }

    /**
     * Returns information about the originator of the session. This corresponds
     * to the o= field of the SDP data.
     *
     * @return the originator data.
     */
    public Origin getOrigin() {
        return originImpl;
    }

    /**
     * Sets information about the originator of the session. This corresponds to
     * the o= field of the SDP data.
     *
     * @param origin
     *            origin - the originator data.
     * @throws SdpException
     *             if the origin is null
     */
    public void setOrigin(Origin origin) throws SdpException {
        if (origin == null)
            throw new SdpException("The parameter is null");
        if (origin instanceof OriginField) {
            OriginField o = (OriginField) origin;
            originImpl = o;
        } else
            throw new SdpException(
                    "The parameter must be an instance of OriginField");
    }

    /**
     * Returns the name of the session. This corresponds to the s= field of the
     * SDP data.
     *
     * @return the session name.
     */
    public SessionName getSessionName() {
        return sessionNameImpl;
    }

    /**
     * Sets the name of the session. This corresponds to the s= field of the SDP
     * data.
     *
     * @param sessionName
     *            name - the session name.
     * @throws SdpException
     *             if the sessionName is null
     */
    public void setSessionName(SessionName sessionName) throws SdpException {
        if (sessionName == null)
            throw new SdpException("The parameter is null");
        if (sessionName instanceof SessionNameField) {
            SessionNameField s = (SessionNameField) sessionName;
            sessionNameImpl = s;
        } else
            throw new SdpException(
                    "The parameter must be an instance of SessionNameField");
    }

    /**
     * Returns value of the info field (i=) of this object.
     *
     * @return info
     */
    public Info getInfo() {
        return infoImpl;
    }

    /**
     * Sets the i= field of this object.
     *
     * @param i
     *            s - new i= value; if null removes the field
     * @throws SdpException
     *             if the info is null
     */
    public void setInfo(Info i) throws SdpException {
        if (i == null)
            throw new SdpException("The parameter is null");
        if (i instanceof InformationField) {
            InformationField info = (InformationField) i;
            infoImpl = info;
        } else
            throw new SdpException(
                    "The parameter must be an instance of InformationField");
    }

    /**
     * Returns a uri to the location of more details about the session. This
     * corresponds to the u= field of the SDP data.
     *
     * @return the uri.
     */
    public URI getURI() {
        return uriImpl;
    }

    /**
     * Sets the uri to the location of more details about the session. This
     * corresponds to the u= field of the SDP data.
     *
     * @param uri
     *            uri - the uri.
     * @throws SdpException
     *             if the uri is null
     */
    public void setURI(URI uri) throws SdpException {
        if (uri == null)
            throw new SdpException("The parameter is null");
        if (uri instanceof URIField) {
            URIField u = (URIField) uri;
            uriImpl = u;
        } else
            throw new SdpException(
                    "The parameter must be an instance of URIField");
    }

    /**
     * Returns an email address to contact for further information about the
     * session. This corresponds to the e= field of the SDP data.
     *
     * @param create
     *            boolean to set
     * @throws SdpParseException
     * @return the email address.
     */
    public Vector getEmails(boolean create) throws SdpParseException {
        if (emailList == null) {
            if (create)
                emailList = new Vector();
        }
        return emailList;
    }

    /**
     * Sets a an email address to contact for further information about the
     * session. This corresponds to the e= field of the SDP data.
     *
     * @param emails
     *            email - the email address.
     * @throws SdpException
     *             if the vector is null
     */
    public void setEmails(Vector emails) throws SdpException {
        if (emails == null)
            throw new SdpException("The parameter is null");
        else
            emailList = emails;
    }

    /**
     * Returns a phone number to contact for further information about the
     * session. This corresponds to the p= field of the SDP data.
     *
     * @param create
     *            boolean to set
     * @throws SdpException
     * @return the phone number.
     */
    public Vector getPhones(boolean create) throws SdpException {
        if (phoneList == null) {
            if (create)
                phoneList = new Vector();
        }
        return phoneList;
    }

    /**
     * Sets a phone number to contact for further information about the session.
     * This corresponds to the p= field of the SDP data.
     *
     * @param phones
     *            phone - the phone number.
     * @throws SdpException
     *             if the vector is null
     */
    public void setPhones(Vector phones) throws SdpException {
        if (phones == null)
            throw new SdpException("The parameter is null");
        else
            phoneList = phones;
    }

    /**
     * Returns a TimeField indicating the start, stop, repetition and time zone
     * information of the session. This corresponds to the t= field of the SDP
     * data.
     *
     * @param create
     *            boolean to set
     * @throws SdpException
     * @return the Time Field.
     */
    public Vector getTimeDescriptions(boolean create) throws SdpException {
        if (timeDescriptions == null) {
            if (create)
                timeDescriptions = new Vector();
        }
        return timeDescriptions;
    }

    /**
     * Sets a TimeField indicating the start, stop, repetition and time zone
     * information of the session. This corresponds to the t= field of the SDP
     * data.
     *
     * @param times
     *            time - the TimeField.
     * @throws SdpException
     *             if the vector is null
     */
    public void setTimeDescriptions(Vector times) throws SdpException {
        if (times == null)
            throw new SdpException("The parameter is null");
        else {
            timeDescriptions = times;
        }
    }

    /**
     * Returns the time zone adjustments for the Session
     *
     * @param create
     *            boolean to set
     * @throws SdpException
     * @return a Hashtable containing the zone adjustments, where the key is the
     *         Adjusted Time Zone and the value is the offset.
     */
    public Vector getZoneAdjustments(boolean create) throws SdpException {
        if (zoneAdjustments == null) {
            if (create)
                zoneAdjustments = new Vector();
        }
        return zoneAdjustments;
    }

    /**
     * Sets the time zone adjustment for the TimeField.
     *
     * @param zoneAdjustments
     *            zoneAdjustments - a Hashtable containing the zone adjustments,
     *            where the key is the Adjusted Time Zone and the value is the
     *            offset.
     * @throws SdpException
     *             if the vector is null
     */
    public void setZoneAdjustments(Vector zoneAdjustments) throws SdpException {
        if (zoneAdjustments == null)
            throw new SdpException("The parameter is null");
        else
            this.zoneAdjustments = zoneAdjustments;
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
        return connectionImpl;
    }

    /**
     * Set the connection data for this entity.
     *
     * @param conn
     *            to set
     * @throws SdpException
     *             if the parameter is null
     */
    public void setConnection(Connection conn) throws SdpException {
        if (conn == null)
            throw new SdpException("The parameter is null");
        if (conn instanceof ConnectionField) {
            ConnectionField c = (ConnectionField) conn;
            connectionImpl = c;
        } else
            throw new SdpException("Bad implementation class ConnectionField");
    }

    /**
     * Returns the Bandwidth of the specified type.
     *
     * @param create
     *            type - type of the Bandwidth to return
     * @return the Bandwidth or null if undefined
     */
    public Vector getBandwidths(boolean create) {
        if (bandwidthList == null) {
            if (create)
                bandwidthList = new Vector();
        }
        return bandwidthList;
    }

    /**
     * set the value of the Bandwidth with the specified type.
     *
     * @param bandwidthList
     *            to set
     * @throws SdpException
     *             if the vector is null
     */
    public void setBandwidths(Vector bandwidthList) throws SdpException {
        if (bandwidthList == null)
            throw new SdpException("The parameter is null");
        else
            this.bandwidthList = bandwidthList;
    }

    /**
     * Returns the integer value of the specified bandwidth name.
     *
     * @param name
     *            name - the name of the bandwidth type
     * @throws SdpParseException
     * @return the value of the named bandwidth
     */
    public int getBandwidth(String name) throws SdpParseException {
        if (name == null)
            return -1;
        else if (bandwidthList == null)
            return -1;
        for (int i = 0; i < bandwidthList.size(); i++) {
            Object o = bandwidthList.elementAt(i);
            if (o instanceof BandwidthField) {
                BandwidthField b = (BandwidthField) o;
                String type = b.getType();
                if (type != null) {
                    if (name.equals(type)) {
                        return b.getValue();
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Sets the value of the specified bandwidth type.
     *
     * @param name
     *            name - the name of the bandwidth type.
     * @param value
     *            value - the value of the named bandwidth type.
     * @throws SdpException
     *             if the name is null
     */
    public void setBandwidth(String name, int value) throws SdpException {
        if (name == null)
            throw new SdpException("The parameter is null");
        else if (bandwidthList != null) {
            for (int i = 0; i < bandwidthList.size(); i++) {
                Object o = bandwidthList.elementAt(i);
                if (o instanceof BandwidthField) {
                    BandwidthField b = (BandwidthField) o;
                    String type = b.getType();
                    if (type != null) {
                        if (name.equals(type)) {
                            b.setValue(value);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes the specified bandwidth type.
     *
     * @param name
     *            name - the name of the bandwidth type
     */
    public void removeBandwidth(String name) {
        if (name != null)
            if (bandwidthList != null) {
                for (int i = 0; i < bandwidthList.size(); i++) {
                    Object o = bandwidthList.elementAt(i);
                    if (o instanceof BandwidthField) {
                        BandwidthField b = (BandwidthField) o;
                        try {
                            String type = b.getType();
                            if (type != null) {
                                if (name.equals(type)) {
                                    bandwidthList.remove(b);
                                }
                            }
                        } catch (SdpParseException e) {
                        }
                    }
                }
            }
    }

    /**
     * Returns the key data.
     *
     * @return key
     */
    public Key getKey() {
        return keyImpl;
    }

    /**
     * Sets encryption key information. This consists of a method and an
     * encryption key included inline.
     *
     * @param key
     *            key - the encryption key data; depending on method may be null
     * @throws SdpException
     *             if the parameter is null
     */
    public void setKey(Key key) throws SdpException {
        if (key == null)
            throw new SdpException("The parameter is null");
        if (key instanceof KeyField) {
            KeyField k = (KeyField) key;
            keyImpl = k;
        } else
            throw new SdpException(
                    "The parameter must be an instance of KeyField");
    }

    /**
     * Returns the value of the specified attribute.
     *
     * @param name
     *            name - the name of the attribute
     * @throws SdpParseException
     * @return the value of the named attribute
     */
    public String getAttribute(String name) throws SdpParseException {
        if (name == null)
            return null;
        else if (attributesList == null)
            return null;
        for (int i = 0; i < attributesList.size(); i++) {
            Object o = attributesList.elementAt(i);
            if (o instanceof AttributeField) {
                AttributeField a = (AttributeField) o;
                String n = a.getName();
                if (n != null) {
                    if (name.equals(n)) {
                        return a.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the set of attributes for this Description as a Vector of
     * Attribute objects in the order they were parsed.
     *
     * @param create
     *            create - specifies whether to return null or a new empty
     *            Vector in case no attributes exists for this Description
     * @return attributes for this Description
     */
    public Vector getAttributes(boolean create) {
        if (attributesList == null) {
            if (create)
                attributesList = new Vector();
        }
        return attributesList;
    }

    /**
     * Removes the attribute specified by the value parameter.
     *
     * @param name
     *            name - the name of the attribute
     */
    public void removeAttribute(String name) {
        if (name != null)
            if (attributesList != null) {
                for (int i = 0; i < attributesList.size(); i++) {
                    Object o = attributesList.elementAt(i);
                    if (o instanceof AttributeField) {
                        AttributeField a = (AttributeField) o;
                        try {
                            String n = a.getName();
                            if (n != null) {
                                if (name.equals(n)) {
                                    attributesList.remove(a);
                                }
                            }
                        } catch (SdpParseException e) {
                        }

                    }
                }
            }
    }

    /**
     * Sets the value of the specified attribute.
     *
     * @param name
     *            name - the name of the attribute.
     * @param value
     *            value - the value of the named attribute.
     * @throws SdpException
     *             if the name or the value is null
     */
    public void setAttribute(String name, String value) throws SdpException {
        if (name == null || value == null)
            throw new SdpException("The parameter is null");
        else if (attributesList != null) {
            for (int i = 0; i < attributesList.size(); i++) {
                Object o = attributesList.elementAt(i);
                if (o instanceof AttributeField) {
                    AttributeField a = (AttributeField) o;
                    String n = a.getName();
                    if (n != null) {
                        if (name.equals(n)) {
                            a.setValue(value);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the specified Attribute to this Description object.
     *
     * @param attributes - the attribute to add
     * @throws SdpException
     *             if the vector is null
     */
    public void setAttributes(Vector attributes) throws SdpException {
        if (attributes == null)
            throw new SdpException("The parameter is null");
        else
            attributesList = attributes;
    }

    /**
     * Adds a MediaDescription to the session description. These correspond to
     * the m= fields of the SDP data.
     *
     * @param create
     *            boolean to set
     * @throws SdpException
     * @return media - the field to add.
     */
    public Vector getMediaDescriptions(boolean create) throws SdpException {
        if (mediaDescriptions == null) {
            if (create)
                mediaDescriptions = new Vector();
        }
        return mediaDescriptions;
    }

    /**
     * Removes all MediaDescriptions from the session description.
     *
     * @param mediaDescriptions
     *            to set
     * @throws SdpException
     *             if the parameter is null
     */
    public void setMediaDescriptions(Vector mediaDescriptions)
            throws SdpException {
        if (mediaDescriptions == null)
            throw new SdpException("The parameter is null");
        else
            this.mediaDescriptions = mediaDescriptions;
    }

    private String encodeVector(Vector vector) {
        StringBuilder encBuff = new StringBuilder();

        for (int i = 0; i < vector.size(); i++)
            encBuff.append(vector.elementAt(i));

        return encBuff.toString();
    }

    /**
     * Returns the canonical string representation of the current
     * SessionDescrption. Acknowledgement - this code was contributed by Emil
     * Ivov.
     *
     * @return Returns the canonical string representation of the current
     *         SessionDescrption.
     */

    public String toString() {
        StringBuilder encBuff = new StringBuilder();

        // Encode single attributes
        encBuff.append(getVersion() == null ? "" : getVersion().toString());
        encBuff.append(getOrigin() == null ? "" : getOrigin().toString());
        encBuff.append(getSessionName() == null ? "" : getSessionName()
                .toString());
        encBuff.append(getInfo() == null ? "" : getInfo().toString());

        // Encode attribute vectors
        try {
            encBuff.append(getURI() == null ? "" : getURI().toString());
            encBuff.append(getEmails(false) == null ? ""
                    : encodeVector(getEmails(false)));
            encBuff.append(getPhones(false) == null ? ""
                    : encodeVector(getPhones(false)));
            encBuff.append(getConnection() == null ? "" : getConnection()
                    .toString());
            encBuff.append(getBandwidths(false) == null ? ""
                    : encodeVector(getBandwidths(false)));
            encBuff.append(getTimeDescriptions(false) == null ? ""
                    : encodeVector(getTimeDescriptions(false)));
            encBuff.append(getZoneAdjustments(false) == null ? ""
                    : encodeVector(getZoneAdjustments(false)));
            encBuff.append(getKey() == null ? "" : getKey().toString());
            encBuff.append(getAttributes(false) == null ? ""
                    : encodeVector(getAttributes(false)));
            encBuff.append(getMediaDescriptions(false) == null ? ""
                    : encodeVector(getMediaDescriptions(false)));
            // adds the final crlf
        } catch (SdpException exc) {
            // add exception handling if necessary
        }
        return encBuff.toString();
    }

}


