package gov.nist.javax.sip.stack;

import gov.nist.core.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.SIPConstants;
import javax.sip.header.*;
import javax.sip.message.*;
import javax.sip.*;
import java.text.ParseException;
import java.io.IOException;
//ifndef SIMULATION
//
// import java.util.Timer;
//else
/*
 import sim.java.util.SimTimer;
 //endif
 */

import java.util.TimerTask;

import java.util.LinkedList;

/**
 * Represents a server transaction. Implements the following state machines.
 * 
 * <pre>
 * 
 *                                |INVITE
 *                                |pass INV to TU
 *             INVITE             V send 100 if TU won't in 200ms
 *             send response+-----------+
 *                 +--------|           |--------+101-199 from TU
 *                 |        | Proceeding|        |send response
 *                 +-------&gt;|           |&lt;-------+
 *                          |           |          Transport Err.
 *                          |           |          Inform TU
 *                          |           |---------------&gt;+
 *                          +-----------+                |
 *             300-699 from TU |     |2xx from TU        |
 *             send response   |     |send response      |
 *                             |     +------------------&gt;+
 *                             |                         |
 *             INVITE          V          Timer G fires  |
 *             send response+-----------+ send response  |
 *                 +--------|           |--------+       |
 *                 |        | Completed |        |       |
 *                 +-------&gt;|           |&lt;-------+       |
 *                          +-----------+                |
 *                             |     |                   |
 *                         ACK |     |                   |
 *                         -   |     +------------------&gt;+
 *                             |        Timer H fires    |
 *                             V        or Transport Err.|
 *                          +-----------+  Inform TU     |
 *                          |           |                |
 *                          | Confirmed |                |
 *                          |           |                |
 *                          +-----------+                |
 *                                |                      |
 *                                |Timer I fires         |
 *                                |-                     |
 *                                |                      |
 *                                V                      |
 *                          +-----------+                |
 *                          |           |                |
 *                          | Terminated|&lt;---------------+
 *                          |           |
 *                          +-----------+
 * 
 *               Figure 7: INVITE server transaction
 * 
 * 
 *    		Request received
 *                                   |pass to TU
 * 
 *                                   V
 *                             +-----------+
 *                             |           |
 *                             | Trying    |-------------+
 *                             |           |             |
 *                             +-----------+             |200-699 from TU
 *                                   |                   |send response
 *                                   |1xx from TU        |
 *                                   |send response      |
 *                                   |                   |
 *                Request            V      1xx from TU  |
 *                send response+-----------+send response|
 *                    +--------|           |--------+    |
 *                    |        | Proceeding|        |    |
 *                    +-------&gt;|           |&lt;-------+    |
 *             +&lt;--------------|           |             |
 *             |Trnsprt Err    +-----------+             |
 *             |Inform TU            |                   |
 *             |                     |                   |
 *             |                     |200-699 from TU    |
 *             |                     |send response      |
 *             |  Request            V                   |
 *             |  send response+-----------+             |
 *             |      +--------|           |             |
 *             |      |        | Completed |&lt;------------+
 *             |      +-------&gt;|           |
 *             +&lt;--------------|           |
 *             |Trnsprt Err    +-----------+
 *             |Inform TU            |
 *             |                     |Timer J fires
 *             |                     |-
 *             |                     |
 *             |                     V
 *             |               +-----------+
 *             |               |           |
 *             +--------------&gt;| Terminated|
 *                             |           |
 *                             +-----------+
 * 
 * 
 * 
 * </pre>
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.50 $ $Date: 2004-10-31 02:19:08 $
 * @author Jeff Keyser
 * @author M. Ranganathan <mranga@nist.gov>
 * @author Bug fixes by Emil Ivov, Antonis Karydas, Daniel Martinez.
 * @author Performance enhancements contributed by Thomas Froment 
 * and Pierre De Rop.  <br/>
 *<a href=" {@docRoot}/uncopyright.html">This code is in the  public domain. </a>
 * 
 *  
 */
