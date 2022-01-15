package examples.subsnotify;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * This class is a UAC template. Shootist is the guy that shoots and notifier is
 * the guy that gets shot.
 *
 * @author M. Ranganathan
 */

public class Notifier implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;


    private int port;

    protected SipProvider udpProvider;

    protected Dialog dialog;

    private static Logger logger = Logger.getLogger(Notifier.class) ;

    protected int notifyCount = 0;



    class MyEventSource implements Runnable {
        private Notifier notifier;
        private EventHeader eventHeader;

        public MyEventSource(Notifier notifier, EventHeader eventHeader ) {
            this.notifier = notifier;
            this.eventHeader = eventHeader;
        }

        public void run() {
            try {
                for (int i = 0; i < 100; i++) {

                    Thread.sleep(100);
                    Request request = this.notifier.dialog.createRequest(Request.NOTIFY);
                    SubscriptionStateHeader subscriptionState = headerFactory
                            .createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
                    request.addHeader(subscriptionState);
                    request.addHeader(eventHeader);

                    // Lets mark our Contact
                    ((SipURI)dialog.getLocalParty().getURI()).setParameter("id","not2");

                    ClientTransaction ct = udpProvider.getNewClientTransaction(request);
                    logger.info("NOTIFY Branch ID " +
                        ((ViaHeader)request.getHeader(ViaHeader.NAME)).getParameter("branch"));
                    this.notifier.dialog.sendRequest(ct);
                    logger.info("Dialog " + dialog);
                    logger.info("Dialog state after active NOTIFY: " + dialog.getState());
                    synchronized (Notifier.this) {
                    notifyCount ++;
                    }
                }

                /*
                 * JvB: Changed the scenario a bit to illustrate an issue: Subscriber
                 * now sends SUBSCRIBE w/ Expires=0 to unsubscribe, upon receiving the
                 * NOTIFY above
                 *
                Request request = this.notifier.dialog
                        .createRequest(Request.NOTIFY);
                SubscriptionStateHeader subscriptionState = headerFactory
                        .createSubscriptionStateHeader(SubscriptionStateHeader.TERMINATED);
                request.addHeader(eventHeader);
                request.addHeader(subscriptionState);
                ClientTransaction ct = udpProvider
                        .getNewClientTransaction(request);
                this.notifier.dialog.sendRequest(ct);
                */
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    protected static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        logger.info(usageString);
        System.exit(0);

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
            if ( eventHeader == null) {
                logger.info("Cannot find event header.... dropping request.");
                return;
            }

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
                if (toHeader.getTag()!=null) {
                    System.err.println( "####ERROR: To-tag!=null but no dialog match! My dialog=" + dialog.getState() );
                }
                toHeader.setTag(toTag); // Application is supposed to set.

                this.dialog = st.getDialog();
                // subscribe dialogs do not terminate on bye.
                this.dialog.terminateOnBye(false);
                if (dialog != null) {
                    logger.info("Dialog " + dialog);
                    logger.info("Dialog state " + dialog.getState());
                }
            } else {
                response = messageFactory.createResponse(200, request);
            }

            // Both 2xx response to SUBSCRIBE and NOTIFY need a Contact
            Address address = addressFactory.createAddress("Notifier <sip:127.0.0.1>");
            ((SipURI)address.getURI()).setPort( udpProvider.getListeningPoint("udp").getPort() );
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
            ClientTransaction ct = udpProvider.getNewClientTransaction(notifyRequest);

            // Let the other side know that the tx is pending acceptance
            //
            dialog.sendRequest(ct);
            logger.info("NOTIFY Branch ID " +
                ((ViaHeader)request.getHeader(ViaHeader.NAME)).getParameter("branch"));
            logger.info("Dialog " + dialog);
            logger.info("Dialog state after pending NOTIFY: " + dialog.getState());

            if (isInitial) {
                Thread myEventSource = new Thread(new MyEventSource(this,eventHeader));
                myEventSource.start();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            // System.exit(0);
        }
    }

    public synchronized void processResponse(ResponseEvent responseReceivedEvent) {
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        if ( response.getStatusCode() !=  200 ) {
            this.notifyCount --;
        } else {
            System.out.println("Notify Count = " + this.notifyCount);
        }

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
    }

    private static void initFactories ( int port ) throws Exception {

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        logger.addAppender(new FileAppender
            ( new SimpleLayout(),"notifieroutputlog_" + port + ".txt" ));

        properties.setProperty("javax.sip.STACK_NAME", "notifier" + port );
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "notifierdebug_"+port+".txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "notifierlog_"+port+".txt");


        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            logger.info("sipStack = " + sipStack);
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
        } catch  (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void createProvider() {

        try {

            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    this.port, "udp");

            this.udpProvider = sipStack.createSipProvider(lp);
            logger.info("udp provider " + udpProvider);

        } catch (Exception ex) {
            logger.info(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }

    public Notifier( int port ) {
        this.port = port;
    }


    public static void main(String args[]) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5070;
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        initFactories( port );
        Notifier notifier = new Notifier( port );
        notifier.createProvider( );
        notifier.udpProvider.addSipListener(notifier);
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        // TODO Auto-generated method stub

    }

}
