package test.tck.msgflow;

import junit.framework.*;

import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import java.util.*;
import java.text.*;
import test.tck.*;

/**
 *
 *  * The test tries to verify that Invite Server Transactions correctly change
 * states as specified by the rfc3261. The Reference Implementation is used
 * to send requests and a Tested Implementation ServerTransaction's states are
 * queried and compared to those in the state machine described in
 * section 17.2.1 of rfc3261
 *
 *<pre>
 *
 *                              |INVITE
 *                              |pass INV to TU
 *            INVITE             V send 100 if TU won't in 200ms
 *            send response+-----------+
 *                +--------|           |--------+101-199 from TU
 *                |        | Proceeding|        |send response
 *                +------->|           |<-------+
 *                         |           |          Transport Err.
 *                         |           |          Inform TU
 *                         |           |--------------->+
 *                         +-----------+                |
 *            300-699 from TU |     |2xx from TU        |
 *            send response   |     |send response      |
 *                            |     +------------------>+
 *                            |                         |
 *            INVITE          V          Timer G fires  |
 *            send response+-----------+ send response  |
 *                +--------|           |--------+       |
 *                |        | Completed |        |       |
 *                +------->|           |<-------+       |
 *                         +-----------+                |
 *                            |     |                   |
 *                        ACK |     |                   |
 *                        -   |     +------------------>+
 *                            |        Timer H fires    |
 *                            V        or Transport Err.|
 *                         +-----------+  Inform TU     |
 *                         |           |                |
 *                         | Confirmed |                |
 *                         |           |                |
 *                         +-----------+                |
 *                               |                      |
 *                               |Timer I fires         |
 *                               |-                     |
 *                               |                      |
 *                               V                      |
 *                         +-----------+                |
 *                         |           |                |
 *                         | Terminated|<---------------+
 *                         |           |
 *                         +-----------+
 *
 *              Figure 7: INVITE server transaction
 *
 *</pre>
 *
 * @author Emil Ivov
 *   Network Research Team, Louis Pasteur University, Strasbourg, France.
 * This  code is in the public domain.
 * @version 1.0
 */

