package javax.sdp;

import gov.nist.javax.sdp.*;
import gov.nist.javax.sdp.fields.*;
import gov.nist.javax.sdp.parser.*;

import java.util.*;
import java.net.*;
import java.text.*;

/**
 * The SdpFactory enables applications to encode and decode SDP messages. The
 * SdpFactory can be used to construct a SessionDescription object
 * programmatically. The SdpFactory can also be used to construct a
 * SessionDescription based on the contents of a String. Acknowledgement: Bugs
 * reported by Brian J. Collins <bjcollins@rockwellcollins.com>. and by Majdi
 * Abuelbassal <majdi.abuelbassal@bbumail.com>. Please refer to IETF RFC 2327
 * for a description of SDP.
 *
 * @author Olivier Deruelle <olivier.deruelle@nist.gov>
 * @author M. Ranganathan
 *
 *
 * @version 1.0
 *
 */
public class SdpFactory extends Object {
    private static final SdpFactory singletonInstance = new SdpFactory();

    /** Creates new SdpFactory */
    private SdpFactory() {
    }

    /**
     * Obtain an instance of an SdpFactory.
     *
     * This static method returns a factory instance.
     *
     * Once an application has obtained a reference to an SdpFactory it can use
     * the factory to configure and obtain parser instances and to create SDP
     * objects.
     *
     * @return a factory instance
     */
    public static SdpFactory getInstance() {
        return singletonInstance;
    }

