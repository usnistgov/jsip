package examples.publish;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

/**
 * The notifier
 *
 * @author M. Ranganathan
 */

public class Notifier implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    protected SipProvider tcpProvider;

    protected SipProvider udpProvider;

    protected Dialog dialog;


    protected static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();

        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        if (request.getMethod().equals(Request.SUBSCRIBE)) {
            processSubscribe(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.PUBLISH)) {
            processPublish(requestEvent, serverTransactionId);
        }

    }

    private void processPublish(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        try {
            EventHeader eventHeader = (EventHeader) requestEvent.getRequest()
                    .getHeader(EventHeader.NAME);
            Request request = this.dialog.createRequest(Request.NOTIFY);
            SubscriptionStateHeader subscriptionState = headerFactory
                    .createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
            request.addHeader(subscriptionState);
            request.addHeader(eventHeader);
            ClientTransaction ct = udpProvider.getNewClientTransaction(request);
            this.dialog.sendRequest(ct);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
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
            System.out.println("notifier: got an Subscribe sending OK");
            System.out.println("notifier:  " + request);
            EventHeader eventHeader = (EventHeader) request
                    .getHeader(EventHeader.NAME);
            if (eventHeader == null) {
                System.out
                        .println("Cannot find event header.... dropping request.");
                return;
            }
            Response response = messageFactory.createResponse(202, request);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("4321"); // Application is supposed to set.
            Address address = addressFactory
                    .createAddress("Notifier <sip:127.0.0.1:5070>");
            ContactHeader contactHeader = headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            System.out.println("got a server transaction " + st);

            this.dialog = st.getDialog();
            // subscribe dialogs do not terminate on bye.
            this.dialog.terminateOnBye(false);
            if (dialog != null) {
                System.out.println("Dialog " + dialog);
                System.out.println("Dialog state " + dialog.getState());
            }
            st.sendResponse(response);
            if (dialog != null) {
                System.out.println("Dialog " + dialog);
                System.out.println("Dialog state " + dialog.getState());
            }
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
            Request notifyRequest = dialog.createRequest(Request.NOTIFY);
            SubscriptionStateHeader sstate = headerFactory
                    .createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
            notifyRequest.addHeader(sstate);
            notifyRequest.addHeader(eventHeader);
            ClientTransaction ct = udpProvider
                    .getNewClientTransaction(notifyRequest);
            dialog.sendRequest(ct);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        System.out.println("Response received with client transaction id "
                + tid + ":\n" + response);

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
        }
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("dialogState = "
                + transaction.getDialog().getState());
        System.out.println("Transaction Time out");
    }

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        properties.setProperty("javax.sip.STACK_NAME", "notifier");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "notifierdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "notifierlog.txt");
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "4096");
        properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS",
                "false");

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("sipStack = " + sipStack);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            System.exit(0);
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    5070, "udp");
            ListeningPoint lp1 = sipStack.createListeningPoint("127.0.0.1",
                    5070, "tcp");

            Notifier listener = this;

            this.udpProvider = sipStack.createSipProvider(lp);
            System.out.println("udp provider " + udpProvider);
            udpProvider.addSipListener(listener);
            this.tcpProvider = sipStack.createSipProvider(lp1);
            System.out.println("tcp provider " + tcpProvider);
            tcpProvider.addSipListener(listener);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }

    public static void main(String args[]) {
        new Notifier().init();
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

}
