
/** An example that illustrates template matching on SIP headers and messages.
* This uses regular expressions for matching on portions of sip messages.
* This is useful for situations when say you want to match with certain
* response classes or request URIs or whatever.
* You construct a match template that can consist of portions that are exact
* matches and portions that use regular expressions for matching.
* You might find this useful for bulding test frameworks.
*/
package examples.nistgoodies.match;
import gov.nist.core.Match;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.message.MessageFactoryImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;

import java.util.regex.Pattern;

import javax.sip.SipFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;


class Matcher implements Match {
   Pattern re;

   Matcher (String matchExpr)  {
       re = Pattern.compile(matchExpr);
   }

   public boolean match(String toMatch) {
    java.util.regex.Matcher m = re.matcher("aaaaab");
    boolean b = m.matches();
    return b;
   }

}

public class RegexpMatchTest {
static final String message1 = "INVITE sip:joe@blah.com SIP/3.0\r\n"+
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

static final String message2 = "SIP/2.0 200 OK\r\n"+
"Via: SIP/2.0/UDP 129.6.55.18:5060\r\n"+
"From: \"3ComIII\" <sip:13019768226@129.6.55.78>;tag=e13b4296\r\n"+
"To: \"3ComIII\" <sip:13019768226@129.6.55.78>\r\n"+
"Call-Id: c5ab5808@129.6.55.18\r\n"+
"CSeq: 49455 REGISTER\r\n"+
"Expires: 1200\r\n"+
"Contact: <sip:13019768226@129.6.55.18>;expires=1199;action=proxy\r\n"+
"Content-Length: 0\r\n"+
"\r\n";


    public static void main(String args[]) throws Exception {
        SIPRequest template = new SIPRequest();
        RequestLine requestLine  = new RequestLine();
        SipFactory sipFactory = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        // AddressFactory addressFactory = sipFactory.createAddressFactory();
        SipUri uri = new SipUri();
        // trap invites on company.com domain for incoming SIP
        // invitations.
         uri.setMatcher(new Matcher("sip:[^.]*company.com"));
         requestLine.setMethod(Request.INVITE);
         requestLine.setUri(uri);
         template.setRequestLine(requestLine);
         MessageFactory messageFactory = sipFactory.createMessageFactory();
        try {



            SIPRequest sipMessage = (SIPRequest) messageFactory.createRequest(message1);
            System.out.println("Match returned " +
                sipMessage.match(template));

        } catch (Exception ex) {
           ex.printStackTrace();
           System.exit(0);
        }

        StatusLine statusLine = new StatusLine();
        // matches on all 2XX,1xx and 4xx responses.
        statusLine.setMatcher(new Matcher("SIP/2.0 [1,2,4][0-9][0-9]"));
        SIPResponse responseTemplate = new SIPResponse();
        responseTemplate.setStatusLine(statusLine);
        try {

            SIPResponse sipResponse  = (SIPResponse) ((MessageFactoryImpl) messageFactory).createResponse(message2);
            System.out.println("Match returned " +
                 sipResponse.match(responseTemplate));

        } catch (Exception ex) {
           ex.printStackTrace();
           System.exit(0);
        }


    }
}
