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
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.LogLevels;
import gov.nist.core.LogWriter;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.SIPClientTransaction.ExpiresTimerTask;

import java.io.IOException;
import java.net.InetAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.Dialog;
import javax.sip.IOExceptionEvent;
import javax.sip.TransactionState;
import javax.sip.address.SipURI;
import javax.sip.message.Request;
import javax.sip.message.Response;

/*
 * Modifications for TLS Support added by Daniel J. Martinez Manzano
 * <dani@dif.um.es> Bug fixes by Jeroen van Bemmel (JvB) and others.
 */

/**
 * Abstract class to support both client and server transactions. Provides an
 * encapsulation of a message channel, handles timer events, and creation of the
 * Via header for a message.
 *
 * @author Jeff Keyser
 * @author M. Ranganathan
 *
 *
 * @version 1.2 $Revision: 1.100 $ $Date: 2010-12-02 22:04:13 $
 */
public abstract class SIPTransaction extends MessageChannel implements
        javax.sip.Transaction, gov.nist.javax.sip.TransactionExt {
	private static StackLogger logger = CommonLogger.getLogger(SIPTransaction.class);

	// Contribution on http://java.net/jira/browse/JSIP-417 from Alexander Saveliev
	private static final Pattern EXTRACT_CN = Pattern.compile(".*CN\\s*=\\s*([\\w*\\.\\-_]+).*");

    protected boolean toListener; // Flag to indicate that the listener gets

    // to see the event.

    protected int BASE_TIMER_INTERVAL = SIPTransactionStack.BASE_TIMER_INTERVAL;
    /**
     * 5 sec Maximum duration a message will remain in the network
     */
    protected int T4 = 5000 / BASE_TIMER_INTERVAL;

    /**
     * The maximum retransmit interval for non-INVITE requests and INVITE
     * responses
     */
    protected int T2 = 4000 / BASE_TIMER_INTERVAL;
    protected int TIMER_I = T4;

    protected int TIMER_K = T4;

    protected int TIMER_D = 32000 / BASE_TIMER_INTERVAL;

    // protected static final int TIMER_C = 3 * 60 * 1000 / BASE_TIMER_INTERVAL;

    /**
     * One timer tick.
     */
    protected static final int T1 = 1;

    /**
     * INVITE request retransmit interval, for UDP only
     */
    protected static final int TIMER_A = 1;

    /**
     * INVITE transaction timeout timer
     */
    protected static final int TIMER_B = 64;

    protected static final int TIMER_J = 64;

    protected static final int TIMER_F = 64;

    protected static final int TIMER_H = 64;

    // Proposed feature for next release.
    protected transient Object applicationData;

    protected SIPResponse lastResponse;

    // private SIPDialog dialog;

    protected boolean isMapped;

    private TransactionSemaphore semaphore;

    // protected boolean eventPending; // indicate that an event is pending
    // here.

    protected String transactionId; // Transaction Id.

    // Audit tag used by the SIP Stack audit
    public long auditTag = 0;

    /**
     * Initialized but no state assigned.
     */
    public static final TransactionState INITIAL_STATE = null;

    /**
     * Trying state.
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
     * Maximum number of ticks between retransmissions.
     */
    protected static final int MAXIMUM_RETRANSMISSION_TICK_COUNT = 8;

    // Parent stack for this transaction
    protected transient SIPTransactionStack sipStack;

    // Original request that is being handled by this transaction
    protected SIPRequest originalRequest;
    //jeand we nullify the originalRequest fast to save on mem and help GC
    // so we keep only those data instead
    protected byte[] originalRequestBytes;
    protected long originalRequestCSeqNumber;
    protected String originalRequestBranch;	
    protected boolean originalRequestHasPort;
    
	
    // Underlying channel being used to send messages for this transaction
    private transient MessageChannel encapsulatedChannel;

    protected AtomicBoolean transactionTimerStarted = new AtomicBoolean(false);

    // Transaction branch ID
    private String branch;

    // Method of the Request used to create the transaction.
    private String method;

    // Current transaction state
    private int currentState = -1;

    // Number of ticks the retransmission timer was set to last
    private transient int retransmissionTimerLastTickCount;

    // Number of ticks before the message is retransmitted
    private transient int retransmissionTimerTicksLeft;

    // Number of ticks before the transaction times out
    protected int timeoutTimerTicksLeft;

    // List of event listeners for this transaction
    private transient Set<SIPTransactionEventListener> eventListeners;


    // Counter for caching of connections.
    // Connection lingers for collectionTime
    // after the Transaction goes to terminated state.
    protected int collectionTime;

    private boolean terminatedEventDelivered;      
    
    // aggressive flag to optimize eagerly
    private boolean releaseReferences;
    
    // caching flags
    private Boolean inviteTransaction = null;
    private Boolean dialogCreatingTransaction = null; 
    
    // caching fork id
    private String forkId = null;
    
    public ExpiresTimerTask expiresTimerTask;
	// http://java.net/jira/browse/JSIP-420
    private MaxTxLifeTimeListener maxTxLifeTimeListener;

    public String getBranchId() {
        return this.branch;
    }

    // [Issue 284] https://jain-sip.dev.java.net/issues/show_bug.cgi?id=284
    // JAIN SIP drops 200 OK due to race condition
    // Wrapper that uses a semaphore for non reentrant listener
    // and a lock for reetrant listener to avoid race conditions 
    // when 2 responses 180/200 OK arrives at the same time
    class TransactionSemaphore {
        
        private static final long serialVersionUID = -1634100711669020804L;
        Semaphore sem = null;
        ReentrantLock lock = null;
        
        public TransactionSemaphore() {
            if(((SipStackImpl)getSIPStack()).isReEntrantListener()) {
                lock = new ReentrantLock();
            } else {
                sem = new Semaphore(1, true);
            }
        }
        
        public boolean acquire() {
            try {
                if(((SipStackImpl)getSIPStack()).isReEntrantListener()) {
                    lock.lock();
                } else {
                    sem.acquire();
                }
                return true;
            } catch (Exception ex) {
                logger.logError("Unexpected exception acquiring sem",
                        ex);
                InternalErrorHandler.handleException(ex);
                return false;
            }        
        }
        
        public boolean tryAcquire() {
            try {
                if(((SipStackImpl)getSIPStack()).isReEntrantListener()) {
                    return lock.tryLock(sipStack.maxListenerResponseTime, TimeUnit.SECONDS);
                } else {
                    return sem.tryAcquire(sipStack.maxListenerResponseTime, TimeUnit.SECONDS);
                }                
            } catch (Exception ex) {
                logger.logError("Unexpected exception trying acquiring sem",
                        ex);
                InternalErrorHandler.handleException(ex);
                return false;
            }        
        }
        
        public void release() {
            try {
                if(((SipStackImpl)getSIPStack()).isReEntrantListener()) {
                    if(lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                } else {
                    sem.release();
                }                
            } catch (Exception ex) {
                logger.logError("Unexpected exception releasing sem",
                                ex);
            }        
        }
    }
    
    /**
     * The linger timer is used to remove the transaction from the transaction
     * table after it goes into terminated state. This allows connection caching
     * and also takes care of race conditins.
     *
     *
     */
    class LingerTimer extends SIPStackTimerTask {

        public LingerTimer() {            
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            	SIPTransaction sipTransaction = SIPTransaction.this;
                logger.logDebug("LingerTimer : "
                        + sipTransaction.getTransactionId());
            }

        }

        public void runTask() {
            cleanUp();
        }
    }
    
    /**
     * http://java.net/jira/browse/JSIP-420
     * This timer task will terminate the transaction after a configurable time
     *
     */
    class MaxTxLifeTimeListener extends SIPStackTimerTask {
        SIPTransaction sipTransaction = SIPTransaction.this;
    
        public void runTask() {
            try {               	
            	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug("Fired MaxTxLifeTimeListener for tx " +  sipTransaction + " , tx id "+ sipTransaction.getTransactionId() + " , state " + sipTransaction.getState());
            	}
            	
        		raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
        	
        		 SIPStackTimerTask myTimer = new LingerTimer();
                 sipStack.getTimer().schedule(myTimer,
                     SIPTransactionStack.CONNECTION_LINGER_TIME * 1000);
                 maxTxLifeTimeListener = null;
                 
            } catch (Exception ex) {
                logger.logError("unexpected exception", ex);
            }
        }
    }

    /**
     * Transaction constructor.
     *
     * @param newParentStack
     *            Parent stack for this transaction.
     * @param newEncapsulatedChannel
     *            Underlying channel for this transaction.
     */
    protected SIPTransaction(SIPTransactionStack newParentStack,
            MessageChannel newEncapsulatedChannel) {

        sipStack = newParentStack;
        this.semaphore = new TransactionSemaphore();
        
        encapsulatedChannel = newEncapsulatedChannel;
     
        if (this.isReliable()) {            
                encapsulatedChannel.useCount++;
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger
                            .logDebug("use count for encapsulated channel"
                                    + this
                                    + " "
                                    + encapsulatedChannel.useCount );
        }

        this.currentState = -1;

        disableRetransmissionTimer();
        disableTimeoutTimer();
        eventListeners = new CopyOnWriteArraySet<SIPTransactionEventListener>();

        // Always add the parent stack as a listener
        // of this transaction
        addEventListener(newParentStack);

        releaseReferences = sipStack.isAggressiveCleanup();
    }

    public abstract void cleanUp();

	/**
     * Sets the request message that this transaction handles.
     *
     * @param newOriginalRequest
     *            Request being handled.
     */
    public void setOriginalRequest(SIPRequest newOriginalRequest) {

        // Branch value of topmost Via header
        String newBranch;

        final String newTransactionId = newOriginalRequest.getTransactionId();
        if (this.originalRequest != null
                && (!this.originalRequest.getTransactionId().equals(
                        newTransactionId))) {
            sipStack.removeTransactionHash(this);
        }
        // This will be cleared later.

        this.originalRequest = newOriginalRequest;
        this.originalRequestCSeqNumber = newOriginalRequest.getCSeq().getSeqNumber();
        final Via topmostVia = newOriginalRequest.getTopmostVia();
        this.originalRequestBranch = topmostVia.getBranch();
        this.originalRequestHasPort = topmostVia.hasPort();
        int originalRequestViaPort = topmostVia.getPort();
       
        if ( originalRequestViaPort == -1 ) {
            if (topmostVia.getTransport().equalsIgnoreCase("TLS") ) {
                originalRequestViaPort = 5061;         
            } else {
                originalRequestViaPort = 5060;
            }
        }
                
        // just cache the control information so the
        // original request can be released later.
        this.method = newOriginalRequest.getMethod();
        
        this.transactionId = newTransactionId;

        originalRequest.setTransaction(this);

        // If the message has an explicit branch value set,
        newBranch = topmostVia.getBranch();
        if (newBranch != null) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("Setting Branch id : " + newBranch);

            // Override the default branch with the one
            // set by the message
            setBranch(newBranch);

        } else {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("Branch id is null - compute TID!"
                        + newOriginalRequest.encode());
            setBranch(newTransactionId);
        }
    }

    /**
     * Gets the request being handled by this transaction.
     *
     * @return -- the original Request associated with this transaction.
     */
    public SIPRequest getOriginalRequest() {
        return this.originalRequest;
    }

    /**
     * Get the original request but cast to a Request structure.
     *
     * @return the request that generated this transaction.
     */
    public Request getRequest() {
        if(isReleaseReferences() && originalRequest == null && originalRequestBytes != null) {
            if(logger.isLoggingEnabled(StackLogger.TRACE_WARN)) {
                logger.logWarning("reparsing original request " + originalRequestBytes + " since it was eagerly cleaned up, but beware this is not efficient with the aggressive flag set !");
            }
            try {
                originalRequest = (SIPRequest) sipStack.getMessageParserFactory().createMessageParser(sipStack).parseSIPMessage(originalRequestBytes, true, false, null);
//                originalRequestBytes = null;
            } catch (ParseException e) {
                logger.logError("message " + originalRequestBytes + " could not be reparsed !");
            }
        }   
        return (Request) originalRequest;
    }

    /**
     * Returns a flag stating whether this transaction is for a request that creates a dialog.
     *
     * @return -- true if this is a request that creates a dialog, false if not.
     */
    public final boolean isDialogCreatingTransaction() {
        if (dialogCreatingTransaction == null) {
        	dialogCreatingTransaction = Boolean.valueOf(isInviteTransaction() || getMethod().equals(Request.SUBSCRIBE) || getMethod().equals(Request.REFER));
        }
    	return dialogCreatingTransaction.booleanValue();
    }
    
    /**
     * Returns a flag stating whether this transaction is for an INVITE request
     * or not.
     *
     * @return -- true if this is an INVITE request, false if not.
     */
    public final boolean isInviteTransaction() {
        if (inviteTransaction == null) {
        	inviteTransaction = Boolean.valueOf(getMethod().equals(Request.INVITE));
        }
    	return inviteTransaction.booleanValue();
    }

    /**
     * Return true if the transaction corresponds to a CANCEL message.
     *
     * @return -- true if the transaciton is a CANCEL transaction.
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
     * Returns the message channel used for transmitting/receiving messages for
     * this transaction. Made public in support of JAIN dual transaction model.
     *
     * @return Encapsulated MessageChannel.
     *
     */
    public MessageChannel getMessageChannel() {
        return encapsulatedChannel;
    }

    /**
     * Sets the Via header branch parameter used to identify this transaction.
     *
     * @param newBranch
     *            New string used as the branch for this transaction.
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
            this.branch = originalRequestBranch;
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
     * Get the Sequence number of the request used to create the transaction.
     *
     * @return the cseq of the request used to create the transaction.
     */
    public final long getCSeq() {
        return this.originalRequestCSeqNumber;
    }

    /**
     * Changes the state of this transaction.
     *
     * @param newState
     *            New state of this transaction.
     */
    public void setState(int newState) {
        // PATCH submitted by sribeyron
        if (currentState == TransactionState._COMPLETED) {
            if (newState != TransactionState._TERMINATED
                    && newState != TransactionState._CONFIRMED)
                newState = TransactionState._COMPLETED;
        }
        if (currentState == TransactionState._CONFIRMED) {
            if (newState != TransactionState._TERMINATED)
                newState = TransactionState._CONFIRMED;
        }
        if (currentState != TransactionState._TERMINATED)
            currentState = newState;
        else
            newState = currentState;
        // END OF PATCH
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("Transaction:setState " + newState
                    + " " + this + " branchID = " + this.getBranch()
                    + " isClient = " + (this instanceof SIPClientTransaction));
            logger.logStackTrace();
        }
    }

    /**
     * Gets the current state of this transaction.
     *
     * @return Current state of this transaction.
     */
    public int getInternalState() {
        return this.currentState;
    }
    
    /**
     * Gets the current state of this transaction.
     *
     * @return Current state of this transaction.
     */
    public TransactionState getState() {
    	if(currentState < 0) {
    		return null;
    	}
        return TransactionState.getObject(this.currentState);
    }

    /**
     * Enables retransmission timer events for this transaction to begin in one
     * tick.
     */
    protected final void enableRetransmissionTimer() {
        enableRetransmissionTimer(1);
    }

    /**
     * Enables retransmission timer events for this transaction to begin after
     * the number of ticks passed to this routine.
     *
     * @param tickCount
     *            Number of ticks before the next retransmission timer event
     *            occurs.
     */
    protected final void enableRetransmissionTimer(int tickCount) {
        // For INVITE Client transactions, double interval each time
        if (isInviteTransaction() && (this instanceof SIPClientTransaction)) {
            retransmissionTimerTicksLeft = tickCount;
        } else {
            // non-INVITE transactions and 3xx-6xx responses are capped at T2
            retransmissionTimerTicksLeft = Math.min(tickCount,
                    MAXIMUM_RETRANSMISSION_TICK_COUNT);
        }
        retransmissionTimerLastTickCount = retransmissionTimerTicksLeft;
    }

    /**
     * Turns off retransmission events for this transaction.
     */
    protected final void disableRetransmissionTimer() {
        retransmissionTimerTicksLeft = -1;
    }

    /**
     * Enables a timeout event to occur for this transaction after the number of
     * ticks passed to this method.
     *
     * @param tickCount
     *            Number of ticks before this transaction times out.
     */
    protected final void enableTimeoutTimer(int tickCount) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("enableTimeoutTimer " + this
                    + " tickCount " + tickCount + " currentTickCount = "
                    + timeoutTimerTicksLeft);

        timeoutTimerTicksLeft = tickCount;
    }

    /**
     * Disabled the timeout timer.
     */
    protected final void disableTimeoutTimer() {
        timeoutTimerTicksLeft = -1;
    }

    /**
     * Fired after each timer tick. Checks the retransmission and timeout timers
     * of this transaction, and fired these events if necessary.
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
     * Tests if this transaction has terminated.
     *
     * @return Trus if this transaction is terminated, false if not.
     */
    public final boolean isTerminated() {
        return currentState == TransactionState._TERMINATED;
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

    public SIPTransactionStack getSIPStack() {
        return (SIPTransactionStack) sipStack;
    }

    public String getPeerAddress() {
        return this.encapsulatedChannel.getPeerAddress();
    }

    public int getPeerPort() {
        return this.encapsulatedChannel.getPeerPort();
    }

    // @@@ hagai
    public int getPeerPacketSourcePort() {
        return this.encapsulatedChannel.getPeerPacketSourcePort();
    }

    public InetAddress getPeerPacketSourceAddress() {
        return this.encapsulatedChannel.getPeerPacketSourceAddress();
    }

    protected InetAddress getPeerInetAddress() {
        return this.encapsulatedChannel.getPeerInetAddress();
    }

    protected String getPeerProtocol() {
        return this.encapsulatedChannel.getPeerProtocol();
    }

    public String getTransport() {
        return encapsulatedChannel.getTransport();
    }

    public boolean isReliable() {
        return encapsulatedChannel.isReliable();
    }

    /**
     * Returns the Via header for this channel. Gets the Via header of the
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
     * Process the message through the transaction and sends it to the SIP peer.
     *
     * @param messageToSend
     *            Message to send to the SIP peer.
     */
    public void sendMessage(final SIPMessage messageToSend) throws IOException {
        // Use the peer address, port and transport
        // that was specified when the transaction was
        // created. Bug was noted by Bruce Evangelder
        // soleo communications.
        try {
        	final RawMessageChannel channel = (RawMessageChannel) encapsulatedChannel;
            for (MessageProcessor messageProcessor : sipStack
                    .getMessageProcessors()) {
            	boolean addrmatch = messageProcessor.getIpAddress().getHostAddress().toString().equals(this.getPeerAddress());
            	if (addrmatch
            			&& messageProcessor.getPort() == this.getPeerPort()
            			&& messageProcessor.getTransport().equalsIgnoreCase(
            					this.getPeerProtocol())) {
            		if (channel instanceof TCPMessageChannel) {
            			try {

            				Runnable processMessageTask = new Runnable() {

            					public void run() {
            						try {
            							((TCPMessageChannel) channel)
            							.processMessage((SIPMessage) messageToSend.clone(), getPeerInetAddress());
            						} catch (Exception ex) {

            							if (logger.isLoggingEnabled(ServerLogger.TRACE_ERROR)) {
            								logger.logError("Error self routing TCP message cause by: ", ex);
            							}
            						}
            					}
            				};
            				getSIPStack().getSelfRoutingThreadpoolExecutor().execute(processMessageTask);

            			} catch (Exception e) {

            				logger.logError("Error passing message in self routing TCP", e);

            			}
            			if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
                        	logger.logDebug("Self routing message TCP");

                        return;
                    }
            		if (channel instanceof TLSMessageChannel) {
            			try {

            				Runnable processMessageTask = new Runnable() {

            					public void run() {
            						try {
            							((TLSMessageChannel) channel)
            							.processMessage((SIPMessage) messageToSend.clone(), getPeerInetAddress());
            						} catch (Exception ex) {
            							if (logger.isLoggingEnabled(ServerLogger.TRACE_ERROR)) {
            								logger.logError("Error self routing TLS message cause by: ", ex);
            							}
            						}
            					}
            				};
            				getSIPStack().getSelfRoutingThreadpoolExecutor().execute(processMessageTask);

            			} catch (Exception e) {
            				logger.logError("Error passing message in TLS self routing", e);
            			}
            			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                        	logger.logDebug("Self routing message TLS");
                        return;
                    }
                    if (channel instanceof RawMessageChannel) {
                        try {
                        	
                        	Runnable processMessageTask = new Runnable() {
    							
    							public void run() {
    								try {
    									((RawMessageChannel) channel).processMessage((SIPMessage) messageToSend.clone());
    								} catch (Exception ex) {
    									if (logger.isLoggingEnabled(ServerLogger.TRACE_ERROR)) {
    						        		logger.logError("Error self routing message cause by: ", ex);
    						        	}
    								}
    							}
    						};
    						getSIPStack().getSelfRoutingThreadpoolExecutor().execute(processMessageTask);
						} catch (Exception e) {
							logger.logError("Error passing message in self routing", e);
						}
                        if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
                        	logger.logDebug("Self routing message");
                        return;
                    }

                }
            }
            encapsulatedChannel.sendMessage(messageToSend,
                    this.getPeerInetAddress(), this.getPeerPort());
        } finally {
            this.startTransactionTimer();
        }
    }

    /**
     * Parse the byte array as a message, process it through the transaction,
     * and send it to the SIP peer. This is just a placeholder method -- calling
     * it will result in an IO exception.
     *
     * @param messageBytes
     *            Bytes of the message to send.
     * @param receiverAddress
     *            Address of the target peer.
     * @param receiverPort
     *            Network port of the target peer.
     *
     * @throws IOException
     *             If called.
     */
    protected void sendMessage(byte[] messageBytes,
            InetAddress receiverAddress, int receiverPort, boolean retry)
            throws IOException {
        throw new IOException(
                "Cannot send unparsed message through Transaction Channel!");
    }

    /**
     * Adds a new event listener to this transaction.
     *
     * @param newListener
     *            Listener to add.
     */
    public void addEventListener(SIPTransactionEventListener newListener) {
        eventListeners.add(newListener);
    }

    /**
     * Removed an event listener from this transaction.
     *
     * @param oldListener
     *            Listener to remove.
     */
    public void removeEventListener(SIPTransactionEventListener oldListener) {
        eventListeners.remove(oldListener);
    }

    /**
     * Creates a SIPTransactionErrorEvent and sends it to all of the listeners
     * of this transaction. This method also flags the transaction as
     * terminated.
     *
     * @param errorEventID
     *            ID of the error to raise.
     */
    protected void raiseErrorEvent(int errorEventID) {

        // Error event to send to all listeners
        SIPTransactionErrorEvent newErrorEvent;
        // Iterator through the list of listeners
        Iterator<SIPTransactionEventListener> listenerIterator;
        // Next listener in the list
        SIPTransactionEventListener nextListener;

        // Create the error event
        newErrorEvent = new SIPTransactionErrorEvent(this, errorEventID);

        // Loop through all listeners of this transaction
        synchronized (eventListeners) {
            listenerIterator = eventListeners.iterator();
            while (listenerIterator.hasNext()) {
                // Send the event to the next listener
                nextListener = (SIPTransactionEventListener) listenerIterator
                        .next();
                nextListener.transactionErrorEvent(newErrorEvent);
            }
        }
        // Clear the event listeners after propagating the error.
        // Retransmit notifications are just an alert to the
        // application (they are not an error).
        if (errorEventID != SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT) {
            eventListeners.clear();

            // Errors always terminate a transaction
            this.setState(TransactionState._TERMINATED);

            if (this instanceof SIPServerTransaction && this.isByeTransaction()
                    && this.getDialog() != null)
                ((SIPDialog) this.getDialog())
                        .setState(SIPDialog.TERMINATED_STATE);
        }
    }

    /**
     * A shortcut way of telling if we are a server transaction.
     */
    protected boolean isServerTransaction() {
        return this instanceof SIPServerTransaction;
    }

    /**
     * Gets the dialog object of this Transaction object. This object returns
     * null if no dialog exists. A dialog only exists for a transaction when a
     * session is setup between a User Agent Client and a User Agent Server,
     * either by a 1xx Provisional Response for an early dialog or a 200OK
     * Response for a committed dialog.
     *
     * @return the Dialog Object of this Transaction object.
     * @see Dialog
     */
    public abstract Dialog getDialog();

    /**
     * set the dialog object.
     *
     * @param sipDialog --
     *            the dialog to set.
     * @param dialogId --
     *            the dialog id ot associate with the dialog.s
     */
    public abstract void setDialog(SIPDialog sipDialog, String dialogId);

    /**
     * Returns the current value of the retransmit timer in milliseconds used to
     * retransmit messages over unreliable transports.
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
     * @return the last response received (for client transactions) or sent (for
     *         server transactions).
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

    /**
     * Hashcode method for fast hashtable lookup.
     */
    public int hashCode() {
        if (this.transactionId == null)
            return -1;
        else
            return this.transactionId.hashCode();
    }

    /**
     * Get the port to assign for the via header of an outgoing message.
     */
    public int getViaPort() {
        return this.getViaHeader().getPort();
    }

    /**
     * A method that can be used to test if an incoming request belongs to this
     * transction. This does not take the transaction state into account when
     * doing the check otherwise it is identical to isMessagePartOfTransaction.
     * This is useful for checking if a CANCEL belongs to this transaction.
     *
     * @param requestToTest
     *            is the request to test.
     * @return true if the the request belongs to the transaction.
     *
     */
    public boolean doesCancelMatchTransaction(SIPRequest requestToTest) {

        // List of Via headers in the message to test
//        ViaList viaHeaders;
        // Topmost Via header in the list
        Via topViaHeader;
        // Branch code in the topmost Via header
        String messageBranch;
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;

        transactionMatches = false;
        final SIPRequest origRequest = getOriginalRequest();
        if (origRequest == null
                || this.getMethod().equals(Request.CANCEL))
            return false;
        // Get the topmost Via header and its branch parameter
        topViaHeader = requestToTest.getTopmostVia();
        if (topViaHeader != null) {

//            topViaHeader = (Via) viaHeaders.getFirst();
            messageBranch = topViaHeader.getBranch();
            if (messageBranch != null) {

                // If the branch parameter exists but
                // does not start with the magic cookie,
                if (!messageBranch.toLowerCase().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {

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
                                origRequest.getTopmostVia().getSentBy())) {
                    transactionMatches = true;
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                        logger.logDebug("returning  true");
                }

            } else {
                // If this is an RFC2543-compliant message,
                // If RequestURI, To tag, From tag,
                // CallID, CSeq number, and top Via
                // headers are the same,
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug("testing against "
                            + origRequest);

                if (origRequest.getRequestURI().equals(
                        requestToTest.getRequestURI())
                        && origRequest.getTo().equals(
                                requestToTest.getTo())
                        && origRequest.getFrom().equals(
                                requestToTest.getFrom())
                        && origRequest.getCallId().getCallId().equals(
                                requestToTest.getCallId().getCallId())
                        && origRequest.getCSeq().getSeqNumber() == requestToTest
                                .getCSeq().getSeqNumber()
                        && topViaHeader.equals(origRequest.getTopmostVia())) {

                    transactionMatches = true;
                }

            }

        }

        // JvB: Need to pass the CANCEL to the listener! Retransmitted INVITEs
        // set it to false
        if (transactionMatches) {
            this.setPassToListener();
        }
        return transactionMatches;
    }

    /**
     * Sets the value of the retransmit timer to the newly supplied timer value.
     * The retransmit timer is expressed in milliseconds and its default value
     * is 500ms. This method allows the application to change the transaction
     * retransmit behavior for different networks. Take the gateway proxy as an
     * example. The internal intranet is likely to be reatively uncongested and
     * the endpoints will be relatively close. The external network is the
     * general Internet. This functionality allows different retransmit times
     * for either side.
     *
     * @param retransmitTimer -
     *            the new integer value of the retransmit timer in milliseconds.
     */
    public void setRetransmitTimer(int retransmitTimer) {

        if (retransmitTimer <= 0)
            throw new IllegalArgumentException(
                    "Retransmit timer must be positive!");
        if (this.transactionTimerStarted.get())
            throw new IllegalStateException(
                    "Transaction timer is already started");
        BASE_TIMER_INTERVAL = retransmitTimer;
        // Commented out for Issue 303 since those timers are configured separately now  
//      T4 = 5000 / BASE_TIMER_INTERVAL;
//      T2 = 4000 / BASE_TIMER_INTERVAL;
//      TIMER_D = 32000 / BASE_TIMER_INTERVAL;

    }

    /**
     * Close the encapsulated channel.
     */
    public void close() {
        this.encapsulatedChannel.close();
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("Closing " + this.encapsulatedChannel);

    }

    public boolean isSecure() {
        return encapsulatedChannel.isSecure();
    }

    public MessageProcessor getMessageProcessor() {
        return this.encapsulatedChannel.getMessageProcessor();
    }

    /**
     * Set the application data pointer. This is un-interpreted by the stack.
     * This is provided as a conveniant way of keeping book-keeping data for
     * applications. Note that null clears the application data pointer
     * (releases it).
     *
     * @param applicationData --
     *            application data pointer to set. null clears the applicationd
     *            data pointer.
     *
     */

    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    /**
     * Get the application data associated with this transaction.
     *
     * @return stored application data.
     */
    public Object getApplicationData() {
        return this.applicationData;
    }

    /**
     * Set the encapsuated channel. The peer inet address and port are set equal
     * to the message channel.
     */
    public void setEncapsulatedChannel(MessageChannel messageChannel) {
        this.encapsulatedChannel = messageChannel;
        if ( this instanceof SIPClientTransaction ) {
        	this.encapsulatedChannel.setEncapsulatedClientTransaction((SIPClientTransaction) this);
        }        
    }

    /**
     * Return the SipProvider for which the transaction is assigned.
     *
     * @return the SipProvider for the transaction.
     */
    public SipProviderImpl getSipProvider() {

        return this.getMessageProcessor().getListeningPoint().getProvider();
    }

    /**
     * Raise an IO Exception event - this is used for reporting asynchronous IO
     * Exceptions that are attributable to this transaction.
     *
     */
    public void raiseIOExceptionEvent() {
        setState(TransactionState._TERMINATED);
        String host = getPeerAddress();
        int port = getPeerPort();
        String transport = getTransport();
        IOExceptionEvent exceptionEvent = new IOExceptionEvent(this, host,
                port, transport);
        getSipProvider().handleEvent(exceptionEvent, this);
    }

    /**
     * A given tx can process only a single outstanding event at a time. This
     * semaphore gaurds re-entrancy to the transaction.
     *
     */
    public boolean acquireSem() {
        boolean retval = false;
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("acquireSem [[[[" + this);
            logger.logStackTrace();
        }
        if ( this.sipStack.maxListenerResponseTime == -1 ) {
            retval = this.semaphore.acquire();            
        } else {
            retval = this.semaphore.tryAcquire();
        }
        if ( logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug(
                "acquireSem() returning : " + retval);
        return retval;
    }
        

    /**
     * Release the transaction semaphore.
     *
     */
    public void releaseSem() {
        try {

            this.toListener = false;
            this.semRelease();

        } catch (Exception ex) {
            logger.logError("Unexpected exception releasing sem",
                    ex);

        }

    }

    protected void semRelease() {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("semRelease ]]]]" + this);
            logger.logStackTrace();
        }
        this.semaphore.release();
    }

    /**
     * Set true to pass the request up to the listener. False otherwise.
     *
     */

    public boolean passToListener() {
        return toListener;
    }

    /**
     * Set the passToListener flag to true.
     */
    public void setPassToListener() {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("setPassToListener()");
        }
        this.toListener = true;

    }

    /**
     * Flag to test if the terminated event is delivered.
     *
     * @return
     */
    protected synchronized boolean testAndSetTransactionTerminatedEvent() {
        boolean retval = !this.terminatedEventDelivered;
        this.terminatedEventDelivered = true;
        return retval;
    }
    
    public String getCipherSuite() throws UnsupportedOperationException {
        if (this.getMessageChannel() instanceof TLSMessageChannel ) {
            if (  ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener() == null ) 
                return null;
            else if ( ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null)
                return null;
            else return ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getCipherSuite();
        } else throw new UnsupportedOperationException("Not a TLS channel");

    }

    
    public java.security.cert.Certificate[] getLocalCertificates() throws UnsupportedOperationException {
         if (this.getMessageChannel() instanceof TLSMessageChannel ) {
            if (  ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener() == null ) 
                return null;
            else if ( ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null)
                return null;
            else return ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getLocalCertificates();
        } else throw new UnsupportedOperationException("Not a TLS channel");
    }

    
    public java.security.cert.Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (this.getMessageChannel() instanceof TLSMessageChannel ) {
            if (  ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener() == null ) 
                return null;
            else if ( ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent() == null)
                return null;
            else return ((TLSMessageChannel) this.getMessageChannel()).getHandshakeCompletedListener().getHandshakeCompletedEvent().getPeerCertificates();
        } else throw new UnsupportedOperationException("Not a TLS channel");

    }

    /**
     * Extract identities from certificates exchanged over TLS, based on guidelines
     * from rfc5922.
     * @return list of authenticated identities
     */
    public List<String> extractCertIdentities() throws SSLPeerUnverifiedException {
        if (this.getMessageChannel() instanceof TLSMessageChannel) {
            List<String> certIdentities = new ArrayList<String>();
            Certificate[] certs = getPeerCertificates();
            if (certs == null) {
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug("No certificates available");
                }
                return certIdentities;
            }
            for (Certificate cert : certs) {
                X509Certificate x509cert = (X509Certificate) cert;
                Collection<List< ? >> subjAltNames = null;
                try {
                    subjAltNames = x509cert.getSubjectAlternativeNames();
                } catch (CertificateParsingException ex) {
                    if (logger.isLoggingEnabled()) {
                        logger.logError("Error parsing TLS certificate", ex);
                    }
                }
                // subjAltName types are defined in rfc2459
                final Integer dnsNameType = 2;
                final Integer uriNameType = 6;
                if (subjAltNames != null) {
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger.logDebug("found subjAltNames: " + subjAltNames);
                    }
                    // First look for a URI in the subjectAltName field
                    for (List< ? > altName : subjAltNames) {
                        // 0th position is the alt name type
                        // 1st position is the alt name data
                        if (altName.get(0).equals(uriNameType)) {
                            SipURI altNameUri;
                            try {
                                altNameUri = new AddressFactoryImpl().createSipURI((String) altName.get(1));
                                // only sip URIs are allowed
                                if(!"sip".equals(altNameUri.getScheme()))
                                    continue;
                                // user certificates are not allowed
                                if(altNameUri.getUser() != null)
                                    continue;
                                String altHostName = altNameUri.getHost();
                                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                                    logger.logDebug(
                                        "found uri " + altName.get(1) + ", hostName " + altHostName);
                                }
                                certIdentities.add(altHostName);
                            } catch (ParseException e) {
                                if (logger.isLoggingEnabled()) {
                                    logger.logError(
                                        "certificate contains invalid uri: " + altName.get(1));
                                }
                            }
                        }

                    }
                    // DNS An implementation MUST accept a domain name system
                    // identifier as a SIP domain identity if and only if no other
                    // identity is found that matches the "sip" URI type described
                    // above.
                    if (certIdentities.isEmpty()) {
                        for (List< ? > altName : subjAltNames) {
                            if (altName.get(0).equals(dnsNameType)) {
                                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                                    logger.logDebug("found dns " + altName.get(1));
                                }
                                certIdentities.add(altName.get(1).toString());
                            }
                        }
                    }
                } else {
                    // If and only if the subjectAltName does not appear in the
                    // certificate, the implementation MAY examine the CN field of the
                    // certificate. If a valid DNS name is found there, the
                    // implementation MAY accept this value as a SIP domain identity.
                    String dname = x509cert.getSubjectDN().getName();
                    String cname = "";
                    try {
                        Matcher matcher = EXTRACT_CN.matcher(dname);
                        if (matcher.matches()) {
                            cname = matcher.group(1);
                            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                                logger.logDebug("found CN: " + cname + " from DN: " + dname);
                            }
                            certIdentities.add(cname);
                        }
                    } catch (Exception ex) {
                        if (logger.isLoggingEnabled()) {
                            logger.logError("exception while extracting CN", ex);
                        }
                    }
                }
            }
            return certIdentities;
        } else
            throw new UnsupportedOperationException("Not a TLS channel");
    }

    /**
     * Start the timer that runs the transaction state machine.
     *
     */

    protected abstract void startTransactionTimer();

    /**
     * Tests a message to see if it is part of this transaction.
     *
     * @return True if the message is part of this transaction, false if not.
     */
    public abstract boolean isMessagePartOfTransaction(SIPMessage messageToTest);

    /**
     * This method is called when this transaction's retransmission timer has
     * fired.
     */
    protected abstract void fireRetransmissionTimer();

    /**
     * This method is called when this transaction's timeout timer has fired.
     */
    protected abstract void fireTimeoutTimer();    

    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.DialogExt#isReleaseReferences()
     */
    public boolean isReleaseReferences() {        
        return releaseReferences;
    }

    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.DialogExt#setReleaseReferences(boolean)
     */
    public void setReleaseReferences(boolean releaseReferences) {
        this.releaseReferences = releaseReferences;
    }
    
    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.TransactionExt#getTimerD()
     */
    public int getTimerD() {      
        return TIMER_D;
    }
   
    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.TransactionExt#getTimerT2()
     */
    public int getTimerT2() {
        return T2;
    }

    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.TransactionExt#getTimerT4()
     */
    public int getTimerT4() {
        return T4;
    }

    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.TransactionExt#setTimerD(int)
     */
    public void setTimerD(int interval) {
        if(interval < 32000) {
            throw new IllegalArgumentException("To be RFC 3261 compliant, the value of Timer D should be at least 32s");
        }
        TIMER_D = interval / BASE_TIMER_INTERVAL;
    }
    
    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.TransactionExt#setTimerT2(int)
     */
    public void setTimerT2(int interval) {
        T2 = interval / BASE_TIMER_INTERVAL; 
    }

    /*
     * (non-Javadoc)
     * @see gov.nist.javax.sip.TransactionExt#setTimerT4(int)
     */
    public void setTimerT4(int interval) {
        T4 = interval / BASE_TIMER_INTERVAL;
        TIMER_I = T4;
        TIMER_K = T4;
    }
    
    
    /**
     * Sets the fork id for the transaction.
     * @param forkId
     */
    public void setForkId(String forkId) {
		this.forkId = forkId;
	}
    
    /**
     * Retrieves the fork id for the transaction.
     * @return
     */
    public String getForkId() {
		return forkId;
	}
    
    protected void scheduleMaxTxLifeTimeTimer() {
    	if (maxTxLifeTimeListener == null && this.getMethod().equalsIgnoreCase(Request.INVITE) && sipStack.getMaxTxLifetimeInvite() > 0) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("Scheduling MaxTxLifeTimeListener for tx " +  this + " , tx id "+ this.getTransactionId() + " , state " + this.getState());
        	}
        	maxTxLifeTimeListener = new MaxTxLifeTimeListener();
            sipStack.getTimer().schedule(maxTxLifeTimeListener,
                    sipStack.getMaxTxLifetimeInvite() * 1000);
        }
        
        if (maxTxLifeTimeListener == null && !this.getMethod().equalsIgnoreCase(Request.INVITE) && sipStack.getMaxTxLifetimeNonInvite() > 0) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("Scheduling MaxTxLifeTimeListener for tx " +  this + " , tx id "+ this.getTransactionId() + " , state " + this.getState());
        	}
        	maxTxLifeTimeListener = new MaxTxLifeTimeListener();
            sipStack.getTimer().schedule(maxTxLifeTimeListener,
                    sipStack.getMaxTxLifetimeNonInvite() * 1000);
        }
    }

	public void cancelMaxTxLifeTimeTimer() {
		if(maxTxLifeTimeListener != null) {
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("Cancelling MaxTxLifeTimeListener for tx " +  this + " , tx id "+ this.getTransactionId() + " , state " + this.getState());
        	}
			sipStack.getTimer().cancel(maxTxLifeTimeListener);
			maxTxLifeTimeListener = null;
		}
	}
}
