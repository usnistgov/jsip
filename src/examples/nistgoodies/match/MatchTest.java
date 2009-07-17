/** An example that illustrates template matching on SIP headers and messages.
* This uses regular expressions for matching on portions of sip messages.
* This is useful for situations when say you want to match with certain
* response classes or request URIs or whatever.
* You construct a match template that can consist of portions that are exact
* matches and portions that use regular expressions for matching.
* This example uses plain strings for patters but you can do wildcard
* matching.
*/

package examples.nistgoodies.match;

import javax.sip.*;
import javax.sip.header.*;
import javax.sip.message.*;

import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.message.*;


public class MatchTest {


static final String message1 = "INVITE sip:joe@company.com SIP/2.0\r\n"+
"To: sip:joe@company.com\r\n"+
"From: sip:caller@university.edu ;tag=1234\r\n"+
"Call-ID: 0ha0isnda977644900765@10.0.0.1\r\n"+
"CSeq: 9 INVITE\r\n"+
"Via: SIP/2.0/UDP 135.180.130.133\r\n"+
"Content-Type: application/sdp\r\n"+
"\r\n"+
"v=0\r\n"+
"o=mhandley 29739 7272939 IN IP4 126.5.4.3\r\n" +
"c=IN IP4 135.180.130.88\r\n" +
"m=video 3227 RTP/AVP 31\r\n" +
"m=audio 4921 RTP/AVP 12\r\n" +
"a=rtpmap:31 LPC\r\n";



    public static void main( String[] args  ) throws Exception {
        SipFactory sipFactory = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        MessageFactory messageFactory = sipFactory.createMessageFactory();
        Message message = messageFactory.createRequest(message1);

        // Create an empty request.
        Message matchTemplate = messageFactory.createRequest(null);

        HeaderFactory headerFactory = sipFactory.createHeaderFactory();

        CSeqHeader cseqHeader =
            headerFactory.createCSeqHeader(1L,Request.INVITE);
        gov.nist.javax.sip.header.CSeq cseq = (CSeq)cseqHeader;


        matchTemplate.setHeader(cseqHeader);

        boolean retval =
        ((SIPRequest) message).match((SIPRequest)matchTemplate);

        System.out.println("match returned = " + retval);

        cseq.setMethod(Request.SUBSCRIBE);

        retval = ((SIPRequest) message).match((SIPRequest)matchTemplate);
        System.out.println("match returned = " + retval);


    }


}
