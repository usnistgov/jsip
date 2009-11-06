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
package test.unit.gov.nist.javax.sip.stack.challenge;

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.msgflow.callflows.ProtocolObjects;

/**
 * This class is a UAC template.
 *
 * @author M. Ranganathan
 */

public class Shootist implements SipListener {

    private SipProvider provider;

    private ContactHeader contactHeader;

    private ListeningPoint listeningPoint;

    private static String PEER_ADDRESS = Shootme.myAddress;

    private static int PEER_PORT = Shootme.myPort;

    private static String peerHostPort = PEER_ADDRESS + ":" + PEER_PORT;

    // To run on two machines change these to suit.
    public static final String myAddress = "127.0.0.1";

    private static final int myPort = 5060;

    protected ClientTransaction inviteTid;

    private int dialogsEndedEvents;

    private static Logger logger = Logger.getLogger(Shootist.class);

    static {
        if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {

            logger.addAppender(new ConsoleAppender(new SimpleLayout()));

        }
    }

    private ProtocolObjects protocolObjects;

    private Dialog dialog1, dialog2;

    public Shootist(ProtocolObjects protocolObjects) {
        super();
        this.protocolObjects = protocolObjects;
    }

    public SipProvider createSipProvider() {
        try {
            listeningPoint = protocolObjects.sipStack.createListeningPoint(
                    myAddress, myPort, protocolObjects.transport);

            provider = protocolObjects.sipStack
                    .createSipProvider(listeningPoint);
            return provider;
        } catch (Exception ex) {
            logger.error(ex);
            ChallengeTest.fail("unable to create provider");
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
            SipURI fromAddress = protocolObjects.addressFactory.createSipURI(
                    fromName, fromSipAddress);

            Address fromNameAddress = protocolObjects.addressFactory
                    .createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = protocolObjects.headerFactory
                    .createFromHeader(fromNameAddress, new Integer((int) (Math
                            .random() * Integer.MAX_VALUE)).toString());

            // create To Header
            SipURI toAddress = protocolObjects.addressFactory.createSipURI(
                    toUser, toSipAddress);
            Address toNameAddress = protocolObjects.addressFactory
                    .createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = protocolObjects.headerFactory.createToHeader(
                    toNameAddress, null);

            // create Request URI
            SipURI requestURI = protocolObjects.addressFactory.createSipURI(
                    toUser, peerHostPort);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            int port = provider.getListeningPoint(protocolObjects.transport)
                    .getPort();

            ViaHeader viaHeader = protocolObjects.headerFactory
                    .createViaHeader(myAddress, port,
                            protocolObjects.transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = provider.getNewCallId();
            // JvB: Make sure that the implementation matches the messagefactory
            callIdHeader = protocolObjects.headerFactory
                    .createCallIdHeader(callIdHeader.getCallId());

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

            // Create the contact name address.
            SipURI contactURI = protocolObjects.addressFactory.createSipURI(
                    fromName, myAddress);
            contactURI.setPort(provider.getListeningPoint(
                    protocolObjects.transport).getPort());

            Address contactAddress = protocolObjects.addressFactory
                    .createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = protocolObjects.headerFactory
                    .createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // The following is the preferred method to route requests
            // to the peer. Create a route header and set the "lr"
            // parameter for the router header.

            Address address = protocolObjects.addressFactory
                    .createAddress("<sip:" + PEER_ADDRESS + ":" + PEER_PORT
                            + ">");
            // SipUri sipUri = (SipUri) address.getURI();
            // sipUri.setPort(PEER_PORT);

            RouteHeader routeHeader = protocolObjects.headerFactory
                    .createRouteHeader(address);
            ((SipURI) address.getURI()).setLrParam();
            request.addHeader(routeHeader);

            // Create the client transaction.
            this.inviteTid = provider.getNewClientTransaction(request);
            this.dialog1 = this.inviteTid.getDialog();
            // Note that the response may have arrived right away so
            // we cannot check after the message is sent.
            ChallengeTest.assertNull(this.dialog1.getState());

            // send the request out.
            this.inviteTid.sendRequest();

        } catch (Exception ex) {
            logger.error("Unexpected exception", ex);
            ChallengeTest.fail("unexpected exception");
        }
    }


    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod() + " received at "
                + protocolObjects.sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        ChallengeTest.fail( "Unexpected request" );
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        logger.info("Got a response");

        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        logger.info("Response received with client transaction id " + tid
                + ":\n" + response.getStatusCode());
        if (tid == null) {
            logger.info("Stray response -- dropping ");
            return;
        }
        logger.info("transaction state is " + tid.getState());
        logger.info("Dialog = " + tid.getDialog());
        logger.info("Dialog State is " + tid.getDialog().getState());
        SipProvider provider = (SipProvider) responseReceivedEvent.getSource();

        try {

            // JvB: new: process challenge response too
            if (response.getStatusCode() == Response.UNAUTHORIZED
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {

                // Test: Dialog object should be reusable? send new request with
                // updated CSeq
                // and challenge response (not actually added here)
                Request origInvite = (Request) tid.getRequest().clone();
                CSeqHeader cseq = (CSeqHeader) origInvite
                        .getHeader(CSeqHeader.NAME);
                cseq.setSeqNumber(cseq.getSeqNumber() + 1);

                // clear Via branch
                ViaHeader via = (ViaHeader) origInvite
                        .getHeader(ViaHeader.NAME);
                via.removeParameter("branch");

                // Cannot reuse Dialog like this!
                // Dialog dialog = tid.getDialog();
                // dialog.sendRequest(
                // provider.getNewClientTransaction(origInvite) );
                inviteTid = provider.getNewClientTransaction(origInvite);
                ChallengeTest.assertNull( dialog2 );
                this.dialog2 = inviteTid.getDialog(); // new one

                ChallengeTest.assertSame( dialog1, responseReceivedEvent.getDialog() );
                ChallengeTest.assertNotSame( dialog2, responseReceivedEvent.getDialog() );

                inviteTid.sendRequest();

            } else if (response.getStatusCode() == Response.UNAUTHORIZED) {

                // BYE challenge response
                Dialog d = tid.getDialog();
                ChallengeTest.assertNotNull( d );

                // State became terminated as soon as we sent BYE
                ChallengeTest.assertEquals( DialogState.TERMINATED, d.getState() );

                Request bye = d.createRequest( Request.BYE );

                // Normally, would add challenge response header here
                d.sendRequest( provider.getNewClientTransaction(bye) );
            } else if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {

                // Request cancel = inviteTid.createCancel();
                // ClientTransaction ct =
                // sipProvider.getNewClientTransaction(cancel);
                Dialog dialog = tid.getDialog();
                CSeqHeader cseq = (CSeqHeader) response
                        .getHeader(CSeqHeader.NAME);
                Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                logger.info("Ack request to send = " + ackRequest);
                logger.info("Sending ACK");
                dialog.sendAck(ackRequest);

                // Then wait a bit, send BYE
                Thread.sleep( 1000 );

                dialog.sendRequest( provider.getNewClientTransaction( dialog.createRequest(Request.BYE)));

            } else if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.BYE)) {

                logger.info( "Got 200 OK for BYE" );
                ChallengeTest.assertSame( dialog2, responseReceivedEvent.getDialog() );
                ChallengeTest.assertEquals( "Dialog2 terminated", DialogState.TERMINATED, dialog2.getState() );

            }
        } catch (Exception ex) {
            ex.printStackTrace();

            logger.error(ex);
            ChallengeTest.fail("unexpceted exception");
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        logger.info("Transaction Time out");
        logger.info("TimeoutEvent " + timeoutEvent.getTimeout());
    }



    public void checkState() {
        ChallengeTest.assertEquals( "Dialog1 terminated", DialogState.TERMINATED, dialog1.getState() );
        ChallengeTest.assertEquals( "Dialog2 terminated:" + dialog2.getClass(), DialogState.TERMINATED, dialog2.getState() );
        ChallengeTest.assertEquals( "Expect 2 Dialog instances", 2, dialogsEndedEvents );
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
     */
    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("IO Exception!");
        ChallengeTest.fail("Unexpected exception");

    }

    /*
     * (non-Javadoc)
     *
     * @seejavax.sip.SipListener#processTransactionTerminated(javax.sip.
     * TransactionTerminatedEvent)
     */
    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {

        logger.info("Transaction Terminated Event!");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent
     * )
     */
    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("Dialog Terminated Event!");
        ++dialogsEndedEvents;
    }
}
