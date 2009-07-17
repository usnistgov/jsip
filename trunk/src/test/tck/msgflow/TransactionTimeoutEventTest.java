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

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.Timeout;
import javax.sip.TimeoutEvent;
import javax.sip.header.ContactHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import test.tck.TckInternalError;
import test.tck.TiUnexpectedError;

/**
 * Timeout test for invite client transactions -- test to see if an timeout
 * event is delivered to the listener if the RI refuses to send OK to the INVITE
 * Client Tx.
 *
 * @author M. Ranganathan
 */

public class TransactionTimeoutEventTest extends MessageFlowHarness {
    private static Logger logger = Logger
            .getLogger(InviteClientTransactionsStateMachineTest.class);

    public TransactionTimeoutEventTest(String name) {
        super(name, false); // disable auto-dialog for the RI, else ACKs get
                            // filtered out
    }

    // ==================== tests ==============================

    /**
     * Test if the tx timeout is delivered.
     */
    public void testClientTransactionTimeout() {
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
            assertNotNull("RequestEvent", inviteReceivedEvent);
            try {
                eventCollector.collectTimeoutEvent(tiSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TiUnexpectedError(
                        "Failed to register a SipListener with TI", ex);
            }
            waitForTimeout();
            TimeoutEvent timeoutEvent = eventCollector
                    .extractCollectedTimeoutEvent();
            assertNotNull("Timeout event", timeoutEvent);
            assertTrue("Timeout event type ", timeoutEvent.getTimeout().equals(
                    Timeout.TRANSACTION));
        } catch (Exception ex) {
            logger.error("unexpected exception ", ex);
            ex.printStackTrace();
            fail("unexpected exception");
        }
    }


    public void testServerTransactionForTimeout() {
        try {
            Request invite = createRiInviteRequest(null, null, null);
            ClientTransaction tran = null;
            try {
                eventCollector.collectRequestEvent(tiSipProvider);
                tran = riSipProvider.getNewClientTransaction(invite);
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
            assertNotNull("RequestEvent not seen at TI", inviteReceivedEvent);
            assertTrue("Server Transaction MUST be null", inviteReceivedEvent
                    .getServerTransaction() == null);
            ServerTransaction st = tiSipProvider
                    .getNewServerTransaction(inviteReceivedEvent.getRequest());
            Response response = tiMessageFactory.createResponse(Response.OK,
                    inviteReceivedEvent.getRequest());
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("123456");
            ContactHeader contact = super.createTiContact();
            response.setHeader(contact);
            st.sendResponse(response);

            eventCollector.collectTimeoutEvent(tiSipProvider);
            waitForTimeout();
            TimeoutEvent timeoutEvent = eventCollector
                    .extractCollectedTimeoutEvent();
            assertNull("Timeout event", timeoutEvent);
            //assertNotNull("Timeout event", timeoutEvent);
            //assertTrue("Timeout event type must be TRANSACTION ", timeoutEvent
                //.getTimeout().equals(Timeout.TRANSACTION));

        } catch (Exception ex) {
            logger.error("unexpected exception ", ex);
            ex.printStackTrace();
            fail("unexpected exception");
        }
    }


    public void testServerTransactionForRetransmissionAlerts() {
        try {
            Request invite = createRiInviteRequest(null, null, null);
            ClientTransaction tran = null;
            tiSipProvider.setAutomaticDialogSupportEnabled(false);
            try {
                eventCollector.collectRequestEvent(tiSipProvider);
                tran = riSipProvider.getNewClientTransaction(invite);
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
            assertNotNull("RequestEvent not seen at TI", inviteReceivedEvent);
            assertTrue("Server Transaction MUST be null", inviteReceivedEvent
                    .getServerTransaction() == null);
            ServerTransaction st = tiSipProvider
                    .getNewServerTransaction(inviteReceivedEvent.getRequest());
            Response response = tiMessageFactory.createResponse(Response.OK,
                    inviteReceivedEvent.getRequest());
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("1234567");
            ContactHeader contact = super.createTiContact();
            response.setHeader(contact);
            st.enableRetransmissionAlerts();
            st.sendResponse(response);
            eventCollector.collectTimeoutEvent(tiSipProvider);
            waitForTimeout();
            TimeoutEvent timeoutEvent = eventCollector
                    .extractCollectedTimeoutEvent();
            assertNotNull("Timeout event not found ", timeoutEvent);
            assertTrue("Timeout event type must be retransmit ", timeoutEvent
                    .getTimeout().equals(Timeout.RETRANSMIT));

        } catch (Exception ex) {
            logger.error("unexpected exception ", ex);
            ex.printStackTrace();
            fail("unexpected exception");
        }
    }
}
