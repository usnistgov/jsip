package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;

import java.io.IOException;
import java.util.*;
import java.net.InetAddress;


import javax.sip.*;
import javax.sip.message.*;


/**
 * Abstract class to support both client and server transactions.  
 * Provides an encapsulation of a message channel, handles timer events, 
 * and creation of the Via header for a message.
 *
 * @author Jeff Keyser 
 * @author M. Ranganathan (modified Jeff's original source and aligned with JAIN-SIP 1.1) 
*  @author Modifications for TLS Support added by Daniel J. Martinez Manzano <dani@dif.um.es>
 * @version  JAIN-SIP-1.1 $Revision: 1.36 $ $Date: 2004-12-01 19:05:16 $
 */
public abstract class SIPTransaction
	extends MessageChannel
	implements javax.sip.Transaction {

	protected static final int BASE_TIMER_INTERVAL =
		SIPTransactionStack.BASE_TIMER_INTERVAL;

	/**
	 * One timer tick.
	 */
	protected static final int T1 = 1;

	/**
	 * 5 sec Maximum duration a message will remain in the network
	 */
	protected static final int T4 = 5000 / BASE_TIMER_INTERVAL;

	/**
	 * The maximum retransmit interval for non-INVITE
	 * requests and INVITE responses
	 */
	protected static final int T2 = 4000 / BASE_TIMER_INTERVAL;

	/**
	 * INVITE request retransmit interval, for UDP only 
	 */
	protected static final int TIMER_A = 1;

	/**
	 * INVITE transaction  timeout timer
	 */
	protected static final int TIMER_B = 64;

	protected static final int TIMER_J = 64;

	protected static final int TIMER_F = 64;

	protected static final int TIMER_H = 64;

	protected static final int TIMER_I = T4;

	protected static final int TIMER_K = T4;

	protected static final int TIMER_D = 32000 / BASE_TIMER_INTERVAL;

	protected static final int TIMER_C = 3 * 60 * 1000 / BASE_TIMER_INTERVAL;

	// Proposed feature for next release.
	protected Object      applicationData;

	protected SIPResponse lastResponse;

	protected SIPDialog dialog;

	protected boolean isMapped;

	protected boolean isAckSeen;

	protected boolean eventPending; // indicate that an event is pending here.

	protected String transactionId; // Transaction Id.

	/**
	 * Initialized but no state assigned.
	 */
	public static final TransactionState INITIAL_STATE = null;

	/**
	 *	Trying state.
	 */
	public static final TransactionState TRYING_STATE = TransactionState.TRYING;

	/**
	 * CALLING State.
	 */
	public static final TransactionState CALLING_STATE = TransactionState.CALLING;

	/**
	 * Proceeding state.
	 */
	public static final TransactionState PROCEEDING_STATE = TransactionState.PROCEEDING;

	/**
	 * Completed state.
	 */
	public static final TransactionState COMPLETED_STATE = TransactionState.COMPLETED;

	/**
	 * Confirmed state.
	 */
	public static final TransactionState CONFIRMED_STATE = TransactionState.CONFIRMED;

	/**
	 * Terminated state.  
	 */
	public static final TransactionState TERMINATED_STATE = TransactionState.TERMINATED;

	/**
	 *	Maximum number of ticks between retransmissions.
	 */
	protected static final int MAXIMUM_RETRANSMISSION_TICK_COUNT = 8;

	// Parent stack for this transaction
	protected SIPTransactionStack sipStack;

	// Original request that is being handled by this transaction
	protected SIPRequest originalRequest;

	// Underlying channel being used to send messages for this transaction
	protected MessageChannel encapsulatedChannel;

	// Port of peer
	protected int peerPort;

	// Address of peer
	protected InetAddress peerInetAddress;

	// Address of peer as a string
	protected String peerAddress;

	// Protocol of peer
	protected String peerProtocol;

        //@@@ hagai - NAT changes
	// Source port extracted from peer packet
        protected int peerPacketSourcePort;

        protected InetAddress peerPacketSourceAddress;

	// Transaction branch ID
	private String branch;

	// Method of the Request used to create the transaction.
	protected String method;

	// Sequence number of request used to create the transaction
	private int cSeq;

	// Current transaction state
	private TransactionState currentState;

	// Number of ticks the retransmission timer was set to last
	private int retransmissionTimerLastTickCount;

	// Number of ticks before the message is retransmitted
	private int retransmissionTimerTicksLeft;

	// Number of ticks before the transaction times out
	protected int timeoutTimerTicksLeft;

	// List of event listeners for this transaction
	private Set eventListeners;

	// Hang on to these - we clear out the request URI after 
	// transaction goes to final state. Pointers to these are kept around
	// for transaction matching as long as the transaction is in
	// the transaction table.
	protected From from;

	protected To to;

	protected Event event;

	protected CallID callId;

	// Back ptr to the JAIN layer.
	private Object wrapper;

	// Counter for caching of connections.
	// Connection lingers for collectionTime
	// after the  Transaction goes to terminated state.
	protected int collectionTime;


	// Transaction timer object.
	protected TimerTask myTimer;



	public String getBranchId() {
		return this.branch;
	}

	class LingerTimer extends TimerTask {
		private SIPTransaction transaction;
		private SIPTransactionStack sipStack;
		
		public LingerTimer (SIPTransaction transaction) {
			this.transaction = transaction;
			this.sipStack = transaction.sipStack;
		}

		public void run() {
			// release the connection associated with this transaction.
			if (transaction instanceof SIPClientTransaction )	 {
				this.transaction.close();
			} else if (transaction instanceof ServerTransaction ) {
				// Remove it from the set
				if (LogWriter.needsLogging)
					sipStack.logWriter.logMessage(
							"removing" + transaction);
					sipStack.removeTransaction(this.transaction);
				if (  ( ! this.sipStack.cacheServerConnections )
				   && transaction.encapsulatedChannel instanceof TCPMessageChannel
			   	   && -- ((TCPMessageChannel) transaction.encapsulatedChannel).useCount == 0 ) {
				  // Close the encapsulated socket if stack is configured 
				    transaction.close();
				} else
				if (  ( ! this.sipStack.cacheServerConnections )
				   && transaction.encapsulatedChannel instanceof TLSMessageChannel
			   	   && -- ((TLSMessageChannel) transaction.encapsulatedChannel).useCount == 0 ) {
				  // Close the encapsulated socket if stack is configured 
				    transaction.close();
				} else {
				   if (LogWriter.needsLogging
				      &&  ( ! this.sipStack.cacheServerConnections )
			              && transaction.isReliable())
				      {
					int UseCount;

					if(transaction.encapsulatedChannel instanceof TCPMessageChannel)
						UseCount = ((TCPMessageChannel) transaction.encapsulatedChannel).useCount;
					else
						UseCount = ((TLSMessageChannel) transaction.encapsulatedChannel).useCount;

				      	sipStack.logWriter.logMessage( "Use Count = " + UseCount);
				      }
				}
			}
		}
	}

	/**
	 *	Transaction constructor.
	 *
	 *	@param newParentStack Parent stack for this transaction.
	 *	@param newEncapsulatedChannel 
	 * 		Underlying channel for this transaction.
	 */
	protected SIPTransaction(
		SIPTransactionStack newParentStack,
		MessageChannel newEncapsulatedChannel) {

		sipStack = newParentStack;
		encapsulatedChannel = newEncapsulatedChannel;
		// Record this to check if the address has changed before sending
		// message to avoid possible race condition.
		this.peerPort = newEncapsulatedChannel.getPeerPort();
		this.peerAddress = newEncapsulatedChannel.getPeerAddress();
		this.peerInetAddress = newEncapsulatedChannel.getPeerInetAddress();
                // @@@ hagai
               this.peerPacketSourcePort = newEncapsulatedChannel.getPeerPacketSourcePort();
               this.peerPacketSourceAddress = newEncapsulatedChannel.getPeerPacketSourceAddress();
		this.peerProtocol = newEncapsulatedChannel.getPeerProtocol();
		if (this.isReliable()) {
			if(encapsulatedChannel instanceof TLSMessageChannel) 
			{
				((TLSMessageChannel)encapsulatedChannel).useCount++;
				if (LogWriter.needsLogging)
				    sipStack.logWriter.logMessage("use count for encapsulated channel" +    this + " " +
					((TLSMessageChannel)encapsulatedChannel).useCount);
			}
			else
			{
				((TCPMessageChannel)encapsulatedChannel).useCount++;
				if (LogWriter.needsLogging)
				    sipStack.logWriter.logMessage("use count for encapsulated channel" +    this + " " +
					((TCPMessageChannel)encapsulatedChannel).useCount);
			}
		}

		this.currentState = null;

		disableRetransmissionTimer();
		disableTimeoutTimer();
		eventListeners = Collections.synchronizedSet(new HashSet());

		// Always add the parent stack as a listener 
		// of this transaction
		addEventListener(newParentStack);
	}

	/**
	 *	Sets the request message that this transaction handles.
	 *
	 *	@param newOriginalRequest Request being handled.
	 */
	public void setOriginalRequest(SIPRequest newOriginalRequest) {

		// Branch value of topmost Via header
		String newBranch;

		if (this.originalRequest != null) {
			sipStack.removeTransactionHash(this);
		}
		// This will be cleared later.

		this.originalRequest = newOriginalRequest;

		// just cache the control information so the
		// original request can be released later.
		this.method = newOriginalRequest.getMethod();
		this.from = (From) newOriginalRequest.getFrom();
		this.to = (To) newOriginalRequest.getTo();
		this.callId = (CallID) newOriginalRequest.getCallId();
		this.cSeq = newOriginalRequest.getCSeq().getSequenceNumber();
		this.event = (Event) newOriginalRequest.getHeader("Event");
		this.transactionId = newOriginalRequest.getTransactionId();

		originalRequest.setTransaction(this);


		// If the message has an explicit branch value set,
		newBranch =
			((Via) newOriginalRequest.getViaHeaders().getFirst()).getBranch();
		if (newBranch != null) {
			if (LogWriter.needsLogging)
				sipStack.logWriter.logMessage(
					"Setting Branch id : " + newBranch);

			// Override the default branch with the one 
			// set by the message
			setBranch(newBranch);

		} else {
			if (LogWriter.needsLogging)
				sipStack.logWriter.logMessage(
					"Branch id is null - compute TID!"
						+ newOriginalRequest.encode());
			setBranch(newOriginalRequest.getTransactionId());
		}
	}

	/**
	 *	Gets the request being handled by this transaction.
	 *
	 *	@return Request being handled.
	 */
	public SIPRequest getOriginalRequest() {
		return originalRequest;
	}

	/** Get the original request but cast to a Request structure.
	*
	* @return the request that generated this transaction.
	*/
	public Request getRequest() {
		return (Request) originalRequest;
	}

	/**
	 *  Returns a flag stating whether this transaction is for an 
	 *	INVITE request or not.
	 *
	 *	@return True if this is an INVITE request, false if not.
	 */
	public final boolean isInviteTransaction() {
		return getMethod().equals(Request.INVITE);
	}

	/**
	 * Return true if the transaction corresponds to a CANCEL message.
	 *
	 * @return true if the transaciton is a CANCEL transaction.
	 */
	public final boolean isCancelTransaction() {
		return getMethod().equals(Request.CANCEL);
	}

	/**
	 * Return a flag that states if this is a BYE transaction.
	 *
	 * @return true if the transaciton is a BYE transaction.
	 */
	public final boolean isByeTransaction() {
		return getMethod().equals(Request.BYE);
	}

	/**
	 *  Returns the message channel used for 
	 * 		transmitting/receiving messages
	 * for this transaction. Made public in support of JAIN dual 
	 * transaction model.
	 *
	 *	@return Encapsulated MessageChannel.
	 *
	 */
	public MessageChannel getMessageChannel() {
		return encapsulatedChannel;
	}

	/**
	 * Sets the Via header branch parameter used to identify 
	 * this transaction.
	 *
	 * @param newBranch New string used as the branch 
	 * for this transaction.
	 */
	public final void setBranch(String newBranch) {
		branch = newBranch;
	}

	/**
	 * Gets the current setting for the branch parameter of this transaction.
	 *
	 * @return Branch parameter for this transaction.
	 */
	public final String getBranch() {
		if (this.branch == null) {
			this.branch = getOriginalRequest().getTopmostVia().getBranch();
		}
		return branch;
	}

	/**
	 * Get the method of the request used to create this transaction.
	 *
	 * @return the method of the request for the transaction.
	 */
	public final String getMethod() {
		return this.method;
	}

	/**
	 * Get the Sequence number of the request used to create the 
	 * transaction.
	 *
	 * @return the cseq of the request used to create the transaction.
	 */
	public final int getCSeq() {
		return this.cSeq;
	}

	/**
	 * Changes the state of this transaction.
	 *
	 * @param newState New state of this transaction.
	 */
	public void setState(TransactionState newState) {
		currentState = newState;
		if (LogWriter.needsLogging) {
			sipStack.logWriter.logMessage(
				"Transaction:setState " + newState + " " + this);
			sipStack.logWriter.logStackTrace();
		}
	}



	/**
	 * Gets the current state of this transaction.
	 *
	 * @return Current state of this transaction.
	 */
	public TransactionState getState() {
		return this.currentState;
	}

	/**
	 * Enables retransmission timer events for this transaction to begin in
	 * one tick.
	 */
	protected final void enableRetransmissionTimer() {
		// Changed this to 2 on request from Joseph Cheung 
		enableRetransmissionTimer(1);
	}

	/**
	 * Enables retransmission timer events for this 
	 * transaction to begin after the number of ticks passed to 
	 * this routine.
	 *
	 * @param tickCount Number of ticks before the 
	 * 	next retransmission timer
	 *	event occurs.
	 */
	protected final void enableRetransmissionTimer(int tickCount) {
		retransmissionTimerTicksLeft =
			Math.min(tickCount, MAXIMUM_RETRANSMISSION_TICK_COUNT);
		retransmissionTimerLastTickCount = retransmissionTimerTicksLeft;
	}

	/**
	 *	Turns off retransmission events for this transaction.
	 */
	protected final void disableRetransmissionTimer() {
		retransmissionTimerTicksLeft = -1;
	}

	/**
	 * Enables a timeout event to occur for this transaction after the number
	 * of ticks passed to this method.
	 *
	 * @param tickCount Number of ticks before this transaction times out.
	 */
	protected final void enableTimeoutTimer(int tickCount) {
		if (LogWriter.needsLogging)
			sipStack.logWriter.logMessage(
				"enableTimeoutTimer "
					+ this
					+ " tickCount "
					+ tickCount
					+ " currentTickCount = "
					+ timeoutTimerTicksLeft);

		timeoutTimerTicksLeft = tickCount;
	}

	/**
	 *	Disabled the timeout timer.
	 */
	protected final void disableTimeoutTimer() {
		timeoutTimerTicksLeft = -1;
	}

	/**
	 * Fired after each timer tick.  
	 * Checks the retransmission and timeout
	 * timers of this transaction, and fired these events 
	 * if necessary.
	 */
	final void fireTimer() {
		// If the timeout timer is enabled,
		if (timeoutTimerTicksLeft != -1) {
			// Count down the timer, and if it has run out,
			if (--timeoutTimerTicksLeft == 0) {
				// Fire the timeout timer
				fireTimeoutTimer();
			}
		}

		// If the retransmission timer is enabled,
		if (retransmissionTimerTicksLeft != -1) {
			// Count down the timer, and if it has run out,
			if (--retransmissionTimerTicksLeft == 0) {
				// Enable this timer to fire again after 
				// twice the original time
				enableRetransmissionTimer(retransmissionTimerLastTickCount * 2);
				// Fire the timeout timer
				fireRetransmissionTimer();
			}
		}
	}

	/**
	 *	Tests a message to see if it is part of this transaction.
	 *
	 *	@return True if the message is part of this 
	 * 		transaction, false if not.
	 */
	public abstract boolean isMessagePartOfTransaction(SIPMessage messageToTest);

	/**
	 * This method is called when this transaction's 
	 * retransmission timer has fired.
	 */
	protected abstract void fireRetransmissionTimer();

	/**
	 * This method is called when this transaction's 
	 * timeout timer has fired.
	 */
	protected abstract void fireTimeoutTimer();

	/**
	 *	Tests if this transaction has terminated.
	 *
	 *	@return Trus if this transaction is terminated, false if not.
	 */
	public final boolean isTerminated() {
		return  getState() == TERMINATED_STATE;
	}

	public String getHost() {
		return encapsulatedChannel.getHost();
	}

	public String getKey() {
		return encapsulatedChannel.getKey();
	}

	public int getPort() {
		return encapsulatedChannel.getPort();
	}

	public SIPMessageStack getSIPStack() {
		return sipStack;
	}

	public String getPeerAddress() {
		return this.peerAddress; 
	}

	public int getPeerPort() {
		return this.peerPort;
	}

        //@@@ hagai
       public int getPeerPacketSourcePort() {
           return this.peerPacketSourcePort;
       }

        public InetAddress getPeerPacketSourceAddress() {
        return this.peerPacketSourceAddress;
        }
		
	protected InetAddress getPeerInetAddress() {
		return this.peerInetAddress;
	}

	protected String getPeerProtocol() {
		return this.peerProtocol;
	}

	public String getTransport() {
		return encapsulatedChannel.getTransport();
	}

	public boolean isReliable() {
		return encapsulatedChannel.isReliable();
	}

	/**
	 * Returns the Via header for this channel.  Gets the Via header of the
	 * underlying message channel, and adds a branch parameter to it for this
	 * transaction.
	 */
	public Via getViaHeader() {
		// Via header of the encapulated channel
		Via channelViaHeader;

		// Add the branch parameter to the underlying 
		// channel's Via header
		channelViaHeader = super.getViaHeader();
		try {
			channelViaHeader.setBranch(branch);
		} catch (java.text.ParseException ex) {
		}
		return channelViaHeader;

	}


	/**
	 * Process the message through the transaction and sends it to the SIP
	 * peer.
	 *
	 * @param messageToSend Message to send to the SIP peer.
	 */
	public void sendMessage(SIPMessage messageToSend)
		throws IOException
	{
	    // Use the peer address, port and transport 
	    // that was specified when the transaction was
	    // created. Bug was noted by Bruce Evangelder
	    // soleo communications.
	    encapsulatedChannel.sendMessage
		(messageToSend, this.peerInetAddress, this.peerPort);
	}

	/**
	 * Parse the byte array as a message, process it through the 
	 * transaction, and send it to the SIP peer. This is just
	 * a placeholder method -- calling it will result in an IO 
	 * exception.
	 *
	 * @param messageBytes Bytes of the message to send.
	 * @param receiverAddress Address of the target peer.
	 * @param receiverPort Network port of the target peer.
	 *
	 * @throws IOException If called.
	 */
	protected void sendMessage(
		byte[] messageBytes,
		InetAddress receiverAddress,
		int receiverPort,
		boolean retry)
		throws IOException {
		throw new IOException
		("Cannot send unparsed message through Transaction Channel!");
	}

	/**
	 * Adds a new event listener to this transaction.
	 *
	 * @param newListener Listener to add.
	 */
	public void addEventListener(SIPTransactionEventListener newListener) {
		eventListeners.add(newListener);
	}

	/**
	 * Removed an event listener from this transaction.
	 *
	 * @param oldListener Listener to remove.
	 */
	public void removeEventListener(SIPTransactionEventListener oldListener) {
		eventListeners.remove(oldListener);
	}

	/**
	 * Creates a SIPTransactionErrorEvent and sends it 
	 * to all of the listeners of this transaction.  
	 * This method also flags the transaction as
	 * terminated.
	 *
	 *	@param errorEventID ID of the error to raise.
	 */
	protected void raiseErrorEvent(int errorEventID) {

		// Error event to send to all listeners
		SIPTransactionErrorEvent newErrorEvent;
		// Iterator through the list of listeners
		Iterator listenerIterator;
		// Next listener in the list
		SIPTransactionEventListener nextListener;

		// Create the error event
		newErrorEvent = new SIPTransactionErrorEvent(this, errorEventID);

		// Loop through all listeners of this transaction
		synchronized (eventListeners) {
			listenerIterator = eventListeners.iterator();
			while (listenerIterator.hasNext()) {
				// Send the event to the next listener
				nextListener =
					(SIPTransactionEventListener) listenerIterator.next();
				nextListener.transactionErrorEvent(newErrorEvent);
			}
		}
		// Clear the event listeners after propagating the error.
		// Retransmit notifications are just an alert to the 
		// application (they are not an error).
		if (errorEventID != SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT) {
			eventListeners.clear();

			// Errors always terminate a transaction
			this.setState(TransactionState.TERMINATED);

			if (this instanceof SIPServerTransaction
				&& this.isByeTransaction()
				&& this.dialog != null)
				this.dialog.setState(SIPDialog.TERMINATED_STATE);
		}
	}


	/**
	 * A shortcut way of telling if we are a server transaction.
	 */
	protected boolean IsServerTransaction() {
		return this instanceof SIPServerTransaction;
	}

	/**
	 * Gets the dialog object of this Transaction object. This object
	 * returns null if no dialog exists. A dialog only exists for a
	 * transaction when a session is setup between a User Agent Client and a
	 * User Agent Server, either by a 1xx Provisional Response for an early
	 * dialog or a 200OK Response for a committed dialog.
	 *
	 * @return the Dialog Object of this Transaction object.
	 * @see Dialog
	 */
	public Dialog getDialog() {
		return this.dialog;

	}

	/**
	 * set the dialog object.
	 * @param dialog -- the dialog to set.
	 */
	public void setDialog(SIPDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * Returns the current value of the retransmit timer in 
	 * milliseconds used to retransmit messages over unreliable transports.
	 *
	 * @return the integer value of the retransmit timer in milliseconds.
	 */
	public int getRetransmitTimer() {
		return SIPTransactionStack.BASE_TIMER_INTERVAL;
	}

	/**
	 * Get the host to assign for an outgoing Request via header.
	 */
	public String getViaHost() {
		return this.getViaHeader().getHost();

	}

	/**
	 * Get the last response. This is used internally by the implementation. 
	 * Dont rely on it.
	 *
	 *@return the last response received (for client transactions) 
	 *   or sent (for server transactions).
	 */
	public SIPResponse getLastResponse() {
		return this.lastResponse;
	}

	/**
	 * Get the JAIN interface response 
	 */
	public Response getResponse() {
		return (Response) this.lastResponse;
	}

	/**
	 * Get the transaction Id.
	 */
	public String getTransactionId() {
		return this.transactionId;
	}


	/** Hashcode method for fast hashtable lookup.
	*/
	public int hashCode() {
		if (this.transactionId == null) return -1;
		else return this.transactionId.hashCode();
	}

	/**
	 * Get the port to assign for the via header of an outgoing message.
	 */
	public int getViaPort() {
		return this.getViaHeader().getPort();
	}
	/**
	 * A method that can be used to test if an incoming request
	 * belongs to this transction. This does not take the transaction
	 * state into account when doing the check otherwise it is identical
	 * to isMessagePartOfTransaction. This is useful for checking if
	 * a CANCEL belongs to this transaction.
	 *
	 * @param requestToTest is the request to test.
	 * @return true if the the request belongs to the transaction.
	 *
	 */
	public boolean doesCancelMatchTransaction(SIPRequest requestToTest) {

		// List of Via headers in the message to test
		ViaList viaHeaders;
		// Topmost Via header in the list
		Via topViaHeader;
		// Branch code in the topmost Via header
		String messageBranch;
		// Flags whether the select message is part of this transaction
		boolean transactionMatches;

		transactionMatches = false;

		if (this.getOriginalRequest() == null
			|| this.getOriginalRequest().getMethod().equalsIgnoreCase(
				Request.CANCEL))
			return false;
		// Get the topmost Via header and its branch parameter
		viaHeaders = requestToTest.getViaHeaders();
		if (viaHeaders != null) {

			topViaHeader = (Via) viaHeaders.getFirst();
			messageBranch = topViaHeader.getBranch();
			if (messageBranch != null) {

				// If the branch parameter exists but
				// does not start with the magic cookie,
				if (!messageBranch
					.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE)) {

					// Flags this as old
					// (RFC2543-compatible) client
					// version
					messageBranch = null;

				}

			}

			// If a new branch parameter exists,
			if (messageBranch != null && this.getBranch() != null) {

				// If the branch equals the branch in
				// this message,
				if (getBranch().equalsIgnoreCase(messageBranch)
					&& topViaHeader.getSentBy().equals(
						((Via) getOriginalRequest().getViaHeaders().getFirst())
							.getSentBy())) {
					transactionMatches = true;
					if (LogWriter.needsLogging)
						sipStack.logWriter.logMessage("returning  true");
				}

			} else {
				// If this is an RFC2543-compliant message,
				// If RequestURI, To tag, From tag,
				// CallID, CSeq number, and top Via
				// headers are the same,
				if (LogWriter.needsLogging)
					sipStack.logWriter.logMessage(
						"testing against " + getOriginalRequest());

				if (getOriginalRequest()
					.getRequestURI()
					.equals(requestToTest.getRequestURI())
					&& getOriginalRequest().getTo().equals(requestToTest.getTo())
					&& getOriginalRequest().getFrom().equals(
						requestToTest.getFrom())
					&& getOriginalRequest().getCallId().getCallId().equals(
						requestToTest.getCallId().getCallId())
					&& getOriginalRequest().getCSeq().getSequenceNumber()
						== requestToTest.getCSeq().getSequenceNumber()
					&& topViaHeader.equals(
						getOriginalRequest().getViaHeaders().getFirst())) {

					transactionMatches = true;
				}

			}

		}

		return transactionMatches;
	}

	/**
	 * Sets the value of the retransmit timer to the 
	 * newly supplied timer value.
	 * The retransmit timer is expressed in milliseconds and its 
	 * default value  is 500ms. This method allows the application
	 * to change the transaction  retransmit behavior for different
	 * networks. Take the gateway proxy as  an example. The internal
	 * intranet is likely to be reatively uncongested  and the
	 * endpoints will be relatively close. The external network is
	 * the  general Internet. This functionality allows different
	 * retransmit times  for either side.  
	 *
	 * @param retransmitTimer - the new integer value of the 
	 * retransmit timer in milliseconds.
	 */
	public void setRetransmitTimer(int retransmitTimer) {
		throw new UnsupportedOperationException("Feature not supported");
	}

	/**
	 * Close the encapsulated channel.
	 */
	public void close() {
		this.encapsulatedChannel.close();
		if (LogWriter.needsLogging) 
		    sipStack.logWriter.logMessage("Closing " + this.encapsulatedChannel);
	}

	public boolean isSecure() {
		return encapsulatedChannel.isSecure();
	}

	public MessageProcessor getMessageProcessor() {
		return this.encapsulatedChannel.getMessageProcessor();
	}

	/** This is book-keeping for retransmission filter management.
	*/
	public void setAckSeen() { 
		this.isAckSeen = true;
	}

	/** This is book-keeping for retransmission filter management.
	*/
	public boolean ackSeen() {
		return this.isAckSeen;
	}


	/**
	* Mark that there is a pending event for this transaction.
	*/
	public void setEventPending ( ) {
			this.eventPending = true;
	}

	
	/** Clear the mark that there is a pending event for this
	* transaction.
	*/
	protected void clearPending() {
		this.eventPending = false;
	}

	/** Set the application data pointer. This is un-interpreted
	* by the stack. This is provided as a conveniant way of keeping
	* book-keeping data for applications. Note that null clears the
	* application data pointer (releases it).
	* 
	* @param applicationData -- application data pointer to set. null 
	* clears the applicationd data pointer.
	*
	*/
	
	public void setApplicationData (Object applicationData) {
		this.applicationData = applicationData; 
	}


	/** Get the application data associated with this 
	* transaction.
	*
	*  @return stored application data.
	*/
	public Object getApplicationData () {
		return this.applicationData;
	}


	/**
	* Set the encapsuated channel. The peer inet address and port are
	* set equal to the message channel.
	*/
	public void setEncapsulatedChannel( MessageChannel messageChannel) {
		this.encapsulatedChannel  = messageChannel;
		this.peerInetAddress = messageChannel.getPeerInetAddress();
		this.peerPort = messageChannel.getPeerPort();
	}

	


	protected abstract void startTransactionTimer();

	public abstract void processPending();

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.35  2004/11/28 17:32:26  mranga
 * Submitted by:  hagai sela
 * Reviewed by:   mranga
 *
 * Support for symmetric nats
 *
 * Revision 1.34  2004/11/19 16:22:56  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * Route bye request to right target (if there is no record routing enabled).
 *
 * Revision 1.33  2004/10/28 19:02:51  mranga
 * Submitted by:  Daniel Martinez
 * Reviewed by:   M. Ranganathan
 *
 * Added changes for TLS support contributed by Daniel Martinez
 *
 * Revision 1.32  2004/10/04 16:03:53  mranga
 * Reviewed by:   mranga
 * attempted fix for memory leak
 *
 * Revision 1.31  2004/10/04 14:43:20  mranga
 * Reviewed by:   mranga
 *
 * Remove transaction from pending list when terminated.
 *
 * Revision 1.30  2004/09/27 18:51:18  mranga
 * Reviewed by:   mranga
 *
 * Additional config flag for proxy servers (dialog is not tracked by stack).
 *
 * Revision 1.29  2004/09/01 18:09:06  mranga
 * Reviewed by:   mranga
 * Allow application to see route header on incoming request though
 * use of a configuration parameter.
 *
 * Revision 1.28  2004/07/23 06:50:05  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Clean up - Get rid of annoying eclipse warnings.
 *
 * Revision 1.27  2004/07/01 05:42:22  mranga
 * Submitted by:  Pierre De Rop and Thomas Froment
 * Reviewed by:    M. Ranganathan
 *
 * More performance hacks.
 *
 * Revision 1.26  2004/06/27 00:41:52  mranga
 * Submitted by:  Thomas Froment and Pierre De Rop
 * Reviewed by:   mranga
 * Performance improvements
 * (auxiliary data structure for fast lookup of transactions).
 *
 * Revision 1.25  2004/06/21 04:59:52  mranga
 * Refactored code - no functional changes.
 *
 * Revision 1.24  2004/06/15 09:54:45  mranga
 * Reviewed by:   mranga
 * re-entrant listener model added.
 * (see configuration property gov.nist.javax.sip.REENTRANT_LISTENER)
 *
 * Revision 1.23  2004/06/01 11:42:59  mranga
 * Reviewed by:   mranga
 * timer fix missed starting the transaction timer in a couple of places.
 *
 * Revision 1.22  2004/05/30 18:55:58  mranga
 * Reviewed by:   mranga
 * Move to timers and eliminate the Transaction scanner Thread
 * to improve scalability and reduce cpu usage.
 *
 * Revision 1.21  2004/05/18 15:26:44  mranga
 * Reviewed by:   mranga
 * Attempted fix at race condition bug. Remove redundant exception (never thrown).
 * Clean up some extraneous junk.
 *
 * Revision 1.20  2004/04/07 00:19:24  mranga
 * Reviewed by:   mranga
 * Fixes a potential race condition for client transactions.
 * Handle re-invites statefully within an established dialog.
 *
 * Revision 1.19  2004/03/09 00:34:44  mranga
 * Reviewed by:   mranga
 * Added TCP connection management for client and server side
 * Transactions. See configuration parameter
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false
 * Releases Client TCP Connections after linger time
 *
 * Revision 1.18  2004/02/22 13:53:57  mranga
 * Reviewed by:   mranga
 * Return null from transaction rather than dummy dialog (following discussion
 * with Phelim et al. ) Note the addition to the errata.
 *
 * Revision 1.17  2004/02/19 16:01:40  mranga
 * Reviewed by:   mranga
 * tighten up retransmission filter to deal with ack retransmissions.
 *
 * Revision 1.16  2004/02/13 13:55:32  mranga
 * Reviewed by:   mranga
 * per the spec, Transactions must always have a valid dialog pointer. Assigned a dummy dialog for transactions that are not assigned to any dialog (such as Message).
 *
 * Revision 1.15  2004/02/05 14:43:21  mranga
 * Reviewed by:   mranga
 * Fixed for correct reporting of transaction state.
 * Remove contact headers from ack
 *
 * Revision 1.14  2004/01/25 16:06:24  mranga
 * Reviewed by:   M. Ranganathan
 *
 * Clean up setting state (Use TransactionState instead of integer). Convert to UNIX file format.
 * Remove extraneous methods.
 *
 * Revision 1.13  2004/01/22 20:15:32  mranga
 * Reviewed by:  mranga
 * Fixed a possible race condition in  nulling out the transaction Request (earlier added for scalability).
 *
 * Revision 1.12  2004/01/22 13:26:33  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
