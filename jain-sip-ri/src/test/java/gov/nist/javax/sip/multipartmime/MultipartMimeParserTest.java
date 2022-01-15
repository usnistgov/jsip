package gov.nist.javax.sip.multipartmime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Request;

import gov.nist.javax.sip.header.ContentType;
import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.message.Content;
import gov.nist.javax.sip.message.MessageExt;
import gov.nist.javax.sip.message.MessageFactoryExt;
import gov.nist.javax.sip.message.MessageFactoryImpl;
import gov.nist.javax.sip.message.MultipartMimeContent;
import gov.nist.javax.sip.message.SIPRequest;
import junit.framework.TestCase;

public class MultipartMimeParserTest extends TestCase {
  private static String messageSting1 = "INVITE sip:5201;phone-context=cdp.udp@nortel.com;user=phone SIP/2.0\r\n"
                                        + "Via: SIP/2.0/TCP 47.11.74.26:5060;branch=z9hG4bKc51760e4d76173c00cd32f19.1;rport=5060\r\n"
                                        + "Via: SIP/2.0/TCP 47.11.27.84:5060;branch=z9hG4bK-34ec-cebbcb-3278c0e6;received=47.11.27.84\r\n"
                                        + "Record-Route: <sip:47.11.74.26@47.11.74.26:5060;transport=tcp;lr>\r\n"
                                        + "From: \"Nirav Sheth2050\" <sip:2108;phone-context=cdp.udp@nortel.com;user=phone>;tag=b5107990-541b0b2f-13c4-40030-34ec-728605b1-34ec\r\n"
                                        + "To: <sip:5201;phone-context=cdp.udp@nortel.com;user=phone>\r\n"
                                        + "Call-ID: b4eda330-541b0b2f-13c4-40030-34ec-55f3f5db-34ec\r\n"
                                        + "CSeq: 1 INVITE\r\n"
                                        + "Contact: <sip:2108;phone-context=cdp.udp@nortel.com:5060;maddr=47.11.27.84;transport=tcp;user=phone>\r\n"
                                        + "Max-Forwards: 69\r\n"
                                        + "Supported: 100rel,x-nortel-sipvc,replaces,timer\r\n"
                                        + "User-Agent: Nortel CS1000 SIP GW release_6.0 version_ssLinux-6.00.18\r\n"
                                        + "P-Asserted-Identity: \"Nirav Sheth2050\" <sip:2108;phone-context=cdp.udp@nortel.com;user=phone>\r\n"
                                        + "Privacy: none\r\n"
                                        + "History-info: <sip:5201;phone-context=cdp.udp@nortel.com;user=phone>;index=1\r\n"
                                        + "X-nt-corr-id: 000000220e182d1508@001bbafd3d0f-2f0b1b42\r\n" + "Min-SE: 0\r\n"
                                        + "Allow: INVITE,ACK,BYE,REGISTER,REFER,NOTIFY,CANCEL,PRACK,OPTIONS,INFO,SUBSCRIBE,UPDATE\r\n"
                                        + "Content-Length: 0\r\n\r\n";
  private static String contentType1 = "Content-Type: multipart/mixed;boundary=unique-boundary-1\r\n";
  private static String contentString1 = "--unique-boundary-1\r\n"
                                         + "Content-Type: application/sdp\r\n\r\n"
                                         + "v=0\r\n"
                                         + "o=- 1 1 IN IP4 47.11.27.84\r\n"
                                         + "s=-\r\n"
                                         + "t=0 0\r\n"
                                         + "m=audio 5200 RTP/AVP 0 8 18 101 111\r\n"
                                         + "c=IN IP4 47.9.17.80\r\n"
                                         + "a=tcap:1 RTP/SAVP\r\n"
                                         + "a=crypto:1 AES_CM_128_HMAC_SHA1_80 inline:+v5UQZeJ2ecQbS/yNQJgnpqHwu2utWwAdpaWh7Ke|2^31|543245432:4\r\n"
                                         + "a=crypto:2 AES_CM_128_HMAC_SHA1_80 inline:+v5UQZeJ2ecQbS/yNQJgnpqHwu2utWwAdpaWh7Ke|2^31\r\n"
                                         + "a=pcfg:1 t=1\r\n" + "a=fmtp:18 annexb=no\r\n" + "a=rtpmap:101 telephone-event/8000\r\n"
                                         + "a=fmtp:101 0-15\r\n" + "a=rtpmap:111 X-nt-inforeq/8000\r\n" + "a=ptime:20\r\n"
                                         + "a=sendrecv\r\n\r\n" + "--unique-boundary-1\r\n"
                                         + "Content-Type: application/x-nt-mcdn-frag-hex;version=ssLinux-6.00.18;base=x2611\r\n"
                                         + "Content-Disposition: signal;handling=optional\r\n\r\n" + "0500c001\r\n"
                                         + "0107130081900000a200\r\n" + "09090f00e9a0830001004000\r\n"
                                         + "131e070011fd1800a1160201010201a1300e8102010582010184020000850104\r\n"
                                         + "1315070011fa0f00a10d02010102020100cc040000748800\r\n" + "1e0403008183\r\n"
                                         + "460e01000a0001000100010000000000\r\n" + "--unique-boundary-1\r\n"
                                         + "Content-Type: application/x-nt-epid-frag-hex;version=ssLinux-6.00.18;base=x2611\r\n"
                                         + "Content-Disposition: signal;handling=optional\r\n\r\n" + "011201\r\n" + "00:1b:ba:fd:3d:0f\r\n"
                                         + "--unique-boundary-1\r\n";

