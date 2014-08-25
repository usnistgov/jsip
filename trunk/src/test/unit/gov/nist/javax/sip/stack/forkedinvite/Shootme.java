package test.unit.gov.nist.javax.sip.stack.forkedinvite;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.header.HeaderExt;
import gov.nist.javax.sip.header.ims.PPreferredServiceHeader;

import java.util.Hashtable;
import java.util.Properties;
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
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;



/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author M. Ranganathan
 */

public class Shootme   implements SipListener {




    private static final String myAddress = "127.0.0.1";

    private Hashtable serverTxTable = new Hashtable();

    private SipProvider sipProvider;

    private int myPort ;

    private static String unexpectedException = "Unexpected exception ";

    private static Logger logger = Logger.getLogger(Shootme.class);

    static {
        if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {

            logger.addAppender(new ConsoleAppender(new SimpleLayout()));

        }
    }
    
    private boolean inviteSeen;


    private boolean byeSeen;

    private boolean ackSeen;


    private SipStack sipStack;

    private int ringingDelay;

    private int okDelay;

    private boolean sendRinging;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static final String transport = "udp";

    private static Timer timer = new Timer();


    class MyTimerTask extends TimerTask {
        RequestEvent  requestEvent;
        String toTag;
        ServerTransaction serverTx;

        public MyTimerTask(RequestEvent requestEvent,  ServerTransaction tx, String toTag) {
            logger.info("MyTimerTask ");
            this.requestEvent = requestEvent;
            this.toTag = toTag;
            this.serverTx = tx;

        }

        public void run() {
            sendInviteOK(requestEvent,serverTx,toTag);
        }

    }



    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
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
        logger.info("shootme: got an ACK! ");
        logger.info("Dialog = " + requestEvent.getDialog());
        logger.info("Dialog State = " + requestEvent.getDialog().getState());

        this.ackSeen = true;
    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            logger.info("shootme: got an Invite "+ request +"sending Trying");
            // logger.info("shootme: " + request);

            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                logger.info("null server tx -- getting a new one");
                st = sipProvider.getNewServerTransaction(request);
            }

            logger.info("getNewServerTransaction : " + st);

            // https://java.net/jira/browse/JSIP-476 Non regression test 
            if(request.getContentLength().getContentLength() < 1) {
            	throw new Exception("Content Length shouldn't be lower than zero, bad parsing occured along the way");
            }
            
            String value = ((HeaderExt)request.getHeader(PPreferredServiceHeader.NAME)).getValue();
            if(value == null || !value.equalsIgnoreCase(InviteTest.PREFERRED_SERVICE_VALUE)) {
            	throw new Exception("Bad value " + value);
            }
            String txId = ((ViaHeader)request.getHeader(ViaHeader.NAME)).getBranch();
            this.serverTxTable.put(txId, st);

            // Create the 100 Trying response.
//            Response response = messageFactory.createResponse(Response.TRYING,
//                    request);
//                ListeningPoint lp = sipProvider.getListeningPoint(transport);
//            int myPort = lp.getPort();
//
            Address address = addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
