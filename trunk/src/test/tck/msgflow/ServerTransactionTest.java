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
import java.text.*;
import test.tck.*;

/**
 * <p>Title: TCK</p>
 * <p>Description: JAIN SIP 1.1 Technology Compatibility Kit</p>
 * @author Emil Ivov  
 * Network Research Team, Louis Pasteur University, Strasbourg, France.
 * This  code is in the public domain.
 * @version 1.0
 * 
 */

public class ServerTransactionTest extends MessageFlowHarness {

	public ServerTransactionTest(String name) {
		super(name);
	}

	//==================== tests ==============================

	public void testSendResponse() {
		try {
			Request invite = createRiInviteRequest(null, null, null);
			SipEventCollector responseCollector = new SipEventCollector();
			//Send the initial request
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
			RequestEvent inviteReceivedEvent =
				eventCollector.extractCollectedRequestEvent();
			if (inviteReceivedEvent == null
				|| inviteReceivedEvent.getRequest() == null)
				throw new TiUnexpectedError("The initial invite request was not received by the TI!");
			//Let's create the transaction
			ServerTransaction tran = null;
			try {
				tran =
					tiSipProvider.getNewServerTransaction(
						inviteReceivedEvent.getRequest());
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
			//Create & send RINGING. See that it is properly sent
			Response ringing = null;
			try {
				ringing =
					tiMessageFactory.createResponse(
						Response.RINGING,
						tran.getRequest());
                // BUG: set contact header on dialog-creating response
                ringing.setHeader(createTiContact());
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
			
			// reset the collector
			responseCollector.extractCollectedResponseEvent();
			
		} catch (Throwable exc) {
			exc.printStackTrace();
			fail(exc.getClass().getName() + ": " + exc.getMessage());
		}

		assertTrue(new Exception().getStackTrace()[0].toString(), true);
	}

	//==================== end of tests

	//====== STATIC JUNIT ==========
	public static Test suite() {
		return new TestSuite(ServerTransactionTest.class);
	}


}
