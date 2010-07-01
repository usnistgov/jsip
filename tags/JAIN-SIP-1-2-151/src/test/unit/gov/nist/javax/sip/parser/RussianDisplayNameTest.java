package test.unit.gov.nist.javax.sip.parser;

import java.nio.charset.Charset;

import javax.sip.header.FromHeader;

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.StringMsgParser;
import junit.framework.TestCase;



public class RussianDisplayNameTest extends TestCase {
	private static final String messageToParse = 
	"INVITE sip:3853@sip.svgc.ru;user=phone SIP/2.0\r\n" +
	"Via: SIP/2.0/UDP 10.0.2.250:5060;branch=z9hG4bK-ztk7799cnhxx;rport\r\n" +
	"From: \"теле Cном_3606\" <sip:3606@sip.svgc.ru>;tag=lqffmqdzmv\r\n" +
	"To: <sip:3853@sip.svgc.ru;user=phone>\r\n"+
	"Call-ID: 3c2688891598-8lqnbdxvv6ip\r\n"+
	"CSeq: 1 INVITE\r\n"+
	"Max-Forwards: 70\r\n"+
	"Contact: <sip:3606@10.0.2.250:5060;line=qbc05th0>;reg-id=1\r\n"+
	"P-Key-Flags: resolution=\"31x13\", keys=\"4\"\r\n"+
	"User-Agent: snom360/7.3.30\r\n"+
	"Accept: application/sdp\r\n"+
	"Allow: INVITE, ACK, CANCEL, BYE, REFER, OPTIONS, NOTIFY, SUBSCRIBE, PRACK, MESSAGE, INFO\r\n"+
	"Allow-Events: talk, hold, refer, call-info\r\n"+
	"Supported: timer, 100rel, replaces, from-change\r\n"+
	"Session-Expires: 3600;refresher=uas\r\n"+
	"Min-SE: 90\r\n"+
	"Content-Length: 0\r\n\r\n";
	
	private static final String binaryMessage = "494e56495445207369703a33383533407369702e737667632e" +
			"72753b757365723d70686f6e65205349502f322e300d0a5669613a205349502f322e302f554450" +
			"2031302e302e322e3235303a353036303b6272616e63683d7a39684734624b2d7a746b37373939636e" +
			"6878783b72706f72740d0a46726f6d3a2022d182d0b5d0bbd0b52043d0bdd0bed0bc5f3336303622203c" +
			"7369703a33363036407369702e737667632e72753e3b7461673d6c7166666d71647a6d760d0a546f3a203c" +
			"7369703a33383533407369702e737667632e72753b757365723d70686f6e653e0d0a43616c6c2d49443a" +
			"203363323638383839313539382d386c716e62647876763669700d0a435365713a203120494e5649544" +
			"50d0a4d61782d466f7277617264733a2037300d0a436f6e746163743a203c7369703a333630364031302" +
			"e302e322e3235303a353036303b6c696e653d71626330357468303e3b7265672d69643d310d0a502d4b65" +
			"792d466c6167733a207265736f6c7574696f6e3d223331783133222c206b6579733d2234220d0a557365722d4167" +
			"656e743a20736e6f6d3336302f372e332e33300d0a4163636570743a206170706c69636174696f6e2f736470" +
			"0d0a416c6c6f773a20494e564954452c2041434b2c2043414e43454c2c204259452c2052454645522c204f50" +
			"54494f4e532c204e4f544946592c205355425343524942452c20505241434b2c204d4553534147452c20494e4" +
			"64f0d0a416c6c6f772d4576656e74733a2074616c6b2c20686f6c642c2072656665722c2063616c6c2d696e66" +
			"6f0d0a537570706f727465643a2074696d65722c2031303072656c2c207265706c616365732c2066726f6d2d6" +
			"368616e67650d0a53657373696f6e2d457870697265733a20333630303b7265667265736865723d7561730d0a4" +
			"d696e2d53453a2039300d0a436f6e74656e742d547970653a206170706c69636174696f6e2f7364700d0a436f6" +
			"e74656e742d4c656e6774683a203336340d0a0d0a763d300d0a6f3d726f6f74203135333135373034323920313" +
			"5333135373034323920494e204950342031302e302e322e3235300d0a733d63616c6c0d0a633d494e204950342031" +
			"302e302e322e3235300d0a743d3020300d0a6d3d617564696f203534373632205254502f41565020302038203920322" +
			"0332031382034203130310d0a613d7274706d61703a302070636d752f383030300d0a613d7274706d61703a38207063" +
			"6d612f383030300d0a613d7274706d61703a3920673732322f383030300d0a613d7274706d61703a3220673732362d33" +
			"322f383030300d0a613d7274706d61703a332067736d2f383030300d0a613d7274706d61703a313820673732392f38303" +
			"0300d0a613d7274706d61703a3420673732332f383030300d0a613d7274706d61703a3130312074656c6570686f6e652d" +
			"6576656e742f383030300d0a613d666d74703a31303120302d31360d0a613d7074696d653a32300d0a613d73656e64726563760d0a";

	
	public void testParseMessage () {
		try {
	
		
			StringMsgParser smp = new StringMsgParser();
	
			SIPMessage message = smp.parseSIPMessage(messageToParse.getBytes("UTF-8"), true, false, null);
			
			System.out.println("Message = " + message);
			
			byte[] bytes = message.encodeAsBytes("UDP");
			
		    smp = new StringMsgParser();
			
		    message = smp.parseSIPMessage(bytes, true, false, null);
		    System.out.println("Message = " + message);
		    char[] mybytes = new char[2];
		    byte[] sipMessageBytes = new byte[binaryMessage.length()];
		    
		    for ( int i = 0; i < binaryMessage.length(); i += 2) {
		    	mybytes[0] = binaryMessage.charAt(i);
		    	mybytes[1] = binaryMessage.charAt(i+1);
		    	String byteString = new String(mybytes);
		    	int val = Integer.parseInt(byteString,16);
		    	sipMessageBytes[i/2] = (byte)val;
		    	
		    }
		    smp = new StringMsgParser();
		    SIPMessage reparsed = smp.parseSIPMessage(sipMessageBytes, true, false, null);
		    
		    System.out.println("Reparsed = " + reparsed);
		    
		    bytes = message.encodeAsBytes("UDP");
		    smp = new StringMsgParser();
		    reparsed = smp.parseSIPMessage(bytes, true, false, null);
		    
		    System.out.println("Reparsed = " + reparsed);
	
			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("unexpected exception");
		}
	}
	

}