  //usnistgovissue16
  private static final String specificBodySize = 
    "--unique-boundary-1\n\r" +
      "Content-Type: application/sdp\n\r" +
      "\n\r" +
      "v=0\n\r" +
      "o=- 999999 999999 IN IP4 99.99.99.99\n\r" +
      "s=-\n\r" +
      "c=IN IP4 99.99.99.99\n\r" +
      "t=0 0\n\r" +
      "m=audio 99999 RTP/AVP 0 101\n\r" +
      "a=rtpmap:0 PCMU/8000\n\r" +
      "a=rtpmap:101 telephone-event/8000\n\r" +
      "a=fmtp:101 0-15\n\r" +
      "a=maxptime:20\n\r" +
      "a=label:3199936\n\r" +
      "a=inactive\n\r" +
      "m=audio 0 RTP/AVP 0 101\n\r" +
      "a=rtpmap:0 PCMU/8000\n\r" +
      "a=rtpmap:101 telephone-event/8000\n\r" +
      "a=fmtp:101 0-15\n\r" +
      "a=maxptime:20\n\r" +
      "a=inactive\n\r" +
      "a=label:3199937\n\r" +
      "\n\r" +
      "--unique-boundary-1\n\r" +
      "Content-Type: application/rs-metadata+xml\n\r" +
      "Content-Disposition: recording-session\n\r" +
      "\n\r" +
      "<?xml version='1.0' encoding='UTF-8'?>\n" +
      "<recording xmlns='urn:ietf:params:xml:ns:recording'>\n" +
      "\t<datamode>complete</datamode>\n" +
      "\t<session id=\"Rs1tZH5ISWxYdbRm/2R4fA==\">\n" +
      "\t\t<associate-time>2016-10-12T12:07:18</associate-time>\n" +
      "\t\t<extensiondata xmlns:apkt=\"http://acmepacket.com/siprec/extensiondata\">\n" +
      "\t\t\t<apkt:ucid>00FA08XXXXXXXXXXXXXXXX;encoding=hex</apkt:ucid>\n" +
      "\t\t\t<apkt:callerOrig>true</apkt:callerOrig>\n" +
      "\t\t</extensiondata>\n" +
      "\t</session>\n" +
      "\t<participant id=\"nN+uOmP7TNBKfStkgTAKlw==\" session=\"Rs1tZH5ISWxYdbRm/2R4fA==\">\n" +
      "\t\t<nameID aor=\"sip:9999999999@999.999.99.999\">\n" +
      "\t\t\t<name>&quot;XXXXXXXXXXXXXXX&quot;</name>\n" +
      "\t\t</nameID>\n" +
      "\t\t<send>GjCvWYoEQh9zN9TsyCVl/w==</send>\n" +
      "\t\t<associate-time>2016-10-12T12:07:18</associate-time>\n" +
      "\t\t<extensiondata xmlns:apkt=\"http://acmepacket.com/siprec/extensiondata\">\n" +
      "\t\t\t<apkt:callingParty>true</apkt:callingParty>\n" +
      "\t\t\t<apkt:request-uri>sip:9999999999@99.99.999.999:5060</apkt:request-uri>\n" +
      "\t\t\t<apkt:realm>outsideXO</apkt:realm>\n" +
      "\t\t\t<apkt:header label=\"From\">\n" +
      "\t\t\t\t<value>&quot;XXXXXXXXXXXXXXX&quot; &lt;sip:9999999999@999.999.99.999:5060;pstn-params=9999999999&gt;;tag=xxxxxxxxxx</value>\n" +
      "\t\t\t</apkt:header>\n" +
      "\t\t\t<apkt:header label=\"To\">\n" +
      "\t\t\t\t<value>&lt;sip:9999999999@99.99.999.999:5060&gt;</value>\n" +
      "\t\t\t</apkt:header>\n" +
      "\t\t\t<apkt:header label=\"Call-ID\">\n" +
      "\t\t\t\t<value>999999999999999999@999.999.99.999</value>\n" +
      "\t\t\t</apkt:header>\n" +
      "\t\t</extensiondata>\n" +
      "\t</participant>\n" +
      "\t<participant id=\"xvlmDeIwSGZLYOtKGCUQvA==\" session=\"Rs1tZH5ISWxYdbRm/2R4fA==\">\n" +
      "\t\t<nameID aor=\"sip:9999999999@99.99.999.999\">\n" +
      "\t\t\t<name>9999999999</name>\n" +
      "\t\t</nameID>\n" +
      "\t\t<associate-time>2016-10-12T12:07:18</associate-time>\n" +
      "\t\t<extensiondata xmlns:apkt=\"http://acmepacket.com/siprec/extensiondata\">\n" +
      "\t\t\t<apkt:callingParty>false</apkt:callingParty>\n" +
      "\t\t</extensiondata>\n" +
      "\t</participant>\n" +
      "\t<stream id=\"GjCvWYoEQh9zN9TsyCVl/w==\" session=\"Rs1tZH5ISWxYdbRm/2R4fA==\">\n" +
      "\t\t<label>3199936</label>\n" +
      "\t\t<mode>separate</mode>\n" +
      "\t\t<associate-time>2016-10-12T12:07:18</associate-time>\n" +
      "\t</stream>\n" +
      "</recording>\r\n" +
      "--unique-boundary-1--\r\n";

