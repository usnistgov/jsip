package test.unit.gov.nist.javax.sip.stack.dialog.termination;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.msgflow.callflows.ProtocolObjects;

import java.util.*;

/**
 * Concurrent calls test. The client creates 20 concurrent dialogs on the
 * server. The server replies to each one.
 *
 * @author baranowb
 */

public class Shootist implements SipListener {

    private ListeningPoint listeningPoint;
    private ProtocolObjects protocolObjects;
    /* move variables as class variables from init() */
    private SipURI requestURI;

    private CSeqHeader cSeqHeader;

    private FromHeader fromHeader;

    private ToHeader toHeader;

    private MaxForwardsHeader maxForwards;

    private SipProvider sipProvider;

    private Address fromNameAddress;

    private ContentTypeHeader contentTypeHeader;

    private ContactHeader contactHeader;
    // If you want to try TCP transport change the following to
    // String transport = "tcp";
    String transport = "udp";

    private HeaderFactory headerFactory;

    private AddressFactory addressFactory;

    private MessageFactory messageFactory;

    private static String PEER_ADDRESS = Shootme.myAddress;

    private static int PEER_PORT = Shootme.myPort;

    private static String peerHostPort = PEER_ADDRESS + ":" + PEER_PORT;

    // To run on two machines change these to suit.
    public static final String myAddress = "127.0.0.1";

    private static final int myPort = 5060;

    private int responseCodeToINFO = 500;

    private boolean stateIsOk = true;

    private static Logger logger = Logger.getLogger(Shootist.class);

    static {
        if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {

            logger.addAppender(new ConsoleAppender(new SimpleLayout()));

        }
    }

    public Shootist(ProtocolObjects protocolObjects) {
        super();
        this.protocolObjects = protocolObjects;

    }

    public boolean checkState() {

        return stateIsOk;
    }

    public SipProvider createSipProvider() {
        try {
            listeningPoint = protocolObjects.sipStack.createListeningPoint(
                    myAddress, myPort, protocolObjects.transport);

            sipProvider = protocolObjects.sipStack
                    .createSipProvider(listeningPoint);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(ex);
            DialogTerminationOn50XTest
                    .fail("Shootist: unable to create provider");
            return null;
        }
    }

    public void init() {
        SipFactory sipFactory = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        /* remote peer host */
        String peerHostPort = Shootist.peerHostPort;
        String localHost = myAddress;

        try {
            headerFactory = protocolObjects.headerFactory;
            addressFactory = protocolObjects.addressFactory;
            messageFactory = protocolObjects.messageFactory;

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
            contactUrl.setPort(listeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(listeningPoint.getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            DialogTerminationOn50XTest.fail("Shootist: Error on init!", ex);
        }
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {

        System.out.println("Shootist: Dialog Terminated Event "
                + dialogTerminatedEvent.getDialog().getDialogId());
        if ((this.responseCodeToINFO) >= 300) {
            DialogTerminationOn50XTest
                    .fail("Shootist: Got DialogTerinatedEvent, this shouldnt happen");
            stateIsOk = false;
        }

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("An IO Exception occured!");
        DialogTerminationOn50XTest.fail("An IO Exception occured!");

    }

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        System.out.println("GOT REQUEST (we shouldnt get that): "
                + request.getMethod());
        DialogTerminationOn50XTest.fail("Shouldnt receive any request:\n"
                + request);

    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();
        System.out.println("GOT RESPONSE:" + response.getStatusCode());

        try {
            if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {
                Dialog dialog = responseReceivedEvent.getDialog();
                CSeqHeader cseq = (CSeqHeader) response
                        .getHeader(CSeqHeader.NAME);
                Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                System.out.println("Sending ACK");

                dialog.sendAck(ackRequest);
                System.out.println("Dialog Confirmed: dialogID = "
                        + dialog.getDialogId() + " dialogState = "
                        + dialog.getState());
                if (tid == null)
                    System.out.println("null txID");

                Thread.currentThread().sleep(100);
                System.out.println("Sending INFO");
                Request infoRequest = dialog.createRequest(Request.INFO);
                ClientTransaction infoCTX = sipProvider
                        .getNewClientTransaction(infoRequest);
                dialog.sendRequest(infoCTX);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            DialogTerminationOn50XTest.fail(
                    "Shootist: Exception on process respons/send info", ex);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        DialogTerminationOn50XTest.fail("Shootist:Received timeout even!!!");
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // System.out.println("TransactionTerminated event notification");
    }

    void sendInviteRequest() {
        System.out.println("====Send INVITE");
        try {
            cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            int fromTag = 1000 + hashCode();

            fromHeader = headerFactory.createFromHeader(fromNameAddress,
                    new Integer(fromTag).toString());
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            // Create ViaHeaders
            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader(myAddress,
                    listeningPoint.getPort(), transport, null);

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
            System.out.println("inviteTid = " + inviteTid + " sipDialog = "
                    + inviteTid.getDialog());

            // send the request out.
            inviteTid.sendRequest();
        } catch (Exception ex) {
            System.out.println("Fail to sendInviteRequest with SipException:\n"
                    + ex.getMessage());
            DialogTerminationOn50XTest.fail(
                    "Shootist: Failed to send invite: ", ex);

        }
        return;
    }

    public void setResponseCodeToINFO(int responseCodeToINFO) {
        this.responseCodeToINFO = responseCodeToINFO;


    }

}
