package test.gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import junit.framework.TestCase;

public class SdpParserTest extends TestCase {

	String sdpData[] = {
			"\r\n    " + "v=0\r\n"
					+ "o=4855 13760799956958020 13760799956958020"
					+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
					+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
					+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
					+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
					+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n",

			"v=0\r\n" + "o=root 14539767 1208 IN IP4 66.237.65.67\r\n"
					+ "s=session\r\n" + "t=0 0\r\n"
					+ "m=audio 38658 RTP/AVP 3 110 97 0 8 101\r\n"
					+ "c=IN IP4 66.237.65.67\r\n" + "a=rtpmap:3 GSM/8000\r\n"
					+ "a=rtpmap:110 speex/8000\r\n"
					+ "a=rtpmap:97 iLBC/8000\r\n" + "a=rtpmap:0 PCMU/8000\r\n"
					+ "a=rtpmap:8 PCMA/8000\r\n"
					+ "a=rtpmap:101 telephone-event/8000\r\n"
					+ "a=fmtp:101 0-16\r\n" + "a=silenceSupp:off - - - -\r\n" };

	public void testSdpParser() throws Exception {
		for (String sdpdata : sdpData) {
			SDPAnnounceParser parser = new SDPAnnounceParser(sdpdata);
			SessionDescriptionImpl sessiondescription = parser.parse();
			SessionDescriptionImpl sessiondescription1 = new SDPAnnounceParser(
					sessiondescription.toString()).parse();
			// Unfortunately equals is not yet implemented.
			//assertEquals("Equality check", sessiondescription,sessiondescription1);
		}

	}

}