  private static String contentString2 = 
		  "--boundary1\n"
		  + "Content-Type: application/sdp\n"
		  + "\n"
		  + "v=0\n"
		  + "o=IWSPM 2266426 2266426 IN IP4 10.92.9.164\n"
		  + "s=-\n"
		  + "c=IN IP4 10.92.9.164\n"
		  + "t=0 0\n"
		  + "m=audio 31956 RTP/AVP 0 8 18 101\n"
		  + "a=ptime:20\n"
		  + "a=rtpmap:101 telephone-event/8000\n"
		  + "a=fmtp:101 0-15\n"
		  + "\n"
		  + "--boundary1\n"
		  + "Content-Type: application/pidf+xml\n"
		  + "Content-ID: alice123@atlanta.example.com\n"
		  + "\n"
		  + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
		  + "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"\n"
				  + "  xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\"\n"
						  + "  xmlns:gml=\"http://www.opengis.net/gml\" \n"
								  + "   xmlns:cl=\"urn:ietf:params:xml:ns:pidf:geopriv10:civicAddr\"\n"
										  + "entity=\"pres:alice@atlanta.example.com\">\n"
										  + "<tuple id=\"sg89ae\">\n"
										  + "<timestamp>2007-07-09T14:00:00Z</timestamp>\n"
		   + "<status>\n"
		    + "<gp:geopriv>\n"
		    + "\r\n"
		  	+ "<gp:location-info>\n"
		  	  + "<gml:location>\n"
		  		+ "<gml:Point srsName=\"urn:ogc:def:crs:EPSG::4326\">\n"
		  		+ "<gml:pos>33.001111 -96.68142</gml:pos>\n"
		  		+ "</gml:Point>\n"
		  	   + "</gml:location>\n"
		  	+ "</gp:location-info>\n"
		  	+ "<gp:usage-rules>\n"
		  	  + "<gp:retransmission-allowed>no</gp:retransmission-allowed>\n"
		  	  + "<gp:retention-expiry>2007-07-27T18:00:00Z</gp:retention-expiry>\n"
		  	+ "</gp:usage-rules>\n"
		  	+ "<gp:method>DHCP</gp:method>\n"
		  	+ "<gp:provided-by>www.example.com</gp:provided-by>\n"
		    + "</gp:geopriv>\n"
		   + "</status>\n"
		  + "</tuple>\n"
		  + "</presence>\n"
		  + "--boundary1--\n";
  
