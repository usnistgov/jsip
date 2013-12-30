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
import gov.nist.core.HostPort;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.LogWriter;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.ParameterNames;
import gov.nist.javax.sip.header.RSeq;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.stack.IllegalTransactionStateException.Reason;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.ObjectInUseException;
import javax.sip.SipException;
import javax.sip.Timeout;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.address.Hop;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.RSeqHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

/*
 * Bug fixes / enhancements:Emil Ivov, Antonis Karydas, Daniel J. Martinez Manzano, Daniel, Hagai
 * Sela, Vazques-Illa, Bill Roome, Thomas Froment and Pierre De Rop, Christophe Anzille and Jeroen
 * van Bemmel, Frank Reif.
 * Carolyn Beeton ( Avaya ).
 *
 */

/**
 * Represents a server transaction. Implements the following state machines.
 *
 * <pre>
 *
 *
 *
 *                                                                      |INVITE
 *                                                                      |pass INV to TU
 *                                                   INVITE             V send 100 if TU won't in 200ms
 *                                                   send response+-----------+
 *                                                       +--------|           |--------+101-199 from TU
 *                                                       |        | Proceeding|        |send response
 *                                                       +-------&gt;|           |&lt;-------+
 *                                                                |           |          Transport Err.
 *                                                                |           |          Inform TU
 *                                                                |           |---------------&gt;+
 *                                                                +-----------+                |
 *                                                   300-699 from TU |     |2xx from TU        |
 *                                                   send response   |     |send response      |
 *                                                                   |     +------------------&gt;+
 *                                                                   |                         |
 *                                                   INVITE          V          Timer G fires  |
 *                                                   send response+-----------+ send response  |
 *                                                       +--------|           |--------+       |
 *                                                       |        | Completed |        |       |
 *                                                       +-------&gt;|           |&lt;-------+       |
 *                                                                +-----------+                |
 *                                                                   |     |                   |
 *                                                               ACK |     |                   |
 *                                                               -   |     +------------------&gt;+
 *                                                                   |        Timer H fires    |
 *                                                                   V        or Transport Err.|
 *                                                                +-----------+  Inform TU     |
 *                                                                |           |                |
 *                                                                | Confirmed |                |
 *                                                                |           |                |
 *                                                                +-----------+                |
 *                                                                      |                      |
 *                                                                      |Timer I fires         |
 *                                                                      |-                     |
 *                                                                      |                      |
 *                                                                      V                      |
 *                                                                +-----------+                |
 *                                                                |           |                |
 *                                                                | Terminated|&lt;---------------+
 *                                                                |           |
 *                                                                +-----------+
 *
 *                                                     Figure 7: INVITE server transaction
 *                                                         Request received
 *                                                                         |pass to TU
 *
 *                                                                         V
 *                                                                   +-----------+
 *                                                                   |           |
 *                                                                   | Trying    |-------------+
 *                                                                   |           |             |
 *                                                                   +-----------+             |200-699 from TU
 *                                                                         |                   |send response
 *                                                                         |1xx from TU        |
 *                                                                         |send response      |
 *                                                                         |                   |
 *                                                      Request            V      1xx from TU  |
 *                                                      send response+-----------+send response|
 *                                                          +--------|           |--------+    |
 *                                                          |        | Proceeding|        |    |
 *                                                          +-------&gt;|           |&lt;-------+    |
 *                                                   +&lt;--------------|           |             |
 *                                                   |Trnsprt Err    +-----------+             |
 *                                                   |Inform TU            |                   |
 *                                                   |                     |                   |
 *                                                   |                     |200-699 from TU    |
 *                                                   |                     |send response      |
 *                                                   |  Request            V                   |
 *                                                   |  send response+-----------+             |
 *                                                   |      +--------|           |             |
 *                                                   |      |        | Completed |&lt;------------+
 *                                                   |      +-------&gt;|           |
 *                                                   +&lt;--------------|           |
 *                                                   |Trnsprt Err    +-----------+
 *                                                   |Inform TU            |
 *                                                   |                     |Timer J fires
 *                                                   |                     |-
 *                                                   |                     |
 *                                                   |                     V
 *                                                   |               +-----------+
 *                                                   |               |           |
 *                                                   +--------------&gt;| Terminated|
 *                                                                   |           |
 *                                                                   +-----------+
 *
 *
 *
 *
 *
 * </pre>
 *
 * @version 1.2 $Revision: 1.150 $ $Date: 2010-12-02 22:04:15 $
 * @author M. Ranganathan
 *
 */
public class SIPServerTransactionImpl extends SIPTransactionImpl implements SIPServerTransaction {
    private static StackLogger logger = CommonLogger.getLogger(SIPServerTransaction.class);
    private int rseqNumber = -1;

    // private LinkedList pendingRequests;

    // Real RequestInterface to pass messages to
    private transient ServerRequestInterface requestOf;

    private SIPDialog dialog;
    // jeand needed because we nullify the dialog ref early and keep only the dialogId to save on mem and help GC
    protected String dialogId;

    // the unacknowledged SIPResponse

//    private SIPResponse pendingReliableResponse;
    // wondering if the pendingReliableResponseAsBytes could be put into the lastResponseAsBytes
    private byte[] pendingReliableResponseAsBytes;
    private String pendingReliableResponseMethod;
    private long pendingReliableCSeqNumber;
    private long pendingReliableRSeqNumber;

    // The pending reliable Response Timer
    private ProvisionalResponseTask provisionalResponseTask;

    private boolean retransmissionAlertEnabled;

    private RetransmissionAlertTimerTask retransmissionAlertTimerTask;

    protected boolean isAckSeen;

    private SIPClientTransaction pendingSubscribeTransaction;

    private SIPServerTransaction inviteTransaction;

    // Experimental.
    private static boolean interlockProvisionalResponses = true;

    private Semaphore provisionalResponseSem = new Semaphore(1);
    
    private Semaphore terminationSemaphore = new Semaphore(0);

    // jeand we nullify the last response fast to save on mem and help GC, but we keep only the information needed
    private byte[] lastResponseAsBytes;
    private String lastResponseHost;
    private int lastResponsePort;
    private String lastResponseTransport;

    private int lastResponseStatusCode;

    private HostPort originalRequestSentBy;
    private String originalRequestFromTag;

    
    /**
     * This timer task is used for alerting the application to send retransmission alerts.
     *
     *
     */
    class RetransmissionAlertTimerTask extends SIPStackTimerTask {

        String dialogId;

        int ticks;

        int ticksLeft;

        public RetransmissionAlertTimerTask(String dialogId) {

            this.ticks = SIPTransactionImpl.T1;
            this.ticksLeft = this.ticks;
            // Fix from http://java.net/jira/browse/JSIP-443
            // by mitchell.c.ackerman
            this.dialogId = dialogId;
        }

        public void runTask() {
            SIPServerTransactionImpl serverTransaction = SIPServerTransactionImpl.this;
            ticksLeft--;
            if (ticksLeft == -1) {
                serverTransaction.fireRetransmissionTimer();
                this.ticksLeft = 2 * ticks;
            }

        }

    }

    class ProvisionalResponseTask extends SIPStackTimerTask {

        int ticks;

        int ticksLeft;

        public ProvisionalResponseTask() {
            this.ticks = SIPTransactionImpl.T1;
            this.ticksLeft = this.ticks;
        }

        public void runTask() {
          SIPServerTransactionImpl serverTransaction = SIPServerTransactionImpl.this;
            /*
             * The reliable provisional response is passed to the transaction layer periodically
             * with an interval that starts at T1 seconds and doubles for each retransmission (T1
             * is defined in Section 17 of RFC 3261). Once passed to the server transaction, it is
             * added to an internal list of unacknowledged reliable provisional responses. The
             * transaction layer will forward each retransmission passed from the UAS core.
             *
             * This differs from retransmissions of 2xx responses, whose intervals cap at T2
             * seconds. This is because retransmissions of ACK are triggered on receipt of a 2xx,
             * but retransmissions of PRACK take place independently of reception of 1xx.
             */
            // If the transaction has terminated,
            if (serverTransaction.isTerminated()) {

                sipStack.getTimer().cancel(this);

            } else {
                ticksLeft--;
                if (ticksLeft == -1) {
                    serverTransaction.fireReliableResponseRetransmissionTimer();
                    this.ticksLeft = 2 * ticks;
                    this.ticks = this.ticksLeft;
                    // timer H MUST be set to fire in 64*T1 seconds for all transports. Timer H
                    // determines when the server
                    // transaction abandons retransmitting the response
                    if (this.ticksLeft >= SIPTransactionImpl.TIMER_H) {
                        sipStack.getTimer().cancel(this);
                        setState(TransactionState._TERMINATED);
                        fireTimeoutTimer();
                    }
                }

            }

        }

    }

