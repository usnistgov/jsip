package test.tck.msgflow;

import junit.framework.*;

import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import java.util.*;
import java.text.*;
import test.tck.*;

/**
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.1 Technology Compatibility Kit</p>
 * @author Emil Ivov
 *         Network Research Team, Louis Pasteur University, Strasbourg, France
 * This  code is in the public domain.
 * @version 1.0
 */

public class ClientTransactionTest extends MessageFlowHarness {

	public ClientTransactionTest(String name) {
		super(name);
	}

	//==================== tests ==============================

	/**
	 * Creates an invite request using the Tested Implementation and then
	 * tests creating a cancel for the same invite request.
	 */
	public void testCreateCancel() {
		try {
			Request invite = createTiInviteRequest(null, null, null);
			ClientTransaction tran = null;
			try {
				tran = tiSipProvider.getNewClientTransaction(invite);
			} catch (TransactionUnavailableException exc) {
				throw new TiUnexpectedError(
					"A TransactionUnavailableException was thrown while trying to "
						+ "create a new client transaction",
					exc);
			}
			Request cancel = null;
			try {
				cancel = tran.createCancel();
			} catch (SipException ex) {
				ex.printStackTrace();
				fail("Failed to create cancel request!");
			}
			assertEquals(
				"The created request did not have a CANCEL method.",
				cancel.getMethod(),
				Request.CANCEL);
			assertEquals(
				"Request-URIs of the original and the cancel request do not match",
				cancel.getRequestURI(),
				invite.getRequestURI());
			assertEquals(
				"Call-IDs of the original and the cancel request do not match",
				cancel.getHeader(CallIdHeader.NAME),
				invite.getHeader(CallIdHeader.NAME));
			assertEquals(
				"ToHeaders of the original and the cancel request do not match",
				cancel.getHeader(ToHeader.NAME),
				invite.getHeader(ToHeader.NAME));
			assertTrue(
				"The CSeqHeader's sequence number of the original and "
					+ "the cancel request do not match",
				((CSeqHeader) cancel.getHeader(CSeqHeader.NAME))
					.getSequenceNumber()
					== ((CSeqHeader) invite.getHeader(CSeqHeader.NAME))
						.getSequenceNumber());
			assertEquals(
				"The CSeqHeader's method of the cancel request was not CANCEL",
				((CSeqHeader) cancel.getHeader(CSeqHeader.NAME)).getMethod(),
				Request.CANCEL);
			assertTrue(
				"There was no ViaHeader in the cancel request",
				cancel.getHeaders(ViaHeader.NAME).hasNext());
			Iterator cancelVias = cancel.getHeaders(ViaHeader.NAME);
			ViaHeader cancelVia = ((ViaHeader) cancelVias.next());
			ViaHeader inviteVia =
				((ViaHeader) invite.getHeaders(ViaHeader.NAME).next());
			assertEquals(
				"ViaHeaders of the original and the cancel request do not match!",
				cancelVia,
				inviteVia);
			assertFalse(
				"Cancel request had more than one ViaHeader.",
				cancelVias.hasNext());
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}

		assertTrue(new Exception().getStackTrace()[0].toString(), true);

	}

	/**
	 * Tests sending a request from a ClientTransaction.
	 */
	public void testSendRequest() {
		try {
			Request invite = createTiInviteRequest(null, null, null);
			RequestEvent receivedRequestEvent = null;
			ClientTransaction tran = null;
			try {
				tran = tiSipProvider.getNewClientTransaction(invite);
				eventCollector.collectRequestEvent(riSipProvider);
				tran.sendRequest();
				waitForMessage();
				receivedRequestEvent =
					eventCollector.extractCollectedRequestEvent();
				assertNotNull(
					"The sent request was not received by the RI!",
					receivedRequestEvent);
				assertNotNull(
					"The sent request was not received by the RI!",
					receivedRequestEvent.getRequest());
			} catch (TransactionUnavailableException exc) {
				throw new TiUnexpectedError(
					"A TransactionUnavailableException was thrown while trying to "
						+ "create a new client transaction",
					exc);
			} catch (SipException exc) {
				exc.printStackTrace();
				fail("The SipException was thrown while trying to send the request.");
			} catch (TooManyListenersException exc) {
				throw new TckInternalError(
					"A  TooManyListenersException was thrown while trying "
						+ "to add a SipListener to an RI SipProvider",
					exc);
			}
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}

		assertTrue(new Exception().getStackTrace()[0].toString(), true);

	}

