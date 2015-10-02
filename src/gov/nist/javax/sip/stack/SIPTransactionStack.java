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

import gov.nist.core.*;
import gov.nist.core.net.AddressResolver;
import gov.nist.core.net.DefaultNetworkLayer;
import gov.nist.core.net.NetworkLayer;
import gov.nist.core.net.SecurityManagerProvider;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.header.Event;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.parser.MessageParserFactory;
import gov.nist.javax.sip.stack.timers.SipTimer;

import javax.sip.*;
import javax.sip.address.Hop;
import javax.sip.address.Router;
import javax.sip.header.CallIdHeader;
import javax.sip.header.EventHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * Jeff Keyser : architectural suggestions and contributions. Pierre De Rop and Thomas Froment :
 * Bug reports. Jeyashankher < jai@lucent.com > : bug reports. Jeroen van Bemmel : Bug fixes.
 *
 *
 */

/**
 *
 * This is the sip stack. It is essentially a management interface. It manages
 * the resources for the JAIN-SIP implementation. This is the structure that is
 * wrapped by the SipStackImpl.
 *
 * @see gov.nist.javax.sip.SipStackImpl
 *
 * @author M. Ranganathan <br/>
 *
 * @version 1.2 $Revision: 1.180 $ $Date: 2010-12-02 22:04:15 $
 */
