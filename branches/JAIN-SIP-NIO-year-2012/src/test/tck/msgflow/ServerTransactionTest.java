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
package test.tck.msgflow;

import junit.framework.*;

import javax.sip.*;
import javax.sip.header.ViaHeader;
import javax.sip.message.*;

import java.util.*;
import java.text.*;
import test.tck.*;

/**
 * <p>
 * Title: TCK
 * </p>
 * <p>
 * Description: JAIN SIP 1.1 Technology Compatibility Kit
 * </p>
 *
 * @author Emil Ivov Network Research Team, Louis Pasteur University,
 *         Strasbourg, France. This code is in the public domain.
 * @version 1.0
 *
 */

public class ServerTransactionTest extends MessageFlowHarness {

    public ServerTransactionTest(String name) {
        super(name);
    }

    // ==================== tests ==============================

    public void testSendResponse() {
        try {
            Request invite = createRiInviteRequest(null, null, null);
            SipEventCollector responseCollector = new SipEventCollector();
            // Send the initial request
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
            RequestEvent inviteReceivedEvent = eventCollector
                    .extractCollectedRequestEvent();
            if (inviteReceivedEvent == null
                    || inviteReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The initial invite request was not received by the TI!");
            // Let's create the transaction
            ServerTransaction tran = null;
            try {
                tran = tiSipProvider
                        .getNewServerTransaction(inviteReceivedEvent
                                .getRequest());
            } catch (Exception ex) {
                ex.printStackTrace();
                fail(ex.getClass().getName() + "was thrown while trying to "
                        + "create the server transaction");
            }
            assertNotNull(
                    "tiSipProvider.getNewServerTransaction() returned null",
                    tran);
            // Create & send RINGING. See that it is properly sent
            Response ringing = null;
            try {
                ringing = tiMessageFactory.createResponse(Response.RINGING,
                        tran.getRequest());
                // BUG: set contact header on dialog-creating response
                ringing.setHeader(createTiContact());
            } catch (ParseException ex) {
                throw new TiUnexpectedError(
                        "A ParseException was thrown while trying to create a ringing "
                                + "response using TI", ex);
            }
            try {
                // listen for the RINGING response
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

            // reset the collector
            responseCollector.extractCollectedResponseEvent();

        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }

        assertTrue(new Exception().getStackTrace()[0].toString(), true);
    }

    public void testCancel() {
        try {
            Request invite = createRiInviteRequest(null, null, null);
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(tiSipProvider);

                // This call overwrites any branch we set
                tran = riSipProvider.getNewClientTransaction(invite);

                // And this call too
                tran.sendRequest();
            } catch (SipException ex) {
                throw new TiUnexpectedError(
                        "A SipExceptionOccurred while trying to send request!",
                        ex);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            waitForMessage();
            RequestEvent inviteReceivedEvent = eventCollector
                    .extractCollectedRequestEvent();

            ServerTransaction st = tiSipProvider
                    .getNewServerTransaction(inviteReceivedEvent.getRequest());

            Request inviteRequest = inviteReceivedEvent.getRequest();

            Dialog dialog = st.getDialog();

            assertNotNull("Dialog ID must be valid", dialog);

            if (inviteReceivedEvent == null
                    || inviteReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The invite request was not received by the TI!");

            // At this point the ClientTransaction should be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());

            // Send a TRYING response
            try {
                eventCollector.collectResponseEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            try {
                Response resp = tiMessageFactory.createResponse(
                        Response.TRYING, inviteReceivedEvent.getRequest());
                addStatus(inviteReceivedEvent.getRequest(), resp);
                st.sendResponse(resp);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a trying response back to the TI",
                        ex);
            }

            waitForMessage();

            ResponseEvent responseEvent = eventCollector
                    .extractCollectedResponseEvent();

            assertNotNull("Response must be seen", responseEvent);
            assertEquals("Must see 100 trying response", responseEvent
                    .getResponse().getStatusCode(), 100);

            // Analyze the TRYING response and Tran state back at the TI

            // Send a CANCEL from the RI
            Request riCancel = tran.createCancel();

            ClientTransaction riCancelTrans;
            try {
                eventCollector.collectRequestEvent(tiSipProvider);
                riCancelTrans = riSipProvider.getNewClientTransaction(riCancel);
                riCancelTrans.sendRequest();
            } catch (SipException ex) {
                throw new TiUnexpectedError(
                        "A SipExceptionOccurred while trying to send CANCEL!",
                        ex);
            }
            waitForMessage();
            RequestEvent cancelReceivedEvent = eventCollector
                    .extractCollectedRequestEvent();
            if (cancelReceivedEvent == null
                    || cancelReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The CANCEL request was not received by the TI!");

            // Send 200 OK to the CANCEL

            assertNotNull("Cancel dialog must be non null", cancelReceivedEvent
                    .getDialog() != null);
            assertSame("Cancel dialog must match Invite", cancelReceivedEvent
                    .getDialog(), dialog);
            try {
                eventCollector.collectResponseEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            Response tiCancelOk = null;
            try {
                tiCancelOk = tiMessageFactory.createResponse(Response.OK,
                        cancelReceivedEvent.getRequest());
                addStatus(cancelReceivedEvent.getRequest(), tiCancelOk);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TI could not send a CANCEL OK response back to the RI",
                        ex);
            }

            ServerTransaction cancelTx = cancelReceivedEvent
                    .getServerTransaction();

            assertNotNull("Must have valid cancel tx", cancelTx);

            assertTrue("Cancel tx must not be the same as the invite Tx",
                    cancelTx != st);

            assertTrue("Method must be cancel ", cancelTx.getRequest()
                    .getMethod().equals(Request.CANCEL));

            assertTrue("Branch must match invite branch", ((ViaHeader) cancelTx
                    .getRequest().getHeader(ViaHeader.NAME)).getBranch()
                    .equals(
                            ((ViaHeader) inviteRequest
                                    .getHeader(ViaHeader.NAME)).getBranch()));

            cancelTx.sendResponse(tiCancelOk);

            waitForMessage();

            // Analyze the OK response and Tran state back at the TI
            responseEvent = eventCollector.extractCollectedResponseEvent();
            if (responseEvent == null || responseEvent.getResponse() == null) {
                throw new TiUnexpectedError(
                        "The CANCEL OK response was not received by the RI!");
            }

            assertEquals("Must see a 200", eventCollector
                    .extractCollectedResponseEvent().getResponse()
                    .getStatusCode(), 200);


            try {
                eventCollector.collectResponseEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            SipEventCollector ackCollector = new SipEventCollector();
            try {
                ackCollector.collectRequestEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            Response tiInviteTerminated = null;
            try {
                tiInviteTerminated = tiMessageFactory.createResponse(
                        Response.REQUEST_TERMINATED, inviteReceivedEvent
                                .getRequest());
                addStatus(inviteReceivedEvent.getRequest(), tiInviteTerminated);
                st.sendResponse(tiInviteTerminated);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a INVITE 487 response back to the TI",
                        ex);
            }
            waitForMessage();
            assertEquals("Must see a 487", eventCollector
                    .extractCollectedResponseEvent().getResponse()
                    .getStatusCode(), 487);


            waitForMessage();



            // check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent = ackCollector
                    .extractCollectedRequestEvent();

            // NOTE -- per 3261 the ack is not passed to the TU.
            assertNull("The TI MUST NOT see the ACK",
                    ackReceivedEvent);

        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

        // Unfortunately we can't assert the TERMINATED state as timerK timerD
        // is not exported by JAIN SIP
    }

    // ==================== end of tests

    // ====== STATIC JUNIT ==========
    public static Test suite() {
        return new TestSuite(ServerTransactionTest.class);
    }

}
