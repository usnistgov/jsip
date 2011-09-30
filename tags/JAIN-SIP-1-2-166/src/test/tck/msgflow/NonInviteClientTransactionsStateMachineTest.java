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
import javax.sip.message.*;
import java.util.*;
import test.tck.*;

/**
 *
 * The test tries to verify that Non Invite Client Transactions correctly change
 * states as specified by the rfc3261. The Tested Implementation is used
 * to send requests and the ReferenceImplementation issues (or not) corresponding
 * responses. ClientTransaction states are constantly queried
 * and compared to those in the state machine described in
 * section 17.1.2 of rfc3261
 *<pre>
 *
 *                                   | Request from TU
 *                                   | send request
 *               Timer E             V
 *               send request  +-----------+
 *                   +---------|           |-------------------+
 *                   |         |  Trying   |  Timer F          |
 *                   +-------->|           |  or Transport Err.|
 *                             +-----------+  inform TU        |
 *                200-699         |  |                         |
 *                resp. to TU     |  |1xx                      |
 *                +---------------+  |resp. to TU              |
 *                |                  |                         |
 *                |   Timer E        V       Timer F           |
 *                |   send req +-----------+ or Transport Err. |
 *                |  +---------|           | inform TU         |
 *                |  |         |Proceeding |------------------>|
 *                |  +-------->|           |-----+             |
 *                |            +-----------+     |1xx          |
 *                |              |      ^        |resp to TU   |
 *                | 200-699      |      +--------+             |
 *                | resp. to TU  |                             |
 *                |              |                             |
 *                |              V                             |
 *                |            +-----------+                   |
 *                |            |           |                   |
 *                |            | Completed |                   |
 *                |            |           |                   |
 *                |            +-----------+                   |
 *                |              ^   |                         |
 *                |              |   | Timer K                 |
 *                +--------------+   | -                       |
 *                                   |                         |
 *                                   V                         |
 *             NOTE:           +-----------+                   |
 *                             |           |                   |
 *         transitions         | Terminated|<------------------+
 *         labeled with        |           |
 *         the event           +-----------+
 *         over the action
 *         to take
 *
 *                 Figure 6: non-INVITE client transaction
 *
 * TODO test timeout events
 *</pre>
 *
 * @author Emil Ivov
 *         Network Research Team, Louis Pasteur University, Strasbourg, France.
 * This  code is in the public domain.
 * @version 1.0
 */

