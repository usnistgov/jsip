package test.unit.gov.nist.javax.sip.stack;

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
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
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import test.tck.TestHarness;
import test.tck.msgflow.callflows.ProtocolObjects;
import test.tck.msgflow.callflows.ScenarioHarness;

public class UdpPrackTimeoutTest extends ScenarioHarness implements SipListener {

    protected Shootist shootist;

    protected Shootme shootme;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        if (!logger.isAttached(console)) {
            logger.addAppender(console);
        }
    }

    public UdpPrackTimeoutTest() {
        super("reliableResponseTimeout", true);
    }

    public void setUp() throws Exception {
        try {
            testedImplFlag = !myFlag;
            myFlag = !testedImplFlag;
            super.transport = "udp";
            super.setUp();

            logger.info("PrackTest: setup()");
            shootist = new Shootist(getTiProtocolObjects());
            SipProvider shootistProvider = shootist.createProvider();
            providerTable.put(shootistProvider, shootist);

            shootme = new Shootme(getRiProtocolObjects());
            SipProvider shootmeProvider = shootme.createProvider();
            providerTable.put(shootmeProvider, shootme);

            shootistProvider.addSipListener(this);
            shootmeProvider.addSipListener(this);

            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
            getRiProtocolObjects().start();
        } catch (Exception ex) {
            logger.error("unexpected excecption ", ex);
            fail("unexpected exception");
        }
    }

    public void tearDown() throws Exception {
        try {
            Thread.sleep(50000);
            this.shootist.checkState();
            this.shootme.checkState();
            getTiProtocolObjects().destroy();
            if (getRiProtocolObjects() != getTiProtocolObjects())
                getRiProtocolObjects().destroy();
            Thread.sleep(1000);
            this.providerTable.clear();

            logTestCompleted();
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("unexpected exception ");
        }
        super.tearDown();
    }

    public class Shootist implements SipListener {

        private SipProvider sipProvider;

        private AddressFactory addressFactory;

        private MessageFactory messageFactory;

        private HeaderFactory headerFactory;

        private SipStack sipStack;

        private ContactHeader contactHeader;

        private ClientTransaction inviteTid;

        private Dialog dialog;

        private String transport;

        private boolean prackTriggerReceived;
        private boolean prackConfirmed;

        public static final int myPort = 5070;

        private String toUser = "LittleGuy";

        private Response lastResponse;

        public Shootist(ProtocolObjects protObjects) {
            addressFactory = protObjects.addressFactory;
            messageFactory = protObjects.messageFactory;
            headerFactory = protObjects.headerFactory;
            sipStack = protObjects.sipStack;
            transport = protObjects.transport;
        }

        public SipProvider createProvider() throws Exception {
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    myPort, transport);

            sipProvider = sipStack.createSipProvider(lp);
            logger.info(transport + " SIP provider " + sipProvider);

            return sipProvider;
        }


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
                TestHarness.fail(ex.getMessage());
                System.exit(0);

            }
        }

        public void processResponse(ResponseEvent responseReceivedEvent) {
            logger.info("Got a response");
            Response response = (Response) responseReceivedEvent.getResponse();
            lastResponse = response;
            ClientTransaction tid = responseReceivedEvent.getClientTransaction();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            logger.info("Response received : Status Code = "
                    + response.getStatusCode() + " " + cseq);

            if (cseq.getMethod() == Request.PRACK) {
                prackConfirmed = true;
            }

            if (tid == null) {
                logger.info("Stray response -- dropping ");
                return;
            }
            logger.info("transaction state is " + tid.getState());
            logger.info("Dialog = " + tid.getDialog());
            logger.info("Dialog State is " + tid.getDialog().getState());
            SipProvider provider = (SipProvider) responseReceivedEvent.getSource();
            dialog = tid.getDialog();

            try {
                if (response.getStatusCode() == Response.OK) {
                    if (cseq.getMethod().equals(Request.INVITE)) {
                        Request ackRequest = dialog.createAck(((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getSeqNumber());
                        logger.info("Sending ACK");
                        dialog.sendAck(ackRequest);
                    }

                } else if ( response.getStatusCode() == Shootme.PRACK_CODE) {
//                  prackTriggerReceived = true;
//                  RequireHeader requireHeader = (RequireHeader) response.getHeader(RequireHeader.NAME);
//                  if ( requireHeader.getOptionTag().equalsIgnoreCase("100rel")) {
//                      Dialog dialog = tid.getDialog();
//                      Request prackRequest = dialog.createPrack(response);
//                      // create Request URI
//                      SipURI requestURI = addressFactory.createSipURI(toUser,
//                              "127.0.0.1:" + Shootme.myPort);
//                      prackRequest.setRequestURI(requestURI);
//                      ClientTransaction ct = provider.getNewClientTransaction(prackRequest);
//                      dialog.sendRequest(ct);
//                  }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                TestHarness.fail("Unexpected exception " + ex.getMessage());

            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

            logger.info("Transaction Time out");
        }

        public void sendInvite() {
            try {
                String fromName = "BigGuy";
                String fromSipAddress = "here.com";
                String fromDisplayName = "The Master Blaster";

                String toSipAddress = "there.com";
                String toDisplayName = "The Little Blister";

                // create >From Header
                SipURI fromAddress = addressFactory.createSipURI(fromName,
                        fromSipAddress);

                Address fromNameAddress = addressFactory.createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = headerFactory.createFromHeader(
                        fromNameAddress, "12345");

                // create To Header
                SipURI toAddress = addressFactory
                        .createSipURI(toUser, toSipAddress);
                Address toNameAddress = addressFactory.createAddress(toAddress);
                toNameAddress.setDisplayName(toDisplayName);
                ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                        null);

                // create Request URI
                SipURI requestURI = addressFactory.createSipURI(toUser,
                        "127.0.0.1:" + Shootme.myPort);

                // Create ViaHeaders

                ArrayList viaHeaders = new ArrayList();
                ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                        sipProvider.getListeningPoint(transport).getPort(),
                        transport, null);

                // add via headers
                viaHeaders.add(viaHeader);

                // Create ContentTypeHeader
                ContentTypeHeader contentTypeHeader = headerFactory
                        .createContentTypeHeader("application", "sdp");

                // Create a new CallId header
                CallIdHeader callIdHeader = sipProvider.getNewCallId();
                // JvB: Make sure that the implementation matches the messagefactory
                callIdHeader = headerFactory.createCallIdHeader( callIdHeader.getCallId() );


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
                ListeningPoint lp = sipProvider.getListeningPoint(transport);
                contactUrl.setPort(lp.getPort());

                // Create the contact name address.
                SipURI contactURI = addressFactory.createSipURI(fromName, host);
                contactURI.setPort(sipProvider.getListeningPoint(transport)
                        .getPort());

                Address contactAddress = addressFactory.createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                contactHeader = headerFactory.createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                /*
                 * When the UAC creates a new request, it can insist on reliable
                 * delivery of provisional responses for that request. To do that,
                 * it inserts a Require header field with the option tag 100rel into
                 * the request.
                 */

                RequireHeader requireHeader = headerFactory
                        .createRequireHeader("100rel");
                request.addHeader(requireHeader);
                // Create the client transaction.
                inviteTid = sipProvider.getNewClientTransaction(request);

                this.dialog = inviteTid.getDialog();

                // send the request out.
                inviteTid.sendRequest();


            } catch (Exception ex) {
                TestHarness.fail("sendInvite failed because of " + ex.getMessage());
            }
        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.info("IOException happened for "
                    + exceptionEvent.getHost() + " port = "
                    + exceptionEvent.getPort());

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            logger.info("Transaction terminated event recieved");
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("dialogTerminatedEvent");
        }

        public void checkState() {
            TestHarness.assertNotNull(this.lastResponse);
            TestHarness.assertTrue(this.lastResponse.getStatusCode() == 500 );
        }
    }

    public class Shootme implements SipListener {

        protected static final int PRACK_CODE = 183;

        private SipProvider sipProvider;

        private AddressFactory addressFactory;

        private MessageFactory messageFactory;

        private HeaderFactory headerFactory;

        private SipStack sipStack;

        private static final String myAddress = "127.0.0.1";

        protected ServerTransaction inviteTid;

        private Request inviteRequest;

        private Dialog dialog;

        private String toTag;

        private String transport;

        private boolean prackRequestReceived;

        private boolean inviteReceived;

        private boolean errorResponseSent;

        private boolean transactionTimedOut;

        public static final int myPort = 5080;

        public Shootme(ProtocolObjects protObjects) {
            addressFactory = protObjects.addressFactory;
            messageFactory = protObjects.messageFactory;
            headerFactory = protObjects.headerFactory;
            sipStack = protObjects.sipStack;
            transport = protObjects.transport;
        }

        public void processRequest(RequestEvent requestEvent) {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransactionId = requestEvent
                    .getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod() + " received at "
                    + sipStack.getStackName() + " with server transaction id "
                    + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE)) {
                processInvite(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.ACK)) {
                processAck(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.BYE)) {
                processBye(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.PRACK)) {
                processPrack(requestEvent, serverTransactionId);
            }

        }

        private void processPrack(RequestEvent requestEvent,
                ServerTransaction serverTransactionId) {
            prackRequestReceived = true;
            try {
                logger.info("shootme: got an PRACK! ");
                logger.info("Dialog State = " + dialog.getState());

                /**
                 * JvB: First, send 200 OK for PRACK
                 */
                Request prack = requestEvent.getRequest();
                Response prackOk = messageFactory.createResponse(200, prack);
                serverTransactionId.sendResponse(prackOk);

                /**
                 * Send a 200 OK response to complete the 3 way handshake for the
                 * INIVTE.
                 */
                Response response = messageFactory.createResponse(200,
                        inviteRequest);
                ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
                to.setTag(this.toTag);
                Address address = addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);
                inviteTid.sendResponse(response);
            } catch (Exception ex) {
                TestHarness.fail(ex.getMessage());
            }
        }

        public void processResponse(ResponseEvent responseEvent) {
        }

        /**
         * Process the ACK request. Send the bye and complete the call flow.
         */
        public void processAck(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {

            try {
                logger.info("shootme: got an ACK! Sending  a BYE");
                logger.info("Dialog State = " + dialog.getState());

                // JvB: there should not be a transaction for ACKs; requestEvent
                // can be used to get it instead
                // Dialog dialog = serverTransaction.getDialog();
                Dialog dialog = requestEvent.getDialog();

                SipProvider provider = (SipProvider) requestEvent.getSource();
                Request byeRequest = dialog.createRequest(Request.BYE);
                ClientTransaction ct = provider.getNewClientTransaction(byeRequest);
                dialog.sendRequest(ct);
            } catch (Exception ex) {
                ex.printStackTrace();
                TestHarness.fail(ex.getMessage());
            }

        }

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            inviteReceived = true;
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            try {
                logger.info("shootme: got an Invite sending Trying");
                // logger.info("shootme: " + request);
                Response response = messageFactory.createResponse(Response.TRYING,
                        request);
                ServerTransaction st = requestEvent.getServerTransaction();

                if (st == null) {
                    st = sipProvider.getNewServerTransaction(request);
                }
                dialog = st.getDialog();

                st.sendResponse(response);

                // reliable provisional response.

                Response okResponse = messageFactory.createResponse(PRACK_CODE,
                        request);
                ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
                this.toTag = "4321";
                toHeader.setTag(toTag); // Application is supposed to set.
                this.inviteTid = st;
                this.inviteRequest = request;

                logger.info("sending reliable provisional response.");

                RequireHeader requireHeader = headerFactory
                        .createRequireHeader("100rel");
                okResponse.addHeader(requireHeader);
                dialog.sendReliableProvisionalResponse(okResponse);
            } catch (Exception ex) {
              ex.printStackTrace();
                TestHarness.fail(ex.getMessage());

            }
        }

        /**
         * Process the bye request.
         */
        public void processBye(RequestEvent requestEvent,
                ServerTransaction serverTransactionId) {
            Request request = requestEvent.getRequest();
            try {
                logger.info("shootme:  got a bye sending OK.");
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                logger.info("Dialog State is "
                        + serverTransactionId.getDialog().getState());

            } catch (Exception ex) {
                TestHarness.fail(ex.getMessage());
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
            logger.info("state = " + transaction.getState());
            logger.info("dialog = " + transaction.getDialog());
            logger.info("dialogState = " + transaction.getDialog().getState());
            logger.info("Transaction Timed out");
            transactionTimedOut = true;
            if(transaction instanceof ServerTransaction) {
                try {
                    Response response = messageFactory.createResponse(Response.SERVER_INTERNAL_ERROR,
                            transaction.getRequest());
                    ServerTransaction st = (ServerTransaction) transaction;

                    if (st == null) {
                        st = sipProvider.getNewServerTransaction(transaction.getRequest());
                    }
                    dialog = st.getDialog();

                    st.sendResponse(response);
                    errorResponseSent = true;
                } catch (Exception ex) {
                    TestHarness.fail(ex.getMessage());
                    System.exit(0);
                }
            }
        }

        public SipProvider createProvider() throws Exception {
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1", myPort,
                    transport);

            sipProvider = sipStack.createSipProvider(lp);
            logger.info(transport + " SIP provider " + sipProvider);

            return sipProvider;
        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.info("IOException");

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            logger.info("Transaction terminated event recieved");

        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("Dialog terminated event recieved");

        }

        public void checkState() {
            TestHarness.assertTrue(transactionTimedOut && errorResponseSent);
        }

    }

    boolean myFlag;


    public void testPrack() {
        this.shootist.sendInvite();

    }
}
