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

            // see if creating a dialog matters
            tran.getDialog();

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
                    .getSeqNumber()
                    == ((CSeqHeader) invite.getHeader(CSeqHeader.NAME))
                        .getSeqNumber());
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

            assertEquals( "To tags must match",
                ((ToHeader) invite.getHeader("to")).getTag(),
                ((ToHeader) cancel.getHeader("to")).getTag()
            );

            assertEquals( "From tags must match",
                    ((FromHeader) invite.getHeader("from")).getTag(),
                    ((FromHeader) cancel.getHeader("from")).getTag()
            );

            assertEquals( "Max-Forwards must match",
                    invite.getHeader( MaxForwardsHeader.NAME ),
                    cancel.getHeader( MaxForwardsHeader.NAME )
            );


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

    //mranga: removed testCreateAck -- should not test for deprecated functionality
    // in the TCK.

    //==================== end of tests

    //====== STATIC JUNIT ==========
    public static Test suite() {
        return new TestSuite(ClientTransactionTest.class);
    }

}
