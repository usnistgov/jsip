package test.tck.msgflow;

import junit.framework.*;

import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import java.util.*;
import test.tck.*;

/**
 *
 * Tests whether the dialog state machine is properly implemented. Dialog state
 * machines are far less complex than transaction FSMs. All they do is passively
 * change states without producing any automatic responses or retransmissions.
 * Therefore we're only testing one scenario per Dialog (one for Server generated
 * and one for Client generated dialogs. Distinction is made server and client
 * generated scenarios are likely to be navigated by server and client transactions
 * respectively and not by a single entity)
 *
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.1 Technology Compatibility Kit</p>
 * @author Emil Ivov
 *      Network Research Team, Louis Pasteur University, Strasbourg, France.
 * This  code is in the public domain.
 * @version 1.0
 */

public class DialogStateMachineTest extends MessageFlowHarness {

	public DialogStateMachineTest(String name) {
		super(name);
	}
	//==================== tests ==============================
	/**
	 * Tests state transitions of a server side dialog
	 */
	public void testClientDialogStates() {
		try {
			Request invite = createTiInviteRequest(null, null, null);
			ClientTransaction tran = null;
			//Send an invite request
			try {
				eventCollector.collectRequestEvent(riSipProvider);
				tran = tiSipProvider.getNewClientTransaction(invite);
				tran.sendRequest();
			} catch (TooManyListenersException e) {
				throw new TckInternalError(
					"Failed to register a listener with the RI",
					e);
			} catch (SipException e) {
				throw new TiUnexpectedError(
					"Failed to send initial invite request",
					e);
			}
			//Wait for the invite to arrive
			waitForMessage();
			RequestEvent inviteReqEvt =
				eventCollector.extractCollectedRequestEvent();
			if (inviteReqEvt == null || inviteReqEvt.getRequest() == null)
				throw new TiUnexpectedError("The TI did not send the initial invite request");
			//get the dialog
			Dialog dialog = tran.getDialog();
			//We should have a null state here
			assertNull(
				"A dialog passed into the "
					+ dialog.getState()
					+ " state before recieving any response!",
				dialog.getState());
			//We will now send RINGING response and see that the Dialog enters an early state
			//start listening for the response
			try {
				eventCollector.collectResponseEvent(tiSipProvider);
			} catch (TooManyListenersException e) {
				throw new TiUnexpectedError(
					"Failed to register a SipListener with the TI.",
					e);
			}
			Response ringing = null;
			try {
				ringing =
					riMessageFactory.createResponse(
						Response.RINGING,
						inviteReqEvt.getRequest());
				((ToHeader) ringing.getHeader(ToHeader.NAME)).setTag(
					Integer.toString(hashCode()));
				riSipProvider.sendResponse(ringing);
			} catch (Exception e) {
				throw new TckInternalError(
					"Failed to create and send a RINGING response",
					e);
			}
			waitForMessage();
			ResponseEvent ringingRespEvt =
				eventCollector.extractCollectedResponseEvent();
			if (ringingRespEvt == null || ringingRespEvt.getResponse() == null)
				throw new TiUnexpectedError("The TI did not pass RINGING response to the TU.");
			//The dialog should now be in its early state.
			assertEquals(
				"The Dialog did not pass into the early state upon reception of a RINGING response",
				DialogState.EARLY,
				dialog.getState());
			//We will now send OK response and see that the Dialog enters a CONFIRMED state
			//start listening for the response
			try {
				eventCollector.collectResponseEvent(tiSipProvider);
			} catch (TooManyListenersException e) {
				throw new TiUnexpectedError(
					"Failed to register a SipListener with the TI.",
					e);
			}
			Response ok = null;
			try {
				ok =
					riMessageFactory.createResponse(
						Response.OK,
						inviteReqEvt.getRequest());
				((ToHeader) ok.getHeader(ToHeader.NAME)).setTag(
					Integer.toString(hashCode()));
				riSipProvider.sendResponse(ok);
			} catch (Exception e) {
				throw new TckInternalError(
					"Failed to create and send a OK response",
					e);
			}
			waitForMessage();
			ResponseEvent okRespEvt =
				eventCollector.extractCollectedResponseEvent();
			if (okRespEvt == null || okRespEvt.getResponse() == null)
				throw new TiUnexpectedError("The TI did not pass OK response to the TU.");
			//The dialog should now be in its confirmed state.
			assertEquals(
				"The Dialog did not pass into the CONFIRMED state upon reception of an OK response",
				DialogState.CONFIRMED,
				dialog.getState());
			//Say bye and go COMPLETED
			try {
				Request bye = dialog.createRequest(Request.BYE);
				ClientTransaction byeTran =
					tiSipProvider.getNewClientTransaction(bye);
				dialog.sendRequest(byeTran);
			} catch (SipException e) {
				throw new TiUnexpectedError(
					"Failed to create and send a BYE request using a dialog.",
					e);
			}
			assertTrue(
				"The dialog did not pass into a final (COMPLETED or TERMINATED) state after sending a BYE.",
				DialogState.COMPLETED.equals(dialog.getState())
					|| DialogState.TERMINATED.equals(dialog.getState()));
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}

		assertTrue(new Exception().getStackTrace()[0].toString(), true);

	}

