/*
 * This source code has been contributed to the public domain by Mobicents
 *
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 *
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package test.unit.gov.nist.javax.sip.stack.tx.timeout;

import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.DialogTimeoutEvent.Reason;
import gov.nist.javax.sip.stack.SIPDialog;

import java.util.Timer;
import java.util.TimerTask;

import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.Timeout;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.msgflow.callflows.ProtocolObjects;

/**
 * This class receives an INVITE and sends a 180 and a 200 OK, the Shootist will not send the ACK to test is the Dialog Timeout Event is correctly passed to the application.
 * The timeout Reason should be ACK not received 
 *
 * @author jean deruelle
 */

public class Shootme implements SipListenerExt {

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
            	String toTag = new Integer((int) (Math.random() * 100000)).toString()+"_ResponseCode_"+responseCodeToINFO;
                // System.out.prntln("shootme: got an Invite sending OK");
            	int statusCode = 180;            	
                Response response = messageFactory.createResponse(statusCode, request);
                ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                toHeader.setTag(toTag);
                Address address = addressFactory.createAddress("Shootme <sip:" + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory.createContactHeader(address);
                response.addHeader(contactHeader);
                
                if(!protocolObjects.autoDialog) {
                	((SipProvider)requestEvent.getSource()).getNewDialog(st);
                	st.getDialog().setApplicationData("some junk");
                }
                // System.out.println("got a server tranasaction " + st);
                st.sendResponse(response); // send 180(RING)
                if(sendOK) {
	                response = messageFactory.createResponse(200, request);
	                toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
	               
	                toHeader.setTag(toTag); // Application is supposed to set.                                
	                
	                response.addHeader(contactHeader);
	
	                st.sendResponse(response);// send 200(OK)
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                TxTimeoutTest.fail("Shootme: Failed in timer task!!!", ex);
            }

        }

    }


    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private boolean stateIsOk = false;
    
    private boolean receiveBye = false;

    private ProtocolObjects protocolObjects;

    private int responseCodeToINFO = 500;

	private boolean sendOK = true;

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
        stateIsOk = protocolObjects.autoDialog;
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
    
    public void init(boolean sendOK) {
		this.init();
		this.sendOK= sendOK;
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
            TxTimeoutTest.fail("Shootme: Failed on process ACK", ex);
        }
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {        
    	if(!protocolObjects.autoDialog && !receiveBye) {
    		stateIsOk = false;
    		TxTimeoutTest.fail("This shouldn't be called since a dialogtimeout event should be passed to the application instead!");
    	} else {
    		stateIsOk = true;
    	}
    	TimerTask timerTask = new CheckAppData(dialogTerminatedEvent.getDialog());
        new Timer().schedule(timerTask, 15000);
    }

    public void processInfo(RequestEvent requestEvent) {
        try {
            Response info500Response = messageFactory.createResponse(this.responseCodeToINFO, requestEvent.getRequest());
            requestEvent.getServerTransaction().sendResponse(info500Response);
        } catch (Exception e) {

            e.printStackTrace();
            TxTimeoutTest.fail("Shootme: Failed on process INFO", e);
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
            TxTimeoutTest.fail("Shootme: Failed on process INVITE", ex);
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException event");
        TxTimeoutTest.fail("Got IOException event");
    }

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

        System.out.println("GOT REQUEST: " + request.getMethod());

        if (request.getMethod().equals(Request.INVITE) || request.getMethod().equals(Request.MESSAGE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.INFO)) {
            processInfo(requestEvent);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent, serverTransactionId);
        } 

    }
    
    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        Dialog dialog = requestEvent.getDialog();
        System.out.println("local party = " + dialog.getLocalParty());
        try {
            System.out.println("shootme:  got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is "
                    + serverTransactionId.getDialog().getState());

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

        System.out.println("GOT RESPONSE: " + response.getStatusCode());
        try {
            if (response.getStatusCode() == Response.OK && ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod().equals(Request.INVITE)) {

                Dialog dialog = tid.getDialog();
                
                Request request = tid.getRequest();
                dialog.sendAck(request);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            TxTimeoutTest.fail("Shootme: Failed on process response: " + response.getStatusCode(), ex);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
    	
    	System.out.println("Timeout event received : " + timeoutEvent);
    	
    	if(timeoutEvent.getTimeout() == Timeout.TRANSACTION) {
    		stateIsOk = true;
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
    
    public void processDialogTimeout(DialogTimeoutEvent timeoutEvent) {
        System.out.println("processDialogTerminated " + timeoutEvent.getDialog());
        
        DialogTimeoutEvent dialogAckTimeoutEvent = (DialogTimeoutEvent)timeoutEvent;
        Dialog timeoutDialog = dialogAckTimeoutEvent.getDialog();
        if(timeoutDialog == null){
            TxTimeoutTest.fail(
                    "Shootist: Exception on timeout, dialog shouldn't be null");
            stateIsOk = false;
            return;
        }        
        if(dialogAckTimeoutEvent.getReason() == Reason.AckNotReceived) {
            stateIsOk = true;
        }
        if(dialogAckTimeoutEvent.getReason() == Reason.EarlyStateTimeout && !sendOK) {
            stateIsOk = true;
        }
        TimerTask timerTask = new CheckAppData(timeoutDialog);
        new Timer().schedule(timerTask, 9000);
	}

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        // System.out.println("TransactionTerminatedEvent");
    }

    public void setResponseCodeToINFO(int responseCodeToINFO) {
        this.responseCodeToINFO = responseCodeToINFO;

    }

	/**
	 * @param receiveBye the receiveBye to set
	 */
	public void setReceiveBye(boolean receiveBye) {
		this.receiveBye = receiveBye;
	}

	/**
	 * @return the receiveBye
	 */
	public boolean isReceiveBye() {
		return receiveBye;
	}

	class CheckAppData extends TimerTask {
	    Dialog dialog;
	    
	    public CheckAppData(Dialog dialog) {
            this.dialog = dialog;
        }
	    
        public void run() {             
            System.out.println("Checking app data " + dialog.getApplicationData());
            if(dialog.getApplicationData() == null || !dialog.getApplicationData().equals("some junk")) {
                stateIsOk = false;
                TxTimeoutTest.fail("application data should never be null except if nullified by the application !");
            }            
        }
	}

}
