package test.unit.gov.nist.javax.sip.stack;



import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.net.SocketFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.sip.ClientTransaction;
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
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
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
public class NIOIdleTimeoutTest extends ScenarioHarness {

	
    public NIOIdleTimeoutTest() {
    	super("NIOIdleTimeoutTest",true);
	}

    private static final int TEST_SOCKETS = 20; 
    private static final int OPEN_DELAY = 1000; 
    private static final int CLOSE_DELAY = 40000; 

	public final int SERVER_PORT = 5600;
	public final int SERVER_PORT2 = 5601;

    public final int CLIENT_PORT = 6500;
    
    protected String testProtocol = "tls";
    protected String testProtocol2 = "tcp";

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
	
	public void testSocketTimeout() throws Exception {
		Server server = new Server();
		Client client = new Client();
		
		client.sendInvite();
		Thread.sleep(1000);
		
		Request serverLastRequestReceived = server.getLastRequestReceived();
		assertNotNull(serverLastRequestReceived);
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
                defaultProperties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
                defaultProperties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS",
                        "false");
                defaultProperties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
                defaultProperties.setProperty("gov.nist.javax.sip.NIO_MAX_SOCKET_IDLE_TIME", "" + CLOSE_DELAY / 4);
                defaultProperties.setProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE", "DisabledAll");
                defaultProperties.setProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS", "SSLv2Hello, TLSv1");
                this.sipFactory = SipFactory.getInstance();
                this.sipFactory.setPathName("gov.nist");
                this.sipStack = this.sipFactory.createSipStack(defaultProperties);
                this.sipStack.start();
                ListeningPoint lp = this.sipStack.createListeningPoint(host, SERVER_PORT, testProtocol);
                ListeningPoint lp2 = this.sipStack.createListeningPoint(host, SERVER_PORT2, testProtocol2);
                this.provider = this.sipStack.createSipProvider(lp);
                this.provider2 = this.sipStack.createSipProvider(lp2);
                this.provider.addSipListener(this);
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
        
        public void sendInvite() throws Exception {
            Socket[] test = new Socket[TEST_SOCKETS]; 
			try {
				for (int i = 0; i < TEST_SOCKETS; i++) {
					if (i % 2 == 0) {
						SocketFactory factory = SSLSocketFactory.getDefault();
						test[i] = factory.createSocket(host, SERVER_PORT);
					} else {
						test[i] = new Socket(host, SERVER_PORT2);
					}
					System.out.println("Socket " + i);
					Thread.sleep(OPEN_DELAY);
				}
				Thread.sleep(CLOSE_DELAY);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				System.out.println("Checking sockets are closed");
				for (int i = 0; i < TEST_SOCKETS; i++) {
					try {
		            	System.out.println(test[i].getInputStream().read());
		            } catch (SocketTimeoutException e) {
						throw new Exception("Socket " + test[i] + " wasn't closed by SocketAuditor", e);
					} catch (SSLHandshakeException e) {
						System.out.println("TLS Socket closed correctly ");
					}
				}
			}
            
            
        	Address fromAddress = addressFactory.createAddress("here@somewhere:5070");
        	ContactHeader contactHeader1 = headerFactory.createContactHeader(addressFactory.createAddress("sip:here@somewhere:5070"));
    		ContactHeader contactHeader2 = headerFactory.createContactHeader(addressFactory.createAddress("sip:here@somewhereelse:5080"));
    		
			CallIdHeader callId = provider.getNewCallId();			
			CSeqHeader cSeq = headerFactory.createCSeqHeader(1l, Request.INVITE);
			FromHeader from = headerFactory.createFromHeader(fromAddress, "1234");
			ToHeader to = headerFactory.createToHeader(addressFactory.createAddress("server@"+host+":"+SERVER_PORT), null);
			ViaHeader via = ((ListeningPointImpl)provider.getListeningPoint(testProtocol)).getViaHeader();
			List<ViaHeader> vias = Arrays.asList(via);			
			MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(10);
    		
    		URI requestURI = addressFactory.createURI("sip:test@"+host+":"+SERVER_PORT);
    		Request request = messageFactory.createRequest(requestURI, Request.INVITE, callId, cSeq, from, to, vias, maxForwards);
    		System.out.println(request);
    		assertTrue(request.toString().indexOf("rport=") == -1);    		
    		
    		request.setRequestURI(requestURI);
    		request.addHeader(contactHeader1);
    		request.addHeader(contactHeader2);
    		ClientTransaction ctx = this.provider.getNewClientTransaction(request);
    		ctx.sendRequest();
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
