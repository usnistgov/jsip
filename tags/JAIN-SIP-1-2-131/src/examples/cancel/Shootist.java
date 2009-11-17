package examples.cancel;

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

    private ContactHeader contactHeader;

    private ListeningPoint listeningPoint;

    private static String transport = "udp";

    private static String host = "127.0.0.1";

    private static int port = 5060;

    private static String peerHost = "127.0.0.1";

    private static int peerPort = 5070;

    private ClientTransaction inviteTid;

    private static String unexpectedException = "Unexpected Exception ";

    private Dialog dialog;

    public static boolean sendDelayedCancel = false;

    private boolean cancelSent;

    private boolean cancelOKReceived;

    private boolean byeSent;

    private boolean byeOkReceived;

    private boolean requestTerminated;

    private static Logger logger = Logger.getLogger(Shootist.class);





    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod()
                + " received at " + ProtocolObjects.sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        // We are the UAC so the only request we get is the BYE.
        SipProvider provider = (SipProvider) requestReceivedEvent.getSource();
        if (request.getMethod().equals(Request.BYE))
            processBye(provider, request, serverTransactionId);

    }

    public void processBye(SipProvider provider, Request request,
            ServerTransaction serverTransactionId) {
        try {
            logger.info("shootist:  got a bye .");
            /*
             * if (serverTransactionId == null) { serverTransactionId =
             * provider.getNewServerTransaction(request); }
             */

            Response response = ProtocolObjects.messageFactory.createResponse(
                    200, request);
            provider.sendResponse(response);
            // serverTransactionId.sendResponse(response);
            logger.info("shootist:  Sending OK.");
            if (serverTransactionId != null) {
                // NULL can happen if the bye arrives late.
                Dialog dialog = serverTransactionId.getDialog();
                logger.info("Dialog State = " + dialog.getState());
            }

        } catch (Exception ex) {
            logger.error(unexpectedException,ex);
            fail(unexpectedException);

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
                if (cseq.getMethod().equals(Request.INVITE)) {
                    if (!sendDelayedCancel) fail("Should not see OK for the Invite");
                    // Got the OK for the invite. If Send Cancel was delayed.

                    Request ackRequest = dialog.createAck( cseq.getSeqNumber() );
                    logger.info("Sending ACK");
                    dialog.sendAck(ackRequest);
                    Request byeRequest = dialog.createRequest(Request.BYE);

                    dialog.sendRequest(sipProvider.getNewClientTransaction(byeRequest));
                    this.byeSent = true;
                } else if (cseq.getMethod().equals(Request.CANCEL)) {
                    this.cancelOKReceived = true;
                    if (dialog.getState() == DialogState.CONFIRMED) {
                        // oops cancel went in too late. Need to hang up the
                        // dialog.
                        logger.info("Sending BYE -- cancel went in too late !!");
                        Request byeRequest = dialog.createRequest(Request.BYE);
                        ClientTransaction ct = sipProvider
                                .getNewClientTransaction(byeRequest);
                        dialog.sendRequest(ct);
                        assertTrue(dialog.getState().equals(DialogState.TERMINATED));

                    }
                } else if (cseq.getMethod().equals(Request.BYE)) {
                    this.byeOkReceived = true;
                }
            } else if (response.getStatusCode() == Response.RINGING) {
                // Cancel the invite.
                if (!cancelSent) {
                    if (! sendDelayedCancel)
                        sendCancel();

                }
            }  else if (response.getStatusCode() == Response.REQUEST_TERMINATED) {
                assertTrue(cseq.getMethod().equals(Request.INVITE));
                this.requestTerminated = true;
            }

        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            fail(unexpectedException);

        }

    }

    public void checkState() {
        if ( sendDelayedCancel )
            assertTrue( byeSent && byeOkReceived  ) ;
        else
            assertTrue(cancelSent && cancelOKReceived && requestTerminated) ;


    }


    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        logger.info("Transaction Time out");
        fail("unexpected timeout");
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
            listeningPoint = ProtocolObjects.sipStack.createListeningPoint(
                    host, port, transport);

            sipProvider = ProtocolObjects.sipStack
                    .createSipProvider(listeningPoint);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            fail(unexpectedException);
            return null;
        }

    }

    public void sendInvite() {
        try {

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress = ProtocolObjects.addressFactory.createSipURI(
                    fromName, fromSipAddress);

            Address fromNameAddress = ProtocolObjects.addressFactory
                    .createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = ProtocolObjects.headerFactory
                    .createFromHeader(fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = ProtocolObjects.addressFactory.createSipURI(
                    toUser, toSipAddress);
            Address toNameAddress = ProtocolObjects.addressFactory
                    .createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = ProtocolObjects.headerFactory.createToHeader(
                    toNameAddress, null);

            // create Request URI
            String peerHostPort = peerHost + ":" + peerPort;
            SipURI requestURI = ProtocolObjects.addressFactory.createSipURI(
                    toUser, peerHostPort);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = ProtocolObjects.headerFactory
                    .createViaHeader(host, sipProvider.getListeningPoint(
                            transport).getPort(), transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            SipURI sipuri = ProtocolObjects.addressFactory.createSipURI(null,
                    host);
            sipuri.setPort(peerPort);
            sipuri.setLrParam();

            RouteHeader routeHeader = ProtocolObjects.headerFactory
                    .createRouteHeader(ProtocolObjects.addressFactory
                            .createAddress(sipuri));

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = ProtocolObjects.headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = ProtocolObjects.headerFactory
                    .createCSeqHeader(1L, Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = ProtocolObjects.headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = ProtocolObjects.messageFactory.createRequest(
                    requestURI, Request.INVITE, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwards);
            // Create contact headers

            SipURI contactUrl = ProtocolObjects.addressFactory.createSipURI(
                    fromName, host);
            contactUrl.setPort(listeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = ProtocolObjects.addressFactory.createSipURI(
                    fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint("udp").getPort());

            Address contactAddress = ProtocolObjects.addressFactory
                    .createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = ProtocolObjects.headerFactory
                    .createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Dont use the Outbound Proxy. Use Lr instead.
            request.setHeader(routeHeader);

            // Add the extension header.
            Header extensionHeader = ProtocolObjects.headerFactory
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

            extensionHeader = ProtocolObjects.headerFactory.createHeader(
                    "My-Other-Header", "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = ProtocolObjects.headerFactory.createHeader(
                    "Call-Info", "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            inviteTid = sipProvider.getNewClientTransaction(request);

            // send the request out.
            inviteTid.sendRequest();

            dialog = inviteTid.getDialog();

        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            fail(unexpectedException);

        }
    }

    public static void main(String args[]) throws Exception {

        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        ProtocolObjects.init("shootist");
        Shootist shootist = new Shootist();
        shootist.createSipProvider();
        shootist.sipProvider.addSipListener(shootist);
        shootist.sendInvite();

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("Got an IO Exception");
        fail("unexpected event");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Got a transaction terminated event");

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("Got a dialog terminated event");
        this.checkState();

    }




}