    /**
     * This timer task is for INVITE server transactions. It will send a trying in 200 ms. if the
     * TU does not do so.
     *
     */
    class SendTrying extends SIPStackTimerTask {

        protected SendTrying() {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                logger.logDebug("scheduled timer for " + SIPServerTransactionImpl.this);

        }

        public void runTask() {
            SIPServerTransactionImpl serverTransaction = SIPServerTransactionImpl.this;

            int realState = serverTransaction.getRealState();

            if (realState < 0 || TransactionState._TRYING == realState) {
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug(" sending Trying current state = "
                            + serverTransaction.getRealState());
                try {
                    serverTransaction.sendMessage(serverTransaction.getOriginalRequest()
                            .createResponse(100, "Trying"));
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                        logger.logDebug(" trying sent "
                                + serverTransaction.getRealState());
                } catch (IOException ex) {
                    if (logger.isLoggingEnabled())
                        logger.logError("IO error sending  TRYING");
                }
            }

        }
    }

    class TransactionTimer extends SIPStackTimerTask {

        public TransactionTimer() {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("TransactionTimer() : " + getTransactionId());
            }

        }

        public void runTask() {
            // If the transaction has terminated,
            if (isTerminated()) {
                // Keep the transaction hanging around in the transaction table
                // to catch the incoming ACK -- this is needed for tcp only.
                // Note that the transaction record is actually removed in
                // the connection linger timer.
                try {
                       sipStack.getTimer().cancel(this);
                } catch (IllegalStateException ex) {
                    if (!sipStack.isAlive())
                        return;
                }


                // Oneshot timer that garbage collects the SeverTransaction
                // after a scheduled amount of time. The linger timer allows
                // the client side of the tx to use the same connection to
                // send an ACK and prevents a race condition for creation
                // of new server tx
                SIPStackTimerTask myTimer = new LingerTimer();

                sipStack.getTimer().schedule(myTimer,
                    SIPTransactionStack.CONNECTION_LINGER_TIME * 1000);
            } else {
                // Add to the fire list -- needs to be moved
                // outside the synchronized block to prevent
                // deadlock.
                fireTimer();
            }
            if(originalRequest != null) {
                originalRequest.cleanUp();
            }
        }

    }

    /**
     * Send a response.
     *
     * @param transactionResponse -- the response to send
     *
     */

    protected void sendResponse(SIPResponse transactionResponse) throws IOException {
        if ( logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("sipServerTransaction::sendResponse " + transactionResponse.getFirstLine());
        }
        try {
            // RFC18.2.2. Sending Responses
            // The server transport uses the value of the top Via header field
            // in
            // order
            // to determine where to send a response.
            // It MUST follow the following process:
            // If the "sent-protocol" is a reliable transport
            // protocol such as TCP or SCTP,
            // or TLS over those, the response MUST be
            // sent using the existing connection
            // to the source of the original request
            // that created the transaction, if that connection is still open.
            if (isReliable()) {

                getMessageChannel().sendMessage(transactionResponse);


            } else {
                Via via = transactionResponse.getTopmostVia();
                String transport = via.getTransport();
                if (transport == null)
                    throw new IOException("missing transport!");
                // @@@ hagai Symmetric NAT support
                int port = via.getRPort();
                if (port == -1)
                    port = via.getPort();
                if (port == -1) {
                    if (transport.equalsIgnoreCase("TLS"))
                        port = 5061;
                    else
                        port = 5060;
                }

                // Otherwise, if the Via header field value contains a
                // "maddr" parameter, the response MUST be forwarded to
                // the address listed there, using the port indicated in
                // "sent-by",
                // or port 5060 if none is present. If the address is a
                // multicast
                // address, the response SHOULD be sent using
                // the TTL indicated in the "ttl" parameter, or with a
                // TTL of 1 if that parameter is not present.
                String host = null;
                if (via.getMAddr() != null) {
                    host = via.getMAddr();
                } else {
                    // Otherwise (for unreliable unicast transports),
                    // if the top Via has a "received" parameter, the response
                    // MUST
                    // be sent to the
                    // address in the "received" parameter, using the port
                    // indicated
                    // in the
                    // "sent-by" value, or using port 5060 if none is specified
                    // explicitly.
                    host = via.getParameter(Via.RECEIVED);
                    if (host == null) {
                        // Otherwise, if it is not receiver-tagged, the response
                        // MUST be
                        // sent to the address indicated by the "sent-by" value,
                        // using the procedures in Section 5
                        // RFC 3263 PROCEDURE TO BE DONE HERE
                        host = via.getHost();
                    }
                }

                Hop hop = sipStack.addressResolver.resolveAddress(new HopImpl(host, port,
                        transport));

                MessageChannel messageChannel = ((SIPTransactionStack) getSIPStack())
                        .createRawMessageChannel(this.getSipProvider().getListeningPoint(
                                hop.getTransport()).getIPAddress(), this.getPort(), hop);
                if (messageChannel != null) {
                    messageChannel.sendMessage(transactionResponse);
                    lastResponseHost = host;
                    lastResponsePort = port;
                    lastResponseTransport = transport;
                } else {
                    throw new IOException("Could not create a message channel for " + hop + " with source IP:Port "+
                            this.getSipProvider().getListeningPoint(
                                    hop.getTransport()).getIPAddress() + ":" + this.getPort());
                }

            }
            lastResponseAsBytes = transactionResponse.encodeAsBytes(this.getTransport());
            lastResponse = null;
        } finally {
            this.startTransactionTimer();
        }
    }

    /**
     * Creates a new server transaction.
     *
     * @param sipStack Transaction stack this transaction belongs to.
     * @param newChannelToUse Channel to encapsulate.
     */
    protected SIPServerTransactionImpl(SIPTransactionStack sipStack, MessageChannel newChannelToUse) {

        super(sipStack, newChannelToUse);
        
        // Only one outstanding request for a given server tx.

        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("Creating Server Transaction" + this.getBranchId());
            logger.logStackTrace();
        }

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#setRequestInterface(gov.nist.javax.sip.stack.ServerRequestInterface)
     */
    @Override
    public void setRequestInterface(ServerRequestInterface newRequestOf) {

        requestOf = newRequestOf;

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getResponseChannel()
     */
    @Override
    public MessageChannel getResponseChannel() {

        return encapsulatedChannel;

    }



    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#isMessagePartOfTransaction(gov.nist.javax.sip.message.SIPMessage)
     */
    @Override
    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {

        // List of Via headers in the message to test
//        ViaList viaHeaders;

        // Flags whether the select message is part of this transaction
        boolean transactionMatches = false;
        final String method = messageToTest.getCSeq().getMethod();
        SIPRequest origRequest = getOriginalRequest();
        // Invite Server transactions linger in the terminated state in the
        // transaction
        // table and are matched to compensate for
        // http://bugs.sipit.net/show_bug.cgi?id=769
        if (isInviteTransaction() || !isTerminated()) {

            // Get the topmost Via header and its branch parameter
            final Via topViaHeader = messageToTest.getTopmostVia();
            if (topViaHeader != null) {

//                topViaHeader = (Via) viaHeaders.getFirst();
                 // Branch code in the topmost Via header
                String messageBranch = topViaHeader.getBranch();
                if (messageBranch != null) {

                    // If the branch parameter exists but
                    // does not start with the magic cookie,
                    if (!messageBranch.toLowerCase().startsWith(
                            SIPConstants.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {

                        // Flags this as old
                        // (RFC2543-compatible) client
                        // version
                        messageBranch = null;

                    }

                }

                // If a new branch parameter exists,
                if (messageBranch != null && this.getBranch() != null) {
                    if (method.equals(Request.CANCEL)) {
                        // Cancel is handled as a special case because it
                        // shares the same same branch id of the invite
                        // that it is trying to cancel.
                        transactionMatches = this.getMethod().equals(Request.CANCEL)
                                && getBranch().equalsIgnoreCase(messageBranch)
                                && topViaHeader.getSentBy().equals(
                                         origRequest.getTopmostVia()
                                                .getSentBy());

                    } else {
                        // Matching server side transaction with only the
                        // branch parameter.
                        if(origRequest != null) {
                            transactionMatches = getBranch().equalsIgnoreCase(messageBranch)
                                && topViaHeader.getSentBy().equals(
                                          origRequest.getTopmostVia()
                                                .getSentBy());
                        } else {
                            transactionMatches = getBranch().equalsIgnoreCase(messageBranch)
                                && topViaHeader.getSentBy().equals(originalRequestSentBy);
                        }

                    }

                } else {
                    // force the reparsing only on non RFC 3261 messages
                    origRequest = (SIPRequest) getRequest();

                    // This is an RFC2543-compliant message; this code is here
                    // for backwards compatibility.
                    // It is a weak check.
                    // If RequestURI, To tag, From tag, CallID, CSeq number, and
                    // top Via headers are the same, the
                    // SIPMessage matches this transaction. An exception is for
                    // a CANCEL request, which is not deemed
                    // to be part of an otherwise-matching INVITE transaction.
                    String originalFromTag = origRequest.getFromTag();

                    String thisFromTag = messageToTest.getFrom().getTag();

                    boolean skipFrom = (originalFromTag == null || thisFromTag == null);

                    String originalToTag = origRequest.getToTag();

                    String thisToTag = messageToTest.getTo().getTag();

                    boolean skipTo = (originalToTag == null || thisToTag == null);
                    boolean isResponse = (messageToTest instanceof SIPResponse);
                    // Issue #96: special case handling for a CANCEL request -
                    // the CSeq method of the original request must
                    // be CANCEL for it to have a chance at matching.
                    if (messageToTest.getCSeq().getMethod().equalsIgnoreCase(Request.CANCEL)
                            && !origRequest.getCSeq().getMethod().equalsIgnoreCase(
                                    Request.CANCEL)) {
                        transactionMatches = false;
                    } else if ((isResponse || origRequest.getRequestURI().equals(
                            ((SIPRequest) messageToTest).getRequestURI()))
                            && (skipFrom || originalFromTag != null && originalFromTag.equalsIgnoreCase(thisFromTag))
                            && (skipTo || originalToTag != null && originalToTag.equalsIgnoreCase(thisToTag))
                            && origRequest.getCallId().getCallId().equalsIgnoreCase(
                                    messageToTest.getCallId().getCallId())
                            && origRequest.getCSeq().getSeqNumber() == messageToTest
                                    .getCSeq().getSeqNumber()
                            && ((!messageToTest.getCSeq().getMethod().equals(Request.CANCEL)) ||
                                    getMethod().equals(messageToTest.getCSeq().getMethod()))
                            && topViaHeader.equals(origRequest.getTopmostVia())) {

                        transactionMatches = true;
                    }

                }

            }

        }
        return transactionMatches;

    }


    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#map()
     */
    @Override
    public void map() {
        // note that TRYING is a pseudo-state for invite transactions

        int realState = getRealState();

        if (realState < 0 || realState == TransactionState._TRYING) {
            // Also sent by intermediate proxies.
            // null check added as the stack may be stopped. TRYING is not sent by reliable transports.
            if (isInviteTransaction() && !this.isMapped && sipStack.getTimer() != null ) {
                this.isMapped = true;
                // Schedule a timer to fire in 200 ms if the
                // TU did not send a trying in that time.
                sipStack.getTimer().schedule(new SendTrying(), 200);

            } else {
                isMapped = true;
            }
        }

        // Pull it out of the pending transactions list.
        sipStack.removePendingTransaction(this);
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#processRequest(gov.nist.javax.sip.message.SIPRequest, gov.nist.javax.sip.stack.MessageChannel)
     */
    @Override
    public void processRequest(SIPRequest transactionRequest, MessageChannel sourceChannel) {
        boolean toTu = false;

        // Can only process a single request directed to the
        // transaction at a time. For a given server transaction
        // the listener sees only one event at a time.

        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("processRequest: " + transactionRequest.getFirstLine());
            logger.logDebug("tx state = " + this.getRealState());
        }

        try {

            // If this is the first request for this transaction,
            if (getRealState() < 0) {
                // Save this request as the one this
                // transaction is handling
                setOriginalRequest(transactionRequest);
                this.setState(TransactionState._TRYING);
                toTu = true;
                this.setPassToListener();

                // Rsends the TRYING on retransmission of the request.
                if (isInviteTransaction() && this.isMapped) {
                    // JvB: also
                    // proxies need
                    // to do this

                    // Has side-effect of setting
                    // state to "Proceeding"
                    sendMessage(transactionRequest.createResponse(100, "Trying"));

                }
                // If an invite transaction is ACK'ed while in
                // the completed state,
            } else if (isInviteTransaction() && TransactionState._COMPLETED == getRealState()
                    && transactionRequest.getMethod().equals(Request.ACK)) {

                // @jvB bug fix
                this.setState(TransactionState._CONFIRMED);
                disableRetransmissionTimer();
                if (!isReliable()) {
                    enableTimeoutTimer(timerI);

                } else {

                    this.setState(TransactionState._TERMINATED);

                }


                // JvB: For the purpose of testing a TI, added a property to
                // pass it anyway
                if (sipStack.isNon2XXAckPassedToListener()) {
                    // This is useful for test applications that want to see
                    // all messages.
                    requestOf.processRequest(transactionRequest, encapsulatedChannel);
                } else {
                    // According to RFC3261 Application should not Ack in
                    // CONFIRMED state
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger.logDebug("ACK received for server Tx "
                                + this.getTransactionId() + " not delivering to application!");

                    }

                    this.semRelease();
                }
                return;

                // If we receive a retransmission of the original
                // request,
            } else if (transactionRequest.getMethod().equals(getMethod())) {

                if (TransactionState._PROCEEDING == getRealState()
                        || TransactionState._COMPLETED == getRealState()) {
                    this.semRelease();
                    // Resend the last response to
                    // the client
                    // Send the message to the client
                    resendLastResponseAsBytes();
                } else if (transactionRequest.getMethod().equals(Request.ACK)) {
                    // This is passed up to the TU to suppress
                    // retransmission of OK
                    if (requestOf != null)
                        requestOf.processRequest(transactionRequest, encapsulatedChannel);
                    else
                        this.semRelease();
		} else {
			// none of the above? well release the lock anyhow!
			this.semRelease();
                }
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug("completed processing retransmitted request : "
                        + transactionRequest.getFirstLine() + this + " txState = "
                        + this.getState() + " lastResponse = " + this.lastResponseAsBytes);
                return;

            }

            // Pass message to the TU
            if (TransactionState._COMPLETED != getRealState()
                    && TransactionState._TERMINATED != getRealState() && requestOf != null) {
                if (getMethod().equals(transactionRequest.getMethod())) {
                    // Only send original request to TU once!
                    if (toTu) {
                        requestOf.processRequest(transactionRequest, encapsulatedChannel);
                    } else
                        this.semRelease();
                } else {
                    if (requestOf != null)
                        requestOf.processRequest(transactionRequest, encapsulatedChannel);
                    else
                        this.semRelease();
                }
            } else {
                // This seems like a common bug so I am allowing it through!
                if (SIPTransactionStack.isDialogCreated(getMethod())
                        && getRealState() == TransactionState._TERMINATED
                        && transactionRequest.getMethod().equals(Request.ACK)
                        && requestOf != null) {
                    SIPDialog thisDialog = (SIPDialog) getDialog();

                    if (thisDialog == null || !thisDialog.ackProcessed) {
                        // Filter out duplicate acks
                        if (thisDialog != null) {
                            thisDialog.ackReceived(transactionRequest.getCSeq().getSeqNumber());
                            thisDialog.ackProcessed = true;
                        }
                        requestOf.processRequest(transactionRequest, encapsulatedChannel);
                    } else {
                        this.semRelease();
                    }

                } else if (transactionRequest.getMethod().equals(Request.CANCEL)) {
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                        logger.logDebug("Too late to cancel Transaction");
                    this.semRelease();
                    // send OK and just ignore the CANCEL.
                    try {
                        this.sendMessage(transactionRequest.createResponse(Response.OK));
                    } catch (IOException ex) {
                        // Transaction is already terminated
                        // just ignore the IOException.
                    }
		} else {
			// none of the above? well release the lock anyhow!
			 this.semRelease();
                }
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug("Dropping request " + getRealState());
            }

        } catch (IOException e) {
            if (logger.isLoggingEnabled())
                logger.logError("IOException " ,e);
            this.semRelease();
            this.raiseIOExceptionEvent();
        }

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#sendMessage(gov.nist.javax.sip.message.SIPMessage)
     */
    @Override
    public void sendMessage(SIPMessage messageToSend) throws IOException {
        if ( logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("sipServerTransaction::sendMessage " + messageToSend.getFirstLine());
        }
        // Message typecast as a response
        final SIPResponse  transactionResponse = (SIPResponse) messageToSend;
        // Status code of the response being sent to the client
        final int statusCode = transactionResponse.getStatusCode();
        try {

            try {
                // Provided we have set the banch id for this we set the BID for
                // the
                // outgoing via.
                if (originalRequestBranch != null)
                    transactionResponse.getTopmostVia().setBranch(this.getBranch());
                else
                    transactionResponse.getTopmostVia().removeParameter(ParameterNames.BRANCH);

                // Make the topmost via headers match identically for the
                // transaction rsponse.
                if (!originalRequestHasPort)
                    transactionResponse.getTopmostVia().removePort();
            } catch (ParseException ex) {
               logger.logError("UnexpectedException",ex);
               throw new IOException("Unexpected exception");
            }

            // Method of the response does not match the request used to
            // create the transaction - transaction state does not change.
            if (!transactionResponse.getCSeq().getMethod().equals(
                    getMethod())) {
                sendResponse(transactionResponse);
                return;
            }

            if(!checkStateTimers(statusCode)) {
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug("checkStateTimers returned false -- not sending message");
                }
                return;
            }

            try {
                // Send the message to the client.
                // Record the last message sent out.
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug(
                            "sendMessage : tx = " + this + " getState = " + this.getState());
                }
                lastResponse = transactionResponse;
                lastResponseStatusCode = transactionResponse.getStatusCode();

                this.sendResponse(transactionResponse);

            } catch (IOException e) {

                this.setState(TransactionState._TERMINATED);
                this.collectionTime = 0;
                throw e;

            }
        } finally {
            this.startTransactionTimer();
        }

    }


    private boolean checkStateTimers(int statusCode) {
        // If the TU sends a provisional response while in the
        // trying state,

        if (getRealState() == TransactionState._TRYING) {
            if (statusCode / 100 == 1) {
                this.setState(TransactionState._PROCEEDING);
            } else if (200 <= statusCode && statusCode <= 699) {
                // INVITE ST has TRYING as a Pseudo state
                // (See issue 76). We are using the TRYING
                // pseudo state invite Transactions
                // to signal if the application
                // has sent trying or not and hence this
                // check is necessary.
                if (!isInviteTransaction()) {
                    if (!isReliable() && getInternalState() != TransactionState._COMPLETED) {
                        // Linger in the completed state to catch
                        // retransmissions if the transport is not
                        // reliable.
                        this.setState(TransactionState._COMPLETED);
                        // Note that Timer J is only set for Unreliable
                        // transports -- see Issue 75.
                        /*
                         * From RFC 3261 Section 17.2.2 (non-invite server transaction)
                         *
                         * When the server transaction enters the "Completed" state, it MUST
                         * set Timer J to fire in 64*T1 seconds for unreliable transports, and
                         * zero seconds for reliable transports. While in the "Completed"
                         * state, the server transaction MUST pass the final response to the
                         * transport layer for retransmission whenever a retransmission of the
                         * request is received. Any other final responses passed by the TU to
                         * the server transaction MUST be discarded while in the "Completed"
                         * state. The server transaction remains in this state until Timer J
                         * fires, at which point it MUST transition to the "Terminated" state.
                         */
                        startTransactionTimerJ(TIMER_J);
                        cleanUpOnTimer();
                    } else {
                        cleanUpOnTimer();
                        this.setState(TransactionState._TERMINATED);
                        startTransactionTimerJ(0);
                    }
                } else {
                    // This is the case for INVITE server transactions.
                    // essentially, it duplicates the code in the
                    // PROCEEDING case below. There is no TRYING state for INVITE
                    // transactions in the RFC. We are using it to signal whether the
                    // application has sent a provisional response or not. Hence
                    // this is treated the same as as Proceeding.
                    if (statusCode / 100 == 2) {
                        // Status code is 2xx means that the
                        // transaction transitions to TERMINATED
                        // for both Reliable as well as unreliable
                        // transports. Note that the dialog layer
                        // takes care of retransmitting 2xx final
                        // responses.
                        /*
                         * RFC 3261 Section 13.3.1.4 Note, however, that the INVITE server
                         * transaction will be destroyed as soon as it receives this final
                         * response and passes it to the transport. Therefore, it is necessary
                         * to periodically pass the response directly to the transport until
                         * the ACK arrives. The 2xx response is passed to the transport with
                         * an interval that starts at T1 seconds and doubles for each
                         * retransmission until it reaches T2 seconds (T1 and T2 are defined
                         * in Section 17). Response retransmissions cease when an ACK request
                         * for the response is received. This is independent of whatever
                         * transport protocols are used to send the response.
                         */
                        this.disableRetransmissionTimer();
                        this.disableTimeoutTimer();
                        this.collectionTime = TIMER_J;
                        cleanUpOnTimer();
                        this.setState(TransactionState._TERMINATED);
                        if (this.getDialog() != null)
                            ((SIPDialog) this.getDialog()).setRetransmissionTicks();
                    } else {
                        // This an error final response.
                        this.setState(TransactionState._COMPLETED);
                        if (!isReliable()) {
                            /*
                             * RFC 3261
                             *
                             * While in the "Proceeding" state, if the TU passes a response
                             * with status code from 300 to 699 to the server transaction, the
                             * response MUST be passed to the transport layer for
                             * transmission, and the state machine MUST enter the "Completed"
                             * state. For unreliable transports, timer G is set to fire in T1
                             * seconds, and is not set to fire for reliable transports.
                             */

                            enableRetransmissionTimer();

                        }
                        cleanUpOnTimer();
                        enableTimeoutTimer(TIMER_H);
                    }
                }

            }

            // If the transaction is in the proceeding state,
        } else if (getRealState() == TransactionState._PROCEEDING) {

            if (isInviteTransaction()) {

                // If the response is a failure message,
                if (statusCode / 100 == 2) {
                    // Set up to catch returning ACKs
                    // The transaction lingers in the
                    // terminated state for some time
                    // to catch retransmitted INVITEs
                    this.disableRetransmissionTimer();
                    this.disableTimeoutTimer();
                    this.collectionTime = TIMER_J;
                    cleanUpOnTimer();
                    this.setState(TransactionState._TERMINATED);
                    if (this.getDialog() != null)
                        ((SIPDialog) this.getDialog()).setRetransmissionTicks();

                } else if (300 <= statusCode && statusCode <= 699) {

                    // Set up to catch returning ACKs
                    this.setState(TransactionState._COMPLETED);
                    if (!isReliable()) {
                        /*
                         * While in the "Proceeding" state, if the TU passes a response with
                         * status code from 300 to 699 to the server transaction, the response
                         * MUST be passed to the transport layer for transmission, and the
                         * state machine MUST enter the "Completed" state. For unreliable
                         * transports, timer G is set to fire in T1 seconds, and is not set to
                         * fire for reliable transports.
                         */
                        enableRetransmissionTimer();
                    }
                    cleanUpOnTimer();
                    enableTimeoutTimer(TIMER_H);
                }
                // If the transaction is not an invite transaction
                // and this is a final response,
            } else if (200 <= statusCode && statusCode <= 699) {
                // This is for Non-invite server transactions.
                // Set up to retransmit this response,
                // or terminate the transaction
                this.setState(TransactionState._COMPLETED);
                if (!isReliable()) {

                    disableRetransmissionTimer();
//                    enableTimeoutTimer(TIMER_J);
                    startTransactionTimerJ(TIMER_J);
                } else {
                    this.setState(TransactionState._TERMINATED);
                    startTransactionTimerJ(0);
                }
                cleanUpOnTimer();
            }
            // If the transaction has already completed,
        } else if (TransactionState._COMPLETED == this.getRealState()) {
            return false;
        }
        return true;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getViaHost()
     */
    @Override
    public String getViaHost() {
        return super.getViaHost();
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getViaPort()
     */
    @Override
    public int getViaPort() {
        return super.getViaPort();
    }


    /**
     * @see gov.nist.javax.sip.stack.SIPTransactionImpl#fireRetransmissionTimer()
     */
    @Override
    public void fireRetransmissionTimer() {

        try {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("fireRetransmissionTimer() -- " + this + " state " + getState());
            }
            // Resend the last response sent by this transaction
            if (isInviteTransaction() && (lastResponse != null || lastResponseAsBytes != null)) {
                // null can happen if this is terminating when the timer fires.
                if (!this.retransmissionAlertEnabled || sipStack.isTransactionPendingAck(this) ) {
                    // Retransmit last response until ack.
                    if (lastResponseStatusCode / 100 >= 2 && !this.isAckSeen) {
                        resendLastResponseAsBytes();
                    }
                } else {
                    // alert the application to retransmit the last response
                    SipProviderImpl sipProvider = (SipProviderImpl) this.getSipProvider();
                    TimeoutEvent txTimeout = new TimeoutEvent(sipProvider, this,
                            Timeout.RETRANSMIT);
                    sipProvider.handleEvent(txTimeout, this);
                }

            }
        } catch (IOException e) {
            if (logger.isLoggingEnabled())
                logger.logException(e);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);

        }

    }

    // jeand we nullify the last response very fast to save on mem and help GC but we keep it as byte array
    // so this method is used to resend the last response either as a response or byte array depending on if it has been nullified
    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#resendLastResponseAsBytes()
     */
    @Override
    public void resendLastResponseAsBytes() throws IOException {

        if(lastResponse != null) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("resend last response " + lastResponse);
            }
            sendMessage(lastResponse);
        } else if (lastResponseAsBytes != null) {
            // Send the message to the client
//                if(!checkStateTimers(lastResponseStatusCode)) {
//                    return;
//                }
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("resend last response " + new String(lastResponseAsBytes));
            }

            if(isReliable()) {
                if (logger.isLoggingEnabled(ServerLogger.TRACE_MESSAGES)) {
                    // Issue 343 : we have to log the retransmission
                    try {
                        SIPResponse lastReparsedResponse = (SIPResponse) sipStack.getMessageParserFactory().createMessageParser(sipStack).parseSIPMessage(lastResponseAsBytes, true, false, null);

                        lastReparsedResponse.setRemoteAddress(
                                        this.getPeerInetAddress());
                        lastReparsedResponse.setRemotePort(this.getPeerPort());
                        lastReparsedResponse.setLocalPort(
                                getMessageChannel().getPort());
                        lastReparsedResponse.setLocalAddress(
                                getMessageChannel()
                                    .getMessageProcessor().getIpAddress());

                        getMessageChannel().logMessage(lastReparsedResponse, this.getPeerInetAddress(), this.getPeerPort(), System.currentTimeMillis());
                    } catch (ParseException e) {
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            logger.logDebug("couldn't reparse last response " + new String(lastResponseAsBytes));
                        }
                    }
                }
                getMessageChannel().sendMessage(lastResponseAsBytes, this.getPeerInetAddress(), this.getPeerPort(), false);
            } else {
                Hop hop = sipStack.addressResolver.resolveAddress(new HopImpl(lastResponseHost, lastResponsePort,
                        lastResponseTransport));

                MessageChannel messageChannel = ((SIPTransactionStack) getSIPStack())
                        .createRawMessageChannel(this.getSipProvider().getListeningPoint(
                                hop.getTransport()).getIPAddress(), this.getPort(), hop);
                if (messageChannel != null) {
                    if (logger.isLoggingEnabled(ServerLogger.TRACE_MESSAGES)) {
                        // Issue 343 : we have to log the retransmission
                        try {
                            SIPResponse lastReparsedResponse = (SIPResponse) sipStack.getMessageParserFactory().createMessageParser(sipStack).parseSIPMessage(lastResponseAsBytes, true, false, null);

                            lastReparsedResponse.setRemoteAddress(messageChannel.getPeerInetAddress());
                            lastReparsedResponse.setRemotePort(messageChannel.getPeerPort());
                            lastReparsedResponse.setLocalPort(messageChannel.getPort());
                            lastReparsedResponse.setLocalAddress(messageChannel.getMessageProcessor().getIpAddress());

                            messageChannel.logMessage(lastReparsedResponse, messageChannel.getPeerInetAddress(), messageChannel.getPeerPort(), System.currentTimeMillis());
                        } catch (ParseException e) {
                            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                                logger.logDebug("couldn't reparse last response " + new String(lastResponseAsBytes));
                            }
                        }
                    }
                    messageChannel.sendMessage(lastResponseAsBytes, InetAddress.getByName(hop.getHost()), hop.getPort(), false);
                } else {
                    throw new IOException("Could not create a message channel for " + hop + " with source IP:Port "+
                            this.getSipProvider().getListeningPoint(
                                    hop.getTransport()).getIPAddress() + ":" + this.getPort());
                }
            }
        }
    }

    private void fireReliableResponseRetransmissionTimer() {
        try {
            resendLastResponseAsBytes();
        } catch (IOException e) {
            if (logger.isLoggingEnabled())
                logger.logException(e);
            this.setState(TransactionState._TERMINATED);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);

        }
    }


    /**
     * @see gov.nist.javax.sip.stack.SIPTransaction#fireTimeoutTimer()
     */
    public void fireTimeoutTimer() {

        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("SIPServerTransaction.fireTimeoutTimer this = " + this
                    + " current state = " + this.getRealState() + " method = "
                    + this.getMethod());

        if (isInviteTransaction() && sipStack.removeTransactionPendingAck(this) ) {
            if ( logger.isLoggingEnabled(LogWriter.TRACE_DEBUG) ) {
                logger.logDebug("Found tx pending ACK - timer H has kicked");
            }

        }
        SIPDialog dialog = (SIPDialog) getDialog();


        if (SIPTransactionStack.isDialogCreated(getMethod())
                && (TransactionState._CALLING == this.getRealState() || TransactionState._TRYING == this
                        .getRealState())) {
            dialog.setState(SIPDialog.TERMINATED_STATE);
        } else if (getMethod().equals(Request.BYE)) {
            if (dialog != null && dialog.isTerminatedOnBye())
                dialog.setState(SIPDialog.TERMINATED_STATE);
        }

        if (TransactionState._COMPLETED == this.getRealState() && isInviteTransaction()) {
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
            this.setState(TransactionState._TERMINATED);
            sipStack.removeTransaction(this);

        } else if (TransactionState._COMPLETED == this.getRealState() && !isInviteTransaction()) {
            this.setState(TransactionState._TERMINATED);
            if(!getMethod().equals(Request.CANCEL)) {
                cleanUp();
            } else {
                sipStack.removeTransaction(this);
            }

        } else if (TransactionState._CONFIRMED == this.getRealState() && isInviteTransaction()) {
            // TIMER_I should not generate a timeout
            // exception to the application when the
            // Invite transaction is in Confirmed state.
            // Just transition to Terminated state.
            this.setState(TransactionState._TERMINATED);
            sipStack.removeTransaction(this);
        } else if (!isInviteTransaction()
                && (TransactionState._COMPLETED == this.getRealState() || TransactionState._CONFIRMED == this
                        .getRealState())) {
            this.setState(TransactionState._TERMINATED);
        } else if (isInviteTransaction() && TransactionState._TERMINATED == this.getRealState()) {
            // This state could be reached when retransmitting

            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
            // TODO -- check this. This does not look right.
            if (dialog != null)
                dialog.setState(SIPDialog.TERMINATED_STATE);
        }

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getLastResponseStatusCode()
     */
    @Override
    public int getLastResponseStatusCode() {
        return this.lastResponseStatusCode;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#setOriginalRequest(gov.nist.javax.sip.message.SIPRequest)
     */
    @Override
    public void setOriginalRequest(SIPRequest originalRequest) {
        super.setOriginalRequest(originalRequest);

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.ServerTransaction#sendResponse(javax.sip.message.Response)
     */
    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#sendResponse(javax.sip.message.Response)
     */
    @Override
    public void sendResponse(Response response) throws SipException {
        SIPResponse sipResponse = (SIPResponse) response;

        SIPDialog dialog =  (SIPDialog) getDialog();
        if (response == null)
            throw new NullPointerException("null response");

        try {
            sipResponse.checkHeaders();
        } catch (ParseException ex) {
        	throw new IllegalTransactionStateException(ex.getMessage(), Reason.MissingRequiredHeader);
        }

        // check for meaningful response.
        final String responseMethod = sipResponse.getCSeq().getMethod();
        if (!responseMethod.equals(this.getMethod())) {
            throw new IllegalTransactionStateException(
                    "CSeq method does not match Request method of request that created the tx.", Reason.UnmatchingCSeq);
        }

        /*
         * 200-class responses to SUBSCRIBE requests also MUST contain an "Expires" header. The
         * period of time in the response MAY be shorter but MUST NOT be longer than specified in
         * the request.
         */
        final int statusCode = response.getStatusCode();
        if (this.getMethod().equals(Request.SUBSCRIBE) && statusCode / 100 == 2) {

            if (response.getHeader(ExpiresHeader.NAME) == null) {
                throw new IllegalTransactionStateException("Expires header is mandatory in 2xx response of SUBSCRIBE", Reason.ExpiresHeaderMandatory);
            } else {
                Expires requestExpires = (Expires) this.getOriginalRequest().getExpires();
                Expires responseExpires = (Expires) response.getExpires();
                /*
                 * If no "Expires" header is present in a SUBSCRIBE request, the implied default
                 * is defined by the event package being used.
                 */
                if (requestExpires != null
                        && responseExpires.getExpires() > requestExpires.getExpires()) {
                    throw new SipException(
                            "Response Expires time exceeds request Expires time : See RFC 3265 3.1.1");
                }
            }

        }

        // Check for mandatory header.
        if (statusCode == 200
                && responseMethod.equals(Request.INVITE)
                && sipResponse.getHeader(ContactHeader.NAME) == null)
            throw new IllegalTransactionStateException("Contact Header is mandatory for the OK to the INVITE", Reason.ContactHeaderMandatory);

        if (!this.isMessagePartOfTransaction((SIPMessage) response)) {
            throw new SipException("Response does not belong to this transaction.");
        }

        // Fix up the response if the dialog has already been established.
        try {
            /*
             * The UAS MAY send a final response to the initial request before
             * having received PRACKs for all unacknowledged reliable provisional responses,
             * unless the final response is 2xx and any of the unacknowledged reliable provisional
             * responses contained a session description. In that case, it MUST NOT send a final
             * response until those provisional responses are acknowledged.
             */
            final ContentTypeHeader contentTypeHeader = ((SIPResponse)response).getContentTypeHeader();
            if (this.pendingReliableResponseAsBytes != null
                    && this.getDialog() != null
                    && this.getInternalState() != TransactionState._TERMINATED
                    && statusCode / 100 == 2
                    && contentTypeHeader != null
                    && contentTypeHeader.getContentType()
                            .equalsIgnoreCase(CONTENT_TYPE_APPLICATION)
                    && contentTypeHeader.getContentSubType()
                            .equalsIgnoreCase(CONTENT_SUBTYPE_SDP)) {
                if (!interlockProvisionalResponses ) {
                    throw new SipException("cannot send response -- unacked provisional");
                } else {
                    try {
                       boolean acquired = this.provisionalResponseSem.tryAcquire(1,TimeUnit.SECONDS);
                       if (!acquired ) {
                           throw new SipException("cannot send response -- unacked provisional");
                       }
                    } catch (InterruptedException ex) {
                        logger.logError ("Interrupted acuqiring PRACK sem");
                        throw new SipException("Cannot aquire PRACK sem");
                    }

                }
            } else {
                // Sending the final response cancels the
                // pending response task.
                if (this.pendingReliableResponseAsBytes != null && sipResponse.isFinalResponse()) {
                    sipStack.getTimer().cancel(provisionalResponseTask);
                    this.provisionalResponseTask = null;
                }
            }

            // Dialog checks. These make sure that the response
            // being sent makes sense.
            if (dialog != null) {
                if (statusCode / 100 == 2
                        && SIPTransactionStack.isDialogCreated(responseMethod)) {
                    if (dialog.getLocalTag() == null && sipResponse.getToTag() == null) {
                        // Trying to send final response and user forgot to set
                        // to
                        // tag on the response -- be nice and assign the tag for
                        // the user.
                        sipResponse.getTo().setTag(Utils.getInstance().generateTag());
                    } else if (dialog.getLocalTag() != null && sipResponse.getToTag() == null) {
                        if ( logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            logger.logDebug("assigning toTag : serverTransaction = " + this + " dialog "
                                    + dialog + " tag = " + dialog.getLocalTag());
                        }
                        sipResponse.setToTag(dialog.getLocalTag());
                    } else if (dialog.getLocalTag() != null && sipResponse.getToTag() != null
                            && !dialog.getLocalTag().equals(sipResponse.getToTag())) {
                        throw new SipException("Tag mismatch dialogTag is "
                                + dialog.getLocalTag() + " responseTag is "
                                + sipResponse.getToTag());
                    }
                }

                if (!sipResponse.getCallId().getCallId().equals(dialog.getCallId().getCallId())) {
                    throw new SipException("Dialog mismatch!");
                }
            }



            // Backward compatibility slippery slope....
            // Only set the from tag in the response when the
            // incoming request has a from tag.
            String fromTag = originalRequestFromTag;
            if(getRequest() != null) {
                fromTag = ((SIPRequest) this.getRequest()).getFromTag();
            }
            if (fromTag != null && sipResponse.getFromTag() != null
                    && !sipResponse.getFromTag().equals(fromTag)) {
                throw new SipException("From tag of request does not match response from tag");
            } else if (fromTag != null) {
                sipResponse.getFrom().setTag(fromTag);
            } else {
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    logger.logDebug("WARNING -- Null From tag in request!!");
            }
            // See if the dialog needs to be inserted into the dialog table
            // or if the state of the dialog needs to be changed.
            if (dialog != null && statusCode != 100) {
                dialog.setResponseTags(sipResponse);
                DialogState oldState = dialog.getState();
                dialog.setLastResponse(this, (SIPResponse) response);
                if (oldState == null && dialog.getState() == DialogState.TERMINATED) {
                    DialogTerminatedEvent event = new DialogTerminatedEvent(dialog
                            .getSipProvider(), dialog);

                    // Provide notification to the listener that the dialog has
                    // ended.
                    dialog.getSipProvider().handleEvent(event, this);

                }

            } else if (dialog == null && isInviteTransaction()
                    && this.retransmissionAlertEnabled
                    && this.retransmissionAlertTimerTask == null
                    && response.getStatusCode() / 100 == 2) {
                String dialogId = ((SIPResponse) response).getDialogId(true);

                this.retransmissionAlertTimerTask = new RetransmissionAlertTimerTask(dialogId);
                sipStack.retransmissionAlertTransactions.put(dialogId, this);
                sipStack.getTimer().scheduleWithFixedDelay(this.retransmissionAlertTimerTask, 0,
                        SIPTransactionStack.BASE_TIMER_INTERVAL);

            }

            // Send message after possibly inserting the Dialog
            // into the dialog table to avoid a possible race condition.


            this.sendMessage((SIPResponse) response);



            if ( dialog != null ) {
                dialog.startRetransmitTimer(this, (SIPResponse)response);
            }

        } catch (IOException ex) {
            if (logger.isLoggingEnabled())
                logger.logException(ex);
            this.setState(TransactionState._TERMINATED);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
            throw new SipException(ex.getMessage(), ex);
        } catch (java.text.ParseException ex1) {
            if (logger.isLoggingEnabled())
                logger.logException(ex1);
            this.setState(TransactionState._TERMINATED);
            throw new SipException(ex1.getMessage(), ex1);
        }
    }

    /**
     * Return the book-keeping information that we actually use.
     */
    protected int getRealState() {
        return super.getInternalState();
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getState()
     */
    @Override
    public TransactionState getState() {
        // Trying is a pseudo state for INVITE transactions.
        if (this.isInviteTransaction() && TransactionState._TRYING == super.getInternalState())
            return TransactionState.PROCEEDING;
        else
            return super.getState();
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#setState(int)
     */
    @Override
    public void setState(int newState) {
        // Set this timer for connection caching
        // of incoming connections.
        if (newState == TransactionState._TERMINATED && this.isReliable()
                && (!getSIPStack().cacheServerConnections)) {
            // Set a time after which the connection
            // is closed.
            this.collectionTime = TIMER_J;
            this.terminationSemaphore.release();
        }

        super.setState(newState);

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPTransaction#startTransactionTimer()
     */
    @Override
    public void startTransactionTimer() {
        if(getMethod().equalsIgnoreCase(Request.INVITE) || getMethod().equalsIgnoreCase(Request.CANCEL) || getMethod().equalsIgnoreCase(Request.ACK)) {
            if (this.transactionTimerStarted.compareAndSet(false, true)) {
                if (sipStack.getTimer() != null && sipStack.getTimer().isStarted() ) {
                    // The timer is set to null when the Stack is
                    // shutting down.
                    SIPStackTimerTask myTimer = new TransactionTimer();
                    // Do not schedule when the stack is not alive.
                    if (sipStack.getTimer() != null && sipStack.getTimer().isStarted() ) {
                        sipStack.getTimer().scheduleWithFixedDelay(myTimer, baseTimerInterval, baseTimerInterval);
                    }
                    myTimer = null;
                }
            }
        }
    }

    /**
     * Start the timer task.
     */
    protected void startTransactionTimerJ(long time) {
            if (this.transactionTimerStarted.compareAndSet(false, true)) {
                if (sipStack.getTimer() != null && sipStack.getTimer().isStarted() ) {
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger.logDebug("starting TransactionTimerJ() : " + getTransactionId() + " time " + time);
                    }
                    // The timer is set to null when the Stack is
                    // shutting down.
                    SIPStackTimerTask task = new SIPStackTimerTask () {

                        public void runTask() {
                            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                               logger.logDebug("executing TransactionTimerJ() : " + getTransactionId());
                            }
                            fireTimeoutTimer();
                            cleanUp();
                            if(originalRequest != null) {
                               originalRequest.cleanUp();
                            }
                        }
                    };
                    if(time > 0) {
                        sipStack.getTimer().schedule(task, time * T1 * baseTimerInterval);
                    } else {
                        task.runTask();
                    }
                }
            }
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if(other == null) return false;
        if (!other.getClass().equals(this.getClass())) {
            return false;
        }
        SIPServerTransaction sst = (SIPServerTransaction) other;
        return this.getBranch().equalsIgnoreCase(sst.getBranch());
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.nist.javax.sip.stack.SIPTransaction#getDialog()
     */
    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getDialog()
     */
    @Override
    public Dialog getDialog() {
        if(dialog == null && dialogId != null) {
            return sipStack.getDialog(dialogId);
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.nist.javax.sip.stack.SIPTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog,
     *      gov.nist.javax.sip.message.SIPMessage)
     */
    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog, java.lang.String)
     */
    @Override
    public void setDialog(SIPDialog sipDialog, String dialogId) {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("setDialog " + this + " dialog = " + sipDialog);
        this.dialog = sipDialog;
           this.dialogId = dialogId;
        if (dialogId != null)
            sipDialog.setAssigned();
        if (this.retransmissionAlertEnabled && this.retransmissionAlertTimerTask != null) {
            sipStack.getTimer().cancel(retransmissionAlertTimerTask);
            if (this.retransmissionAlertTimerTask.dialogId != null) {
                sipStack.retransmissionAlertTransactions
                        .remove(this.retransmissionAlertTimerTask.dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }

        this.retransmissionAlertEnabled = false;

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.Transaction#terminate()
     */
    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#terminate()
     */
    @Override
    public void terminate() throws ObjectInUseException {
        this.setState(TransactionState._TERMINATED);
        if (this.retransmissionAlertTimerTask != null) {
            sipStack.getTimer().cancel(retransmissionAlertTimerTask);
            if (retransmissionAlertTimerTask.dialogId != null) {
                this.sipStack.retransmissionAlertTransactions
                        .remove(retransmissionAlertTimerTask.dialogId);
            }
            this.retransmissionAlertTimerTask = null;

        }
        if(!transactionTimerStarted.get()) {
    		// if no transaction timer was started just remove the tx without firing a transaction terminated event
        	testAndSetTransactionTerminatedEvent();
        	sipStack.removeTransaction(this);
        }
    }

    @Override
    public void sendReliableProvisionalResponse(Response relResponse) throws SipException {

        /*
         * After the first reliable provisional response for a request has been acknowledged, the
         * UAS MAY send additional reliable provisional responses. The UAS MUST NOT send a second
         * reliable provisional response until the first is acknowledged.
         */
        if (this.pendingReliableResponseAsBytes != null) {
            throw new SipException("Unacknowledged response");

        } else {
            SIPResponse reliableResponse = (SIPResponse) relResponse;
            this.pendingReliableResponseAsBytes = reliableResponse.encodeAsBytes(this.getTransport());
            this.pendingReliableResponseMethod = reliableResponse.getCSeq().getMethod();
            this.pendingReliableCSeqNumber = reliableResponse.getCSeq().getSeqNumber();
        }
        /*
         * In addition, it MUST contain a Require header field containing the option tag 100rel,
         * and MUST include an RSeq header field.
         */
        RSeq rseq = (RSeq) relResponse.getHeader(RSeqHeader.NAME);
        if (relResponse.getHeader(RSeqHeader.NAME) == null) {
            rseq = new RSeq();
            relResponse.setHeader(rseq);
        }

        try {
            if(rseqNumber < 0) {
                this.rseqNumber = (int) (Math.random() * 1000);
            }
            this.rseqNumber++;
            rseq.setSeqNumber(this.rseqNumber);
            this.pendingReliableRSeqNumber = rseq.getSeqNumber();

            // start the timer task which will retransmit the reliable response
            // until the PRACK is received. Cannot send a second provisional.
            this.lastResponse = (SIPResponse) relResponse;
            if ( this.getDialog() != null  && interlockProvisionalResponses ) {
                boolean acquired = this.provisionalResponseSem.tryAcquire(1, TimeUnit.SECONDS);
                if (!acquired) {
                    throw new SipException("Unacknowledged reliable response");
                }
            }
            //moved the task scheduling before the sending of the message to overcome
            // Issue 265 : https://jain-sip.dev.java.net/issues/show_bug.cgi?id=265
            this.provisionalResponseTask = new ProvisionalResponseTask();
            this.sipStack.getTimer().scheduleWithFixedDelay(provisionalResponseTask, 0,
                    SIPTransactionStack.BASE_TIMER_INTERVAL);
            this.sendMessage((SIPMessage) relResponse);
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getReliableProvisionalResponse()
     */
    @Override
    public byte[] getReliableProvisionalResponse() {

        return this.pendingReliableResponseAsBytes;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#prackRecieved()
     */
    @Override
    public boolean prackRecieved() {

        if (this.pendingReliableResponseAsBytes == null)
            return false;
           if(provisionalResponseTask != null) {
               sipStack.getTimer().cancel(provisionalResponseTask);
               this.provisionalResponseTask = null;
           }

        this.pendingReliableResponseAsBytes = null;
        if ( interlockProvisionalResponses && getDialog() != null )  {
            this.provisionalResponseSem.release();
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.ServerTransaction#enableRetransmissionAlerts()
     */

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#enableRetransmissionAlerts()
     */
    @Override
    public void enableRetransmissionAlerts() throws SipException {
        if (this.getDialog() != null)
            throw new SipException("Dialog associated with tx");

        else if (!isInviteTransaction())
            throw new SipException("Request Method must be INVITE");

        this.retransmissionAlertEnabled = true;

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#isRetransmissionAlertEnabled()
     */
    @Override
    public boolean isRetransmissionAlertEnabled() {
        return this.retransmissionAlertEnabled;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#disableRetransmissionAlerts()
     */
    @Override
    public void disableRetransmissionAlerts() {
        if (this.retransmissionAlertTimerTask != null && this.retransmissionAlertEnabled) {
               sipStack.getTimer().cancel(retransmissionAlertTimerTask);
            this.retransmissionAlertEnabled = false;

            String dialogId = this.retransmissionAlertTimerTask.dialogId;
            if (dialogId != null) {
                sipStack.retransmissionAlertTransactions.remove(dialogId);
            }
            this.retransmissionAlertTimerTask = null;
        }
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#setAckSeen()
     */
    @Override
    public void setAckSeen() {
        this.isAckSeen = true;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#ackSeen()
     */
    @Override
    public boolean ackSeen() {
        return this.isAckSeen;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#setMapped(boolean)
     */
    @Override
    public void setMapped(boolean b) {
        this.isMapped = true;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#setPendingSubscribe(gov.nist.javax.sip.stack.SIPClientTransaction)
     */
    @Override
    public void setPendingSubscribe(SIPClientTransaction pendingSubscribeClientTx) {
        this.pendingSubscribeTransaction = pendingSubscribeClientTx;

    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#releaseSem()
     */
    @Override
    public void releaseSem() {
        if (this.pendingSubscribeTransaction != null) {
            /*
             * When a notify is being processed we take a lock on the subscribe to avoid racing
             * with the OK of the subscribe.
             */
            if (! sipStack.isDeliverUnsolicitedNotify()) {
                pendingSubscribeTransaction.releaseSem();
            }
        } else if (this.inviteTransaction != null && this.getMethod().equals(Request.CANCEL)) {
            /*
             * When a CANCEL is being processed we take a nested lock on the associated INVITE
             * server tx.
             */
            this.inviteTransaction.releaseSem();
        }
        super.releaseSem();
    }

    /**
     * The INVITE Server Transaction corresponding to a CANCEL Server Transaction.
     *
     * @param st -- the invite server tx corresponding to the cancel server transaction.
     */
    @Override
    public void setInviteTransaction(SIPServerTransaction st) {
        this.inviteTransaction = st;

    }

    /**
     * TODO -- this method has to be added to the api.
     *
     * @return
     */
    @Override
    public SIPServerTransaction getCanceledInviteTransaction() {
        return this.inviteTransaction;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#scheduleAckRemoval()
     */
    @Override
    public void scheduleAckRemoval() throws IllegalStateException {
        if (this.getMethod() == null || !this.getMethod().equals(Request.ACK)) {
            throw new IllegalStateException("Method is null[" + (getMethod() == null)
                    + "] or method is not ACK[" + this.getMethod() + "]");
        }

        this.startTransactionTimer();
    }

    // jeand cleanup the state of the stx to help GC
    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#cleanUp()
     */
    @Override
    public void cleanUp() {
        // Remove it from the set
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger.logDebug("removing" + this);

        if(isReleaseReferences()) {

            // release the connection associated with this transaction.
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("cleanup : "
                        + getTransactionId());
            }
            // we keep the request in a byte array to be able to recreate it
            // no matter what to keep API backward compatibility            
            if(originalRequest == null && originalRequestBytes != null) {
                try {
                    originalRequest = (SIPRequest) sipStack.getMessageParserFactory().createMessageParser(sipStack).parseSIPMessage(originalRequestBytes, true, false, null);
//                    originalRequestBytes = null;
                } catch (ParseException e) {
                    logger.logError("message " + originalRequestBytes + "could not be reparsed !");
                }
            } else if (originalRequest != null && originalRequestBytes == null) {
                originalRequestBytes = originalRequest.encodeAsBytes(this.getTransport());
            }
            // http://java.net/jira/browse/JSIP-429
            // store the merge id from the tx to avoid reparsing of request on aggressive cleanup
            super.mergeId =  ((SIPRequest)originalRequest).getMergeId();
            sipStack.removeTransaction(this);
            cleanUpOnTimer();
            // commented out because the application can hold on a ref to the tx
            // after it has been removed from the stack
            // and want to get the request or branch from it
//          originalRequestBytes = null;
//          originalRequestBranch = null;
            originalRequestFromTag = null;
            originalRequestSentBy = null;
            // it should be available in the processTxTerminatedEvent, so we can nullify it only here
            if(originalRequest != null) {
    //            originalRequestSentBy = originalRequest.getTopmostVia().getSentBy();
    //            originalRequestFromTag = originalRequest.getFromTag();
                originalRequest = null;
            }
            if(!isReliable() && inviteTransaction != null) {
                inviteTransaction = null;
            }
            // Application Data has to be cleared by the application
    //        applicationData = null;
            lastResponse = null;
            // Issue 318 : (https://jain-sip.dev.java.net/issues/show_bug.cgi?id=318)
            // Re-transmission of 200 to INVITE terminates prematurely :
            // don't nullify since the transaction may be terminated
            // but the ack not received so the 200 retransmissions should continue
//            lastResponseAsBytes = null;
        
            // don't clean up because on sending 200 OK to CANCEL otherwise we try to start the transaction timer
            // but due to timer J it has already been cleaned up
//            transactionTimerStarted = null;
        } else {
            sipStack.removeTransaction(this);
        }
        
        // Uncache the server tx 
        if ((!sipStack.cacheServerConnections) && isReliable()
                && --getMessageChannel().useCount <= 0) {
            // Close the encapsulated socket if stack is configured
            close();
        } else {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)
                    && (!sipStack.cacheServerConnections)
                    && isReliable()) {
                int useCount = getMessageChannel().useCount;
                logger.logDebug("Use Count = " + useCount);
            }
        }

    }

    // clean up the state of the stx when it goes to completed or terminated to help GC
    protected void cleanUpOnTimer() {
        if(isReleaseReferences()) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("cleanup on timer : "
                        + getTransactionId());
            }
            if(dialog != null && getMethod().equals(Request.CANCEL)) {
                // used to deal with getting the dialog on cancel tx after the 200 OK to CANCEL has been sent
                dialogId = dialog.getDialogId();
            }
            dialog = null;
            // we don't nullify the inviteTx for CANCEL since the app can get it from getCanceledInviteTransaction
            if(inviteTransaction != null && !getMethod().equals(Request.CANCEL)) {
                // we release the semaphore for Cancel processing
                inviteTransaction.releaseSem();
                inviteTransaction = null;
            }
            if(originalRequest != null) {
	            // http://java.net/jira/browse/JSIP-429
	            // store the merge id from the tx to avoid reparsing of request on aggressive cleanup
            	super.mergeId =  ((SIPRequest)originalRequest).getMergeId();
                originalRequest.setTransaction(null);
                originalRequest.setInviteTransaction(null);
                if(!getMethod().equalsIgnoreCase(Request.INVITE)) {
                    if(originalRequestSentBy == null) {
                        originalRequestSentBy = originalRequest.getTopmostVia().getSentBy();
                    }
                    if(originalRequestFromTag == null) {
                        originalRequestFromTag = originalRequest.getFromTag();
                    }
                }
                // we keep the request in a byte array to be able to recreate it
                // no matter what to keep API backward compatibility
                if(originalRequestBytes == null) {
                    originalRequestBytes = originalRequest.encodeAsBytes(this.getTransport());
                }
                if(!getMethod().equalsIgnoreCase(Request.INVITE) && !getMethod().equalsIgnoreCase(Request.CANCEL)) {
                    originalRequest = null;
                }
            }
            if(lastResponse != null) {
                lastResponseAsBytes = lastResponse.encodeAsBytes(this.getTransport());
                lastResponse = null;
            }
            pendingReliableResponseAsBytes = null;
            pendingReliableResponseMethod = null;
            pendingSubscribeTransaction = null;
            provisionalResponseSem = null;
            retransmissionAlertTimerTask = null;
            requestOf = null;
        }
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getPendingReliableResponseMethod()
     */
    @Override
    public String getPendingReliableResponseMethod() {
        return pendingReliableResponseMethod;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getPendingReliableCSeqNumber()
     */
    @Override
    public long getPendingReliableCSeqNumber() {
        return pendingReliableCSeqNumber;
    }

    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#getPendingReliableRSeqNumber()
     */
    @Override
    public long getPendingReliableRSeqNumber() {
        return pendingReliableRSeqNumber;
    }
    
    /**
     * @see gov.nist.javax.sip.stack.SIPServerTransaction#waitForTermination()
     */
    @Override
    public void waitForTermination() {
    	
    	try {
			this.terminationSemaphore.acquire();
		} catch (InterruptedException e) {
			
		}
  }

}