public class SIPServerTransaction extends SIPTransaction implements
        ServerRequestInterface, javax.sip.ServerTransaction, PendingRecord {

    protected boolean toListener; // Hack alert - if this is set to true then
                                  // force the listener to see transaction

    private LinkedList pendingRequests;

    // Real RequestInterface to pass messages to
    private ServerRequestInterface requestOf;

    class PendingRequest {
        protected SIPRequest sipRequest;

        protected MessageChannel messageChannel;

        public PendingRequest(SIPRequest sipRequest,
                MessageChannel messageChannel) {
            this.sipRequest = sipRequest;
            this.messageChannel = messageChannel;
        }
    }

    class SendTrying extends TimerTask {
        private SIPServerTransaction serverTransaction;

        protected SendTrying(SIPServerTransaction st) {
            if (LogWriter.needsLogging)
                sipStack.logWriter.logMessage("scheduled timer for " + st);
            this.serverTransaction = st;
        }

        public void run() {
            if (this.serverTransaction.getRealState() == null
                    || TransactionState.TRYING == this.serverTransaction
                            .getRealState()) {
                if (LogWriter.needsLogging)
                    sipStack.logWriter
                            .logMessage(" sending Trying current state = "
                                    + this.serverTransaction.getRealState());
                try {
                    serverTransaction
                            .sendMessage(serverTransaction.getOriginalRequest()
                                    .createResponse(100, "Trying"));
                    if (LogWriter.needsLogging)
                        sipStack.logWriter.logMessage(" trying sent "
                                + this.serverTransaction.getRealState());
                } catch (IOException ex) {
                    if (LogWriter.needsLogging)
                        sipStack.logWriter
                                .logMessage("IO error sending  TRYING");
                }
            }
        }
    }

    class TransactionTimer extends TimerTask {
        SIPServerTransaction myTransaction;

        SIPTransactionStack sipStack;

        public TransactionTimer(SIPServerTransaction myTransaction) {
            this.myTransaction = myTransaction;
            this.sipStack = myTransaction.sipStack;
        }

        public void run() {
            // If the transaction has terminated,
            if (myTransaction.isTerminated()) {
                // Keep the transaction hanging around in the transaction table
                // to catch the incoming ACK retransmission.
                // Note that the transaction record is actually removed in
                // the connection linger timer.
                // Note - BUG report from Antonis Karydas
                try {
                    this.cancel();
                } catch (IllegalStateException ex) {
                    if (!sipStack.isAlive())
                        return;
                }
                myTransaction.myTimer = new LingerTimer(this.myTransaction);
                // Oneshot timer.
                sipStack.timer.schedule(myTimer,
                        SIPTransactionStack.CONNECTION_LINGER_TIME * 1000);
                //adIf this transaction has not
                //terminated,
            } else {
                // Add to the fire list -- needs to be moved
                // outside the synchronized block to prevent
                // deadlock.
                fireTimer();

            }
        }

    }

    private void sendResponse(SIPResponse transactionResponse)
            throws IOException {
        // Bug report by Shanti Kadiyala
        if (transactionResponse.getTopmostVia().getParameter(Via.RECEIVED) == null) {
            // Send the response back on the same peer as received.
            super.sendMessage(transactionResponse);
        } else {
            // Respond to the host name in the received parameter.
            Via via = transactionResponse.getTopmostVia();
            String host = via.getParameter(Via.RECEIVED);
            int port = via.getPort();
            if (port == -1)
                port = 5060;
            String transport = via.getTransport();

	    // Changed by Daniel J. Martinez Manzano <dani@dif.um.es>
	    // Original code called constructor with concatenated
	    // parameters, which didn't work for IPv6 addresses.
            HopImpl hop = new HopImpl(host, port, transport);

            MessageChannel messageChannel =
            ((SIPTransactionStack) getSIPStack()).createRawMessageChannel(
            hop);
            messageChannel.sendMessage(transactionResponse);
        }
    }

    /**
     * Creates a new server transaction.
     * 
     * @param newSIPStack
     *            Transaction stack this transaction belongs to.
     * @param newChannelToUse
     *            Channel to encapsulate.
     */
    protected SIPServerTransaction(SIPTransactionStack newSIPStack,
            MessageChannel newChannelToUse) {

        super(newSIPStack, newChannelToUse);
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("Creating Server Transaction" + this);
            sipStack.logWriter.logStackTrace();
        }
        this.pendingRequests = new LinkedList();

    }

    /**
     * Sets the real RequestInterface this transaction encapsulates.
     * 
     * @param newRequestOf
     *            RequestInterface to send messages to.
     */
    public void setRequestInterface(ServerRequestInterface newRequestOf) {

        requestOf = newRequestOf;

    }

    public String getProcessingInfo() {
        return requestOf != null ? requestOf.getProcessingInfo() : null;

    }

    /**
     * Returns this transaction.
     */
    public MessageChannel getResponseChannel() {

        return this;

    }

    /**
     * Deterines if the message is a part of this transaction.
     * 
     * @param messageToTest
     *            Message to check if it is part of this transaction.
     * 
     * @return True if the message is part of this transaction, false if not.
     */
    public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {

        // List of Via headers in the message to test
        ViaList viaHeaders;
        // Topmost Via header in the list
        Via topViaHeader;
        // Branch code in the topmost Via header
        String messageBranch;
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;

        transactionMatches = false;

        // Compensation for retransmits after OK has been dispatched
        // as suggested by Antonis Karydas. Cancel Processing is
        // special because we want to look for the invite
        if (((SIPRequest) messageToTest).getMethod().equals(Request.CANCEL)
                || ((((SIPTransactionStack) getSIPStack())
                        .isDialogCreated(((SIPRequest) messageToTest)
                                .getMethod())) || !isTerminated())) {

            // Get the topmost Via header and its branch parameter
            viaHeaders = messageToTest.getViaHeaders();
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

                    if (getBranch().equals(messageBranch)
                            && topViaHeader.getSentBy().equals(
                                    ((Via) getOriginalRequest().getViaHeaders()
                                            .getFirst()).getSentBy())) {
                        // Matching server side transaction with only the
                        // branch parameter.
                        transactionMatches = true;
                    }

                    // If this is an RFC2543-compliant message,
                } else {

                    // If RequestURI, To tag, From tag,
                    // CallID, CSeq number, and top Via
                    // headers are the same,
                    String originalFromTag = getOriginalRequest().getFrom()
                            .getTag();

                    String thisFromTag = messageToTest.getFrom().getTag();

                    boolean skipFrom = (originalFromTag == null || thisFromTag == null);

                    String originalToTag = getOriginalRequest().getTo()
                            .getTag();

                    String thisToTag = messageToTest.getTo().getTag();

                    boolean skipTo = (originalToTag == null || thisToTag == null);

                    if (getOriginalRequest().getRequestURI().equals(
                            ((SIPRequest) messageToTest).getRequestURI())
                            && (skipFrom || originalFromTag.equals(thisFromTag))
                            && (skipTo || originalToTag.equals(thisToTag))
                            && getOriginalRequest().getCallId().getCallId()
                                    .equals(
                                            messageToTest.getCallId()
                                                    .getCallId())
                            && getOriginalRequest().getCSeq()
                                    .getSequenceNumber() == messageToTest
                                    .getCSeq().getSequenceNumber()
                            && topViaHeader.equals(getOriginalRequest()
                                    .getViaHeaders().getFirst())) {

                        transactionMatches = true;
                    }

                }

            }

        }
        return transactionMatches;

    }

    /**
     * Send out a trying response (only happens when the transaction is mapped).
     * Otherwise the transaction is not known to the stack.
     */
    protected void map() {
        // note that TRYING is a pseudo-state for invite transactions

        if (getRealState() == null || getRealState() == TransactionState.TRYING) {
            if (isInviteTransaction() && !this.isMapped) {
                this.isMapped = true;
                // Schedule a timer to fire in 200 ms if the
                // TU did not send a trying in that time.

                //ifndef SIMULATION
                //
                sipStack.timer.schedule(new SendTrying(this), 200);
                //else
                /*
                 * new SimTimer().schedule( new SendTrying( this ), 200);
                 * //endif
                 */

            } else {
                isMapped = true;
            }
        }
        // Pull it out of the pending transactions list.
        sipStack.removePendingTransaction(this);
    }

    /**
     * Return true if the transaction is known to stack.
     */
    public boolean isTransactionMapped() {
        return this.isMapped;
    }

    /**
     * Process a new request message through this transaction. If necessary,
     * this message will also be passed onto the TU.
     * 
     * @param transactionRequest
     *            Request to process.
     * @param sourceChannel
     *            Channel that received this message.
     */
    public void processRequest(SIPRequest transactionRequest,
            MessageChannel sourceChannel) {
        boolean toTu = false;

        // Can only process a single request directed to the
        // transaction at a time.
        if (this.eventPending) {
            synchronized (this.pendingRequests) {
                if (this.pendingRequests.size() < 4)
                    this.pendingRequests.add(new PendingRequest(
                            transactionRequest, sourceChannel));
            }
            sipStack.putPending(this);
            return;
        }

        try {

            // If this is the first request for this transaction,
            if (getRealState() == null) {
                // Save this request as the one this
                // transaction is handling
                setOriginalRequest(transactionRequest);
                this.setState(TransactionState.TRYING);
                toTu = true;
                if (isInviteTransaction() && this.isMapped) {

                    // Has side-effect of setting
                    // state to "Proceeding"
                    sendMessage(transactionRequest
                            .createResponse(100, "Trying"));

                }
                // If an invite transaction is ACK'ed while in
                // the completed state,
            } else if (isInviteTransaction()
                    && TransactionState.COMPLETED == getRealState()
                    && transactionRequest.getMethod().equals(Request.ACK)) {

                this.setState(TransactionState.CONFIRMED);
                disableRetransmissionTimer();
                if (!isReliable()) {
                    if (this.lastResponse != null
                            && this.lastResponse.getStatusCode() == Response.REQUEST_TERMINATED) {
                        // Bug report by Antonis Karydas
                        this.setState(TransactionState.TERMINATED);
                    } else {
                        enableTimeoutTimer(TIMER_I);
                    }

                } else {

                    this.setState(TransactionState.TERMINATED);

                }
                // Application should not Ack in CONFIRMED state
                // Bug (and fix thereof) reported by Emil Ivov
                return;

                // If we receive a retransmission of the original
                // request,
            } else if (transactionRequest.getMethod().equals(
                    getOriginalRequest().getMethod())) {

                if (TransactionState.PROCEEDING == getRealState()
                        || TransactionState.COMPLETED == getRealState()) {

                    // Resend the last response to
                    // the client
                    if (lastResponse != null) {
                        try {
                            // Send the message to the client
                            super.sendMessage(lastResponse);
                        } catch (IOException e) {
                            this.setState(TransactionState.TERMINATED);
                            throw e;

                        }
                    }
                } else if (transactionRequest.getMethod().equals(Request.ACK)
                        && requestOf != null) {
                    // null check bug fix by Emil Ivov
                    // This is passed up to the TU to suppress
                    // retransmission of OK
                    requestOf.processRequest(transactionRequest, this);
                }
                return;

            }

            // Pass message to the TU
            if (TransactionState.COMPLETED != getRealState()
                    && TransactionState.TERMINATED != getRealState()
                    && requestOf != null) {
                if (getOriginalRequest().getMethod().equals(
                        transactionRequest.getMethod())) {
                    // Only send original request to TU once!
                    if (toTu)
                        requestOf.processRequest(transactionRequest, this);
                } else {
                    if (requestOf != null)
                        requestOf.processRequest(transactionRequest, this);
                }
            } else {
                // This seems like a common bug so I am allowing it through!
                if (((SIPTransactionStack) getSIPStack())
                        .isDialogCreated(getOriginalRequest().getMethod())
                        && getRealState() == TransactionState.TERMINATED
                        && transactionRequest.getMethod().equals(Request.ACK)
                        && requestOf != null) {
                    SIPDialog thisDialog = (SIPDialog) this.dialog;
                    thisDialog.ackReceived(transactionRequest);

                    if (((SIPTransactionStack) getSIPStack()).retransmissionFilter) {
                        if (!thisDialog.ackProcessed) {
                            // Filter out duplicate acks if retransmission
                            // filter
                            // is enabled.
                            thisDialog.ackProcessed = true;
                            requestOf.processRequest(transactionRequest, this);
                        }
                    } else {
                        // Duplicate ACKs are seen by the application.
                        if (requestOf != null)
                            requestOf.processRequest(transactionRequest, this);
                    }
                } else if (transactionRequest.getMethod()
                        .equals(Request.CANCEL)) {
                    if (LogWriter.needsLogging)
                        sipStack.logWriter
                                .logMessage("Too late to cancel Transaction");
                    // send OK and just ignore the CANCEL.
                    try {
                        this.sendMessage(transactionRequest
                                .createResponse(Response.OK));
                    } catch (IOException ex) {
                        // Transaction is already terminated
                        // just ignore the IOException.
                    }
                }
                sipStack.logWriter.logMessage("Dropping request "
                        + getRealState());
            }

        } catch (IOException e) {
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
        }

    }

    /**
     * Send a response message through this transactionand onto the client. The
     * response drives the state machine.
     * 
     * @param messageToSend
     *            Response to process and send.
     */
    public void sendMessage(SIPMessage messageToSend) throws IOException {

        // Message typecast as a response
        SIPResponse transactionResponse;
        // Status code of the response being sent to the client
        int statusCode;

        // Get the status code from the response
        transactionResponse = (SIPResponse) messageToSend;
        statusCode = transactionResponse.getStatusCode();

        SIPDialog dialog = this.dialog;

        try {
            // Provided we have set the banch id for this we set the BID for the
            // outgoing via.
            if (this.getOriginalRequest().getTopmostVia().getBranch() != null)
                transactionResponse.getTopmostVia().setBranch(this.getBranch());
            else
                transactionResponse.getTopmostVia().removeParameter(
                        ParameterNames.BRANCH);

            // Make the topmost via headers match identically for the
            // transaction
            // rsponse.
            if (!this.getOriginalRequest().getTopmostVia().hasPort())
                transactionResponse.getTopmostVia().removePort();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        // Method of the response does not match the request used to
        // create the transaction - transaction state does not change.
        if (!transactionResponse.getCSeq().getMethod().equals(
                getOriginalRequest().getMethod())) {
            sendResponse(transactionResponse);
            return;
        }

        // Dialog state machine state adjustment.
        
        if (this.dialog != null) {
            if (this.dialog.getRemoteTag() == null
                    && transactionResponse.getTo().getTag() != null
                    && ((SIPTransactionStack) this.getSIPStack())
                            .isDialogCreated(transactionResponse.getCSeq()
                                    .getMethod())) {
                this.dialog.setRemoteTag(transactionResponse.getTo().getTag());
                ((SIPTransactionStack) this.getSIPStack())
                        .putDialog(this.dialog);
                if (statusCode / 100 == 1)
                    this.dialog.setState(SIPDialog.EARLY_STATE);
            } 
            
            if (((SIPTransactionStack) this.getSIPStack())
                    .isDialogCreated(transactionResponse.getCSeq().getMethod())
                    && transactionResponse.getCSeq().getMethod().equals(
                            getOriginalRequest().getMethod())) {
                if (statusCode / 100 == 2) {
                    // The state changes when the ACK is received for an invite
                    // transaction
                    // For other dialogs, the state changes when you send out
                    // the response.
                    if (!this.isInviteTransaction()) {
                        this.dialog.setState(SIPDialog.CONFIRMED_STATE);
                    } else {
                        if (this.dialog.getState() == null)
                            this.dialog.setState(SIPDialog.EARLY_STATE);
                    }
                } else if (statusCode >= 300 && statusCode <= 699) {
                    this.dialog.setState(SIPDialog.TERMINATED_STATE);
                }
            } else if (transactionResponse.getCSeq().getMethod().equals(
                    Request.BYE)
                    && statusCode / 100 == 2 && dialog != null) {
                // Dialog will be terminated when the transction is terminated.
                if (!isReliable())
                    this.dialog.setState(SIPDialog.COMPLETED_STATE);
                else
                    this.dialog.setState(SIPDialog.TERMINATED_STATE);
            }
        }

        // If the TU sends a provisional response while in the
        // trying state,
        if (getRealState() == TransactionState.TRYING) {
            if (statusCode / 100 == 1) {
                this.setState(TransactionState.PROCEEDING);
            } else if (200 <= statusCode && statusCode <= 699) {
                // Check -- bug report from christophe
                if (!isInviteTransaction()) {
                    this.setState(TransactionState.COMPLETED);
                } else {
                    if (statusCode / 100 == 2) {
                        this.setState(TransactionState.TERMINATED);
                    } else
                        this.setState(TransactionState.COMPLETED);
                }
                if (!isReliable()) {

                    enableRetransmissionTimer();

                }
                enableTimeoutTimer(TIMER_J);
            }

            // If the transaction is in the proceeding state,
        } else if (getRealState() == TransactionState.PROCEEDING) {

            if (isInviteTransaction()) {

                // If the response is a failure message,
                if (statusCode / 100 == 2) {
                    // Set up to catch returning ACKs

                    // Antonis Karydas: Suggestion
                    // Recall that the CANCEL's response will go
                    // through this transaction
                    // and this may well be it. Do NOT change the
                    // transaction state if this
                    // is a response for a CANCEL.
                    // Wait, instead for the 487 from TU.
                    if (!transactionResponse.getCSeq().getMethod().equals(
                            Request.CANCEL)) {

                        this.collectionTime = TIMER_J;
                        this.setState(TransactionState.TERMINATED);
                        if (!isReliable()) {
                            this.dialog.setRetransmissionTicks();
                            enableRetransmissionTimer();

                        }
                        enableTimeoutTimer(TIMER_J);
                    }
                } else if (300 <= statusCode && statusCode <= 699) {

                    // Set up to catch returning ACKs
                    this.setState(TransactionState.COMPLETED);
                    if (!isReliable()) {

                        enableRetransmissionTimer();

                    }
                    // Changed to TIMER_H as suggested by
                    // Antonis Karydas
                    enableTimeoutTimer(TIMER_H);

                    // If the response is a success message,
                } else if (statusCode / 100 == 2) {

                    // Terminate the transaction
                    this.setState(TransactionState.TERMINATED);
                    disableRetransmissionTimer();
                    disableTimeoutTimer();

                }

                // If the transaction is not an invite transaction
                // and this is a final response,
            } else if (200 <= statusCode && statusCode <= 699) {

                // Set up to retransmit this response,
                // or terminate the transaction
                this.setState(TransactionState.COMPLETED);
                if (!isReliable()) {

                    disableRetransmissionTimer();
                    enableTimeoutTimer(TIMER_J);

                } else {

                    this.setState(TransactionState.TERMINATED);

                }

            }

            // If the transaction has already completed,
        } else if (TransactionState.COMPLETED == this.getRealState()) {

            return;
        }
        try {
            // Send the message to the client.
            // Record the last message sent out.
            lastResponse = transactionResponse;
            this.sendResponse(transactionResponse);

        } catch (IOException e) {

            this.setState(TransactionState.TERMINATED);
            this.collectionTime = 0;
            throw e;

        }

    }

    public String getViaHost() {

        return encapsulatedChannel.getViaHost();

    }

    public int getViaPort() {

        return encapsulatedChannel.getViaPort();

    }

    /**
     * Called by the transaction stack when a retransmission timer fires. This
     * retransmits the last response when the retransmission filter is enabled.
     */
    protected void fireRetransmissionTimer() {

        try {

            // Resend the last response sent by this transaction
            if (isInviteTransaction()) {
                if (((SIPTransactionStack) getSIPStack()).retransmissionFilter) {
                    super.sendMessage(lastResponse);
                } else {
                    // Inform the application to retransmit the last response.
                    raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT);
                }
            }
        } catch (IOException e) {
            if (LogWriter.needsLogging)
                sipStack.logWriter.logException(e);
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);

        }

    }

    /**
     * Called by the transaction stack when a timeout timer fires.
     */
    protected void fireTimeoutTimer() {

        if (LogWriter.needsLogging)
            sipStack.logWriter
                    .logMessage("SIPServerTransaction.fireTimeoutTimer "
                            + this.getRealState() + " method = "
                            + this.getOriginalRequest().getMethod());

        SIPDialog dialog = (SIPDialog) this.dialog;
        if (((SIPTransactionStack) getSIPStack()).isDialogCreated(this
                .getOriginalRequest().getMethod())
                && (TransactionState.CALLING == this.getRealState() || TransactionState.TRYING == this
                        .getRealState())) {
            dialog.setState(SIPDialog.TERMINATED_STATE);
        } else if (getOriginalRequest().getMethod().equals(Request.BYE)) {
            if (dialog != null)
                dialog.setState(SIPDialog.TERMINATED_STATE);
        }

        if (TransactionState.COMPLETED == this.getRealState()
                && isInviteTransaction()) {
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);

            this.setState(TransactionState.TERMINATED);

        } else if (TransactionState.CONFIRMED == this.getRealState()
                && isInviteTransaction()) {
            // TIMER_I should not generate a timeout
            // exception to the application when the
            // Invite transaction is in Confirmed state.
            // Just transition to Terminated state.
            this.setState(TransactionState.TERMINATED);
        } else if (!isInviteTransaction()
                && (TransactionState.COMPLETED == this.getRealState() || TransactionState.CONFIRMED == this
                        .getRealState())) {
            this.setState(TransactionState.TERMINATED);
        } else if (isInviteTransaction()
                && TransactionState.TERMINATED == this.getRealState()) {
            // This state could be reached when retransmitting
            // Bug report sent in by Christophe
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
            if (dialog != null)
                dialog.setState(SIPDialog.TERMINATED_STATE);
        }

    }

    /**
     * Get the last response.
     */
    public SIPResponse getLastResponse() {
        return this.lastResponse;
    }

    /**
     * Set the original request.
     */
    public void setOriginalRequest(SIPRequest originalRequest) {
        super.setOriginalRequest(originalRequest);
        // ACK Server Transaction is just a dummy transaction.
        if (originalRequest.getMethod().equals("ACK"))
            this.setState(TransactionState.TERMINATED);

    }

    /**
     * Sends specified Response message to a Request which is identified by the
     * specified server transaction identifier. The semantics for various
     * application behaviour on sending Responses to Requests is outlined at
     * {@link SipListener#processRequest(RequestEvent)}.
     * <p>
     * Note that when a UAS core sends a 2xx response to an INVITE, the server
     * transaction is destroyed, by the underlying JAIN SIP implementation. This
     * means that when the ACK sent by the corresponding UAC arrives at the UAS,
     * there will be no matching server transaction for the ACK, and based on
     * this rule, the ACK is passed to the UAS application core, where it is
     * processed. This ensures that the three way handsake of an INVITE that is
     * managed by the UAS application and not JAIN SIP.
     * 
     * @param response -
     *            the Response to send to the Request
     * @throws SipException
     *             if implementation cannot send response for any other reason
     * @see Response
     */
    public void sendResponse(Response response) throws SipException {

        SIPDialog dialog = this.dialog;
        // Fix up the response if the dialog has already been established.
        try {
            SIPResponse responseImpl = (SIPResponse) response;

            // Dialog check for null - bug reported by Bill Roome.
            if (dialog != null
                    && responseImpl.getStatusCode() == 200
                    && sipStack.isDialogCreated(responseImpl.getCSeq()
                            .getMethod()) && dialog.getLocalTag() == null
                    && responseImpl.getTo().getTag() == null)
                throw new SipException("To tag must be set for OK");

            if (responseImpl.getStatusCode() == 200
                    && responseImpl.getCSeq().getMethod()
                            .equals(Request.INVITE)
                    && responseImpl.getHeader(ContactHeader.NAME) == null)
                throw new SipException("Contact Header is mandatory for the OK");

            // If sending the response within an established dialog, then
            // set up the tags appropriately.

            if (dialog != null && dialog.getLocalTag() != null)
                responseImpl.getTo().setTag(dialog.getLocalTag());

            // Backward compatibility slippery slope....
            // Only set the from tag in the response when the
            // incoming request has a from tag.
            String fromTag = ((SIPRequest) this.getRequest()).getFrom()
                    .getTag();
            if (fromTag != null)
                responseImpl.getFrom().setTag(fromTag);
            else {
                if (LogWriter.needsLogging)
                    sipStack.logWriter
                            .logMessage("WARNING -- Null From tag  Dialog layer in jeopardy!!");
            }

            this.sendMessage((SIPResponse) response);
            
            // See if the dialog needs to be inserted into the dialog table
            // or if the state of the dialog needs to be changed.
            if (dialog != null) {
                // Transaction successfully cancelled but dialog has not yet
                // been established so delete the dialog.
                // Does not apply to re-invite (Bug report by Martin LeClerc )

                if (responseImpl.getCSeq().getMethod().equalsIgnoreCase("CANCEL")
                        && responseImpl.getStatusCode() == 200
                        && (!dialog.isReInvite())
                        && sipStack.isDialogCreated(getOriginalRequest()
                                .getMethod())
                        && (dialog.getState() == null || dialog.getState()
                                .getValue() == SIPDialog.EARLY_STATE)) {
                    dialog.setState(SIPDialog.TERMINATED_STATE);
                } else if (responseImpl.getCSeq().getMethod().equals(Request.BYE)
                        && responseImpl.getStatusCode() == 200) {
                    // Only transition to terminated state when
                    // 200 OK is returned for the BYE. Other
                    // status codes just result in leaving the
                    // state in COMPLETED state.
                    dialog.setState(SIPDialog.TERMINATED_STATE);
                } else if (responseImpl.getCSeq().getMethod().equalsIgnoreCase(
                        Request.CANCEL)) {
                    if (dialog.getState() == null
                            || dialog.getState().getValue() == SIPDialog.EARLY_STATE) {
                        dialog.setState(SIPDialog.TERMINATED_STATE);
                    }
                } else if (dialog.getLocalTag() == null
                        && responseImpl.getTo().getTag() != null) {
                    if (responseImpl.getStatusCode() != 100)
                        dialog.setLocalTag(responseImpl.getTo().getTag());
                    
                    //Check if we want to put the dialog in the dialog table.
                    //A dialog is put into the dialog table when the server
                    //transaction is responded to by a provisional response
                  
                    if (sipStack.isDialogCreated(responseImpl.getCSeq()
                            .getMethod())) {
                        if (response.getStatusCode() / 100 == 1) {
                            dialog.setState(SIPDialog.EARLY_STATE);
                        }
                        // Enter into our dialog table provided this is a
                        // dialog creating method. Note that we dont put
                        // things into the dialog table for an error response.
                        if (responseImpl.getStatusCode() != 100 && 
                            responseImpl.getStatusCode()/100 <= 2  )
                            sipStack.putDialog(dialog);
                        else
                            dialog.setState(SIPDialog.TERMINATED_STATE);
                        

                        if (responseImpl.getStatusCode() / 100 == 2) {
                            if (responseImpl.getCSeq().getMethod().equals(
                                    Request.INVITE)) {
                                if (dialog.getState() == null)
                                    dialog.setState(SIPDialog.EARLY_STATE);
                                dialog.startTimer(this);
                            } else {
                                dialog.setState(SIPDialog.CONFIRMED_STATE);
                            }
                        }

                    }
                } else if (response.getStatusCode() / 100 == 2) {
                    if (sipStack.isDialogCreated(responseImpl.getCSeq()
                            .getMethod())) {
                        if (!responseImpl.getCSeq().getMethod().equals(
                                Request.INVITE)) {
                            dialog.setState(SIPDialog.CONFIRMED_STATE);
                        } else {
                            if (this.dialog.getState() == null) {
                                // On the server side of the dialog,
                                // go to confirmed state only after ACK.
                                this.dialog.setState(SIPDialog.EARLY_STATE);

                            }

                            // Start the dialog timer to send notifications
                            // to application.
                            this.dialog.startTimer(this);
                        }
                        // For an INVITE transaction, wait till we get the
                        // ACK back to go into the CONFIRMED state.
                        sipStack.putDialog(dialog);
                    }
                }
            }
        } catch (IOException ex) {
            throw new SipException(ex.getMessage());
        } catch (java.text.ParseException ex1) {
            throw new SipException(ex1.getMessage());
        }
    }

    /**
     * Return the book-keeping information that we actually use.
     */
    private TransactionState getRealState() {
        return super.getState();
    }

    /**
     * Return the current transaction state according to the RFC 3261
     * transaction state machine. Invite transactions do not have a trying
     * state. We just use this as a pseudo state for processing requests.
     * 
     * @return the state of the transaction.
     */
    public TransactionState getState() {
        // Trying is a pseudo state for INVITE transactions.
        if (this.isInviteTransaction()
                && TransactionState.TRYING == super.getState())
            return TransactionState.PROCEEDING;
        else
            return super.getState();
    }

    /**
     * Sets a timeout after which the connection is closed (provided the server
     * does not use the connection for outgoing requests in this time period)
     * and calls the superclass to set state.
     */
    public void setState(TransactionState newState) {
        // Set this timer for connection caching
        // of incoming connections.
        if (newState == TransactionState.TERMINATED && this.isReliable()
                && (!getSIPStack().cacheServerConnections)) {
            // Set a time after which the connection
            // is closed.
            this.collectionTime = TIMER_J;
        }
        super.setState(newState);
    }

    /**
     * This is a hack to force the listener to process the transaction. This
     * hack is necessary to get around a potential race condition when the
     * dialog creating request arrives (see check in EventScanner).
     */

    public boolean passToListener() {
        return toListener;
    }

    /**
     * Run any pending responses - gets called at the end of the event loop.
     */
    public void processPending() {
        PendingRequest pr;
        synchronized (pendingRequests) {
            if (pendingRequests.isEmpty())
                return;
            pr = (PendingRequest) this.pendingRequests.removeFirst();
        }
        this.processRequest(pr.sipRequest, pr.messageChannel);
    }

    public boolean hasPending() {
        synchronized (this.pendingRequests) {
            return !this.pendingRequests.isEmpty();
        }
    }

    public void clearPending() {
        boolean toNotify = false;
        synchronized (this.pendingRequests) {
            super.clearPending();
            if (this.isTerminated() || !pendingRequests.isEmpty()) {
                toNotify = true;
            }
        }
        if (toNotify)
            sipStack.notifyPendingRecordScanner();
    }

    /**
     * Start the timer task.
     */
    protected void startTransactionTimer() {
        myTimer = new TransactionTimer(this);
        sipStack.timer.schedule(myTimer, 0,
                SIPTransactionStack.BASE_TIMER_INTERVAL);
    }

    public boolean equals(Object other) {
        if (!other.getClass().equals(this.getClass())) {
            return false;
        }
        SIPServerTransaction sst = (SIPServerTransaction) other;
        return this.getBranch().equalsIgnoreCase(sst.getBranch());
    }

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.49  2004/10/28 19:02:51  mranga
 * Submitted by:  Daniel Martinez
 * Reviewed by:   M. Ranganathan
 *
 * Added changes for TLS support contributed by Daniel Martinez
 *
 * Revision 1.48  2004/10/06 18:56:07  mranga
 * *** empty log message ***
 *
 * Revision 1.47  2004/10/06 16:57:50  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Memory leak fix
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
 * Revision 1.46  2004/10/05 16:22:38  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  Xavi Ferro
 * Reviewed by:   mranga
 *
 * Another attempted fix for memory leak.
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
 * Revision 1.45 2004/10/04 16:03:53 mranga
 * Reviewed by: mranga attempted fix for memory leak
 * 
 * Revision 1.44 2004/10/04 14:43:20 mranga Reviewed by: mranga
 * 
 * Remove transaction from pending list when terminated.
 * 
 * Revision 1.43 2004/10/01 16:05:08 mranga Submitted by: mranga Fixed memory
 * leak
 * 
 * Revision 1.42 2004/09/10 13:35:13 mranga Submitted by: Bill Roome Reviewed
 * by: mranga
 * 
 * check for null dialog.
 * 
 * Revision 1.41 2004/07/23 06:50:04 mranga Submitted by: mranga Reviewed by:
 * mranga
 * 
 * Clean up - Get rid of annoying eclipse warnings.
 * 
 * Revision 1.40 2004/07/01 05:42:22 mranga Submitted by: Pierre De Rop and
 * Thomas Froment Reviewed by: M. Ranganathan
 * 
 * More performance hacks.
 * 
 * Revision 1.39 2004/06/21 05:42:32 mranga Reviewed by: mranga more code
 * smithing
 * 
 * Revision 1.38 2004/06/21 04:59:51 mranga Refactored code - no functional
 * changes.
 * 
 * Revision 1.37 2004/06/17 15:22:31 mranga Reviewed by: mranga
 * 
 * Added buffering of out-of-order in-dialog requests for more efficient
 * processing of such requests (this is a performance optimization ).
 * 
 * Revision 1.36 2004/06/16 02:53:20 mranga Submitted by: mranga Reviewed by:
 * implement re-entrant multithreaded listener model.
 * 
 * Revision 1.35 2004/06/15 09:54:45 mranga Reviewed by: mranga re-entrant
 * listener model added. (see configuration property
 * gov.nist.javax.sip.REENTRANT_LISTENER)
 * 
 * Revision 1.34 2004/06/02 13:09:58 mranga Submitted by: Peter Parnes Reviewed
 * by: mranga Fixed illegal state exception.
 * 
 * Revision 1.33 2004/05/30 18:55:58 mranga Reviewed by: mranga Move to timers
 * and eliminate the Transaction scanner Thread to improve scalability and
 * reduce cpu usage.
 * 
 * Revision 1.32 2004/05/20 13:59:23 mranga Reviewed by: mranga Cleaned up
 * slighly ugly code.
 * 
 * Revision 1.31 2004/05/18 15:26:44 mranga Reviewed by: mranga Attempted fix at
 * race condition bug. Remove redundant exception (never thrown). Clean up some
 * extraneous junk.
 * 
 * Revision 1.30 2004/05/14 20:20:03 mranga
 * 
 * Submitted by: Dave Stuart Reviewed by: mranga
 * 
 * Stun support hacks -- use the original address specified to bind tcp
 * transport socket.
 * 
 * Revision 1.29 2004/04/22 22:51:18 mranga Submitted by: Thomas Froment
 * Reviewed by: mranga
 * 
 * Fixed corner cases.
 * 
 * Revision 1.28 2004/04/19 21:51:04 mranga Submitted by: mranga Reviewed by:
 * ivov Support for stun.
 * 
 * Revision 1.27 2004/04/07 00:19:23 mranga Reviewed by: mranga Fixes a
 * potential race condition for client transactions. Handle re-invites
 * statefully within an established dialog.
 * 
 * Revision 1.26 2004/03/09 00:34:44 mranga Reviewed by: mranga Added TCP
 * connection management for client and server side Transactions. See
 * configuration parameter gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false Releases Client TCP
 * Connections after linger time
 * 
 * Revision 1.25 2004/03/07 22:25:24 mranga Reviewed by: mranga Added a new
 * configuration parameter that instructs the stack to drop a server connection
 * after server transaction termination set
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this Default behavior
 * is true.
 * 
 * Revision 1.24 2004/02/26 17:30:21 mranga Submitted by: jeand Reviewed by:
 * mranga Dont transition to terminated on anything but 200 ok receipt on the
 * client side.
 * 
 * Revision 1.23 2004/02/25 19:17:55 mranga Submitted by: Bruce van Gelder
 * Reviewed by: mranga Test original request before setting branch and port on
 * reply
 * 
 * Revision 1.22 2004/02/13 14:12:43 mranga Reviewed by: mranga Check for null
 * dialog assignment (for spurious bye).
 * 
 * Revision 1.64 2004/02/13 14:01:02 mranga Assign dialog ptr for all
 * transactions
 * 
 * Revision 1.21 2004/02/13 13:55:32 mranga Reviewed by: mranga per the spec,
 * Transactions must always have a valid dialog pointer. Assigned a dummy dialog
 * for transactions that are not assigned to any dialog (such as Message).
 * 
 * Revision 1.20 2004/02/05 14:43:21 mranga Reviewed by: mranga Fixed for
 * correct reporting of transaction state. Remove contact headers from ack
 * 
 * Revision 1.19 2004/01/27 13:52:11 mranga Reviewed by: mranga Fixed
 * server/user-agent parser. suppress sending ack to TU when retransFilter is
 * enabled and ack is retransmitted.
 * 
 * Revision 1.18 2004/01/25 16:06:24 mranga Reviewed by: M. Ranganathan
 * 
 * Clean up setting state (Use TransactionState instead of integer). Convert to
 * UNIX file format. Remove extraneous methods.
 * 
 * Revision 1.17 2004/01/22 18:39:41 mranga Reviewed by: M. Ranganathan Moved
 * the ifdef SIMULATION and associated tags to the first column so Prep
 * preprocessor can deal with them.
 * 
 * Revision 1.16 2004/01/22 14:23:45 mranga Reviewed by: mranga Fixed some minor
 * formatting issues.
 * 
 * Revision 1.15 2004/01/22 13:26:33 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 * 
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *  
 */
