/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
package gov.nist.javax.sip;

import java.util.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import javax.sip.message.*;
import javax.sip.*;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;
import gov.nist.core.*;
import java.io.*;
/*bug fixes SIPQuest  communications and Shu-Lin Chen. */

/**
 * Event Scanner to deliver events to the Listener.
 * 
 * @version 1.2 $Revision: 1.22 $ $Date: 2006-07-13 09:02:51 $
 * 
 * @author M. Ranganathan <br/> 
 * 
 * 
 */
class EventScanner implements Runnable {

	private boolean isStopped;

	protected int refCount;

	// SIPquest: Fix for deadlocks
	private LinkedList pendingEvents = new LinkedList();

	private int[] eventMutex = { 0 };

	private SipStackImpl sipStack;

	public EventScanner(SipStackImpl sipStackImpl) {
		this.pendingEvents = new LinkedList();
		Thread myThread = new Thread(this);
		// This needs to be set to false else the
		// main thread mysteriously exits.
		myThread.setDaemon(false);

		this.sipStack = sipStackImpl;

		myThread.setName("EventScannerThread");

		myThread.start();

	}

	public void addEvent(EventWrapper eventWrapper) {
		sipStack.getLogWriter().logDebug("addEvent " + eventWrapper);
		synchronized (this.eventMutex) {

			pendingEvents.add(eventWrapper);

			// Add the event into the pending events list

			eventMutex.notify();
		}

	}

	/**
	 * Stop the event scanner. Decrement the reference count and exit the
	 * scanner thread if the ref count goes to 0.
	 */

	public void stop() {

		if (this.refCount > 0)
			this.refCount--;

		if (this.refCount == 0) {
			synchronized (eventMutex) {
				isStopped = true;
				eventMutex.notify();
			}
		}
	}

