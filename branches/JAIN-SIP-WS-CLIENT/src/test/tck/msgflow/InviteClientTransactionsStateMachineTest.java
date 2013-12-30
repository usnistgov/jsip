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

import java.util.TooManyListenersException;

import javax.sip.*;
import javax.sip.header.ContactHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.tck.TckInternalError;
import test.tck.TiUnexpectedError;

/**
 * The test tries to verify that Invite Client Transactions correctly change
 * states as specified by the rfc3261. The Tested Implementation is used to send
 * requests and the ReferenceImplementation issues (or not) corresponding
 * responses. ClientTransaction states are constantly queried and compared to
 * those in the state machine described in section 17.1.1 of rfc3261
 *
 * <pre>
 *
 *                               |INVITE from TU
 *             Timer A fires     |INVITE sent
 *             Reset A,          V                      Timer B fires
 *             INVITE sent +-----------+                or Transport Err.
 *               +---------|           |---------------+inform TU
 *               |         |  Calling  |               |
 *               +--------&gt;|           |--------------&gt;|
 *                         +-----------+ 2xx           |
 *                            |  |       2xx to TU     |
 *                            |  |1xx                  |
 *    300-699 +---------------+  |1xx to TU            |
 *   ACK sent |                  |                     |
 * esp. to TU |  1xx             V                     |
 *            |  1xx to TU  -----------+               |
 *            |  +---------|           |               |
 *            |  |         |Proceeding |--------------&gt;|
 *            |  +--------&gt;|           | 2xx           |
 *            |            +-----------+ 2xx to TU     |
 *            |       300-699    |                     |
 *            |       ACK sent,  |                     |
 *            |       resp. to TU|                     |
 *            |                  |                     |      NOTE:
 *            |  300-699         V                     |
 *            |  ACK sent  +-----------+Transport Err. |  transitions
 *            |  +---------|           |Inform TU      |  labeled with
 *            |  |         | Completed |--------------&gt;|  the event
 *            |  +--------&gt;|           |               |  over the action
 *            |            +-----------+               |  to take
 *            |              &circ;   |                     |
 *            |              |   | Timer D fires       |
 *            +--------------+   | -                   |
 *                               |                     |
 *                               V                     |
 *                         +-----------+               |
 *                         |           |               |
 *                         | Terminated|&lt;--------------+
 *                         |           |
 *                         +-----------+
 *
 *                 Figure 5: INVITE client transaction
 *
 *
 * </pre>
 *
 * TODO Currently, transactions' behaviour is not tested for misappropriation of
 * incoming messages.
 *
 * TODO Currently, invite transactions are not tested for proper termination
 * upon respective timeouts.
 *
 * @author Emil Ivov Network Research Team, Louis Pasteur University,
 *         Strasbourg, France. This code is in the public domain.
 * @version 1.0
 */
