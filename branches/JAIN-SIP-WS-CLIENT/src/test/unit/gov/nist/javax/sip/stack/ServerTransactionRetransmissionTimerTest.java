package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionState;
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
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class ServerTransactionRetransmissionTimerTest extends TestCase {
    public static final boolean callerSendsBye = true;

    private static Logger logger = Logger.getLogger( ServerTransactionRetransmissionTimerTest.class);
    static {
        if ( ! logger.getAllAppenders().hasMoreElements())
            logger.addAppender(new ConsoleAppender(new SimpleLayout()));
    }
    class Shootist implements SipListener {

        private SipProvider sipProvider;

        private AddressFactory addressFactory;

        private MessageFactory messageFactory;

        private HeaderFactory headerFactory;

        private SipStack sipStack;

        private ContactHeader contactHeader;

        private ListeningPoint udpListeningPoint;

        private ClientTransaction inviteTid;

        private Dialog dialog;

        private long startTime = System.currentTimeMillis();

        private boolean byeTaskRunning;
        
        public boolean sendAck = true;

        class ByeTask extends TimerTask {
            Dialog dialog;

            public ByeTask(Dialog dialog) {
                this.dialog = dialog;
            }

            public void run() {
                try {
                    Request byeRequest = this.dialog.createRequest(Request.BYE);
                    ClientTransaction ct = sipProvider
                            .getNewClientTransaction(byeRequest);
                    logger.info("15s are over: sending BYE now");
                    dialog.sendRequest(ct);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(0);
                }

            }

        }

        private static final String usageString = "java "
                + "examples.shootist.Shootist \n"
                + ">>>> is your class path set to the root?";

        public void processRequest(RequestEvent requestReceivedEvent) {
            Request request = requestReceivedEvent.getRequest();
            ServerTransaction serverTransactionId = requestReceivedEvent
                    .getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod()
                    + " received at " + sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            // We are the UAC so the only request we get is the BYE.
            if (request.getMethod().equals(Request.BYE))
                processBye(request, serverTransactionId);
            else {
                try {
                    serverTransactionId.sendResponse(messageFactory
                            .createResponse(202, request));
                } catch (Exception e) {
                    fail("Unexpected exception");
                    logger.error("Unexpected exception", e);
                }
            }

        }

        public void processBye(Request request,
                ServerTransaction serverTransactionId) {
            try {
                logger.info("shootist:  got a bye .");
                if (serverTransactionId == null) {
                    logger.info("shootist:  null TID.");
                    return;
                }
                Dialog dialog = serverTransactionId.getDialog();
                logger.info("Dialog State = " + dialog.getState());
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                logger.info("shootist:  Sending OK.");
                logger.info("Dialog State = " + dialog.getState());

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);

            }
        }

        // Save the created ACK request, to respond to retransmitted 2xx
        private Request ackRequest;

        public void processResponse(ResponseEvent responseReceivedEvent) {
            logger.info(System.currentTimeMillis() - startTime
                    + "ms: Got a response");
            Response response = (Response) responseReceivedEvent.getResponse();
            ClientTransaction tid = responseReceivedEvent
                    .getClientTransaction();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            logger.info("Response received : Status Code = "
                    + response.getStatusCode() + " " + cseq);

            if (tid == null) {
                TestCase.assertTrue("retrans flag should be true", ((ResponseEventExt)responseReceivedEvent).isRetransmission());
                // RFC3261: MUST respond to every 2xx
                if (ackRequest != null && dialog != null) {
                    logger.info("re-sending ACK");
                    try {
                        dialog.sendAck(ackRequest);
                    } catch (SipException se) {
                        se.printStackTrace();
                    }
                }
                return;
            }
            // If the caller is supposed to send the bye
            if (callerSendsBye
                    && !byeTaskRunning) {
                byeTaskRunning = true;
                new Timer().schedule(new ByeTask(dialog), 30000); // Frank
                                                                    // Reif:
                                                                    // modified
                                                                    // from 4000
                                                                    // to 15000
                                                                    // to allow
                                                                    // delayed-ack
                                                                    // demo
            }
            logger.info("transaction state is " + tid.getState());
            logger.info("Dialog = " + tid.getDialog());
            logger.info("Dialog State is " + tid.getDialog().getState());

            try {
                if (response.getStatusCode() == Response.OK) {
                    if (cseq.getMethod().equals(Request.INVITE)) {

                        if (sendAck) {
                            TestCase.assertFalse("retrans flag should be false", ((ResponseEventExt)responseReceivedEvent).isRetransmission());
                            // *****************************************************************
                            // BEGIN
                            // Frank Reif: delayed-ack after 6s
                            logger.info("Sending ACK after 15s ...");
                            new Timer().schedule(new AckTimerTask(dialog,cseq.getSeqNumber()), 15000);
    
                            // JvB: test REFER, reported bug in tag handling
                            // dialog.sendRequest(
                            // sipProvider.getNewClientTransaction(
                            // dialog.createRequest("REFER") ));
    
                            // *****************************************************************
                            // END
                        }

                    } else if (cseq.getMethod().equals(Request.CANCEL)) {
                        if (dialog.getState() == DialogState.CONFIRMED) {
                            // oops cancel went in too late. Need to hang up the
                            // dialog.
                            System.out
                                    .println("Sending BYE -- cancel went in too late !!");
                            Request byeRequest = dialog
                                    .createRequest(Request.BYE);
                            ClientTransaction ct = sipProvider
                                    .getNewClientTransaction(byeRequest);
                            dialog.sendRequest(ct);

                        }

                    }
                }
            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                fail("Unexpected exception");

            }

        }

        // *****************************************************************
        // BEGIN
        // Frank Reif: delayed-ack after 10s
        class AckTimerTask extends TimerTask {
            Dialog dialog;
            private final long cseq;

            public AckTimerTask(Dialog dialog,long cseq) {
                this.dialog = dialog;
                this.cseq = cseq;
            }

            public void run() {
                logger.info("15s are over: now sending ACK");
                try {
                    Request ackRequest = dialog.createAck(cseq);
                    dialog.sendAck(ackRequest);
                } catch (Exception e) {
                    logger.error("Unexpected exception", e);
                    fail ("Unexpected exception");
                }
            }
        }

        // ***************************************************************** END

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

            logger.info("Transaction Time out");
            fail("Unexpected exception -- ");
        }

        public void init() {
            SipFactory sipFactory = null;
            sipStack = null;
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            // If you want to try TCP transport change the following to
            String transport = "udp";
            String peerHostPort = "127.0.0.1:5070";
            properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort
                    + "/" + transport);
            // If you want to use UDP then uncomment this.
            properties.setProperty("javax.sip.STACK_NAME", "shootist");

            // The following properties are specific to nist-sip
            // and are not necessarily part of any other jain-sip
            // implementation.
            // You can set a max message size for tcp transport to
            // guard against denial of service attack.
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "logs/" + this.getClass().getName() + ".shootistdebug.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "logs/" + this.getClass().getName() + ".shootistlog.txt");

            // Drop the client connection after we are done with the
            // transaction.
            properties.setProperty(
                    "gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
            // Set to 0 (or NONE) in your production code for max speed.
            // You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for
            // debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	logger.info("\nNIO Enabled\n");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);
                logger.info("createSipStack " + sipStack);
            } catch (PeerUnavailableException e) {
                // could not find
                // gov.nist.jain.protocol.ip.sip.SipStackImpl
                // in the classpath
                e.printStackTrace();
                System.err.println(e.getMessage());
                System.exit(0);
            }

            try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                udpListeningPoint = sipStack.createListeningPoint("127.0.0.1",
                        5060, "udp");
                sipProvider = sipStack.createSipProvider(udpListeningPoint);
                Shootist listener = this;
                sipProvider.addSipListener(listener);

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
                        fromNameAddress, "12345");

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
                String ipAddress = udpListeningPoint.getIPAddress();
                ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                        sipProvider.getListeningPoint(transport).getPort(),
                        transport, null);

                // add via headers
                viaHeaders.add(viaHeader);

                // Create ContentTypeHeader
                ContentTypeHeader contentTypeHeader = headerFactory
                        .createContentTypeHeader("application", "sdp");

                // Create a new CallId header
                CallIdHeader callIdHeader = sipProvider.getNewCallId();

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
                String host = "127.0.0.1";

                SipURI contactUrl = addressFactory.createSipURI(fromName, host);
                contactUrl.setPort(udpListeningPoint.getPort());
                contactUrl.setLrParam();

                // Create the contact name address.
                SipURI contactURI = addressFactory.createSipURI(fromName, host);
                contactURI.setPort(sipProvider.getListeningPoint(transport)
                        .getPort());

                Address contactAddress = addressFactory
                        .createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                contactHeader = headerFactory
                        .createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                // You can add extension headers of your own making
                // to the outgoing SIP request.
                // Add the extension header.
                Header extensionHeader = headerFactory.createHeader(
                        "My-Header", "my header value");
                request.addHeader(extensionHeader);

                String sdpData = "v=0\r\n"
                        + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n"
                        + "s=mysession session\r\n" + "p=+46 8 52018010\r\n"
                        + "c=IN IP4  129.6.55.78\r\n" + "t=0 0\r\n"
                        + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n"
                        + "a=rtpmap:4 G723/8000\r\n"
                        + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
                byte[] contents = sdpData.getBytes();

                request.setContent(contents, contentTypeHeader);
                // You can add as many extension headers as you
                // want.

                extensionHeader = headerFactory.createHeader("My-Other-Header",
                        "my new header value ");
                request.addHeader(extensionHeader);

                Header callInfoHeader = headerFactory.createHeader("Call-Info",
                        "<http://www.antd.nist.gov>");
                request.addHeader(callInfoHeader);

                // Create the client transaction.
                inviteTid = sipProvider.getNewClientTransaction(request);

                // send the request out.
                inviteTid.sendRequest();

                dialog = inviteTid.getDialog();

            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                fail("Unexpected exception ");
            }
        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.error("IOException happened for "
                    + exceptionEvent.getHost() + " port = "
                    + exceptionEvent.getPort());

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            if ( transactionTerminatedEvent.getServerTransaction() != null)
                logger.info("Shootist: Transaction terminated event recieved on transaction : " +
                    transactionTerminatedEvent.getServerTransaction());
            else
                logger.info("Shootist : Transaction terminated event recieved on transaction : " +
                    transactionTerminatedEvent.getClientTransaction());
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("dialogTerminatedEvent");

        }

        public void terminate() {
            sipStack.stop();
        }
    }

    public class Shootme implements SipListener {

        private AddressFactory addressFactory;

        private MessageFactory messageFactory;

        private HeaderFactory headerFactory;

        private SipStack sipStack=null;

        private static final String myAddress = "127.0.0.1";

        private static final int myPort = 5070;

        protected ServerTransaction inviteTid;

        private Response okResponse;

        private Request inviteRequest;

        private Dialog dialog;

		public boolean enableRetransmitAlerts;
		
		ListeningPoint lp = null;
		SipProvider sipProvider = null;


        class MyTimerTask extends TimerTask {
            Shootme shootme;

            public MyTimerTask(Shootme shootme) {
                this.shootme = shootme;

            }

            public void run() {
                shootme.sendInviteOK();
            }

        }

        protected static final String usageString = "java "
                + "examples.shootist.Shootist \n"
                + ">>>> is your class path set to the root?";

        public void processRequest(RequestEvent requestEvent) {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransactionId = requestEvent
                    .getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod()
                    + " received at " + sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE)) {
                processInvite(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.ACK)) {
                processAck(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.BYE)) {
                processBye(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.CANCEL)) {
                processCancel(requestEvent, serverTransactionId);
            } else {
                try {
                    serverTransactionId.sendResponse(messageFactory
                            .createResponse(202, request));

                    // send one back
                    SipProvider prov = (SipProvider) requestEvent.getSource();
                    Request refer = requestEvent.getDialog().createRequest(
                            "REFER");
                    requestEvent.getDialog().sendRequest(
                            prov.getNewClientTransaction(refer));

                } catch (Exception e) {
                    fail("Unexpected exception");
                }
            }

        }

        public void processResponse(ResponseEvent responseEvent) {
            fail("Unexpected event");
        }

        /**
         * Process the ACK request. Send the bye and complete the call flow.
         */
        public void processAck(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            try {
                logger.info("shootme: got an ACK! ");
                if(!enableRetransmitAlerts) {
                	logger.info("Dialog State = " + dialog.getState());
                }
                SipProvider provider = (SipProvider) requestEvent.getSource();
                if (!callerSendsBye) {
                    Request byeRequest = dialog.createRequest(Request.BYE);
                    ClientTransaction ct = provider
                            .getNewClientTransaction(byeRequest);
                    dialog.sendRequest(ct);
                }
            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                fail("unexpected exception");
            }

        }

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            try {
                logger.info("shootme: got an Invite sending Trying");
                // logger.info("shootme: " + request);
                Response response = messageFactory.createResponse(
                        Response.TRYING, request);
                ServerTransaction st = requestEvent.getServerTransaction();

                if (st == null) {
                    st = sipProvider.getNewServerTransaction(request);
                    if(enableRetransmitAlerts) {
                    	st.enableRetransmissionAlerts();
                    }
                }
                dialog = st.getDialog();
//                if(enableRetransmitAlerts) {
//                	dialog = sipProvider.getNewDialog(st);
//                }

                st.sendResponse(response);

                this.okResponse = messageFactory.createResponse(Response.OK,
                        request);
                Address address = addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);
                ToHeader toHeader = (ToHeader) okResponse
                        .getHeader(ToHeader.NAME);
                toHeader.setTag("4321"); // Application is supposed to set.
                okResponse.addHeader(contactHeader);
                this.inviteTid = st;
                // Defer sending the OK to simulate the phone ringing.
                // Answered in 1 second ( this guy is fast at taking calls)
                this.inviteRequest = request;

                new Timer().schedule(new MyTimerTask(this), 1000);
            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                fail("Unexpected exception");
            }
        }

        private void sendInviteOK() {
            try {
                if (inviteTid.getState() != TransactionState.COMPLETED) {
                	if(!enableRetransmitAlerts) {
                		logger.info("shootme: Dialog state before 200: "
                            + inviteTid.getDialog().getState());
                	}
                    inviteTid.sendResponse(okResponse);
                    if(!enableRetransmitAlerts) {
                    	logger.info("shootme: Dialog state after 200: "
                            + inviteTid.getDialog().getState());
                    }
                }
            } catch (Exception ex) {
                logger.error("Unexpected exception",ex);
                fail("Unexpected exception");
            }
        }

        /**
         * Process the bye request.
         */
        public void processBye(RequestEvent requestEvent,
                ServerTransaction serverTransactionId) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            Dialog dialog = requestEvent.getDialog();
            logger.info("shootme: local party = " + dialog.getLocalParty());
            try {
                logger.info("shootme:  got a bye sending OK.");
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                logger.info("shootme: Dialog State is "
                        + serverTransactionId.getDialog().getState());

            } catch (Exception ex) {
                logger.error("UNexpected exception",ex);
                fail("UNexpected exception");

            }
        }

        public void processCancel(RequestEvent requestEvent,
                ServerTransaction serverTransactionId) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            try {
                logger.info("shootme:  got a cancel.");
                if (serverTransactionId == null) {
                    logger.info("shootme:  null tid.");
                    return;
                }
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                if (dialog.getState() != DialogState.CONFIRMED) {
                    response = messageFactory.createResponse(
                            Response.REQUEST_TERMINATED, inviteRequest);
                    inviteTid.sendResponse(response);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);

            }
        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
            Transaction transaction;
            if (timeoutEvent.isServerTransaction()) {
                transaction = timeoutEvent.getServerTransaction();
            } else {
                transaction = timeoutEvent.getClientTransaction();
            }
            logger.info("Shootme: Transaction Time out : " + transaction);
        }

        public void init(String name) {
            SipFactory sipFactory = null;
            sipStack = null;
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", name);
            // You need 16 for logging traces. 32 for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "logs/stxretransmission_shootmedebug.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "logs/stxretransmission_shootmelog.txt");
            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	logger.info("\nNIO Enabled\n");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            if(enableRetransmitAlerts) {
            	properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
            }
            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);
                logger.info("sipStack = " + sipStack);
            } catch (PeerUnavailableException e) {
                // could not find
                // gov.nist.jain.protocol.ip.sip.SipStackImpl
                // in the classpath
                e.printStackTrace();
                System.err.println(e.getMessage());
                if (e.getCause() != null)
                    e.getCause().printStackTrace();
                logger.error("Unexpected error creating stack", e);
                fail ("Unexpected error");
            }

            try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                lp = sipStack.createListeningPoint("127.0.0.1",
                        myPort, "udp");

                Shootme listener = this;

                sipProvider = sipStack.createSipProvider(lp);
                logger.info("udp provider " + sipProvider);
                sipProvider.addSipListener(listener);

            } catch (Exception ex) {
                fail("Unexpected exception");
            }

        }

        public void terminate() {        	
        	sipProvider.removeSipListener(this);
            while (true) {
                try {                	
                	sipStack.deleteListeningPoint(lp);
                    // This will close down the stack and exit all threads                    
                    sipStack.deleteSipProvider(sipProvider);
                    break;
                } catch (ObjectInUseException ex) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
            this.sipStack.stop();
        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.info("IOException");

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            if (transactionTerminatedEvent.isServerTransaction())
                logger.info("Transaction terminated event recieved"
                        + transactionTerminatedEvent.getServerTransaction());
            else
                logger.info("Transaction terminated "
                        + transactionTerminatedEvent.getClientTransaction());

        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("Shootme: Dialog terminated event recieved");
            Dialog d = dialogTerminatedEvent.getDialog();
            logger.info("Local Party = " + d.getLocalParty());

        }

    }

    private Shootist shootist;
    private Shootme shootme;

    public void setUp() {
        this.shootme = new Shootme();
        this.shootist = new Shootist();


    }
    public void tearDown() {
        shootist.terminate();
        shootme.terminate();
    }

    public void testRetransmit() {
        this.shootme.init("shootme_retransmit");
        this.shootist.init();
        try {
            Thread.sleep(60000);
        } catch (Exception ex) {

        }
    }
    
    public void testRetransmitNoAckSent() {
        this.shootme.init("shootme_retransmit_ack_sent");
        this.shootist.sendAck = false;
        this.shootist.init();
        try {
            Thread.sleep(60000);
        } catch (Exception ex) {

        }
    }
    
    // Non Regression Test for Issue http://java.net/jira/browse/JSIP-443
    public void testEnableRetransmitionAlertsLeaks() {
    	this.shootme.enableRetransmitAlerts = true;
        this.shootme.init("shootme_retransmit_alerts");        
        this.shootist.init();
        try {
            Thread.sleep(60000);
        } catch (Exception ex) {

        }
        assertNull(((SipStackImpl)this.shootme.sipStack).getRetransmissionAlertTransaction(((SIPResponse)this.shootme.okResponse).getDialogId(true)));
    }

}
