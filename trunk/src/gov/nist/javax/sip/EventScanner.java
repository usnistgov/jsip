package gov.nist.javax.sip;

import java.util.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import javax.sip.message.*;
import javax.sip.*;
import gov.nist.core.*;
import java.io.*;

/**
 * Event Scanner to deliver events to the Listener.
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.18 $ $Date: 2005-03-16 21:13:09 $
 * 
 * @author M. Ranganathan <mranga@nist.gov><br/>
 * 
 * <a href=" {@docRoot}/uncopyright.html">This code is in the public domain.
 * </a>
 *  
 */
class EventScanner implements Runnable {

	private boolean isStopped;

	protected int refCount;

	// SIPquest: Fix for deadlocks
	private LinkedList pendingEvents = new LinkedList();

	private int[] eventMutex = { 0 };

	private SipStackImpl sipStackImpl;


	public EventScanner(SipStackImpl sipStackImpl) {
		this.pendingEvents = new LinkedList();
		Thread myThread = new Thread(this);
		// This needs to be set to false else the
		// main thread mysteriously exits.
		myThread.setDaemon(false);
		//endif
		//
		this.sipStackImpl = sipStackImpl;
		myThread.setName("EventScannerThread");

		myThread.start();

	}

	public void addEvent(EventWrapper eventWrapper) {

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
		SipListener sipListener = ((SipProviderImpl) sipEvent.getSource()).sipListener;

		if (sipEvent instanceof RequestEvent) {
		  
			// Check if this request has already created a
			// transaction
			SIPRequest sipRequest = (SIPRequest) ((RequestEvent) sipEvent)
					.getRequest();
			  
		    if (LogWriter.needsLogging)
			sipStackImpl.logMessage("deliverEvent : " 
				+ sipRequest.getFirstLine() + 
				" transaction " + eventWrapper.transaction );
			// If this is a dialog creating method for which a server
			// transaction already exists or a method which is
			// not dialog creating and not within an existing dialog
			// (special handling for cancel) then check to see if
			// the listener already created a transaction to handle
			// this request and discard the duplicate request if a
			// transaction already exists. If the listener chose
			// to handle the request statelessly, then the listener
			// will see the retransmission.
			// Note that in both of these two cases, JAIN SIP will allow
			// you to handle the request statefully or statelessly.
			// An example of the latter case is REGISTER and an example
			// of the former case is INVITE.
			if (sipStackImpl.isDialogCreated(sipRequest.getMethod())) {
				SIPServerTransaction tr = (SIPServerTransaction) sipStackImpl
						.findTransaction(sipRequest, true);
				SIPDialog dialog = sipStackImpl.getDialog(sipRequest
						.getDialogId(true));
				synchronized (this) {
					if (tr != null && !tr.passToListener()) {
						if (LogWriter.needsLogging)
							sipStackImpl
									.logMessage("transaction already exists! " + tr);
						return;
					} else if (sipStackImpl.findPendingTransaction(sipRequest) != null) {
						if (LogWriter.needsLogging)
							sipStackImpl
									.logMessage("transaction already exists!!");
						return;
					} else {
						// Put it in the pending list so that if a repeat
						// request comes along it will not get assigned a new
						// transaction
						SIPServerTransaction st = (SIPServerTransaction) eventWrapper.transaction;
						sipStackImpl.putPendingTransaction(st);
					}
				}
			} else if ((!sipRequest.getMethod().equals(Request.CANCEL))
					&& sipStackImpl.getDialog(sipRequest.getDialogId(true)) == null) {
				// not dialog creating and not a cancel.
				// transaction already processed this message.
				SIPTransaction tr = sipStackImpl.findTransaction(sipRequest,
						true);
				// If transaction already exists bail.
				if (tr != null) {
					if (LogWriter.needsLogging)
						sipStackImpl.logMessage("transaction already exists!");
					return;
				}
			}
			// Set up a pointer to the transaction.
			sipRequest.setTransaction(eventWrapper.transaction);
			// Processing incoming CANCEL.
			if (sipRequest.getMethod().equals(Request.CANCEL)) {
				SIPTransaction tr = sipStackImpl.findCancelTransaction(sipRequest,
						true);
				if (tr != null
						&& tr.getState() == SIPTransaction.TERMINATED_STATE) {
					// If transaction already exists but it is
					// too late to cancel the transaction then
					// just respond OK to the CANCEL and bail.
					if (LogWriter.needsLogging)
						sipStackImpl
								.logMessage("Too late to cancel Transaction");
					// send OK and just ignore the CANCEL.
					try {
						tr.sendMessage(sipRequest.createResponse(Response.OK));
					} catch (IOException ex) {
						// Ignore?
					}
					return;
				} 
				if (LogWriter.needsLogging)
						sipStackImpl.logMessage("Cancel transaction = " + tr );
				
			}

			// Change made by SIPquest
			try {

				if (LogWriter.needsLogging) {
					sipStackImpl.logMessage("Calling listener "
							+ sipRequest.getRequestURI());
					sipStackImpl.logMessage("Calling listener " + eventWrapper.transaction);
                                }
				if (sipListener != null)
					sipListener.processRequest((RequestEvent) sipEvent);
				sipStackImpl
						.removePendingTransaction((SIPServerTransaction) eventWrapper.transaction);
				if (eventWrapper.transaction != null) {
					SIPDialog dialog = (SIPDialog) eventWrapper.transaction
							.getDialog();
					// Tell the dialog that request has been consumed - this
					// copies the remote seqno
					if (dialog != null)
						dialog.requestConsumed();
				}
			} catch (Exception ex) {
				// We cannot let this thread die under any
				// circumstances. Protect ourselves by logging
				// errors to the console but continue.
				ex.printStackTrace();
			}
			if (eventWrapper.transaction != null) {
				((SIPServerTransaction) eventWrapper.transaction)
						.clearPending();
			}

		} else if (sipEvent instanceof ResponseEvent) {
			// Change made by SIPquest
			try {
				if (LogWriter.needsLogging) {
					SIPResponse sipResponse = (SIPResponse) ((ResponseEvent) sipEvent)
							.getResponse();
					sipStackImpl.logMessage("Calling listener for "
							+ sipResponse.getFirstLine());
				}
				if (sipListener != null)
					sipListener.processResponse((ResponseEvent) sipEvent);
			} catch (Exception ex) {
				// We cannot let this thread die under any
				// circumstances. Protect ourselves by logging
				// errors to the console but continue.
				ex.printStackTrace();
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
			if (ct != null) {
				// If responses have been received in the window
				// notify the pending response thread so he can take care of it.
				// cannot do this in the context of the current thread else it
				// will deadlock.
				ct.clearPending();
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
				ex.printStackTrace();
			}
		} else {
			if (LogWriter.needsLogging)
				sipStackImpl.logMessage("bad event" + sipEvent);
		}

	}

	/**
	 * For the non-re-entrant listener this delivers the events to the listener
	 * from a single queue. If the listener is re-entrant, then the stack just
	 * calls the deliverEvent method above.
	 */

	public void run() {
		while (true) {
			EventObject sipEvent = null;
			EventWrapper eventWrapper = null;

			LinkedList eventsToDeliver;
			synchronized (this.eventMutex) {
				// First, wait for some events to become available.
				while (pendingEvents.isEmpty()) {
					// There's nothing in the list, check to make sure we
					// haven't
					// been stopped. If we have, then let the thread die.
					if (this.isStopped) {
						if (LogWriter.needsLogging)
							sipStackImpl.logMessage("Stopped event scanner!!");
						return;
					}

					// We haven't been stopped, and the event list is indeed
					// rather empty. Wait for some events to come along.
					try {
						eventMutex.wait();
					} catch (InterruptedException ex) {
						// Let the thread die a normal death
						sipStackImpl.logMessage("Interrupted!");
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
				if (LogWriter.needsLogging) {
					sipStackImpl.logMessage("Processing " + eventWrapper
							+ "nevents " + eventsToDeliver.size());
				}
				deliverEvent(eventWrapper);

			}
		} // end While
	}
}
