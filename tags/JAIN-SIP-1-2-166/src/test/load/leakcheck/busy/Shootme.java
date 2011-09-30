package test.load.leakcheck.busy;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author M. Ranganathan
 */

public class Shootme implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private static final String myAddress = "127.0.0.1";

    private static final int myPort = 5070;

    protected ServerTransaction inviteTid;

    private int counter;

    class Ttask extends TimerTask {

        public Ttask() {
            new Timer().schedule(this, 0, 20000);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.TimerTask#run()
         */
        public void run() {
            System.gc();
            long freemem = Runtime.getRuntime().totalMemory();
            System.out.println("mem = " + freemem);
        }

    }

    class ApplicationData {
        protected int ackCount;
    }

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

        /**
         * System.out.println("\n\nRequest " + request.getMethod() + " received
         * at " + sipStack.getStackName() + " with server transaction id " +
         * serverTransactionId);
         */

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent, serverTransactionId);
        }

    }

    /**
     * Process the ACK request. Send the bye and complete the call flow.
     */
    public void processAck(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        System.out.println("sip provider: " + sipProvider);
        try {
            System.out.println("shootme: got an ACK "
                    + requestEvent.getRequest());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();

        if (counter == 99)
            new Ttask();
        this.counter++;

        try {

            Response response = messageFactory.createResponse(
                    Response.BUSY_HERE, request);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("4321"); // Application is supposed to set.
            Address address = addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
            ContactHeader contactHeader = headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            if (serverTransaction == null) {
                serverTransaction = sipProvider
                        .getNewServerTransaction(request);
            }
            serverTransaction.sendResponse(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        System.out.println("sip provider: " + sipProvider);
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootme:  got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request,
                    null, null);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is "
                    + serverTransactionId.getDialog().getState());

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
        try {
            if (response.getStatusCode() == Response.BUSY_HERE
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {
                if (tid != this.inviteTid) {
                    new Exception().printStackTrace();
                    System.exit(0);
                }
                Dialog dialog = tid.getDialog();
                // Save the tags for the dialog here.
                Request request = tid.getRequest();
                dialog.sendAck(request);
            }
            Dialog dialog = tid.getDialog();
            System.out.println("Dalog State = " + dialog.getState());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

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
        //ifdef SIMULATION
        /*
         * properties.setProperty("javax.sip.IP_ADDRESS","129.6.55.62"); //else
         */
        properties.setProperty("javax.sip.IP_ADDRESS", myAddress);
        //endif
        //
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
        properties.setProperty("javax.sip.STACK_NAME", "shootme");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootmedebug.txt");
        // Guard against starvation.
        properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
        // properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE",
        // "4096");
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
            ListeningPoint lp = sipStack.createListeningPoint(sipStack.getIPAddress(), 5070, "udp");
            ListeningPoint lp1 = sipStack.createListeningPoint(sipStack.getIPAddress(), 5070, "tcp");

            Shootme listener = this;

            SipProvider sipProvider = sipStack.createSipProvider(lp);
            System.out.println("udp provider " + sipProvider);
            sipProvider.addSipListener(listener);
            sipProvider = sipStack.createSipProvider(lp1);
            System.out.println("tcp provider " + sipProvider);
            sipProvider.addSipListener(listener);

            System.gc();
            long freemem = Runtime.getRuntime().totalMemory();
            System.out.println("Current memory" + freemem);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }


    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException occured while retransmitting requests:" + exceptionEvent);
    }
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction Terminated event: " + transactionTerminatedEvent );
    }
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog Terminated event: " + dialogTerminatedEvent);
    }

    public static void main(String args[]) {
        new Shootme().init();
    }

}
