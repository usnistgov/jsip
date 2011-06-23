package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.message.MessageFactoryImpl;

import java.text.ParseException;

import javax.sip.message.Request;

import junit.framework.TestCase;

public class JunkAtEndOfMessageTest extends TestCase {

    public void testMessageSyntax() {
        MessageFactoryImpl messageFactory = new MessageFactoryImpl();
        try {
            Request request = messageFactory
                    .createRequest("BYE sip:127.0.0.1:5080;transport=tcp SIP/2.0\r\n"
                            + "Via: SIP/2.0/TCP 127.0.0.1:5060;rport=5060;branch=z9hG4bKd2c87858eb0a7a09becc7a115c608d27\r\n"
                            + "CSeq: 2 BYE\r\n"
                            + "Call-ID: 84a5c57fd263bcce6fec05edf20c5aba@127.0.0.1\r\n"
                            + "From: \"The Master Blaster\" <sip:BigGuy@here.com>;tag=12345\r\n"
                            + "To: \"The Little Blister\" <sip:LittleGuy@there.com>;tag=2955\r\n"
                            + "Max-Forwards: 70\r\n"
                            + "Route: \"proxy\" <sip:proxy@127.0.0.1:5070;transport=tcp;lr>\r\n"
                            + "Content-Length: 0\r\n" + " \r\n" // the space here is invalid
                    );
            fail("Should throw an exception");
        } catch (junit.framework.AssertionFailedError afe) {
            fail("Should throw exception ");
        } catch (ParseException ex) {
            System.out.println("Got expected error");
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Should throw a ParseException");
        } finally {
            System.out.println("testMessageSyntax()");
        }
    }

    public void testMessageSyntax2() {
        MessageFactoryImpl messageFactory = new MessageFactoryImpl();

        try {
            Request request = messageFactory
                    .createRequest("BYE sip:127.0.0.1:5080;transport=tcp SIP/2.0\r\n"
                            + "Via: SIP/2.0/TCP 127.0.0.1:5060;rport=5060;branch=z9hG4bKd2c87858eb0a7a09becc7a115c608d27\r\n"
                            + "CSeq: 2 BYE\r\n"
                            + "Call-ID: 84a5c57fd263bcce6fec05edf20c5aba@127.0.0.1\r\n"
                            + "From: \"The Master Blaster\" <sip:BigGuy@here.com>;tag=12345\r\n"
                            + "To: \"The Little Blister\" <sip:LittleGuy@there.com>;tag=2955\r\n"
                            + "Max-Forwards: 70\r\n"
                            + "Route: \"proxy\" <sip:proxy@127.0.0.1:5070;transport=tcp;lr>\r\n"
                            + "Content-Length: 0\r\n" + "\r \n" // the space here is invalid
                    );
            fail("Should throw an exception");
        } catch (junit.framework.AssertionFailedError afe) {
            fail("Should throw exception");
        } catch (ParseException ex) {
            System.out.println("Got expected error");
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Should throw a ParseException");
        } finally {
            System.out.println("testMessageSyntax2()");
        }
    }

}
