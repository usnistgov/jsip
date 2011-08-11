package test.load.multidialog;

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

    private static String myAddress = "127.0.0.1";

    private static int myPort = 5070;

    int numInvite = 0;

    private int terminatedCount;

    private int createdCount;

    private HashSet dialogIds;

    private HashMap transactionIDs;

    class TTask extends TimerTask {

        RequestEvent requestEvent;

        ServerTransaction st;

        public TTask(RequestEvent requestEvent, ServerTransaction st) {
            this.requestEvent = requestEvent;
            this.st = st;
        }

        public void run() {
            Request request = requestEvent.getRequest();
            try {
                //System.out.println("shootme: got an Invite sending OK");
                Response response = messageFactory.createResponse(180, request);
                ToHeader toHeader = (ToHeader) response
                        .getHeader(ToHeader.NAME);
                Address address = addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);

                //System.out.println("got a server tranasaction " + st);
                Dialog dialog = st.getDialog();
                /*
                 * if (dialog != null) { System.out.println("Dialog " + dialog);
                 * System.out.println("Dialog state " + dialog.getState()); }
                 */
                st.sendResponse(response); // send 180(RING)
                response = messageFactory.createResponse(200, request);
                toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                String toTag = new Integer( (int) (Math.random() * 1000) ).toString();
                toHeader.setTag(toTag); // Application is supposed to set.
                response.addHeader(contactHeader);

                st.sendResponse(response);// send 200(OK)

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
            }

        }

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


             /*
             * System.out.println("\n\nRequest " + request.getMethod() + "
             * received at " + sipStack.getStackName() + " with server
             * transaction id " + serverTransactionId);
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
        try {
            // System.out.println("*** shootme: got an ACK "
            // + requestEvent.getRequest());
            if (serverTransaction == null) {
                System.out
                        .println("null server transaction -- ignoring the ACK!");
                return;
            }
            Dialog dialog = serverTransaction.getDialog();
            this.createdCount ++;
            System.out.println("Dialog Created = " + dialog.getDialogId() + " createdCount " + this.createdCount +
                    " Dialog State = " + dialog.getState() );

            if ( this.dialogIds.contains(dialog.getDialogId())) {
                System.out.println("OOPS ! I already saw " + dialog.getDialogId());
            } else {
                this.dialogIds.add(dialog.getDialogId());
            }

            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction tr = sipProvider
                    .getNewClientTransaction(byeRequest);
            // System.out.println("shootme: got an ACK -- sending bye! ");
            dialog.sendRequest(tr);

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
        try {
            //System.out.println("ProcessInvite");
            Request request = requestEvent.getRequest();
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            // Note you need to create the Server Transaction
            // before the listener returns but you can delay sending the response

            ServerTransaction st = sipProvider.getNewServerTransaction(request);
            if ( transactionIDs.containsKey(st.getBranchId())) {
                System.out.println("OOOPS -- seen this guy before!! This must be a late guy " + st.getBranchId()
                        + " st = " + transactionIDs.get(st.getBranchId()));
                return;
            } else {
                transactionIDs.put( st.getBranchId(),st);
            }

            TTask ttask = new TTask(requestEvent, st);
            int ttime;
            if ((numInvite % 4) == 0)
                ttime = 5000;
            else if ((numInvite % 4) == 1)
                ttime = 1000;
            else
                ttime = 300;
            numInvite++;
            new Timer().schedule(ttask, ttime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        Request request = requestEvent.getRequest();
        try {
            // System.out.println("shootme: got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request,
                    null, null);
            serverTransactionId.sendResponse(response);
            // System.out.println("Dialog State is "
            // + serverTransactionId.getDialog().getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        // System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        // System.out.println("Response received with client transaction id "
        // + tid + ":\n" + response);


        try {
            if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {

                Dialog dialog = tid.getDialog();
                Request request = tid.getRequest();
                dialog.sendAck(request);
            }

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
        /*
         * System.out.println("state = " + transaction.getState());
         * System.out.println("dialog = " + transaction.getDialog());
         * System.out.println("dialogState = " +
         * transaction.getDialog().getState());
         * System.out.println("Transaction Time out" + transaction.getBranchId());
         */

    }

    public void init() {
        this.dialogIds = new HashSet();
        this.transactionIDs = new HashMap();
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        properties.setProperty("javax.sip.STACK_NAME", "shootme");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootmedebuglog.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootmelog.txt");
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
            ListeningPoint lp = sipStack.createListeningPoint(myAddress, 5070,
                    "udp");
            ListeningPoint lp1 = sipStack.createListeningPoint(myAddress, 5070,
                    "tcp");

            Shootme listener = this;

            SipProvider sipProvider = sipStack.createSipProvider(lp);
            System.out.println("udp provider " + sipProvider);
            sipProvider.addSipListener(listener);
            sipProvider = sipStack.createSipProvider(lp1);
            System.out.println("tcp provider " + sipProvider);
            sipProvider.addSipListener(listener);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }

    public static void main(String args[]) {
        /* pass dynamic parameters in *.bat file(command line) */
        if (args.length >= 1)
            myAddress = args[0];
        if (args.length >= 2)
            myPort = Integer.parseInt(args[1]);

        System.out.println("\n***Address=<" + myAddress + ">, Port=<" + myPort
                + ">.");
        new Shootme().init();
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException event");
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        //System.out.println("TransactionTerminatedEvent");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        Dialog dialog = dialogTerminatedEvent.getDialog();
        this.terminatedCount++;
        System.out.println("Dialog Terminated Event "  + dialog.getDialogId() + " terminatedCount = " + terminatedCount);
        if ( ! this.dialogIds.contains(dialog.getDialogId())) {
            System.out.println("Saw a terminated event for an unknown dialog id");
        } else {
            this.dialogIds.remove(dialog.getDialogId());
        }
    }

}
