/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others. 
* This software is has been contributed to the public domain. 
* As a result, a formal license is not needed to use the software.
* 
* This software is provided "AS IS."  
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* 
*/
package test.unit.gov.nist.javax.sip.stack.ackretransmission;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;

import test.tck.msgflow.callflows.ProtocolObjects;

/**
 * This class is a UAC template.
 * 
 * @author M. Ranganathan
 */

public class Shootme  implements SipListener {


	private ProtocolObjects  protocolObjects;
	
	
	// To run on two machines change these to suit.
	public static final String myAddress = "127.0.0.1";

	public static final int myPort = 5070;

	private ServerTransaction inviteTid;

	
	private static Logger logger = Logger.getLogger(Shootme.class);
	
	static{
		if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {
			
			logger.addAppender(new ConsoleAppender(new SimpleLayout()));
			
			
		}
	}
	
	private Dialog dialog;

	private ServerTransaction reSendSt = null;
	private Response reSendResponse = null;
	private int dropAckCount = 0;

	public Shootme(ProtocolObjects protocolObjects) {
		this.protocolObjects = protocolObjects;
	}
	

	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId = requestEvent
				.getServerTransaction();

		logger.info("\n\nRequest " + request.getMethod()
				+ " received at " + protocolObjects.sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.ACK)) {
			processAck(requestEvent, serverTransactionId);
		}

	}

	/**
	 * Process the ACK request. Send the bye and complete the call flow.
	 */
	public void processAck(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		logger.info("shootme: got an ACK " + requestEvent.getRequest());
		if (dropAckCount <= 3){
			boolean skip = false;
			//drop the ACK, resend 200 OK
			try {
				reSendSt.sendResponse(reSendResponse);
				logger.info("shootme: resending the previous 200");
		    }
			catch (Exception ex) {
				String s = "Unexpected error";
				logger.error(s, ex);
				AckReTransmissionTest.fail(s);
				skip = true;
			}
			if(!skip) {
				dropAckCount++;
				return;
			}
		}
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		try {
			AckReTransmissionTest.assertTrue("ACK was not successfully retransmitted 4 times", 4 == dropAckCount);
			//Create BYE request
			Request byeRequest = dialog.createRequest(Request.BYE);
			ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
			dialog.sendRequest(ct);
			reSendSt = null;
			reSendResponse = null;
			logger.info("shootme: Sending a BYE");
		} catch (Exception ex) {
			String s = "Unexpected error";
			logger.error(s,ex);
			AckReTransmissionTest.fail(s);
		}
	}

	/**
	 * Process the invite request.
	 */
	public void processInvite(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		logger.info("Got an INVITE  " + request);
		try {
			Response response = protocolObjects.messageFactory.createResponse(180, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321");
			Address address = protocolObjects.addressFactory.createAddress("Shootme <sip:"
					+ myAddress + ":" + myPort + ">");
			ContactHeader contactHeader = protocolObjects.headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);
			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
				logger.info("Server transaction created!" + request);

				logger.info("Dialog = " + st.getDialog());
			}

			byte[] content = request.getRawContent();
			if (content != null) {
				logger.info(" content = " + new String(content));
				ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
						.createContentTypeHeader("application", "sdp");
				logger.info("response = " + response);
				response.setContent(content, contentTypeHeader);
			}
			dialog = st.getDialog();
			if (dialog != null) {
				logger.info("Dialog " + dialog);
				logger.info("Dialog state " + dialog.getState());
			}
			st.sendResponse(response);
			response = protocolObjects.messageFactory.createResponse(200, request);
			toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321");
			// Application is supposed to set.
			response.addHeader(contactHeader);
			st.sendResponse(response);
			reSendSt = st;
			reSendResponse = response;
			logger.info("TxState after sendResponse = " + st.getState());
			this.inviteTid = st;
		} catch (Exception ex) {
			String s = "unexpected exception";
			
			logger.error(s,ex);
			AckReTransmissionTest.fail(s);
		}
	}

	
	public void processResponse(ResponseEvent responseReceivedEvent) {
		logger.info("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		Transaction tid = responseReceivedEvent.getClientTransaction();

		logger.info("Response received with client transaction id "
				+ tid + ":\n" + response);
		try {
			if (response.getStatusCode() == Response.OK
					&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
							.getMethod().equals(Request.INVITE)) {
				Dialog dialog = tid.getDialog();
				Request request = dialog.createRequest(Request.ACK);
				dialog.sendAck(request);
			}
			if ( tid != null ) {
			    Dialog dialog = tid.getDialog();
			    logger.info("Dialog State = " + dialog.getState());			}
		} catch (Exception ex) {			
			String s = "Unexpected exception";			
			logger.error(s,ex);
			AckReTransmissionTest.fail(s);
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

	

	
	public SipProvider createSipProvider() throws Exception {
		ListeningPoint lp = protocolObjects.sipStack.createListeningPoint(myAddress,
				myPort, protocolObjects.transport);

		
		SipProvider sipProvider = protocolObjects.sipStack.createSipProvider(lp);
		return sipProvider;
	}
	
	
	public static void main(String args[]) throws Exception	 {
		logger.addAppender( new ConsoleAppender(new SimpleLayout()));
		ProtocolObjects protocolObjects = new ProtocolObjects("shootme", "gov.nist","udp",true);
		
		Shootme shootme = new Shootme(protocolObjects);
		shootme.createSipProvider().addSipListener(shootme);
		
	}

	public void checkState() {
		AckReTransmissionTest.assertTrue("ACK was not successfully retransmitted 4 times", 4==dropAckCount);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
	 */
	public void processIOException(IOExceptionEvent exceptionEvent) {
		logger.error("An IO Exception was detected : "
				+ exceptionEvent.getHost());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
	 */
	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		logger.info("Tx terminated event ");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
	 */
	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		logger.info("Dialog terminated event detected ");

	}

}