//
//            // Add a random sleep to stagger the two OK's for the benifit of implementations
//            // that may not be too good about handling re-entrancy.
//            int timeToSleep = (int) ( Math.random() * 1000);
//
//            Thread.sleep(timeToSleep);
//
//            st.sendResponse(response);

            Response ringingResponse = messageFactory.createResponse(Response.RINGING,
                    request);
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            ringingResponse.addHeader(contactHeader);
            ToHeader toHeader = (ToHeader) ringingResponse.getHeader(ToHeader.NAME);
            String toTag =  "shootme-" + myPort + "-" + new Integer(new Random().nextInt()).toString();             
            toHeader.setTag(toTag);
            if ( sendRinging ) {
                ringingResponse.addHeader(contactHeader);
                Thread.sleep(this.ringingDelay / 2);
                st.sendResponse(ringingResponse);
            }
            Dialog dialog =  st.getDialog();
            dialog.setApplicationData(st);

            this.inviteSeen = true;

            timer.schedule(new MyTimerTask(requestEvent,st,toTag), this.okDelay);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void sendInviteOK(RequestEvent requestEvent, ServerTransaction inviteTid, String toTag) {
        try {
            logger.info("sendInviteOK: " + inviteTid);
            if (inviteTid.getState() != TransactionState.COMPLETED) {
                logger.info("shootme: Dialog state before OK: "
                        + inviteTid.getDialog().getState());

                SipProvider sipProvider = (SipProvider) requestEvent.getSource();
                Request request = requestEvent.getRequest();
                Response okResponse = messageFactory.createResponse(Response.OK,
                        request);
                    ListeningPoint lp = sipProvider.getListeningPoint(transport);
                int myPort = lp.getPort();


                ((ToHeader)okResponse.getHeader(ToHeader.NAME)).setTag(toTag);


                Address address = addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                okResponse.addHeader(contactHeader);
                inviteTid.sendResponse(okResponse);
                logger.info("shootme: Dialog state after OK: "
                        + inviteTid.getDialog().getState());
                TestCase.assertEquals( DialogState.CONFIRMED , inviteTid.getDialog().getState() );
            } else {
                logger.info("semdInviteOK: inviteTid = " + inviteTid + " state = " + inviteTid.getState());
            }
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
            logger.info("shootme:  got a bye sending OK.");
            logger.info("shootme:  dialog = " + requestEvent.getDialog());
            logger.info("shootme:  dialogState = " + requestEvent.getDialog().getState());
            Response response = messageFactory.createResponse(200, request);
            if ( serverTransactionId != null) {
                serverTransactionId.sendResponse(response);
            }
            logger.info("shootme:  dialogState = " + requestEvent.getDialog().getState());

            this.byeSeen = true;


        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processCancel(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        Request request = requestEvent.getRequest();
        SipProvider sipProvider = (SipProvider)requestEvent.getSource();
        try {
            logger.info("shootme:  got a cancel. " );
            // Because this is not an In-dialog request, you will get a null server Tx id here.
            if (serverTransactionId == null) {
                serverTransactionId = sipProvider.getNewServerTransaction(request);
            }
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);

            String serverTxId = ((ViaHeader)response.getHeader(ViaHeader.NAME)).getBranch();
            ServerTransaction serverTx = (ServerTransaction) this.serverTxTable.get(serverTxId);
            if ( serverTx != null && (serverTx.getState().equals(TransactionState.TRYING) ||
                    serverTx.getState().equals(TransactionState.PROCEEDING))) {
                Request originalRequest = serverTx.getRequest();
                Response resp = messageFactory.createResponse(Response.REQUEST_TERMINATED,originalRequest);
                serverTx.sendResponse(resp);
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
        logger.info("state = " + transaction.getState());
        logger.info("dialog = " + transaction.getDialog());
        logger.info("dialogState = "
                + transaction.getDialog().getState());
        logger.info("Transaction Time out");
    }

    public SipProvider createProvider() {
        try {

            ListeningPoint lp = sipStack.createListeningPoint(myAddress,
                    myPort, transport);

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

    public Shootme( int myPort, boolean sendRinging, int ringingDelay, int okDelay ) {
        this.myPort = myPort;
        this.ringingDelay = ringingDelay;
        this.okDelay = okDelay;
        this.sendRinging = sendRinging;

        SipObjects sipObjects = new SipObjects(myPort, "shootme","on");
        addressFactory = sipObjects.addressFactory;
        messageFactory = sipObjects.messageFactory;
        headerFactory = sipObjects.headerFactory;
        this.sipStack = sipObjects.sipStack;
    }



    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("IOException");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Transaction terminated event recieved");

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("Dialog terminated event recieved");

    }

    public void checkState() {
        TestCase.assertTrue("Should see invite", inviteSeen);

        TestCase.assertTrue("Should see BYE",byeSeen );

    }

    public boolean checkBye() {
        return this.byeSeen;
    }

    public void stop() {
        this.sipStack.stop();
    }


    /**
     * @return the ackSeen
     *
     * Exactly one Dialog must get an ACK.
     */
    public boolean isAckSeen() {
        return ackSeen;
    }



}
