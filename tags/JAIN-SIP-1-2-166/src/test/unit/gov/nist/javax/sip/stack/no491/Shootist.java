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
package test.unit.gov.nist.javax.sip.stack.no491;

import gov.nist.javax.sip.DialogExt;
import gov.nist.javax.sip.address.SipUri;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.TestHarness;
import test.tck.msgflow.callflows.ProtocolObjects;

import java.util.*;

import junit.framework.TestCase;

/**
 * This class is a UAC template.
 * 
 * @author M. Ranganathan
 */

public class Shootist implements SipListener {

    private SipProvider provider;

    private int reInviteCount;

    private ContactHeader contactHeader;

    private ListeningPoint listeningPoint;

    private int counter;

    private static String PEER_ADDRESS = Shootme.myAddress;

    private static int PEER_PORT = Shootme.myPort;

    private static String peerHostPort = PEER_ADDRESS + ":" + PEER_PORT;

    // To run on two machines change these to suit.
    public static final String myAddress = "127.0.0.1";

    private static final int myPort = 5060;

    protected ClientTransaction inviteTid;

    private boolean okReceived;

    private boolean byeOkRecieved;

    private boolean byeSent;

    int reInviteReceivedCount;

    private static Logger logger = Logger.getLogger(Shootist.class);

    static {
        if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {

            logger.addAppender(new ConsoleAppender(new SimpleLayout()));

        }
    }

    private ProtocolObjects protocolObjects;

    private Dialog dialog;

    private boolean requestPendingSeen;

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

        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);
        else if (request.getMethod().equals(Request.INVITE))
            processInvite(request, serverTransactionId);
        else if (request.getMethod().equals(Request.ACK))
            processAck(request, serverTransactionId);

    }

    public void processInvite(Request request, ServerTransaction st) {
        try {
            this.reInviteReceivedCount++;
            Dialog dialog = st.getDialog();
            Response response = protocolObjects.messageFactory.createResponse(Response.OK,
                    request);
            ((ToHeader) response.getHeader(ToHeader.NAME)).setTag(((ToHeader) request
                    .getHeader(ToHeader.NAME)).getTag());

            Address address = protocolObjects.addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
            ContactHeader contactHeader = protocolObjects.headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            st.sendResponse(response);
            ReInviteTest.assertEquals("Dialog for reinvite must match original dialog", dialog,
                    this.dialog);
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            ReInviteTest.fail("unexpected exception");
        }
    }

    public void processAck(Request request, ServerTransaction tid) {
        try {
            logger.info("Got an ACK! sending bye : " + tid);
            if (tid != null) {
                Dialog dialog = tid.getDialog();
                ReInviteTest.assertSame("dialog id mismatch", dialog, this.dialog);
                Request bye = dialog.createRequest(Request.BYE);
                MaxForwardsHeader mf = protocolObjects.headerFactory.createMaxForwardsHeader(10);
                bye.addHeader(mf);
                ClientTransaction ct = provider.getNewClientTransaction(bye);
                dialog.sendRequest(ct);
                this.byeSent = true;
            }
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            ReInviteTest.fail("unexpected exception");

        }
    }

    public void processBye(Request request, ServerTransaction serverTransactionId) {
        try {
            logger.info("shootist:  got a bye .");
            if (serverTransactionId == null) {
                logger.info("shootist:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            ReInviteTest.assertSame("dialog mismatch", dialog, this.dialog);
            logger.info("Dialog State = " + dialog.getState());
            Response response = protocolObjects.messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            logger.info("shootist:  Sending OK.");
            logger.info("Dialog State = " + dialog.getState());
            ReInviteTest.assertEquals("Should be terminated", dialog.getState(),
                    DialogState.TERMINATED);

        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            ReInviteTest.fail("unexpected exception");

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
            if (response.getStatusCode() == Response.REQUEST_PENDING) {
                ReInviteTest.fail("Should not see a REQUEST PENDING");
                return;
            }
            if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod().equals(
                            Request.INVITE)) {

                Dialog dialog = tid.getDialog();

                reInviteCount++;
                if(reInviteCount < 25) {
                    Request inviteRequest = dialog.createRequest(Request.INVITE);
                    ((SipURI) inviteRequest.getRequestURI()).removeParameter("transport");
                    ((ViaHeader) inviteRequest.getHeader(ViaHeader.NAME)).setTransport(protocolObjects.transport);
                    inviteRequest.setHeader(contactHeader);
                    MaxForwardsHeader mf = protocolObjects.headerFactory.createMaxForwardsHeader(10);
                    inviteRequest.addHeader(mf);
    
                    ClientTransaction ct = provider.getNewClientTransaction(inviteRequest);
                    // This will block until the ACK is seen.
                    dialog.sendRequest(ct);
                }
                
                // Deliberately send the ACK out of sequence.
                Request ackRequest = dialog.createAck(((CSeqHeader)response.getHeader(CSeqHeader.NAME)).getSeqNumber());

                dialog.sendAck(ackRequest);
            } else if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod().equals(
                            Request.BYE)) {
                this.byeOkRecieved = true;
            } 
        } catch (Exception ex) {
            ex.printStackTrace();

            logger.error(ex);
            ReInviteTest.fail("unexpceted exception");
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
            ReInviteTest.fail("unable to create provider");
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

            Address fromNameAddress = protocolObjects.addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = protocolObjects.headerFactory.createFromHeader(
                    fromNameAddress, new Integer((int) (Math.random() * Integer.MAX_VALUE))
                            .toString());

            // create To Header
            SipURI toAddress = protocolObjects.addressFactory.createSipURI(toUser, toSipAddress);
            Address toNameAddress = protocolObjects.addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = protocolObjects.headerFactory.createToHeader(toNameAddress, null);

            // create Request URI
            SipURI requestURI = protocolObjects.addressFactory.createSipURI(toUser, peerHostPort);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            int port = provider.getListeningPoint(protocolObjects.transport).getPort();

            ViaHeader viaHeader = protocolObjects.headerFactory.createViaHeader(myAddress, port,
                    protocolObjects.transport, null);

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
            CSeqHeader cSeqHeader = protocolObjects.headerFactory.createCSeqHeader(1234L,
                    Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = protocolObjects.headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = protocolObjects.messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders,
                    maxForwards);
            // Create contact headers

            // Create the contact name address.
            SipURI contactURI = protocolObjects.addressFactory.createSipURI(fromName, myAddress);
            contactURI.setPort(provider.getListeningPoint(protocolObjects.transport).getPort());

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

            Address address = protocolObjects.addressFactory.createAddress("<sip:" + PEER_ADDRESS
                    + ":" + PEER_PORT + ">");
            // SipUri sipUri = (SipUri) address.getURI();
            // sipUri.setPort(PEER_PORT);

            RouteHeader routeHeader = protocolObjects.headerFactory.createRouteHeader(address);
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
            ReInviteTest.assertTrue(this.dialog.getState() == null);

            // send the request out.
            this.inviteTid.sendRequest();

        } catch (Exception ex) {
            logger.error("Unexpected exception", ex);
            ReInviteTest.fail("unexpected exception");
        }
    }

    public void checkState() {
        ReInviteTest.assertTrue("Expecting a requestPending", !this.requestPendingSeen);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
     */
    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("IO Exception!");
        ReInviteTest.fail("Unexpected exception");

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
     */
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

        logger.info("Transaction Terminated Event!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
     */
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("Dialog Terminated Event!");

    }
}
