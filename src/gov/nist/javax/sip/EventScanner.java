
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
 * @version JAIN-SIP-1.1 $Revision: 1.1 $ $Date: 2004-03-18 14:40:38 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
class EventScanner implements Runnable {

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
			EventWrapper eventWrapper = new EventWrapper();
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
					if (sipEvent == null) {
						if (LogWriter.needsLogging)
							sipStackImpl.logMessage("Exiting provider thread!");
						return;
					}
					SipListener sipListener = ((SipProviderImpl)sipEvent.getSource()).sipListener;
					if (sipEvent instanceof RequestEvent) {
						// this.currentTransaction =
						// (SIPServerTransaction) eventWrapper.transaction;
						// Check if this request has already created a 
						// transaction
						SIPRequest sipRequest =
							(SIPRequest) ((RequestEvent) sipEvent).getRequest();
						if (sipStackImpl.isDialogCreated(sipRequest.getMethod())) {
							SIPTransaction tr =
								sipStackImpl.findTransaction(sipRequest, true);
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
