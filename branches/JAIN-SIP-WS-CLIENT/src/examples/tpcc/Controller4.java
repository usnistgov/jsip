package examples.tpcc;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.util.*;
import java.io.IOException;
import java.text.ParseException;

/**
 * The Click to dial third party call controller flow IV application.
 *
 * @author Kathleen McCallum
 *
 * <pre>
 *    main () -&gt; init()
 *   init()
 *     createSipStack
 *     createInvite() -&gt; First
 *   processResponse()
 *     if (OK) first
 *       ack() first
 *      createInvite() -&gt; second no SDP
 *     else if (OK) second
 *        ack() -&gt; second
 *        ack() -&gt; first
 * </pre>
 *
 *
 *
 */

public class Controller4 implements SipListener {

    private static SipProvider sipProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private ContactHeader contactHeader;

    private ListeningPoint udpListeningPoint;

    protected ClientTransaction inviteFirst;

    protected ClientTransaction inviteSecond;

    private String currentState;

    String transport = "udp";


    protected static final String usageString = "java "
            + "examples.ctd.ctdControll \n"
            + ">>>> is your class path set to the root?";


    private Dialog firstDialog;

    private Dialog secondDialog;
    private static Logger logger = Logger.getLogger(Controller4.class);

    private String auser = "AGuy";

    private String aSipAddressDomain = "Afirst.com";

    private String aDisplayName = "The A first";

    private String buser = "BGuy";

    private String bSipAddressDomain = "BSecond.com";

    private String bDisplayName = "The B second";

    private String peerHostPortA = "127.0.0.1:5070";

    private String peerHostPortB = "127.0.0.1:5080";

    int first = 0, second = 0;

