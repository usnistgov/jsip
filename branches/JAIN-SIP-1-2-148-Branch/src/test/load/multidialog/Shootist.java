package test.load.multidialog;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import java.text.ParseException;
import java.util.*;

/**
 * Concurrent calls test. The client creates 20 concurrent dialogs on the server.
 * The server replies to each one.
 *
 * @author M. Ranganathan
 */

public class Shootist implements SipListener {

    private static SipProvider tcpProvider;

    private static SipProvider udpProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private ContactHeader contactHeader;

    private ListeningPoint tcpListeningPoint;

    private ListeningPoint udpListeningPoint;

    /* move variables as class variables from init() */
    private SipURI requestURI;

    private CSeqHeader cSeqHeader;

    private FromHeader fromHeader;

    private ToHeader toHeader;


    private MaxForwardsHeader maxForwards;

    private Shootist listener;

    private SipProvider sipProvider;

    private Address fromNameAddress;

    private ContentTypeHeader contentTypeHeader;

    private int terminatedCount;

    private int confirmationCount;
    // If you want to try TCP transport change the following to
    // String transport = "tcp";
    String transport = "udp";


    /* remote peer host */
    static String peerHostPort = "127.0.0.1:5070";

    static String localHost = "127.0.0.1";

    protected static final String usageString = "java "
            + "examples.multi.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);

    }

    public void processBye(Request request,
            ServerTransaction serverTransactionId) {
        try {
            //System.out.println("shootist:  got a bye .");
            if (serverTransactionId == null) {
                //System.out.println("shootist:  null TID.");
                return;
            }
            //System.out.println("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            //System.out.println("shootist:  Sending OK.");
            //System.out.println("Dialog State = " + dialog.getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        //System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        try {
            if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {
                Dialog dialog = responseReceivedEvent.getDialog();
                CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                //System.out.println("Sending ACK");
                dialog.sendAck(ackRequest);
                System.out.println("Dialog Confirmed: Count = " + ++this.confirmationCount+ " dialogID = " +
                        dialog.getDialogId() + " dialogState = " + dialog.getState() );
                if ( tid == null) System.out.println("null txID");

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        //System.out.println("Transaction Time out");
    }

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();


        /* remote peer host */
        String peerHostPort = "127.0.0.1:5070";
        String localHost = "127.0.0.1";

        properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                + transport);

        properties.setProperty("javax.sip.STACK_NAME", "shootist");

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties
                .setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootistdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootistlog.txt");

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                "false");
        // Set to 0 in your production code for max speed.
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
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

            /* udp ListeningPoint */
            udpListeningPoint = sipStack.createListeningPoint(localHost, 5060,
                    "udp");
            udpProvider = sipStack.createSipProvider(udpListeningPoint);
            listener = this;
            udpProvider.addSipListener(listener);

            /* tcp ListeningPoint */
            tcpListeningPoint = sipStack.createListeningPoint(localHost, 5060,
                    "tcp");
            tcpProvider = sipStack.createSipProvider(tcpListeningPoint);
            tcpProvider.addSipListener(listener);

            sipProvider = transport.equalsIgnoreCase("udp") ? udpProvider
                    : tcpProvider;

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            toHeader = headerFactory.createToHeader(toNameAddress, null);

            // create Request URI
            requestURI = addressFactory.createSipURI(toUser, peerHostPort);



            // Create ContentTypeHeader
            contentTypeHeader = headerFactory.createContentTypeHeader(
                    "application", "sdp");

            // Create a new MaxForwardsHeader
            maxForwards = headerFactory.createMaxForwardsHeader(70);

            // Create contact headers
            String host = localHost;

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(tcpListeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint(transport)
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    /* extract something about Request as a seperated function from init() */
    void sendInviteRequest(int cmdSeq) {
        System.out.println("====Send INVITE at times: " + cmdSeq);
        try {
            cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            int fromTag = 1000 + cmdSeq;

            fromHeader = headerFactory.createFromHeader(fromNameAddress,
                    new Integer(fromTag).toString());
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            //Create ViaHeaders
            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader(localHost, sipProvider.getListeningPoint(transport)
                    .getPort(), transport, null);

            // add via headers
            viaHeaders.add(viaHeader);
            Request request = messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);

            request.setHeader(contactHeader);
            request.setContent(sdpData, contentTypeHeader);
            // Create the client transaction.
            ClientTransaction inviteTid = sipProvider
                    .getNewClientTransaction(request);
            System.out.println("inviteTid = " + inviteTid + " sipDialog = " + inviteTid.getDialog());

            // send the request out.
            inviteTid.sendRequest();
        } catch (Exception ex) {
            System.out.println("Fail to sendInviteRequest with SipException:\n"
                    + ex.getMessage());
        }
        return;
    }

    public static void main(String args[]) throws Exception {


        System.out.println("\n***localHost=<" + localHost + ">, peerHostPort=<"
                + peerHostPort + ">.");

        int loops = 500;
        int cmdSeq = 1;

        Shootist shootist = new Shootist();
        shootist.init();

        /* simply send multiple INVITEs, no multi-thread */
        for (int i = 0; i < loops; i++) {
            Thread.sleep(10);
            shootist.sendInviteRequest(cmdSeq++);
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("An IO Exception occured!");
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // System.out.println("TransactionTerminated event notification");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        this.terminatedCount ++;
        System.out.println("DialogTerminatedEvent notification " + this.terminatedCount +
                " dialog ID = "+ dialogTerminatedEvent.getDialog().getDialogId());
    }
}
