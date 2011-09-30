package test.load.subsnotify;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

/**
 * This class is a UAS template.
 *
 * @author M. Ranganathan
 */

public class Notifier implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private static final int myPort = 5070;

    protected ServerTransaction inviteTid;

    private SipProvider sipProvider;

    class MyTimerTask extends TimerTask {
        Notifier notify;

        public MyTimerTask(Notifier notify1) {
            notify = notify1;

        }

        public void run() {

        }

    }

    protected static final String usageString = "java " + "notifier \n"
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
            ServerTransaction st = processSubscribe(requestEvent,
                    serverTransactionId);
            // This could be a retransmission for a Server Transaction
            // associated with Dialog that we have already deleted.
            if (st.getDialog().getState() != DialogState.TERMINATED) {
                sendNotify(st.getDialog());
            }
        }

    }

    public void sendNotify(Dialog dialog) {

        try {

            Request request = dialog.createRequest(Request.NOTIFY);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);
            request.setHeader(maxForwards);

            // Create contact headers
            String host = "127.0.0.1";

            SipURI contactUrl = addressFactory.createSipURI(null, host);
            contactUrl.setLrParam();
            contactUrl.setPort(sipProvider.getListeningPoint("udp").getPort());

            Address contactAddress = addressFactory.createAddress(contactUrl);

            request
                    .setHeader(headerFactory
                            .createContactHeader(contactAddress));

            EventHeader eventHeader = null;
            eventHeader = headerFactory.createEventHeader("reg");
            request.addHeader(eventHeader);

            SubscriptionStateHeader subscriptionStateHeader = headerFactory
                    .createSubscriptionStateHeader("active");
            subscriptionStateHeader.setExpires(200);
            request.addHeader(subscriptionStateHeader);

            ClientTransaction ct = sipProvider.getNewClientTransaction(request);
            dialog.sendRequest(ct);

            // send the request out.

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }

    public void processResponse(ResponseEvent responseEvent) {
    }

    public ServerTransaction processSubscribe(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("notifier:  got a subscribe. sending OK.");
            Response response = messageFactory.createResponse(200, request);
            if (serverTransactionId == null) {
                serverTransactionId = sipProvider
                        .getNewServerTransaction(request);
            }

            ExpiresHeader expires = (ExpiresHeader) request
                    .getHeader(ExpiresHeader.NAME);
            response.addHeader(request.getHeader(ExpiresHeader.NAME));
            response.addHeader(request.getHeader(EventHeader.NAME));
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            if (toHeader.getTag() == null) {
                String tag = new Long(new java.util.Random().nextLong())
                        .toString();
                toHeader.setTag(tag);
            }
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is "
                    + serverTransactionId.getDialog().getState());
            if (expires.getExpires() == 0) {
                System.out.println("Deleting Dialog ");
                serverTransactionId.getDialog().delete();
            }
            return serverTransactionId;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
        return null;
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
        String transport = "udp";
        String peerHostPort = "127.0.0.1:5060";
        properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                + transport);
        properties.setProperty("javax.sip.STACK_NAME", "notifier");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "NONE");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootmedebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootmelog.txt");
        properties.setProperty("gov.nist.javax.sip.MAX_LISTENER_RESPONSE_TIME",
                "20");

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
                    myPort, "udp");

            Notifier listener = this;

            sipProvider = sipStack.createSipProvider(lp);
            System.out.println("udp provider " + sipProvider);
            sipProvider.addSipListener(listener);

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
        System.out.println("IOException");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction terminated event recieved");

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog terminated event recieved");

    }

}
