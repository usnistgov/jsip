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
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import test.tck.TestHarness;
import test.tck.msgflow.callflows.ProtocolObjects;

/**
 * This is the side that sends out the notify.
 *
 * This code is released to  domain.
 *
 * @author M. Ranganathan
 */

public class Notifier implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;


    private int port;

    protected SipProvider sipProvider;

    protected Dialog dialog;

    private String transport;

    private static Logger logger = Logger.getLogger(Notifier.class) ;

    private boolean gotSubscribeRequest;

    static {
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(new FileAppender(new SimpleLayout(),
                    "logs/notifieroutputlog.txt"));
        } catch (Exception ex) {
            logger.info(ex.getMessage(), ex);
            TestHarness.fail("Failed to initialize Subscriber, because of " + ex.getMessage());
        }
    }

    class MyEventSource implements Runnable {
        private Notifier notifier;
        private EventHeader eventHeader;

        public MyEventSource(Notifier notifier, EventHeader eventHeader ) {
            this.notifier = notifier;
            this.eventHeader = eventHeader;
        }

        public void run() {
            try {
                for (int i = 0; i < 1; i++) {

                    Thread.sleep(1000);
                    Request request = this.notifier.dialog.createRequest(Request.NOTIFY);
                    SubscriptionStateHeader subscriptionState = headerFactory
                            .createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
                    request.addHeader(subscriptionState);
                    request.addHeader(eventHeader);

                    // Lets mark our Contact
                    ((SipURI)dialog.getLocalParty().getURI()).setParameter("id","not2");

                    ClientTransaction ct = sipProvider.getNewClientTransaction(request);
                    logger.info("NOTIFY Branch ID " +
                        ((ViaHeader)request.getHeader(ViaHeader.NAME)).getParameter("branch"));
                    this.notifier.dialog.sendRequest(ct);
                    logger.info("Dialog " + dialog);
                    logger.info("Dialog state after active NOTIFY: " + dialog.getState());
                }


            } catch (Throwable ex) {
                logger.info(ex.getMessage(), ex);
                TestHarness.fail("Failed MyEventSource.run(), because of " + ex.getMessage());
            }
        }
    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId
                + " and dialog id " + requestEvent.getDialog() );

        if (request.getMethod().equals(Request.SUBSCRIBE)) {
            processSubscribe(requestEvent, serverTransactionId);
        }

    }

    /**
     * Process the invite request.
     */
    public void processSubscribe(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            logger.info("notifier: got an Subscribe sending OK");
            logger.info("notifier:  " + request);
            logger.info("notifier : dialog = " + requestEvent.getDialog());
            EventHeader eventHeader = (EventHeader) request.getHeader(EventHeader.NAME);
            this.gotSubscribeRequest = true;

            AbstractSubsnotifyTestCase.assertTrue("Event header is null ", eventHeader != null);

            // Always create a ServerTransaction, best as early as possible in the code
            Response response = null;
            ServerTransaction st = requestEvent.getServerTransaction();
            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }

            // Check if it is an initial SUBSCRIBE or a refresh / unsubscribe
            boolean isInitial = requestEvent.getDialog() == null;
            if ( isInitial ) {
                // JvB: need random tags to test forking
                String toTag = Integer.toHexString( (int) (Math.random() * Integer.MAX_VALUE) );
                response = messageFactory.createResponse(202, request);
                ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);

                // Sanity check: to header should not ahve a tag. Else the dialog
                // should have matched
                AbstractSubsnotifyTestCase.assertTrue("To tag should  be null ", toHeader.getTag() == null);
                toHeader.setTag(toTag); // Application is supposed to set.

                this.dialog = st.getDialog();
                // subscribe dialogs do not terminate on bye.
                this.dialog.terminateOnBye(false);

                AbstractSubsnotifyTestCase.assertTrue("initial -- dialog assigned to the transaction not null " , dialog != null );
                AbstractSubsnotifyTestCase.assertTrue("Dialog state should be null ", dialog.getState() == null);

            } else {
                response = messageFactory.createResponse(200, request);
            }

            // Both 2xx response to SUBSCRIBE and NOTIFY need a Contact
            Address address = addressFactory.createAddress("Notifier <sip:127.0.0.1>");
            ((SipURI)address.getURI()).setPort( sipProvider.getListeningPoint(transport).getPort() );
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);

            // Expires header is mandatory in 2xx responses to SUBSCRIBE
            ExpiresHeader expires = (ExpiresHeader) request.getHeader( ExpiresHeader.NAME );
            if (expires==null) {
                expires = headerFactory.createExpiresHeader(30);// rather short
            }
            response.addHeader( expires );

            /*
             * JvB: The SUBSCRIBE MUST be answered first. See RFC3265 3.1.6.2:
             * "[...] a NOTIFY message is always sent immediately after any 200-
             * class response to a SUBSCRIBE request"
             *
             *  Do this before creating the NOTIFY request below
             */
            st.sendResponse(response);
            //Thread.sleep(1000); // Be kind to implementations

            /*
             * NOTIFY requests MUST contain a "Subscription-State" header with a
             * value of "active", "pending", or "terminated". The "active" value
             * indicates that the subscription has been accepted and has been
             * authorized (in most cases; see section 5.2.). The "pending" value
             * indicates that the subscription has been received, but that
             * policy information is insufficient to accept or deny the
             * subscription at this time. The "terminated" value indicates that
             * the subscription is not active.
             */

            Request notifyRequest = dialog.createRequest( "NOTIFY" );


            // Mark the contact header, to check that the remote contact is updated
            ((SipURI)contactHeader.getAddress().getURI()).setParameter("id","not");

            // Initial state is pending, second time we assume terminated (Expires==0)
            SubscriptionStateHeader sstate = headerFactory.createSubscriptionStateHeader(
                    isInitial ? SubscriptionStateHeader.PENDING : SubscriptionStateHeader.TERMINATED );

            // Need a reason for terminated
            if ( sstate.getState().equalsIgnoreCase("terminated") ) {
                sstate.setReasonCode( "deactivated" );
            }

            notifyRequest.addHeader(sstate);
            notifyRequest.setHeader(eventHeader);
            notifyRequest.setHeader(contactHeader);
            // notifyRequest.setHeader(routeHeader);
            ClientTransaction ct = sipProvider.getNewClientTransaction(notifyRequest);

            // Let the other side know that the tx is pending acceptance
            //
            dialog.sendRequest(ct);
            logger.info("NOTIFY Branch ID " +
                ((ViaHeader)request.getHeader(ViaHeader.NAME)).getParameter("branch"));
            logger.info("Dialog " + dialog);
            logger.info("Dialog state after pending NOTIFY: " + dialog.getState());
            AbstractSubsnotifyTestCase.assertTrue("Dialog state after pending NOTIFY ",
                    dialog.getState() == DialogState.CONFIRMED);

            if (isInitial) {
                Thread myEventSource = new Thread(new MyEventSource(this,eventHeader));
                myEventSource.start();
            }
        } catch (Throwable ex) {
            logger.info(ex.getMessage(), ex);
            TestHarness.fail("Failed to processs Subscriber, because of " + ex.getMessage());
        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        logger.info("Response received with client transaction id "
                + tid + " CSeq = " +
                response.getHeader(CSeqHeader.NAME)
                + " status code = " + response.getStatusCode() );

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
        logger.info("dialogState = "
                + transaction.getDialog().getState());
        logger.info("Transaction Time out");

        AbstractSubsnotifyTestCase.fail("Unexpected timeout event");
    }

    public SipProvider createProvider(int newPort) {

        try {

            port = newPort;

            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    this.port, transport);

            this.sipProvider = sipStack.createSipProvider(lp);
            logger.info("udp provider " + sipProvider);

        } catch (Exception ex) {
            logger.info(ex.getMessage(), ex);
            sipProvider = null;
            TestHarness.fail("Failed to create SIP Provider on port " + newPort + ", because of " + ex.getMessage());
        }

        return sipProvider;
    }

    public Notifier(ProtocolObjects protObjects) {
        addressFactory = protObjects.addressFactory;
        messageFactory = protObjects.messageFactory;
        headerFactory = protObjects.headerFactory;
        sipStack = protObjects.sipStack;
        transport = protObjects.transport;
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        // TODO Auto-generated method stub

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // TODO Auto-generated method stub

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        // TODO Auto-generated method stub

    }

    public void checkState() {
        TestHarness.assertTrue("Did not see subscribe", this.gotSubscribeRequest);
    }

}
