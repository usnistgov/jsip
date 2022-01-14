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
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
import java.util.*;
/**
*    Media field SDP header.
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan   <br/>
*
*
*
*/
public class MediaField extends SDPField implements Media {
    protected String media;
    protected int port;
    protected int nports;
    protected String proto;
    protected Vector formats;

    public MediaField() {
        super(SDPFieldNames.MEDIA_FIELD);
        formats = new Vector();
    }

    public String getMedia() {
        return media;
    }
    public int getPort() {
        return port;
    }
    public int getNports() {
        return nports;
    }
    public String getProto() {
        return proto;
    }
    public Vector getFormats() {
        return formats;
    }
    /**
    * Set the media member
    */
    public void setMedia(String m) {
        media = m;
    }
    /**
    * Set the port member
    */
    public void setPort(int p) {
        port = p;
    }
    /**
    * Set the nports member
    */
    public void setNports(int n) {
        nports = n;
    }
    /**
    * Set the proto member
    */
    public void setProto(String p) {
        proto = p;
    }
    /**
    * Set the fmt member
    */
    public void setFormats(Vector formats) {
        this.formats = formats;
    }
    /** Returns the type (audio,video etc) of the
    * media defined by this description.
    * @throws SdpParseException
    * @return the string media type.
    */
    public String getMediaType() throws SdpParseException {
        return getMedia();
    }

    /** Sets the type (audio,video etc) of the media defined by this description.
     * @param mediaType to set
     * @throws SdpException if mediaType is null
     */
    public void setMediaType(String mediaType) throws SdpException {
        if (mediaType == null)
            throw new SdpException("The mediaType is null");
        else
            setMedia(mediaType);
    }

    /** Returns the port of the media defined by this description
     * @throws SdpParseException
     * @return the integer media port.
     */
    public int getMediaPort() throws SdpParseException {
        return getPort();
    }

    /** Sets the port of the media defined by this description
     * @param port to set
     * @throws SdpException
     */
    public void setMediaPort(int port) throws SdpException {
        if (port < 0)
            throw new SdpException("The port is < 0");
        else
            setPort(port);
    }

    /** Returns the number of ports associated with this media description
     * @throws SdpParseException
     * @return the integer port count.
     */
    public int getPortCount() throws SdpParseException {
        return getNports();
    }

    /** Sets the number of ports associated with this media description.
     * @param portCount portCount - the integer port count.
     * @throws SdpException
     */
    public void setPortCount(int portCount) throws SdpException {
        if (portCount < 0)
            throw new SdpException("The port count is < 0");
        else
            setNports(portCount);
    }

    /** Returns the protocol over which this media should be transmitted.
     * @throws SdpParseException
     * @return the String protocol, e.g. RTP/AVP.
     */
    public String getProtocol() throws SdpParseException {
        return getProto();
    }

    /** Sets the protocol over which this media should be transmitted.
     * @param protocol  - the String protocol, e.g. RTP/AVP.
     * @throws SdpException if the protocol is null
     */
    public void setProtocol(String protocol) throws SdpException {
        if (protocol == null)
            throw new SdpException("The protocol is null");
        else
            setProto(protocol);
    }

    /** Returns an Vector of the media formats supported by this description.
     * Each element in this Vector will be an String value which matches one of
     * the a=rtpmap: attribute fields of the media description.
     * @param create to set
     * @throws SdpException
     * @return the Vector.
     */
    public Vector getMediaFormats(boolean create) throws SdpParseException {

        if (!create && formats.size() == 0)
            return null;
        else
            return formats;
    }

    /** Adds a media format to the media description.
     * Each element in this Vector should be an String value which matches one of the
     * a=rtpmap: attribute fields of the media description.
     * @param mediaFormats the format to add.
     * @throws SdpException if the vector is null
     */
    public void setMediaFormats(Vector mediaFormats) throws SdpException {
        if (mediaFormats == null)
            throw new SdpException("The mediaFormats is null");
        this.formats = mediaFormats;
    }

    private String encodeFormats() {
    StringBuilder retval = new StringBuilder(3 * formats.size ());
        for (int i = 0; i < formats.size(); i++) {
            retval.append(formats.elementAt(i));
            if (i < formats.size() - 1)
                retval.append(Separators.SP);
        }
        return retval.toString();
    }

    /**
     *  Get the string encoded version of this object
     * @since v1.0
     */
    public String encode() {
        String encoded_string;
        encoded_string = MEDIA_FIELD;
        if (media != null)
            encoded_string += media.toLowerCase() + Separators.SP + port;
        // Workaround for Microsoft Messenger contributed by Emil Ivov
        // Leave out the nports parameter as this confuses the messenger.
        if (nports > 1)
            encoded_string += Separators.SLASH + nports;

        if (proto != null)
            encoded_string += Separators.SP + proto;

        if (formats != null)
            encoded_string += Separators.SP + encodeFormats();

        encoded_string += Separators.NEWLINE;
        return encoded_string;
    }

    public Object clone() {
        MediaField retval = (MediaField) super.clone();
        if (this.formats != null)
            retval.formats = (Vector) this.formats.clone();
        return retval;
    }
}

