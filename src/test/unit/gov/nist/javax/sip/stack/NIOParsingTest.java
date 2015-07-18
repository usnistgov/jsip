package test.unit.gov.nist.javax.sip.stack;



import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.net.Socket;
import java.util.Properties;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import junit.framework.Assert;
import test.tck.msgflow.callflows.ScenarioHarness;
import test.unit.gov.nist.javax.sip.stack.tls.TlsTest;
/**
 * Test Issue 309 Via.setRPort() creates malformed rport parameter
 * @author jean.deruelle@gmail.com
 *
 */
public class NIOParsingTest extends ScenarioHarness {

	
    public NIOParsingTest() {
    	super("NIOParsingTest",true);
	}
    private static final int OPEN_DELAY = 5000;
    private static final int CLOSE_DELAY = 40000; 

	public final int SERVER_PORT = 5601;

    public final int CLIENT_PORT = 6500;
    
    protected String testProtocol = "tcp";

	public HeaderFactory headerFactory;

	public MessageFactory messageFactory;

	public AddressFactory addressFactory;

	public String host;
    	
	public void setUp() throws Exception {
		System.setProperty( "javax.net.ssl.keyStore",  TlsTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.trustStore", TlsTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
	}
	
	public void testParse() throws Exception {
		Server server = new Server();
		Client client = new Client();
		
		client.sendGarbage();
		Thread.sleep(1000);
	}
	
	
	public class Server extends SipAdapter {
        protected SipStack sipStack;

        protected SipFactory sipFactory = null;

        protected SipProvider provider = null;
        protected SipProvider provider2 = null;

        private Request lastRequestReceived;
        
        public Server() {
            try {
                final Properties defaultProperties = new Properties();
                host = "127.0.0.1";

                defaultProperties.setProperty("javax.sip.STACK_NAME", "server");
                defaultProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
                defaultProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "server_debug_ViaRPortTest.txt");
                defaultProperties.setProperty("gov.nist.javax.sip.SERVER_LOG", "server_log_ViaRPortTest.txt");
                defaultProperties.setProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE", "64");
                defaultProperties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
                this.sipFactory = SipFactory.getInstance();
                this.sipFactory.setPathName("gov.nist");
                this.sipStack = this.sipFactory.createSipStack(defaultProperties);
                this.sipStack.start();
                ListeningPoint lp2 = this.sipStack.createListeningPoint(host, SERVER_PORT, testProtocol);
                this.provider2 = this.sipStack.createSipProvider(lp2);
                this.provider2.addSipListener(this);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("unexpected exception ");
            }

        }

        public void stop() {
            this.sipStack.stop();
        }

        public void processRequest(RequestEvent requestEvent) {
           lastRequestReceived = requestEvent.getRequest();
        }

		public Request getLastRequestReceived() {
			return lastRequestReceived;
		}                
    }

    public class Client extends SipAdapter {

        private SipFactory sipFactory;
        private SipStack sipStack;
        private SipProvider provider;

        public Client() {
            try {
                final Properties defaultProperties = new Properties();
                String host = "127.0.0.1";
                defaultProperties.setProperty("javax.sip.STACK_NAME", "client");
                defaultProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
                defaultProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "client_debug.txt");
                defaultProperties.setProperty("gov.nist.javax.sip.SERVER_LOG", "client_log.txt");
                defaultProperties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
                defaultProperties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS","false");
                defaultProperties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
                defaultProperties.setProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE", "DisabledAll");
                defaultProperties.setProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS", "SSLv2Hello, TLSv1");
                
                this.sipFactory = SipFactory.getInstance();
                this.sipFactory.setPathName("gov.nist");
                this.sipStack = this.sipFactory.createSipStack(defaultProperties);
                this.sipStack.start();
                ListeningPoint lp = this.sipStack.createListeningPoint(host, CLIENT_PORT, testProtocol);
                this.provider = this.sipStack.createSipProvider(lp);
                headerFactory = this.sipFactory.createHeaderFactory();
                messageFactory = this.sipFactory.createMessageFactory();
                addressFactory = this.sipFactory.createAddressFactory();
                this.provider.addSipListener(this);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("unexpected exception ");
            }
        }
        
        String testmessage= "INVITE sip:bob@biloxi.com SIP/2.0\r\n" + 
        					"Via: SIP/2.0/UDP pc33.atlanta.com;branch=z9hG4bK776asdhds\r\n" +
        					"Max-Forwards: 70\r\n" +
        					"To: Bob <sip:bob@biloxi.com>\r\n" +
        					"From: Alice <sip:alice@atlanta.com>;tag=1928301774\r\n" +
        					"Call-ID: a84b4c76e66710@pc33.atlanta.com\r\n" +
        					"CSeq: 314159 INVITE\r\n" +
        					"Contact: <sip:alice@pc33.atlanta.com>\r\n" +
        					"Content-Type: application/sdp\r\n" +
        					"Content-Length: 0\r\n\r\n";
        
        String testmessage2= "000\r\n\r\n";
        
        public void sendGarbage() throws Exception {
            Socket test = new Socket(host, SERVER_PORT);
			test.getOutputStream().write(testmessage.getBytes());
			test.getOutputStream().flush();
			test.getOutputStream().write(testmessage2.getBytes());
			test.getOutputStream().flush();
//			test.close();
				
			Thread.sleep(OPEN_DELAY);          
            
        	
        }
    }


	private static class SipAdapter implements SipListener {

		public void processDialogTerminated(DialogTerminatedEvent arg0) {}

		public void processIOException(IOExceptionEvent arg0) {}

		public void processRequest(RequestEvent arg0) {}

		public void processResponse(ResponseEvent arg0) {}

		public void processTimeout(TimeoutEvent arg0) {}
		
		public void processTransactionTerminated(TransactionTerminatedEvent arg0) {}
	}
}
