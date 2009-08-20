package test.unit.gov.nist.javax.sip.multipartmime;

import java.util.Iterator;

import javax.sip.header.ContentTypeHeader;

import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.header.HeaderFactoryImpl;
import gov.nist.javax.sip.message.Content;
import gov.nist.javax.sip.message.MessageExt;
import gov.nist.javax.sip.message.MessageFactoryExt;
import gov.nist.javax.sip.message.MessageFactoryImpl;
import gov.nist.javax.sip.message.MultipartMimeContent;
import junit.framework.TestCase;

public class MultipartMimeParserTest extends TestCase {
    private static String messageString = 
        "INVITE sip:user2@server2.com SIP/2.0\r\n" +
        "Via: SIP/2.0/UDP pc33.server1.com;branch=z9hG4bK776asdhds\r\n" +
        "Max-Forwards: 70\r\n" +
        "To: user2 <sip:user2@server2.com>\r\n" +
        "From: user1 <sip:user1@server1.com>;tag=1928301774\r\n" +
        "Call-ID: a84b4c76e66710@pc33.server1.com\r\n" +
        "CSeq: 314159 INVITE\r\n" +
        "Contact: <sip:user1@pc33.server1.com>\r\n";
    
    String type[] = {"application","applicaiton"};
    String subtype[] = {"sdp","p25issi"};
    String content[] = {
        "v=0\r\n" +
        "o=ali 1122334455 1122334466 IN IP4 rams.example.com\r\n" +
        "s=Rapid Acquisition Examples\r\n" +
        "t=0 0\r\n" +
        "a=group:FID 1 2\r\n" +
        "a=rtcp-unicast:rsi\r\n" +
        "m=video 40000 RTP/AVPF 96\r\n" +
        "c=IN IP4 224.1.1.1/255\r\n" +
        "a=source-filter: incl IN IP4 224.1.1.1 192.0.2.2\r\n" +
        "a=recvonly\r\n" +
        "a=rtpmap:96 MP2T/90000\r\n" +
        "m=video 40002 RTP/AVPF 97\r\n" +
        "i=Unicast Retransmission Stream #1 (Ret. Support Only)\r\n" +
        "c=IN IP4 192.0.2.1\r\n" +
        "a=recvonly\r\n" +
        "a=rtpmap:97 rtx/90000\r\n" +
        "a=rtcp:40003\r\n" +
        "a=fmtp:97 apt=96\r\n",
        
          "g-access:1\r\n"
        + "g-agroup:0B4561A27271\r\n" + "g-pri:2\r\n" + "g-ecap:1\r\n"
        + "g-eprempt:0\r\n" + "g-rfhangt:0\r\n" + "g-ccsetupT:0\r\n"
        + "g-intmode:0\r\n" + "g-sec:1\r\n" + "g-ic:0\r\n"
        + "g-icsecstart:0\r\n"};


    private MessageExt sipMessage;
    private ContentTypeHeader cth;
    
    private static MessageFactoryExt messageFactory  = new MessageFactoryImpl();
    private static HeaderFactoryExt headerFactory = new HeaderFactoryImpl();
    
    @Override 
    public void setUp() throws Exception {
        sipMessage = (MessageExt) new MessageFactoryImpl().createRequest(messageString);
        cth = headerFactory.createContentTypeHeader("multipart", "alternative");
        cth.setParameter("boundary","myboundary");
    }
    
    public void testMultipartMimeContent() throws Exception {
        MultipartMimeContent mmc = messageFactory.createMultipartMimeContent(cth, type, subtype, content);
        Iterator<Content> contentIterator = mmc.getContents();
        
        int i  = 0;
        while (contentIterator.hasNext() ) {
            Content contentImpl = contentIterator.next();
            assertEquals("Content must match",contentImpl.getContent(), content[i]);
            assertEquals("Content Type match", contentImpl.getContentTypeHeader().getContentType(),type[i]);
            assertEquals("Content subtype match",contentImpl.getContentTypeHeader().getContentSubType(),subtype[i]);
            i++;
        }
        
        String mmcContentString = mmc.toString();
        sipMessage.setContent(mmcContentString, cth);
        System.out.println("SIP Message After adding content " + sipMessage);
        MultipartMimeContent mmc1 = sipMessage.getMultipartMimeContent();
        contentIterator = mmc1.getContents();
        
        i  = 0;
        while (contentIterator.hasNext() ) {
            Content contentImpl = contentIterator.next();
            assertEquals("Content must match",contentImpl.getContent(), content[i]);
            assertEquals("Content Type match", contentImpl.getContentTypeHeader().getContentType(),type[i]);
            assertEquals("Content subtype match",contentImpl.getContentTypeHeader().getContentSubType(),subtype[i]);
            i++;
        }
    }

}