    /**
     * Creates a new, empty SessionDescription. The session is set as follows:
     *
     * v=0
     *
     * o=this.createOrigin ("user", InetAddress.getLocalHost().toString());
     *
     * s=-
     *
     * t=0 0
     *
     * @throws SdpException
     *             SdpException, - if there is a problem constructing the
     *             SessionDescription.
     * @return a new, empty SessionDescription.
     */
    public SessionDescription createSessionDescription() throws SdpException {
        SessionDescriptionImpl sessionDescriptionImpl = new SessionDescriptionImpl();

        ProtoVersionField ProtoVersionField = new ProtoVersionField();
        ProtoVersionField.setVersion(0);
        sessionDescriptionImpl.setVersion(ProtoVersionField);

        OriginField originImpl = null;
        try {
            originImpl = (OriginField) this.createOrigin("user", InetAddress
                    .getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        sessionDescriptionImpl.setOrigin(originImpl);

        SessionNameField sessionNameImpl = new SessionNameField();
        sessionNameImpl.setValue("-");
        sessionDescriptionImpl.setSessionName(sessionNameImpl);

        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        TimeField timeImpl = new TimeField();
        timeImpl.setZero();
        timeDescriptionImpl.setTime(timeImpl);
        Vector times = new Vector();
        times.addElement(timeDescriptionImpl);
        sessionDescriptionImpl.setTimeDescriptions(times);

        return sessionDescriptionImpl;
    }

    /**
     * Creates a new SessionDescription, deep copy of another
     * SessionDescription.
     *
     * @param otherSessionDescription -
     *            the SessionDescription to copy from.
     * @return a new SessionDescription, exact and deep copy of the
     *         otherSessionDescription.
     * @throws SdpException -
     *             if there is a problem constructing the SessionDescription.
     */
    public SessionDescription createSessionDescription(
            SessionDescription otherSessionDescription) throws SdpException {
        return new SessionDescriptionImpl(otherSessionDescription);
    }

    /**
     * Creates a SessionDescription populated with the information contained
     * within the string parameter.
     *
     * Note: unknown field types should not cause exceptions.
     *
     * @param s
     *            s - the sdp message that is to be parsed.
     * @throws SdpParseException
     *             SdpParseException - if there is a problem parsing the String.
     * @return a populated SessionDescription object.
     */
    public SessionDescription createSessionDescription(String s)
            throws SdpParseException {
        try {

            SDPAnnounceParser sdpParser = new SDPAnnounceParser(s);
            return sdpParser.parse();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new SdpParseException(0, 0, "Could not parse message");
        }
    }

    /**
     * Returns Bandwidth object with the specified values.
     *
     * @param modifier
     *            modifier - the bandwidth type
     * @param value
     *            the bandwidth value measured in kilobits per second
     * @return bandwidth
     */
    public BandWidth createBandwidth(String modifier, int value) {
        BandwidthField bandWidthImpl = new BandwidthField();
        try {

            bandWidthImpl.setType(modifier);
            bandWidthImpl.setValue(value);

        } catch (SdpException s) {
            s.printStackTrace();
        }
        return bandWidthImpl;
    }

    /**
     * Returns Attribute object with the specified values.
     *
     * @param name
     *            the namee of the attribute
     * @param value
     *            the value of the attribute
     * @return Attribute
     */
    public Attribute createAttribute(String name, String value) {
        AttributeField attributeImpl = new AttributeField();
        try {

            attributeImpl.setName(name);
            attributeImpl.setValueAllowNull(value);

        } catch (SdpException s) {
            s.printStackTrace();
        }
        return attributeImpl;
    }

    /**
     * Returns Info object with the specified value.
     *
     * @param value
     *            the string containing the description.
     * @return Info
     */
    public Info createInfo(String value) {
        InformationField infoImpl = new InformationField();
        try {

            infoImpl.setValue(value);

        } catch (SdpException s) {
            s.printStackTrace();
        }
        return infoImpl;
    }

    /**
     * Returns Phone object with the specified value.
     *
     * @param value
     *            the string containing the description.
     * @return Phone
     */
    public Phone createPhone(String value) {
        PhoneField phoneImpl = new PhoneField();
        try {

            phoneImpl.setValue(value);

        } catch (SdpException s) {
            s.printStackTrace();
        }
        return phoneImpl;
    }

    /**
     * Returns EMail object with the specified value.
     *
     * @param value
     *            the string containing the description.
     * @return EMail
     */
    public EMail createEMail(String value) {
        EmailField emailImpl = new EmailField();
        try {

            emailImpl.setValue(value);

        } catch (SdpException s) {
            s.printStackTrace();
        }
        return emailImpl;
    }

    /**
     * Returns URI object with the specified value.
     *
     * @param value
     *            the URL containing the description.
     * @throws SdpException
     * @return URI
     */
    public javax.sdp.URI createURI(URL value) throws SdpException {

        URIField uriImpl = new URIField();
        uriImpl.set(value);
        return uriImpl;

    }

    /**
     * Returns SessionName object with the specified name.
     *
     * @param name
     *            the string containing the name of the session.
     * @return SessionName
     */
    public SessionName createSessionName(String name) {
        SessionNameField sessionNameImpl = new SessionNameField();
        try {

            sessionNameImpl.setValue(name);

        } catch (SdpException s) {
            s.printStackTrace();
        }
        return sessionNameImpl;
    }

    /**
     * Returns Key object with the specified value.
     *
     * @param method
     *            the string containing the method type.
     * @param key
     *            the key to set
     * @return Key
     */
    public Key createKey(String method, String key) {
        KeyField keyImpl = new KeyField();
        try {

            keyImpl.setMethod(method);
            keyImpl.setKey(key);

        } catch (SdpException s) {
            s.printStackTrace();
            return null;
        }
        return keyImpl;
    }

    /**
     * Returns Version object with the specified values.
     *
     * @param value
     *            the version number.
     * @return Version
     */
    public Version createVersion(int value) {
        ProtoVersionField protoVersionField = new ProtoVersionField();
        try {

            protoVersionField.setVersion(value);

        } catch (SdpException s) {
            s.printStackTrace();
            return null;
        }
        return protoVersionField;
    }

    /**
     * Returns Media object with the specified properties.
     *
     * @param media
     *            the media type, eg "audio"
     * @param port
     *            port number on which to receive media
     * @param numPorts
     *            number of ports used for this media stream
     * @param transport
     *            transport type, eg "RTP/AVP"
     * @param staticRtpAvpTypes
     *            vector to set
     * @throws SdpException
     * @return Media
     */
    public Media createMedia(String media, int port, int numPorts,
            String transport, Vector staticRtpAvpTypes) throws SdpException {
        MediaField mediaImpl = new MediaField();
        mediaImpl.setMediaType(media);
        mediaImpl.setMediaPort(port);
        mediaImpl.setPortCount(numPorts);
        mediaImpl.setProtocol(transport);
        mediaImpl.setMediaFormats(staticRtpAvpTypes);
        return mediaImpl;
    }

    /**
     * Returns Origin object with the specified properties.
     *
     * @param userName
     *            the user name.
     * @param address
     *            the IP4 encoded address.
     * @throws SdpException
     *             if the parameters are null
     * @return Origin
     */
    public Origin createOrigin(String userName, String address)
            throws SdpException {
        OriginField originImpl = new OriginField();
        originImpl.setUsername(userName);
        originImpl.setAddress(address);
        originImpl.setNetworkType(SDPKeywords.IN);
        originImpl.setAddressType(SDPKeywords.IPV4);
        return originImpl;
    }

    /**
     * Returns Origin object with the specified properties.
     *
     * @param userName
     *            String containing the user that created the string.
     * @param sessionId
     *            long containing the session identifier.
     * @param sessionVersion
     *            long containing the session version.
     * @param networkType
     *            String network type for the origin (usually "IN").
     * @param addrType
     *            String address type (usually "IP4").
     * @param address
     *            String IP address usually the address of the host.
     * @throws SdpException
     *             if the parameters are null
     * @return Origin object with the specified properties.
     */
    public Origin createOrigin(String userName, long sessionId,
            long sessionVersion, String networkType, String addrType,
            String address) throws SdpException {
        OriginField originImpl = new OriginField();
        originImpl.setUsername(userName);
        originImpl.setAddress(address);
        originImpl.setSessionId(sessionId);
        originImpl.setSessionVersion(sessionVersion);
        originImpl.setAddressType(addrType);
        originImpl.setNetworkType(networkType);
        return originImpl;
    }

    /**
     * Returns MediaDescription object with the specified properties. The
     * returned object will respond to Media.getMediaFormats(boolean) with a
     * Vector of media formats.
     *
     * @param media
     *            media -
     * @param port
     *            port number on which to receive media
     * @param numPorts
     *            number of ports used for this media stream
     * @param transport
     *            transport type, eg "RTP/AVP"
     * @param staticRtpAvpTypes
     *            list of static RTP/AVP media payload types which should be
     *            specified by the returned MediaDescription throws
     *            IllegalArgumentException if passed an invalid RTP/AVP payload
     *            type
     * @throws IllegalArgumentException
     * @throws SdpException
     * @return MediaDescription
     */
    public MediaDescription createMediaDescription(String media, int port,
            int numPorts, String transport, int[] staticRtpAvpTypes)
            throws IllegalArgumentException, SdpException {
        MediaDescriptionImpl mediaDescriptionImpl = new MediaDescriptionImpl();
        MediaField mediaImpl = new MediaField();
        mediaImpl.setMediaType(media);
        mediaImpl.setMediaPort(port);
        mediaImpl.setPortCount(numPorts);
        mediaImpl.setProtocol(transport);
        mediaDescriptionImpl.setMedia(mediaImpl);
        // Bug fix contributed by Paloma Ortega.
        Vector payload = new Vector();
        for (int i = 0; i < staticRtpAvpTypes.length; i++)
            payload.add(new Integer(staticRtpAvpTypes[i]).toString());
        mediaImpl.setMediaFormats(payload);
        return mediaDescriptionImpl;
    }

    /**
     * Returns MediaDescription object with the specified properties. The
     * returned object will respond to Media.getMediaFormats(boolean) with a
     * Vector of String objects specified by the 'formats argument.
     *
     * @param media
     *            the media type, eg "audio"
     * @param port
     *            port number on which to receive media
     * @param numPorts
     *            number of ports used for this media stream
     * @param transport
     *            transport type, eg "RTP/AVP"
     * @param formats
     *            list of formats which should be specified by the returned
     *            MediaDescription
     * @return MediaDescription
     */
    public MediaDescription createMediaDescription(String media, int port,
            int numPorts, String transport, String[] formats) {
        MediaDescriptionImpl mediaDescriptionImpl = new MediaDescriptionImpl();
        try {

            MediaField mediaImpl = new MediaField();
            mediaImpl.setMediaType(media);
            mediaImpl.setMediaPort(port);
            mediaImpl.setPortCount(numPorts);
            mediaImpl.setProtocol(transport);

            Vector formatsV = new Vector(formats.length);
            for (int i = 0; i < formats.length; i++)
                formatsV.add(formats[i]);
            mediaImpl.setMediaFormats(formatsV);
            mediaDescriptionImpl.setMedia(mediaImpl);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return mediaDescriptionImpl;
    }

    /**
     * Returns TimeDescription object with the specified properties.
     *
     * @param t
     *            the Time that the time description applies to. Returns
     *            TimeDescription object with the specified properties.
     * @throws SdpException
     * @return TimeDescription
     */
    public TimeDescription createTimeDescription(Time t) throws SdpException {
        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        timeDescriptionImpl.setTime(t);
        return timeDescriptionImpl;
    }

    /**
     * Returns TimeDescription unbounded (i.e. "t=0 0");
     *
     * @throws SdpException
     * @return TimeDescription unbounded (i.e. "t=0 0");
     */
    public TimeDescription createTimeDescription() throws SdpException {
        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        TimeField timeImpl = new TimeField();
        timeImpl.setZero();
        timeDescriptionImpl.setTime(timeImpl);
        return timeDescriptionImpl;
    }

    /**
     * Returns TimeDescription object with the specified properties.
     *
     * @param start
     *            start time.
     * @param stop
     *            stop time.
     * @throws SdpException
     *             if the parameters are null
     * @return TimeDescription
     */
    public TimeDescription createTimeDescription(Date start, Date stop)
            throws SdpException {
        TimeDescriptionImpl timeDescriptionImpl = new TimeDescriptionImpl();
        TimeField timeImpl = new TimeField();
        timeImpl.setStart(start);
        timeImpl.setStop(stop);
        timeDescriptionImpl.setTime(timeImpl);
        return timeDescriptionImpl;
    }

    /**
     * Returns a String containing the computed form for a multi-connection
     * address. Parameters: addr - connection address ttl - time to live (TTL)
     * for multicast addresses numAddrs - number of addresses used by the
     * connection Returns: a String containing the computed form for a
     * multi-connection address.
     */
    public String formatMulticastAddress(String addr, int ttl, int numAddrs) {
        String res = addr + "/" + ttl + "/" + numAddrs;
        return res;
    }

    /**
     * Returns a Connection object with the specified properties a
     *
     * @param netType
     *            network type, eg "IN" for "Internet"
     * @param addrType
     *            address type, eg "IP4" for IPv4 type addresses
     * @param addr
     *            connection address
     * @param ttl
     *            time to live (TTL) for multicast addresses
     * @param numAddrs
     *            number of addresses used by the connection
     * @return Connection
     */
    public Connection createConnection(String netType, String addrType,
            String addr, int ttl, int numAddrs) throws SdpException {
        ConnectionField connectionImpl = new ConnectionField();

        connectionImpl.setNetworkType(netType);
        connectionImpl.setAddressType(addrType);
        connectionImpl.setAddress(addr);

        return connectionImpl;
    }

    /**
     * Returns a Connection object with the specified properties and no TTL and
     * a default number of addresses (1).
     *
     * @param netType
     *            network type, eg "IN" for "Internet"
     * @param addrType
     *            address type, eg "IP4" for IPv4 type addresses
     * @param addr
     *            connection address
     * @throws SdpException
     *             if the parameters are null
     * @return Connection
     */
    public Connection createConnection(String netType, String addrType,
            String addr) throws SdpException {
        ConnectionField connectionImpl = new ConnectionField();

        connectionImpl.setNetworkType(netType);
        connectionImpl.setAddressType(addrType);
        connectionImpl.setAddress(addr);

        return connectionImpl;
    }

    /**
     * Returns a Connection object with the specified properties and a network
     * and address type of "IN" and "IP4" respectively.
     *
     * @param addr
     *            connection address
     * @param ttl
     *            time to live (TTL) for multicast addresses
     * @param numAddrs
     *            number of addresses used by the connection
     * @return Connection
     */
    public Connection createConnection(String addr, int ttl, int numAddrs)
            throws SdpException {
        ConnectionField connectionImpl = new ConnectionField();

        connectionImpl.setAddress(addr);

        return connectionImpl;
    }

    /**
     * Returns a Connection object with the specified address. This is
     * equivalent to
     *
     * createConnection("IN", "IP4", addr);
     *
     * @param addr
     *            connection address
     * @throws SdpException
     *             if the parameter is null
     * @return Connection
     */
    public Connection createConnection(String addr) throws SdpException {

        return createConnection("IN", "IP4", addr);

    }

    /**
     * Returns a Time specification with the specified start and stop times.
     *
     * @param start
     *            start time
     * @param stop
     *            stop time
     * @throws SdpException
     *             if the parameters are null
     * @return a Time specification with the specified start and stop times.
     */
    public Time createTime(Date start, Date stop) throws SdpException {
        TimeField timeImpl = new TimeField();
        timeImpl.setStart(start);
        timeImpl.setStop(stop);
        return timeImpl;
    }

    /**
     * Returns an unbounded Time specification (i.e., "t=0 0").
     *
     * @throws SdpException
     * @return an unbounded Time specification (i.e., "t=0 0").
     */
    public Time createTime() throws SdpException {
        TimeField timeImpl = new TimeField();
        timeImpl.setZero();
        return timeImpl;
    }

    /**
     * Returns a RepeatTime object with the specified interval, duration, and
     * time offsets.
     *
     * @param repeatInterval
     *            the "repeat interval" in seconds
     * @param activeDuration
     *            the "active duration" in seconds
     * @param offsets
     *            the list of offsets relative to the start time of the Time
     *            object with which the returned RepeatTime will be associated
     * @return RepeatTime
     */
    public RepeatTime createRepeatTime(int repeatInterval, int activeDuration,
            int[] offsets) {
        RepeatField repeatTimeField = new RepeatField();
        try {

            repeatTimeField.setRepeatInterval(repeatInterval);
            repeatTimeField.setActiveDuration(activeDuration);
            repeatTimeField.setOffsetArray(offsets);

        } catch (SdpException s) {
            s.printStackTrace();
        }
        return repeatTimeField;
    }

    /**
     * Constructs a timezone adjustment record.
     *
     * @param d
     *            the Date at which the adjustment is going to take place.
     * @param offset
     *            the adjustment in number of seconds relative to the start time
     *            of the SessionDescription with which this object is
     *            associated.
     * @return TimeZoneAdjustment
     */
    public TimeZoneAdjustment createTimeZoneAdjustment(Date d, int offset) {
        ZoneField timeZoneAdjustmentImpl = new ZoneField();
        try {

            Hashtable map = new Hashtable();
            map.put(d, new Integer(offset));
            timeZoneAdjustmentImpl.setZoneAdjustments(map);
        } catch (SdpException s) {
            s.printStackTrace();
        }
        return timeZoneAdjustmentImpl;
    }

    /**
     * Test main.
     *
     * @param args
     * @throws SdpException
     *             public static void main(String[] args) throws SdpException {
     *  }
     */

    /**
     * @param ntpTime
     *            long to set
     * @return Returns a Date object for a given NTP date value.
     */
    public static Date getDateFromNtp(long ntpTime) {
        return new Date((ntpTime - SdpConstants.NTP_CONST) * 1000);
    }

    /**
     * Returns a long containing the NTP value for a given Java Date.
     *
     * @param d
     *            Date to set
     * @return long
     */
    public static long getNtpTime(Date d) throws SdpParseException {
        if (d == null)
            return -1;
        return ((d.getTime() / 1000) + SdpConstants.NTP_CONST);
    }

    public static void main(String[] args) throws SdpParseException,
            SdpException {

        String sdpFields = "v=0\r\n"
                + "o=CiscoSystemsSIP-GW-UserAgent 2578 3027 IN IP4 83.211.215.216\r\n"
                + "s=SIP Call\r\n" + "c=IN IP4 62.94.199.36\r\n" + "t=0 0\r\n"
                + "m=audio 62278 RTP/AVP 18 8 0 4 3 125 101 19\r\n"
                + "c=IN IP4 62.94.199.36\r\n" + "a=rtpmap:18 G729/8000\r\n"
                + "a=fmtp:18 annexb=yes\r\n" + "a=rtpmap:8 PCMA/8000\r\n"
                + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                + "a=fmtp:4 bitrate=5.3;annexa=no\r\n"
                + "a=rtpmap:3 GSM/8000\r\n" + "a=rtpmap:125 X-CCD/8000\r\n"
                + "a=rtpmap:101 telephone-event/8000\r\n"
                + "a=fmtp:101 0-16\r\n" + "a=rtpmap:19 CN/8000\r\n"
                + "a=direction:passive\r\n";

        SdpFactory sdpFactory = new SdpFactory();
        SessionDescription sessionDescription = sdpFactory
                .createSessionDescription(sdpFields);

        System.out.println("sessionDescription = " + sessionDescription);
        Vector mediaDescriptions = sessionDescription
                .getMediaDescriptions(true);

        for (int i = 0; i < mediaDescriptions.size(); i++) {
            MediaDescription m = (MediaDescription) mediaDescriptions
                    .elementAt(i);
            ((MediaDescriptionImpl) m).setDuplexity("sendrecv");
            System.out.println("m = " + m.toString());
            Media media = m.getMedia();
            Vector formats = media.getMediaFormats(false);
            System.out.println("formats = " + formats);
        }
    }

}
