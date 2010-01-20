package examples.parser;

import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import gov.nist.javax.sip.header.ims.PPreferredIdentityHeader;

import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * This example shows you how you can use the message factory to parse SIP
 * messages. You dont need to create a sip stack for this example.
 *
 * @author M. Ranganathan
 *
 */
public class Parser {

    public static void main(String[] args) throws Exception {
        SipFactory sipFactory = null;
        HeaderFactory headerFactory;
        AddressFactory addressFactory;
        MessageFactory messageFactory;

        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
        // If you get a request from a socket, you can use the jsip api to parse it.
        String request = "INVITE sip:00001002000022@p25dr;user=TIA-P25-SU SIP/2.0\r\n"
                + "CSeq: 1 INVITE\r\n"
                + "From: <sip:0000100200000c@p25dr;user=TIA-P25-SU>;tag=841\r\n"
                + "To: <sip:00001002000022@p25dr;user=TIA-P25-SU>\r\n"
                + "Via: SIP/2.0/UDP 02.002.00001.p25dr;branch=z9hG4bKa10f04383e3d8e8dbf3f6d06f6bb6880\r\n"
                + "Max-Forwards: 70\r\n"
                + "Route: <sip:TIA-P25-U2UOrig@01.002.00001.p25dr;lr>,<sip:TIA-P25-U2UDest@03.002.00001.p25dr;lr>\r\n"
                + "Contact: <sip:02.002.00001.p25dr>\r\n"
                + "Timestamp: 1154567665687\r\n"
                + "Allow: REGISTER,INVITE,ACK,BYE,CANCEL\r\n"
                + "Accept: application/sdp ;level=1,application/x-tia-p25-issi\r\n"
                + "Call-ID: c6a12ddad0ddc1946d9f443c884a7768@127.0.0.1\r\n"
                + "Content-Type: application/sdp;level=1\r\n"
                + "P-Asserted-Identity: <sip:x>\r\n"
                + "P-Preferred-Identity: <sip:x>\r\n"
                + "Content-Length: 145\r\n\r\n"
                + "v=0\r\n"
                + "o=- 30576 0 IN IP4 127.0.0.1\r\n"
                + "s=TIA-P25-SuToSuCall\r\n"
                + "t=0 0\r\n"
                + "c=IN IP4 127.0.0.1\r\n"
                + "m=audio 12412 RTP/AVP 100\r\n"
                + "a=rtpmap:100 X-TIA-P25-IMBE/8000\r\n";
        Request sipRequest = messageFactory.createRequest(request);
        byte[] contentBytes = sipRequest.getRawContent();
        String contentString = new String(contentBytes);
        //SdpFactory sdpFactory = SdpFactory.getInstance();
        //SessionDescription sd = sdpFactory
        //      .createSessionDescription(contentString);

        PAssertedIdentityHeader h = (PAssertedIdentityHeader)sipRequest.getHeader(PAssertedIdentityHeader.NAME);
        System.out.println( h.getClass() );
        System.out.println( h instanceof ExtensionHeader );
        System.out.println( h instanceof PAssertedIdentityHeader );

        PPreferredIdentityHeader h2 = (PPreferredIdentityHeader) sipRequest.getHeader(PPreferredIdentityHeader.NAME);
        System.out.println( h2.getClass() );
        System.out.println( h2 instanceof ExtensionHeader );
        System.out.println( h2 instanceof PPreferredIdentityHeader );


        System.out.println("Parsed SIPRequest is :\n" + sipRequest.toString());
        //System.out.println("Parsed Content is :\n" + sd.toString());

        // Similarly, if you get a response via a socket, you can use the jsip api to parse it
        String response = "SIP/2.0 200 OK\r\n"+
        "CSeq: 1 INVITE\r\n"+
        "From: <sip:0000100200000c@p25dr;user=TIA-P25-SU>;tag=397\r\n"+
        "To: <sip:00001002000022@p25dr;user=TIA-P25-SU>;tag=466\r\n"+
        "Via: SIP/2.0/UDP 01.002.00001.p25dr;branch=z9hG4bK7d2479f1f7d746f315664fb5d0e85d08," +
        "SIP/2.0/UDP 02.002.00001.p25dr;branch=z9hG4bKa10f04383e3d8e8dbf3f6d06f6bb6880\r\n"+
        "Call-ID: c6a12ddad0ddc1946d9f443c884a7768@127.0.0.1\r\n" +
        "Record-Route: <sip:03.002.00001.p25dr;lr>,<sip:01.002.00001.p25dr;lr>\r\n"+
        "Contact: <sip:04.002.00001.p25dr;user=TIA-P25-SU>\r\n"+
        "Timestamp: 1154567665687\r\n"+
        "Content-Type: application/sdp;level=1\r\n"+
        "Content-Length: 145\r\n\r\n"+
        "v=0\r\n"+
        "o=- 30576 0 in ip4 127.0.0.1\r\n"+
        "s=tia-p25-sutosucall\r\n"+
        "c=in ip4 127.0.0.1\r\n"+
        "t=0 0\r\n"+
        "m=audio 12230 rtp/avp 100\r\n"+
        "a=rtpmap:100 x-tia-p25-imbe/8000\r\n";

        Response sipResponse = messageFactory.createResponse(response);
        System.out.println("Parsed SIP Response is :\n" + sipResponse);
        contentBytes = sipResponse.getRawContent();
        contentString = new String(contentBytes);
        SdpFactory sdpFactory = SdpFactory.getInstance();
        SessionDescription sd = sdpFactory.createSessionDescription(contentString);
        System.out.println("Parsed Content is :\n" + sd.toString());

    }

}
