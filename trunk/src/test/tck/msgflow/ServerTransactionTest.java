package test.tck.msgflow;

import junit.framework.*;

import javax.sip.*;
import javax.sip.message.*;
import java.util.*;
import java.text.*;
import test.tck.*;

/**
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.1 Technology Compatibility Kit</p>
 * @author Emil Ivov  
 * Network Research Team, Louis Pasteur University, Strasbourg, France.
 * This  code is in the public domain.
 * @version 1.0
 * 
 */

public class ServerTransactionTest extends MessageFlowHarness {

	public ServerTransactionTest(String name) {
		super(name);
	}

	//==================== tests ==============================

	public void testSendResponse() {
		try {
			Request invite = createRiInviteRequest(null, null, null);
			SipEventCollector responseCollector = new SipEventCollector();
			//Send the initial request
			try {
				eventCollector.collectRequestEvent(tiSipProvider);
				riSipProvider.sendRequest(invite);
			} catch (SipException ex) {
				throw new TckInternalError(
					"A SipExceptionOccurred while trying to send request!",
					ex);
			} catch (TooManyListenersException ex) {
				throw new TiUnexpectedError(
					"Failed to register a SipListener with a TI SipProvider",
					ex);
			}
			waitForMessage();
			RequestEvent inviteReceivedEvent =
				eventCollector.extractCollectedRequestEvent();
			if (inviteReceivedEvent == null
				|| inviteReceivedEvent.getRequest() == null)
				throw new TiUnexpectedError("The initial invite request was not received by the TI!");
			//Let's create the transaction
			ServerTransaction tran = null;
			try {
				tran =
					tiSipProvider.getNewServerTransaction(
						inviteReceivedEvent.getRequest());
			} catch (Exception ex) {
				ex.printStackTrace();
				fail(
					ex.getClass().getName()
						+ "was thrown while trying to "
						+ "create the server transaction");
			}
			assertNotNull(
				"tiSipProvider.getNewServerTransaction() returned null",
				tran);
			//Create & send RINGING. See that it is properly sent
			Response ringing = null;
			try {
				ringing =
					tiMessageFactory.createResponse(
						Response.RINGING,
						tran.getRequest());
			} catch (ParseException ex) {
				throw new TiUnexpectedError(
					"A ParseException was thrown while trying to create a ringing "
						+ "response using TI",
					ex);
			}
			try {
				//listen for the RINGING response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
			try {
				tran.sendResponse(ringing);
			} catch (SipException ex) {
				ex.printStackTrace();
				fail("The TI failed to send a RINGING response");
			}
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}

		assertTrue(new Exception().getStackTrace()[0].toString(), true);
	}

	//==================== end of tests

	//====== STATIC JUNIT ==========
	public static Test suite() {
		return new TestSuite(ServerTransactionTest.class);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(ServerTransactionTest.class);
	}

}
