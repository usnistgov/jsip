package examples.redirect;


import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.util.*;

import junit.framework.TestCase;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author M. Ranganathan
 */

public class Shootme extends TestCase implements SipListener {


    private ProtocolObjects protocolObjects;

    private static final String myAddress = "127.0.0.1";

    public static final int myPort = 5070;

    protected ServerTransaction inviteTid;

    private Response okResponse;

    private Request inviteRequest;

    private Dialog dialog;

    private SipProvider sipProvider;

    private int inviteCount = 0;

    private int dialogTerminationCount = 0;

    private int dialogCount;

    private static Logger logger = Logger.getLogger(Shootme.class);

    class MyTimerTask extends TimerTask {
        Shootme shootme;

        public MyTimerTask(Shootme shootme) {
            this.shootme = shootme;

        }

        public void run() {
            shootme.sendInviteOK();
        }

    }

    protected static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";



    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod() + " received at "
                + protocolObjects.sipStack.getStackName() + " with server transaction id "
                + serverTransactionId);

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else {
            fail("unexpected request recieved");
        }
    }

    public void processResponse(ResponseEvent responseEvent) {

      Response r = responseEvent.getResponse();
         logger.info("\n\nResponse " + r.getStatusCode() + " received at "
                + protocolObjects.sipStack.getStackName() );

      ClientTransaction ct = responseEvent.getClientTransaction();
      assertNotNull( ct );
      assertNotNull( ct.getDialog() );
      assertEquals( DialogState.TERMINATED, ct.getDialog().getState() );
    }

    /**
     * Process the ACK request. Send the bye and complete the call flow.
     */
    public void processAck(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        try {
            logger.info("shootme: got an ACK! " + requestEvent.getRequest());
            logger.info("Dialog State = " + dialog.getState() + " sending BYE ");
            assertTrue(dialog.getState() == DialogState.CONFIRMED);
            Request bye = dialog.createRequest(Request.BYE);
            logger.info("bye request = " + bye);

            ClientTransaction ct = this.sipProvider
                    .getNewClientTransaction(bye);
                dialog.sendRequest(ct);

            // JvB: not yet, set upon receiving BYE OK response
            // assertEquals( DialogState.TERMINATED, dialog.getState() );
        } catch (Exception ex) {
            logger.error("unexpected exception", ex);
            fail("unexpected exception sending bye");
        }
    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            this.inviteCount++;
            logger.info("shootme: got an Invite " + request);
            assertTrue(request.getHeader(ContactHeader.NAME) != null );
            Response response = protocolObjects.messageFactory.createResponse(
                    Response.TRYING, request);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            Address address = protocolObjects.addressFactory
                    .createAddress("Shootme <sip:" + myAddress + ":" + myPort
                            +";transport="+protocolObjects.transport
                            + ">");
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }
            Dialog dialog = st.getDialog();

            assertTrue(this.dialog != dialog);
            this.dialogCount ++;
            this.dialog = dialog;

            logger.info("Shootme: dialog = " + dialog);



            st.sendResponse(response);
            ContactHeader contactHeader = protocolObjects.headerFactory.createContactHeader(address);

            /**
             * We distinguish here after the display header in the Request URI
             * to create a final response
             */
            if (((SipURI)(request.getRequestURI())).getParameter("redirection") == null) {
                Response moved = protocolObjects.messageFactory.createResponse(
                        Response.MOVED_TEMPORARILY, request);
                moved.addHeader(contactHeader);
                toHeader = (ToHeader) moved.getHeader(ToHeader.NAME);
                toHeader.setTag("4321"); // Application is supposed to set.
                st.sendResponse(moved);
                // Check that the stack is assigning the right state to the
                // dialog.
                assertTrue("dialog state should be terminated",dialog.getState() == DialogState.TERMINATED);

            } else {
                Response ringing = protocolObjects.messageFactory
                .createResponse(Response.RINGING, request);
                toHeader = (ToHeader) ringing.getHeader(ToHeader.NAME);
                toHeader.setTag("5432"); // Application is supposed to set.
                st.sendResponse(ringing);
                assertTrue("server tx state should be proceeding",st.getState() == TransactionState.PROCEEDING);

                this.okResponse = protocolObjects.messageFactory
                        .createResponse(Response.OK, request);
                toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
                toHeader.setTag("5432"); // Application is supposed to set.
                okResponse.addHeader(contactHeader);
                this.inviteTid = st;
                // Defer sending the OK to simulate the phone ringing.
                this.inviteRequest = request;


                new Timer().schedule(new MyTimerTask(this), 1000);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Error sending response to INVITE");
        }
    }

    private void sendInviteOK() {
        try {
                assertTrue(inviteTid.getState() == TransactionState.PROCEEDING);

                inviteTid.sendResponse(okResponse);
                logger.info("Dialog = " + inviteTid.getDialog());
                logger.info("shootme: Dialog state after response: "
                        + okResponse.getStatusCode() + " "
                        + inviteTid.getDialog().getState());

                assertTrue(inviteTid.getState() == TransactionState.TERMINATED);

        } catch (SipException ex) {
            logger.error("unexpected exception", ex);
            fail("unexpected exception");

        } catch (InvalidArgumentException ex) {
            logger.error("unexpceted exception", ex);
            fail("unexpected exception");
        }
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            logger.info("shootme:  got a bye sending OK.");
            Response response = protocolObjects.messageFactory.createResponse(
                    200, request);
            serverTransactionId.sendResponse(response);
            logger.info("Dialog State is "
                    + serverTransactionId.getDialog().getState());

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
        logger.info("state = " + transaction.getState());
        logger.info("dialog = " + transaction.getDialog());
        logger.info("dialogState = " + transaction.getDialog().getState());
        logger.info("Transaction Time out");
        fail("unexpected timeout occured");
    }

    public SipProvider createProvider() throws Exception {
        ListeningPoint lp = protocolObjects.sipStack.createListeningPoint("127.0.0.1", myPort,
                protocolObjects.transport);
        this.sipProvider = protocolObjects.sipStack.createSipProvider(lp);
        return this.sipProvider;
    }

    public Shootme(ProtocolObjects protocolObjects) {
        this.protocolObjects = protocolObjects;
    }

    public static void main(String args[]) throws Exception {
        Shootme shootme = new Shootme(new ProtocolObjects("shootme", true,"udp",""));
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        shootme.createProvider();
        shootme.sipProvider.addSipListener(shootme);

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("IOException");
        fail("unexpected exception");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Transaction terminated event recieved");

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("Dialog terminated event recieved dialog = " + dialogTerminatedEvent.getDialog());
        this.dialogTerminationCount++;

    }

    public void checkState() {
        assertTrue(this.dialogTerminationCount == this.dialogCount);

    }

}
