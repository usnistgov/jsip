/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/

package gov.nist.javax.sip;
import java.util.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.stack.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;
import javax.sip.*;
import gov.nist.core.*;
import java.io.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;
//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/



/** Implementation of the JAIN-SIP provider interface.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class SipProviderImpl implements Runnable,
javax.sip.SipProvider, SIPTransactionEventListener {
    
    protected   SipListener sipListener;
    
    private     LinkedList pendingEvents;

//ifdef SIMULATION
/*
    private     SimMessageObject pendingEventsShadow;
//endif
*/
    
    protected boolean isActive;
    
    private     SIPTransactionStack sipStack;
    
    protected   SipStackImpl sipStackImpl;
    
    protected  ListeningPointImpl listeningPoint;
    
    
    
    /** Stop processing messages for this provider. Post an empty message to our
     *message processing queue that signals us to quit.
     */
    protected  void stop() {
        // Put an empty event in the queue and post ourselves a message.
        if(LogWriter.needsLogging)
            sipStackImpl.logMessage("Exiting provider" );
        synchronized (this) {
            listeningPoint.removeSipProvider();
        }
//ifndef SIMULATION
//
        synchronized ( this.pendingEvents) 
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
    
    
    /**
     * Handle the SIP event - because we have only one listener and we are
     * already in the context of a separate thread, we dont need to enque
     * the event and signal another thread.
     *
     *@param sipEvent is the event to process.
     *
     */
    
    public void handleEvent(EventObject sipEvent,
    SIPTransaction transaction ) {
        if (LogWriter.needsLogging) {
            sipStackImpl.logMessage("handleEvent " + sipEvent +
            "currentTransaction = " + transaction +
            "this.sipListener = " + this.sipListener );
	     sipStackImpl.logStackTrace();
        }
	
        if (this.sipListener == null) {
		if (LogWriter.needsLogging) {
		  sipStackImpl.logMessage("Dropping listener = null");
		}

		return;
	}

//ifndef SIMULATION
//
        synchronized ( this.pendingEvents ) 
//else
/*
	this.pendingEventsShadow.enterCriticalSection();
	try
//endif
*/
      {
            EventWrapper eventWrapper = new EventWrapper();
            eventWrapper.sipEvent = sipEvent;
            eventWrapper.transaction = transaction;
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
    
    
    
    
    
    
    /** Creates a new instance of SipProviderImpl */
    protected SipProviderImpl() {
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
	// myThread.setPriority(Thread.MAX_PRIORITY);
        myThread.start();
    }
    
    protected Object clone() throws java.lang.CloneNotSupportedException {
        throw new java.lang.CloneNotSupportedException();
    }
    
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    public void run() {
        while(true) {
            EventObject sipEvent = null;
            EventWrapper eventWrapper = null;
//ifndef SIMULATION
//
            synchronized ( this.pendingEvents) 
//else
/*
	    this.pendingEventsShadow.enterCriticalSection();
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
                        sipStackImpl.logMessage("Interrupted!" );
                        continue;
                    }
                }
                ListIterator iterator = pendingEvents.listIterator();
                while (iterator.hasNext()) {
                    eventWrapper = (EventWrapper) iterator.next();
                    sipEvent = eventWrapper.sipEvent;
                    if (LogWriter.needsLogging) {
                        sipStackImpl.logMessage("Processing " + sipEvent +
                        "nevents "  + pendingEvents.size());
                    }
                    if (sipEvent == null) {
                        if(LogWriter.needsLogging)
                            sipStackImpl.logMessage
				("Exiting provider thread!" );
                        listeningPoint.removeSipProvider();
                        return;
                    }
                    if (sipEvent instanceof RequestEvent) {
                        // this.currentTransaction =
                        // (SIPServerTransaction) eventWrapper.transaction;
                        // Check if this request has already created a 
			// transaction
                        SIPRequest sipRequest =
                        (SIPRequest) ((RequestEvent) sipEvent).getRequest();
                        if (sipStack.isDialogCreated(sipRequest.getMethod())) {
                            SIPTransaction tr =
                            sipStack.findTransaction(sipRequest,true);
                            // If transaction already exists bail.
                            if (tr != null) {
                                if (LogWriter.needsLogging)
                                    sipStackImpl.logMessage
                                    ("transaction already exists!");
                                continue;
                            }
                        }
			// Set up a pointer to the transaction.
			sipRequest.setTransaction(eventWrapper.transaction);
			// Processing incoming CANCEL.
		        if (sipRequest.getMethod().equals(Request.CANCEL) ) {
                            SIPTransaction tr =
                            sipStack.findTransaction(sipRequest,true);
                            if (tr != null && 
				tr.getState().getValue() == 
				SIPTransaction.TERMINATED_STATE ) {
                                // If transaction already exists but it is
				// too late to cancel the transaction then 
				// just respond OK to the CANCEL and bail.
                                if (LogWriter.needsLogging)
                                    sipStackImpl.logMessage
                                    ("Too late to cancel Transaction");
				// send OK and just ignore the CANCEL.
				 try {
				  tr.sendMessage
				  (sipRequest.createResponse(Response.OK));
				} catch (IOException ex) {
				  // Ignore?
				}
                                continue;
                            }
			}
			     
                        
                        sipListener.processRequest((RequestEvent) sipEvent);
                    } else if (sipEvent instanceof ResponseEvent) {
                        sipListener.processResponse((ResponseEvent) sipEvent);
                    } else if (sipEvent instanceof TimeoutEvent) {
                        sipListener.processTimeout((TimeoutEvent) sipEvent);
                    } else {
                        if (LogWriter.needsLogging)
                            sipStackImpl.logMessage("bad event" + sipEvent);
                    }
                } // Bug report by Laurent Schwitzer
                pendingEvents.clear();
            }// end of Synchronized block
//ifdef SIMULATION
/*
	    this.pendingEventsShadow.leaveCriticalSection();
//endif
*/
        } // end While
    }
    
    
    
    
    
    
    /** This method registers the SipListener object to this SipProvider, once
     * registered the SIP Listener can send events on the SipProvider and
     * recieve events emitted from the SipProvider. As JAIN SIP resticts a
     * unicast Listener special case, that is, that one and only one Listener
     * may be registered on the SipProvider concurrently.
     * <p>
     * If an attempt is made to re-register the existing SipListener this
     * method returns silently. A previous SipListener must be removed from the
     * SipProvider before another SipListener can be registered to
     * the SipProvider.
     *
     * @param <var>SipListener</var> SipListener to be registered with the
     * Provider.
     * @throws TooManyListenersException this exception is thrown when a new
     * SipListener attempts to register with the SipProvider when another
     * SipListener is already registered with this SipProvider.
     *
     */
    public void addSipListener(SipListener sipListener)
    throws TooManyListenersException {
        
        synchronized(sipStackImpl) {
            Iterator it = sipStackImpl.getSipProviders();
            while (it.hasNext()) {
                SipProviderImpl provider = (SipProviderImpl) it.next();
                if (provider.sipListener  != null &&
                provider.sipListener != sipListener)
                    throw new TooManyListenersException();
            }
        }
        if (LogWriter.needsLogging)
            sipStackImpl.logMessage("add SipListener " + sipListener);
        this.sipListener = sipListener;
        synchronized(sipStackImpl) {
            Iterator it = sipStackImpl.getSipProviders();
            while (it.hasNext()) {
                SipProviderImpl provider = (SipProviderImpl) it.next();
                provider.sipListener = sipListener;
            }
        }
    }
    
    /** Returns the ListeningPoint of this SipProvider.
     * A SipProvider has a single Listening Point at any specific point in time.
     *
     * @see ListeningPoint
     * @return the ListeningPoint of this SipProvider
     */
    public ListeningPoint getListeningPoint() {
        return this.listeningPoint;
    }
    
    /** Returns a unique CallIdHeader for identifying dialogues between two
     * SIP applications.
     *
     * @return new CallId unique within the SIP Stack.
     */
    public CallIdHeader getNewCallId() {
        String callId = Utils.generateCallIdentifier
        (this.getSipStack().getIPAddress());
        CallID callid = new  CallID();
        try {
            callid.setCallId(callId);
        } catch( java.text.ParseException ex) {}
        return callid;
        
    }
    
    /** Once an application wants to a send a new request it must first request
     * a new client transaction identifier. This method is called by an
     * application to create the client transaction befores it sends the Request
     * via the SipProvider on that transaction. This methods returns a new
     * unique client transaction identifier that can be passed to the stateful
     * sendRequest method on the SipProvider and the sendAck/sendBye
     * methods on the Dialog in order to send a request.
     *
     * @param the new Request message that is to handled statefully by the
     * Provider.
     * @return a new unique client transation identifier
     * @see ClientTransaction
     * @since v1.1
     */
    public ClientTransaction getNewClientTransaction(Request request)
    throws TransactionUnavailableException {
        if (request == null)
            throw new NullPointerException("null request");
        
        SIPRequest sipRequest = (SIPRequest) request;
        if (sipRequest.getTransaction() != null)
            throw new TransactionUnavailableException
            ("Transaction already assigned to request");
        if (request.getMethod().equalsIgnoreCase(Request.CANCEL)) {
            SIPClientTransaction ct = (SIPClientTransaction)
            sipStack.findCancelTransaction((SIPRequest)request,false);
            if (ct != null ) {
                ClientTransaction retval =   sipStack.createClientTransaction
                (ct.getMessageChannel());
                ((SIPTransaction)retval).
                setOriginalRequest((SIPRequest)request);
                ((SIPTransaction)retval).addEventListener(this);
                sipStack.addTransaction((SIPClientTransaction)retval);
                ((SIPClientTransaction)retval).setDialog
                ( (DialogImpl)ct.getDialog());
                return  retval;
            }
            
        }
        if (LogWriter.needsLogging)
            sipStackImpl.logMessage("could not find existing transaction for "
            + ((SIPRequest)request).getFirstLine());
        
        
        // Could not find a dialog or the route is not set in dialog.
        Iterator it = sipStackImpl.getRouter().getNextHops(request);
        String dialogId = sipRequest.getDialogId(false);
        DialogImpl dialog = sipStack.getDialog(dialogId);
        if (it == null || !it.hasNext() )  {
            // could not route the request as out of dialog.
            // maybe the user has no router or the router cannot resolve
            // the route.
            // If this is part of a dialog then use the route from the dialog
            if (dialog != null)   {
                try {
                    HopImpl hop = dialog.getNextHop();
                    if (hop != null) {
                        SIPClientTransaction ct =
                        (SIPClientTransaction) sipStack.createMessageChannel
						(hop);
                        String branchId = Utils.generateBranchId();
                        if (sipRequest.getTopmostVia() != null) {
                            sipRequest.getTopmostVia().setBranch(branchId);
                        } else {
                            // Find a message processor to assign this 
                            // transaction to.
                            // MessageProcessor messageProcessor =
                            // sipStack.getMessageProcessor(hop.getTransport());

                            Via via = this.listeningPoint.
					messageProcessor.getViaHeader();
                            sipRequest.addHeader(via);
                        }
                        ct.setOriginalRequest(sipRequest);
                        ct.setBranch(branchId);
                        ct.setDialog(dialog);
                        ct.addEventListener(this);
                        return (ClientTransaction) ct;
                    }
                } catch (Exception ex) {
                    throw new TransactionUnavailableException(ex.getMessage());
                }
            } else throw new TransactionUnavailableException("no route!");
        } else {
            // An out of dialog route was found. Assign this to the 
            // client transaction.
            while (it.hasNext()) {
                Hop hop = (Hop) it.next();
                try {
                    SIPClientTransaction ct =
                    (SIPClientTransaction) sipStack.createMessageChannel(hop);
		    if (ct == null) continue;
                    String branchId = Utils.generateBranchId();
                    if (sipRequest.getTopmostVia() != null) {
                        sipRequest.getTopmostVia().setBranch(branchId);
                    } else {
                        // Find a message processor to assign this 
			// transaction to. MessageProcessor messageProcessor =
                        // sipStack.getMessageProcessor(hop.getTransport());

                        Via via = this.listeningPoint.messageProcessor.
					getViaHeader();
                        sipRequest.addHeader(via);

                    }
                    ct.setOriginalRequest(sipRequest);
                    ct.setBranch(branchId);
                    if (sipStack.isDialogCreated(request.getMethod())) {
                        // create a new dialog to contain this transaction
                        // provided this is necessary.
                        // This could be a re-invite 
			// (but noticed by Brad Templeton)
                        if (dialog != null)
                            ct.setDialog(dialog);
                        else sipStack.createDialog(ct);
                    }  else {
                        
                        ct.setDialog(dialog);
                        
                    }
                    
                    // The provider is the event listener for all transactions.
                    ct.addEventListener(this);
                    return (ClientTransaction) ct;
                } catch (java.net.UnknownHostException ex) {
                    continue;
                } catch (java.text.ParseException ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }
        }
        throw new TransactionUnavailableException
        ("Could not resolve next hop or listening point unavailable! ");
        
    }
    
    /** An application has the responsibility of deciding to respond to a
     * Request that does not match an existing server transaction. The method
     * is called by an application that decides to respond to an unmatched
     * Request statefully. This methods return a new unique server transaction
     * identifier that can be passed to the stateful sendResponse methods in
     * order to respond to the request.
     *
     * @param the initial Request message that the doesn't match an existing
     * transaction that the application decides to handle statefully.
     * @return a new unique server transation identifier
     * @throws TransactionAlreadyExistsException if a transaction already exists
     * that is already handling this Request. This may happen if the application
     * gets retransmits of the same request before the initial transaction is
     * allocated.
     * @see ServerTransaction
     * @since v1.1
     */
    public ServerTransaction
    getNewServerTransaction(Request request)
    throws TransactionAlreadyExistsException,
    TransactionUnavailableException {
        
        SIPServerTransaction transaction = null;
        SIPRequest sipRequest = (SIPRequest) request;
        if ( sipStack.isDialogCreated(sipRequest.getMethod())) {
            if ( sipStack.findTransaction((SIPRequest)request,true) != null)
                throw new TransactionAlreadyExistsException
                ("server transaction already exists!");
            if (! sipStack.hasResources())
                throw new TransactionUnavailableException
                ("Resource Not available!");
            transaction = (SIPServerTransaction) 
			  ( (SIPRequest) request).getTransaction();
	    if (transaction == null) 
		throw new  TransactionUnavailableException
		("Transaction not available");
	    /**
            if (!transaction.isMessagePartOfTransaction((SIPRequest) request)) {
                throw new TransactionUnavailableException
                ("Request Mismatch");
            }
	    **/
	    if (transaction.getOriginalRequest() == null) 
            	transaction.setOriginalRequest(sipRequest);
            try {
                sipStack.addTransaction(transaction);
            } catch (IOException ex) {
                throw new TransactionUnavailableException
                ("Error sending provisional response");
            }
            // So I can handle timeouts.
            transaction.addEventListener(this);
            String dialogId = sipRequest.getDialogId(true);
            DialogImpl dialog = sipStack.getDialog(dialogId);
            if (dialog == null) dialog = sipStack.createDialog(transaction);
            else transaction.setDialog(dialog);
            dialog.setStack(this.sipStack);
            dialog.addRoute(sipRequest);
            if (dialog.getRemoteTag() != null &&
            dialog.getLocalTag() != null)  {
                this.sipStack.putDialog(dialog);
            }
        } else {
            transaction = (SIPServerTransaction)
            sipStack.findTransaction((SIPRequest) request,true);
            if (transaction != null)
                throw new TransactionAlreadyExistsException
                ("Transaction exists! ");
	    transaction = (SIPServerTransaction) 
			  ( (SIPRequest) request).getTransaction();
            if (transaction == null)
                throw new TransactionUnavailableException
                ("Transaction not available!");
	    if (transaction.getOriginalRequest() == null) 
                transaction.setOriginalRequest(sipRequest);
            // Map the transaction.
            try {
                sipStack.addTransaction(transaction);
            } catch (IOException ex ) {
                throw new TransactionUnavailableException
                ("Could not send back provisional response!");
            }
            String dialogId =   sipRequest.getDialogId(true);
            DialogImpl dialog = sipStack.getDialog(dialogId);
            if ( dialog != null) {
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
            }
        }
        return transaction;
        
    }
    
    /** Returns the SipStack that this SipProvider is attached to. A SipProvider
     * can only be attached to a single SipStack object which belongs to
     * the same SIP stack as the SipProvider.
     *
     * @see SipStack
     * @return the attached SipStack.
     */
    public SipStack getSipStack() {
        return (SipStack) this.sipStackImpl;
    }
    
    /** Removes the SipListener from this SipProvider. This method returns
     * silently if the <var>sipListener</var> argument is not registered
     * with the SipProvider.
     *
     * @param SipListener - the SipListener to be removed from this
     * SipProvider
     */
    public void removeSipListener(SipListener sipListener) {
        if (sipListener == this.sipListener) {
            this.sipListener = null;
        }
    }
    
    
    
    
    /** Sends specified {@link javax.sip.message.Request} and returns void i.e.
     * no transaction record is associated with this action. This method
     * implies that the application is functioning statelessly specific to this
     * Request, hence the underlying SipProvider acts statelessly.
     * <p>
     * Once the Request message has been passed to this method, the SipProvider
     * will forget about this Request. No transaction semantics will be
     * associated with the Request and no retranmissions will occur on the
     * Request by the SipProvider, if these semantics are required it is the
     * responsibility of the application not the JAIN SIP Stack.
     * <ul>
     * <li>Stateless Proxy - A stateless proxy simply forwards every request
     *  it receives downstream and discards information about the request
     *  message once the message has been forwarded. A stateless proxy does not
     *  have any notion of a transaction.
     * </ul>
     *
     * @since v1.1
     * @see Request
     * @param request - the Request message to send statelessly
     * @throws SipException if implementation cannot send request for any reason
     */
    public void sendRequest(Request request) throws SipException {
        Iterator it = sipStack.getRouter().getNextHops(request);
        if (it == null || !it.hasNext())
            throw new SipException("could not determine next hop!");
        // Will slow down the implementation because it involves
        // a search to see if a transaction exists.
        // This is a common bug so adding some assertion
        // checking under debug.
        SIPTransaction tr = sipStack.findTransaction
        ((SIPRequest) request,false);
        if (tr != null )
            throw new SipException
            ("Cannot send statelssly Transaction found!");
        
        while (it.hasNext()) {
            Hop nextHop = (Hop) it.next();
            try {
                SIPRequest sipRequest = (SIPRequest) request;
                String bid = sipRequest.getTransactionId();
                Via via = sipRequest.getTopmostVia();
                via.setBranch(bid);
                SIPRequest newRequest;
                // Do not create a transaction for this request. If it has
                // Mutliple route headers then take the first one off the
                // list and copy into the request URI.
                if (sipRequest.getHeader(RouteHeader.NAME) != null) {
                    newRequest = (SIPRequest) sipRequest.clone();
                    ListIterator rl =
                    newRequest.getHeaders(RouteHeader.NAME);
                    Route route = (Route) rl.next();
                    //newRequest.setRequestURI(route.getAddress().getURI());
                    rl.remove();
                } else newRequest = sipRequest;
                MessageChannel messageChannel =
                sipStack.createRawMessageChannel(nextHop);
                if (messageChannel != null)
                    messageChannel.sendMessage((SIPMessage)newRequest);
                else throw new SipException("could not forward request");
            } catch (IOException ex) {
                throw new SipException(ex.getMessage());
            } catch (ParseException ex1) {
                InternalErrorHandler.handleException(ex1);
            }
        }
        
    }
    
    /** Sends specified {@link javax.sip.message.Response} and returns void i.e.
     * no transaction record is associated with this action. This method implies
     * that the application is functioning as either a stateless proxy or a
     * stateless User Agent Server.
     * <ul>
     *  <li> Stateless proxy - A stateless proxy simply forwards every response
     *  it receives upstream and discards information about the response message
     *  once the message has been forwarded. A stateless proxy does not
     *  have any notion of a transaction.
     *  <li>Stateless User Agent Server - A stateless UAS does not maintain
     *  transaction state. It replies to requests normally, but discards
     *  any state that would ordinarily be retained by a UAS after a response
     *  has been sent.  If a stateless UAS receives a retransmission of a
     *  request, it regenerates the response and resends it, just as if it
     *  were replying to the first instance of the request. A UAS cannot be
     *  stateless unless the request processing for that method would always
     *  result in the same response if the requests are identical. Stateless
     *  UASs do not use a transaction layer; they receive requests directly
     *  from the transport layer and send responses directly to the transport
     *  layer.
     * </ul>
     *
     * @see Response
     * @param response - the Response to send statelessly.
     * @throws SipException if implementation cannot send
     *   response for any reason
     * @see Response
     * @since v1.1
     */
    public void sendResponse(Response response)
    throws SipException {
        SIPResponse sipResponse = (SIPResponse) response;
        Via via = sipResponse.getTopmostVia();
        if (via == null)
            throw new SipException("No via header in response!");
        int      port = via.getPort();
        String   transport = via.getTransport();
        //Bug report by Shanti Kadiyala
        //check to see if Via has "received paramaeter". If so
        //set the host to the via parameter. Else set it to the
        //Via host.
        String host = via.getReceived();
        
        if( host == null)
            host = via.getHost() ;
        
        //TODO Need to check for transport before setting port.
        if (port == -1)  port = 5060;
        Hop hop = new HopImpl(host+":"+port+"/" +transport);
        try {
            MessageChannel messageChannel =
            sipStack.createRawMessageChannel(hop);
            messageChannel.sendMessage(sipResponse);
        } catch (IOException ex) {
            throw new SipException(ex.getMessage());
        }
    }
    
    /** This method sets the listening point of the SipProvider.
     * A SipProvider can only have a single listening point at any
     * specific time. This method returns
     * silently if the same <var>listeningPoint</var> argument is re-set
     * on the SipProvider.
     * <p>
     * JAIN SIP supports recieving messages from
     * any port and interface that a server listens on for UDP, on that same
     * port and interface for TCP in case a message may need to be sent
     * using TCP, rather than UDP, if it is too large. In order to satisfy this
     * functionality an application must create two SipProviders and set
     * identical listeningPoints except for transport on each SipProvder.
     * <p>
     * Multiple SipProviders are prohibited to listen on the same
     * listening point.
     *
     * @param the <var>listeningPoint</var> of this SipProvider
     * @see ListeningPoint
     * @since v1.1
     */
    public void setListeningPoint(ListeningPoint listeningPoint)  {
        if (listeningPoint == null)
            throw new NullPointerException("Null listening point");
        ListeningPointImpl lp = (ListeningPointImpl) listeningPoint;
        lp.sipProviderImpl = this;
        this.listeningPoint = (ListeningPointImpl) listeningPoint;
        
    }
    
    /** This is part of the implementation.
     */
    protected void setSipStack(SipStackImpl sipStackImpl) {
        this.sipStackImpl = sipStackImpl;
        this.sipStack = (SIPTransactionStack) sipStackImpl;
    }
    
    /** 	Invoked when an error has ocurred with a transaction.
     *Propagate up to the listeners.
     *
     * 	@param transactionErrorEvent Error event.
     */
    public void transactionErrorEvent
    (SIPTransactionErrorEvent transactionErrorEvent) {
        SIPTransaction transaction =
        (SIPTransaction) transactionErrorEvent.getSource();
        
        if (transactionErrorEvent.getErrorID() ==
            SIPTransactionErrorEvent.TRANSPORT_ERROR) {
	    // There must be a way to inform the TU here!!
            if (LogWriter.needsLogging) {
                sipStackImpl.logMessage
                ("TransportError occured on " + transaction);
            }
        } else if (transactionErrorEvent.getErrorID() == 
            SIPTransactionErrorEvent.TIMEOUT_ERROR) {
            // This is a timeout event.
            Object errorObject = transactionErrorEvent.getSource();
            Timeout timeout = Timeout.TRANSACTION;
            TimeoutEvent ev = null;
            
            if (errorObject instanceof SIPServerTransaction) {
                ev = new TimeoutEvent(this,(ServerTransaction)
                			errorObject,timeout);
            } else {
                ev = new TimeoutEvent(this,(ClientTransaction) errorObject,
                timeout);
            }
            this.handleEvent(ev,(SIPTransaction) errorObject);
            
        } else if (transactionErrorEvent.getErrorID() == 
            SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT) {
            // This is a timeout retransmit event. 
	    // We should never get this if retransmit filter is
	    // enabled (ie. in that case the stack should handle.
	    // all retransmits.
	    if ( sipStackImpl.isRetransmissionFilterActive()) 
		InternalErrorHandler.handleException("Unexpected event !");
            Object errorObject = transactionErrorEvent.getSource();
            Timeout timeout = Timeout.RETRANSMIT;
            TimeoutEvent ev = null;
            
            if (errorObject instanceof SIPServerTransaction) {
                ev = new TimeoutEvent(this,(ServerTransaction)
                errorObject,timeout);
            } else {
                ev = new TimeoutEvent(this,(ClientTransaction) errorObject,
                timeout);
            }
            this.handleEvent(ev,(SIPTransaction) errorObject);
	}
    }
    
}