  private static String simpleContentWithEmptyLine = "\n"
    + "v=0\n"
    + "o=IWSPM 2266426 2266426 IN IP4 10.92.9.164\n"
    + "s=-\n"
    + "c=IN IP4 10.92.9.164\n"
    + "\n"
    + "t=0 0\n"
    + "m=audio 31956 RTP/AVP 0 8 18 101\n"
    + "a=ptime:20\n"
    + "a=rtpmap:101 telephone-event/8000\n"
    + "a=fmtp:101 0-15\n"
    + "\n";
  
  private static String isup = "^A^P`^@\n" 
    +"^C^F^M^Cï¿½ï¿½ï¿½^G^C^P^Tf49^U\n"
    +"\n"
    + "ï¿½^S^U^@atï¿½D^Q^Eï¿½^A^@ï¿½^G^C^P^T6pw\"ï¿½^C^Px'ï¿½&^@^@\"\n"
    + "\n";
  
  private static String multipartContentWithEmptyLine = "\n"
    + "--boundary1\n"
    + "Content-Type: message/sip\n"
    + "\n"
    + "INVITE sip:bob@biloxi.com SIP/2.0\n"
    + "Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bKnashds8\n"
    + "To: Bob <bob@biloxi.com>\n"
    + "From: Alice <alice@atlanta.com>;tag=1928301774\n"
    + "Call-ID: a84b4c76e66710\n"
    + "CSeq: 314159 INVITE\n"
    + "Max-Forwards: 70\n"
    + "Date: Thu, 21 Feb 2002 13:02:03 GMT\n"
    + "Contact: <sip:alice@pc33.atlanta.com>\n"
    + "Content-Type: application/sdp\n"
    + "Content-Length: 147\n"
    + "\n"
    + "v=0\n"
    + "o=UserA 2890844526 2890844526 IN IP4 here.com\n"
    + "s=Session SDP\n"
    + "c=IN IP4 pc33.atlanta.com\n"
    + "t=0 0\n"
    + "m=audio 49172 RTP/AVP 0\n"
    + "a=rtpmap:0 PCMU/8000\n"
    + "\n"
    + "--boundary1\n"
    + "Content-Type: application/pkcs7-signature; name=smime.p7s\n"
    + "Content-Transfer-Encoding: base64\n"
    + "Content-Disposition: attachment; filename=smime.p7s; handling=required\n"
    + "\n"
    + "ghyHhHUujhJhjH77n8HHGTrfvbnj756tbB9HG4VQpfyF467GhIGfHfYT6\n"
    + "4VQpfyF467GhIGfHfYT6jH77n8HHGghyHhHUujhJh756tbB9HGTrfvbnj\n"
    + "n8HHGTrfvhJhjH776tbB9HG4VQbnj7567GhIGfHfYT6ghyHhHUujpfyF4\n"
    + "7GhIGfHfYT64VQbnj756\n"
    + "\n"
    + "--boundary1--\n";

  
  private static String messageString = "INVITE sip:user2@server2.com SIP/2.0\r\n"
                                        + "Via: SIP/2.0/UDP pc33.server1.com;branch=z9hG4bK776asdhds\r\n" + "Max-Forwards: 70\r\n"
                                        + "To: user2 <sip:user2@server2.com>\r\n"
                                        + "From: user1 <sip:user1@server1.com>;tag=1928301774\r\n"
                                        + "Call-ID: a84b4c76e66710@pc33.server1.com\r\n" + "CSeq: 314159 INVITE\r\n"
                                        + "Contact: <sip:user1@pc33.server1.com>\r\n" + "Content-Length: 0\r\n\r\n";

