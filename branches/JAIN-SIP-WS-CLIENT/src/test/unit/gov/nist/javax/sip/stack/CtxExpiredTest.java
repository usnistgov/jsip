package test.unit.gov.nist.javax.sip.stack;

import java.util.ArrayList;
import java.util.Properties;

import gov.nist.javax.sip.ServerTransactionExt;
import gov.nist.javax.sip.SipProviderExt;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.header.HeaderFactoryExt;
import gov.nist.javax.sip.message.MessageFactoryExt;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

import junit.framework.TestCase;

public class CtxExpiredTest extends TestCase {

    AddressFactory addressFactory;

    HeaderFactoryExt headerFactory;

    MessageFactoryExt messageFactory;

    private Shootist shootist;

    private Shootme shootme;

    private SipStackExt shootistStack;

    private SipStackExt shootmeStack;

    private static String PEER_ADDRESS = Shootme.myAddress;

    private static int PEER_PORT = Shootme.myPort;

    private static String peerHostPort = PEER_ADDRESS + ":" + PEER_PORT;

    private static Logger logger = Logger.getLogger(CtxExpiredTest.class);
    static {
        logger.addAppender(new ConsoleAppender());
    }

    class Shootist implements SipListener {

        protected static final String myAddress = "127.0.0.1";

        protected static final int myPort = 6050;

        private SipProviderExt provider;

        private boolean saw1xx;

        private ClientTransaction inviteTid;

        private Dialog dialog;

        private boolean timeoutSeen;

        public void checkState() {
            assertTrue("Should see timeout ", timeoutSeen);
            assertTrue("Should see 1xx ", saw1xx);
        }

        public Shootist(SipStackExt sipStack) throws Exception {
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    myPort, "udp");
            this.provider = (SipProviderExt) sipStack.createSipProvider(lp);
            provider.addSipListener(this);
        }

        public void sendInvite() {

            try {

                // Note that a provider has multiple listening points.
                // all the listening points must have the same IP address
                // and port but differ in their transport parameters.

                String fromName = "BigGuy";
                String fromSipAddress = "here.com";
                String fromDisplayName = "The Master Blaster";

                String toSipAddress = "there.com";
                String toUser = "LittleGuy";
                String toDisplayName = "The Little Blister";

                // create >From Header
                SipURI fromAddress = addressFactory.createSipURI(fromName,
                        fromSipAddress);

                Address fromNameAddress = addressFactory
                        .createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = headerFactory.createFromHeader(
                        fromNameAddress, new Integer(
                                (int) (Math.random() * Integer.MAX_VALUE))
                                .toString());

                // create To Header
                SipURI toAddress = addressFactory.createSipURI(toUser,
                        toSipAddress);
                Address toNameAddress = addressFactory.createAddress(toAddress);
                toNameAddress.setDisplayName(toDisplayName);
                ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                        null);

                // create Request URI
                SipURI requestURI = addressFactory.createSipURI(toUser,
                        peerHostPort);

                // Create ViaHeaders

                ArrayList viaHeaders = new ArrayList();
                int port = provider.getListeningPoint("udp").getPort();

                ViaHeader viaHeader = headerFactory.createViaHeader(myAddress,
                        port, "udp", null);

                // add via headers
                viaHeaders.add(viaHeader);

                // Create ContentTypeHeader
                ContentTypeHeader contentTypeHeader = headerFactory
                        .createContentTypeHeader("application", "sdp");

                // Create a new CallId header
                CallIdHeader callIdHeader = provider.getNewCallId();
                // JvB: Make sure that the implementation matches the
                // messagefactory
                callIdHeader = headerFactory.createCallIdHeader(callIdHeader
                        .getCallId());

                // Create a new Cseq header
                CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                        Request.INVITE);

                // Create a new MaxForwardsHeader
                MaxForwardsHeader maxForwards = headerFactory
                        .createMaxForwardsHeader(70);

                // Create the request.
                Request request = messageFactory.createRequest(requestURI,
                        Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                        toHeader, viaHeaders, maxForwards);
                // Create contact headers

                // Create the contact name address.
                SipURI contactURI = addressFactory.createSipURI(fromName,
                        myAddress);
                contactURI.setPort(provider.getListeningPoint("udp").getPort());

                Address contactAddress = addressFactory
                        .createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                ContactHeader contactHeader = headerFactory
                        .createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                ExpiresHeader expires = headerFactory.createExpiresHeader(20);

                request.setHeader(expires);

                String sdpData = "v=0\r\n"
                        + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n"
                        + "s=mysession session\r\n" + "p=+46 8 52018010\r\n"
                        + "c=IN IP4  129.6.55.78\r\n" + "t=0 0\r\n"
                        + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n"
                        + "a=rtpmap:4 G723/8000\r\n"
                        + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";

                request.setContent(sdpData, contentTypeHeader);

                // The following is the preferred method to route requests
                // to the peer. Create a route header and set the "lr"
                // parameter for the router header.