	public void deliverEvent(EventWrapper eventWrapper) {
		EventObject sipEvent = eventWrapper.sipEvent;
		if (sipStack.isLoggingEnabled())
			sipStack.getLogWriter().logDebug(
				"sipEvent = " + sipEvent + "source = " + sipEvent.getSource());
		SipListener sipListener = null;

		if (!(sipEvent instanceof IOExceptionEvent)) {
			sipListener = ((SipProviderImpl) sipEvent.getSource()).sipListener;
		} else {
			sipListener = sipStack.getSipListener();
		}

		if (sipEvent instanceof RequestEvent) {
			try {
				// Check if this request has already created a
				// transaction
				SIPRequest sipRequest = (SIPRequest) ((RequestEvent) sipEvent)
						.getRequest();

				if (sipStack.isLoggingEnabled()) {
					sipStack.getLogWriter().logDebug(
							"deliverEvent : "
									+ sipRequest.getFirstLine()
									+ " transaction "
									+ eventWrapper.transaction
									+ " sipEvent.serverTx = "
									+ ((RequestEvent) sipEvent)
											.getServerTransaction());
				}

				// Discard the duplicate request if a
				// transaction already exists. If the listener chose
				// to handle the request statelessly, then the listener
				// will see the retransmission.
				// Note that in both of these two cases, JAIN SIP will allow
				// you to handle the request statefully or statelessly.
				// An example of the latter case is REGISTER and an example
				// of the former case is INVITE.

				SIPServerTransaction tx = (SIPServerTransaction) sipStack
						.findTransaction(sipRequest, true);

				if (tx != null && !tx.passToListener()) {
				  
				  // JvB: make an exception for a very rare case: some (broken) UACs use 
				  // the  same branch parameter for an ACK. Such an ACK should be passed
				  // to the listener (tx == INVITE ST, terminated upon sending 2xx but
				  // lingering to catch retransmitted INVITEs)
				  if (sipRequest.getMethod().equals(Request.ACK) 
				    && tx.isInviteTransaction()) {

  					if (sipStack.isLoggingEnabled())
  						sipStack.getLogWriter().logDebug(
  								"Detected broken client sending ACK with same branch! Passing..." );				      
				  } else {
  					if (sipStack.isLoggingEnabled())
  						sipStack.getLogWriter().logDebug(
  								"transaction already exists! " + tx);
  					return;
  				}
				} else if (sipStack.findPendingTransaction(sipRequest) != null) {
					if (sipStack.isLoggingEnabled())
						sipStack.getLogWriter().logDebug(
								"transaction already exists!!");

					return;
				} else {
					// Put it in the pending list so that if a repeat
					// request comes along it will not get assigned a
					// new transaction
					SIPServerTransaction st = (SIPServerTransaction) eventWrapper.transaction;
					sipStack.putPendingTransaction(st);
				}

				

				// Set up a pointer to the transaction.
				sipRequest.setTransaction(eventWrapper.transaction);
				// Change made by SIPquest
				try {

					if (sipStack.isLoggingEnabled()) {
						sipStack.getLogWriter()
								.logDebug(
										"Calling listener "
												+ sipRequest.getFirstLine());
						sipStack.getLogWriter().logDebug(
								"Calling listener " + eventWrapper.transaction);
					}
					if (sipListener != null)
						sipListener.processRequest((RequestEvent) sipEvent);

					if (sipStack.isLoggingEnabled()) {
						sipStack.getLogWriter().logDebug(
								"Done processing Message "
										+ sipRequest.getFirstLine());
					}
					if (eventWrapper.transaction != null) {
						
						SIPDialog dialog = (SIPDialog) eventWrapper.transaction.getDialog();
						if (dialog != null)
							dialog.requestConsumed();
					
					}
				} catch (Exception ex) {
					// We cannot let this thread die under any
					// circumstances. Protect ourselves by logging
					// errors to the console but continue.
					sipStack.getLogWriter().logException(ex);
				}
			} finally {
				if (sipStack.isLoggingEnabled()) {
					sipStack.getLogWriter().logDebug(
							"Done processing Message "
									+ ((SIPRequest) (((RequestEvent) sipEvent)
											.getRequest())).getFirstLine());
				}
				if (eventWrapper.transaction != null &&
						((SIPServerTransaction) eventWrapper.transaction)
						.passToListener()) {
					((SIPServerTransaction) eventWrapper.transaction)
							.releaseSem();
				}
			
				if (eventWrapper.transaction != null ) 
					sipStack
						.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
				if ( eventWrapper.transaction.getOriginalRequest().getMethod().equals(Request.ACK)) {
					// Set the tx state to terminated so it is removed from the stack
					// if the user configured to get notification on ACK termination
					eventWrapper.transaction.setState(TransactionState.TERMINATED);
				}
			}

		} else if (sipEvent instanceof ResponseEvent) {
			try {
				ResponseEvent responseEvent = (ResponseEvent) sipEvent;
				SIPResponse sipResponse = (SIPResponse) responseEvent
						.getResponse();
				SIPDialog sipDialog = ((SIPDialog) responseEvent.getDialog());
				try {
					if (sipStack.isLoggingEnabled()) {

						sipStack.getLogWriter().logDebug(
								"Calling listener for "
										+ sipResponse.getFirstLine());
					}
					if (sipListener != null) {
						SIPTransaction tx = eventWrapper.transaction;
						if (tx != null) {
							tx.setPassToListener();
						}
						sipListener.processResponse((ResponseEvent) sipEvent);
					}

					/*
					 * If the response for a request within a dialog is a 481
					 * (Call/Transaction Does Not Exist) or a 408 (Request
					 * Timeout), the UAC SHOULD terminate the dialog.
					 */
					if ((sipDialog != null && ( sipDialog.getState() == null || !sipDialog.getState().equals(
							DialogState.TERMINATED)))
							&& (sipResponse.getStatusCode() == Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST || sipResponse
									.getStatusCode() == Response.REQUEST_TIMEOUT)) {
						if (sipStack.getLogWriter().isLoggingEnabled()) {
							sipStack.getLogWriter().logDebug(
									"Removing dialog on 408 or 481 response");
						}
						sipDialog.doDeferredDelete();
					}

					/*
					 * The Client tx disappears after the first 2xx response
					 * However, additional 2xx responses may arrive later for
					 * example in the following scenario:
					 * 
					 * Multiple 2xx responses may arrive at the UAC for a single
					 * INVITE request due to a forking proxy. Each response is
					 * distinguished by the tag parameter in the To header
					 * field, and each represents a distinct dialog, with a
					 * distinct dialog identifier.
					 * 
					 * If the Listener does not ACK the 200 then we assume he
					 * does not care about the dialog and gc the dialog after some
					 * time.
					 * 
					 */
					if (sipResponse.getCSeq().getMethod()
							.equals(Request.INVITE)
							&& sipDialog != null
							&& sipResponse.getStatusCode() == 200
							&& sipDialog.getLastAck() == null) {
						if (sipStack.getLogWriter().isLoggingEnabled()) {
							sipStack.getLogWriter().logDebug(
									"Garbage collecting unacknowledged dialog");
						}
						sipDialog.doDeferredDelete();

					}
				} catch (Exception ex) {
					// We cannot let this thread die under any
					// circumstances. Protect ourselves by logging
					// errors to the console but continue.
					sipStack.getLogWriter().logException(ex);
				}
				// The original request is not needed except for INVITE
				// transactions -- null the pointers to the transactions so
				// that state may be released.
				SIPClientTransaction ct = (SIPClientTransaction) eventWrapper.transaction;
				if (ct != null
						&& TransactionState.COMPLETED == ct.getState()
						&& ct.getOriginalRequest() != null
						&& !ct.getOriginalRequest().getMethod().equals(
								Request.INVITE)) {
					// reduce the state to minimum
					// This assumes that the application will not need
					// to access the request once the transaction is
					// completed.
					ct.clearState();
				}
				// mark no longer in the event queue.
			} finally {
				if (eventWrapper.transaction != null
						&& eventWrapper.transaction.passToListener()) {
					eventWrapper.transaction.releaseSem();
				}
			}

		} else if (sipEvent instanceof TimeoutEvent) {
			// Change made by SIPquest
			try {
				// Check for null as listener could be removed.
				if (sipListener != null)
					sipListener.processTimeout((TimeoutEvent) sipEvent);
			} catch (Exception ex) {
				// We cannot let this thread die under any
				// circumstances. Protect ourselves by logging
				// errors to the console but continue.
				sipStack.getLogWriter().logException(ex);
			}

		} else if (sipEvent instanceof IOExceptionEvent) {
			try {
				if (sipListener != null)
					sipListener.processIOException((IOExceptionEvent) sipEvent);
			} catch (Exception ex) {
				sipStack.getLogWriter().logException(ex);
			}
		} else if (sipEvent instanceof TransactionTerminatedEvent) {
			try {
				if (sipStack.isLoggingEnabled()) {
					sipStack.getLogWriter().logDebug(
							"About to deliver transactionTerminatedEvent");
					sipStack.getLogWriter().logDebug("tx = " + 
							((TransactionTerminatedEvent) sipEvent).getClientTransaction());
					sipStack.getLogWriter().logDebug("tx = " + 
							((TransactionTerminatedEvent) sipEvent).getServerTransaction());
					
				}
				if (sipListener != null)
					sipListener
							.processTransactionTerminated((TransactionTerminatedEvent) sipEvent);
			} catch (Exception ex) {
				sipStack.getLogWriter().logException(ex);
			}
		} else if (sipEvent instanceof DialogTerminatedEvent) {
			try {
				if (sipListener != null)
					sipListener
							.processDialogTerminated((DialogTerminatedEvent) sipEvent);
			} catch (Exception ex) {
				sipStack.getLogWriter().logException(ex);
			}
		} else {

			sipStack.getLogWriter().logFatalError("bad event" + sipEvent);
		}

	}

