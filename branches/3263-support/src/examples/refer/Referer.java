package examples.refer;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;


import java.util.*;

/**
 * This example shows an out-of-dialog REFER scenario:
 *
 * referer sends REFER to referee, with Refer-To set to Shootme
 * referee sends INVITE to Shootme, and NOTIFYs to referer about call progress
 *
 * @author Jeroen van Bemmel
 */

public class Referer implements SipListener {

    private SipProvider sipProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private ContactHeader contactHeader;

    private static String transport;

    private int count;

    private static Logger logger = Logger.getLogger(Referer.class);

    static {
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(new ConsoleAppender(new SimpleLayout()));
            logger.addAppender(new FileAppender(new SimpleLayout(),
                    "refereroutputlog.txt"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ClientTransaction subscribeTid;

    private ListeningPoint listeningPoint;

    protected static final String usageString = "java "
            + "examples.subsnotify.Subscriber \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        logger.info(usageString);
        System.exit(0);

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
        logger.info( request );
        if (request.getMethod().equals(Request.NOTIFY))
            processNotify(requestReceivedEvent, serverTransactionId);

    }

    public void processNotify(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider provider = (SipProvider) requestEvent.getSource();
        Request notify = requestEvent.getRequest();
        try {
            logger.info("referer:  got a notify count  " + this.count++ );
            if (serverTransactionId == null) {
                logger.info("referer:  null TID.");
                serverTransactionId = provider.getNewServerTransaction(notify);
            }
            Dialog dialog = serverTransactionId.getDialog();
            logger.info("Dialog = " + dialog);

            if (dialog != null) {
                logger.info("Dialog State = " + dialog.getState());
            }
            Response response = messageFactory.createResponse(200, notify);
            // SHOULD add a Contact
            ContactHeader contact = (ContactHeader) contactHeader.clone();
            ((SipURI)contact.getAddress().getURI()).setParameter( "id", "sub" );
            response.addHeader( contact );
            logger.info("Transaction State = " + serverTransactionId.getState());
            serverTransactionId.sendResponse(response);
            if (dialog != null ) {
                logger.info("Dialog State = " + dialog.getState());
            }
            SubscriptionStateHeader subscriptionState = (SubscriptionStateHeader) notify
                    .getHeader(SubscriptionStateHeader.NAME);

            // Subscription is terminated?
            String state = subscriptionState.getState();
            if (state.equalsIgnoreCase(SubscriptionStateHeader.TERMINATED)) {
                dialog.delete();
            } else {
                logger.info("Referer: state now " + state);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Unexpected exception",ex);
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        logger.info("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        logger.info("Response received with client transaction id " + tid
                + ":\n" + response.getStatusCode()  );
        if (tid == null) {
            logger.info("Stray response -- dropping ");
            return;
        }
        logger.info("transaction state is " + tid.getState());
        logger.info("Dialog = " + tid.getDialog());
        if ( tid.getDialog () != null )
        logger.info("Dialog State is " + tid.getDialog().getState());

    }

    public void createProvider() throws Exception {

        this.listeningPoint = sipStack.createListeningPoint("127.0.0.1", 5060,
                transport);
        sipProvider = sipStack.createSipProvider(listeningPoint);

    }

    public void sendSubscribe() {

        try {

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "127.0.0.1";
            String toUser = "referee";
            String toDisplayName = "Referee";

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
            requestURI.setPort( 5065 ); // referee

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            int port = sipProvider.getListeningPoint(transport).getPort();
            ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                    port, transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.REFER);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                    Request.REFER, callIdHeader, cSeqHeader, fromHeader,
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

            // Create the client transaction.
            subscribeTid = sipProvider.getNewClientTransaction(request);

            // REFER has an implicit "refer" event
            // Create an event header for the subscription.
            // EventHeader eventHeader = headerFactory.createEventHeader("foo");
            // eventHeader.setEventId("foo");
            // request.addHeader(eventHeader);

            ReferToHeader referTo = headerFactory.createReferToHeader(
                    addressFactory.createAddress( "<sip:127.0.0.1:" + 5070 + ">" )
            );
            request.addHeader( referTo );

            logger.info("Refer Dialog = " + subscribeTid.getDialog());

            // send the request out.
            subscribeTid.sendRequest();

        } catch (Throwable ex) {
            logger.info(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public static void main(String args[]) throws Exception {

        transport = "udp";

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        properties.setProperty("javax.sip.USE_ROUTER_FOR_ALL_URIS", "false");

        properties.setProperty("javax.sip.STACK_NAME", "referer");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "refererdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "refererlog.txt");

        properties.setProperty("javax.sip.FORKABLE_EVENTS", "foo");

        // Set to 0 in your production code for max speed.
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

        sipStack = sipFactory.createSipStack(properties);
        logger.info("createSipStack " + sipStack);
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        Referer subscriber = new Referer();
        subscriber.createProvider();
        subscriber.sipProvider.addSipListener(subscriber);
        subscriber.sendSubscribe();

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("io exception event recieved");
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent tte) {
        logger.info("transaction terminated:" + tte );

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("dialog terminated event recieved");
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        logger.info("Transaction Time out");
    }
}
