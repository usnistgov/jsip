package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
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
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import test.tck.msgflow.callflows.ScenarioHarness;

public class CancelEventTest extends  ScenarioHarness {

    private static String transport = "udp";
    private static String unexpectedException = "Unexpected Exception ";

    private static String host = "127.0.0.1";

    private static int port = 6050;

    private static int peerPort = 6060;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        if (!logger.isAttached(console))
            logger.addAppender(console);
    }

    public CancelEventTest() {
        super("CanceEventTest",true);
    }


    class Shootist implements SipListener {

        private SipProvider sipProvider;

        private ContactHeader contactHeader;

        private ListeningPoint listeningPoint;

        private String peerHost = "127.0.0.1";

        private ClientTransaction inviteTid;

        private Dialog dialog;

        private boolean cancelOk = false;
        private boolean cancelSent = false;
        private boolean cancelTxTerm = false;
        private boolean inviteTxTerm = false;
        private boolean dialogTerminated = false;



        AddressFactory addressFactory;

        MessageFactory messageFactory;

        HeaderFactory headerFactory;

        SipStack sipStack;

        int logLevel = 32;

        String logFileDirectory = "logs/";

        Shootist() {
            SipFactory sipFactory = null;

            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            // If you want to try TCP transport change the following to

            // If you want to use UDP then uncomment this.
            String stackname = "shootist";
            properties.setProperty("javax.sip.STACK_NAME", stackname);

            // The following properties are specific to nist-sip
            // and are not necessarily part of any other jain-sip
            // implementation.
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    logFileDirectory + this.getClass().getName()  + ".debug.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    logFileDirectory + stackname + "log.txt");

            // Set to 0 in your production code for max speed.
            // You need 16 for logging traces. 32 for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
                    new Integer(logLevel).toString());

            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	logger.info("\nNIO Enabled\n");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            
            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);

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

        public void destroy() {
            sipStack.stop();
        }

        public void start() throws Exception {
            sipStack.start();

        }

        public void processRequest(RequestEvent requestReceivedEvent) {
            Request request = requestReceivedEvent.getRequest();
            ServerTransaction serverTransactionId = requestReceivedEvent
                    .getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod() + " received at "
                    + sipStack.getStackName() + " with server transaction id "
                    + serverTransactionId);

        }

        public void processResponse(ResponseEvent responseReceivedEvent) {
            Response response = (Response) responseReceivedEvent.getResponse();
            logger.info("Got a response"
                    + ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod());

            ClientTransaction tid = responseReceivedEvent
                    .getClientTransaction();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            logger.info("Response received : Status Code = "
                    + response.getStatusCode() + " " + cseq);
            if (tid == null) {
                logger.info("Stray response -- dropping ");
                return;
            }
            logger.info("transaction state is " + tid.getState());
            logger.info("Dialog = " + tid.getDialog());
            logger.info("Dialog State is " + tid.getDialog().getState());

            if (dialog == null) {
                logger.info("SETTING DIALOG SINCE IT WAS NULL!!!!!!");
                dialog = tid.getDialog();
            }

            if (response.getStatusCode() == 180) {
                sendCancel();
            } else if (response.getStatusCode() == 200)// more checks?
            {
                cancelOk = true;
            } else {
                logger.info("Got weird response:" + response);
            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
            Transaction transaction = null;
            if (timeoutEvent.isServerTransaction()) {
                transaction = timeoutEvent.getServerTransaction();
            } else {
                transaction = timeoutEvent.getClientTransaction();
            }
            logger.info("state = " + transaction.getState());
            logger.info("dialog = " + transaction.getDialog());
            logger.info("dialogState = " + transaction.getDialog().getState());
            logger.info("Transaction Time out");
            fail("Timeout Shouldnt happen on UAC side!!!");
        }

        private void sendCancel() {
            try {
                logger.info("Sending cancel");

                Request cancelRequest = inviteTid.createCancel();
                ClientTransaction cancelTid = sipProvider
                        .getNewClientTransaction(cancelRequest);
                cancelTid.sendRequest();
                cancelSent = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(unexpectedException, ex);
                fail(unexpectedException);
            }
        }

        public SipProvider createSipProvider() {
            try {
                listeningPoint = sipStack.createListeningPoint(host, port,
                        transport);

                sipProvider = sipStack.createSipProvider(listeningPoint);
                return sipProvider;
            } catch (Exception ex) {
                logger.error(unexpectedException, ex);
                fail(unexpectedException);
                return null;
            }

        }

        public void sendInvite() {

            String fromName = "BigGuy";
            // String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            // String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";
            String localAddress = sipProvider.getListeningPoint("udp")
                    .getIPAddress();
            int localPort = sipProvider.getListeningPoint("udp").getPort();
            String localTransport = sipProvider.getListeningPoint("udp")
                    .getTransport();

            ContactHeader contactHeader = null;
            ToHeader toHeader = null;
            FromHeader fromHeader = null;
            CSeqHeader cseqHeader = null;
            ViaHeader viaHeader = null;
            CallIdHeader callIdHeader = null;
            MaxForwardsHeader maxForwardsHeader = null;
            ContentTypeHeader contentTypeHeader = null;
            RouteHeader routeHeader = null;
            // LETS CREATEOUR HEADERS

            try {
                cseqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);
                viaHeader = headerFactory.createViaHeader(localAddress,
                        localPort, localTransport, null);
                Address fromAddres = addressFactory
                        .createAddress("sip:SimpleSIPPing@" + localAddress
                                + ":" + localPort);
                // Address
                // toAddress=addressFactory.createAddress("sip:pingReceiver@"+peerAddres+":"+peerPort);
                Address toAddress = addressFactory.createAddress("sip:"
                        + this.peerHost + ":" + peerPort);

                contactHeader = headerFactory.createContactHeader(fromAddres);
                toHeader = headerFactory.createToHeader(toAddress, null);
                fromHeader = headerFactory.createFromHeader(fromAddres, Integer
                        .toString(new Random().nextInt()));
                callIdHeader = sipProvider.getNewCallId();
                maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
                contentTypeHeader = headerFactory.createContentTypeHeader(
                        "text", "plain");
                Address routeAddress = addressFactory.createAddress("sip:"
                        + this.peerHost + ":" + peerPort);
                routeHeader = headerFactory.createRouteHeader(routeAddress);

                // LETS CREATE OUR REQUEST AND
                ArrayList list = new ArrayList();
                list.add(viaHeader);
                URI requestURI = null;
                Request request = null;
                Request cancel = null;
                Request inviteRequest = null;

                requestURI = addressFactory.createURI("sip:" + localAddress);
                inviteRequest = request = messageFactory.createRequest(
                        requestURI, Request.INVITE, callIdHeader, cseqHeader,
                        fromHeader, toHeader, list, maxForwardsHeader,
                        contentTypeHeader, "CANCEL".getBytes());
                request.addHeader(routeHeader);
                request.addHeader(contactHeader);

                // ClientTransaction CTInvite = null;
                // ClientTransaction CTCancel = null;

                inviteTid = sipProvider.getNewClientTransaction(request);

                inviteTid.sendRequest();
                dialog = inviteTid.getDialog();
                logger.info("SET DIALOG TO[" + dialog + "]");
            } catch (Exception e) {

                logger.error("Unexpected exception", e);
                fail("Unexpected exception");

            }

        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.info("Got an IO Exception");
            fail("unexpected event");

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            if (!transactionTerminatedEvent.isServerTransaction()) {
                ClientTransaction clientTx = transactionTerminatedEvent
                        .getClientTransaction();

                String method = clientTx.getRequest().getMethod();

                logger.info("Server Tx : " + method + " terminated ");
                if (method.equals("INVITE")) {
                    inviteTxTerm = true;
                } else if (method.equals("CANCEL")) {
                    cancelTxTerm = true;
                } else {
                    fail("Unexpected transaction has ended!!![" + method + "]");
                }
            }
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("Got a dialog terminated event");
            if (dialog == dialogTerminatedEvent.getDialog()) {
                logger.info("Dialog matches dialog created before");
                dialogTerminated = true;
            }

        }

        public boolean conditionMet() {
            System.out.println("cancelOK = " + cancelOk);
            System.out.println("cancelTerm = " + cancelTxTerm);
            System.out.println("inviteTxTerm = " + inviteTxTerm);
            System.out.println("dialogTerminated = " + dialogTerminated);

            return cancelOk && cancelTxTerm && inviteTxTerm && dialogTerminated;
        }

        public String[] conditionsState() {
            String[] result = new String[5];
            result[0] = "[" + cancelOk + "] - OK received for CANCEL Req";
            result[1] = "[" + cancelTxTerm + "] - CANCEL STX terminated";
            result[2] = "[" + inviteTxTerm + "] - INVITE STX terminated";
            // echhh
            String state = null;
            if (dialog == null)
                state = "DIALOG IS NULL";
            else
                state = dialog.getState().toString();
            result[3] = "[" + dialogTerminated + "] - Dialog terminated state["
                    + state + "]";
            result[4] = "[" + cancelSent + "] - CANCEL sent";
            return result;
        }



    }

    class Shootme  implements SipListener {

        private static final String myAddress = "127.0.0.1";

        private int myPort = peerPort;

        private ServerTransaction inviteTid;

        private SipProvider sipProvider;

        private Dialog dialog;

        private boolean cancelOk = false;
        private boolean cancelTxTerm = false;
        private boolean inviteTxTerm = false;
        private boolean dialogTerminated = false;
        private int dteCount = 0;

        private static final String unexpectedException = "Unexpected Exception ";

        SipStack sipStack;

        int logLevel = 32;

        String logFileDirectory = "logs/";

        AddressFactory addressFactory;

        MessageFactory messageFactory;

        HeaderFactory headerFactory;

		private boolean dialogOnCancelTx = true;

        Shootme () {
            SipFactory sipFactory = null;
            String stackname = "shootme";

            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            // If you want to try TCP transport change the following to

            // If you want to use UDP then uncomment this.
            properties.setProperty("javax.sip.STACK_NAME", stackname);

            // The following properties are specific to nist-sip
            // and are not necessarily part of any other jain-sip
            // implementation.
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    logFileDirectory + this.getClass().getName() + ".debug.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    logFileDirectory + stackname + "log.txt");

            // Set to 0 in your production code for max speed.
            // You need 16 for logging traces. 32 for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
                    new Integer(logLevel).toString());

            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	logger.info("\nNIO Enabled\n");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            
            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);

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

        public void destroy() {
            sipStack.stop();
        }

        public void start() throws Exception {
            sipStack.start();

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

            } else if (request.getMethod().equals(Request.CANCEL)) {
                processCancel(requestEvent, serverTransactionId);
            }

        }

        public void processResponse(ResponseEvent responseEvent) {
        }

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            try {
                logger.info("shootme: got an Invite sending RINGING");
                // logger.info("shootme: " + request);
                Response response = messageFactory.createResponse(180, request);
                ToHeader toHeader = (ToHeader) response
                        .getHeader(ToHeader.NAME);
                toHeader.setTag("4321"); // Application is supposed to set.
                Address address = addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);
                ServerTransaction st = requestEvent.getServerTransaction();

                if (st == null) {
                    st = sipProvider.getNewServerTransaction(request);
                    logger.info("Created a new server transaction for "
                            + request.getMethod() + " serverTransaction = "
                            + st);
                }
                inviteTid = st;

                dialog = st.getDialog();

                st.sendResponse(response);

            } catch (Exception ex) {
                logger.error(ex);
                fail(unexpectedException);
            }
        }

        public void processCancel(RequestEvent requestEvent,
                ServerTransaction serverTransactionId) {

            Request request = requestEvent.getRequest();
            try {
                logger.info("shootme:  got a cancel.");
                if (serverTransactionId == null) {
                    logger.info("shootme:  null tid.");
                    return;
                }
                TestCase.assertTrue(inviteTid != serverTransactionId);
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                Request inviteRequest = inviteTid.getRequest();
                if (inviteTid.getState() != TransactionState.TERMINATED) {
                    response = messageFactory.createResponse(
                            Response.REQUEST_TERMINATED, inviteRequest);
                    inviteTid.sendResponse(response);
                }
                cancelOk = true;
                if(serverTransactionId.getDialog() == null) {
                	dialogOnCancelTx = false;
                }
            } catch (Exception ex) {
                // logger.error(ex);
                ex.printStackTrace();
                fail(unexpectedException);

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
            fail("Timeout Shouldnt happen on UAS side!!!");
        }

        public SipProvider createProvider() {
            try {

                ListeningPoint lp = sipStack.createListeningPoint(myAddress,
                        myPort, transport);

                sipProvider = sipStack.createSipProvider(lp);
                logger.info("udp provider " + sipProvider);
                return sipProvider;
            } catch (Exception ex) {
                logger.error(ex);
                fail(unexpectedException);
                return null;

            }

        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            // TODO Auto-generated method stub

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            if (transactionTerminatedEvent.isServerTransaction()) {
                ServerTransaction serverTx = transactionTerminatedEvent
                        .getServerTransaction();

                String method = serverTx.getRequest().getMethod();

                logger.info("Server Tx : " + method + " terminated ");
                if (method.equals("INVITE")) {
                    inviteTxTerm = true;
                } else if (method.equals("CANCEL")) {
                    cancelTxTerm = true;
                } else {
                    fail("Unexpected transaction has ended!!![" + method + "]");
                }
            }
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {

            dialogTerminated = true;
            dteCount++;
        }

        public boolean conditionMet() {
             System.out.println("cancelOK = " + cancelOk);
             System.out.println("cancelTerm = " + cancelTxTerm);
             System.out.println("inviteTxTerm = " + inviteTxTerm);
             System.out.println("dialogTerminated = " + dialogTerminated);
             System.out.println("dialogOnCancelTx = " + dialogOnCancelTx);

             return cancelOk && cancelTxTerm && inviteTxTerm && dialogTerminated && dialogOnCancelTx;
        }

        public String[] conditionsState() {
            String[] result = new String[4];
            result[0] = "[" + cancelOk + "] - OK sent for CANCEL Req";
            result[1] = "[" + cancelTxTerm + "] - CANCEL STX terminated";
            result[2] = "[" + inviteTxTerm + "] - INVITE STX terminated";
            // echhh
            String state = null;
            if (dialog == null)
                state = "DIALOG IS NULL";
            else
                state = dialog.getState().toString();
            result[3] = "[" + dialogTerminated + "] - Dialog terminated state["
                    + state + "] count [" + dteCount + "]";
            return result;
        }

    }

    Shootist shootist;
    Shootme shootme;

    public void setUp() throws Exception {


        shootist = new Shootist();
        shootist.createSipProvider();
        shootist.sipProvider.addSipListener(shootist);
        shootme = new Shootme();
        shootme.createProvider();
        shootme.sipProvider.addSipListener(shootme);



    }

    public void testCancelEvent() throws Exception {
        shootist.sendInvite();
        Thread.sleep(40000);
        assertTrue ( shootist.conditionMet());
        assertTrue ( shootme.conditionMet());
    }

    public void tearDown() {
        shootist.destroy();
        shootme.destroy();
    }

}