  String type[] = { "application", "applicaiton" };
  String subtype[] = { "sdp", "p25issi" };
  String content[] = {
                      "v=0\r\n" + "o=ali 1122334455 1122334466 IN IP4 rams.example.com\r\n" + "s=Rapid Acquisition Examples\r\n"
                        + "t=0 0\r\n" + "a=group:FID 1 2\r\n" + "a=rtcp-unicast:rsi\r\n" + "m=video 40000 RTP/AVPF 96\r\n"
                        + "c=IN IP4 224.1.1.1/255\r\n" + "a=source-filter: incl IN IP4 224.1.1.1 192.0.2.2\r\n" + "a=recvonly\r\n"
                        + "a=rtpmap:96 MP2T/90000\r\n" + "m=video 40002 RTP/AVPF 97\r\n"
                        + "i=Unicast Retransmission Stream #1 (Ret. Support Only)\r\n" + "c=IN IP4 192.0.2.1\r\n" + "a=recvonly\r\n"
                        + "a=rtpmap:97 rtx/90000\r\n" + "a=rtcp:40003\r\n" + "a=fmtp:97 apt=96\r\n",

                      "g-access:1\r\n" + "g-agroup:0B4561A27271\r\n" + "g-pri:2\r\n" + "g-ecap:1\r\n" + "g-eprempt:0\r\n"
                        + "g-rfhangt:0\r\n" + "g-ccsetupT:0\r\n" + "g-intmode:0\r\n" + "g-sec:1\r\n" + "g-ic:0\r\n" + "g-icsecstart:0\r\n" };

  private static MessageFactoryExt messageFactory = new MessageFactoryImpl();
  private static HeaderFactoryExt headerFactory = new HeaderFactoryImpl();

  @Override
  public void setUp() throws Exception {

  }

  public void testCreateMultipartMimeContent() throws Exception {
    MessageExt sipMessage = (MessageExt) messageFactory.createRequest(messageString);
    ContentTypeHeader cth = headerFactory.createContentTypeHeader("multipart", "alternative");
    cth.setParameter("boundary", "myboundary");
    MultipartMimeContent mmc = messageFactory.createMultipartMimeContent(cth, type, subtype, content);
    Iterator<Content> contentIterator = mmc.getContents();

    int i = 0;
    while (contentIterator.hasNext()) {
      Content contentImpl = contentIterator.next();
      assertEquals("Content must match", content[i], contentImpl.getContent());
      assertEquals("Content Type match", type[i], contentImpl.getContentTypeHeader().getContentType());
      assertEquals("Content subtype match", subtype[i], contentImpl.getContentTypeHeader().getContentSubType());
      i++;
    }

    String mmcContentString = mmc.toString();
    sipMessage.setContent(mmcContentString, cth);
    MultipartMimeContent mmc1 = sipMessage.getMultipartMimeContent();
    contentIterator = mmc1.getContents();

    i = 0;
    while (contentIterator.hasNext()) {
      Content contentImpl = contentIterator.next();
      assertEquals("Content must match", content[i], contentImpl.getContent().toString());
      assertEquals("Content Type match", type[i], contentImpl.getContentTypeHeader().getContentType());
      assertEquals("Content subtype match", subtype[i], contentImpl.getContentTypeHeader().getContentSubType());
      i++;
    }

    MessageExt message1 = (MessageExt) messageFactory.createRequest(messageSting1);
    ContentTypeHeader cth1 = (ContentTypeHeader) ((HeaderFactoryExt) headerFactory).createHeader(contentType1);
    message1.setContent(contentString1, cth1);

    System.out.println("SIP Message after adding content " + message1);

    MultipartMimeContent mmc2 = message1.getMultipartMimeContent();

    Iterator<Content> contents = mmc2.getContents();
    while (contents.hasNext()) {
      Content content = contents.next();
      System.out.println("contentTypeHeader = " + content.getContentTypeHeader());
      System.out.println("content = " + content.getContent());
    }

    Request request2 = messageFactory.createRequest(message1.toString());

    MultipartMimeContent mmc3 = ((MessageExt) request2).getMultipartMimeContent();

    Iterator<Content> contents2 = mmc3.getContents();

    contents = mmc2.getContents();
    assertTrue("number of fragments dont match", mmc2.getContentCount() == mmc3.getContentCount());
    while (contents2.hasNext()) {
      Content c1 = contents.next();
      Content c2 = contents2.next();
      assertTrue("contents must match", c1.getContent().equals(c2.getContent()));

    }

    System.out.println("After " + request2);

  }

