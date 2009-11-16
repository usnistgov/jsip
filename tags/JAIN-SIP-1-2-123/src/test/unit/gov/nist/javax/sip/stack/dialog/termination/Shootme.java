package test.unit.gov.nist.javax.sip.stack.dialog.termination;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.msgflow.callflows.ProtocolObjects;

import java.text.ParseException;
import java.util.*;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author baranowb
 */

public class Shootme implements SipListener {

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
                // System.out.println("shootme: got an Invite sending OK");
                Response response = messageFactory.createResponse(180, request);
                ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                Address address = addressFactory.createAddress("Shootme <sip:" + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory.createContactHeader(address);
                response.addHeader(contactHeader);

                // System.out.println("got a server tranasaction " + st);
                Dialog dialog = st.getDialog();

                st.sendResponse(response); // send 180(RING)
                response = messageFactory.createResponse(200, request);
                toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                String toTag = new Integer((int) (Math.random() * 100000)).toString()+"_ResponseCode_"+responseCodeToINFO;
                toHeader.setTag(toTag); // Application is supposed to set.
                response.addHeader(contactHeader);

                st.sendResponse(response);// send 200(OK)

            } catch (Exception ex) {
                ex.printStackTrace();
                DialogTerminationOn50XTest.fail("Shootme: Failed in timer task!!!", ex);
            }

        }

    }


    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;


    private boolean stateIsOk = true;

    private ProtocolObjects protocolObjects;

    private int responseCodeToINFO = 500;

    // To run on two machines change these to suit.
    public static final String myAddress = "127.0.0.1";

    public static final int myPort = 5070;

    private static Logger logger = Logger.getLogger(Shootme.class);

    static {
        if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {

            logger.addAppender(new ConsoleAppender(new SimpleLayout()));

        }
    }

    public Shootme(ProtocolObjects protocolObjects) {
        this.protocolObjects = protocolObjects;
    }

    public boolean checkState() {

        return stateIsOk;
    }

    public SipProvider createSipProvider() throws Exception {
        ListeningPoint lp = protocolObjects.sipStack.createListeningPoint(myAddress, myPort, protocolObjects.transport);

        SipProvider sipProvider = protocolObjects.sipStack.createSipProvider(lp);
        return sipProvider;
    }

    public void init() {

        headerFactory = protocolObjects.headerFactory;
        addressFactory = protocolObjects.addressFactory;
        messageFactory = protocolObjects.messageFactory;

    }

    /**
     * Process the ACK request. Send the bye and complete the call flow.
     */
    public void processAck(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        try {
            // System.out.println("*** shootme: got an ACK "
            // + requestEvent.getRequest());
            if (serverTransaction == null) {
                System.out.println("null server transaction -- ignoring the ACK!");
                return;
            }
            Dialog dialog = serverTransaction.getDialog();

            System.out.println("Dialog Created = " + dialog.getDialogId() + " Dialog State = " + dialog.getState());

            System.out.println("Waiting for INFO");

        } catch (Exception ex) {
            ex.printStackTrace();
            DialogTerminationOn50XTest.fail("Shootme: Failed on process ACK", ex);
        }
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        Dialog dialog = dialogTerminatedEvent.getDialog();

        System.out.println("Dialog Terminated Event " + dialog.getDialogId());
        if ( this.responseCodeToINFO>=300)
        {
            DialogTerminationOn50XTest.fail("Shootme: Got DialogTerinatedEvent, this shouldnt happen");
            stateIsOk=false;
        }

    }

    public void processInfo(RequestEvent requestEvent) {
        try {
            Response info500Response = messageFactory.createResponse(this.responseCodeToINFO, requestEvent.getRequest());
            requestEvent.getServerTransaction().sendResponse(info500Response);
        } catch (Exception e) {

            e.printStackTrace();
            DialogTerminationOn50XTest.fail("Shootme: Failed on process INFO", e);
        }

    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        try {
            // System.out.println("ProcessInvite");
            Request request = requestEvent.getRequest();
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            // Note you need to create the Server Transaction
            // before the listener returns but you can delay sending the
            // response

            ServerTransaction st = sipProvider.getNewServerTransaction(request);

            TTask ttask = new TTask(requestEvent, st);
            int ttime = 100;

            new Timer().schedule(ttask, ttime);
        } catch (Exception ex) {
            ex.printStackTrace();
            DialogTerminationOn50XTest.fail("Shootme: Failed on process INVITE", ex);
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException event");
        DialogTerminationOn50XTest.fail("Got IOException event");
    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

        System.out.println("GOT REQUEST: " + request.getMethod());

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.INFO)) {
            processInfo(requestEvent);
        }

    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        // System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        // System.out.println("Response received with client transaction id "
        // + tid + ":\n" + response);

        System.out.println("GOT RESPONSE: " + response.getStatusCode());
        try {
            if (response.getStatusCode() == Response.OK && ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod().equals(Request.INVITE)) {

                Dialog dialog = tid.getDialog();
                Request request = tid.getRequest();
                dialog.sendAck(request);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            DialogTerminationOn50XTest.fail("Shootme: Failed on process response: " + response.getStatusCode(), ex);
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
         * System.out.println("Transaction Time out" +
         * transaction.getBranchId());
         */

    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        // System.out.println("TransactionTerminatedEvent");
    }

    public void setResponseCodeToINFO(int responseCodeToINFO) {
        this.responseCodeToINFO = responseCodeToINFO;

    }

}
