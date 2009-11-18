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
package test.unit.gov.nist.javax.sip.stack.dialog.timeout;

import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.DialogTimeoutEvent.Reason;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.msgflow.callflows.ProtocolObjects;

/**
 * This class sends an INVITE and upon receiving a 200 OK it doesn't send the ACK to test is the Dialog Timeout Event is correctly passed to the application
 * The timeout Reason should be ACK not sent
 *
 * @author jean deruelle
 */

public class Shootist implements SipListenerExt {

    private ListeningPoint listeningPoint;
    private ProtocolObjects protocolObjects;
    /* move variables as class variables from init() */
    private SipURI requestURI;

    private CSeqHeader cSeqHeader;

    private FromHeader fromHeader;

    private ToHeader toHeader;

    private MaxForwardsHeader maxForwards;

    private SipProvider sipProvider;

    private Address fromNameAddress;

    private ContentTypeHeader contentTypeHeader;

    private ContactHeader contactHeader;
    // If you want to try TCP transport change the following to
    // String transport = "tcp";
    String transport = "udp";

    private HeaderFactory headerFactory;

    private AddressFactory addressFactory;

    private MessageFactory messageFactory;

    private static String PEER_ADDRESS = Shootme.myAddress;

    private static int PEER_PORT = Shootme.myPort;

    private static String peerHostPort = PEER_ADDRESS + ":" + PEER_PORT;

    // To run on two machines change these to suit.
    public static final String myAddress = "127.0.0.1";

    private static final int myPort = 5060;

    private boolean stateIsOk = false;
    
    private boolean sendByeOnDialogTimeout = false;
    
    private Dialog dialog = null;

    private static Logger logger = Logger.getLogger(Shootist.class);

    static {
        if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {

            logger.addAppender(new ConsoleAppender(new SimpleLayout()));

        }
    }

    class ByeTask  extends TimerTask {
        Dialog dialog;
        public ByeTask(Dialog dialog)  {
            this.dialog = dialog;
        }
        public void run () {
            try {
               Request byeRequest = this.dialog.createRequest(Request.BYE);
               ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
               dialog.sendRequest(ct);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
            }

        }
    }
    
    public Shootist(ProtocolObjects protocolObjects) {
        super();
        this.protocolObjects = protocolObjects;
        stateIsOk = protocolObjects.autoDialog;
    }

    public boolean checkState() {

        return stateIsOk;
    }

    public SipProvider createSipProvider() {
        try {
            listeningPoint = protocolObjects.sipStack.createListeningPoint(
                    myAddress, myPort, protocolObjects.transport);

            sipProvider = protocolObjects.sipStack
                    .createSipProvider(listeningPoint);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(ex);
            DialogTimeoutTest
                    .fail("Shootist: unable to create provider");
            return null;
        }
    }

    public void init() {
        SipFactory sipFactory = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        /* remote peer host */
        String peerHostPort = Shootist.peerHostPort;
        String localHost = myAddress;

        try {
            headerFactory = protocolObjects.headerFactory;
            addressFactory = protocolObjects.addressFactory;
            messageFactory = protocolObjects.messageFactory;

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            toHeader = headerFactory.createToHeader(toNameAddress, null);

            // create Request URI
            requestURI = addressFactory.createSipURI(toUser, peerHostPort);

            // Create ContentTypeHeader
            contentTypeHeader = headerFactory.createContentTypeHeader(
                    "application", "sdp");

            // Create a new MaxForwardsHeader
            maxForwards = headerFactory.createMaxForwardsHeader(70);

            // Create contact headers
            String host = localHost;

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(listeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(listeningPoint.getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            DialogTimeoutTest.fail("Shootist: Error on init!", ex);
        }
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
    	if(sendByeOnDialogTimeout) {
    		stateIsOk = true;
    		return;
    	}
    	if(((SipStackImpl)protocolObjects.sipStack).isBackToBackUserAgent()) {
    		stateIsOk = true;
    		return;
    	}
    	if(!protocolObjects.autoDialog) {
    		stateIsOk = false;
    		DialogTimeoutTest.fail("This shouldn't be called since a dialogtimeout event should be passed to the application instead!");
    	} else {
    		stateIsOk = true;
    	}
    	
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("An IO Exception occured!");
        DialogTimeoutTest.fail("An IO Exception occured!");

    }

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        System.out.println("GOT REQUEST (we shouldnt get that): "
                + request.getMethod());
        DialogTimeoutTest.fail("Shouldnt receive any request:\n"
                + request);

    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        Response response = (Response) responseReceivedEvent.getResponse();
        System.out.println("GOT RESPONSE:" + response.getStatusCode());
        if(responseReceivedEvent.getClientTransaction() == null) {
        	return;
        }
        try {
            if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {            	
            	System.out.println("Not Sending ACK to test dialog timeout");            	
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            DialogTimeoutTest.fail(
                    "Shootist: Exception on process respons/send info", ex);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
    	if(protocolObjects.autoDialog) {
    		DialogTimeoutTest.fail(
            	"Shootist: Exception on timeout, event shouldn't be thrown on automatic dailog creation by the stack");
    	}    	
    }
    
    public void processDialogTimeout(DialogTimeoutEvent timeoutEvent) {    	
		DialogTimeoutEvent dialogTimeoutEvent = (DialogTimeoutEvent)timeoutEvent;
		Dialog timeoutDialog = dialogTimeoutEvent.getDialog();
		if(timeoutDialog == null){
			DialogTimeoutTest.fail(
                    "Shootist: Exception on timeout, dialog shouldn't be null");
		}
		if(dialogTimeoutEvent.getReason() == Reason.AckNotSent) {
			if(sendByeOnDialogTimeout) {
				new Timer().schedule(new ByeTask(dialog), 4000) ;
			} else {
				stateIsOk = true;
			}
		}
	}
    

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // System.out.println("TransactionTerminated event notification");
    }

    void sendInviteRequest() {
        System.out.println("====Send INVITE");
        try {
            cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            int fromTag = 1000 + hashCode();

            fromHeader = headerFactory.createFromHeader(fromNameAddress,
                    new Integer(fromTag).toString());
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
            // Create ViaHeaders
            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader(myAddress,
                    listeningPoint.getPort(), transport, null);

            // add via headers
            viaHeaders.add(viaHeader);
            Request request = messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);

            request.setHeader(contactHeader);
            request.setContent(sdpData, contentTypeHeader);
            // Create the client transaction.
            ClientTransaction inviteTid = sipProvider
                    .getNewClientTransaction(request);
            if(!protocolObjects.autoDialog) {
            	dialog = sipProvider.getNewDialog(inviteTid);
            }
            System.out.println("inviteTid = " + inviteTid + " sipDialog = "
                    + inviteTid.getDialog());

            // send the request out.
            inviteTid.sendRequest();
        } catch (Exception ex) {
            System.out.println("Fail to sendInviteRequest with SipException:\n"
                    + ex.getMessage());
            DialogTimeoutTest.fail(
                    "Shootist: Failed to send invite: ", ex);

        }
        return;
    }

	/**
	 * @param sendByeOnDialogTimeout the sendByeOnDialogTimeout to set
	 */
	public void setSendByeOnDialogTimeout(boolean sendByeOnDialogTimeout) {
		this.sendByeOnDialogTimeout = sendByeOnDialogTimeout;
	}

	/**
	 * @return the sendByeOnDialogTimeout
	 */
	public boolean isSendByeOnDialogTimeout() {
		return sendByeOnDialogTimeout;
	}

}
