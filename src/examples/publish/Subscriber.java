package examples.publish;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

//ifdef SIMULATION
/*
import sim.java.*;
//endif
*/

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme
 * is the guy that gets shot.
 *
 *@author M. Ranganathan
 */

public class Subscriber implements SipListener {

    private static SipProvider sipProvider;
    private static AddressFactory addressFactory;
    private static MessageFactory messageFactory;
    private static HeaderFactory headerFactory;
    private static SipStack sipStack;
    private ContactHeader contactHeader;

    protected ClientTransaction subscribeTid;

    protected static final String usageString =
        "java "
            + "examples.subsnotify.Subscriber \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId =
            requestReceivedEvent.getServerTransaction();

        System.out.println(
            "\n\nRequest "
                + request.getMethod()
                + " received at "
                + sipStack.getStackName()
                + " with server transaction id "
                + serverTransactionId);

        if (request.getMethod().equals(Request.NOTIFY))
            processNotify(request, serverTransactionId);

    }

    public void processNotify(
        Request request,
        ServerTransaction serverTransactionId) {
        try {
            System.out.println("subscriber:  got a notify .");
            if (serverTransactionId == null) {
                System.out.println("subscriber:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            System.out.println("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse(200,request);
            serverTransactionId.sendResponse(response);
            SubscriptionStateHeader subscriptionState =  (SubscriptionStateHeader)
                        request.getHeader(SubscriptionStateHeader.NAME);
            // Subscription is terminated.
            if ( subscriptionState.getState().equals(SubscriptionStateHeader.TERMINATED)) {
                dialog.delete();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        System.out.println(
            "Response received with client transaction id "
                + tid
                + ":\n"
                + response);
        if (tid == null) {
            System.out.println("Stray response -- dropping ");
            return;
        }
        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
        System.out.println("Dialog State is " + tid.getDialog().getState());

    }



    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipProvider = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        String transport = "udp";
        properties.setProperty(
            "javax.sip.OUTBOUND_PROXY",
            "127.0.0.1:5070/" + transport);
        properties.setProperty("javax.sip.STACK_NAME", "subscriber");
        properties.setProperty("javax.sip.MAX_MESSAGE_SIZE", "1048576");
        properties.setProperty(
            "gov.nist.javax.sip.DEBUG_LOG",
            "subscriberdebug.txt");
        properties.setProperty(
            "gov.nist.javax.sip.SERVER_LOG",
            "subscriberlog.txt");
        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
        // Set to 0 in your production code for max speed.
        // You need  16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",5060, "udp");
            sipProvider = sipStack.createSipProvider(lp);
            Subscriber listener = this;
            sipProvider.addSipListener(listener);
            lp = sipStack.createListeningPoint("127.0.0.1",5060, "tcp");
            SipProvider sipProvider1 = sipStack.createSipProvider(lp);
            sipProvider1.addSipListener(listener);
            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress =
                addressFactory.createSipURI(fromName, fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader =
                headerFactory.createFromHeader(fromNameAddress, "12345");

            // create To Header
            SipURI toAddress =
                addressFactory.createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader =
                headerFactory.createToHeader(toNameAddress, null);

            // create Request URI
            SipURI requestURI =
                addressFactory.createSipURI(toUser, toSipAddress);


            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            int port = sipProvider.getListeningPoint("udp").getPort();
            ViaHeader viaHeader =
                headerFactory.createViaHeader(
                    "127.0.0.1",
                    port,
                    transport,
                    null);


            // add via headers
            viaHeaders.add(viaHeader);


            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader =
                headerFactory.createCSeqHeader(1L, Request.SUBSCRIBE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards =
                headerFactory.createMaxForwardsHeader(70);

            // Create the request.
            Request request =
                messageFactory.createRequest(
                    requestURI,
                    Request.SUBSCRIBE,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwards);
            // Create contact headers
            String host = lp.getIPAddress();

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(lp.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint("udp").getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader =
                headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Create the client transaction.
            listener.subscribeTid = sipProvider.getNewClientTransaction(request);

            // send the request out.
            listener.subscribeTid.sendRequest();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public static void main(String args[]) {
        new Subscriber().init();

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("io exception event recieved");
    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("transaction terminated");

    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("dialog terminated event recieved");
    }
    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        System.out.println("Transaction Time out");
    }
}
