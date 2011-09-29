package test.unit.gov.nist.javax.sip.stack.timeoutontermineted;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */

public class Shootme implements SipListener {

    private static final String myAddress = "127.0.0.1";


    private SipProvider sipProvider;

    private final int myPort;

    private static String unexpectedException = "Unexpected exception ";

    private static Logger logger = Logger.getLogger(Shootme.class);

    private boolean inviteSeen;

    private final SipStack sipStack;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static final String transport = "udp";

    private static Timer timer = new Timer();
    private boolean seen_txTerm, seen_txTimeout, seen_dte;

    private ServerTransaction inviteTid;

    private Dialog inviteDialog;


    private final int delay;

    class MyTimerTask extends TimerTask {
        RequestEvent requestEvent;
        String toTag;
        ServerTransaction serverTx;

        public MyTimerTask(RequestEvent requestEvent, ServerTransaction tx, String toTag) {
            logger.info("MyTimerTask ");
            this.requestEvent = requestEvent;
            this.toTag = toTag;
            this.serverTx = tx;

        }

        @Override
        public void run() {
            sendInviteOK(requestEvent, serverTx, toTag);
        }

    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod() + " received at " + sipStack.getStackName() + " with server transaction id " + serverTransactionId);

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        }

    }

    public void processResponse(ResponseEvent responseEvent) {
    }



    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            logger.info("shootme: got an Invite sending Trying");
            // logger.info("shootme: " + request);

            ServerTransaction st = requestEvent.getServerTransaction();
            inviteTid = st;
            if (st == null) {
                logger.info("null server tx -- getting a new one");
                st = sipProvider.getNewServerTransaction(request);
            }

            logger.info("getNewServerTransaction : " + st);



            // Create the 100 Trying response.
            Response response = messageFactory.createResponse(Response.TRYING, request);
            ListeningPoint lp = sipProvider.getListeningPoint(transport);
            int myPort = lp.getPort();

            Address address = addressFactory.createAddress("Shootme <sip:" + myAddress + ":" + myPort + ">");

            // Add a random sleep to stagger the two OK's for the benifit of
            // implementations
            // that may not be too good about handling re-entrancy.
            int timeToSleep = (int) (Math.random() * 1000);

            Thread.sleep(timeToSleep);

            st.sendResponse(response);


            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);

            String toTag = new Integer(new Random().nextInt()).toString();

            Dialog dialog = st.getDialog();
            inviteDialog = dialog;
            inviteDialog.terminateOnBye(true);
            dialog.setApplicationData(st);

            this.inviteSeen = true;

            timer.schedule(new MyTimerTask(requestEvent, st, toTag), this.delay);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void sendInviteOK(RequestEvent requestEvent, ServerTransaction inviteTid, String toTag) {
        try {
            logger.info("sendInviteOK: " + inviteTid);
            if (inviteTid.getState() == TransactionState.PROCEEDING) {
                logger.info("shootme: Dialog state before OK: " + inviteTid.getDialog().getState());
                System.err.println("shootme: Dialog state before OK: " + inviteTid.getDialog().getState());
                SipProvider sipProvider = (SipProvider) requestEvent.getSource();
                Request request = requestEvent.getRequest();
                Response okResponse = messageFactory.createResponse(Response.OK, request);
                ListeningPoint lp = sipProvider.getListeningPoint(transport);
                int myPort = lp.getPort();

                ((ToHeader) okResponse.getHeader(ToHeader.NAME)).setTag(toTag);

                Address address = addressFactory.createAddress("Shootme <sip:" + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory.createContactHeader(address);
                okResponse.addHeader(contactHeader);
                inviteTid.sendResponse(okResponse);
                logger.info("shootme: Dialog state after OK: " + inviteTid.getDialog().getState());
                TestCase.assertEquals(DialogState.CONFIRMED, inviteTid.getDialog().getState());
            } else {
                logger.info("semdInviteOK: inviteTid = " + inviteTid + " state = " + inviteTid.getState());
                System.err.println("sentInviteOK: inviteTid = " + inviteTid + " state = " + inviteTid.getState());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("IOException happened for " + exceptionEvent.getHost() + " port = " + exceptionEvent.getPort());
        TestCase.fail("Unexpected exception");

    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("[s]Transaction terminated event recieved: "+transactionTerminatedEvent.getServerTransaction());
        System.err.println("[s]Transaction terminated event recieved: "+transactionTerminatedEvent.getServerTransaction());
        //if (transactionTerminatedEvent.getClientTransaction() == inviteTid)
            seen_txTerm = true;
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        logger.info("[s]Transaction timedout event recieved: "+timeoutEvent.getServerTransaction());
        System.err.println("[s]Transaction timedout event recieved: "+timeoutEvent.getServerTransaction());
        //if (timeoutEvent.getClientTransaction() == inviteTid)
            seen_txTimeout = true;
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("[s]Dialog terminated event recieved: "+dialogTerminatedEvent.getDialog());
        System.err.println("[s]Dialog terminated event recieved: "+dialogTerminatedEvent.getDialog());
        //if (dialogTerminatedEvent.getDialog() == inviteDialog)
            seen_dte = true;

    }

    public SipProvider createProvider() {
        try {

            ListeningPoint lp = sipStack.createListeningPoint(myAddress, myPort, transport);

            sipProvider = sipStack.createSipProvider(lp);
            logger.info("provider " + sipProvider);
            logger.info("sipStack = " + sipStack);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(ex);
            TestCase.fail(unexpectedException);
            return null;

        }

    }

    public Shootme(int myPort, int delay ) {
        this.myPort = myPort;
        this.delay = delay;


        SipObjects sipObjects = new SipObjects(myPort, "shootme", "on");
        addressFactory = sipObjects.addressFactory;
        messageFactory = sipObjects.messageFactory;
        headerFactory = sipObjects.headerFactory;
        this.sipStack = sipObjects.sipStack;
    }

    public void checkState() {
        TestCase.assertTrue("INVTE must be observed",inviteSeen);
        TestCase.assertTrue("INVITE transaction should temrinate.", seen_txTerm);
        TestCase.assertFalse("INVITE transaction should not timeout.", seen_txTimeout);
        TestCase.assertTrue("INVITE dialog should die.", seen_dte);

    }

    public void stop() {
        this.sipStack.stop();
    }


}