  public void testMultiPartMimeMarshallingAndUnMarshallingWithExtraHeaders() throws Exception {
    SIPRequest request = new SIPRequest();
    byte[] content = contentString2.getBytes("UTF-8");
    ContentType contentType = new ContentType("multipart", "mixed");
    contentType.setParameter("boundary", "boundary1");
    
    request.setContent(content, contentType);
    MultipartMimeContent multipartMimeContent = request.getMultipartMimeContent();
    checkMultiPart(multipartMimeContent);

    // let's now marshall back the body and reparse it to check consistency
    String bodyContent = multipartMimeContent.toString();
    request.setContent(bodyContent, contentType);
    MultipartMimeContent multipartMimeContent2 = request.getMultipartMimeContent();
    checkMultiPart(multipartMimeContent2);
  }
  
  public void testMultiPartMimeMarshallingAndUnMarshallingWithExtraHeadersAndSpaces() throws Exception {
    SIPRequest request = new SIPRequest();
    byte[] content = multipartContentWithEmptyLine.getBytes("UTF-8");
    ContentType contentType = new ContentType("multipart", "mixed");
    contentType.setParameter("boundary", "boundary1");
    
    request.setContent(content, contentType);
    MultipartMimeContent multipartMimeContent = request.getMultipartMimeContent();
    checkMultiPartWithSpaces(multipartMimeContent);
    
    // let's now marshall back the body and reparse it to check consistency
    String bodyContent = multipartMimeContent.toString();
    request.setContent(bodyContent, contentType);
    MultipartMimeContent multipartMimeContent2 = request.getMultipartMimeContent();
    checkMultiPartWithSpaces(multipartMimeContent2);
  }
  
  /**
   * specific test for MultiPart Usnistgov Issue 16
   */
  public void testMultiPartMimeMarshallingAndUnMarshallingSpecificBodySize() throws Exception {
    SIPRequest request = new SIPRequest();
    byte[] content = specificBodySize.getBytes("UTF-8");
    ContentType contentType = new ContentType("multipart", "mixed");
    contentType.setParameter("boundary", "unique-boundary-1");
    
    request.setContent(content, contentType);
    MultipartMimeContent multipartMimeContent = request.getMultipartMimeContent();
    checkMultiPartRecordingSession(multipartMimeContent);
    
    // let's now marshall back the body and reparse it to check consistency
    String bodyContent = multipartMimeContent.toString();
    request.setContent(bodyContent, contentType);
    MultipartMimeContent multipartMimeContent2 = request.getMultipartMimeContent();
    checkMultiPartRecordingSession(multipartMimeContent2);
    
  }
  
  public void testMultiPartMimeMarshallingAndUnMarshallingWithANonMultiPartBodyWithAnEmptyLine() throws Exception {
    SIPRequest request = new SIPRequest();
    byte[] content = simpleContentWithEmptyLine.getBytes("UTF-8");
    ContentType contentType = new ContentType("application", "sdp");
    request.setContent(content, contentType);
    MultipartMimeContent multipartMimeContent = request.getMultipartMimeContent();
    checkSimpleBody(multipartMimeContent);
    
    // let's now marshall back the body and reparse it to check consistency
    // we just want the content, not the boundaries (which are null)
    String bodyContent = multipartMimeContent.getContents().next().getContent().toString();
    request.setContent(bodyContent, contentType);
    MultipartMimeContent multipartMimeContent2 = request.getMultipartMimeContent();
    checkSimpleBody(multipartMimeContent2);
  }
  
