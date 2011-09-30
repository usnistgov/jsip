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
import javax.sip.header.*;
import java.util.*;
import java.text.*;
import test.tck.*;

/**
 *
 * The test tries to verify that Invite Server Transactions correctly change
 * states as specified by the rfc3261. The Reference Implementation is used
 * to send requests and a Tested Implementation ServerTransaction's states are
 * queried and compared to those in the state machine described in
 * section 17.2.1 of rfc3261
 *<pre>
 *
 *                        |Request received
 *                                  |pass to TU
 *                                  V
 *                            +-----------+
 *                            |           |
 *                            | Trying    |-------------+
 *                            |           |             |
 *                            +-----------+             |200-699 from TU
 *                                  |                   |send response
 *                                  |1xx from TU        |
 *                                  |send response      |
 *                                  |                   |
 *               Request            V      1xx from TU  |
 *               send response+-----------+send response|
 *                   +--------|           |--------+    |
 *                   |        | Proceeding|        |    |
 *                   +------->|           |<-------+    |
 *            +<--------------|           |             |
 *            |Trnsprt Err    +-----------+             |
 *            |Inform TU            |                   |
 *            |                     |                   |
 *            |                     |200-699 from TU    |
 *            |                     |send response      |
 *            |  Request            V                   |
 *            |  send response+-----------+             |
 *            |      +--------|           |             |
 *            |      |        | Completed |<------------+
 *            |      +------->|           |
 *            +<--------------|           |
 *            |Trnsprt Err    +-----------+
 *            |Inform TU            |
 *            |                     |Timer J fires
 *            |                     |-
 *            |                     |
 *            |                     V
 *            |               +-----------+
 *            |               |           |
 *            +-------------->| Terminated|
 *                            |           |
 *                            +-----------+
 *
 *                Figure 8: non-INVITE server transaction
 *
 *</pre>
 *
 * @author Emil Ivov
 *      Network Research Team, Louis Pasteur University, Strasbourg, France.
 * This  code is in the public domain.
 * @version 1.0
 */