	/**
	 * Tests state transitions of a client side dialog
	 */
	public void testServerDialogStates() {
		try {
			ClientTransaction inviteTransaction = null;
			Request invite = createRiInviteRequest(null, null, null);
			//Send an invite request
			try {
				eventCollector.collectRequestEvent(tiSipProvider);
				// riSipProvider.sendRequest(invite);
				// Made this stateful 
				inviteTransaction =
					riSipProvider.getNewClientTransaction(invite);
				inviteTransaction.sendRequest();
			} catch (TooManyListenersException exc) {
				throw new TiUnexpectedError(
					"Failed to register a listener with the TI",
					exc);
			} catch (SipException exc) {
				throw new TckInternalError(
					"Failed to send initial invite request",
					exc);
			}
			//Wait for the invite to arrive
			waitForMessage();
			RequestEvent inviteReqEvt =
				eventCollector.extractCollectedRequestEvent();
			if (inviteReqEvt == null || inviteReqEvt.getRequest() == null)
				throw new TiUnexpectedError("The TI did not dispatch the initial invite request");
			//Create a transaction
			ServerTransaction tran = null;
			try {
				tran =
					tiSipProvider.getNewServerTransaction(
						inviteReqEvt.getRequest());
			} catch (Exception ex) {
				throw new TiUnexpectedError("The TI failed to create a Server transaction for an incoming request");
			}
			//get the dialog
			Dialog dialog = tran.getDialog();
			//We should have a null state here
			assertNull(
				"A dialog passed into the "
					+ dialog.getState()
					+ " state before sending any response!",
				dialog.getState());
			//We will now send RINGING response and see that the Dialog enters an early state
			//start listening for the response
			try {
				eventCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException e) {
				throw new TckInternalError(
					"Failed to register a SipListener with the RI.",
					e);
			}
			Response ringing = null;
			try {
				ringing =
					tiMessageFactory.createResponse(
						Response.RINGING,
						inviteReqEvt.getRequest());
				//!discuss with Ranga
				((ToHeader) ringing.getHeader(ToHeader.NAME)).setTag(
					Integer.toString(hashCode()));
				tran.sendResponse(ringing);
			} catch (Exception e) {
				throw new TiUnexpectedError(
					"Failed to create and send a RINGING response",
					e);
			}
			waitForMessage();
			ResponseEvent ringingRespEvt =
				eventCollector.extractCollectedResponseEvent();
			if (ringingRespEvt == null || ringingRespEvt.getResponse() == null)
				throw new TiUnexpectedError("The TI did not send the RINGING response.");
			//The dialog should now be in its early state.
			assertEquals(
				"The Dialog did not pass into the early state after sending a RINGING response",
				DialogState.EARLY,
				dialog.getState());
			//We will now send an OK response and see that the Dialog enters a CONFIRMED state
			//start listening for the response
			try {
				eventCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException e) {
				throw new TckInternalError(
					"Failed to register a SipListener with the RI.",
					e);
			}
			Response ok = null;
			try {
				ok =
					tiMessageFactory.createResponse(
						Response.OK,
						inviteReqEvt.getRequest());
				ContactHeader contact = createTiContact();
				ok.addHeader(contact);

				tran.sendResponse(ok);
			} catch (Exception e) {
				throw new TiUnexpectedError(
					"Failed to create and send an OK response",
					e);
			}
			waitForMessage();
			ResponseEvent okRespEvt =
				eventCollector.extractCollectedResponseEvent();
			if (okRespEvt == null || okRespEvt.getResponse() == null)
				throw new TiUnexpectedError("The TI did not send an OK response.");
			//The dialog should now be in its CONFIRMED state.
			assertEquals(
				"The Dialog did not pass into the CONFIRMED state upon reception of an OK response",
				DialogState.CONFIRMED,
				dialog.getState());
			//Say bye from the RI and see that TI goes COMPLETED
			//it is the ri that should say bye here as we are testing dialog navigation
			//by server transactions
			try {
				eventCollector.collectRequestEvent(tiSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TiUnexpectedError(
					"Failed to register a SipListener with the TI",
					ex);
			}
			try {
				// Ranga - use the dialog here.
				Dialog d = inviteTransaction.getDialog();
				Request bye = d.createRequest(Request.BYE);
				ClientTransaction ct =
					riSipProvider.getNewClientTransaction(bye);
				d.sendRequest(ct);

				//Request bye = (Request)invite.clone();
				//bye.setMethod(Request.BYE);
				//riSipProvider.sendRequest(bye);
			} catch (Exception e) {
				throw new TckInternalError(
					"Failed to create and send a BYE request using a dialog.",
					e);
			}
			waitForMessage();
			RequestEvent byeEvt = eventCollector.extractCollectedRequestEvent();
			if (byeEvt == null || byeEvt.getRequest() == null)
				throw new TiUnexpectedError("The TI did not dispatch a BYE request");
			ServerTransaction byeTran = null;

			/** This should be in the transaction 
				
			            try
			            {
			                byeTran = tiSipProvider.getNewServerTransaction(byeEvt.getRequest());
			            } catch (TransactionUnavailableException ex) {
					// Could have already fielded the bye - in which case a
					// transaction for the BYE cannot be created.
					ex.printStackTrace();
			               assertTrue(new Exception().getStackTrace()[0].toString(), true);
				    } catch(Exception ex) {
					ex.printStackTrace();
			                System.out.println("Failed to create a transaction for an incoming bye request.");
			            }
			**/
			byeTran = (ServerTransaction) byeEvt.getServerTransaction();
			//We will now send an OK response and see that the 
			// Dialog enters a COMPLETED/TERMINATED state
			//start listening for the response
			try {
				eventCollector.collectResponseEvent(riSipProvider);
			} catch (TooManyListenersException e) {
				throw new TckInternalError(
					"Failed to register a SipListener with the RI.",
					e);
			}
			try {
				ok =
					tiMessageFactory.createResponse(
						Response.OK,
						byeEvt.getRequest());
				ok.addHeader(createTiContact());
				byeTran.sendResponse(ok);
			} catch (Exception e) {
				throw new TiUnexpectedError(
					"Failed to create and send an OK response",
					e);
			}
			waitForMessage();
			ResponseEvent byeOkRespEvt =
				eventCollector.extractCollectedResponseEvent();
			if (byeOkRespEvt == null || byeOkRespEvt.getResponse() == null)
				throw new TiUnexpectedError("The TI did not send an OK response to a bye request.");
			//The dialog should now be in the COMPLETED/TERMINATED state.
			assertTrue(
				"The dialog did not pass into a final (COMPLETED or TERMINATED) state after recieving a BYE.",
				DialogState.COMPLETED.equals(dialog.getState())
					|| DialogState.TERMINATED.equals(dialog.getState()));
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}

		assertTrue(new Exception().getStackTrace()[0].toString(), true);

	}

	//==================== end of tests

	//====== STATIC JUNIT ==========
	public static Test suite() {
		return new TestSuite(DialogStateMachineTest.class);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(DialogStateMachineTest.class);
	}

}