                Address address = addressFactory.createAddress("<sip:"
                        + PEER_ADDRESS + ":" + PEER_PORT + ">");
                // SipUri sipUri = (SipUri) address.getURI();
                // sipUri.setPort(PEER_PORT);

                RouteHeader routeHeader = headerFactory
                        .createRouteHeader(address);
                ((SipURI) address.getURI()).setLrParam();
                request.addHeader(routeHeader);

                // Create the client transaction.
                this.inviteTid = provider.getNewClientTransaction(request);
                this.dialog = this.inviteTid.getDialog();
                // Note that the response may have arrived right away so
                // we cannot check after the message is sent.
                TestCase
                        .assertTrue(this.dialog.getState() == null);

                // send the request out.
                this.inviteTid.sendRequest();

            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                TestCase.fail("unexpected exception");
            }
        }

        
        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {

        }

        
        public void processIOException(IOExceptionEvent exceptionEvent) {
            TestCase.fail("Unexpected event");
        }

        
        public void processRequest(RequestEvent requestEvent) {
            TestCase.fail("Unexpected event : processRequest");

        }

        
        public void processResponse(ResponseEvent responseEvent) {

            if (responseEvent.getResponse().getStatusCode() == 100) {
                this.saw1xx = true;
            }
        }

        
        public void processTimeout(TimeoutEvent timeoutEvent) {
            this.timeoutSeen = true;
            ClientTransaction ctx = timeoutEvent.getClientTransaction();
            TestCase.assertSame("Should see a timeout for INVITE", ctx,
                    this.inviteTid);
            TestCase.assertEquals("Should be in proceeding state",
                    TransactionState.PROCEEDING, ctx.getState());
            try {
                Request cancelRequest = timeoutEvent.getClientTransaction()
                        .createCancel();
                ClientTransaction cancelTx = this.provider
                        .getNewClientTransaction(cancelRequest);
                cancelTx.sendRequest();
            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                TestCase.fail("Unexpected exception");
            }

        }

        
        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            logger.debug("Transaction Terminated Event seen");

        }

    }

    public class Shootme implements SipListener {

        public static final int myPort = 6060;
        public static final String myAddress = "127.0.0.1";
        private SipProviderExt provider;

        public Shootme(SipStackExt sipStack) throws Exception {
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    myPort, "udp");
            this.provider = (SipProviderExt) sipStack.createSipProvider(lp);
            provider.addSipListener(this);
        }

        
        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            // TODO Auto-generated method stub

        }

        
        public void processIOException(IOExceptionEvent exceptionEvent) {
            // TODO Auto-generated method stub

        }

        
        public void processRequest(RequestEvent requestEvent) {
            try {
                Request request = requestEvent.getRequest();

                if (request.getMethod().equals(Request.INVITE)) {
                    if (requestEvent.getServerTransaction() == null) {

                        ServerTransactionExt serverTransaction = (ServerTransactionExt) this.provider
                                .getNewServerTransaction(request);

                        Response tryingResponse = messageFactory
                                .createResponse(100, request);
                        serverTransaction.sendResponse(tryingResponse);

                    }
                } else if (request.getMethod().equals(Request.CANCEL)) {
                    ServerTransaction stx = requestEvent.getServerTransaction();
                    Response okResponse = messageFactory.createResponse(200,
                            request);
                    stx.sendResponse(okResponse);

                }
            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                TestCase.fail("Unexpected exception");
            }
        }

        
        public void processResponse(ResponseEvent responseEvent) {
            // TODO Auto-generated method stub

        }

        
        public void processTimeout(TimeoutEvent timeoutEvent) {
            // TODO Auto-generated method stub

        }

        
        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            // TODO Auto-generated method stub

        }

    }

    
    public void setUp() throws Exception {
        SipFactory sipFactory = null;

        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties;

        try {
            headerFactory = (HeaderFactoryExt) sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = (MessageFactoryExt) sipFactory
                    .createMessageFactory();

        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception");
        }
        try {
            // Create SipStack object
            properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", "shootist");
            // You need 16 for logging traces. 32 for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "shootistdebug.txt");
            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	logger.info("\nNIO Enabled\n");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
          
            this.shootistStack = (SipStackExt) sipFactory
                    .createSipStack(properties);
            this.shootist = new Shootist(shootistStack);

            // -----------------------------
            properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", "shootme");
            // You need 16 for logging traces. 32 for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "shootmedebug.txt");
            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	logger.info("\nNIO Enabled\n");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            this.shootmeStack = (SipStackExt) sipFactory
                    .createSipStack(properties);
            this.shootme = new Shootme(shootmeStack);

        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            TestCase.fail("Unexpected exception");
        }

    }

    
    public void tearDown() throws Exception {
        Thread.sleep(30000);
        this.shootist.checkState();
        this.shootistStack.stop();
        this.shootmeStack.stop();
    }

    public void testSendInviteExpectTimeout() {
        this.shootist.sendInvite();
    }
}
