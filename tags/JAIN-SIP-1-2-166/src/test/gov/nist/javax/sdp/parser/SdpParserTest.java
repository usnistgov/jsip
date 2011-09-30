package test.gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import javax.sdp.Attribute;
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
