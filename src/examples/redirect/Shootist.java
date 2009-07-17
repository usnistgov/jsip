package examples.redirect;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.util.*;

import junit.framework.TestCase;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author M. Ranganathan
 */

public class Shootist extends TestCase implements SipListener {

    private SipProvider sipProvider;

    private ProtocolObjects protocolObjects;

    private ContactHeader contactHeader;

    private ListeningPoint listeningPoint;

    private ClientTransaction inviteTid;

    private Dialog dialog;

    public static final int myPort = 5080;

    private int peerPort;

    private String peerHostPort;

    private int dialogTerminatedCount;

    private int transctionTerminatedCount;

    private int transactionCount;

    private int dialogCount;

    private boolean byeReceived;

    private boolean redirectReceived;

    private SipURI requestURI;

    private static Logger logger = Logger.getLogger(Shootist.class);



    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod()
                + " received at " + protocolObjects.sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);

    }

    public void processBye(Request request,
            ServerTransaction serverTransactionId) {
        try {
            logger.info("shootist:  got a bye . ServerTxId = " + serverTransactionId);
            this.byeReceived  = true;
            if (serverTransactionId == null) {
                logger.info("shootist:  null TID.");
                return;
            }

            Dialog dialog = serverTransactionId.getDialog();
            assertTrue(dialog == this.dialog);
            logger.info("Dialog State = " + dialog.getState());
            Response response = protocolObjects.messageFactory.createResponse(
                    200, request);
            serverTransactionId.sendResponse(response);
            this.transactionCount++;
            logger.info("shootist:  Sending OK.");
            logger.info("Dialog State = " + dialog.getState());
            assertTrue(serverTransactionId.getState() == TransactionState.COMPLETED);
            assertTrue(dialog.getState() == DialogState.TERMINATED);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
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
                logger.info("response = " + response);
                if (cseq.getMethod().equals(Request.INVITE)) {
                    Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                    logger.info("Sending ACK");
                    dialog.sendAck(ackRequest);
                }
            } else if  (response.getStatusCode() == Response.MOVED_TEMPORARILY) {
                // Dialog dies as soon as you get an error response.
                assertTrue(tid.getDialog().getState() == DialogState.TERMINATED);
                assertTrue(tid.getDialog() == this.dialog);
                this.redirectReceived = true;
                if (cseq.getMethod().equals(Request.INVITE)) {
                    // lookup the contact header
                    ContactHeader contHdr = (ContactHeader) response
                            .getHeader(ContactHeader.NAME);
                    // we can re-use the from header
                    FromHeader from = ((FromHeader) response
                            .getHeader(FromHeader.NAME));
                    // we use the to-address, but without the tag
                    ToHeader to = (ToHeader) (response.getHeader(ToHeader.NAME)).clone();
                    to.removeParameter("tag");
                    // the call-id can be re-used
                    CallIdHeader callID = ((CallIdHeader) response
                            .getHeader(CallIdHeader.NAME));
                    // we take the next cseq
                    long seqNo = (((CSeqHeader) response
                            .getHeader(CSeqHeader.NAME)).getSeqNumber());
                    logger.info("seqNo = " + seqNo);
                    CSeqHeader cseqNew = protocolObjects.headerFactory
                            .createCSeqHeader(++seqNo, "INVITE");
                    // Create ViaHeaders (either use tcp or udp)
                    ArrayList viaHeaders = new ArrayList();
                    ViaHeader viaHeader = protocolObjects.headerFactory
                            .createViaHeader("127.0.0.1", sipProvider
                                    .getListeningPoint(protocolObjects.transport).getPort(),
                                    protocolObjects.transport,
                                    null);
                    // add via headers
                    viaHeaders.add(viaHeader);
                    // create max forwards
                    MaxForwardsHeader maxForwardsHeader = protocolObjects.headerFactory
                            .createMaxForwardsHeader(10);
                    // create invite Request
                    SipURI newUri = (SipURI)this.requestURI.clone();
                    newUri.setParameter("redirection", "true");

                    Request invRequest = protocolObjects.messageFactory
                            .createRequest(newUri,
                                    "INVITE", callID, cseqNew, from, to,
                                    viaHeaders, maxForwardsHeader);
                    // we set the Request URI to the address given
                    SipURI contactURI =
                    protocolObjects.addressFactory.createSipURI(null, this.listeningPoint.getIPAddress());

                    contactURI.setPort(this.listeningPoint.getPort());
                    contactURI.setTransportParam(protocolObjects.transport);

                    Address address = protocolObjects.addressFactory.createAddress(contactURI);
                    ContactHeader contact = protocolObjects.headerFactory.createContactHeader(address);
                    invRequest.addHeader(contact);

                    // the contacat header in the response contains where to redirect
                    // the request to -- which in this case happens to be back to the
                    // same location.
                    ContactHeader chdr = (ContactHeader)response.getHeader(ContactHeader.NAME);

                    SipURI sipUri = (SipURI)chdr.getAddress().getURI();
                    sipUri.setLrParam();
                    RouteHeader routeHeader =
                        protocolObjects.headerFactory.createRouteHeader(chdr.getAddress());
                    invRequest.addHeader(routeHeader);

                    logger.info("Sending INVITE to "
                            + contHdr.getAddress().getURI().toString());
                    inviteTid = sipProvider.getNewClientTransaction(invRequest);
                    this.transactionCount++;

                    logger.info("New TID = " + inviteTid);
                    inviteTid.sendRequest();
                    assertTrue(inviteTid.getState() == TransactionState.CALLING);
                    logger.info("sendReqeust succeeded " + inviteTid);
                    Dialog dialog = inviteTid.getDialog();
                    assertTrue("Stack must allocate a new dialog", dialog != this.dialog);
                    this.dialogCount ++;
                    this.dialog = dialog;


                }
            }
            /**
             * end of modified code
             */
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpeced exception");
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        logger.info("Transaction Time out");
        fail("Unexpected event");
    }



    public SipProvider createProvider() throws Exception {
        logger.info("Shootist: createProvider()");
        listeningPoint = protocolObjects.sipStack.createListeningPoint(
                "127.0.0.1", myPort, protocolObjects.transport);
        this.sipProvider = protocolObjects.sipStack
                .createSipProvider(listeningPoint);
        assertTrue("listening point should be the same as what the provider returns for this transport",
                listeningPoint == sipProvider.getListeningPoint(protocolObjects.transport));
        return sipProvider;

    }

    public void sendInvite() {

        try {
            /**
             * either use udp or tcp
             */

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress = protocolObjects.addressFactory.createSipURI(
                    fromName, fromSipAddress);

            Address fromNameAddress = protocolObjects.addressFactory
                    .createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = protocolObjects.headerFactory
                    .createFromHeader(fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = protocolObjects.addressFactory.createSipURI(
                    toUser, toSipAddress);
            Address toNameAddress = protocolObjects.addressFactory
                    .createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = protocolObjects.headerFactory.createToHeader(
                    toNameAddress, null);

            // create Request URI
            this.requestURI = protocolObjects.addressFactory.createSipURI(
                    toUser, peerHostPort);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = protocolObjects.headerFactory
                    .createViaHeader("127.0.0.1", sipProvider
                            .getListeningPoint(protocolObjects.transport).getPort(), protocolObjects.transport,
                            null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = protocolObjects.headerFactory
                    .createCSeqHeader(1L, Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = protocolObjects.headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = protocolObjects.messageFactory.createRequest(
                    requestURI, Request.INVITE, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = "127.0.0.1";

            SipURI contactUrl = protocolObjects.addressFactory.createSipURI(
                    fromName, host);
            /**
             * either use tcp or udp
             */
            contactUrl.setPort(listeningPoint.getPort());
            contactUrl.setTransportParam(protocolObjects.transport);

            // Create the contact name address.

            Address contactAddress = protocolObjects.addressFactory
                    .createAddress(contactUrl);
            contactUrl.setLrParam();

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = protocolObjects.headerFactory
                    .createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            SipURI uri = protocolObjects.addressFactory.createSipURI(null, "127.0.0.1");

            uri.setLrParam();
            uri.setTransportParam(protocolObjects.transport);
            uri.setPort(this.peerPort);


            Address address = protocolObjects.addressFactory.createAddress(uri);
            RouteHeader routeHeader = protocolObjects.headerFactory.createRouteHeader(address);
            request.addHeader(routeHeader);
            // Add the extension header.
            Header extensionHeader = protocolObjects.headerFactory
                    .createHeader("My-Header", "my header value");
            request.addHeader(extensionHeader);

            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();

            request.setContent(contents, contentTypeHeader);

            extensionHeader = protocolObjects.headerFactory.createHeader(
                    "My-Other-Header", "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = protocolObjects.headerFactory.createHeader(
                    "Call-Info", "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            inviteTid = sipProvider.getNewClientTransaction(request);

            // send the request out.
            inviteTid.sendRequest();

            this.transactionCount ++;

            assertTrue(inviteTid.getState() == TransactionState.CALLING);

            logger.info("client tx = " + inviteTid);
            dialog = inviteTid.getDialog();
            this.dialogCount++;
            assertTrue(dialog != null);
            assertTrue(dialog.getState() == null);

        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
            fail("unexpected exception");
        }
    }

    public Shootist (ProtocolObjects protocolObjects) {
        this.protocolObjects = protocolObjects;
        this.peerPort = Shootme.myPort;
        this.peerHostPort = "127.0.0.1:"+ peerPort;
    }

    public static void main(String args[]) throws Exception {
        ProtocolObjects  protocolObjects = new ProtocolObjects ("shootist",true,"udp","");
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        Shootist shootist = new Shootist(protocolObjects);
        shootist.createProvider();
        shootist.sipProvider.addSipListener(shootist);
        shootist.sendInvite();

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("IOException happened for "
                + exceptionEvent.getHost() + " port = "
                + exceptionEvent.getPort());

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Transaction terminated event recieved for " +
                transactionTerminatedEvent.getClientTransaction());
        this.transctionTerminatedCount++;
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        this.dialogTerminatedCount++;

    }

    public void checkState() {
        assertTrue(dialogTerminatedCount == dialogCount);
        assertTrue(this.byeReceived  && this.redirectReceived );

    }
}