public abstract class SIPTransactionStack implements
        SIPTransactionEventListener, SIPDialogEventListener {
	private static StackLogger logger = CommonLogger.getLogger(SIPTransactionStack.class);
    /*
     * Number of milliseconds between timer ticks (500).
     */
    public static final int BASE_TIMER_INTERVAL = 500;

    /*
     * Connection linger time (seconds) this is the time (in seconds) for which
     * we linger the TCP connection before closing it.
     */
    public static final int CONNECTION_LINGER_TIME = 8;

    /*
     * Dialog Early state timeout duration.
     */
    protected int earlyDialogTimeout = 180;


    /*
     * Table of retransmission Alert timers.
     */
    protected ConcurrentHashMap<String, SIPServerTransaction> retransmissionAlertTransactions;

    // Table of early dialogs ( to keep identity mapping )
    protected ConcurrentHashMap<String, SIPDialog> earlyDialogTable;

    // Table of dialogs.
    protected ConcurrentHashMap<String, SIPDialog> dialogTable;

    // Table of server dialogs ( for loop detection)
    protected ConcurrentHashMap<String, SIPDialog> serverDialogMergeTestTable;

    // A set of methods that result in dialog creations.
    protected static final Set<String> dialogCreatingMethods = new HashSet<String>();

    // Global timer. Use this for all timer tasks.

    private SipTimer timer;

    // List of pending server transactions
    private ConcurrentHashMap<String, SIPServerTransaction> pendingTransactions;

    // hashtable for fast lookup
    protected ConcurrentHashMap<String, SIPClientTransaction> clientTransactionTable;

    // Set to false if you want hiwat and lowat to be consulted.
    protected boolean unlimitedServerTransactionTableSize = true;

    // Set to false if you want unlimited size of client trnansactin table.
    protected boolean unlimitedClientTransactionTableSize = true;

    // High water mark for ServerTransaction Table
    // after which requests are dropped.
    protected int serverTransactionTableHighwaterMark = 5000;

    // Low water mark for Server Tx table size after which
    // requests are selectively dropped
    protected int serverTransactionTableLowaterMark = 4000;

    // Hiwater mark for client transaction table. These defaults can be
    // overriden by stack
    // configuration.
    protected int clientTransactionTableHiwaterMark = 1000;

    // Low water mark for client tx table.
    protected int clientTransactionTableLowaterMark = 800;

    private AtomicInteger activeClientTransactionCount = new AtomicInteger(0);

    // Hashtable for server transactions.
    protected ConcurrentHashMap<String, SIPServerTransaction> serverTransactionTable;

    // A table of ongoing transactions indexed by mergeId ( for detecting merged
    // requests.
    private ConcurrentHashMap<String, SIPServerTransaction> mergeTable;

    private ConcurrentHashMap<String,SIPServerTransaction> terminatedServerTransactionsPendingAck;

    private ConcurrentHashMap<String,SIPClientTransaction> forkedClientTransactionTable;

    protected boolean deliverRetransmittedAckToListener = false;

    /*
     * ServerLog is used just for logging stack message tracecs.
     */
    protected ServerLogger serverLogger;

    /*
     * We support UDP on this stack.
     */
    boolean udpFlag;

    /*
     * Internal router. Use this for all sip: request routing.
     */
    protected DefaultRouter defaultRouter;

    /*
     * Global flag that turns logging off
     */
    protected boolean needsLogging;

    /*
     * Flag used for testing TI, bypasses filtering of ACK to non-2xx
     */
    private boolean non2XXAckPassedToListener;

    /*
     * Class that handles caching of TCP/TLS connections.
     */
    protected IOHandler ioHandler;

    /*
     * Flag that indicates that the stack is active.
     */
    protected boolean toExit;

    /*
     * Name of the stack.
     */
    protected String stackName;

    /*
     * IP address of stack -- this can be re-written by stun.
     *
     * @deprecated
     */
    protected String stackAddress;

    /*
     * INET address of stack (cached to avoid repeated lookup)
     *
     * @deprecated
     */
    protected InetAddress stackInetAddress;

    /*
     * Request factory interface (to be provided by the application)
     */
    protected StackMessageFactory sipMessageFactory;

    /*
     * Router to determine where to forward the request.
     */
    protected javax.sip.address.Router router;

    /*
     * Number of pre-allocated threads for processing udp messages. -1 means no
     * preallocated threads ( dynamically allocated threads).
     */
    protected int threadPoolSize;

    /*
     * max number of simultaneous connections.
     */
    protected int maxConnections;

    /*
     * Close accept socket on completion.
     */
    protected boolean cacheServerConnections;

    /*
     * Close connect socket on Tx termination.
     */
    protected boolean cacheClientConnections;

    /*
     * Use the user supplied router for all out of dialog requests.
     */
    protected boolean useRouterForAll;

    /*
     * Max size of message that can be read from a TCP connection.
     */
    protected int maxContentLength;

    /*
     * Max # of headers that a SIP message can contain.
     */
    protected int maxMessageSize;

    /*
     * A collection of message processors.
     */
    private Collection<MessageProcessor> messageProcessors;

    /*
     * Read timeout on TCP incoming sockets -- defines the time between reads
     * for after delivery of first byte of message.
     */
    protected int readTimeout;

    /*
     * The socket factory. Can be overriden by applications that want direct
     * access to the underlying socket.
     */

    protected NetworkLayer networkLayer;

    /*
     * Outbound proxy String ( to be handed to the outbound proxy class on
     * creation).
     */
    protected String outboundProxy;

    protected String routerPath;

    // Flag to indicate whether the stack will provide dialog
    // support.
    protected boolean isAutomaticDialogSupportEnabled;

    // The set of events for which subscriptions can be forked.

    protected HashSet<String> forkedEvents;

    // Generate a timestamp header for retransmitted requests.
    protected boolean generateTimeStampHeader;

    protected AddressResolver addressResolver;

    // Max time that the listener is allowed to take to respond to a
    // request. Default is "infinity". This property allows
    // containers to defend against buggy clients (that do not
    // want to respond to requests).
    protected int maxListenerResponseTime;
    
    //http://java.net/jira/browse/JSIP-420
    // Max time that an INVITE tx is allowed to live in the stack. Default is infinity
    protected int maxTxLifetimeInvite;
    // Max time that a Non INVITE tx is allowed to live in the stack. Default is infinity
    protected int maxTxLifetimeNonInvite;

    // A flag that indicates whether or not RFC 2543 clients are fully
    // supported.
    // If this is set to true, then To tag checking on the Dialog layer is
    // disabled in a few places - resulting in possible breakage of forked
    // dialogs.
    protected boolean rfc2543Supported = true;

    // / Provides a mechanism for applications to check the health of threads in
    // the stack
    protected ThreadAuditor threadAuditor = new ThreadAuditor();

    protected LogRecordFactory logRecordFactory;

    // Set to true if the client CANCEL transaction should be checked before
    // sending
    // it out.
    protected boolean cancelClientTransactionChecked = true;

    // Is to tag reassignment allowed.
    protected boolean remoteTagReassignmentAllowed = true;

    protected boolean logStackTraceOnMessageSend = true;

    // Receive UDP buffer size
    protected int receiveUdpBufferSize;

    // Send UDP buffer size
    protected int sendUdpBufferSize;

    protected int stackCongenstionControlTimeout = 0;

    protected boolean isBackToBackUserAgent = false;

    protected boolean checkBranchId;

    protected boolean isAutomaticDialogErrorHandlingEnabled = true;

    protected boolean isDialogTerminatedEventDeliveredForNullDialog = false;

    // Max time for a forked response to arrive. After this time, the original
    // dialog
    // is not tracked. If you want to track the original transaction you need to
    // specify
    // the max fork time with a stack init property.
    protected int maxForkTime = 0;

    // Whether or not to deliver unsolicited NOTIFY

    private boolean deliverUnsolicitedNotify = false;

    private boolean deliverTerminatedEventForAck = false;

    protected ClientAuthType clientAuth = ClientAuthType.Default;
    
    // ThreadPool when parsed SIP messages are processed. Affects the case when many TCP calls use single socket.
    private int tcpPostParsingThreadPoolSize = 0;

    // Minimum time between NAT kee alive pings from clients.
    // Any ping that exceeds this time will result in  CRLF CRLF going
    // from the UDP message channel.
    protected long minKeepAliveInterval = -1L;

    // The time after which a "dialog timeout event" is delivered to a listener.
    protected int dialogTimeoutFactor = 64;

    // factory used to create MessageParser objects
    public MessageParserFactory messageParserFactory;
    // factory used to create MessageProcessor objects
    public MessageProcessorFactory messageProcessorFactory;
    
    public long nioSocketMaxIdleTime;

    protected boolean aggressiveCleanup = false;

    public SIPMessageValve sipMessageValve;
    
    public SIPEventInterceptor sipEventInterceptor;

    protected static Executor selfRoutingThreadpoolExecutor;

    private int threadPriority = Thread.MAX_PRIORITY;

    /*
     * The socket factory. Can be overriden by applications that want direct
     * access to the underlying socket.
     */

    protected SecurityManagerProvider securityManagerProvider;

    /**
     *  Keepalive support and cleanup for client-initiated connections as per RFC 5626.
     *
     *  Based on the maximum CRLF keep-alive period of 840 seconds, per http://tools.ietf.org/html/rfc5626#section-4.4.1.
     *  a value < 0 means that the RFC 5626 will not be triggered, as a default we don't enable it not to change existing apps behavior.
     */
    protected int reliableConnectionKeepAliveTimeout = -1;
    
    private long sslHandshakeTimeout = -1;
    
    private boolean sslRenegotiationEnabled = false;
    
    protected SocketTimeoutAuditor socketTimeoutAuditor = null;

    private static class SameThreadExecutor implements Executor {

        public void execute(Runnable command) {
            command.run(); // Just run the command is the same thread
        }

    }

    public Executor getSelfRoutingThreadpoolExecutor() {
        if(selfRoutingThreadpoolExecutor == null) {
            if(this.threadPoolSize<=0) {
                selfRoutingThreadpoolExecutor = new SameThreadExecutor();
            } else {
                selfRoutingThreadpoolExecutor = Executors.newFixedThreadPool(this.threadPoolSize, new ThreadFactory() {
                    private int threadCount = 0;

                    public Thread newThread(Runnable pRunnable) {
                    	Thread thread = new Thread(pRunnable, String.format("%s-%d",
                                        "SelfRoutingThread", threadCount++));
                    	thread.setPriority(threadPriority);
                    	return thread;
                    }
                });
            }
        }
        return selfRoutingThreadpoolExecutor;
    }
      /**
     * Executor used to optimise the ReinviteSender Runnable in the sendRequest
     * of the SipDialog
       */
    private ExecutorService reinviteExecutor = Executors
            .newCachedThreadPool(new ThreadFactory() {
        private int threadCount = 0;

        public Thread newThread(Runnable pRunnable) {
                    return new Thread(pRunnable, String.format("%s-%d",
                            "ReInviteSender", threadCount++));
        }
    });

    // / Timer to regularly ping the thread auditor (on behalf of the timer
    // thread)
    protected class PingTimer extends SIPStackTimerTask {
        // / Timer thread handle
        ThreadAuditor.ThreadHandle threadHandle;

        // / Constructor
        public PingTimer(ThreadAuditor.ThreadHandle a_oThreadHandle) {
            threadHandle = a_oThreadHandle;
        }

        public void runTask() {
            // Check if we still have a timer (it may be null after shutdown)
            if (getTimer() != null) {
                // Register the timer task if we haven't done so
                if (threadHandle == null) {
                    // This happens only once since the thread handle is passed
                    // to the next scheduled ping timer
                    threadHandle = getThreadAuditor().addCurrentThread();
                }

                // Let the thread auditor know that the timer task is alive
                threadHandle.ping();

                // Schedule the next ping
                getTimer().schedule(new PingTimer(threadHandle),
                        threadHandle.getPingIntervalInMillisecs());
            }
        }

    }


    class RemoveForkedTransactionTimerTask extends SIPStackTimerTask {

        private final String forkId;

        public RemoveForkedTransactionTimerTask(String forkId) {
            this.forkId = forkId;
        }

        @Override
        public void runTask() {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            	logger.logDebug(
                        "Removing forked client transaction : forkId = " + forkId);
        	}
            forkedClientTransactionTable.remove(forkId);
        }

    }

    static {
        // Standard set of methods that create dialogs.
        dialogCreatingMethods.add(Request.REFER);
        dialogCreatingMethods.add(Request.INVITE);
        dialogCreatingMethods.add(Request.SUBSCRIBE);
    }

    /**
     * Default constructor.
     */
    protected SIPTransactionStack() {
        this.toExit = false;
        this.forkedEvents = new HashSet<String>();
        // set of events for which subscriptions can be forked.
        // Set an infinite thread pool size.
        this.threadPoolSize = -1;
        // Close response socket after infinte time.
        // for max performance
        this.cacheServerConnections = true;
        // Close the request socket after infinite time.
        // for max performance
        this.cacheClientConnections = true;
        // Max number of simultaneous connections.
        this.maxConnections = -1;
        // Array of message processors.
        // jeand : using concurrent data structure to avoid excessive blocking
        messageProcessors = new CopyOnWriteArrayList<MessageProcessor>();
        // Handle IO for this process.
        this.ioHandler = new IOHandler(this);

        // The read time out is infinite.
        this.readTimeout = -1;

        this.maxListenerResponseTime = -1;

        // The default (identity) address lookup scheme

        this.addressResolver = new DefaultAddressResolver();

        // Notify may or may not create a dialog. This is handled in
        // the code.
        // Create the transaction collections

        // Dialog dable.
        this.dialogTable = new ConcurrentHashMap<String, SIPDialog>();
        this.earlyDialogTable = new ConcurrentHashMap<String, SIPDialog>();
        this.serverDialogMergeTestTable = new ConcurrentHashMap<String, SIPDialog>();

        clientTransactionTable = new ConcurrentHashMap<String, SIPClientTransaction>();
        serverTransactionTable = new ConcurrentHashMap<String, SIPServerTransaction>();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap<String, SIPServerTransaction>();
        mergeTable = new ConcurrentHashMap<String, SIPServerTransaction>();
        retransmissionAlertTransactions = new ConcurrentHashMap<String, SIPServerTransaction>();

        // Start the timer event thread.

//        this.timer = new DefaultTimer();
        this.pendingTransactions = new ConcurrentHashMap<String, SIPServerTransaction>();


        this.forkedClientTransactionTable = new ConcurrentHashMap<String,SIPClientTransaction>();
    }

    /**
     * Re Initialize the stack instance.
     */
    protected void reInit() {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("Re-initializing !");

        // Array of message processors.
        messageProcessors = new CopyOnWriteArrayList<MessageProcessor>();
        // Handle IO for this process.
        this.ioHandler = new IOHandler(this);
        pendingTransactions = new ConcurrentHashMap<String, SIPServerTransaction>();
        clientTransactionTable = new ConcurrentHashMap<String, SIPClientTransaction>();
        serverTransactionTable = new ConcurrentHashMap<String, SIPServerTransaction>();
        retransmissionAlertTransactions = new ConcurrentHashMap<String, SIPServerTransaction>();
        mergeTable = new ConcurrentHashMap<String, SIPServerTransaction>();
        // Dialog dable.
        this.dialogTable = new ConcurrentHashMap<String, SIPDialog>();
        this.earlyDialogTable = new ConcurrentHashMap<String, SIPDialog>();
        this.serverDialogMergeTestTable = new ConcurrentHashMap<String, SIPDialog>();
        this.terminatedServerTransactionsPendingAck = new ConcurrentHashMap<String,SIPServerTransaction>();
        this.forkedClientTransactionTable = new ConcurrentHashMap<String,SIPClientTransaction>();

//        this.timer = new DefaultTimer();

        this.activeClientTransactionCount = new AtomicInteger(0);

    }

    /**
     * Creates and binds, if necessary, a socket connected to the specified
     * destination address and port and then returns its local address.
     *
     * @param dst the destination address that the socket would need to connect
     * to.
     * @param dstPort the port number that the connection would be established
     * with.
     * @param localAddress the address that we would like to bind on (null for
     * the "any" address).
     * @param localPort the port that we'd like our socket to bind to (0 for a
     * random port).
     *
     * @return the SocketAddress that this handler would use when connecting to
     * the specified destination address and port.
     *
     * @throws IOException if binding the socket fails
     */
    public SocketAddress getLocalAddressForTcpDst(InetAddress dst, int dstPort,
            InetAddress localAddress, int localPort) throws IOException {
        if (getMessageProcessorFactory() instanceof NioMessageProcessorFactory) {
            // First find the TLS message processor
            MessageProcessor[] processors = getMessageProcessors();
            for (MessageProcessor processor : processors){
                if ("TCP".equals(processor.getTransport())) {
                    NioTcpMessageChannel msgChannel =
                            (NioTcpMessageChannel) processor.createMessageChannel(dst, dstPort);
                    return msgChannel.socketChannel.socket().getLocalSocketAddress();
                }
            }
            return null;
        }
        return this.ioHandler.getLocalAddressForTcpDst(
                        dst, dstPort, localAddress, localPort);
    }

    /**
      * Creates and binds, if necessary, a TCP SSL socket connected to the
      * specified destination address and port and then returns its local
      * address.
      *
      * @param dst the destination address that the socket would need to connect
      * to.
      * @param dstPort the port number that the connection would be established
      * with.
      * @param localAddress the address that we would like to bind on
      * (null for the "any" address).
      *
      * @return the SocketAddress that this handler would use when connecting to
      * the specified destination address and port.
      *
      * @throws IOException if binding the socket fails
      */
     public SocketAddress getLocalAddressForTlsDst( InetAddress dst,
             int dstPort, InetAddress localAddress) throws IOException {

        // First find the TLS message processor
        MessageProcessor[] processors = getMessageProcessors();
        for (MessageProcessor processor : processors){
            if(processor instanceof TLSMessageProcessor){
                // Here we don't create the channel but if the channel is already
                // existing will be returned
                TLSMessageChannel msgChannel =
                    (TLSMessageChannel) processor.createMessageChannel(dst, dstPort);

                return this.ioHandler.getLocalAddressForTlsDst(
                    dst, dstPort, localAddress, msgChannel);
            } else if(processor instanceof NioTlsMessageProcessor) {
                NioTlsMessageChannel msgChannel =
                    (NioTlsMessageChannel) processor.createMessageChannel(dst, dstPort);
                return msgChannel.socketChannel.socket().getLocalSocketAddress();
            }
        }

        return null;

     }

    /**
     * For debugging -- allows you to disable logging or enable logging
     * selectively.
     *
     *
     */
    public void disableLogging() {
        this.logger.disableLogging();
    }

    /**
     * Globally enable message logging ( for debugging)
     *
     */
    public void enableLogging() {
        this.logger.enableLogging();
    }

    /**
     * Print the dialog table.
     *
     */
    public void printDialogTable() {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            this.logger.logDebug(
                    "dialog table  = " + this.dialogTable);
        }
    }

    /**
     * Retrieve a transaction from our table of transactions with pending
     * retransmission alerts.
     *
     * @param dialogId
     * @return -- the RetransmissionAlert enabled transaction corresponding to
     *         the given dialog ID.
     */
    public SIPServerTransaction getRetransmissionAlertTransaction(
            String dialogId) {
        return (SIPServerTransaction) this.retransmissionAlertTransactions
                .get(dialogId);
    }

    /**
     * Return true if extension is supported.
     *
     * @return true if extension is supported and false otherwise.
     */
    public static boolean isDialogCreated(String method) {
        return dialogCreatingMethods.contains(method);
    }

    /**
     * Add an extension method.
     *
     * @param extensionMethod
     *            -- extension method to support for dialog creation
     */
    public void addExtensionMethod(String extensionMethod) {
        if (extensionMethod.equals(Request.NOTIFY)) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("NOTIFY Supported Natively");
        } else {
            dialogCreatingMethods.add(Utils.toUpperCase(extensionMethod.trim()));
        }
    }

    /**
     * Put a dialog into the dialog table.
     *
     * @param dialog
     *            -- dialog to put into the dialog table.
     *
     */
    public SIPDialog putDialog(SIPDialog dialog) {
        String dialogId = dialog.getDialogId();
        if (dialogTable.containsKey(dialogId)) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger
                        .logDebug("putDialog: dialog already exists" + dialogId
                                + " in table = " + dialogTable.get(dialogId));
            }
            return dialogTable.get(dialogId);
        }
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("putDialog dialogId=" + dialogId
                    + " dialog = " + dialog);
        }
        dialog.setStack(this);
        if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
            logger.logStackTrace();
        dialogTable.put(dialogId, dialog);
        putMergeDialog(dialog);
        
        return dialog;
    }

    /**
     * Create a dialog and add this transaction to it.
     *
     * @param transaction
     *            -- tx to add to the dialog.
     * @return the newly created Dialog.
     */
    /**
     * Create a dialog and add this transaction to it.
     *
     * @param transaction
     *            -- tx to add to the dialog.
     * @return the newly created Dialog.
     */
    public SIPDialog createDialog(SIPTransaction transaction) {

        SIPDialog retval = null;

        if (transaction instanceof SIPClientTransaction) {
            String dialogId = ((SIPRequest) transaction.getRequest())
                    .getDialogId(false);
            if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                logger.logDebug("createDialog dialogId=" + dialogId);
            }
            if (this.earlyDialogTable.get(dialogId) != null) {
                SIPDialog dialog = this.earlyDialogTable.get(dialogId);
                if (dialog.getState() == null
                        || dialog.getState() == DialogState.EARLY) {
                    retval = dialog;
                    if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                        logger.logDebug("createDialog early Dialog found : earlyDialogId="
                                + dialogId + " earlyDialog= " + dialog);
                    }
                } else {
                    retval = new SIPDialog(transaction);
                    this.earlyDialogTable.put(dialogId, retval);
                }
            } else {
                retval = new SIPDialog(transaction);
                this.earlyDialogTable.put(dialogId, retval);
                if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                    logger.logDebug("createDialog early Dialog not found : earlyDialogId="
                            + dialogId + " created one " + retval);
                }
            }
        } else {
            retval = new SIPDialog(transaction);
        }

        return retval;

    }

    /**
     * Create a Dialog given a client tx and response.
     *
     * @param transaction
     * @param sipResponse
     * @return
     */

    public SIPDialog createDialog(SIPClientTransaction transaction,
            SIPResponse sipResponse) {
        String originalDialogId = ((SIPRequest)transaction.getRequest()).getDialogId(false);
        String earlyDialogId = sipResponse.getDialogId(false);
        if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
            logger.logDebug("createDialog originalDialogId=" + originalDialogId);
            logger.logDebug("createDialog earlyDialogId=" + earlyDialogId);
            logger.logDebug("createDialog default Dialog=" + transaction.getDefaultDialog());
            if(transaction.getDefaultDialog() != null) {
                logger.logDebug("createDialog default Dialog Id=" + transaction.getDefaultDialog().getDialogId());
            }
        }
        SIPDialog retval = null;
        SIPDialog earlyDialog = this.earlyDialogTable.get(originalDialogId);
        if (earlyDialog != null && transaction != null && (transaction.getDefaultDialog() == null || transaction.getDefaultDialog().getDialogId().equals(originalDialogId))) {
            retval = earlyDialog;
            if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                logger.logDebug("createDialog early Dialog found : earlyDialogId="
                        + originalDialogId + " earlyDialog= " + retval);
            }
            if (sipResponse.isFinalResponse()) {
                this.earlyDialogTable.remove(originalDialogId);
            }

        } else {
            retval = new SIPDialog(transaction, sipResponse);
            if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                logger.logDebug("createDialog early Dialog not found : earlyDialogId="
                        + earlyDialogId + " created one " + retval);
            }
        }
        return retval;

    }
    /**
     * Create a Dialog given a sip provider and response.
     *
     * @param sipProvider
     * @param sipResponse
     * @return
     */
    public SIPDialog createDialog(SipProviderImpl sipProvider,
            SIPResponse sipResponse) {
        return new SIPDialog(sipProvider, sipResponse);
    }
    
    /**
     * Creates a new dialog based on a received NOTIFY. The dialog state is
     * initialized appropriately. The NOTIFY differs in the From tag
     * 
     * Made this a separate method to clearly distinguish what's happening here
     * - this is a non-trivial case
     * 
     * @param subscribeTx
     *            - the transaction started with the SUBSCRIBE that we sent
     * @param notifyST
     *            - the ServerTransaction created for an incoming NOTIFY
     * @return -- a new dialog created from the subscribe original SUBSCRIBE
     *         transaction.
     * 
     * 
     */
    public SIPDialog createDialog(SIPClientTransaction subscribeTx, SIPTransaction notifyST) {
      return new SIPDialog(subscribeTx, notifyST);
    }

    /**
     * Remove the dialog from the dialog table.
     *
     * @param dialog
     *            -- dialog to remove.
     */
    public void removeDialog(SIPDialog dialog) {

        String id = dialog.getDialogId();

        String earlyId = dialog.getEarlyDialogId();

        if (earlyId != null) {
            this.earlyDialogTable.remove(earlyId);
            this.dialogTable.remove(earlyId);
        }

        removeMergeDialog(dialog.getMergeId());

        if (id != null) {

            // FHT: Remove dialog from table only if its associated dialog is
            // the same as the one
            // specified

            Object old = this.dialogTable.get(id);

            if (old == dialog) {
                this.dialogTable.remove(id);
            }

            // We now deliver DTE even when the dialog is not originally present
            // in the Dialog
            // Table
            // This happens before the dialog state is assigned.

            if (!dialog.testAndSetIsDialogTerminatedEventDelivered()) {
                DialogTerminatedEvent event = new DialogTerminatedEvent(dialog
                        .getSipProvider(), dialog);

                // Provide notification to the listener that the dialog has
                // ended.
                dialog.getSipProvider().handleEvent(event, null);

            }

        } else if ( this.isDialogTerminatedEventDeliveredForNullDialog ) {
            if (!dialog.testAndSetIsDialogTerminatedEventDelivered()) {
                DialogTerminatedEvent event = new DialogTerminatedEvent(dialog
                        .getSipProvider(), dialog);

                // Provide notification to the listener that the dialog has
                // ended.
                dialog.getSipProvider().handleEvent(event, null);

            }
        }

    }

    public SIPDialog getEarlyDialog(String dialogId) {

        SIPDialog sipDialog = (SIPDialog) earlyDialogTable.get(dialogId);
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("getEarlyDialog(" + dialogId + ") : returning " + sipDialog);
        }
        return sipDialog;

    }

    protected void removeMergeDialog(String mergeId) {
		if(mergeId != null) {
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Tyring to remove Dialog from serverDialogMerge table with Merge Dialog Id " + mergeId);
			}
			SIPDialog sipDialog = serverDialogMergeTestTable.remove(mergeId);		
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG) && sipDialog != null) {
				logger.logDebug("removed Dialog " + sipDialog + " from serverDialogMerge table with Merge Dialog Id " + mergeId);
			}
		}
	}
	
	protected void putMergeDialog(SIPDialog sipDialog) {
		if(sipDialog != null) {
			String mergeId = sipDialog.getMergeId();
			if(mergeId != null) {
				serverDialogMergeTestTable.put(mergeId, sipDialog);
				if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
					logger.logDebug("put Dialog " + sipDialog + " in serverDialogMerge table with Merge Dialog Id " + mergeId);
				}
			}
		}
	}
    
    /**
     * Return the dialog for a given dialog ID. If compatibility is enabled then
     * we do not assume the presence of tags and hence need to add a flag to
     * indicate whether this is a server or client transaction.
     *
     * @param dialogId
     *            is the dialog id to check.
     */

    public SIPDialog getDialog(String dialogId) {

        SIPDialog sipDialog = (SIPDialog) dialogTable.get(dialogId);
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("getDialog(" + dialogId + ") : returning "
                    + sipDialog);
        }
        return sipDialog;

    }

    /**
     * Remove the dialog given its dialog id. This is used for dialog id
     * re-assignment only.
     *
     * @param dialogId
     *            is the dialog Id to remove.
     */
    public void removeDialog(String dialogId) {
        if (logger.isLoggingEnabled()) {
            logger.logWarning("Silently removing dialog from table");
        }
        dialogTable.remove(dialogId);
    }

    /**
     * Find a matching client SUBSCRIBE to the incoming notify. NOTIFY requests
     * are matched to such SUBSCRIBE requests if they contain the same
     * "Call-ID", a "To" header "tag" parameter which matches the "From" header
     * "tag" parameter of the SUBSCRIBE, and the same "Event" header field.
     * Rules for comparisons of the "Event" headers are described in section
     * 7.2.1. If a matching NOTIFY request contains a "Subscription-State" of
     * "active" or "pending", it creates a new subscription and a new dialog
     * (unless they have already been created by a matching response, as
     * described above).
     *
     * @param notifyMessage
     * @return -- the matching ClientTransaction with semaphore aquired or null
     *         if no such client transaction can be found.
     */
    public SIPClientTransaction findSubscribeTransaction(
            SIPRequest notifyMessage, ListeningPointImpl listeningPoint) {
        SIPClientTransaction retval = null;
        try {
            Iterator it = clientTransactionTable.values().iterator();
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("ct table size = "
                        + clientTransactionTable.size());
            String thisToTag = notifyMessage.getTo().getTag();
            if (thisToTag == null) {
                return retval;
            }
            Event eventHdr = (Event) notifyMessage.getHeader(EventHeader.NAME);
            if (eventHdr == null) {
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger
                            .logDebug("event Header is null -- returning null");
                }

                return retval;
            }
            while (it.hasNext()) {
                SIPClientTransaction ct = (SIPClientTransaction) it.next();
                if (!ct.getMethod().equals(Request.SUBSCRIBE))
                    continue;

                // if ( sipProvider.getListeningPoint(transport) == null)
                String fromTag = ct.getOriginalRequestFromTag();
                Event hisEvent = (Event) ct.getOriginalRequestEvent();
                // Event header is mandatory but some slopply clients
                // dont include it.
                if (hisEvent == null)
                    continue;
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug("ct.fromTag = " + fromTag);
                    logger.logDebug("thisToTag = " + thisToTag);
                    logger.logDebug("hisEvent = " + hisEvent);
                    logger.logDebug("eventHdr " + eventHdr);
                }

                if (  fromTag.equalsIgnoreCase(thisToTag)
                      && hisEvent != null
                      && eventHdr.match(hisEvent)
                      && notifyMessage.getCallId().getCallId().equalsIgnoreCase(
                                ct.getOriginalRequestCallId())) {
                    if (!this.isDeliverUnsolicitedNotify() ) {
                        ct.acquireSem();
                    }
                    retval = ct;
                    return ct;
                }
            }

            return retval;
        } finally {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("findSubscribeTransaction : returning "
                        + retval);

        }

    }

    /**
     * Add entry to "Transaction Pending ACK" table.
     *
     * @param serverTransaction
     */
    public void addTransactionPendingAck(SIPServerTransaction serverTransaction) {
        String branchId = ((SIPRequest) serverTransaction.getRequest())
                .getTopmostVia().getBranch();
        if ( branchId != null ) {
            this.terminatedServerTransactionsPendingAck.put(branchId,
                    serverTransaction);
        }

    }

    /**
     * Get entry in the server transaction pending ACK table corresponding to an
     * ACK.
     *
     * @param ackMessage
     * @return
     */
    public SIPServerTransaction findTransactionPendingAck(SIPRequest ackMessage) {
        return this.terminatedServerTransactionsPendingAck.get(ackMessage
                .getTopmostVia().getBranch());
    }

    /**
     * Remove entry from "Transaction Pending ACK" table.
     *
     * @param serverTransaction
     * @return
     */

    public boolean removeTransactionPendingAck(SIPServerTransaction serverTransaction) {
//        String branchId = ((SIPRequest)serverTransaction.getRequest()).getTopmostVia().getBranch();
        String branchId = serverTransaction.getBranchId();
        if ( branchId != null
                && this.terminatedServerTransactionsPendingAck.
                        containsKey(branchId) ) {
            this.terminatedServerTransactionsPendingAck.remove(branchId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if this entry exists in the "Transaction Pending ACK" table.
     *
     * @param serverTransaction
     * @return
     */
    public boolean isTransactionPendingAck(
            SIPServerTransaction serverTransaction) {
        String branchId = ((SIPRequest) serverTransaction.getRequest())
                .getTopmostVia().getBranch();
        return this.terminatedServerTransactionsPendingAck.contains(branchId);
    }

    /**
     * Find the transaction corresponding to a given request.
     *
     * @param sipMessage
     *            request for which to retrieve the transaction.
     *
     * @param isServer
     *            search the server transaction table if true.
     *
     * @return the transaction object corresponding to the request or null if no
     *         such mapping exists.
     */
    public SIPTransaction findTransaction(SIPMessage sipMessage,
            boolean isServer) {
        SIPTransaction retval = null;
        try {
            if (isServer) {
                Via via = sipMessage.getTopmostVia();
                if (via.getBranch() != null) {
                    String key = sipMessage.getTransactionId();

                    retval = (SIPTransaction) serverTransactionTable.get(key);
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                        logger
                                .logDebug(
                                        "serverTx: looking for key " + key
                                                + " existing="
                                + serverTransactionTable);
                    if (key
                            .startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                        return retval;
                    }

                }
                // Need to scan the table for old style transactions (RFC 2543
                // style)
                Iterator<SIPServerTransaction> it = serverTransactionTable
                        .values().iterator();
                while (it.hasNext()) {
                    SIPServerTransaction sipServerTransaction = (SIPServerTransaction) it
                            .next();
                    if (sipServerTransaction
                            .isMessagePartOfTransaction(sipMessage)) {
                        retval = sipServerTransaction;
                        return retval;
                    }
                }

            } else {
                Via via = sipMessage.getTopmostVia();
                if (via.getBranch() != null) {
                    String key = sipMessage.getTransactionId();
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                        logger.logDebug(
                                "clientTx: looking for key " + key);
                    retval = (SIPTransaction) clientTransactionTable.get(key);
                    if (key
                            .startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                        return retval;
                    }

                }
                // Need to scan the table for old style transactions (RFC 2543
                // style). This is terribly slow but we need to do this
                // for backasswords compatibility.
                Iterator<SIPClientTransaction> it = clientTransactionTable
                        .values().iterator();
                while (it.hasNext()) {
                    SIPClientTransaction clientTransaction = (SIPClientTransaction) it
                            .next();
                    if (clientTransaction
                            .isMessagePartOfTransaction(sipMessage)) {
                        retval = clientTransaction;
                        return retval;
                    }
                }

            }
        } finally {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug(
                        "findTransaction: returning  : " + retval);
            }
        }
        return retval;

    }

    public SIPTransaction findTransaction(String transactionId, boolean isServer) {
        if(isServer) {
            return serverTransactionTable.get(transactionId);
        } else {
            return clientTransactionTable.get(transactionId);
        }
    }

    /**
     * Get the transaction to cancel. Search the server transaction table for a
     * transaction that matches the given transaction.
     */
    public SIPTransaction findCancelTransaction(SIPRequest cancelRequest,
            boolean isServer) {

        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("findCancelTransaction request= \n"
                    + cancelRequest + "\nfindCancelRequest isServer="
                    + isServer);
        }

        if (isServer) {
            Iterator<SIPServerTransaction> li = this.serverTransactionTable
                    .values().iterator();
            while (li.hasNext()) {
                SIPTransaction transaction = (SIPTransaction) li.next();

                SIPServerTransaction sipServerTransaction = (SIPServerTransaction) transaction;
                if (sipServerTransaction
                        .doesCancelMatchTransaction(cancelRequest))
                    return sipServerTransaction;
            }

        } else {
            Iterator<SIPClientTransaction> li = this.clientTransactionTable
                    .values().iterator();
            while (li.hasNext()) {
                SIPTransaction transaction = (SIPTransaction) li.next();

                SIPClientTransaction sipClientTransaction = (SIPClientTransaction) transaction;
                if (sipClientTransaction
                        .doesCancelMatchTransaction(cancelRequest))
                    return sipClientTransaction;

            }

        }
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger
                    .logDebug("Could not find transaction for cancel request");
        return null;
    }

    /**
     * Construcor for the stack. Registers the request and response factories
     * for the stack.
     *
     * @param messageFactory
     *            User-implemented factory for processing messages.
     */
    protected SIPTransactionStack(StackMessageFactory messageFactory) {
        this();
        this.sipMessageFactory = messageFactory;
    }

    /**
     * Finds a pending server transaction. Since each request may be handled
     * either statefully or statelessly, we keep a map of pending transactions
     * so that a duplicate transaction is not created if a second request is
     * recieved while the first one is being processed.
     *
     * @param requestReceived
     * @return -- the pending transaction or null if no such transaction exists.
     */
    public SIPServerTransaction findPendingTransaction(
            String transactionId) {
        if (this.logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            this.logger.logDebug("looking for pending tx for :"
                    + transactionId);
        }
        return (SIPServerTransaction) pendingTransactions.get(transactionId);

    }

    /**
     * See if there is a pending transaction with the same Merge ID as the Merge
     * ID obtained from the SIP Request. The Merge table is for handling the
     * following condition: If the request has no tag in the To header field,
     * the UAS core MUST check the request against ongoing transactions. If the
     * From tag, Call-ID, and CSeq exactly match those associated with an
     * ongoing transaction, but the request does not match that transaction
     * (based on the matching rules in Section 17.2.3), the UAS core SHOULD
     * generate a 482 (Loop Detected) response and pass it to the server
     * transaction.
     */
    public boolean findMergedTransaction(SIPRequest sipRequest) {
        if (!sipRequest.getMethod().equals(Request.INVITE)) {
            /*
             * Dont need to worry about request merging for Non-INVITE
             * transactions.
             */
            return false;
        }
        String mergeId = sipRequest.getMergeId();
        if (mergeId != null) {
            SIPServerTransaction mergedTransaction = (SIPServerTransaction) this.mergeTable
                    .get(mergeId);
            if (mergedTransaction != null
                    && !mergedTransaction
                            .isMessagePartOfTransaction(sipRequest)) {
            	if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
            		logger.logDebug("Mathcing merged transaction for merge id " + mergeId + " with "+ mergedTransaction);
            	}
                return true;
            }else {
                /*
                 * Check for loop detection for really late arriving
                 * requests
                 */
                SIPDialog serverDialog = this.serverDialogMergeTestTable
                        .get(mergeId);
                if (serverDialog != null && serverDialog.firstTransactionIsServerTransaction
                        && serverDialog.getState() == DialogState.CONFIRMED) {
                	if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
                		logger.logDebug("Mathcing merged dialog for merge id " + mergeId + " with "+ serverDialog);
                	}
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Remove a pending Server transaction from the stack. This is called after
     * the user code has completed execution in the listener.
     *
     * @param tr
     *            -- pending transaction to remove.
     */
    public void removePendingTransaction(SIPServerTransaction tr) {
        if (this.logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            this.logger.logDebug("removePendingTx: "
                    + tr.getTransactionId());
        }
        this.pendingTransactions.remove(tr.getTransactionId());

    }

    /**
     * Remove a transaction from the merge table.
     *
     * @param tr
     *            -- the server transaction to remove from the merge table.
     *
     */
    public void removeFromMergeTable(SIPServerTransaction tr) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("Removing tx from merge table ");
        }
        // http://java.net/jira/browse/JSIP-429
        // get the merge id from the tx instead of the request to avoid reparsing on aggressive cleanup
        String key = tr.getMergeId();        
        if (key != null) {
            this.mergeTable.remove(key);
        }
    }

    /**
     * Put this into the merge request table.
     *
     * @param sipTransaction
     *            -- transaction to put into the merge table.
     *
     */
    public void putInMergeTable(SIPServerTransaction sipTransaction,
            SIPRequest sipRequest) {
        String mergeKey = sipRequest.getMergeId();
        if (mergeKey != null) {
            this.mergeTable.put(mergeKey, sipTransaction);
        }
    }

    /**
     * Map a Server transaction (possibly sending out a 100 if the server tx is
     * an INVITE). This actually places it in the hash table and makes it known
     * to the stack.
     *
     * @param transaction
     *            -- the server transaction to map.
     */
    public void mapTransaction(SIPServerTransaction transaction) {
        if (transaction.isTransactionMapped())
            return;
        addTransactionHash(transaction);
        // transaction.startTransactionTimer();
        transaction.setTransactionMapped(true);
    }

    /**
     * Handles a new SIP request. It finds a server transaction to handle this
     * message. If none exists, it creates a new transaction.
     *
     * @param requestReceived
     *            Request to handle.
     * @param requestMessageChannel
     *            Channel that received message.
     *
     * @return A server transaction.
     */
    public ServerRequestInterface newSIPServerRequest(
            SIPRequest requestReceived, MessageChannel requestMessageChannel) {
        // Next transaction in the set
        SIPServerTransaction nextTransaction;

        final String key = requestReceived.getTransactionId();

        requestReceived.setMessageChannel(requestMessageChannel);

        if(sipMessageValve != null) {
        	// https://java.net/jira/browse/JSIP-511
        	// catching all exceptions so it doesn't make JAIN SIP to fail
        	try {	
	            if(!sipMessageValve.processRequest(
	                    requestReceived, requestMessageChannel)) {
	                if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
	                    logger.logDebug(
	                            "Request dropped by the SIP message valve. Request = " + requestReceived);
	                }
	                return null;
	            }
        	} catch(Exception e) {
        		if(logger.isLoggingEnabled(LogWriter.TRACE_ERROR)) {
                    logger.logError(
                            "An issue happening the valve on request " + 
                            		requestReceived + 
                            		" thus the message will not be processed further", e);
                }
        		return null;
        	}
        }

        // Transaction to handle this request
        SIPServerTransaction currentTransaction = (SIPServerTransaction) findTransaction(key, true);

        // Got to do this for bacasswards compatibility.
        if (currentTransaction == null
                || !currentTransaction
                        .isMessagePartOfTransaction(requestReceived)) {

            // Loop through all server transactions
            currentTransaction = null;
            if (!key.toLowerCase().startsWith(
                    SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
                Iterator<SIPServerTransaction> transactionIterator = serverTransactionTable.values().iterator();
                while (transactionIterator.hasNext()
                        && currentTransaction == null) {

                    nextTransaction = (SIPServerTransaction) transactionIterator
                            .next();

                    // If this transaction should handle this request,
                    if (nextTransaction
                            .isMessagePartOfTransaction(requestReceived)) {
                        // Mark this transaction as the one
                        // to handle this message
                        currentTransaction = nextTransaction;
                    }
                }
            }

            // If no transaction exists to handle this message
            if (currentTransaction == null) {
                currentTransaction = findPendingTransaction(key);
                if (currentTransaction != null) {
                    // Associate the tx with the received request.
                    requestReceived.setTransaction(currentTransaction);
                    if (currentTransaction.acquireSem())
                        return currentTransaction;
                    else
                        return null;

                }
                // Creating a new server tx. May fail under heavy load.
                currentTransaction = createServerTransaction(requestMessageChannel);
                if (currentTransaction != null) {
                    // currentTransaction.setPassToListener();
                    currentTransaction.setOriginalRequest(requestReceived);
                    // Associate the tx with the received request.
                    requestReceived.setTransaction(currentTransaction);
                }

            }

        }

        // Set ths transaction's encapsulated request
        // interface from the superclass
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("newSIPServerRequest( "
                    + requestReceived.getMethod() + ":"
                    + requestReceived.getTopmostVia().getBranch() + "):"
                    + currentTransaction);
        }

        if (currentTransaction != null)
            currentTransaction.setRequestInterface(sipMessageFactory
                    .newSIPServerRequest(requestReceived, currentTransaction));

        if (currentTransaction != null && currentTransaction.acquireSem()) {
            return currentTransaction;
        } else if (currentTransaction != null) {
            try {
                /*
                 * Already processing a message for this transaction. SEND a
                 * trying ( message already being processed ).
                 */
                if (currentTransaction
                        .isMessagePartOfTransaction(requestReceived)
                        && currentTransaction.getMethod().equals(
                                requestReceived.getMethod())) {
                    SIPResponse trying = requestReceived
                            .createResponse(Response.TRYING);
                    trying.removeContent();
                    currentTransaction.getMessageChannel().sendMessage(trying);
                }
            } catch (Exception ex) {
                if (logger.isLoggingEnabled())
                    logger.logError("Exception occured sending TRYING");
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Handles a new SIP response. It finds a client transaction to handle this
     * message. If none exists, it sends the message directly to the superclass.
     *
     * @param responseReceived
     *            Response to handle.
     * @param responseMessageChannel
     *            Channel that received message.
     *
     * @return A client transaction.
     */
    public ServerResponseInterface newSIPServerResponse(
            SIPResponse responseReceived, MessageChannel responseMessageChannel) {

        // Iterator through all client transactions
        Iterator<SIPClientTransaction> transactionIterator;
        // Next transaction in the set
        SIPClientTransaction nextTransaction;
        // Transaction to handle this request
        SIPClientTransaction currentTransaction;

        if(sipMessageValve != null) {
        	// https://java.net/jira/browse/JSIP-511
        	// catching all exceptions so it doesn't make JAIN SIP to fail
        	try {
	            if(!sipMessageValve.processResponse(
	                    responseReceived, responseMessageChannel)) {
	                if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
	                    logger.logDebug(
	                            "Response dropped by the SIP message valve. Response = " + responseReceived);
	                }
	                return null;
	            }
        	} catch(Exception e) {
        		if(logger.isLoggingEnabled(LogWriter.TRACE_ERROR)) {
                    logger.logError(
                            "An issue happening the valve on response " + 
                            		responseReceived + 
                            		" thus the message will not be processed further", e);
                }
        		return null;
        	}
        }

        String key = responseReceived.getTransactionId();

        // Note that for RFC 3261 compliant operation, this lookup will
        // return a tx if one exists and hence no need to search through
        // the table.
        currentTransaction = (SIPClientTransaction) findTransaction(key, false);

        if (currentTransaction == null
                || (!currentTransaction
                        .isMessagePartOfTransaction(responseReceived) && !key
                        .startsWith(SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE))) {
            // Loop through all client transactions

            transactionIterator = clientTransactionTable.values().iterator();
            currentTransaction = null;
            while (transactionIterator.hasNext() && currentTransaction == null) {

                nextTransaction = (SIPClientTransaction) transactionIterator
                        .next();

                // If this transaction should handle this request,
                if (nextTransaction
                        .isMessagePartOfTransaction(responseReceived)) {

                    // Mark this transaction as the one to
                    // handle this message
                    currentTransaction = nextTransaction;

                }

            }

            // If no transaction exists to handle this message,
            if (currentTransaction == null) {
                // JvB: Need to log before passing the response to the client
                // app, it
                // gets modified!
                if (this.logger.isLoggingEnabled(StackLogger.TRACE_INFO)) {
                    responseMessageChannel.logResponse(responseReceived, System
                            .currentTimeMillis(), "before processing");
                }

                // Pass the message directly to the TU
                return sipMessageFactory.newSIPServerResponse(responseReceived,
                        responseMessageChannel);

            }
        }

        // Aquire the sem -- previous request may still be processing.
        boolean acquired = currentTransaction.acquireSem();
        // Set ths transaction's encapsulated response interface
        // from the superclass
        if (this.logger.isLoggingEnabled(StackLogger.TRACE_INFO)) {
            currentTransaction.getMessageChannel().logResponse(responseReceived, System
                    .currentTimeMillis(), "before processing");
        }

        if (acquired) {
            ServerResponseInterface sri = sipMessageFactory
                    .newSIPServerResponse(responseReceived, currentTransaction.getMessageChannel());
            if (sri != null) {
                currentTransaction.setResponseInterface(sri);
            } else {
                if (this.logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    this.logger
                            .logDebug("returning null - serverResponseInterface is null!");
                }
                currentTransaction.releaseSem();
                return null;
            }
        } else {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                this.logger.logDebug("Could not aquire semaphore !!");
        }

        if (acquired)
            return currentTransaction;
        else
            return null;

    }

    /**
     * Creates a client transaction to handle a new request. Gets the real
     * message channel from the superclass, and then creates a new client
     * transaction wrapped around this channel.
     *
     * @param nextHop
     *            Hop to create a channel to contact.
     */
    public MessageChannel createMessageChannel(SIPRequest request,
            MessageProcessor mp, Hop nextHop) throws IOException {

        // Create a new client transaction around the
        // superclass' message channel
        // Create the host/port of the target hop
        Host targetHost = new Host();
        targetHost.setHostname(nextHop.getHost());
        HostPort targetHostPort = new HostPort();
        targetHostPort.setHost(targetHost);
        targetHostPort.setPort(nextHop.getPort());
        MessageChannel returnChannel = mp.createMessageChannel(targetHostPort);
        return returnChannel;

    }

    /**
     * Creates a client transaction that encapsulates a MessageChannel. Useful
     * for implementations that want to subclass the standard
     *
     * @param encapsulatedMessageChannel
     *            Message channel of the transport layer.
     */
    public SIPClientTransaction createClientTransaction(SIPRequest sipRequest,
            MessageChannel encapsulatedMessageChannel) {
        SIPClientTransaction ct = new SIPClientTransactionImpl(this,
                encapsulatedMessageChannel);
        ct.setOriginalRequest(sipRequest);
        return ct;
    }

    /**
     * Creates a server transaction that encapsulates a MessageChannel. Useful
     * for implementations that want to subclass the standard
     *
     * @param encapsulatedMessageChannel
     *            Message channel of the transport layer.
     */
    public SIPServerTransaction createServerTransaction(
            MessageChannel encapsulatedMessageChannel) {
        // Issue 256 : be consistent with createClientTransaction, if
        // unlimitedServerTransactionTableSize is true,
        // a new Server Transaction is created no matter what
        if (unlimitedServerTransactionTableSize) {
            return new SIPServerTransactionImpl(this, encapsulatedMessageChannel);
        } else {
            float threshold = ((float) (serverTransactionTable.size() - serverTransactionTableLowaterMark))
                    / ((float) (serverTransactionTableHighwaterMark - serverTransactionTableLowaterMark));
            boolean decision = Math.random() > 1.0 - threshold;
            if (decision) {
                return null;
            } else {
                return new SIPServerTransactionImpl(this,
                        encapsulatedMessageChannel);
            }

        }

    }

    /**
     * Get the size of the client transaction table.
     *
     * @return -- size of the ct table.
     */
    public int getClientTransactionTableSize() {
        return this.clientTransactionTable.size();
    }

    /**
     * Get the size of the server transaction table.
     *
     * @return -- size of the server table.
     */
    public int getServerTransactionTableSize() {
        return this.serverTransactionTable.size();
    }

    /**
     * Add a new client transaction to the set of existing transactions. Add it
     * to the top of the list so an incoming response has less work to do in
     * order to find the transaction.
     *
     * @param clientTransaction
     *            -- client transaction to add to the set.
     */
    public void addTransaction(SIPClientTransaction clientTransaction) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("added transaction " + clientTransaction);
        addTransactionHash(clientTransaction);

    }

    /**
     * Remove transaction. This actually gets the tx out of the search
     * structures which the stack keeps around. When the tx
     */
    public void removeTransaction(SIPTransaction sipTransaction) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("removeTransaction: Removing Transaction = "
                    + sipTransaction.getTransactionId() + " transaction = "
                    + sipTransaction);
        }
        Object removed = null;
        try {
        	if (sipTransaction instanceof SIPServerTransaction) {
        		if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
        			logger.logStackTrace();
        		}
        		String key = sipTransaction.getTransactionId();
        		removed = serverTransactionTable.remove(key);
        		String method = sipTransaction.getMethod();
        		this
        		.removePendingTransaction((SIPServerTransaction) sipTransaction);
        		this
        		.removeTransactionPendingAck((SIPServerTransaction) sipTransaction);
        		if (method.equalsIgnoreCase(Request.INVITE)) {
        			this
        			.removeFromMergeTable((SIPServerTransaction) sipTransaction);
        		}
        		// Send a notification to the listener.
        		SipProviderImpl sipProvider = (SipProviderImpl) sipTransaction
        		.getSipProvider();
        		if (removed != null
        				&& sipTransaction.testAndSetTransactionTerminatedEvent()) {
        			TransactionTerminatedEvent event = new TransactionTerminatedEvent(
        					sipProvider, (ServerTransaction) sipTransaction);

        			sipProvider.handleEvent(event, sipTransaction);

        		}
        	} else {

        		String key = sipTransaction.getTransactionId();
        		removed = clientTransactionTable.remove(key);

        		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        			logger.logDebug("REMOVED client tx " + removed + " KEY = "
        					+ key);


        		}
        		if ( removed != null ) {
        			SIPClientTransaction clientTx = (SIPClientTransaction)removed;
        			final String forkId = clientTx.getForkId();
        			if (forkId != null && clientTx.isInviteTransaction()
        					&& this.maxForkTime != 0) {
        				if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        	            	logger.logDebug(
        	                        "Scheduling to remove forked client transaction : forkId = " + forkId + " in "  + this.maxForkTime + " seconds");
        	        	}
        				this.timer.schedule(new RemoveForkedTransactionTimerTask(
        						forkId), this.maxForkTime * 1000);
        				clientTx.stopExpiresTimer();
        			}
        		}

        		// Send a notification to the listener.
        		if (removed != null
        				&& sipTransaction.testAndSetTransactionTerminatedEvent()) {
        			SipProviderImpl sipProvider = (SipProviderImpl) sipTransaction
        			.getSipProvider();
        			TransactionTerminatedEvent event = new TransactionTerminatedEvent(
        					sipProvider, (ClientTransaction) sipTransaction);

        			sipProvider.handleEvent(event, sipTransaction);
        		}

        	}
        } finally {
        	// http://java.net/jira/browse/JSIP-420
        	if(removed != null) {
            	((SIPTransaction)removed).cancelMaxTxLifeTimeTimer();
            }
        	if ( logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        		logger.logDebug(String.format("removeTransaction: Table size : " +
        				" clientTransactionTable %d " +
        				" serverTransactionTable %d " +
        				" mergetTable %d " +
        				" terminatedServerTransactionsPendingAck %d  " +
        				" forkedClientTransactionTable %d " +
        				" pendingTransactions %d " , 
        				clientTransactionTable.size(),
        				serverTransactionTable.size(),
        				mergeTable.size(),
        				terminatedServerTransactionsPendingAck.size(),
        				forkedClientTransactionTable.size(),
        				pendingTransactions.size()
        		));
        }
    }

    /**
     * Add a new server transaction to the set of existing transactions. Add it
     * to the top of the list so an incoming ack has less work to do in order to
     * find the transaction.
     *
     * @param serverTransaction
     *            -- server transaction to add to the set.
     */
    public void addTransaction(SIPServerTransaction serverTransaction)
            throws IOException {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("added transaction " + serverTransaction);
        serverTransaction.map();

        addTransactionHash(serverTransaction);

    }

    /**
     * Hash table for quick lookup of transactions. Here we wait for room if
     * needed.
     */
    private void addTransactionHash(SIPTransaction sipTransaction) {
        SIPRequest sipRequest = sipTransaction.getOriginalRequest();
        SIPTransaction existingTx = null;
        if (sipTransaction instanceof SIPClientTransaction) {
            if (!this.unlimitedClientTransactionTableSize) {
                if (this.activeClientTransactionCount.get() > clientTransactionTableHiwaterMark) {
                    try {
                        synchronized (this.clientTransactionTable) {
                            this.clientTransactionTable.wait();
                            this.activeClientTransactionCount.incrementAndGet();
                        }

                    } catch (Exception ex) {
                        if (logger.isLoggingEnabled()) {
                            logger.logError(
                                    "Exception occured while waiting for room",
                                    ex);
                        }

                    }
                }
            } else {
                this.activeClientTransactionCount.incrementAndGet();
            }
            String key = sipRequest.getTransactionId();
            existingTx = clientTransactionTable.putIfAbsent(key,
                    (SIPClientTransaction) sipTransaction);
            
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger
                        .logDebug(" putTransactionHash : " + " key = " + key);
            }
        } else {
            String key = sipRequest.getTransactionId();

            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger
                        .logDebug(" putTransactionHash : " + " key = " + key);
            }
            existingTx = serverTransactionTable.putIfAbsent(key,
                    (SIPServerTransaction) sipTransaction);

        }
    	// http://java.net/jira/browse/JSIP-420
        if(existingTx == null) {
        	sipTransaction.scheduleMaxTxLifeTimeTimer();
        }

    }

    /**
     * This method is called when a client tx transitions to the Completed or
     * Terminated state.
     *
     */
    protected void decrementActiveClientTransactionCount() {

        if (this.activeClientTransactionCount.decrementAndGet() <= this.clientTransactionTableLowaterMark
                && !this.unlimitedClientTransactionTableSize) {
            synchronized (this.clientTransactionTable) {

                clientTransactionTable.notify();

            }
        }
    }

    /**
     * Remove the transaction from transaction hash.
     */
    protected void removeTransactionHash(SIPTransaction sipTransaction) {
        SIPRequest sipRequest = sipTransaction.getOriginalRequest();
        if (sipRequest == null)
            return;
        Object removed = null;
        if (sipTransaction instanceof SIPClientTransaction) {
            String key = sipTransaction.getTransactionId();
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logStackTrace();
                logger.logDebug("removing client Tx : " + key);
            }
            removed = clientTransactionTable.remove(key);

        } else if (sipTransaction instanceof SIPServerTransaction) {
            String key = sipTransaction.getTransactionId();
            removed = serverTransactionTable.remove(key);
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("removing server Tx : " + key);
            }
        }
    	// http://java.net/jira/browse/JSIP-420
        if(removed != null) {
        	((SIPTransaction)removed).cancelMaxTxLifeTimeTimer();
        }
    }

    /**
     * Invoked when an error has ocurred with a transaction.
     *
     * @param transactionErrorEvent
     *            Error event.
     */
    public synchronized void transactionErrorEvent(
            SIPTransactionErrorEvent transactionErrorEvent) {
        SIPTransaction transaction = (SIPTransaction) transactionErrorEvent
                .getSource();

        if (transactionErrorEvent.getErrorID() == SIPTransactionErrorEvent.TRANSPORT_ERROR) {
            // Kill scanning of this transaction.
            transaction.setState(TransactionState._TERMINATED);
            if (transaction instanceof SIPServerTransaction) {
                // let the reaper get him
                ((SIPServerTransaction) transaction).setCollectionTime(0);
            }
            transaction.disableTimeoutTimer();
            transaction.disableRetransmissionTimer();
            // Send a IO Exception to the Listener.
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * gov.nist.javax.sip.stack.SIPDialogEventListener#dialogErrorEvent(gov.
     * nist.javax.sip.stack.SIPDialogErrorEvent)
     */
    public synchronized void dialogErrorEvent(
            SIPDialogErrorEvent dialogErrorEvent) {
        SIPDialog sipDialog = (SIPDialog) dialogErrorEvent.getSource();
        SipListener sipListener = ((SipStackImpl)this).getSipListener();
        // if the app is not implementing the SipListenerExt interface we delete
        // the dialog to avoid leaks
        if(sipDialog != null && !(sipListener instanceof SipListenerExt)) {
            sipDialog.delete();
        }
    }

    /**
     * Stop stack. Clear all the timer stuff. Make the stack close all accept
     * connections and return. This is useful if you want to start/stop the
     * stack several times from your application. Caution : use of this function
     * could cause peculiar bugs as messages are prcessed asynchronously by the
     * stack.
     */
    public void stopStack() {
        // Prevent NPE on two concurrent stops
        this.toExit = true;        

        // JvB: set it to null, SIPDialog tries to schedule things after stop
        this.pendingTransactions.clear();
        synchronized (this) {
            this.notifyAll();
        }
        synchronized (this.clientTransactionTable) {
            clientTransactionTable.notifyAll();
        }
        
        if(selfRoutingThreadpoolExecutor != null && selfRoutingThreadpoolExecutor instanceof ExecutorService) {
        	((ExecutorService)selfRoutingThreadpoolExecutor).shutdown();
        }
        selfRoutingThreadpoolExecutor = null;

        // Threads must periodically check this flag.
        MessageProcessor[] processorList;
        processorList = getMessageProcessors();
        for (int processorIndex = 0; processorIndex < processorList.length; processorIndex++) {
            removeMessageProcessor(processorList[processorIndex]);
        }
        closeAllSockets();
        // Let the processing complete.

        if (this.timer != null)
            this.timer.stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        this.clientTransactionTable.clear();
        this.serverTransactionTable.clear();

        this.dialogTable.clear();
        this.serverLogger.closeLogFile();

    }
    
    public void closeAllSockets() {
    	this.ioHandler.closeAll();
    	for(MessageProcessor p : messageProcessors) {
    		if(p instanceof NioTcpMessageProcessor) {
    			NioTcpMessageProcessor niop = (NioTcpMessageProcessor)p;
    			niop.nioHandler.closeAll();
    		}
    	}
    }

    /**
     * Put a transaction in the pending transaction list. This is to avoid a
     * race condition when a duplicate may arrive when the application is
     * deciding whether to create a transaction or not.
     */
    public void putPendingTransaction(SIPServerTransaction tr) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("putPendingTransaction: " + tr);

        this.pendingTransactions.put(tr.getTransactionId(), tr);

    }

    /**
     * Return the network layer (i.e. the interface for socket creation or the
     * socket factory for the stack).
     *
     * @return -- the registered Network Layer.
     */
    public NetworkLayer getNetworkLayer() {
        if (networkLayer == null) {
            return DefaultNetworkLayer.SINGLETON;
        } else {
            return networkLayer;
        }
    }

    /**
     * Return true if logging is enabled for this stack.
     * Deprecated. Use StackLogger.isLoggingEnabled instead
     *
     * @return true if logging is enabled for this stack instance.
     */
    @Deprecated
    public boolean isLoggingEnabled() {
        return logger == null ? false : logger
                .isLoggingEnabled();
    }

    /**
     * Deprecated. Use StackLogger.isLoggingEnabled instead
     * @param level
     * @return
     */
    @Deprecated
    public boolean isLoggingEnabled( int level ) {
        return logger == null ? false
                : logger.isLoggingEnabled( level );
    }

    /**
     * Get the logger. This method should be deprected.
     * Use static logger = CommonLogger.getLogger() instead
     *
     * @return --the logger for the sip stack. Each stack has its own logger
     *         instance.
     */
    @Deprecated
    public StackLogger getStackLogger() {
        return logger;
    }

    /**
     * Server log is the place where we log messages for the signaling trace
     * viewer.
     *
     * @return -- the log file where messages are logged for viewing by the
     *         trace viewer.
     */
    public ServerLogger getServerLogger() {
        return this.serverLogger;
    }

    /**
     * Maximum size of a single TCP message. Limiting the size of a single TCP
     * message prevents flooding attacks.
     *
     * @return the size of a single TCP message.
     */
    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }

    /**
     * Set the flag that instructs the stack to only start a single thread for
     * sequentially processing incoming udp messages (thus serializing the
     * processing). Same as setting thread pool size to 1.
     */
    public void setSingleThreaded() {
        this.threadPoolSize = 1;
    }

    /**
     * If all calls are occurring on a single TCP socket then the stack would process them in single thread.
     * This property allows immediately after parsing to split the load into many threads which increases
     * the performance significantly. If set to 0 then we just use the old model with single thread.
     *
     * @return
     */
    public int getTcpPostParsingThreadPoolSize() {
        return tcpPostParsingThreadPoolSize;
    }

    /**
     * If all calls are occurring on a single TCP socket then the stack would process them in single thread.
     * This property allows immediately after parsing to split the load into many threads which increases
     * the performance significantly. If set to 0 then we just use the old model with single thread.
     *
     * @param tcpPostParsingThreadPoolSize
     */
    public void setTcpPostParsingThreadPoolSize(int tcpPostParsingThreadPoolSize) {
        this.tcpPostParsingThreadPoolSize = tcpPostParsingThreadPoolSize;
    }

    /**
     * Set the thread pool size for processing incoming UDP messages. Limit the
     * total number of threads for processing udp messages.
     *
     * @param size
     *            -- the thread pool size.
     *
     */
    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
    }

    /**
     * Set the max # of simultaneously handled TCP connections.
     *
     * @param nconnections
     *            -- the number of connections to handle.
     */
    public void setMaxConnections(int nconnections) {
        this.maxConnections = nconnections;
    }

    /**
     * Get the default route string.
     *
     * @param sipRequest
     *            is the request for which we want to compute the next hop.
     * @throws SipException
     */
    public Hop getNextHop(SIPRequest sipRequest) throws SipException {
        if (this.useRouterForAll) {
            // Use custom router to route all messages.
            if (router != null)
                return router.getNextHop(sipRequest);
            else
                return null;
        } else {
            // Also non-SIP request containing Route headers goes to the default
            // router
            if (sipRequest.getRequestURI().isSipURI()
                    || sipRequest.getRouteHeaders() != null) {
                return defaultRouter.getNextHop(sipRequest);
            } else if (router != null) {
                return router.getNextHop(sipRequest);
            } else
                return null;
        }
    }

    /**
     * Set the descriptive name of the stack.
     *
     * @param stackName
     *            -- descriptive name of the stack.
     */
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }



    /**
     * Set my address.
     *
     * @param stackAddress
     *            -- A string containing the stack address.
     */
    protected void setHostAddress(String stackAddress)
            throws UnknownHostException {
        if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
                && stackAddress.trim().charAt(0) != '[')
            this.stackAddress = '[' + stackAddress + ']';
        else
            this.stackAddress = stackAddress;
        this.stackInetAddress = InetAddress.getByName(stackAddress);
    }

    /**
     * Get my address.
     *
     * @return hostAddress - my host address or null if no host address is
     *         defined.
     * @deprecated
     */
    public String getHostAddress() {

        // JvB: for 1.2 this may return null...
        return this.stackAddress;
    }

    /**
     * Set the router algorithm. This is meant for routing messages out of
     * dialog or for non-sip uri's.
     *
     * @param router
     *            A class that implements the Router interface.
     */
    protected void setRouter(Router router) {
        this.router = router;
    }

    /**
     * Get the router algorithm.
     *
     * @return Router router
     */
    public Router getRouter(SIPRequest request) {
        if (request.getRequestLine() == null) {
            return this.defaultRouter;
        } else if (this.useRouterForAll) {
            return this.router;
        } else {
            if (request.getRequestURI().getScheme().equals("sip")
                    || request.getRequestURI().getScheme().equals("sips")) {
                return this.defaultRouter;
            } else {
                if (this.router != null)
                    return this.router;
                else
                    return defaultRouter;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.SipStack#getRouter()
     */
    public Router getRouter() {
        return this.router;
    }

    /**
     * return the status of the toExit flag.
     *
     * @return true if the stack object is alive and false otherwise.
     */
    public boolean isAlive() {
        return !toExit;
    }

    /**
     * Adds a new MessageProcessor to the list of running processors for this
     * SIPStack and starts it. You can use this method for dynamic stack
     * configuration.
     */
    protected void addMessageProcessor(MessageProcessor newMessageProcessor)
            throws IOException {
            // Suggested changes by Jeyashankher, jai@lucent.com
            // newMessageProcessor.start() can fail
            // because a local port is not available
            // This throws an IOException.
            // We should not add the message processor to the
            // local list of processors unless the start()
            // call is successful.
            // newMessageProcessor.start();
            messageProcessors.add(newMessageProcessor);


    }

    /**
     * Removes a MessageProcessor from this SIPStack.
     *
     * @param oldMessageProcessor
     */
    protected void removeMessageProcessor(MessageProcessor oldMessageProcessor) {
            if (messageProcessors.remove(oldMessageProcessor)) {
                oldMessageProcessor.stop();
            }

    }

    /**
     * Gets an array of running MessageProcessors on this SIPStack.
     * Acknowledgement: Jeff Keyser suggested that applications should have
     * access to the running message processors and contributed this code.
     *
     * @return an array of running message processors.
     */
    protected MessageProcessor[] getMessageProcessors() {
            return (MessageProcessor[]) messageProcessors
                    .toArray(new MessageProcessor[0]);

    }

    /**
     * Creates the equivalent of a JAIN listening point and attaches to the
     * stack.
     *
     * @param ipAddress
     *            -- ip address for the listening point.
     * @param port
     *            -- port for the listening point.
     * @param transport
     *            -- transport for the listening point.
     */
    protected MessageProcessor createMessageProcessor(InetAddress ipAddress,
            int port, String transport) throws java.io.IOException {
        MessageProcessor newMessageProcessor = messageProcessorFactory.createMessageProcessor(this, ipAddress, port, transport);
        this.addMessageProcessor(newMessageProcessor);
        return newMessageProcessor;
    }

    /**
     * Set the message factory.
     *
     * @param messageFactory
     *            -- messageFactory to set.
     */
    protected void setMessageFactory(StackMessageFactory messageFactory) {
        this.sipMessageFactory = messageFactory;
    }

    /**
     * Creates a new MessageChannel for a given Hop.
     *
     * @param sourceIpAddress
     *            - Ip address of the source of this message.
     *
     * @param sourcePort
     *            - source port of the message channel to be created.
     *
     * @param nextHop
     *            Hop to create a MessageChannel to.
     *
     * @return A MessageChannel to the specified Hop, or null if no
     *         MessageProcessors support contacting that Hop.
     *
     * @throws UnknownHostException
     *             If the host in the Hop doesn't exist.
     */
    public MessageChannel createRawMessageChannel(String sourceIpAddress,
            int sourcePort, Hop nextHop) throws UnknownHostException {
        Host targetHost;
        HostPort targetHostPort;
        Iterator processorIterator;
        MessageProcessor nextProcessor;
        MessageChannel newChannel;

        // Create the host/port of the target hop
        targetHost = new Host();
        targetHost.setHostname(nextHop.getHost());
        targetHostPort = new HostPort();
        targetHostPort.setHost(targetHost);
        targetHostPort.setPort(nextHop.getPort());

        // Search each processor for the correct transport
        newChannel = null;
        processorIterator = messageProcessors.iterator();
        while (processorIterator.hasNext() && newChannel == null) {
            nextProcessor = (MessageProcessor) processorIterator.next();
            // If a processor that supports the correct
            // transport is found,
            if (nextHop.getTransport().equalsIgnoreCase(
                    nextProcessor.getTransport())
                    && sourceIpAddress.equals(nextProcessor.getIpAddress()
                            .getHostAddress())
                    && sourcePort == nextProcessor.getPort()) {
                try {
                    // Create a channel to the target
                    // host/port
                    newChannel = nextProcessor
                            .createMessageChannel(targetHostPort);
                } catch (UnknownHostException ex) {
                    if (logger.isLoggingEnabled())
                        logger.logException(ex);
                    throw ex;
                } catch (IOException e) {
                    if (logger.isLoggingEnabled())
                        logger.logException(e);
                    // Ignore channel creation error -
                    // try next processor
                }
            }
        }

        if(newChannel == null) {
            if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("newChanne is null, messageProcessors.size = " + messageProcessors.size());
                processorIterator = messageProcessors.iterator();
                while (processorIterator.hasNext() && newChannel == null) {
                    nextProcessor = (MessageProcessor) processorIterator.next();
                    logger.logDebug("nextProcessor:" + nextProcessor + "| transport = " +
                            nextProcessor.getTransport() + " ipAddress=" +
                            nextProcessor.getIpAddress() + " port=" +
                            nextProcessor.getPort());
                }
                logger.logDebug("More info on newChannel=null");
                logger.logDebug("nextHop=" + nextHop + " sourceIp=" + sourceIpAddress +
                        " sourcePort=" + sourcePort + " targetHostPort=" + targetHostPort);
            }
        }
        // Return the newly-created channel
        return newChannel;
    }

    /**
     * Return true if a given event can result in a forked subscription. The
     * stack is configured with a set of event names that can result in forked
     * subscriptions.
     *
     * @param ename
     *            -- event name to check.
     *
     */
    public boolean isEventForked(String ename) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("isEventForked: " + ename + " returning "
                    + this.forkedEvents.contains(ename));
        }
        return this.forkedEvents.contains(ename);
    }

    /**
     * get the address resolver interface.
     *
     * @return -- the registered address resolver.
     */
    public AddressResolver getAddressResolver() {
        return this.addressResolver;
    }

    /**
     * Set the address resolution interface
     *
     * @param addressResolver
     *            -- the address resolver to set.
     */
    public void setAddressResolver(AddressResolver addressResolver) {
        this.addressResolver = addressResolver;
    }

    /**
     * Set the logger factory.
     *
     * @param logRecordFactory
     *            -- the log record factory to set.
     */
    public void setLogRecordFactory(LogRecordFactory logRecordFactory) {
        this.logRecordFactory = logRecordFactory;
    }

    /**
     * get the thread auditor object
     *
     * @return -- the thread auditor of the stack
     */
    public ThreadAuditor getThreadAuditor() {
        return this.threadAuditor;
    }

    // /
    // / Stack Audit methods
    // /

    /**
     * Audits the SIP Stack for leaks
     *
     * @return Audit report, null if no leaks were found
     */
    public String auditStack(Set activeCallIDs, long leakedDialogTimer,
            long leakedTransactionTimer) {
        String auditReport = null;
        String leakedDialogs = auditDialogs(activeCallIDs, leakedDialogTimer);
        String leakedServerTransactions = auditTransactions(
                serverTransactionTable, leakedTransactionTimer);
        String leakedClientTransactions = auditTransactions(
                clientTransactionTable, leakedTransactionTimer);
        if (leakedDialogs != null || leakedServerTransactions != null
                || leakedClientTransactions != null) {
            auditReport = "SIP Stack Audit:\n"
                    + (leakedDialogs != null ? leakedDialogs : "")
                    + (leakedServerTransactions != null ? leakedServerTransactions
                            : "")
                    + (leakedClientTransactions != null ? leakedClientTransactions
                            : "");
        }
        return auditReport;
    }

    /**
     * Audits SIP dialogs for leaks - Compares the dialogs in the dialogTable
     * with a list of Call IDs passed by the application. - Dialogs that are not
     * known by the application are leak suspects. - Kill the dialogs that are
     * still around after the timer specified.
     *
     * @return Audit report, null if no dialog leaks were found
     */
    private String auditDialogs(Set activeCallIDs, long leakedDialogTimer) {
        String auditReport = "  Leaked dialogs:\n";
        int leakedDialogs = 0;
        long currentTime = System.currentTimeMillis();

        // Make a shallow copy of the dialog list.
        // This copy will remain intact as leaked dialogs are removed by the
        // stack.
        LinkedList dialogs;
        synchronized (dialogTable) {
            dialogs = new LinkedList(dialogTable.values());
        }

        // Iterate through the dialogDialog, get the callID of each dialog and
        // check if it's in the
        // list of active calls passed by the application. If it isn't, start
        // the timer on it.
        // If the timer has expired, kill the dialog.
        Iterator it = dialogs.iterator();
        while (it.hasNext()) {
            // Get the next dialog
            SIPDialog itDialog = (SIPDialog) it.next();

            // Get the call id associated with this dialog
            CallIdHeader callIdHeader = (itDialog != null ? itDialog
                    .getCallId() : null);
            String callID = (callIdHeader != null ? callIdHeader.getCallId()
                    : null);

            // Check if the application knows about this call id
            if (itDialog != null && callID != null
                    && !activeCallIDs.contains(callID)) {
                // Application doesn't know anything about this dialog...
                if (itDialog.auditTag == 0) {
                    // Mark this dialog as suspect
                    itDialog.auditTag = currentTime;
                } else {
                    // We already audited this dialog before. Check if his
                    // time's up.
                    if (currentTime - itDialog.auditTag >= leakedDialogTimer) {
                        // Leaked dialog found
                        leakedDialogs++;

                        // Generate report
                        DialogState dialogState = itDialog.getState();
                        String dialogReport = "dialog id: "
                                + itDialog.getDialogId()
                                + ", dialog state: "
                                + (dialogState != null ? dialogState.toString()
                                        : "null");
                        auditReport += "    " + dialogReport + "\n";

                        // Kill it
                        itDialog.setState(SIPDialog.TERMINATED_STATE);
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                            logger.logDebug("auditDialogs: leaked "
                                    + dialogReport);
                    }
                }
            }
        }

        // Return final report
        if (leakedDialogs > 0) {
            auditReport += "    Total: " + Integer.toString(leakedDialogs)
                    + " leaked dialogs detected and removed.\n";
        } else {
            auditReport = null;
        }
        return auditReport;
    }

    /**
     * Audits SIP transactions for leaks
     *
     * @return Audit report, null if no transaction leaks were found
     */
    private String auditTransactions(ConcurrentHashMap transactionsMap,
            long a_nLeakedTransactionTimer) {
        String auditReport = "  Leaked transactions:\n";
        int leakedTransactions = 0;
        long currentTime = System.currentTimeMillis();

        // Make a shallow copy of the transaction list.
        // This copy will remain intact as leaked transactions are removed by
        // the stack.
        LinkedList transactionsList = new LinkedList(transactionsMap.values());

        // Iterate through our copy
        Iterator it = transactionsList.iterator();
        while (it.hasNext()) {
            SIPTransaction sipTransaction = (SIPTransaction) it.next();
            if (sipTransaction != null) {
                if (sipTransaction.getAuditTag() == 0) {
                    // First time we see this transaction. Mark it as audited.
                    sipTransaction.setAuditTag(currentTime);
                } else {
                    // We've seen this transaction before. Check if his time's
                    // up.
                    if (currentTime - sipTransaction.getAuditTag() >= a_nLeakedTransactionTimer) {
                        // Leaked transaction found
                        leakedTransactions++;

                        // Generate some report
                        TransactionState transactionState = sipTransaction
                                .getState();
                        SIPRequest origRequest = sipTransaction
                                .getOriginalRequest();
                        String origRequestMethod = (origRequest != null ? origRequest
                                .getMethod()
                                : null);
                        String transactionReport = sipTransaction.getClass()
                                .getName()
                                + ", state: "
                                + (transactionState != null ? transactionState
                                        .toString() : "null")
                                + ", OR: "
                                + (origRequestMethod != null ? origRequestMethod
                                        : "null");
                        auditReport += "    " + transactionReport + "\n";

                        // Kill it
                        removeTransaction(sipTransaction);
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                            logger.logDebug("auditTransactions: leaked "
                                    + transactionReport);
                    }
                }
            }
        }

        // Return final report
        if (leakedTransactions > 0) {
            auditReport += "    Total: " + Integer.toString(leakedTransactions)
                    + " leaked transactions detected and removed.\n";
        } else {
            auditReport = null;
        }
        return auditReport;
    }

    public void setNon2XXAckPassedToListener(boolean passToListener) {
        this.non2XXAckPassedToListener = passToListener;
    }

    /**
     * @return the non2XXAckPassedToListener
     */
    public boolean isNon2XXAckPassedToListener() {
        return non2XXAckPassedToListener;
    }

    /**
     * Get the count of client transactions that is not in the completed or
     * terminated state.
     *
     * @return the activeClientTransactionCount
     */
    public int getActiveClientTransactionCount() {
        return activeClientTransactionCount.get();
    }

    public boolean isRfc2543Supported() {

        return this.rfc2543Supported;
    }

    public boolean isCancelClientTransactionChecked() {
        return this.cancelClientTransactionChecked;
    }

    public boolean isRemoteTagReassignmentAllowed() {
        return this.remoteTagReassignmentAllowed;
    }

    /**
     * This method is slated for addition to the next spec revision.
     *
     *
     * @return -- the collection of dialogs that is being managed by the stack.
     */
    public Collection<Dialog> getDialogs() {
        HashSet<Dialog> dialogs = new HashSet<Dialog>();
        dialogs.addAll(this.dialogTable.values());
        dialogs.addAll(this.earlyDialogTable.values());
        return dialogs;
    }

    /**
     *
     * @return -- the collection of dialogs matching the state that is being
     *         managed by the stack.
     */
    public Collection<Dialog> getDialogs(DialogState state) {
        HashSet<Dialog> matchingDialogs = new HashSet<Dialog>();
        if (DialogState.EARLY.equals(state)) {
            matchingDialogs.addAll(this.earlyDialogTable.values());
        } else {
            Collection<SIPDialog> dialogs = dialogTable.values();
            for (SIPDialog dialog : dialogs) {
                if (dialog.getState() != null
                        && dialog.getState().equals(state)) {
                    matchingDialogs.add(dialog);
                }
            }
        }
        return matchingDialogs;
    }

    /**
     * Get the Replaced Dialog from the stack.
     *
     * @param replacesHeader
     *            -- the header that references the dialog being replaced.
     */
    public Dialog getReplacesDialog(ReplacesHeader replacesHeader) {
        String cid = replacesHeader.getCallId();
        String fromTag = replacesHeader.getFromTag();
        String toTag = replacesHeader.getToTag();
        
        for ( SIPDialog dialog : this.dialogTable.values() ) {
            if ( dialog.getCallId().getCallId().equals(cid) 
                    && fromTag.equalsIgnoreCase(dialog.lastResponseFromTag) 
                    && toTag.equalsIgnoreCase(dialog.lastResponseToTag)) {
                return dialog;
            }
        }

        StringBuilder dialogId = new StringBuilder(cid);

        // retval.append(COLON).append(to.getUserAtHostPort());
        if (toTag != null) {
            dialogId.append(":");
            dialogId.append(toTag);
        }
        // retval.append(COLON).append(from.getUserAtHostPort());
        if (fromTag != null) {
            dialogId.append(":");
            dialogId.append(fromTag);
        }
        String did = dialogId.toString().toLowerCase();
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("Looking for dialog " + did);
        /*
         * Check if we can find this dialog in our dialog table.
         */
        Dialog replacesDialog =  this.dialogTable.get(did);
        /*
         * This could be a forked dialog. Search for it.
         */
        if ( replacesDialog == null ) {
            for (SIPClientTransaction ctx : this.clientTransactionTable
                    .values()) {
               if ( ctx.getDialog(did) != null ) {
                   replacesDialog = ctx.getDialog(did);
                   break;
               }
           }
        }

        return replacesDialog;
    }

    /**
     * Get the Join Dialog from the stack.
     *
     * @param joinHeader
     *            -- the header that references the dialog being joined.
     */
    public Dialog getJoinDialog(JoinHeader joinHeader) {
        String cid = joinHeader.getCallId();
        String fromTag = joinHeader.getFromTag();
        String toTag = joinHeader.getToTag();

        StringBuilder retval = new StringBuilder(cid);

        // retval.append(COLON).append(to.getUserAtHostPort());
        if (toTag != null) {
            retval.append(":");
            retval.append(toTag);
        }
        // retval.append(COLON).append(from.getUserAtHostPort());
        if (fromTag != null) {
            retval.append(":");
            retval.append(fromTag);
        }
        return this.dialogTable.get(retval.toString().toLowerCase());
    }

    /**
     * @param timer
     *            the timer to set
     */
    public void setTimer(SipTimer timer) {
        this.timer = timer;
    }

    /**
     * @return the timer
     */
    public SipTimer getTimer() throws IllegalStateException {
//        if(timer == null)
//            throw new IllegalStateException("Stack has been stopped, no further tasks can be scheduled.");
        return timer;
    }


    /**
     * Size of the receive UDP buffer. This property affects performance under
     * load. Bigger buffer is better under load.
     *
     * @return
     */
    public int getReceiveUdpBufferSize() {
        return receiveUdpBufferSize;
    }

    /**
     * Size of the receive UDP buffer. This property affects performance under
     * load. Bigger buffer is better under load.
     *
     * @return
     */
    public void setReceiveUdpBufferSize(int receiveUdpBufferSize) {
        this.receiveUdpBufferSize = receiveUdpBufferSize;
    }

    /**
     * Size of the send UDP buffer. This property affects performance under
     * load. Bigger buffer is better under load.
     *
     * @return
     */
    public int getSendUdpBufferSize() {
        return sendUdpBufferSize;
    }

    /**
     * Size of the send UDP buffer. This property affects performance under
     * load. Bigger buffer is better under load.
     *
     * @return
     */
    public void setSendUdpBufferSize(int sendUdpBufferSize) {
        this.sendUdpBufferSize = sendUdpBufferSize;
    }

     /**
      * Flag that reqests checking of branch IDs on responses.
      *
      * @return
      */
     public boolean checkBranchId() {
           return this.checkBranchId;
     }

    /**
     * @param logStackTraceOnMessageSend
     *            the logStackTraceOnMessageSend to set
     */
    public void setLogStackTraceOnMessageSend(boolean logStackTraceOnMessageSend) {
        this.logStackTraceOnMessageSend = logStackTraceOnMessageSend;
    }

    /**
     * @return the logStackTraceOnMessageSend
     */
    public boolean isLogStackTraceOnMessageSend() {
        return logStackTraceOnMessageSend;
    }

    public void setDeliverDialogTerminatedEventForNullDialog() {
        this.isDialogTerminatedEventDeliveredForNullDialog = true;
    }

    public void addForkedClientTransaction(
            SIPClientTransaction clientTransaction) {
        String forkId = ((SIPRequest)clientTransaction.getRequest()).getForkId();
        clientTransaction.setForkId(forkId);
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        	logger.logStackTrace();
            logger.logDebug(
                    "Adding forked client transaction : " + clientTransaction + " branch=" + clientTransaction.getBranch() + 
                    " forkId = " + forkId + "  sipDialog = " + clientTransaction.getDefaultDialog() + 
                    " sipDialogId= " + clientTransaction.getDefaultDialog().getDialogId());
    	}

        this.forkedClientTransactionTable.put(forkId, clientTransaction);
    }

    public SIPClientTransaction getForkedTransaction(String transactionId) {
        return this.forkedClientTransactionTable.get(transactionId);
    }

    /**
     * @param deliverUnsolicitedNotify
     *            the deliverUnsolicitedNotify to set
     */
    public void setDeliverUnsolicitedNotify(boolean deliverUnsolicitedNotify) {
        this.deliverUnsolicitedNotify = deliverUnsolicitedNotify;
    }

    /**
     * @return the deliverUnsolicitedNotify
     */
    public boolean isDeliverUnsolicitedNotify() {
        return deliverUnsolicitedNotify;
    }

    /**
     * @param deliverTerminatedEventForAck
     *            the deliverTerminatedEventForAck to set
     */
    public void setDeliverTerminatedEventForAck(
            boolean deliverTerminatedEventForAck) {
        this.deliverTerminatedEventForAck = deliverTerminatedEventForAck;
    }

    /**
     * @return the deliverTerminatedEventForAck
     */
    public boolean isDeliverTerminatedEventForAck() {
        return deliverTerminatedEventForAck;
    }

    public long getMinKeepAliveInterval() {
       return this.minKeepAliveInterval;
    }

    /**
     * @param maxForkTime
     *            the maxForkTime to set
     */
    public void setMaxForkTime(int maxForkTime) {
        this.maxForkTime = maxForkTime;
    }

    /**
     * @return the maxForkTime
     */
    public int getMaxForkTime() {
        return maxForkTime;
    }

    /**
     * This is a testing interface. Normally the application does not see
     * retransmitted ACK for 200 OK retransmissions.
     *
     * @return
     */
    public boolean isDeliverRetransmittedAckToListener() {
        return this.deliverRetransmittedAckToListener;
    }


    /**
     * Get the dialog timeout counter.
     *
     * @return
     */

    public int getAckTimeoutFactor() {
        if (getSipListener() != null
                && getSipListener() instanceof SipListenerExt) {
            return dialogTimeoutFactor;
        } else {
            return 64;
        }
    }

    public abstract SipListener getSipListener();

    /**
     * Executor used to optimize the ReinviteSender Runnable in the sendRequest
     * of the SipDialog
   */
    public ExecutorService getReinviteExecutor() {
    return reinviteExecutor;
  }

    /**
     * @param messageParserFactory the messageParserFactory to set
     */
    public void setMessageParserFactory(MessageParserFactory messageParserFactory) {
        this.messageParserFactory = messageParserFactory;
    }

    /**
     * @return the messageParserFactory
     */
    public MessageParserFactory getMessageParserFactory() {
        return messageParserFactory;
    }

    /**
     * @param messageProcessorFactory the messageProcessorFactory to set
     */
    public void setMessageProcessorFactory(MessageProcessorFactory messageProcessorFactory) {
        this.messageProcessorFactory = messageProcessorFactory;
    }

    /**
     * @return the messageProcessorFactory
     */
    public MessageProcessorFactory getMessageProcessorFactory() {
        return messageProcessorFactory;
    }

    /**
     * @param aggressiveCleanup the aggressiveCleanup to set
     */
    public void setAggressiveCleanup(boolean aggressiveCleanup) {
        this.aggressiveCleanup = aggressiveCleanup;
    }

    /**
     * @return the aggressiveCleanup
     */
    public boolean isAggressiveCleanup() {
        return aggressiveCleanup;
    }



    public int getEarlyDialogTimeout() {
        return this.earlyDialogTimeout;
    }

    /**
     * @param clientAuth the clientAuth to set
     */
    public void setClientAuth(ClientAuthType clientAuth) {
        this.clientAuth = clientAuth;
    }

    /**
     * @return the clientAuth
     */
    public ClientAuthType getClientAuth() {
        return clientAuth;
    }

	/**
	 * @param threadPriority the threadPriority to set
	 */
	public void setThreadPriority(int threadPriority) {
		if(threadPriority < Thread.MIN_PRIORITY)
			throw new IllegalArgumentException("The Stack Thread Priority shouldn't be lower than Thread.MIN_PRIORITY");
		if(threadPriority > Thread.MAX_PRIORITY)
			throw new IllegalArgumentException("The Stack Thread Priority shouldn't be higher than Thread.MAX_PRIORITY");
		if(logger.isLoggingEnabled(StackLogger.TRACE_INFO)) {
			logger.logInfo("Setting Stack Thread priority to " + threadPriority);
		}
		this.threadPriority = threadPriority;
	}

	/**
	 * @return the threadPriority
	 */
	public int getThreadPriority() {
		return threadPriority;
	}

    public int getReliableConnectionKeepAliveTimeout() {
        return reliableConnectionKeepAliveTimeout;
    }

    public void setReliableConnectionKeepAliveTimeout(int reliableConnectionKeepAliveTimeout) {
//        if (reliableConnectionKeepAliveTimeout < 0){
//            throw new IllegalArgumentException("The Stack reliableConnectionKeepAliveTimeout can not be negative. reliableConnectionKeepAliveTimeoutCandidate = " + reliableConnectionKeepAliveTimeout);
//
//        } else 
        if (reliableConnectionKeepAliveTimeout == 0){

            if (logger.isLoggingEnabled(LogWriter.TRACE_INFO)) {
                logger.logInfo("Default value (840000 ms) will be used for reliableConnectionKeepAliveTimeout stack property");
            }
            reliableConnectionKeepAliveTimeout = 840000;
        }
        if (logger.isLoggingEnabled(LogWriter.TRACE_INFO)) {
            logger.logInfo("value " + reliableConnectionKeepAliveTimeout + " will be used for reliableConnectionKeepAliveTimeout stack property");
        }
        this.reliableConnectionKeepAliveTimeout = reliableConnectionKeepAliveTimeout;
    }

    private MessageProcessor findMessageProcessor(String myAddress, int myPort, String transport) {
        for (MessageProcessor processor : getMessageProcessors()) {
            if (processor.getTransport().equalsIgnoreCase(transport)
                    && processor.getSavedIpAddress().equals(myAddress)
                    && processor.getPort() == myPort) {
                return processor;
            }
        }
        return null;
    }

    /**
    * Find suitable MessageProcessor and calls it's {@link MessageProcessor#setKeepAliveTimeout(String, int, long)} method passing
    * peerAddress and peerPort as arguments.
    *
    * @param myAddress - server ip address
    * @param myPort - server port
    * @param transport - transport
    * @param peerAddress - peerAddress
    * @param peerPort - peerPort
    * @return result of invocation of {@link MessageProcessor#setKeepAliveTimeout(String, int, long)} if MessageProcessor was found
    */
    public boolean setKeepAliveTimeout(String myAddress, int myPort, String transport, String peerAddress,
                                       int peerPort, long keepAliveTimeout) {

        MessageProcessor processor = findMessageProcessor(myAddress, myPort, transport);

        if (processor == null || !(processor instanceof ConnectionOrientedMessageProcessor)) {
            return false;
        }

        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("~~~ Trying to find MessageChannel and set new KeepAliveTimeout( myAddress=" + myAddress + ", myPort=" + myPort
                    + ", transport=" + transport + ", peerAddress=" + peerAddress + ", peerPort=" + peerPort
                    + ", keepAliveTimeout=" + keepAliveTimeout + "), MessageProcessor=" + processor);
        }
        return  ((ConnectionOrientedMessageProcessor)processor).setKeepAliveTimeout(peerAddress, peerPort, keepAliveTimeout);

    }

    /**
     * Find suitable MessageProcessor and calls it's {@link MessageProcessor#closeReliableConnection(String, int)}
     * method passing peerAddress and peerPort as arguments.
     *
     * @param myAddress - server ip address
     * @param myPort - server port
     * @param transport - transport
     * @param peerAddress - peerAddress
     * @param peerPort - peerPort
     */
    public boolean closeReliableConnection(String myAddress, int myPort, String transport, String peerAddress, int peerPort) {

        MessageProcessor processor = findMessageProcessor(myAddress, myPort, transport);

        if (processor != null && processor instanceof ConnectionOrientedMessageProcessor) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("~~~ closeReliableConnection( myAddress=" + myAddress + ", myPort=" + myPort
                        + ", transport=" + transport + ", peerAddress=" + peerAddress + ", peerPort=" + peerPort
                        + "), MessageProcessor=" + processor);
            }
            return ((ConnectionOrientedMessageProcessor)processor).closeReliableConnection(peerAddress, peerPort);
        }
        return false;
    }

	/**
	 * @return the sslHandshakeTimeout
	 */
	public long getSslHandshakeTimeout() {
		return sslHandshakeTimeout;
	}

	/**
	 * @param sslHandshakeTimeout the sslHandshakeTimeout to set
	 */
	public void setSslHandshakeTimeout(long sslHandshakeTimeout) {
		this.sslHandshakeTimeout = sslHandshakeTimeout;
	}

	/**
	 * @param earlyDialogTimeout the earlyDialogTimeout to set
	 */
	public void setEarlyDialogTimeout(int earlyDialogTimeout) {
		this.earlyDialogTimeout = earlyDialogTimeout;
	}

	/**
	 * @return the maxTxLifetimeInvite
	 */
	public int getMaxTxLifetimeInvite() {
		return maxTxLifetimeInvite;
	}

	/**
	 * @param maxTxLifetimeInvite the maxTxLifetimeInvite to set
	 */
	public void setMaxTxLifetimeInvite(int maxTxLifetimeInvite) {
		this.maxTxLifetimeInvite = maxTxLifetimeInvite;
	}

	/**
	 * @return the maxTxLifetimeNonInvite
	 */
	public int getMaxTxLifetimeNonInvite() {
		return maxTxLifetimeNonInvite;
	}

	/**
	 * @param maxTxLifetimeNonInvite the maxTxLifetimeNonInvite to set
	 */
	public void setMaxTxLifetimeNonInvite(int maxTxLifetimeNonInvite) {
		this.maxTxLifetimeNonInvite = maxTxLifetimeNonInvite;
	}
	
	public boolean isSslRenegotiationEnabled() {
		return sslRenegotiationEnabled;
	}

	public void setSslRenegotiationEnabled(boolean sslRenegotiationEnabled) {
		this.sslRenegotiationEnabled = sslRenegotiationEnabled;
	}
}