	/**
	 * For the non-re-entrant listener this delivers the events to the listener
	 * from a single queue. If the listener is re-entrant, then the stack just
	 * calls the deliverEvent method above.
	 */

	public void run() {
		while (true) {
			EventWrapper eventWrapper = null;

			LinkedList eventsToDeliver;
			synchronized (this.eventMutex) {
				// First, wait for some events to become available.
				while (pendingEvents.isEmpty()) {
					// There's nothing in the list, check to make sure we
					// haven't
					// been stopped. If we have, then let the thread die.
					if (this.isStopped) {
						if (sipStack.isLoggingEnabled())
							sipStack.getLogWriter().logDebug(
									"Stopped event scanner!!");
						return;
					}

					// We haven't been stopped, and the event list is indeed
					// rather empty. Wait for some events to come along.
					try {
						eventMutex.wait();
					} catch (InterruptedException ex) {
						// Let the thread die a normal death
						sipStack.getLogWriter().logDebug("Interrupted!");
						return;
					}
				}

				// There are events in the 'pending events list' that need
				// processing. Hold onto the old 'pending Events' list, but
				// make a new one for the other methods to operate on. This
				// tap-dancing is to avoid deadlocks and also to ensure that
				// the list is not modified while we are iterating over it.
				eventsToDeliver = pendingEvents;
				pendingEvents = new LinkedList();
			}
			ListIterator iterator = eventsToDeliver.listIterator();
			while (iterator.hasNext()) {
				eventWrapper = (EventWrapper) iterator.next();
				if (sipStack.isLoggingEnabled()) {
					sipStack.getLogWriter().logDebug(
							"Processing " + eventWrapper + "nevents "
									+ eventsToDeliver.size());
				}
				deliverEvent(eventWrapper);

			}
		} // end While
	}
}
