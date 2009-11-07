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
package test.tck.msgflow.callflows.subsnotify;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import test.tck.TestHarness;
import test.tck.msgflow.callflows.ProtocolObjects;

import java.util.*;

/**
 * This class is a Subscriber template.
 * The subscriber handles upstream forking by a proxy server. The stack is set up to
 * expect forking of this event type. The test checks that exactly two distinct dialogs
 * are created.
 *
 * This code is released to  domain.
 *
 * @author M. Ranganathan
 */

public class Subscriber implements SipListener {

    private SipProvider sipProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private ContactHeader contactHeader;

    private static String transport;

    private int count;

    private HashSet dialogs;

    private static Logger logger = Logger.getLogger(Subscriber.class);

    static {
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(new FileAppender(new SimpleLayout(),
                    "logs/subscriberoutputlog.txt"));
        } catch (Exception ex) {
            logger.info(ex.getMessage(), ex);
            TestHarness.fail("Failed to initialize Subscriber, because of " + ex.getMessage());
        }
    }

    private ClientTransaction subscribeTid;

    private ListeningPoint listeningPoint;

    private int port;

    private Dialog subscriberDialog;

    private Dialog forkedDialog;

    public Subscriber(ProtocolObjects protObjects) {
        addressFactory = protObjects.addressFactory;
        messageFactory = protObjects.messageFactory;
        headerFactory = protObjects.headerFactory;
        sipStack = protObjects.sipStack;
        transport = protObjects.transport;
        this.dialogs = new HashSet();
    }

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();
        String viaBranch = ((ViaHeader)(request.getHeaders(ViaHeader.NAME).next())).getParameter("branch");

        logger.info("\n\nRequest " + request.getMethod() + " received at "
                + sipStack.getStackName() + " with server transaction id "
                + serverTransactionId +
                " branch ID = " + viaBranch);

        if (request.getMethod().equals(Request.NOTIFY))
            processNotify(requestReceivedEvent, serverTransactionId);

    }

    public void processNotify(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider provider = (SipProvider) requestEvent.getSource();
        AbstractSubsnotifyTestCase.assertTrue("provider must be the same as my provider ", provider == this.sipProvider);
        Request notify = requestEvent.getRequest();
        try {
            logger.info("subscriber:  got a notify count  " + this.count++ );
            if (serverTransactionId == null) {
                logger.info("subscriber:  null TID.");
                serverTransactionId = provider.getNewServerTransaction(notify);
            }
            Dialog dialog = serverTransactionId.getDialog();
            AbstractSubsnotifyTestCase.assertTrue("subscriberDialog", subscriberDialog != null);
            AbstractSubsnotifyTestCase.assertTrue("Dialog should not be null", dialog != null);
            if ( dialog != subscriberDialog ) {
                if (forkedDialog == null) {
                    forkedDialog = dialog;
                } else  {
                    AbstractSubsnotifyTestCase.assertTrue("Dialog should be either the subscriber dialog ",
                            forkedDialog  == dialog);
                }
            }

            this.dialogs.add(dialog);
            logger.info("Dialog State = " + dialog.getState());

            AbstractSubsnotifyTestCase.assertTrue("Dialog state should be confirmed ", dialog.getState() == DialogState.CONFIRMED);

            Response response = messageFactory.createResponse(200, notify);
            // SHOULD add a Contact
            ContactHeader contact = (ContactHeader) contactHeader.clone();
            ((SipURI)contact.getAddress().getURI()).setParameter( "id", "sub" );
            response.addHeader( contact );
            logger.info("Transaction State = " + serverTransactionId.getState());
            AbstractSubsnotifyTestCase.assertTrue("transaction state should be trying",
                    serverTransactionId.getState() == TransactionState.TRYING);
            serverTransactionId.sendResponse(response);
            logger.info("Dialog State = " + dialog.getState());
            SubscriptionStateHeader subscriptionState = (SubscriptionStateHeader) notify
                    .getHeader(SubscriptionStateHeader.NAME);

            // Subscription is terminated?
            String state = subscriptionState.getState();
            if (state.equalsIgnoreCase(SubscriptionStateHeader.TERMINATED)) {
                dialog.delete();
            } else if (state.equalsIgnoreCase(SubscriptionStateHeader.ACTIVE)) {
                logger.info("Subscriber: sending unSUBSCRIBE");

                // Else we end it ourselves
                Request unsubscribe = dialog.createRequest(Request.SUBSCRIBE);

                logger.info( "dialog created:" + unsubscribe );
                // SHOULD add a Contact (done by dialog), lets mark it to test updates
                ((SipURI) dialog.getLocalParty().getURI()).setParameter( "id", "unsub" );
                ExpiresHeader expires = headerFactory.createExpiresHeader(0);
                unsubscribe.addHeader(expires);
                // JvB note : stack should do this!
                unsubscribe.addHeader(notify.getHeader(EventHeader.NAME)); // copy
                                            // event
                                            // header
                logger.info("Sending Unsubscribe : " + unsubscribe);
                logger.info("unsubscribe dialog  " + dialog);
                ClientTransaction ct = sipProvider.getNewClientTransaction(unsubscribe);
                AbstractSubsnotifyTestCase.assertTrue( "Dialog mismatch " + ct.getDialog() + " dialog " + dialog, ct.getDialog() == dialog );

                dialog.sendRequest(ct);


            } else {
                logger.info("Subscriber: state now " + state);// pending
                // usually
                AbstractSubsnotifyTestCase.assertTrue("State should be pending was "
                        + state, state.equalsIgnoreCase(SubscriptionStateHeader.PENDING ));

            }

        } catch (Exception ex) {
            logger.error("Unexpected exception",ex);
            TestHarness.fail("Failed to process Notify, because of " + ex.getMessage());



        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        logger.info("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        logger.info("Response received with client transaction id " + tid
                + ":\n" + response.getStatusCode()  );
        if (tid == null) {
            logger.warn("Stray response -- dropping ");
            return;
        }
        logger.info("transaction state is " + tid.getState());
        logger.info("Dialog = " + tid.getDialog());
        if ( tid.getDialog () != null )
            logger.info("Dialog State is " + tid.getDialog().getState());

        if ( tid.getDialog() != null ) this.dialogs.add(tid.getDialog());

    }

    public SipProvider createProvider(int newPort) {
        try {
            port = newPort;

            listeningPoint = sipStack.createListeningPoint("127.0.0.1",
                    this.port, transport);

            this.sipProvider = sipStack.createSipProvider(listeningPoint);
            this.sipProvider.setAutomaticDialogSupportEnabled(true);
            logger.info("udp provider " + sipProvider);

        } catch (Exception ex) {
            logger.info(ex.getMessage(), ex);
            TestHarness.fail("Failed to create SIP Provider on port " + newPort + ", because of " + ex.getMessage());
            sipProvider = null;
        }
        return sipProvider;
    }

    /**
     * when notifierPort is 5065, sends subscription to the forker.
     * when notifierPort is 5070, sends subscription directly to the notifier.
     *
     * @param notifierPort
     */
    public void sendSubscribe(int notifierPort) {

        try {

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
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
                    toSipAddress);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            int port = sipProvider.getListeningPoint(transport).getPort();
            ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                    port, transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            // JvB: Make sure that the implementation matches the messagefactory
            callIdHeader = headerFactory.createCallIdHeader( callIdHeader.getCallId() );


            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.SUBSCRIBE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                    Request.SUBSCRIBE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = listeningPoint.getIPAddress();

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(listeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setTransportParam(transport);
            contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // JvB: To test forked SUBSCRIBEs, send it via the Forker
            // Note: BIG Gotcha: Need to do this before creating the
            // ClientTransaction!

            RouteHeader route = headerFactory.createRouteHeader(addressFactory
                    .createAddress("<sip:127.0.0.1:" + notifierPort
                            + ";transport=" + transport + ";lr>"));
            request.addHeader(route);
            // JvB end added

            // Create the client transaction.
            subscribeTid = sipProvider.getNewClientTransaction(request);

            // Create an event header for the subscription.
            EventHeader eventHeader = headerFactory.createEventHeader("foo");
            eventHeader.setEventId("foo");
            request.addHeader(eventHeader);

            logger.info("Subscribe Dialog = " + subscribeTid.getDialog());

            // send the request out.

            this.subscriberDialog = subscribeTid.getDialog();
            this.dialogs.add(subscriberDialog);

            subscribeTid.sendRequest();
            } catch (Throwable ex) {
            logger.info(ex.getMessage(), ex);
            TestHarness.fail("Failed to send Subscribe to notifier port" + notifierPort + ", because of " + ex.getMessage());
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("io exception event recieved");
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("transaction terminated");

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("dialog terminated event recieved");
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        logger.info("Transaction Time out");
    }

    public void checkState() {
        // AbstractSubsnotifyTestCase.assertTrue("Should have two distinct dialogs", this.dialogs.size() == 2);
    }
}
