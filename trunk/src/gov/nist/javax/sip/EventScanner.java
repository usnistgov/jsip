
package gov.nist.javax.sip;

import java.util.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import javax.sip.*;
import gov.nist.core.*;
import java.io.*;
import java.text.ParseException;
//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/
/** Event Scanner to deliver events to the Listener.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-04-02 19:36:18 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
class EventScanner implements Runnable {

	private   boolean isStopped;

	protected int refCount;

	private LinkedList pendingEvents;

	private SipStackImpl sipStackImpl;

//ifdef SIMULATION
/*
	private     SimMessageObject pendingEventsShadow;
//endif
*/


	public EventScanner(SipStackImpl sipStackImpl) {
		this.pendingEvents = new LinkedList();
//ifdef SIMULATION
/*
		this.pendingEventsShadow = new SimMessageObject();
		SimThread myThread = new SimThread(this);
//else
*/
		Thread myThread = new Thread(this);
//endif
//
		this.sipStackImpl = sipStackImpl;
		myThread.setName("EventScannerThread");

		myThread.start();

	}


	public void addEvent( EventWrapper eventWrapper ) {

//ifndef SIMULATION
//
		synchronized (this.pendingEvents)
//else
/*
			this.pendingEventsShadow.enterCriticalSection();
			try
//endif
*/ 
			{
			this.pendingEvents.add(eventWrapper);
//ifdef SIMULATION
/*
			this.pendingEventsShadow.doNotify();
//else
*/
			this.pendingEvents.notify();
//endif
//
			}
//ifdef SIMULATION
/*
			finally { this.pendingEventsShadow.leaveCriticalSection(); }
//endif
*/


	}


	/** Stop the event scanner.
	* Decrement the reference count and exit the scanner thread if the ref count
	* goes to 0.
	*/

	public void stop() {

		if (this.refCount >  0 ) this.refCount --;

		if (this.refCount == 0) {
			synchronized( this.pendingEvents) {
				this.isStopped = true;
				this.pendingEvents.notify();
			}
		}

	}


	
	public void run() {
		while (true) {
			EventObject sipEvent = null;
			EventWrapper eventWrapper = null;
//ifndef SIMULATION
//
			synchronized (this.pendingEvents)
//else
/*
				this.pendingEventsShadow.enterCriticalSection();
				try 
//endif
*/ 
				{
				if (pendingEvents.isEmpty()) {
					if (this.isStopped)  {
						if (LogWriter.needsLogging)
						    sipStackImpl.logMessage( "Stopped event scanner!!");
						return;
					}
					try {
//ifdef SIMULATION
/*
						this.pendingEventsShadow.doWait();
//else
*/
						this.pendingEvents.wait();
//endif
//
					} catch (InterruptedException ex) {
						sipStackImpl.logMessage("Interrupted!");
						continue;
					}
				}


				ListIterator iterator = pendingEvents.listIterator();
				while (iterator.hasNext()) {
					eventWrapper = (EventWrapper) iterator.next();
					sipEvent = eventWrapper.sipEvent;
					if (LogWriter.needsLogging) {
						sipStackImpl.logMessage(
							"Processing "
								+ sipEvent
								+ "nevents "
								+ pendingEvents.size());
					}
					SipListener sipListener = ((SipProviderImpl)sipEvent.getSource()).sipListener;
					if (sipEvent instanceof RequestEvent) {
						// this.currentTransaction =
						// (SIPServerTransaction) eventWrapper.transaction;
						// Check if this request has already created a 
						// transaction
						SIPRequest sipRequest =
							(SIPRequest) ((RequestEvent) sipEvent).getRequest();
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
						if (sipStackImpl.isDialogCreated(sipRequest.getMethod()) ||
						    ( (!sipRequest.getMethod().equals(Request.CANCEL)) &&
						      sipStackImpl.getDialog(sipRequest.getDialogId(true)) 
						       == null) ) {
						       SIPTransaction tr = sipStackImpl.findTransaction(sipRequest, true);
							// If transaction already exists bail.
							if (tr != null) {
								if (LogWriter.needsLogging)
									sipStackImpl.logMessage(
										"transaction already exists!");
								continue;
							}
						}
						// Set up a pointer to the transaction.
						sipRequest.setTransaction(eventWrapper.transaction);
						// Processing incoming CANCEL.
						if (sipRequest.getMethod().equals(Request.CANCEL)) {
							SIPTransaction tr =
								sipStackImpl.findTransaction(sipRequest, true);
							if (tr != null
								&& tr.getState()
									== SIPTransaction.TERMINATED_STATE) {
								// If transaction already exists but it is
								// too late to cancel the transaction then 
								// just respond OK to the CANCEL and bail.
								if (LogWriter.needsLogging)
									sipStackImpl.logMessage(
										"Too late to cancel Transaction");
								// send OK and just ignore the CANCEL.
								try {
									tr.sendMessage(
										sipRequest.createResponse(Response.OK));
								} catch (IOException ex) {
									// Ignore?
								}
								continue;
							}
						}

						sipListener.processRequest((RequestEvent) sipEvent);
						
					} else if (sipEvent instanceof ResponseEvent) {
						sipListener.processResponse((ResponseEvent) sipEvent);
						// The original request is not needed except for INVITE
						// transactions -- null the pointers to the transactions so
						// that state may be released.
					        SIPClientTransaction ct = (SIPClientTransaction) 
											eventWrapper.transaction;
						if ( ct != null 
							&& TransactionState.COMPLETED == ct.getState()
							&& ct.getOriginalRequest() != null
							&& !ct.getOriginalRequest().getMethod().equals
							 	(Request.INVITE)) {
							// reduce the state to minimum
							// This assumes that the application will not need
							// to access the request once the transaction is 
							// completed. 
							ct.clearState() ;
						}
					} else if (sipEvent instanceof TimeoutEvent) {
						sipListener.processTimeout((TimeoutEvent) sipEvent);
					} else {
						if (LogWriter.needsLogging)
							sipStackImpl.logMessage("bad event" + sipEvent);
					}
				} // Bug report by Laurent Schwitzer
				pendingEvents.clear();
			} // end of Synchronized block
//ifdef SIMULATION
/*
			finally { this.pendingEventsShadow.leaveCriticalSection(); }
//endif
*/
		} // end While
	}


}
