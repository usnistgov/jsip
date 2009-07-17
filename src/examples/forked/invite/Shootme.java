package examples.forked.invite;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
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

public class Shootme  extends TestCase implements SipListener {



    private static String transport = "udp";


    private static final String myAddress = "127.0.0.1";

    private Hashtable serverTxTable = new Hashtable();

    private SipProvider sipProvider;

    private int myPort ;

    private static String unexpectedException = "Unexpected exception ";

    private static Logger logger = Logger.getLogger(Shootme.class);

    static {
        try {
        logger.addAppender(new FileAppender(new SimpleLayout(),
                    ProtocolObjects.logFileDirectory + "shootmeconsolelog.txt"));
        //logger.addAppender( new ConsoleAppender(new SimpleLayout()));
        } catch (Exception ex) {
            throw new RuntimeException ("could not open log file");
        }
    }

    class MyTimerTask extends TimerTask {
        RequestEvent  requestEvent;
        String toTag;
        ServerTransaction serverTx;

        public MyTimerTask(RequestEvent requestEvent,ServerTransaction tx, String toTag) {
            logger.info("MyTimerTask ");
            this.requestEvent = requestEvent;
            this.toTag = toTag;
            this.serverTx = tx;

        }

        public void run() {
            sendInviteOK(requestEvent,serverTx,toTag);
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
                + " received at shootme "
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
        logger.info("processResponse " + responseEvent.getClientTransaction() +
                "\nresponse = " + responseEvent.getResponse());
    }

    /**
     * Process the ACK request. Send the bye and complete the call flow.
     */
    public void processAck(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        logger.info("shootme: got an ACK! ");
        logger.info("Dialog = " + requestEvent.getDialog());
        logger.info("Dialog State = " + requestEvent.getDialog().getState());
    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            logger.info("shootme: got an Invite sending Trying");
            // logger.info("shootme: " + request);

            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                logger.info("null server tx -- getting a new one");
                st = sipProvider.getNewServerTransaction(request);
            }

            logger.info("getNewServerTransaction : " + st);

            String txId = ((ViaHeader)request.getHeader(ViaHeader.NAME)).getBranch();
            this.serverTxTable.put(txId, st);

            // Create the 100 Trying response.
            Response response = ProtocolObjects.messageFactory.createResponse(Response.TRYING,
                    request);
                ListeningPoint lp = sipProvider.getListeningPoint("udp");
            int myPort = lp.getPort();

            Address address = ProtocolObjects.addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");

            st.sendResponse(response);

            Response ringingResponse = ProtocolObjects.messageFactory.createResponse(Response.RINGING,
                    request);
            ContactHeader contactHeader = ProtocolObjects.headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);
            ToHeader toHeader = (ToHeader) ringingResponse.getHeader(ToHeader.NAME);
            String toTag = new Integer((int) (Math.random() * 10000)).toString();
            toHeader.setTag(toTag); // Application is supposed to set.
            ringingResponse.addHeader(contactHeader);
            st.sendResponse(ringingResponse);
            Dialog dialog =  st.getDialog();
            dialog.setApplicationData(st);



            new Timer().schedule(new MyTimerTask(requestEvent,st,toTag), 100);
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
                Response okResponse = ProtocolObjects.messageFactory.createResponse(Response.OK,
                        request);
                    ListeningPoint lp = sipProvider.getListeningPoint("udp");
                int myPort = lp.getPort();

                Address address = ProtocolObjects.addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = ProtocolObjects.headerFactory
                        .createContactHeader(address);
                okResponse.addHeader(contactHeader);
                inviteTid.sendResponse(okResponse);
                logger.info("shootme: Dialog state after OK: "
                        + inviteTid.getDialog().getState());
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
            Response response = ProtocolObjects.messageFactory.createResponse(200, request);
            if ( serverTransactionId != null) {
                serverTransactionId.sendResponse(response);
            }
            logger.info("shootme:  dialogState = " + requestEvent.getDialog().getState());


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
            Response response = ProtocolObjects.messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);

            String serverTxId = ((ViaHeader)response.getHeader(ViaHeader.NAME)).getBranch();
            ServerTransaction serverTx = (ServerTransaction) this.serverTxTable.get(serverTxId);
            if ( serverTx != null && (serverTx.getState().equals(TransactionState.TRYING) ||
                    serverTx.getState().equals(TransactionState.PROCEEDING))) {
                Request originalRequest = serverTx.getRequest();
                Response resp = ProtocolObjects.messageFactory.createResponse(Response.REQUEST_TERMINATED,originalRequest);
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

    public Shootme( int myPort) {
        this.myPort = myPort;
    }

    public static void main(String args[]) throws Exception {
        int myPort = new Integer(args[0]).intValue();

        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        ProtocolObjects.init("shootme_"+myPort,true);
        Shootme shootme = new Shootme(myPort);
        shootme.createProvider();
        shootme.sipProvider.addSipListener(shootme);
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

}