  public void testMultiPartMimeMarshallingAndUnMarshallingWithANonMultiPartBodyWithAnEmptyLineWithNoLeadingLine() throws Exception {
    SIPRequest request = new SIPRequest();
    byte[] content = isup.getBytes("UTF-8");
    ContentType contentType = new ContentType("application", "sdp");
    request.setContent(content, contentType);
    MultipartMimeContent multipartMimeContent = request.getMultipartMimeContent();
    checkSimpleBody(multipartMimeContent);
    
    // let's now marshall back the body and reparse it to check consistency
    // we just want the content, not the boundaries (which are null)
    String bodyContent = multipartMimeContent.getContents().next().getContent().toString();
    request.setContent(bodyContent, contentType);
    MultipartMimeContent multipartMimeContent2 = request.getMultipartMimeContent();
    checkSimpleBody(multipartMimeContent2);
  }

  private void checkMultiPart(MultipartMimeContent multipartMimeContent) {
    Iterator<Content> partContentIterator = multipartMimeContent.getContents();
    Content sdpPart = partContentIterator.next();
    Content pidfPart = partContentIterator.next();

    assertEquals("application/sdp", ((ContentType) sdpPart.getContentTypeHeader()).getValue());
    assertFalse(sdpPart.getExtensionHeaders().hasNext());

    assertEquals("application/pidf+xml", ((ContentType) pidfPart.getContentTypeHeader()).getValue());
    assertTrue(pidfPart.getExtensionHeaders().hasNext());
    assertNotNull(pidfPart.getContent());
    SIPHeader extensionHeader = (SIPHeader) pidfPart.getExtensionHeaders().next();
    assertEquals("Content-ID", extensionHeader.getName());
    assertEquals("alice123@atlanta.example.com", extensionHeader.getValue());
    assertNotNull(sdpPart.getContent());
  }
  
  private void checkMultiPartRecordingSession(MultipartMimeContent multipartMimeContent) {
    Iterator<Content> partContentIterator = multipartMimeContent.getContents();
    Content sdpPart = partContentIterator.next();
    Content secondPart = partContentIterator.next();

    assertEquals("application/sdp", ((ContentType) sdpPart.getContentTypeHeader()).getValue());
    assertFalse(sdpPart.getExtensionHeaders().hasNext());
    assertNotNull(sdpPart.getContent());

    assertEquals("application/rs-metadata+xml", ((ContentType) secondPart.getContentTypeHeader()).getValue());
    assertFalse(secondPart.getExtensionHeaders().hasNext());
    assertNotNull(secondPart.getContent());
    ContentDispositionHeader contentDispositionHeader = secondPart.getContentDispositionHeader();
    assertEquals("recording-session", contentDispositionHeader.getDispositionType());
  }
  
  private void checkMultiPartWithSpaces(MultipartMimeContent multipartMimeContent) {
    Iterator<Content> partContentIterator = multipartMimeContent.getContents();
    Content part1 = partContentIterator.next();
    Content part2 = partContentIterator.next();

    assertEquals("message/sip", ((ContentType) part1.getContentTypeHeader()).getValue());
    assertFalse(part1.getExtensionHeaders().hasNext());
    assertNotNull(part1.getContent());

    assertEquals("application/pkcs7-signature;name=smime.p7s", ((ContentType) part2.getContentTypeHeader()).getValue());
    assertTrue(part2.getExtensionHeaders().hasNext());
    assertEquals("ghyHhHUujhJhjH77n8HHGTrfvbnj756tbB9HG4VQpfyF467GhIGfHfYT6\n"
                 + "4VQpfyF467GhIGfHfYT6jH77n8HHGghyHhHUujhJh756tbB9HGTrfvbnj\n"
                 + "n8HHGTrfvhJhjH776tbB9HG4VQbnj7567GhIGfHfYT6ghyHhHUujpfyF4\n"
                 + "7GhIGfHfYT64VQbnj756\n", part2.getContent());
    
  }
  

  private void checkSimpleBody(MultipartMimeContent multipartMimeContent) {
    Iterator<Content> partContentIterator = multipartMimeContent.getContents();
    Content sdpPart = partContentIterator.next();
    
    assertEquals("application/sdp", ((ContentType) sdpPart.getContentTypeHeader()).getValue());
    assertFalse(sdpPart.getExtensionHeaders().hasNext());
    
    assertNotNull(sdpPart.getContent());
    assertFalse(partContentIterator.hasNext());
  }
  
  private byte[] toByteArray(InputStream input) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
    }
    return output.toByteArray();
  }

}