public class InviteServerTransactionsStateMachineTest
	extends MessageFlowHarness {

	public InviteServerTransactionsStateMachineTest(String name) {
		super(name);
	}

	//==================== tests ==============================
	/**
	 * Tries to steer a TI server transaction through the following scenario
	 * Proceeding-->Completed-->Confirmed-->Terminated. Apart from state
	 * transitions, we also test, retransmissions and proper hiding/passing
	 * of messages to the TU.
	 */
	public void testProceedingCompletedConfirmedScenario() {
		try {
			Request invite = createRiInviteRequest(null, null, null);
			SipEventCollector responseCollector = new SipEventCollector();
			//Before Sending the request we should first register a listener with the
			//RI that would catch the TRYING response
			try {
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
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
			//Check whether a TRYING response has been sent.
			//wait for the trying response
			waitForMessage();
			// At this point state must be PROCEEDING
			assertEquals(TransactionState.PROCEEDING, tran.getState());
			ResponseEvent responseEvent =
				responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"No TRYING response has been sent by the TI upon reception "
					+ "of an INVITE request",
				responseEvent);
			assertTrue(
				"A response different from 100 was sent by the TI upon "
					+ "reception of INVITE",
				Response.TRYING == responseEvent.getResponse().getStatusCode());
			//Resend the invite and see that a TRYING response is resent
			try {
				//listen for the Trying response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
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
			//Wait for the INVITE
			waitForMessage();
			inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
			assertNull(
				"Retransmitted INVITEs should not be passed to the TU",
				inviteReceivedEvent);
			//Wait for a retransmitted TRYING response
			waitForMessage();
			//Verify whether there was a TRYING response
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"No TRYING response has been sent by the TI upon reception "
					+ "of an INVITE request",
				responseEvent);
			assertTrue(
				"A response different from 100 was sent by the TI upon "
					+ "reception of INVITE",
				Response.TRYING == responseEvent.getResponse().getStatusCode());
			//Create & send RINGING. See that it is properly sent
			//and that tran state doesn't change
			Response ringing = null;
			try {
				ringing =
					tiMessageFactory.createResponse(
						Response.RINGING,
						tran.getRequest());
				((ToHeader) ringing.getHeader(ToHeader.NAME)).setTag(
					Integer.toString(hashCode()));
				addStatus(tran.getRequest(), ringing);
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
			//The Transaction should still be PROCEEDING
			assertEquals(
				"The Transaction did not remain PROCEEDING after transmitting a RINGING response",
				TransactionState.PROCEEDING,
				tran.getState());
			//Check whether the RINGING is received by the RI.
			waitForMessage();
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"The RINGING response was not received by the RI",
				responseEvent);
			assertTrue(
				"A response different from RINGING was sent by the TI",
				Response.RINGING
					== responseEvent.getResponse().getStatusCode());
			//Resend the INVITE, see that it is hidden from the TU and see that
			//the _RINGING_ response is resent (and not the TRYING)
			try {
				//listen for the Trying response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
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
			//Wait for the INVITE
			waitForMessage();
			inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
			assertNull(
				"Retransmitted INVITEs should not be passed to the TU",
				inviteReceivedEvent);
			//Wait for a retransmitted RINGING response
			waitForMessage();
			//Verify whether there was a RINGING response
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"No RINGING response has been sent by the TI upon reception "
					+ "of an INVITE request",
				responseEvent);
			assertTrue(
				"A response different from RINGING was sent by the TI upon "
					+ "reception of a retransmitted invite INVITE",
				Response.RINGING
					== responseEvent.getResponse().getStatusCode());
			//We should still be proceeding
			assertEquals(
				"The server transaction left the PROCEEDING state.",
				TransactionState.PROCEEDING,
				tran.getState());
			//Send 300 - 699 from TU and see the tran goes COMPLETED
			Response busy = null;
			try {
				busy =
					tiMessageFactory.createResponse(
						Response.BUSY_HERE,
						tran.getRequest());
				addStatus(tran.getRequest(), busy);
			} catch (ParseException ex) {
				throw new TiUnexpectedError(
					"A ParseException was thrown while trying to create a busy_here "
						+ "response using TI",
					ex);
			}
			try {
				//listen for the BUSY_HERE response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
			try {
				tran.sendResponse(busy);
			} catch (SipException ex) {
				ex.printStackTrace();
				fail("The TI failed to send a BUSY_HERE response");
			}
			//The Transaction should now be COMPLETED
			assertEquals(
				"The Transaction did not remain COMPLETED after transmitting a BUSY_HERE response",
				TransactionState.COMPLETED,
				tran.getState());
			//Check whether the BUSY_HERE is received by the RI.
			waitForMessage();
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"The BUSY_HERE response was not received by the RI",
				responseEvent);
			assertTrue(
				"A response different from BUSY_HERE was sent by the TI",
				Response.BUSY_HERE
					== responseEvent.getResponse().getStatusCode());
			//Resend the initial from INVITE from the RI and see that TI
			//resends the 300 - 699 (see that tran state remains COMPLETED)
			try {
				//listen for the Trying response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
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
			//Wait for the INVITE
			waitForMessage();
			inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
			assertNull(
				"Retransmitted INVITEs should not be passed to the TU",
				inviteReceivedEvent);
			//Wait for a retransmitted BUSY_HERE response
			waitForMessage();
			//Verify whether there was a BUSY_HERE response
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"No BUSY_HERE response has been sent by the TI upon reception "
					+ "of a retransmitted INVITE request",
				responseEvent);
			assertTrue(
				"A response different from BUSY_HERE was sent by the TI upon "
					+ "reception of a retransmitted invite INVITE",
				Response.BUSY_HERE
					== responseEvent.getResponse().getStatusCode());
			//We should still be COMPLETED
			assertEquals(
				"The server transaction left the COMPLETED state.",
				TransactionState.COMPLETED,
				tran.getState());
			//Send an ack from the RI and see that the tran goes CONFIRMED
			//and that response retransmissions cease
			Request ack = (Request) invite.clone();
			try {
				eventCollector.collectRequestEvent(tiSipProvider);
				ack.setMethod(Request.ACK);
				riSipProvider.sendRequest(ack);
			} catch (TooManyListenersException ex) {
				throw new TiUnexpectedError(
					"Failed to register a SipListener with the TI provider",
					ex);
			} catch (Exception ex) {
				throw new TckInternalError(
					"Failed to create an ack request",
					ex);
			}
			waitForMessage();
			RequestEvent ackEvent =
				eventCollector.extractCollectedRequestEvent();

			assertNull(
				"ACKs in ServerInviteTransactions shouldn't be passed to the TU.",
				ackEvent);
			assertEquals(
				"The ServerTransaction did not pas into the confirmed state"
					+ "after receiving an ACK.",
				TransactionState.CONFIRMED,
				tran.getState());
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}
		assertTrue(new Exception().getStackTrace()[0].toString(), true);

	}

	/**
	 * Tries to steer a TI server transaction through the following scenario
	 * Proceeding-->Terminated. Apart from state transitions, we also test,
	 * retransmissions and proper hiding/passing of messages to the TU.
	 */
	public void testProceedingTerminatedScenario() {
		try {
			Request invite = createRiInviteRequest(null, null, null);
			SipEventCollector responseCollector = new SipEventCollector();
			//Before Sending the request we should first register a listener with the
			//RI that would catch the TRYING response
			try {
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
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
			//Check whether a TRYING response has been sent.
			//wait for the trying response
			waitForMessage();
			// State must be proceeding After Response has been sent.
			assertEquals(TransactionState.PROCEEDING, tran.getState());
			ResponseEvent responseEvent =
				responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"No TRYING response has been sent by the TI upon reception "
					+ "of an INVITE request",
				responseEvent);
			assertTrue(
				"A response different from 100 was sent by the TI upon "
					+ "reception of INVITE",
				Response.TRYING == responseEvent.getResponse().getStatusCode());
			//Resend the invite and see that a TRYING response is resent
			try {
				//listen for the Trying response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
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
			//Wait for the INVITE
			waitForMessage();
			inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
			assertNull(
				"Retransmitted INVITEs should not be passed to the TU",
				inviteReceivedEvent);
			//Wait for a retransmitted TRYING response
			waitForMessage();
			//Verify whether there was a TRYING response
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"No TRYING response has been sent by the TI upon reception "
					+ "of an INVITE request",
				responseEvent);
			assertTrue(
				"A response different from 100 was sent by the TI upon "
					+ "reception of INVITE",
				Response.TRYING == responseEvent.getResponse().getStatusCode());
			//Create & send RINGING. See that it is properly sent
			//and that tran state doesn't change
			Response ringing = null;
			try {
				ringing =
					tiMessageFactory.createResponse(
						Response.RINGING,
						tran.getRequest());
				((ToHeader) ringing.getHeader(ToHeader.NAME)).setTag(
					Integer.toString(hashCode()));
				addStatus(tran.getRequest(), ringing);
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
			//The Transaction should still be PROCEEDING
			assertEquals(
				"The Transaction did not remain PROCEEDING after transmitting a RINGING response",
				TransactionState.PROCEEDING,
				tran.getState());
			//Check whether the RINGING is received by the RI.
			waitForMessage();
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"The RINGING response was not received by the RI",
				responseEvent);
			assertTrue(
				"A response different from RINGING was sent by the TI",
				Response.RINGING
					== responseEvent.getResponse().getStatusCode());
			//Resend the INVITE, see that it is hidden from the TU and see that
			//the _RINGING_ response is resent (and not the TRYING)
			try {
				//listen for the Trying response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
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
			//Wait for the INVITE
			waitForMessage();
			inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
			assertNull(
				"Retransmitted INVITEs should not be passed to the TU",
				inviteReceivedEvent);
			//Wait for a retransmitted RINGING response
			waitForMessage();
			//Verify whether there was a RINGING response
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"No RINGING response has been sent by the TI upon reception "
					+ "of an INVITE request",
				responseEvent);
			assertTrue(
				"A response different from RINGING was sent by the TI upon "
					+ "reception of a retransmitted invite INVITE",
				Response.RINGING
					== responseEvent.getResponse().getStatusCode());
			//We should still be proceeding
			assertEquals(
				"The server transaction left the PROCEEDING state.",
				TransactionState.PROCEEDING,
				tran.getState());
			//Create a 2xx final response and test transaction termination
			Response ok = null;
			try {
				ok =
					tiMessageFactory.createResponse(
						Response.OK,
						tran.getRequest());
				//!			discuss
				ContactHeader contact = tiHeaderFactory.createContactHeader();
				ok.addHeader(contact);
				((ToHeader) ok.getHeader(ToHeader.NAME)).setTag(
					Integer.toString(hashCode()));
				addStatus(tran.getRequest(), ok);
			} catch (ParseException ex) {
				throw new TiUnexpectedError(
					"A ParseException was thrown while trying to create an ok "
						+ "response using TI",
					ex);
			}
			try {
				//listen for the OK response
				responseCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"Failed to register a SipListener with an RI SipProvider",
					ex);
			}
			try {
				tran.sendResponse(ok);
			} catch (SipException ex) {
				ex.printStackTrace();
				fail("The TI failed to send a OK response");
			}
			//The Transaction should now be TERMINATED
			assertEquals(
				"The Transaction did not move to the TERMINATED state "
					+ "after transmitting an OK response",
				TransactionState.TERMINATED,
				tran.getState());
			//Check whether the OK is received by the RI.
			waitForMessage();
			responseEvent = responseCollector.extractCollectedResponseEvent();
			assertNotNull(
				"The OK response was not received by the RI",
				responseEvent);
			assertTrue(
				"A response different from OK was sent by the TI",
				Response.OK == responseEvent.getResponse().getStatusCode());

			// Send an ACK to the other side.
			Request ack = (Request) invite.clone();
			ack.setMethod(Request.ACK);
			((ToHeader) ack.getHeader(ToHeader.NAME)).setTag(
				(((ToHeader) ok.getHeader(ToHeader.NAME)).getParameter("tag")));
			try {
				riSipProvider.sendRequest(ack);
			} catch (SipException ex) {
				ex.printStackTrace();
				fail("error sending ack ");
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
		return new TestSuite(InviteServerTransactionsStateMachineTest.class);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(
			InviteServerTransactionsStateMachineTest.class);
	}

}
