/*
 * Conditions Of Use 
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), and others. 
 * This software is has been contributed to the public domain. 
 * As a result, a formal license is not needed to use the software.
 * 
 * This software is provided "AS IS."  
 * NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * 
 */
/**
 * 
 */
package test.unit.gov.nist.javax.sip.stack;



import gov.nist.javax.sip.message.ResponseExt;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import test.tck.msgflow.callflows.NonSipUriRouter;

/**
 * @author M. Ranganathan
 * 
 */
public class ReInviteBusyTest extends TestCase {

    protected Shootist shootist;

    private Shootme shootme;

    private ProtocolObjects shootistProtocolObjs;

    private ProtocolObjects shootmeProtocolObjs;

    protected static final Appender console = new ConsoleAppender(new SimpleLayout());

    protected static Logger logger = Logger.getLogger(ReInviteBusyTest.class);
    
    

    static {

        if (!logger.isAttached(console))
            logger.addAppender(console);
    }

    private static String PEER_ADDRESS = Shootme.myAddress;

    private static int PEER_PORT = Shootme.myPort;

    private static String peerHostPort = PEER_ADDRESS + ":" + PEER_PORT;
    
    
   
    /**
     * @author M. Ranganathan
     *
     */
    public class ProtocolObjects {
        public final AddressFactory addressFactory;

        public final MessageFactory messageFactory;

        public final HeaderFactory headerFactory;

        public final SipStack sipStack;

        public int logLevel = 32;

        String logFileDirectory = "logs/";

        public final String transport;

        private boolean isStarted;

    	public boolean autoDialog;
    	

        public ProtocolObjects(String stackname, String pathname, String transport,
                boolean autoDialog, boolean isBackToBackUserAgent) {

        	this.autoDialog = autoDialog;
            this.transport = transport;
            SipFactory sipFactory = SipFactory.getInstance();
            sipFactory.resetFactory();
            sipFactory.setPathName(pathname);
            Properties properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", stackname);

            // The following properties are specific to nist-sip
            // and are not necessarily part of any other jain-sip
            // implementation.
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", logFileDirectory
                    + ReInviteBusyTest.class.getName() + "-debuglog.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    logFileDirectory + "ReInviteBusyTest-" + "log.txt");

            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
                    (autoDialog ? "on" : "off"));

            // For the forked subscribe notify test
            properties.setProperty("javax.sip.FORKABLE_EVENTS", "foo");

            //For the TelUrlRouter test.
            //properties.setProperty("javax.sip.ROUTER_PATH", NonSipUriRouter.class.getName());

            // Dont use the router for all requests.
            properties.setProperty("javax.sip.USE_ROUTER_FOR_ALL_URIS", "false");


            properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "1");
            
