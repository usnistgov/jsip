package examples.authorization;

import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is the guy that gets
 * shot and asks for authorization.
 * 
 * @author M. Ranganathan
 * @author Kathleen McCallum
 */

public class ShootmeAuth implements SipListener {

    private static AddressFactory addressFactory;
    private static MessageFactory messageFactory;
    private static HeaderFactory headerFactory;
    private static SipStack sipStack;
    private static final String myAddress = "127.0.0.1";
    private static final int myPort = 5070;
    protected ServerTransaction inviteTid;
    private Response okResponse;
    private Request inviteRequest;
    private Dialog dialog;
    DigestServerAuthenticationHelper dsam;

    class MyTimerTask extends TimerTask {
        ShootmeAuth shootme;

        public MyTimerTask(ShootmeAuth shootme) {
            this.shootme = shootme;

        }

        public void run() {
            shootme.sendInviteOK();
        }
    }

    protected static final String usageString = "java " + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

        System.out.println("\n\nRequest " + request.getMethod() + " received at "
                + sipStack.getStackName() + " with server transaction id " + serverTransactionId);

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.CANCEL)) {
            processCancel(requestEvent, serverTransactionId);
        }

    }

    public void processResponse(ResponseEvent responseEvent) {
    }

    /**
     * Process the ACK request. Send the bye and complete the call flow.
     */
    public void processAck(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        System.out.println("shootme: got an ACK! ");
        System.out.println("Dialog State = " + dialog.getState());
    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();

        try {
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);

            }
            System.out.println("shootme: got an Invite with Authorization, sending Trying");
            // System.out.println("shootme: " + request);
            Response response = messageFactory.createResponse(Response.TRYING, request);
            st.sendResponse(response);
       
            // Verify AUTHORIZATION !!!!!!!!!!!!!!!!
            dsam = new DigestServerAuthenticationHelper();

            if (!dsam.doAuthenticatePlainTextPassword(request, "pass")) {
                Response challengeResponse = messageFactory.createResponse(
                        Response.PROXY_AUTHENTICATION_REQUIRED, request);
                dsam.generateChallenge(headerFactory, challengeResponse, "nist.gov");
                st.sendResponse(challengeResponse);
                return;

            }

            System.out.println("shootme: got an Invite with Authorization, sending Trying");
            // System.out.println("shootme: " + request);

            dialog = st.getDialog();

            st.sendResponse(response);

            this.okResponse = messageFactory.createResponse(Response.OK, request);
            Address address = addressFactory.createAddress("Shootme <sip:" + myAddress + ":"
                    + myPort + ">");
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);
            ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
            toHeader.setTag("4321"); // Application is supposed to set.
            okResponse.addHeader(contactHeader);
            this.inviteTid = st;
            // Defer sending the OK to simulate the phone ringing.
            // Answered in 1 second ( this guy is fast at taking calls)
            this.inviteRequest = request;

            new Timer().schedule(new MyTimerTask(this), 1000);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void sendInviteOK() {
        try {
            if (inviteTid.getState() != TransactionState.COMPLETED) {
                System.out.println("shootme: Dialog state before 200: "
                        + inviteTid.getDialog().getState());
                inviteTid.sendResponse(okResponse);
                System.out.println("shootme: Dialog state after 200: "
                        + inviteTid.getDialog().getState());
            }
        } catch (SipException ex) {
            ex.printStackTrace();
        } catch (InvalidArgumentException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootme:  got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is " + serverTransactionId.getDialog().getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processCancel(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("shootme:  got a cancel.");
            if (serverTransactionId == null) {
                System.out.println("shootme:  null tid.");
                return;
            }
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            if (dialog.getState() != DialogState.CONFIRMED) {
                response = messageFactory.createResponse(Response.REQUEST_TERMINATED,
                        inviteRequest);
                inviteTid.sendResponse(response);
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
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("dialogState = " + transaction.getDialog().getState());
        System.out.println("Transaction Time out");
    }

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "shootme");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "shootmedebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "shootmelog.txt");

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
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1", myPort, "udp");

            ShootmeAuth listener = this;

            SipProvider sipProvider = sipStack.createSipProvider(lp);
            System.out.println("udp provider " + sipProvider);
            sipProvider.addSipListener(listener);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public static void main(String args[]) {
        new ShootmeAuth().init();
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException");

    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction terminated event recieved");

    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog terminated event recieved");

    }

}
