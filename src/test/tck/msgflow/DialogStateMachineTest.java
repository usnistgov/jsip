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
        super(name,true);   // enable auto-dialog support
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
                    + " state before receiving any response!",
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
                // BUG: set contact header on dialog-creating response
                ringing.setHeader(createRiContact());
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

            // JvB: @todo Test that UPDATE (or PRACK) requests during early dialog
            // dont make dialog CONFIRMED
            /*
            Request update = null;
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                update = dialog.createRequest( "UPDATE" );
                update.setHeader(createTiContact());
                dialog.sendRequest( tiSipProvider.getNewClientTransaction(update) );
            } catch (Exception e) {
                throw new TckInternalError(
                    "Failed to create and send an UPDATE request",
                    e);
            }
            waitForMessage();
            RequestEvent updateEvt =
                eventCollector.extractCollectedRequestEvent();
            if (updateEvt == null || updateEvt.getRequest() == null)
                throw new TiUnexpectedError("The TI did not send the UPDATE request");
            //The dialog should still be in its EARLY state.
            assertEquals(
                "The Dialog did not stay in the EARLY state upon reception of an UPDATE request",
                DialogState.EARLY,
                dialog.getState());

            // finish the UPDATE
            riSipProvider.sendResponse( riMessageFactory.createResponse(200, updateEvt.getRequest()) );
            */

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
                // BUG: set contact header on dialog-creating response
                ok.setHeader(createRiContact());
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
            Request bye = null;
            try {
                bye = dialog.createRequest(Request.BYE);
                ClientTransaction byeTran =
                    tiSipProvider.getNewClientTransaction(bye);
                dialog.sendRequest(byeTran);
            } catch (SipException e) {
                throw new TiUnexpectedError(
                    "Failed to create and send a BYE request using a dialog.",
                    e);
            }
            // Send response before checking that the state goes to
            // terminated state.
            waitForMessage();
            tiSipProvider.sendResponse(tiMessageFactory.createResponse(200,bye));
            waitForMessage();
            assertTrue(
                "The dialog did not pass into a final ( TERMINATED) state after getting OK for a BYE.",
                 DialogState.TERMINATED.equals(dialog.getState()));
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
                inviteTransaction = riSipProvider.getNewClientTransaction(invite);


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

            // We should have a null state here
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
                // BUG report by Ben Evans (Open cloud):
                // set contact header on dialog-creating response
                ringing.setHeader(createTiContact());
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
            SipEventCollector tteCollector = new SipEventCollector();
            try {
                eventCollector.collectResponseEvent(riSipProvider);

                // JvB: Also receive TransactionTerminated event
                tteCollector.collectTransactionTermiatedEvent(tiSipProvider);
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
                // Bug - need to set to-tag on OK response
                // NIST-SIP fills in the to-tag but this behaviour is
                // not specified
                ((ToHeader) ok.getHeader(ToHeader.NAME)).setTag(
                    Integer.toString(hashCode()));
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
            ClientTransaction ct = okRespEvt.getClientTransaction();

            // JvB: With auto-dialog-support OFF, this returns *null* !
            Dialog clientDialog = ct.getDialog();
            assertNotNull( clientDialog );
            Request ackReq = clientDialog.createAck( ( (CSeqHeader)okRespEvt.getResponse().getHeader(CSeqHeader.NAME)).getSeqNumber());;
            clientDialog.sendAck(ackReq);
            waitForMessage();
            //The dialog should now be in its CONFIRMED state.
            assertEquals(
                "The Dialog did not pass into the CONFIRMED state upon reception of an OK response",
                DialogState.CONFIRMED,
                dialog.getState());

            // After sending 2xx, there should be a ServerTransactionTerminated event at UAS side
            waitForTimeout();   // May take up to 64*T1 seconds
            TransactionTerminatedEvent tte = tteCollector.extractCollectedTransactionTerminatedEvent();
            assertNotNull( tte );
            assertTrue( tte.isServerTransaction() );
            assertEquals( tran, tte.getServerTransaction() );

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
                ct = riSipProvider.getNewClientTransaction(bye);
                d.sendRequest(ct);

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


}