            properties.setProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.toString(isBackToBackUserAgent));
            //For the TelUrlRouter test.
            properties.setProperty("javax.sip.ROUTER_PATH", NonSipUriRouter.class.getName());


            // Set to 0 in your production code for max speed.
            // You need 16 for logging traces. 32 for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", new Integer(
                    logLevel).toString());
            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	logger.info("\nNIO Enabled\n");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);

                NonSipUriRouter router = (NonSipUriRouter) sipStack.getRouter();

                router.setMyPort(5080);

                System.out.println("createSipStack " + sipStack);
            } catch (Exception e) {
                // could not find
                // gov.nist.jain.protocol.ip.sip.SipStackImpl
                // in the classpath
                e.printStackTrace();
                System.err.println(e.getMessage());
                throw new RuntimeException("Stack failed to initialize");
            }

            try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
            } catch (SipException ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }

        public synchronized void destroy() {

            HashSet hashSet = new HashSet();

            for (Iterator it = sipStack.getSipProviders(); it.hasNext();) {

                SipProvider sipProvider = (SipProvider) it.next();
                hashSet.add(sipProvider);
            }

            for ( Iterator it = hashSet.iterator(); it.hasNext();) {
                SipProvider sipProvider = (SipProvider) it.next();

                for (int j = 0; j < 5; j++) {
                    try {
                        sipStack.deleteSipProvider(sipProvider);
                    } catch (ObjectInUseException ex) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }

                    }
                }
            }

            sipStack.stop();
        }

        public void start() throws Exception {
            if (this.isStarted)
                return;
            sipStack.start();
            this.isStarted = true;

        }
    }


    public class Shootme implements SipListener {

        private ProtocolObjects protocolObjects;

        // To run on two machines change these to suit.
        public static final String myAddress = "127.0.0.1";

        public static final int myPort = 5071;

        private ServerTransaction inviteTid;

        private Dialog dialog;

        private boolean okRecieved;

        private int reInviteCount;

        private boolean isToTagInTryingReInvitePresent = true;

        class ApplicationData {
            protected int ackCount;
        }

        public Shootme(ProtocolObjects protocolObjects) {
            this.protocolObjects = protocolObjects;
        }

        public void processRequest(RequestEvent requestEvent) {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod() + " received at "
                    + protocolObjects.sipStack.getStackName() + " with server transaction id "
                    + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE)) {
                processInvite(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.ACK)) {
                processAck(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.BYE)) {
                processBye(requestEvent, serverTransactionId);
            }

        }

        public void processAck(RequestEvent requestEvent, ServerTransaction serverTransaction) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            try {
                logger.info("shootme: got an ACK " + requestEvent.getRequest());

               /* int ackCount = ((ApplicationData) dialog.getApplicationData()).ackCount;*/

                dialog = inviteTid.getDialog();
                Thread.sleep(1000);
                logger.info("shootme is sending RE INVITE now");
                System.out.println("Got an ACK " );
                this.reInviteCount++;
                this.sendReInvite(sipProvider);

            } catch (Exception ex) {
                String s = "Unexpected error";
                logger.error(s, ex);
                ReInviteBusyTest.fail(s);
            }
        }

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
             try {
                ServerTransaction st = requestEvent.getServerTransaction();
                int finalResponse;
                logger.info("Got an INVITE  " + request + " serverTx = " + st);
                Thread.sleep(300);
                
                if (st == null) {
                    st = sipProvider.getNewServerTransaction(request);
                    logger.info("Server transaction created!" + request);

                    System.out.println("Dialog = " + st.getDialog());
                    if (st.getDialog().getApplicationData() == null) {
                        st.getDialog().setApplicationData(new ApplicationData());
                    }
                 
                    finalResponse = 200;
                } else {
                    // If Server transaction is not null, then
                    // this is a re-invite.
                	System.out.println("Dialog = " + st.getDialog());
                       
                    logger.info("This is a RE INVITE ");
                    this.reInviteCount++;
                    ReInviteBusyTest.assertSame("Dialog mismatch ", st.getDialog(), this.dialog);
                    finalResponse = Response.BUSY_HERE;
                }
                logger.info("shootme: got an Invite sending " + finalResponse);

                Response response = protocolObjects.messageFactory.createResponse(finalResponse,
                        request);
                ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                toHeader.setTag("4321");
                Address address = protocolObjects.addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = protocolObjects.headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);

                // Thread.sleep(5000);
                logger.info("got a server tranasaction " + st);
                byte[] content = request.getRawContent();
                if (content != null) {
                    ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
                            .createContentTypeHeader("application", "sdp");
                    response.setContent(content, contentTypeHeader);
                }
                dialog = st.getDialog();
                if (dialog != null) {
                    logger.info("Dialog " + dialog);
                    logger.info("Dialog state " + dialog.getState());
                }
                st.sendResponse(response);
                response = protocolObjects.messageFactory.createResponse(finalResponse, request);
                toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                toHeader.setTag("4321");
                // Application is supposed to set.
                response.addHeader(contactHeader);
                st.sendResponse(response);
                logger.info("TxState after sendResponse = " + st.getState());
                this.inviteTid = st;
            } catch (Exception ex) {
                String s = "unexpected exception";

                logger.error(s, ex);
                ReInviteBusyTest.fail(s);
            }
        }

        public void sendReInvite(SipProvider sipProvider) throws Exception {
            Request inviteRequest = dialog.createRequest(Request.INVITE);
            ((SipURI) inviteRequest.getRequestURI()).removeParameter("transport");
            MaxForwardsHeader mf = protocolObjects.headerFactory.createMaxForwardsHeader(10);
            inviteRequest.addHeader(mf);
            ((ViaHeader) inviteRequest.getHeader(ViaHeader.NAME))
                    .setTransport(protocolObjects.transport);
            Address address = protocolObjects.addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
            ContactHeader contactHeader = protocolObjects.headerFactory
                    .createContactHeader(address);
            inviteRequest.addHeader(contactHeader);
            ClientTransaction ct = sipProvider.getNewClientTransaction(inviteRequest);
            dialog.sendRequest(ct);
        }

        /**
         * Process the bye request.
         */
        public void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {

            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            try {
                logger.info("shootme:  got a bye sending OK.");
                Response response = protocolObjects.messageFactory.createResponse(200, request);
                if (serverTransactionId != null) {
                    serverTransactionId.sendResponse(response);
                    logger.info("Dialog State is " + serverTransactionId.getDialog().getState());
                } else {
                    logger.info("null server tx.");
                    // sipProvider.sendResponse(response);
                }

            } catch (Exception ex) {
                String s = "Unexpected exception";
                logger.error(s, ex);
                ReInviteBusyTest.fail(s);

            }
        }

        public void processResponse(ResponseEvent responseReceivedEvent) {
            logger.info("Got a response");
            Response response = (Response) responseReceivedEvent.getResponse();
            Transaction tid = responseReceivedEvent.getClientTransaction();

            logger.info("Response received with client transaction id " + tid + ":\n" + response);
            try {
                if (response.getStatusCode() == Response.OK
                        && ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod().equals(
                                Request.INVITE)) {
                    this.okRecieved = true;
                    ReInviteBusyTest.assertNotNull("INVITE 200 response should match a transaction",
                            tid);
                    Dialog dialog = tid.getDialog();
                    CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                    Request request = dialog.createAck(cseq.getSeqNumber());
                    dialog.sendAck(request);
                }
                if (tid != null) {
                    Dialog dialog = tid.getDialog();
                    logger.info("Dalog State = " + dialog.getState());
                    String toTag = ((ResponseExt)response).getToHeader().getTag(); 
                    // Note that TRYING is an optional response.
                    if(DialogState.CONFIRMED.equals(dialog.getState()) && toTag == null && response.getStatusCode() == Response.TRYING) {
                        this.isToTagInTryingReInvitePresent  = false;
                        logger.info("to tag for trying in re INVITE is present " + toTag);
                    }
                }
            } catch (Exception ex) {

                String s = "Unexpected exception";

                logger.error(s, ex);
                ReInviteBusyTest.fail(s);
            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
            Transaction transaction;
            if (timeoutEvent.isServerTransaction()) {
                transaction = timeoutEvent.getServerTransaction();
            } else {
                transaction = timeoutEvent.getClientTransaction();
            }
            logger.info("state = " + transaction.getState());
            logger.info("dialog = " + transaction.getDialog());
            logger.info("dialogState = " + transaction.getDialog().getState());
            logger.info("Transaction Time out");
        }

        public SipProvider createSipProvider() throws Exception {
            ListeningPoint lp = protocolObjects.sipStack.createListeningPoint(myAddress, myPort,
                    protocolObjects.transport);

            SipProvider sipProvider = protocolObjects.sipStack.createSipProvider(lp);
            return sipProvider;
        }

        public void checkState() {
            assertTrue("should see a re-INVITE", this.reInviteCount >=2 );
            assertTrue("To tag in trying for re-INVITE shoulnd't be null", isToTagInTryingReInvitePresent);
          
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
         */
        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.error("An IO Exception was detected : " + exceptionEvent.getHost());

        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
         */
        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            logger.info("Tx terminated event ");

        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
         */
        public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("Dialog terminated event detected ");
            fail("shootme: Dialog TERMINATED event unexpected");

        }

    }

    class Shootist implements SipListener {

        private SipProvider provider;

        private int reInviteCount;

        private ContactHeader contactHeader;

        private ListeningPoint listeningPoint;

        // To run on two machines change these to suit.
        public static final String myAddress = "127.0.0.1";

        private static final int myPort = 5072;

        protected ClientTransaction inviteTid;

        private boolean okReceived;

        int reInviteReceivedCount;

        private ProtocolObjects protocolObjects;

        private Dialog dialog;

        private boolean busyHereReceived;

        public Shootist(ProtocolObjects protocolObjects) {
            super();
            this.protocolObjects = protocolObjects;

        }

       
        public void processRequest(RequestEvent requestReceivedEvent) {
            Request request = requestReceivedEvent.getRequest();
            ServerTransaction serverTransactionId = requestReceivedEvent.getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod() + " received at "
                    + protocolObjects.sipStack.getStackName() + " with server transaction id "
                    + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE))
                processInvite(request, serverTransactionId);
            else if (request.getMethod().equals(Request.ACK))
                processAck(request, serverTransactionId);

        }

        public void processInvite(Request request, ServerTransaction st) {
            try {
                this.reInviteReceivedCount++;
                Dialog dialog = st.getDialog();
                logger.info("shootist received RE INVITE. Sending BUSY_HERE");
                assertEquals("Dialog state must in confirmed state ",DialogState.CONFIRMED,dialog.getState());
                
                Thread.sleep(300);
                
                Response response = protocolObjects.messageFactory.createResponse(Response.BUSY_HERE,
                        request);
                ((ToHeader) response.getHeader(ToHeader.NAME)).setTag(((ToHeader) request
                        .getHeader(ToHeader.NAME)).getTag());

                Address address = protocolObjects.addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = protocolObjects.headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);
                st.sendResponse(response);
                assertEquals("Dialog state must be in confirmed state ",DialogState.CONFIRMED,dialog.getState());
                ReInviteBusyTest.assertEquals("Dialog for reinvite must match original dialog",
                        dialog, this.dialog);

            } catch (Exception ex) {
                logger.error("unexpected exception", ex);
                ReInviteBusyTest.fail("unexpected exception");
            }
        }

        public void processAck(Request request, ServerTransaction tid) {
            try {
                logger.info("Got an ACK! ");
            } catch (Exception ex) {
                logger.error("unexpected exception", ex);
                ReInviteBusyTest.fail("unexpected exception");

            }
        }

        public void processResponse(ResponseEvent responseReceivedEvent) {
            logger.info("Got a response");

            Response response = (Response) responseReceivedEvent.getResponse();
            Transaction tid = responseReceivedEvent.getClientTransaction();

            logger.info("Response received with client transaction id " + tid + ":\n"
                    + response.getStatusCode());
            if (tid == null) {
                logger.info("Stray response -- dropping ");
                return;
            }
            logger.info("transaction state is " + tid.getState());
            logger.info("Dialog = " + tid.getDialog());
            logger.info("Dialog State is " + tid.getDialog().getState());
            SipProvider provider = (SipProvider) responseReceivedEvent.getSource();

            try {
                if (response.getStatusCode() == Response.OK
                        && ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod().equals(
                                Request.INVITE)) {

                    // Request cancel = inviteTid.createCancel();
                    // ClientTransaction ct =
                    // sipProvider.getNewClientTransaction(cancel);
                    Dialog dialog = tid.getDialog();
                    CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                    Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                    logger.info("Ack request to send = " + ackRequest);
                    logger.info("Sending ACK");
                    dialog.sendAck(ackRequest);

                    Thread.sleep(100);
                  
                    Request inviteRequest = dialog.createRequest(Request.INVITE);
                    ((SipURI) inviteRequest.getRequestURI()).removeParameter("transport");
                    ((ViaHeader) inviteRequest.getHeader(ViaHeader.NAME)).setTransport("udp");
                    inviteRequest.addHeader(contactHeader);
                    MaxForwardsHeader mf = protocolObjects.headerFactory
                            .createMaxForwardsHeader(10);
                    inviteRequest.addHeader(mf);
                     ClientTransaction ct = provider.getNewClientTransaction(inviteRequest);
                    dialog.sendRequest(ct);
                    reInviteCount++;
                    logger.info("RE-INVITE sent");

                } else if (response.getStatusCode() == Response.BUSY_HERE) {
                    this.busyHereReceived = true;
                    TestCase.assertEquals("Dialog State must be CONFIRMED", dialog.getState(), DialogState.CONFIRMED);
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                logger.error(ex);
                ReInviteBusyTest.fail("unexpceted exception");
            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

            logger.info("Transaction Time out");
            logger.info("TimeoutEvent " + timeoutEvent.getTimeout());
        }

        public SipProvider createSipProvider() {
            try {
                listeningPoint = protocolObjects.sipStack.createListeningPoint(myAddress, myPort,
                        protocolObjects.transport);

                provider = protocolObjects.sipStack.createSipProvider(listeningPoint);
                return provider;
            } catch (Exception ex) {
                logger.error(ex);
                ReInviteBusyTest.fail("unable to create provider");
                return null;
            }
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
                SipURI fromAddress = protocolObjects.addressFactory.createSipURI(fromName,
                        fromSipAddress);

                Address fromNameAddress = protocolObjects.addressFactory
                        .createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = protocolObjects.headerFactory.createFromHeader(
                        fromNameAddress, new Integer((int) (Math.random() * Integer.MAX_VALUE))
                                .toString());

                // create To Header
                SipURI toAddress = protocolObjects.addressFactory.createSipURI(toUser,
                        toSipAddress);
                Address toNameAddress = protocolObjects.addressFactory.createAddress(toAddress);
                toNameAddress.setDisplayName(toDisplayName);
                ToHeader toHeader = protocolObjects.headerFactory.createToHeader(toNameAddress,
                        null);

                // create Request URI
                SipURI requestURI = protocolObjects.addressFactory.createSipURI(toUser,
                        peerHostPort);

                // Create ViaHeaders

                ArrayList viaHeaders = new ArrayList();
                int port = provider.getListeningPoint(protocolObjects.transport).getPort();

                ViaHeader viaHeader = protocolObjects.headerFactory.createViaHeader(myAddress,
                        port, protocolObjects.transport, null);

                // add via headers
                viaHeaders.add(viaHeader);

                // Create ContentTypeHeader
                ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
                        .createContentTypeHeader("application", "sdp");

                // Create a new CallId header
                CallIdHeader callIdHeader = provider.getNewCallId();
                // JvB: Make sure that the implementation matches the messagefactory
                callIdHeader = protocolObjects.headerFactory.createCallIdHeader(callIdHeader
                        .getCallId());

                // Create a new Cseq header
                CSeqHeader cSeqHeader = protocolObjects.headerFactory.createCSeqHeader(1L,
                        Request.INVITE);

                // Create a new MaxForwardsHeader
                MaxForwardsHeader maxForwards = protocolObjects.headerFactory
                        .createMaxForwardsHeader(70);

                // Create the request.
                Request request = protocolObjects.messageFactory.createRequest(requestURI,
                        Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader,
                        viaHeaders, maxForwards);
                // Create contact headers

                // Create the contact name address.
                SipURI contactURI = protocolObjects.addressFactory.createSipURI(fromName,
                        myAddress);
                contactURI.setPort(provider.getListeningPoint(protocolObjects.transport)
                        .getPort());

                Address contactAddress = protocolObjects.addressFactory.createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                contactHeader = protocolObjects.headerFactory.createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                // Add the extension header.
                Header extensionHeader = protocolObjects.headerFactory.createHeader("My-Header",
                        "my header value");
                request.addHeader(extensionHeader);

                String sdpData = "v=0\r\n" + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                        + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n" + "t=0 0\r\n"
                        + "m=audio 6022 RTP/AVP 0 4 18\r\n" + "a=rtpmap:0 PCMU/8000\r\n"
                        + "a=rtpmap:4 G723/8000\r\n" + "a=rtpmap:18 G729A/8000\r\n"
                        + "a=ptime:20\r\n";

                request.setContent(sdpData, contentTypeHeader);

                // The following is the preferred method to route requests
                // to the peer. Create a route header and set the "lr"
                // parameter for the router header.

                Address address = protocolObjects.addressFactory.createAddress("<sip:"
                        + PEER_ADDRESS + ":" + PEER_PORT + ">");
                // SipUri sipUri = (SipUri) address.getURI();
                // sipUri.setPort(PEER_PORT);

                RouteHeader routeHeader = protocolObjects.headerFactory
                        .createRouteHeader(address);
                ((SipURI) address.getURI()).setLrParam();
                request.addHeader(routeHeader);
                extensionHeader = protocolObjects.headerFactory.createHeader("My-Other-Header",
                        "my new header value ");
                request.addHeader(extensionHeader);

                Header callInfoHeader = protocolObjects.headerFactory.createHeader("Call-Info",
                        "<http://www.antd.nist.gov>");
                request.addHeader(callInfoHeader);

                // Create the client transaction.
                this.inviteTid = provider.getNewClientTransaction(request);
                this.dialog = this.inviteTid.getDialog();
                // Note that the response may have arrived right away so
                // we cannot check after the message is sent.
                ReInviteBusyTest.assertTrue(this.dialog.getState() == null);

                // send the request out.
                this.inviteTid.sendRequest();

            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                ReInviteBusyTest.fail("unexpected exception");
            }
        }

        public void checkState() {
            ReInviteBusyTest.assertTrue("Expect to send a re-invite", reInviteCount == 1);
           // ReInviteBusyTest.assertTrue("Expecting a BUSY here", this.busyHereReceived);

        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
         */
        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.error("IO Exception!");
            ReInviteBusyTest.fail("Unexpected exception");

        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
         */
        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {

            logger.info("Transaction Terminated Event!");
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
         */
        public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("Dialog Terminated Event!");
            fail("shootist: unexpected event -- dialog terminated!");

        }
    }

    public ReInviteBusyTest() {
        super("reinvitetest");
    }

    public void setUp() {

        try {

            super.setUp();
            // String stackname, String pathname, String transport,
            // boolean autoDialog
            this.shootistProtocolObjs = new ProtocolObjects("shootist", "gov.nist", "udp", true,true);
            shootist = new Shootist(shootistProtocolObjs);
            SipProvider shootistProvider = shootist.createSipProvider();

            this.shootmeProtocolObjs = new ProtocolObjects("shootme", "gov.nist", "udp", true,true);
            shootme = new Shootme(shootmeProtocolObjs);
            SipProvider shootmeProvider = shootme.createSipProvider();
            
            shootistProvider.addSipListener(shootist);
            shootmeProvider.addSipListener(shootme);

            shootistProtocolObjs.start();
            shootmeProtocolObjs.start();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    }

    public void testSendInvite() {
        this.shootist.sendInvite();
    }

    public void tearDown() {
        try {
            Thread.sleep(10000);
            this.shootist.checkState();
            this.shootme.checkState();
            shootmeProtocolObjs.destroy();
            shootistProtocolObjs.destroy();
            Thread.sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