public class NonInviteServerTransactionsStateMachineTest
    extends MessageFlowHarness {

    public NonInviteServerTransactionsStateMachineTest(String name) {
        super(name);
    }

    //==================== tests ==============================

    /**
     * Tries to steer a TI server transaction through the following scenario
     * Trying-->Proceeding-->Completed-->Terminated. Apart from state
     * transitions, we also test, retransmissions and proper hiding/passing
     * of messages to the TU.
     */
    public void testTryingProceedingCompletedScenario() {
        try {
            Request register = createRiRegisterRequest();
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
                riSipProvider.sendRequest(register);
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
            RequestEvent registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            if (registerReceivedEvent == null
                || registerReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError("The initial register request was not received by the TI!");
            //Let's create the transaction
            ServerTransaction tran = null;
            try {
                tran =
                    tiSipProvider.getNewServerTransaction(
                        registerReceivedEvent.getRequest());
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
            //At this point the ServerTransaction should be TRYING!
            assertEquals(TransactionState.TRYING, tran.getState());
            //Send a TRYING response
            Response trying = null;
            try {
                trying =
                    tiMessageFactory.createResponse(
                        Response.TRYING,
                        registerReceivedEvent.getRequest());
                tran.sendResponse(trying);
            } catch (Exception ex) {
                throw new TiUnexpectedError(
                    "Failed to send a TRYING response",
                    ex);
            }
            waitForMessage();
            //The transaction should now be PROCEEDING
            assertEquals(
                "The transaction did not pass into the PROCEEDING state "
                    + "upon transmission of a 1xx response.",
                TransactionState.PROCEEDING,
                tran.getState());
            ResponseEvent responseEvent =
                responseCollector.extractCollectedResponseEvent();
            assertNotNull(
                "The TRYING response has not been sent by the TI.",
                responseEvent);
            assertTrue(
                "A response different from 100 was sent by the TI",
                Response.TRYING == responseEvent.getResponse().getStatusCode());
            //Resend the REGISTER and see that the TRYING response is resent
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
                riSipProvider.sendRequest(register);
            } catch (SipException ex) {
                throw new TckInternalError(
                    "A SipExceptionOccurred while trying to send request!",
                    ex);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with a TI SipProvider",
                    ex);
            }
            //Wait for the REGISTER
            waitForMessage();
            registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            assertNull(
                "Retransmitted REGISTERs should not be passed to the TU",
                registerReceivedEvent);
            //Wait for a retransmitted TRYING response
            waitForMessage();
            //Verify whether there was a TRYING response
            responseEvent = responseCollector.extractCollectedResponseEvent();
            assertNotNull(
                "No TRYING response has been resent by the TI upon reception "
                    + "of a retransmitted REGISTER request",
                responseEvent);
            assertTrue(
                "A response different from 100 was sent by the TI upon "
                    + "reception of a retransmitted REGISTER",
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
            //Resend the REGISTER, see that it is hidden from the TU and see that
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
                riSipProvider.sendRequest(register);
            } catch (SipException ex) {
                throw new TckInternalError(
                    "A SipExceptionOccurred while trying to send request!",
                    ex);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with a TI SipProvider",
                    ex);
            }
            //Wait for the REGISTER
            waitForMessage();
            registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            assertNull(
                "Retransmitted REGISTERs should not be passed to the TU",
                registerReceivedEvent);
            //Wait for a retransmitted RINGING response
            waitForMessage();
            //Verify whether there was a RINGING response
            responseEvent = responseCollector.extractCollectedResponseEvent();
            assertNotNull(
                "No RINGING response has been sent by the TI upon reception "
                    + "of an REGISTER request",
                responseEvent);
            assertTrue(
                "A response different from RINGING was sent by the TI upon "
                    + "reception of a retransmitted REGISTER",
                Response.RINGING
                    == responseEvent.getResponse().getStatusCode());
            //We should still be proceeding
            assertEquals(
                "The server transaction left the PROCEEDING state.",
                TransactionState.PROCEEDING,
                tran.getState());
            //Send 200 - 699 from TU and see the tran goes COMPLETED
            Response ok = null;
            try {
                ok =
                    tiMessageFactory.createResponse(
                        Response.OK,
                        tran.getRequest());
                ((ToHeader) ok.getHeader(ToHeader.NAME)).setTag(
                    Integer.toString(hashCode()));
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
                tran.sendResponse(ok);
            } catch (SipException ex) {
                ex.printStackTrace();
                fail("The TI failed to send a OK response");
            }
            //The Transaction should now be COMPLETED
            assertEquals(
                "The Transaction did not remain COMPLETED after transmitting a BUSY_HERE response",
                TransactionState.COMPLETED,
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
            //Resend the initial from REGISTER from the RI and see that TI
            //resends the 200 - 699 (see that tran state remains COMPLETED)
            try {
                //listen for the OK response
                responseCollector.collectResponseEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                    "Failed to register a SipListener with an RI SipProvider",
                    ex);
            }
            try {
                eventCollector.collectRequestEvent(tiSipProvider);
                riSipProvider.sendRequest(register);
            } catch (SipException ex) {
                throw new TckInternalError(
                    "A SipExceptionOccurred while trying to send request!",
                    ex);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with a TI SipProvider",
                    ex);
            }
            //Wait for the REGISTER
            waitForMessage();
            registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            assertNull(
                "Retransmitted REGISTERs should not be passed to the TU",
                registerReceivedEvent);
            //Wait for a retransmitted OK response
            waitForMessage();
            //Verify whether there was an OK response
            responseEvent = responseCollector.extractCollectedResponseEvent();
            assertNotNull(
                "No OK response has been sent by the TI upon reception "
                    + "of a retransmitted REGISTER request",
                responseEvent);
            assertTrue(
                "A response different from OK was sent by the TI upon "
                    + "reception of a retransmitted invite REGISTER",
                Response.OK == responseEvent.getResponse().getStatusCode());
            //We should still be COMPLETED
            assertEquals(
                "The server transaction left the COMPLETED state.",
                TransactionState.COMPLETED,
                tran.getState());
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    /**
     * Tries to steer a TI server transaction through the following scenario
     * Trying-->Completed. Apart from state
     * transitions, we also test, retransmissions and proper hiding/passing
     * of messages to the TU.
     */
    public void testTryingCompletedScenario() {
        try {

            Request register = createRiRegisterRequest();
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
                riSipProvider.sendRequest(register);
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
            RequestEvent registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            if (registerReceivedEvent == null
                || registerReceivedEvent.getRequest() == null)
                throw new TiUnexpectedError("The initial REGISTER request was not received by the TI!");
            //Let's create the transaction
            ServerTransaction tran = null;
            try {
                tran =
                    tiSipProvider.getNewServerTransaction(
                        registerReceivedEvent.getRequest());
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
            //At this point the ServerTransaction should be TRYING!
            assertEquals(TransactionState.TRYING, tran.getState());
            //Send a TRYING response
            Response trying = null;
            try {
                trying =
                    tiMessageFactory.createResponse(
                        Response.TRYING,
                        registerReceivedEvent.getRequest());
                tran.sendResponse(trying);
            } catch (Exception ex) {
                throw new TiUnexpectedError(
                    "Failed to send a TRYING response",
                    ex);
            }
            //The transaction should now be PROCEEDING
            assertEquals(
                "The transaction did not pass into the PROCEEDING state "
                    + "upon transmission of a 1xx response.",
                TransactionState.PROCEEDING,
                tran.getState());
            waitForMessage();
            ResponseEvent responseEvent =
                responseCollector.extractCollectedResponseEvent();
            assertNotNull(
                "The TRYING response has not been sent by the TI.",
                responseEvent);
            assertTrue(
                "A response different from 100 was sent by the TI",
                Response.TRYING == responseEvent.getResponse().getStatusCode());
            //Resend the REGISTER and see that the TRYING response is resent
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
                riSipProvider.sendRequest(register);
            } catch (SipException ex) {
                throw new TckInternalError(
                    "A SipExceptionOccurred while trying to send request!",
                    ex);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with a TI SipProvider",
                    ex);
            }
            //Wait for the REGISTER
            waitForMessage();
            registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            assertNull(
                "Retransmitted REGISTERs should not be passed to the TU",
                registerReceivedEvent);
            //Wait for a retransmitted TRYING response
            waitForMessage();
            //Verify whether there was a TRYING response
            responseEvent = responseCollector.extractCollectedResponseEvent();
            assertNotNull(
                "No TRYING response has been resent by the TI upon reception "
                    + "of a retransmitted REGISTER request",
                responseEvent);
            assertTrue(
                "A response different from 100 was sent by the TI upon "
                    + "reception of a retransmitted REGISTER",
                Response.TRYING == responseEvent.getResponse().getStatusCode());
            //Send 200 - 699 from TU and see the tran goes COMPLETED
            Response ok = null;
            try {
                ok =
                    tiMessageFactory.createResponse(
                        Response.OK,
                        tran.getRequest());
                ((ToHeader) ok.getHeader(ToHeader.NAME)).setTag(
                    Integer.toString(hashCode()));
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
                tran.sendResponse(ok);
            } catch (SipException ex) {
                ex.printStackTrace();
                fail("The TI failed to send a OK response");
            }
            //The Transaction should now be COMPLETED
            assertEquals(
                "The Transaction did not remain COMPLETED after transmitting a BUSY_HERE response",
                TransactionState.COMPLETED,
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
            //Resend the initial from REGISTER from the RI and see that TI
            //resends the 200 - 699 (see that tran state remains COMPLETED)
            try {
                //listen for the OK response
                responseCollector.collectResponseEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                    "Failed to register a SipListener with an RI SipProvider",
                    ex);
            }
            try {
                eventCollector.collectRequestEvent(tiSipProvider);
                riSipProvider.sendRequest(register);
            } catch (SipException ex) {
                throw new TckInternalError(
                    "A SipExceptionOccurred while trying to send request!",
                    ex);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with a TI SipProvider",
                    ex);
            }
            //Wait for the REGISTER
            waitForMessage();
            registerReceivedEvent =
                eventCollector.extractCollectedRequestEvent();
            assertNull(
                "Retransmitted REGISTERs should not be passed to the TU",
                registerReceivedEvent);
            //Wait for a retransmitted OK response
            waitForMessage();
            //Verify whether there was an OK response
            responseEvent = responseCollector.extractCollectedResponseEvent();
            assertNotNull(
                "No OK response has been sent by the TI upon reception "
                    + "of a retransmitted REGISTER request",
                responseEvent);
            assertTrue(
                "A response different from OK was sent by the TI upon "
                    + "reception of a retransmitted invite REGISTER",
                Response.OK == responseEvent.getResponse().getStatusCode());
            //We should still be COMPLETED
            assertEquals(
                "The server transaction left the COMPLETED state.",
                TransactionState.COMPLETED,
                tran.getState());
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    //==================== end of tests

    //====== STATIC JUNIT ==========
    public static Test suite() {
        return new TestSuite(NonInviteServerTransactionsStateMachineTest.class);
    }


}