public class NonInviteClientTransactionsStateMachineTest
    extends MessageFlowHarness {

    public NonInviteClientTransactionsStateMachineTest(String name) {
        super(name);
    }
    //==================== tests ==============================

    /**
     * Tries to walk a TI client transaction through the following scenario
     * Trying-->Proceeding-->Completed-->Terminated. Apart from state
     * transitions, we also test, retransmissions and proper hiding/passing
     * of messages to the TU.
     */
    public void testTryingProceedingCompletedTerminatedScenario() {
        try {
            Request register = createTiRegisterRequest();
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                tran = tiSipProvider.getNewClientTransaction(register);
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
            RequestEvent registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            if (registerReceivedEvent == null
                || registerReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError("The REGISTER request was not received by the RI!");
            //At this point the ClientTransaction should be TRYING!
            assertEquals(TransactionState.TRYING, tran.getState());
            //Check Request retransmission
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                    "Failed to regiest a SipListener with an RI SipProvider",
                    ex);
            }
            //Wait for the retransmission timer to fire if it had not already done so.
            if (tran.getRetransmitTimer() > MESSAGES_ARRIVE_FOR)
                sleep((long) tran.getRetransmitTimer() - MESSAGES_ARRIVE_FOR);
            //subtract the time we waited for the REGISTER
            //Wait for the retransmitted request to arrive
            waitForMessage();
            registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            assertNotNull(
                "The REGISTER request was not retransmitted!",
                registerReceivedEvent);
            assertNotNull(
                "The REGISTER request was not retransmitted!",
                registerReceivedEvent.getRequest());
            assertEquals(
                Request.REGISTER,
                registerReceivedEvent.getRequest().getMethod());
            //At this point the ClientTransaction should STILL be TRYING!
            assertEquals(TransactionState.TRYING, tran.getState());
            //Send a TRYING response
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with TI",
                    ex);
            }
            try {
                Response res =
                    riMessageFactory.createResponse(
                        Response.TRYING,
                        registerReceivedEvent.getRequest());
                addStatus(registerReceivedEvent.getRequest(), res);
                riSipProvider.sendResponse(res);
            } catch (Throwable ex) {
                throw new TckInternalError(
                    "The TCK could not send a trying response back to the TI",
                    ex);
            }
            waitForMessage();
            //Analyze the TRYING response and Tran state back at the TI
            ResponseEvent responseEvent =
                eventCollector.extractCollectedResponseEvent();
            assertNotNull(
                "The Tested Implementation did not pass a 1xx response to the TU!",
                responseEvent);
            assertNotNull(
                "The Tested Implementation did not pass a 1xx response to the TU!",
                responseEvent.getResponse());
            assertTrue(
                "A response different from TRYING was passed to the TU!",
                responseEvent.getResponse().getStatusCode() == Response.TRYING);
            assertSame(
                "The TRYING response was not associated with the right transaction",
                tran,
                responseEvent.getClientTransaction());
            //verify the the tran state is now PROCEEDING
            assertEquals(
                "The ClientTransaction did not pass in the PROCEEDING state after "
                    + "receiving 1xx provisional response",
                tran.getState(),
                TransactionState.PROCEEDING);
            //Send a 200 OK (final) response from the RI
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with TI",
                    ex);
            }
            //The OK response shouldn't trigger any ACKs so let's register
            //a listener with the RI to verify whether that is the case
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
                ok =
                    riMessageFactory.createResponse(
                        Response.OK,
                        registerReceivedEvent.getRequest());
                addStatus(registerReceivedEvent.getRequest(), ok);

                riSipProvider.sendResponse((Response) ok.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                    "The TCK could not send a OK response back to the TI",
                    ex);
            }
            waitForMessage();
            //Analyze the OK response and Tran state back at the TI
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNotNull(
                "The Tested Implementation did not pass a 200-699 response to the TU!",
                responseEvent);
            assertNotNull(
                "The Tested Implementation did not pass a 200-699 response to the TU!",
                responseEvent.getResponse());
            assertSame(
                "The OK response was not associated with the right transaction",
                tran,
                responseEvent.getClientTransaction());
            assertSame(
                "A response different from OK was passed to the TU",
                tran,
                responseEvent.getClientTransaction());
            assertEquals(
                "The ClientTransaction did not pass in the COMPLETED state after "
                    + "receiving 200-699 final response",
                tran.getState(),
                TransactionState.COMPLETED);
            //check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent =
                ackCollector.extractCollectedRequestEvent();
            assertNull(
                "The TI sent an ACK request in a non INVITE transaction",
                ackReceivedEvent);
            //Now let's retransmit the final response. See again that no acks are sent
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with TI",
                    ex);
            }
            //go fish the ack
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
                    "The TCK could not send a OK response back to the TI",
                    ex);
            }
            waitForMessage();
            //The TU shouldn't see the retransmitted OK response
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNull(
                "The Tested Implementation passed a retransmitted 200-699 response "
                    + "to the TU.",
                responseEvent);
            //We must still be in the completed state.
            assertTrue(
                "The ClientTransaction did not stay long enough in the COMPLETED "
                    + "state.",
                tran.getState().equals(TransactionState.COMPLETED)
                    || tran.getState().equals(TransactionState.TERMINATED));
            //check whether the ackCollector has caught any fish
            ackReceivedEvent = ackCollector.extractCollectedRequestEvent();
            assertNull(
                "The TI replied with an ACK to a nonINVITE request",
                ackReceivedEvent);
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    /**
     * Tries to walk a TI client transaction through the following scenario
     * Trying-->Completed-->Terminated. Apart from state
     * transitions, we also test, retransmissions and proper hiding/passing
     * of messages to the TU.
     */
    public void testTryingCompletedTerminatedScenario() {
        try {
            Request register = createTiRegisterRequest();
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                tran = tiSipProvider.getNewClientTransaction(register);
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
            RequestEvent registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            if (registerReceivedEvent == null
                || registerReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError("The REGISTER request was not received by the RI!");
            //At this point the ClientTransaction should be TRYING!
            assertEquals(TransactionState.TRYING, tran.getState());
            //Check Request retransmission
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                    "Failed to regiest a SipListener with an RI SipProvider",
                    ex);
            }
            //Wait for the retransmission timer to fire if it had not already done so.
            if (tran.getRetransmitTimer() > MESSAGES_ARRIVE_FOR)
                sleep((long) tran.getRetransmitTimer() - MESSAGES_ARRIVE_FOR);
            //subtract the time we waited for the REGISTER
            //Wait for the retransmitted request to arrive
            waitForMessage();
            registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            assertNotNull(
                "The REGISTER request was not retransmitted!",
                registerReceivedEvent);
            assertNotNull(
                "The REGISTER request was not retransmitted!",
                registerReceivedEvent.getRequest());
            assertEquals(
                Request.REGISTER,
                registerReceivedEvent.getRequest().getMethod());
            //At this point the ClientTransaction should STILL be TRYING!
            assertEquals(TransactionState.TRYING, tran.getState());
            //Send a 200 OK (final) response from the RI
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with TI",
                    ex);
            }
            //The OK response shouldn't trigger any ACKs so let's register
            //a listener with the RI to verify whether that is the case
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
                ok =
                    riMessageFactory.createResponse(
                        Response.OK,
                        registerReceivedEvent.getRequest());
                addStatus(registerReceivedEvent.getRequest(), ok);
                riSipProvider.sendResponse((Response) ok.clone());
            } catch (Throwable ex) {
                throw new TckInternalError(
                    "The TCK could not send a OK response back to the TI",
                    ex);
            }
            waitForMessage();
            //Analyze the OK response and Tran state back at the TI
            ResponseEvent responseEvent =
                eventCollector.extractCollectedResponseEvent();
            assertNotNull(
                "The Tested Implementation did not pass a 200-699 response to the TU!",
                responseEvent);
            assertNotNull(
                "The Tested Implementation did not pass a 200-699 response to the TU!",
                responseEvent.getResponse());
            assertSame(
                "The OK response was not associated with the right transaction",
                tran,
                responseEvent.getClientTransaction());
            assertSame(
                "A response different from OK was passed to the TU",
                tran,
                responseEvent.getClientTransaction());
            assertEquals(
                "The ClientTransaction did not pass in the COMPLETED state after "
                    + "receiving 200-699 final response",
                tran.getState(),
                TransactionState.COMPLETED);
            //check whether the ackCollector has caught any fish
            RequestEvent ackReceivedEvent =
                ackCollector.extractCollectedRequestEvent();
            assertNull(
                "The TI sent an ACK request in a non INVITE transaction",
                ackReceivedEvent);
            //Now let's retransmit the final response. See again that no acks are sent
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with TI",
                    ex);
            }
            //go fish the ack
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
                    "The TCK could not send a OK response back to the TI",
                    ex);
            }
            waitForMessage();
            //The TU shouldn't see the retransmitted OK response
            responseEvent = eventCollector.extractCollectedResponseEvent();
            assertNull(
                "The Tested Implementation passed a retransmitted 200-699 response "
                    + "to the TU.",
                responseEvent);
            //We must still be in the completed state.
            assertTrue(
                "The ClientTransaction did not stay long enough in the COMPLETED "
                    + "state.",
                tran.getState().equals(TransactionState.COMPLETED)
                    || tran.getState().equals(TransactionState.TERMINATED));
            //check whether the ackCollector has caught any fish
            ackReceivedEvent = ackCollector.extractCollectedRequestEvent();
            assertNull(
                "The TI replied with an ACK to a nonINVITE request",
                ackReceivedEvent);
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    //==================== end of tests

    //====== STATIC JUNIT ==========
    public static Test suite() {
        return new TestSuite(NonInviteClientTransactionsStateMachineTest.class);
    }


}
