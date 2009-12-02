package examples.cancel;

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



    private static final String myAddress = "127.0.0.1";

    private static final String transport = "udp";

    private static final int myPort = 5070;

    private ServerTransaction inviteTid;

    private Response okResponse;

    private Request inviteRequest;

    private SipProvider sipProvider;

    private Dialog dialog;

    private static Logger logger = Logger.getLogger(Shootme.class);

    private static final String unexpectedException = "Unexpected Exception ";

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

        logger.info("\n\nRequest " + request.getMethod()
                + " received at " + ProtocolObjects.sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

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
    public void processAck(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        try {
            if (dialog.getState() == DialogState.CONFIRMED) {
                Request byeRequest = dialog.createRequest(Request.BYE);
                ClientTransaction tr = sipProvider
                        .getNewClientTransaction(byeRequest);
                logger.info("shootme: got an ACK -- sending bye! ");
                dialog.sendRequest(tr);
                logger.info("Dialog State = " + dialog.getState());
            }
        } catch (Exception ex) {
            logger.error(ex);
            fail(unexpectedException);
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
            logger.info("shootme: got an Invite sending RINGING");
            // logger.info("shootme: " + request);
            Response response = ProtocolObjects.messageFactory.createResponse(180, request);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("4321"); // Application is supposed to set.
            Address address = ProtocolObjects.addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
            ContactHeader contactHeader = ProtocolObjects.headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
                logger.info("Created a new server transaction for "
                        + request.getMethod() + " serverTransaction = " + st);
            }
            dialog = st.getDialog();

            st.sendResponse(response);
            this.okResponse = ProtocolObjects.messageFactory.createResponse(200, request);
            toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
            toHeader.setTag("4321"); // Application is supposed to set.
            okResponse.addHeader(contactHeader);
            this.inviteTid = st;
            // Defer sending the OK to simulate the phone ringing.
            this.inviteRequest = request;

            new Timer().schedule(new MyTimerTask(this), 300);
        } catch (Exception ex) {
            logger.error(ex);
            fail(unexpectedException);
        }
    }

    private void sendInviteOK() {
        try {
            if (inviteTid.getState() != TransactionState.COMPLETED) {
                logger.info("shootme: got an Invite sending OK");
                inviteTid.sendResponse(okResponse);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {

        Request request = requestEvent.getRequest();
        try {
            logger.info("shootme:  got a bye sending OK.");
            Response response = ProtocolObjects.messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            logger.info("Dialog State is "
                    + serverTransactionId.getDialog().getState());

        } catch (Exception ex) {
            logger.error(ex);
            fail(unexpectedException);

        }
    }

    public void processCancel(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {

        Request request = requestEvent.getRequest();
        try {
            logger.info("shootme:  got a cancel.");
            if (serverTransactionId == null) {
                logger.info("shootme:  null tid.");
                return;
            }
            TestCase.assertTrue(inviteTid != serverTransactionId);
            Response response = ProtocolObjects.messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            if (dialog.getState() != DialogState.CONFIRMED) {
                response = ProtocolObjects.messageFactory.createResponse(
                        Response.REQUEST_TERMINATED, inviteRequest);
                inviteTid.sendResponse(response);
            }

        } catch (Exception ex) {
            logger.error(ex);
            fail(unexpectedException);

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

    public SipProvider createProvider() {
        try {

            ListeningPoint lp = ProtocolObjects.sipStack.createListeningPoint(myAddress,
                    myPort, transport);

            sipProvider = ProtocolObjects.sipStack.createSipProvider(lp);
            logger.info("udp provider " + sipProvider);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(ex);
            fail(unexpectedException);
            return null;

        }

    }




    public static void main(String args[])throws Exception  {
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        ProtocolObjects.init("shootme");
        Shootme shootme = new Shootme();
        shootme.createProvider();
        shootme.sipProvider.addSipListener(shootme);
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        // TODO Auto-generated method stub

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        if (transactionTerminatedEvent.isServerTransaction()) {
            ServerTransaction serverTx = transactionTerminatedEvent
                    .getServerTransaction();

            String method = serverTx.getRequest().getMethod();

            logger.info("Server Tx : " + method + " terminated ");
        }
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        // TODO Auto-generated method stub

    }

}