public class InviteClientTransactionsStateMachineTest extends
        MessageFlowHarness {

    private static Logger logger = Logger
            .getLogger(InviteClientTransactionsStateMachineTest.class);

    public InviteClientTransactionsStateMachineTest(String name) {
        super(name, false); // disable auto-dialog for the RI, else ACKs get
                            // filtered out
    }

    // ==================== tests ==============================

    /**
     * Tries to steer a TI client transaction through the following scenario
     * Calling-->Proceeding-->Completed-->Terminated. Apart from state
     * transitions, we also test, retransmissions and proper hiding/passing of
     * messages to the TU.
     */
    public void testCallingProceedingCompletedTerminatedScenario() {
        try {
            Request invite = createTiInviteRequest(null, null, null);
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                tran = tiSipProvider.getNewClientTransaction(invite);
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
            if (inviteReceivedEvent == null
                    || inviteReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The invite request was not received by the RI!");
            // At this point the ClientTransaction should be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Check Request retransmission
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            // Wait for the retransmission timer to fire if it had not already
            // done so.
            if (tran.getRetransmitTimer() > MESSAGES_ARRIVE_FOR)
                sleep((long) tran.getRetransmitTimer() - MESSAGES_ARRIVE_FOR); // subtract
                                                                                // the
                                                                                // time
                                                                                // we
                                                                                // waited
                                                                                // for
                                                                                // the
                                                                                // invite
            // Wait for the retransmitted request to arrive
            waitForMessage();
            inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent);
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent.getRequest());
            assertEquals(Request.INVITE, inviteReceivedEvent.getRequest()
                    .getMethod());
            // At this point the ClientTransaction should STILL be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Send a TRYING response
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            try {
                Response resp = riMessageFactory.createResponse(
                        Response.TRYING, inviteReceivedEvent.getRequest());
                addStatus(inviteReceivedEvent.getRequest(), resp);
                riSipProvider.sendResponse(resp);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a trying response back to the TI",
                        ex);
            }

            waitForMessage();
            // Analyze the TRYING response and Tran state back at the TI
            ResponseEvent responseEvent = eventCollector
                    .extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 1xx response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 1xx response to the TU!",
                    responseEvent.getResponse());
            assertTrue(
                    "A response different from TYING was passed to the TU!",
                    responseEvent.getResponse().getStatusCode() == Response.TRYING);
            assertSame(
                    "The TRYING response was not associated with the right transaction.",
                    tran, responseEvent.getClientTransaction());
            // verify the the tran state is now PROCEEDING
            assertEquals(
                    "The ClientTransaction did not pass in the PROCEEDING state after "
                            + "receiving 1xx provisional response", tran
                            .getState(), TransactionState.PROCEEDING);
            // Send a 486 BUSY HERE (final) response from the RI
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // The BUSY_HERE response should trigger some ACKs so let's register
            // a listener with the RI
            SipEventCollector ackCollector = new SipEventCollector();
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            Response busyHere = null;
            try {
                busyHere = riMessageFactory.createResponse(Response.BUSY_HERE,
                        inviteReceivedEvent.getRequest());
                addStatus(inviteReceivedEvent.getRequest(), busyHere);

                // JvB: set to-tag too, mandatory and tests that ACK is properly
                // formatted
                ((ToHeader) busyHere.getHeader("to")).setTag("busy-here");

                riSipProvider.sendResponse(busyHere);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a BUSY HERE response back to the TI",
                        ex);
            }
            waitForMessage();

            // Analyze the BUSY_HERE response and Tran state back at the TI
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 300-699 response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 300-699 response to the TU!",
                    responseEvent.getResponse());
            assertSame(
                    "The BUSY_HERE response was not associated with the right transaction",
                    tran, responseEvent.getClientTransaction());
            assertEquals(
                    "A response different from BUSY_HERE was passed to the TU",
                    Response.BUSY_HERE, responseEvent.getResponse()
                            .getStatusCode());
            assertEquals(
                    "The ClientTransaction did not pass in the COMPLETED state after "
                            + "receiving 300-699 final response", tran
                            .getState(), TransactionState.COMPLETED);
            // check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent = ackCollector
                    .extractCollectedRequestEvent();

            // JvB: With auto-dialog-support enabled, the ACK should be filtered
            // by the RI
            assertNotNull("The TI did not send an ACK request",
                    ackReceivedEvent);
            assertNotNull("The TI did not send an ACK request",
                    ackReceivedEvent.getRequest());
            assertEquals(Request.ACK, ackReceivedEvent.getRequest().getMethod());

            // Try to kill remaining ACK retransmissions
            // TODO this may not always work .. should give it a specific
            // timeout value
            waitForMessage();
            // Now let's retransmit the final response. This time it shouldn't
            // be
            // passed to the TU but an ACK should still be sent
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // go fish the ack
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            try {
                riSipProvider.sendResponse((Response) busyHere.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a BUSY HERE response back to the TI",
                        ex);
            }
            waitForMessage();
            // The TU shouldn't see the retransmitted BUSY_HERE response
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNull(
                    "The Tested Implementation passed a retransmitted 300-699 response "
                            + "to the TU instead of just silently acknowledging it!",
                    responseEvent);
            // We must still be in the completed state.
            assertEquals(
                    "The ClientTransaction did not stay long enough in the COMPLETED "
                            + "state.", tran.getState(),
                    TransactionState.COMPLETED);
            // check whether the ackCollector has caught any fish
            ackReceivedEvent = ackCollector.extractCollectedRequestEvent();
            assertNotNull(
                    "The TI did not send an ACK request to the second response",
                    ackReceivedEvent);
            assertNotNull(
                    "The TI did not send an ACK request to the second response",
                    ackReceivedEvent.getRequest());
            assertEquals(Request.ACK, ackReceivedEvent.getRequest().getMethod());
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

        // Unfortunately we can't assert the TERMINATED state as timerK timerD
        // is not exported by JAIN SIP

    }

    /**
     * JvB: Tests transmission of an INVITE followed by CANCELlation of that
     * request
     *  -> INVITE <- 100 -> CANCEL <- OK <- 487 -> ACK
     */
    public void testInviteCancel() {
        doCancelTest(true);
    }

    /**
     * Same as above, but for non-RFC3261 client (i.e. no Via branch, no from/to
     * tags
     *
     * Cannot do this on 1.2 stack
     *
     * public void testInviteCancelNonRFC3261() { doCancelTest(false); }
     */

    /**
     * JvB: Tests transmission of an INVITE followed by CANCELlation of that
     * request
     *  -> INVITE <- 100 -> CANCEL <- OK <- 487 -> ACK
     *
     * Note: for 1.2 it is impossible to manually set the top via branch to
     * something not RFC3261-compliant
     */
    private void doCancelTest(boolean rfc3261Compliant) {
        try {
            Request invite = createTiInviteRequest(null, null, null);
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);

                // This call overwrites any branch we set
                tran = tiSipProvider.getNewClientTransaction(invite);

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

            if (inviteReceivedEvent == null
                    || inviteReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The invite request was not received by the RI!");

            // At this point the ClientTransaction should be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());

            // Send a TRYING response
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            try {
                Response resp = riMessageFactory.createResponse(
                        Response.TRYING, inviteReceivedEvent.getRequest());
                addStatus(inviteReceivedEvent.getRequest(), resp);
                riSipProvider.sendResponse(resp);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a trying response back to the TI",
                        ex);
            }

            waitForMessage();
            // Analyze the TRYING response and Tran state back at the TI
            ResponseEvent responseEvent = eventCollector
                    .extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 1xx response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 1xx response to the TU!",
                    responseEvent.getResponse());
            assertTrue(
                    "A response different from TYING was passed to the TU!",
                    responseEvent.getResponse().getStatusCode() == Response.TRYING);
            assertSame(
                    "The TRYING response was not associated with the right transaction.",
                    tran, responseEvent.getClientTransaction());
            // verify the the tran state is now PROCEEDING
            assertEquals(
                    "The ClientTransaction did not pass in the PROCEEDING state after "
                            + "receiving 1xx provisional response", tran
                            .getState(), TransactionState.PROCEEDING);

            // Send a CANCEL from the TI
            Request tiCancel = tran.createCancel();

            /*
             * this works, but since we cannot patch the INVITE the test fails
             * if (!rfc3261Compliant) { ((ViaHeader)
             * tiCancel.getHeader("Via")).setBranch( "xxx" ); // Not allowed by
             * RI // ((FromHeader) tiCancel.getHeader("From")).setTag( "" ); }
             */

            ClientTransaction tiCancelTrans;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                tiCancelTrans = tiSipProvider.getNewClientTransaction(tiCancel);
                tiCancelTrans.sendRequest();
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
                        "The CANCEL request was not received by the RI!");

            // Send 200 OK to the CANCEL
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            Response riCancelOk = null;
            try {
                riCancelOk = riMessageFactory.createResponse(Response.OK,
                        cancelReceivedEvent.getRequest());
                addStatus(cancelReceivedEvent.getRequest(), riCancelOk);
                riSipProvider.sendResponse(riCancelOk);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a CANCEL OK response back to the TI",
                        ex);
            }
            waitForMessage();

            // Analyze the OK response and Tran state back at the TI
            responseEvent = eventCollector.extractCollectedResponseEvent();
            if (responseEvent == null || responseEvent.getResponse() == null) {
                throw new TiUnexpectedError(
                        "The CANCEL OK response was not received by the TI!");
            }

            // Send 487 to the INVITE, expect ACK
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            SipEventCollector ackCollector = new SipEventCollector();
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }

            Response riInviteTerminated = null;
            try {
                riInviteTerminated = riMessageFactory.createResponse(
                        Response.REQUEST_TERMINATED, inviteReceivedEvent
                                .getRequest());
                addStatus(inviteReceivedEvent.getRequest(), riInviteTerminated);
                riSipProvider.sendResponse(riInviteTerminated);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a INVITE 487 response back to the TI",
                        ex);
            }
            waitForMessage();

            // Analyze the REQUEST_TERMINATED response and Tran state back at
            // the TI
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 300-699 response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 300-699 response to the TU!",
                    responseEvent.getResponse());
            assertSame(
                    "The 487 response was not associated with the right transaction",
                    tran, responseEvent.getClientTransaction());
            assertEquals("A response different from 487 was passed to the TU",
                    Response.REQUEST_TERMINATED, responseEvent.getResponse()
                            .getStatusCode());
            assertEquals(
                    "The ClientTransaction did not pass in the COMPLETED state after "
                            + "receiving 300-699 final response", tran
                            .getState(), TransactionState.COMPLETED);
            // check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent = ackCollector
                    .extractCollectedRequestEvent();
            assertNotNull("The TI did not send an ACK request event",
                    ackReceivedEvent);
            assertNotNull("The TI did not send an ACK request",
                    ackReceivedEvent.getRequest());
            assertEquals(Request.ACK, ackReceivedEvent.getRequest().getMethod());

            // Try to kill remaining ACK retransmissions?
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

        // Unfortunately we can't assert the TERMINATED state as timerK timerD
        // is not exported by JAIN SIP
    }



    /**
     * Tries to steer a TI client transaction through the following scenario
     * Calling-->Completed-->Terminated. Apart from state transitions, we also
     * test, retransmissions and proper hiding/passing of messages to the TU.
     */
    public void testCallingCompletedTerminatedScenario() {
        try {
            Request invite = createTiInviteRequest(null, null, null);

            // JvB: test
            // ((MessageExt) invite).getFirstViaHeader().setParameter( "rport",
            // null );

            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                tran = tiSipProvider.getNewClientTransaction(invite);
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
            if (inviteReceivedEvent == null
                    || inviteReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The invite request was not received by the RI!");
            // At this point the ClientTransaction should be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Check Request retransmission
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            // Wait for the retransmission timer to fire if it had not already
            // done so.
            if (tran.getRetransmitTimer() > MESSAGES_ARRIVE_FOR)
                sleep((long) tran.getRetransmitTimer() - MESSAGES_ARRIVE_FOR); // subtract
                                                                                // the
                                                                                // time
                                                                                // we
                                                                                // waited
                                                                                // for
                                                                                // the
                                                                                // invite
            // Wait for the retransmitted request to arrive
            waitForMessage();
            inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent);
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent.getRequest());
            assertEquals(Request.INVITE, inviteReceivedEvent.getRequest()
                    .getMethod());
            // At this point the ClientTransaction should STILL be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Send a 486 BUSY HERE (final) response from the RI
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // The BUSY_HERE response should trigger some ACKs so let's register
            // a listener with the RI
            SipEventCollector ackCollector = new SipEventCollector();
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            Response busyHere = null;
            try {
                busyHere = riMessageFactory.createResponse(Response.BUSY_HERE,
                        inviteReceivedEvent.getRequest());
                addStatus(inviteReceivedEvent.getRequest(), busyHere);

                // JvB: set to-tag, check it against the ACK generated
                ((ToHeader) busyHere.getHeader("to")).setTag("ack-to-test");

                riSipProvider.sendResponse((Response) busyHere.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a BUSY HERE response back to the TI",
                        ex);
            }
            waitForMessage();
            // Analyze the BUSY_HERE response and Tran state back at the TI
            ResponseEvent responseEvent = eventCollector
                    .extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 300-699 response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 300-699 response to the TU!",
                    responseEvent.getResponse());
            assertSame(
                    "The BUSY_HERE response was not associated with the right transaction",
                    tran, responseEvent.getClientTransaction());
            assertSame(
                    "A response different from BUSY_HERE was passed to the TU",
                    tran, responseEvent.getClientTransaction());
            assertEquals(
                    "The ClientTransaction did not pass in the COMPLETED state after "
                            + "receiving 300-699 final response", tran
                            .getState(), TransactionState.COMPLETED);
            // check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent = ackCollector
                    .extractCollectedRequestEvent();
            assertNotNull("The TI did not send an ACK request",
                    ackReceivedEvent);
            assertNotNull("The TI did not send an ACK request",
                    ackReceivedEvent.getRequest());
            assertEquals(Request.ACK, ackReceivedEvent.getRequest().getMethod());
            // Try to kill remaining ACK retransmissions
            // TODO this may not always work .. should give it a specific
            // timeout value
            waitForMessage();
            // Now let's retransmit the final response. This time it shouldn't
            // be
            // passed to the TU but an ACK should still be sent
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // go fish the ack
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            try {
                riSipProvider.sendResponse((Response) busyHere.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a BUSY HERE response back to the TI",
                        ex);
            }
            waitForMessage();
            // The TU shouldn't see the retransmitted BUSY_HERE response
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNull(
                    "The Tested Implementation passed a retransmitted 300-699 response "
                            + "to the TU instead of just silently acknowledging it!",
                    responseEvent);
            // We must still be in the completed state.
            assertEquals(
                    "The ClientTransaction did not stay long enough in the COMPLETED "
                            + "state.", tran.getState(),
                    TransactionState.COMPLETED);
            // check whether the ackCollector has caught any fish
            ackReceivedEvent = ackCollector.extractCollectedRequestEvent();
            assertNotNull(
                    "The TI did not send an ACK request to the second response",
                    ackReceivedEvent);
            assertNotNull(
                    "The TI did not send an ACK request to the second response",
                    ackReceivedEvent.getRequest());

            assertEquals(Request.ACK, ackReceivedEvent.getRequest().getMethod());

            // JvB: verify to tag
            assertEquals(
                    "The To header field in the ACK MUST equal the To header field "
                            + " in the response being acknowledged",
                    "ack-to-test", ((ToHeader) ackReceivedEvent.getRequest()
                            .getHeader("to")).getTag());
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

        // Unfortunately we can't assert the TERMINATED state as timerD
        // is not exported by JAIN SIP

    }

    /**
     * Tries to steer a TI client transaction through the following scenario
     * Calling-->Proceeding-->Terminated. Apart from state transitions, we also
     * test, retransmissions and proper hiding/passing of messages to the TU.
     */
    public void testCallingProceedingTerminatedScenario() {
        try {
            Request invite = createTiInviteRequest(null, null, null);
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                tran = tiSipProvider.getNewClientTransaction(invite);
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
            if (inviteReceivedEvent == null
                    || inviteReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The invite request was not received by the RI!");
            // At this point the ClientTransaction should be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Check Request retransmission
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            // Wait for the retransmission timer to fire if it had not already
            // done so.
            if (tran.getRetransmitTimer() > MESSAGES_ARRIVE_FOR)
                sleep((long) tran.getRetransmitTimer() - MESSAGES_ARRIVE_FOR); // subtract
                                                                                // the
                                                                                // time
                                                                                // we
                                                                                // waited
                                                                                // for
                                                                                // the
                                                                                // invite
            // Wait for the retransmitted request to arrive
            waitForMessage();
            inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent);
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent.getRequest());
            assertEquals(Request.INVITE, inviteReceivedEvent.getRequest()
                    .getMethod());
            // At this point the ClientTransaction should STILL be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Send a TRYING response
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            try {
                Response resp = riMessageFactory.createResponse(
                        Response.TRYING, inviteReceivedEvent.getRequest());
                addStatus(inviteReceivedEvent.getRequest(), resp);
                riSipProvider.sendResponse(resp);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send a trying response back to the TI",
                        ex);
            }
            waitForMessage();
            // Analyze the TRYING response and Tran state back at the TI
            ResponseEvent responseEvent = eventCollector
                    .extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 1xx response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 1xx response to the TU!",
                    responseEvent.getResponse());
            assertTrue(
                    "A response different from TYING was passed to the TU!",
                    responseEvent.getResponse().getStatusCode() == Response.TRYING);
            assertSame(
                    "The TRYING response was not associated with the right transaction",
                    tran, responseEvent.getClientTransaction());
            // verify the the tran state is now PROCEEDING
            assertEquals(
                    "The ClientTransaction did not pass in the PROCEEDING state after "
                            + "receiving 1xx provisional response", tran
                            .getState(), TransactionState.PROCEEDING);
            // Send a 200 OK (final) response from the RI
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // The OK response shouldn't trigger any ACKs so let's register
            // a listener with the RI to verify whether that is the case
            SipEventCollector ackCollector = new SipEventCollector();
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            Response ok = null;
            try {
                ok = riMessageFactory.createResponse(Response.OK,
                        inviteReceivedEvent.getRequest());

                // JvB: MUST add Contact too!
                ContactHeader contact = riHeaderFactory
                        .createContactHeader(((ToHeader) ok.getHeader("To"))
                                .getAddress());
                ok.addHeader(contact);
                // end JvB

                addStatus(inviteReceivedEvent.getRequest(), ok);
                riSipProvider.sendResponse((Response) ok.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send an OK response back to the TI",
                        ex);
            }
            waitForMessage();
            // Analyze the OK response and Tran state back at the TI
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 200 OK response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 200 OK response to the TU!",
                    responseEvent.getResponse());
            assertSame(
                    "The OK response was not associated with the right transaction",
                    tran, responseEvent.getClientTransaction());
            assertSame("A response different from OK was passed to the TU",
                    tran, responseEvent.getClientTransaction());
            assertEquals(
                    "The ClientTransaction did not pass in the TERMINATED state after "
                            + "receiving 200 final response", tran.getState(),
                    TransactionState.TERMINATED);
            // check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent = ackCollector
                    .extractCollectedRequestEvent();
            if (ackReceivedEvent != null)
                logger.error("Shouldn't have received that="
                        + ackReceivedEvent.getRequest());
            assertNull("The TI sent an ACK to an OK (this is TU's job)!",
                    ackReceivedEvent);
            // Now let's retransmit the final response and see it is
            // passed to the TU (again no ACKs)
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // go fish the ack
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            try {
                riSipProvider.sendResponse((Response) ok.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send an OK response back to the TI",
                        ex);
            }
            waitForMessage();
            // Did we get the 2nd OK?
            responseEvent = eventCollector.extractCollectedResponseEvent();

            /*
             * JvB: the stack should in fact ~not~ pass the retransmitted
             * response, but filter it out
             *
             * assertNotNull( "The TI did not pass to the TU a retransmitted OK
             * response!", responseEvent); assertNotNull( "The TI did not pass
             * to the TU a retransmitted OK response!",
             * responseEvent.getResponse()); assertTrue( "The TI passed to the
             * TU a bad response!",
             * responseEvent.getResponse().getStatusCode()==Response.OK);
             */
            // TBD
            // assertNull( "The TI should filter the retransmitted OK response",
            // responseEvent );

            // We must still be in the terminated state.
            assertEquals(
                    "The ClientTransaction mysteriously left the TERMINATED state!",
                    tran.getState(), TransactionState.TERMINATED);
            // check whether the ackCollector has caught any fish
            ackReceivedEvent = ackCollector.extractCollectedRequestEvent();
            assertNull("The TI sent an ACK request to the second OK response "
                    + "(OK acks are TU's responsibility)!", ackReceivedEvent);
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);
    }

    /**
     * Tries to steer a TI client transaction through the following scenario
     * Calling-->Terminated. Apart from state transitions, we also test,
     * retransmissions and proper hiding/passing of messages to the TU.
     */
    public void testCallingTerminatedScenario() {
        try {
            Request invite = createTiInviteRequest(null, null, null);
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                tran = tiSipProvider.getNewClientTransaction(invite);
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
            if (inviteReceivedEvent == null
                    || inviteReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError(
                        "The invite request was not received by the RI!");
            // At this point the ClientTransaction should be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Check Request retransmission
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            // Wait for the retransmission timer to fire if it had not already
            // done so.
            if (tran.getRetransmitTimer() > MESSAGES_ARRIVE_FOR)
                sleep((long) tran.getRetransmitTimer() - MESSAGES_ARRIVE_FOR); // subtract
                                                                                // the
                                                                                // time
                                                                                // we
                                                                                // waited
                                                                                // for
                                                                                // the
                                                                                // invite
            // Wait for the retransmitted request to arrive
            waitForMessage();
            inviteReceivedEvent = eventCollector.extractCollectedRequestEvent();
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent);
            assertNotNull("The invite request was not retransmitted!",
                    inviteReceivedEvent.getRequest());
            assertEquals(Request.INVITE, inviteReceivedEvent.getRequest()
                    .getMethod());
            // At this point the ClientTransaction should STILL be CALLING!
            assertEquals(TransactionState.CALLING, tran.getState());
            // Send a 200 OK (final) response from the RI
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // The OK response shouldn't trigger any ACKs so let's register
            // a listener with the RI to verify whether that is the case
            SipEventCollector ackCollector = new SipEventCollector();
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            Response ok = null;
            try {
                ok = riMessageFactory.createResponse(Response.OK,
                        inviteReceivedEvent.getRequest());
                addStatus(inviteReceivedEvent.getRequest(), ok);

                // JvB: MUST add Contact too!
                ContactHeader contact = riHeaderFactory
                        .createContactHeader(((ToHeader) ok.getHeader("To"))
                                .getAddress());
                ok.addHeader(contact);
                // end JvB

                riSipProvider.sendResponse(ok);
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send an OK response back to the TI",
                        ex);
            }
            waitForMessage();
            // Analyze the OK response and Tran state back at the TI
            ResponseEvent responseEvent = eventCollector
                    .extractCollectedResponseEvent();
            assertNotNull(
                    "The Tested Implementation did not pass a 200 OK response to the TU!",
                    responseEvent);
            assertNotNull(
                    "The Tested Implementation did not pass a 200 OK response to the TU!",
                    responseEvent.getResponse());
            assertSame(
                    "The OK response was not associated with the right transaction",
                    tran, responseEvent.getClientTransaction());
            assertSame("A response different from OK was passed to the TU",
                    tran, responseEvent.getClientTransaction());
            assertEquals(
                    "The ClientTransaction did not pass in the TERMINATED state after "
                            + "receiving 200 final response", tran.getState(),
                    TransactionState.TERMINATED);
            // check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent = ackCollector
                    .extractCollectedRequestEvent();
            assertNull("The TI sent an ACK to an OK (this is TU's job)!",
                    ackReceivedEvent);
            // Now let's retransmit the final response and see that it is
            // passed to the TU (again no ACKs should be sent by the TI)
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            // go fish the ack
            try {
                ackCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                        "Failed to regiest a SipListener with an RI SipProvider",
                        ex);
            }
            try {
                riSipProvider.sendResponse((Response) ok.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                        "The TCK could not send an OK response back to the TI",
                        ex);
            }
            waitForMessage();
            // Did we get the 2nd OK?
            responseEvent = eventCollector.extractCollectedResponseEvent();
            /*
             * JvB: see previous comment
             *
             * assertNotNull( "The TI did not pass to the TU a retransmitted OK
             * response!", responseEvent); assertNotNull( "The TI did not pass
             * to the TU a retransmitted OK response!",
             * responseEvent.getResponse()); assertTrue( "The TI passed to the
             * TU a bad response!",
             * responseEvent.getResponse().getStatusCode()==Response.OK);
             */
            // JvB: TBD
            // assertNull( "The TI should filter the retransmitted OK response",
            // responseEvent );

            // We must still be in the terminated state.
            assertEquals(
                    "The ClientTransaction mysteriously left the TERMINATED state!",
                    tran.getState(), TransactionState.TERMINATED);
            // check whether the ackCollector has caught any fish
            ackReceivedEvent = ackCollector.extractCollectedRequestEvent();
            assertNull("The TI sent an ACK request to the second OK response "
                    + "(OK acks are TU's responsibility)!", ackReceivedEvent);
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);
    }

    // ==================== end of tests

    // ====== STATIC JUNIT ==========
    public static Test suite() {
        return new TestSuite(InviteClientTransactionsStateMachineTest.class);
    }

}
