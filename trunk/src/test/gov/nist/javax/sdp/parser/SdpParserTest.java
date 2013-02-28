package test.gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.sdp.Attribute;
import javax.sdp.Connection;
import javax.sdp.MediaDescription;

import junit.framework.TestCase;

public class SdpParserTest extends TestCase {

    String sdpData[] = {
        
          "\r\n " + "v=0\r\n" + "o=4855 13760799956958020 13760799956958020" + " IN IP4 129.6.55.78\r\n" + "s=mysession session\r\n"
          + "p=+46 8 52018010\r\n" + "c=IN IP4 129.6.55.78\r\n" + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n" 
          + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n" + "a=rtpmap:18 G729A/8000\r\n" +
          "a=ptime:20\r\n",
          
          "v=0\r\n" + "o=root 14539767 1208 IN IP4 66.237.65.67\r\n" + "s=session\r\n"
          + "t=0 0\r\n" + "m=audio 38658 RTP/AVP 3 110 97 0 8 101\r\n" + "c=IN IP4 66.237.65.67\r\n" +
          "a=rtpmap:3 GSM/8000\r\n" + "a=rtpmap:110 speex/8000\r\n" 
          + "a=rtpmap:97 iLBC/8000\r\n" +
          "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:8 PCMA/8000\r\n" 
          + "a=rtpmap:101 telephone-event/8000\r\n" + "a=fmtp:101 0-16\r\n" + "a=silenceSupp:off - - - -\r\n",
          
          "v=0\r\n" + "o=Cisco-SIPUA 10163 1 IN IP4 192.168.0.103\r\n" + "s=SIP Call\r\n" 
          + "t=0 0\r\n" + "m=audio 27866 RTP/AVP 0 8 18 101\r\n" + "c=IN IP4 192.168.0.103\r\n" +
          "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:8 PCMA/8000\r\n" + "a=rtpmap:18 G729/8000\r\n" +
          "a=fmtp:18 annexb=no\r\n" + "a=rtpmap:101 telephone-event/8000\r\n" 
          + "a=fmtp:101 0-15\r\n" + "a=sendonly\r\n" ,
         
        "v=0\r\n" + "o=- 1167770389 1167770390 IN IP4 192.168.5.242\r\n"
                + "s=Polycom IP Phone\r\n" + "c=IN IP4 192.168.5.242\r\n" + "t=0 0\r\n"
                + "a=sendonly\r\n" + "m=audio 2222 RTP/AVP 0 101\r\n"
                + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:101 telephone-event/8000\r\n"
    };
    
    String rtcSdp = "v=0\n" + 
    		"o=- 3212632920 2 IN IP4 127.0.0.1\n" + 
    		"s=plivo\n" + 
    		"t=0 0\n" + 
    		"a=group:BUNDLE audio video\n" + 
    		"m=audio 49665 RTP/SAVPF 103 104 0 8 106 105 13 126\n" + 
    		"c=IN IP4 192.168.3.1\n" + 
    		"a=rtcp:49665 IN IP4 93.63.22.6\n" + 
    		"a=candidate:1668076467 1 udp 2113937151 192.168.1.4 54624 typ host generation 0\n" + 
    		"a=candidate:1668076467 2 udp 2113937151 192.168.1.4 54624 typ host generation 0\n" + 
    		"a=candidate:3794064647 1 udp 1677729535 93.63.22.6 49665 typ srflx generation 0\n" + 
    		"a=candidate:3794064647 2 udp 1677729535 93.63.22.6 49665 typ srflx generation 0\n" + 
    		"a=candidate:770649923 1 tcp 1509957375 192.168.1.4 64263 typ host generation 0\n" + 
    		"a=candidate:770649923 2 tcp 1509957375 192.168.1.4 64263 typ host generation 0\n" + 
    		"a=ice-ufrag:DWtY72g0C9JhcJtl\n" + 
    		"a=ice-pwd:GGGqAh3oxbFT3NfUYvAWcAH4\n" + 
    		"a=ice-options:google-ice\n" + 
    		"a=sendrecv\n" + 
    		"a=mid:audio\n" + 
    		"a=rtcp-mux\n" + 
    		"a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:i3ZhWGTXvX8vxKxtht+lCLsT/nhuyM2rgDQFInTx\n" + 
    		"a=rtpmap:103 ISAC/16000\n" + 
    		"a=rtpmap:104 ISAC/32000\n" + 
    		"a=rtpmap:0 PCMU/8000\n" + 
    		"a=rtpmap:8 PCMA/8000\n" + 
    		"a=rtpmap:106 CN/32000\n" + 
    		"a=rtpmap:105 CN/16000\n" + 
    		"a=rtpmap:13 CN/8000\n" + 
    		"a=rtpmap:126 telephone-event/8000\n" + 
    		"a=ssrc:2399224977 cname:Wq4cj1yaLwKIQPRA\n" + 
    		"a=ssrc:2399224977 mslabel:SeAfUDCzSeGWhdcVyHTVIt9HBI2acjoawxkI\n" + 
    		"a=ssrc:2399224977 label:SeAfUDCzSeGWhdcVyHTVIt9HBI2acjoawxkI00\n";
    
    public void testWebRtcSdpParser() throws Exception {
            SDPAnnounceParser parser = new SDPAnnounceParser(rtcSdp);
            SessionDescriptionImpl sessiondescription = parser.parse();
            sessiondescription.getAttribute("crypto:1");
            String nt = sessiondescription.getConnection()==null?null:sessiondescription.getConnection().getNetworkType();
            MediaDescription md = (MediaDescription) sessiondescription.getMediaDescriptions(false).get(0);
            nt = md.getConnection().getNetworkType();
            assertNotNull(nt);
            assertNotNull(md);

    }

    public void testSdpParser() throws Exception {
        for (String sdpdata : sdpData) {
            SDPAnnounceParser parser = new SDPAnnounceParser(sdpdata);
            SessionDescriptionImpl sessiondescription = parser.parse();

            Vector attrs = sessiondescription.getAttributes(false);

            if (attrs != null) {
                Attribute attrib = (Attribute) attrs.get(0);
                System.out.println("attrs = " + attrib.getName());
            }

            MediaDescription md = (MediaDescription) sessiondescription.getMediaDescriptions(
                    false).get(0);

            System.out.println("md attributes " + md.getAttributes(false));

            SessionDescriptionImpl sessiondescription1 = new SDPAnnounceParser(sessiondescription
                    .toString()).parse();

            System.out.println("sessionDescription1 " + sessiondescription1);

            assertNotNull(sessiondescription1);
            // Unfortunately equals is not yet implemented.
            // assertEquals("Equality check",
            // sessiondescription,sessiondescription1);
            
            // Check if SDP is serializable
            File outFile = File.createTempFile("sdpObj",".dat");
            outFile.deleteOnExit();
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(outFile, false));
            os.writeObject(sessiondescription1);
        }

    }

}