    private Response secondDialogOK;



    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod() + " received at "
                + sipStack.getStackName() + " with server transaction id "
                + serverTransactionId);

        // We are the Controller so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);

    }

    public void processBye(Request request,
            ServerTransaction serverTransactionId) {

        try {
            logger.info("Controller4:  got a bye .");
            if (serverTransactionId == null) {
                logger.info("Controller4:  null TID.");
                return;
            }

            logger.info("Create OK para BYE: ");
            // 1: OK BYE
            Response ok = messageFactory.createResponse(Response.OK, request);
            serverTransactionId.sendResponse(ok);

            // 2do: BYE for the other side (send a new clientTransaction)
            logger.info("Send BYE in new clientTransaction");

            Dialog secondBye = (Dialog) (serverTransactionId.getDialog()
                    .getApplicationData());
            Request requestBye = secondBye.createRequest(Request.BYE);
            ClientTransaction clientTransaction = null;
            clientTransaction = sipProvider.getNewClientTransaction(requestBye);
            secondBye.sendRequest(clientTransaction);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
        logger.info("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        ClientTransaction tid = responseReceivedEvent.getClientTransaction();
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

        try {
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE)) {
                    if (currentState.equals("first")) {
                        logger.info("processResponse FIRST");
                        // send ACK
                        this.firstDialog = tid.getDialog();
                        Request ackRequest = firstDialog.createAck(cseq
                                .getSeqNumber());
                        logger.info("Sending ACK firtInvite no media");
                        firstDialog.sendAck(ackRequest);

                        // invite second no SDP
                        // get call-id
                        String callId = ((CallIdHeader) response
                                .getHeader(CallIdHeader.NAME)).getCallId();
                        // Create second Invite
                        second++;
                        currentState = "second";
                        Request requestSecond = this.createInvite(currentState,
                                String.valueOf(second), callId, null,
                                peerHostPortB);
                        inviteSecond = sipProvider
                                .getNewClientTransaction(requestSecond);
                        inviteSecond.sendRequest();
                        logger.info("INVITE second sent:\n" + requestSecond);

                    } else if (currentState.equals("second")) {
                        logger.info("processResponse SECOND");
                        // get offer of second
                        byte[] content = response.getRawContent();
                        ContentTypeHeader cth = (ContentTypeHeader) response
                                .getHeader(ContentTypeHeader.NAME);

                        // reinvite First
                        Request reinvite = firstDialog.createRequest(Request.INVITE);
                        reinvite.removeContent();
                        reinvite.setContent(content, cth);

                        // Re-Invte offer2'
                        ClientTransaction ct = sipProvider
                                .getNewClientTransaction(reinvite);
                        firstDialog.sendRequest(ct);
                        this.secondDialog = tid.getDialog();
                        this.secondDialogOK = response;
                        logger.info("RE-INVITE sent:\n" + reinvite);
                        currentState = "re-invite";

                    } else if (currentState.equals("re-invite")) {
                        logger.info("processResponse re-invite");
                        // send ack
                        CSeqHeader cseq2 = (CSeqHeader) this.secondDialogOK.getHeader(CSeqHeader.NAME);
                        Request ackRequest = secondDialog.createAck(cseq2
                                .getSeqNumber());// secondDialog.createRequest(Request.ACK);
                        logger.info("Sending ACK second " + secondDialog);
                        secondDialog.sendAck(ackRequest);// secondDialog.sendAck(ackRequest);

                        Request ackRequestFirst = this.firstDialog.createAck(
                                        cseq.getSeqNumber());
                        ackRequestFirst.setContent(response.getContent(),
                                (ContentTypeHeader) (response
                                        .getHeader("Content-Type")));

                        logger.info("Sending ACK first");
                        firstDialog.sendAck(ackRequestFirst);

                        // save the dialog of the other side, for the bye...
                        firstDialog.setApplicationData(secondDialog);
                        secondDialog.setApplicationData(firstDialog);

                        currentState = "fin";
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public Request createInvite(String headerName, String headerValue,
            String callerId, String tagVal, String peerHostPort)
            throws ParseException, InvalidArgumentException {

        String fromSipAddressDomain = "", toSipAddressDomain = "";
        String fromDisplayName = "";
        String toDisplayName = "";
        String fromVal = "", toVal = "";

        if (headerName.equals("first")) {
            fromVal = auser;
            fromSipAddressDomain = aSipAddressDomain;
            fromDisplayName = aDisplayName;
            toVal = buser;
            toSipAddressDomain = bSipAddressDomain;
            toDisplayName = bDisplayName;
        } else if (headerName.equals("second")) {
            fromVal = buser;
            fromSipAddressDomain = bSipAddressDomain;
            fromDisplayName = bDisplayName;
            toVal = auser;
            toSipAddressDomain = aSipAddressDomain;
            toDisplayName = aDisplayName;
        }
        logger.info("CreateInvite ");

        // create >From Header
        SipURI fromAddress = addressFactory.createSipURI(fromVal,
                fromSipAddressDomain);
        Address fromNameAddress = addressFactory.createAddress(fromAddress);
        fromNameAddress.setDisplayName(fromDisplayName);
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                new Integer((int) (Math.random() * 10000)).toString());

        // create To Header
        SipURI toAddress = addressFactory.createSipURI(toVal,
                toSipAddressDomain);
        Address toNameAddress = addressFactory.createAddress(toAddress);
        toNameAddress.setDisplayName(toDisplayName);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        // create Request URI
        SipURI requestURI = addressFactory.createSipURI(toVal, peerHostPort);

        // Create ViaHeaders
        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                sipProvider.getListeningPoint(transport).getPort(), transport,
                null);
        viaHeaders.add(viaHeader);

        // Create a new CallId header
        CallIdHeader callIdHeader = null;
        if (callerId == null) {
            callIdHeader = sipProvider.getNewCallId();
        } else {
            callIdHeader = headerFactory.createCallIdHeader(callerId);
        }

        // Create a new Cseq header
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(Long
                .parseLong(headerValue), Request.INVITE);

        // Create a new MaxForwardsHeader
        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);

        // Create the request.
        Request request = messageFactory.createRequest(requestURI,
                Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader,
                viaHeaders, maxForwards);
        // Create contact headers
        String host = "127.0.0.1";
        SipURI contactUrl = addressFactory.createSipURI(fromVal, host);
        contactUrl.setPort(udpListeningPoint.getPort());

        // Create the contact name address.
        SipURI contactURI = addressFactory.createSipURI(fromVal, host);
        contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());
        Address contactAddress = addressFactory.createAddress(contactURI);

        // Add the contact address.
        contactAddress.setDisplayName(fromVal);
        contactHeader = headerFactory.createContactHeader(contactAddress);
        request.addHeader(contactHeader);

        // Allow header. With PUBLISH, to indicate that we'd like to have an
        // server-sided PA
        String methods = Request.INVITE + ", " + Request.ACK + ", "
                + Request.OPTIONS + ", " + Request.CANCEL + ", " + Request.BYE
                + ", " + Request.INFO + ", " + Request.REFER + ", "
                + Request.MESSAGE + ", " + Request.NOTIFY + ", "
                + Request.SUBSCRIBE;
        AllowHeader allowHeader = headerFactory.createAllowHeader(methods);
        request.addHeader(allowHeader);

        return request;
    }

    public void init() throws IOException {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // This one is optional so I remove it, since I will call 2 parts
        // properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
        // + transport);
        properties.setProperty("javax.sip.STACK_NAME", "Controller");

        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "controllerdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "controllerlog.txt");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
        properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "true");

        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        logger.addAppender(new FileAppender(new SimpleLayout(),
                "controllerconsolelog.txt"));

        try {
            sipStack = sipFactory.createSipStack(properties);
            logger.info("createSipStack " + sipStack);

            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            udpListeningPoint = sipStack.createListeningPoint("127.0.0.1",
                    5050, "udp");
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            Controller4 listener = this;
            sipProvider.addSipListener(listener);

        } catch (PeerUnavailableException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            logger.info("Creating Listener Points");
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        try {
            logger.info("ProcessCTD ");
            first++;
            this.currentState = "first";
            Request request = this.createInvite(currentState, String
                    .valueOf(first), null, null, peerHostPortA);

            // Create ContentTypeHeader, !no media type!
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "a=rtpmap:0 PCMU/8000\r\n"
                    + "a=rtpmap:4 G723/8000\r\n" + "a=rtpmap:18 G729A/8000\r\n"
                    + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();
            request.setContent(contents, contentTypeHeader);

            // Create the client transaction.
            inviteFirst = sipProvider.getNewClientTransaction(request);
            // send the request out.
            inviteFirst.sendRequest();
            logger.info("INVITE first sent:\n" + request);

        } catch (Exception e) {
            logger.info("Creating call CreateInvite()");
            logger.info(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        new Controller4().init();
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        logger.info("Transaction Time out");
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("IOException happened for " + exceptionEvent.getHost()
                + " port = " + exceptionEvent.getPort());
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Transaction terminated event recieved");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("dialogTerminatedEvent");
    }
}
