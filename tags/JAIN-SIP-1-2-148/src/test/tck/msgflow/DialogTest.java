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
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.2 Technology Compatibility Kit</p>
 * @author Emil Ivov
 *     Network Research Team, Louis Pasteur University, Strasbourg, France.
 * This  code is in the public domain.
 * @version 1.0
 */

public class DialogTest extends MessageFlowHarness {
    /** The Dialog to test.*/
    private Dialog dialog = null;
    /** The request that was sent by the TI*/
    private Request tiInvite = null;
    /** The initial invite request at the RI side.*/
    private Request riInvite = null;
    private ClientTransaction cliTran = null;
    private Response ringing = null;


    private String riToTag; // JvB: to-tag set by RI


    public DialogTest(String name) {
        super(name);
    }

    //==================== add a dialog to the fixture ========
    /**
     * Calls MessageFlowHarness.setUp() and creates a dialog afterwards.
     * @throws Exception if anything goes wrong
     */
    public void setUp() throws Exception {
        try {
            super.setUp();
            tiInvite = createTiInviteRequest(null, null, null);
            //Send an invite request
            try {
                eventCollector.collectRequestEvent(riSipProvider);
                cliTran = tiSipProvider.getNewClientTransaction(tiInvite);
                cliTran.sendRequest();
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
            riInvite = inviteReqEvt.getRequest();
            //get the dialog
            dialog = cliTran.getDialog();
            //Send ringing to initialise dialog
            //start listening for the response
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException e) {
                throw new TiUnexpectedError(
                    "Failed to register a SipListener with the TI.",
                    e);
            }
            try {
                ringing =
                    riMessageFactory.createResponse(
                        Response.RINGING,
                        inviteReqEvt.getRequest());
                ((ToHeader) ringing.getHeader(ToHeader.NAME)).setTag(
                    riToTag = Integer.toString(hashCode()));
                // BUG report from Ben Evans:
                // set contact header on dialog-creating response
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
                throw new TiUnexpectedError("The TI did not dispatch a RINGING response.");
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }

        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    //==================== tests ==============================
    /**
     * Test whether dialog fields are properly set
     */
    public void testDialogProperties() {
        try {
            //CallId
            assertEquals(
                "The Dialog did not have the right Call ID.",
                ((CallIdHeader) riInvite.getHeader(CallIdHeader.NAME))
                    .getCallId(),
                dialog.getCallId().getCallId());
            //Tran
            /* Deprecated
             * assertSame(
                "The Dialog.getTransaction did not return the right transaction.",
                cliTran,
                dialog.getFirstTransaction());
            */
            //LocalParty
            assertEquals(
                "Dialog.getLocalParty() returned a bad address.",
                ((FromHeader) tiInvite.getHeader(FromHeader.NAME)).getAddress(),
                dialog.getLocalParty());
            //SeqNum
            assertTrue(
                "Dialog.getLocalSequenceNumber() returned a bad value.",
                1 == dialog.getLocalSeqNumber());
            //LocalTag
            assertEquals(
                "Dialog.getLocalTag() returned a bad tag",
                ((FromHeader) riInvite.getHeader(FromHeader.NAME)).getTag(),
                dialog.getLocalTag());
            //RemoteParty
            assertEquals(
                "Dialog.getRemoteParty() returned a bad address.",
                ((ToHeader) tiInvite.getHeader(ToHeader.NAME)).getAddress(),
                dialog.getRemoteParty());
            //RemoteTag
                assertEquals(
                "Dialog.getRemoteTag() returned a bad tag",
            ((ToHeader) ringing.getHeader(ToHeader.NAME)).getTag(),
                dialog.getRemoteTag());
            //is server
            assertFalse(
                "Dialog.isServer returned true for a client side dialog",
                dialog.isServer());
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }

        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    /**
     * Create a BYE request and check whether major fields are properly set
     */
    public void testCreateRequest() {
        try {

          // JvB: First abort the INVITE, this should trigger a TransactionTerminatedEvent
          cliTran.terminate();

            Request bye = null;
            try {
                bye = dialog.createRequest(Request.BYE);
            } catch (SipException ex) {
                ex.printStackTrace();
                fail("A dialog failed to create a BYE request.");
            }
            //check method
            assertEquals(
                "Dialog.createRequest() returned a request with a bad method.",
                Request.BYE,
                bye.getMethod());
            //check CSeq number
            assertEquals(
                "Dialog.createRequest() returned a request with a bad sequence number.",
                dialog.getLocalSeqNumber() + 1,
                ((CSeqHeader) bye.getHeader(CSeqHeader.NAME))
                    .getSeqNumber());
            //Check From
            FromHeader byeFrom = (FromHeader) bye.getHeader(FromHeader.NAME);
            assertEquals(
                "Dialog.createRequest() returned a request with a bad From header.",
                dialog.getLocalParty(),
                byeFrom.getAddress());
            //Check From tags
            assertEquals(
                "Dialog.createRequest() returned a request with a bad From tag.",
                dialog.getLocalTag(),
                byeFrom.getTag());
            //Check To
            ToHeader byeTo = (ToHeader) bye.getHeader(ToHeader.NAME);
            assertEquals(
                "Dialog.createRequest() returned a request with a bad To header.",
                dialog.getRemoteParty(),
                byeTo.getAddress());
            //Check To tags
            assertEquals(
                "Dialog.createRequest() returned a request with a bad To tag.",
                dialog.getRemoteTag(),
                byeTo.getTag());
            ClientTransaction ct = super.tiSipProvider.getNewClientTransaction(bye);

      // JvB: set the SipListener before sending the BYE
            try {
                eventCollector.collectDialogTermiatedEvent(tiSipProvider);
            } catch( TooManyListenersException ex) {
                throw new TckInternalError("failed to regiser a listener iwth the TI", ex);
            }

            dialog.sendRequest(ct);
            assertEquals("Dialog mismatch ", ct.getDialog(),dialog );
            waitForMessage();

            waitForTimeout();
            DialogTerminatedEvent dte = eventCollector.extractCollectedDialogTerminatedEvent();
            // Should see a DTE here also for early Dialog
            assertNotNull("No DTE received for early Dialog terminated via BYE", dte);

        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    /**
     * Steer the dialog to a CONFIRMED state and try sending an ack
     * An invite has been sent by the TI in the setUp method so we take it from
     * that point on.
     */
    public void testSendAck() {
        this.doTestSendAck(false);
    }

    /**
     * Regression test for broken clients that send ACK to 2xx with same branch as INVITE
  
    public void testSendAckWithSameBranch() {
        this.doTestSendAck(true);
    }
    */

    private void doTestSendAck( boolean sameBranch ) {
    	
    	System.out.println("doTestSendAck " + sameBranch);
        try {
           
            //We will now send an OK response
            //start listening for the response
            try {
                eventCollector.collectResponseEvent(tiSipProvider);
            } catch (TooManyListenersException e) {
                throw new TckInternalError(
                    "Failed to register a SipListener with the RI.",
                    e);
            }
            Response ok = null;
            try {
                ok = riMessageFactory.createResponse(Response.OK, riInvite);
                ok.addHeader(
                    createRiInviteRequest(null, null, null).getHeader(
                        ContactHeader.NAME));
                ToHeader okToHeader = (ToHeader)ok.getHeader(ToHeader.NAME);
                okToHeader.setTag( riToTag );// needs same tag as 180 ringing!
                // riSipProvider.sendResponse(ok);

                // Need to explicitly create the dialog on RI side
                ServerTransaction riST = riSipProvider.getNewServerTransaction( riInvite );
                riST.getDialog();
                riST.sendResponse( ok );
            } catch (Exception e) {
                throw new TckInternalError(
                    "Failed to create and send an OK response",
                    e);
            }
            waitForMessage();
            ResponseEvent okRespEvt =
                eventCollector.extractCollectedResponseEvent();
            if (okRespEvt == null || okRespEvt.getResponse() == null)
                throw new TiUnexpectedError("The TI did not dispatch an OK response.");
            
            String okBranch = 
                ((ViaHeader) okRespEvt.getResponse().getHeader(ViaHeader.NAME)).getBranch();
            

            // After 2xx, dialog should be in CONFIRMED state. Needed to send ACK
            assertEquals( DialogState.CONFIRMED, dialog.getState() );

            //Send the ack
            //Setup the ack listener
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                    "Failed to register a SipListener with the RI",
                    ex);
            }
            Request ack = null;
            try {
                CSeqHeader cseq = (CSeqHeader) okRespEvt.getResponse().getHeader(CSeqHeader.NAME);
                ack = dialog.createAck(cseq.getSeqNumber());
                //System.out.println( "Created ACK:" + ack );
                //System.out.println( "original INVITE:" + riInvite );

                // This is wrong according to RFC3261, but some clients do this...
                if (sameBranch) {
                    ViaHeader via = (ViaHeader) ack.getHeader("Via");
                    via.setBranch( ((ViaHeader)riInvite.getHeader("Via")).getBranch() );
                } 

            } catch (SipException ex) {
                throw new TiUnexpectedError(
                    "Failed to create an ACK request.",
                    ex);
            }
            try {
                dialog.sendAck(ack);
            } catch (Throwable ex) {
                ex.printStackTrace();
                fail("SipException; Failed to send an ACK request using Dialog.sendAck()");
            }
            waitForMessage();
            
           

            // Did the RI get the ACK? If the dialog is not found, the ACK is filtered!
            RequestEvent ackEvt = eventCollector.extractCollectedRequestEvent();
            assertNotNull(
                "No requestEvent sent by Dialog.sendAck() was received by the RI",
                ackEvt);
            assertNotNull(
                "The request sent by Dialog.sendAck() was not received by the RI",
                ackEvt.getRequest());
            if ( !sameBranch ) {
                String ackBranchId = ((ViaHeader) ackEvt.getRequest().getHeader(ViaHeader.NAME)).getBranch();
                super.assertNotSame("ACK branch ID must differ from INVITE OK branch ID", ackBranchId,
                        okBranch);
            }
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    public void testSendRequest() {
        try {
            Request reInvite = null;
            ClientTransaction reInviteTran = null;
            //Create
            try {
                reInvite = dialog.createRequest(Request.INVITE);
                reInviteTran = tiSipProvider.getNewClientTransaction(reInvite);
            } catch (Exception ex) {
                throw new TiUnexpectedError(
                    "Failed to create a CANCEL request with Dialog.createRequest()",
                    ex);
            }
            //Listen
            try {
                eventCollector.collectRequestEvent(riSipProvider);
            } catch (TooManyListenersException ex) {
                throw new TckInternalError(
                    "Failed to register a SipListener with the RI",
                    ex);
            }
            //Send
            try {
                dialog.sendRequest(reInviteTran);
            } catch (SipException ex) {
                ex.printStackTrace();
                fail("Failed to send a cancel request using Dialog.sendRequest()");
            }
            waitForMessage();
            //Did they get it?
            RequestEvent cancelEvt =
                eventCollector.extractCollectedRequestEvent();
            assertNotNull("The RI did not receive the sent request", cancelEvt);
            assertNotNull(
                "The RI did not receive the sent request",
                cancelEvt.getRequest());
        } catch (Throwable exc) {
            exc.printStackTrace();
            fail(exc.getClass().getName() + ": " + exc.getMessage());
        }
        assertTrue(new Exception().getStackTrace()[0].toString(), true);

    }

    //==================== end of tests

    //====== STATIC JUNIT ==========
    public static Test suite() {
        return new TestSuite(DialogTest.class);
    }


}