	/**
	 * Tests creating of ACK requests.
	 */
	public void testCreateAck() {
		try {
			// 1. Create and send the original request

			Request invite = createTiInviteRequest(null, null, null);
			RequestEvent receivedRequestEvent = null;
			ClientTransaction tran = null;
			try {
				tran = tiSipProvider.getNewClientTransaction(invite);
				eventCollector.collectRequestEvent(riSipProvider);
				tran.sendRequest();
				waitForMessage();
				receivedRequestEvent =
					eventCollector.extractCollectedRequestEvent();
				if (receivedRequestEvent == null
					|| receivedRequestEvent.getRequest() == null)
					throw new TiUnexpectedError("The sent request was not received by the RI!");
			} catch (TooManyListenersException ex) {
				throw new TckInternalError(
					"A TooManyListenersException was thrown while trying to add "
						+ "a SipListener to an RI SipProvider.",
					ex);
			} catch (SipException ex) {
				throw new TiUnexpectedError(
					"The TI failed to send the request!",
					ex);
			}
			Request receivedRequest = receivedRequestEvent.getRequest();
			// 2. Create and send the response
			Response ok = null;
			try {
				ok =
					riMessageFactory.createResponse(
						Response.OK,
						receivedRequest);
                // BUG submitted by Ben Evans (Opencloud): 
				// need to set contact header on dialog-creating response
                ok.setHeader(createRiContact());
			} catch (ParseException ex) {
				throw new TckInternalError(
					"Failed to create an OK response!",
					ex);
			}
			//Send the response using the RI and collect using TI
			try {
				eventCollector.collectResponseEvent(tiSipProvider);
			} catch (TooManyListenersException ex) {
				throw new TiUnexpectedError("Error while trying to add riSipProvider");
			}
			try {
				riSipProvider.sendResponse(ok);
			} catch (SipException ex) {
				throw new TckInternalError(
					"Could not send back the response",
					ex);
			}
			waitForMessage();
			ResponseEvent responseEvent =
				eventCollector.extractCollectedResponseEvent();
			//3. Now let's create the ack
			if (responseEvent == null || responseEvent.getResponse() == null)
				throw new TiUnexpectedError("The TI failed to receive the response!");
			if (responseEvent.getClientTransaction() != tran)
				throw new TiUnexpectedError(
					"The TI has associated a new ClientTransaction to a response "
						+ "instead of using existing one!");
			Request ack = null;
			try {
				ack = tran.createAck();
			} catch (SipException ex) {
				ex.printStackTrace();
				fail("A SipException was thrown while creating an ack request");
			}
			assertNotNull("ClientTransaction.createAck returned null!", ack);
			assertEquals(
				"The created request did not have a CANCEL method.",
				ack.getMethod(),
				Request.ACK);
			assertEquals(
				"Request-URIs of the original and the ack request do not match",
				ack.getRequestURI(),
				invite.getRequestURI());
			assertEquals(
				"Call-IDs of the original and the ack request do not match",
				ack.getHeader(CallIdHeader.NAME),
				invite.getHeader(CallIdHeader.NAME));
			assertEquals(
				"ToHeaders of the original and the ack request do not match",
				ack.getHeader(ToHeader.NAME),
				invite.getHeader(ToHeader.NAME));
			assertTrue(
				"The CSeqHeader's sequence number of the original and "
					+ "the ack request do not match",
				((CSeqHeader) ack.getHeader(CSeqHeader.NAME))
					.getSequenceNumber()
					== ((CSeqHeader) invite.getHeader(CSeqHeader.NAME))
						.getSequenceNumber());
			assertEquals(
				"The CSeqHeader's method of the ack request was not ACK",
				((CSeqHeader) ack.getHeader(CSeqHeader.NAME)).getMethod(),
				Request.ACK);
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}

		assertTrue(new Exception().getStackTrace()[0].toString(), true);

	}

	//==================== end of tests

	//====== STATIC JUNIT ==========
	public static Test suite() {
		return new TestSuite(ClientTransactionTest.class);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(ClientTransactionTest.class);
	}
}
