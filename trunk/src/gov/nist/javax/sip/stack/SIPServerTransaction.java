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
import java.util.Timer;
//else
/*
import sim.java.util.SimTimer;
//endif
*/

import java.util.TimerTask;

/**
 * Represents a server transaction. Implements the following
 * state machines.
 * <pre>
 *                               |INVITE
 *                               |pass INV to TU
 *            INVITE             V send 100 if TU won't in 200ms
 *            send response+-----------+
 *                +--------|           |--------+101-199 from TU
 *                |        | Proceeding|        |send response
 *                +------->|           |<-------+
 *                         |           |          Transport Err.
 *                         |           |          Inform TU
 *                         |           |--------------->+
 *                         +-----------+                |
 *            300-699 from TU |     |2xx from TU        |
 *            send response   |     |send response      |
 *                            |     +------------------>+
 *                            |                         |
 *            INVITE          V          Timer G fires  |
 *            send response+-----------+ send response  |
 *                +--------|           |--------+       |
 *                |        | Completed |        |       |
 *                +------->|           |<-------+       |
 *                         +-----------+                |
 *                            |     |                   |
 *                        ACK |     |                   |
 *                        -   |     +------------------>+
 *                            |        Timer H fires    |
 *                            V        or Transport Err.|
 *                         +-----------+  Inform TU     |
 *                         |           |                |
 *                         | Confirmed |                |
 *                         |           |                |
 *                         +-----------+                |
 *                               |                      |
 *                               |Timer I fires         |
 *                               |-                     |
 *                               |                      |
 *                               V                      |
 *                         +-----------+                |
 *                         |           |                |
 *                         | Terminated|<---------------+
 *                         |           |
 *                         +-----------+
 *
 *              Figure 7: INVITE server transaction
 *
 *
 *   		Request received
 *                                  |pass to TU
 *
 *                                  V
 *                            +-----------+
 *                            |           |
 *                            | Trying    |-------------+
 *                            |           |             |
 *                            +-----------+             |200-699 from TU
 *                                  |                   |send response
 *                                  |1xx from TU        |
 *                                  |send response      |
 *                                  |                   |
 *               Request            V      1xx from TU  |
 *               send response+-----------+send response|
 *                   +--------|           |--------+    |
 *                   |        | Proceeding|        |    |
 *                   +------->|           |<-------+    |
 *            +<--------------|           |             |
 *            |Trnsprt Err    +-----------+             |
 *            |Inform TU            |                   |
 *            |                     |                   |
 *            |                     |200-699 from TU    |
 *            |                     |send response      |
 *            |  Request            V                   |
 *            |  send response+-----------+             |
 *            |      +--------|           |             |
 *            |      |        | Completed |<------------+
 *            |      +------->|           |
 *            +<--------------|           |
 *            |Trnsprt Err    +-----------+
 *            |Inform TU            |
 *            |                     |Timer J fires
 *            |                     |-
 *            |                     |
 *            |                     V
 *            |               +-----------+
 *            |               |           |
 *            +-------------->| Terminated|
 *                            |           |
 *                            +-----------+
 *
 *
 *</pre>
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.22 $ $Date: 2004-02-13 14:12:43 $
 * @author Jeff Keyser 
 * @author M. Ranganathan <mranga@nist.gov>  
 * @author Bug fixes by Emil Ivov, Antonis Karydas.
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *
 */
public class SIPServerTransaction
	extends SIPTransaction
	implements SIPServerRequestInterface, javax.sip.ServerTransaction {

	protected int collectionTime;

	// Real RequestInterface to pass messages to
	private SIPServerRequestInterface requestOf;

	class SendTrying extends TimerTask {
		private SIPServerTransaction serverTransaction;

		protected SendTrying(SIPServerTransaction st) {
			if (LogWriter.needsLogging)
				parentStack.logWriter.logMessage("scheduled timer for " + st);
			this.serverTransaction = st;
		}

		public void run() {
			if (this.serverTransaction.getRealState() == null
				|| TransactionState.TRYING == this.serverTransaction.getRealState()) {
				if (LogWriter.needsLogging)
					parentStack.logWriter.logMessage(
						" sending Trying current state = "
							+ this.serverTransaction.getRealState());
				try {
					serverTransaction.sendMessage(
						serverTransaction.getOriginalRequest().createResponse(
							100,
							"Trying"));
					if (LogWriter.needsLogging)
						parentStack.logWriter.logMessage(
							" trying sent "
								+ this.serverTransaction.getRealState());
				} catch (IOException ex) {
					if (LogWriter.needsLogging)
						parentStack.logWriter.logMessage(
							"IO error sending  TRYING");
				}
			}
		}
	}

	private void sendResponse(SIPResponse transactionResponse)
		throws IOException {
		// Bug report by Shanti Kadiyala	
		if (transactionResponse.getTopmostVia().getParameter(Via.RECEIVED)
			== null) {
			// Send the response back on the same peer as received.
			getMessageChannel().sendMessage(transactionResponse);
		} else {
			// Respond to the host name in the received parameter.
			Via via = transactionResponse.getTopmostVia();
			String host = via.getParameter(Via.RECEIVED);
			int port = via.getPort();
			if (port == -1)
				port = 5060;
			String transport = via.getTransport();
			HopImpl hop = new HopImpl(host + ":" + port + "/" + transport);
			MessageChannel messageChannel =
				((SIPTransactionStack) getSIPStack()).createRawMessageChannel(
					hop);
			messageChannel.sendMessage(transactionResponse);
		}
	}

	/**
	 * Creates a new server transaction.
	 *
	 * @param newSIPStack Transaction stack this transaction
	 * belongs to.
	 * @param newChannelToUse Channel to encapsulate.
	 */
	protected SIPServerTransaction(
		SIPTransactionStack newSIPStack,
		MessageChannel newChannelToUse) {

		super(newSIPStack, newChannelToUse);
		if (LogWriter.needsLogging) {
			parentStack.logWriter.logMessage(
				"Creating Server Transaction" + this);
			parentStack.logWriter.logStackTrace();
		}

	}

	/**
	 * Sets the real RequestInterface this transaction encapsulates.
	 *
	 * @param newRequestOf RequestInterface to send messages to.
	 */
	public void setRequestInterface(SIPServerRequestInterface newRequestOf) {

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
	 * @param messageToTest Message to check if it is part of this
	 * transaction.
	 *
	 * @return True if the message is part of this transaction,
	 * false if not.
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
		if (LogWriter.needsLogging) {
			parentStack.logWriter.logMessage("--------- TEST ------------");
			parentStack.logWriter.logMessage(
				" testing " + this.getOriginalRequest());
			parentStack.logWriter.logMessage("Against " + messageToTest);
			parentStack.logWriter.logMessage(
				"isTerminated = " + isTerminated());
		}

		// Compensation for retransmits after OK has been dispatched  
		// as suggested by Antonis Karydas. Cancel Processing is 
		// special because we want to look for the invite
		if (((SIPRequest) messageToTest).getMethod().equals(Request.CANCEL)
			|| ((((SIPTransactionStack) getSIPStack())
				.isDialogCreated(((SIPRequest) messageToTest).getMethod()))
				|| !isTerminated())) {

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
							((Via) getOriginalRequest()
								.getViaHeaders()
								.getFirst())
								.getSentBy())) {
						// Matching server side transaction with only the
						// branch parameter.
						transactionMatches = true;
					}

					// If this is an RFC2543-compliant message,
				} else {

					// If RequestURI, To tag, From tag,
					// CallID, CSeq number, and top Via
					// headers are the same,
					String originalFromTag =
						getOriginalRequest().getFrom().getTag();

					String thisFromTag = messageToTest.getFrom().getTag();

					boolean skipFrom =
						(originalFromTag == null || thisFromTag == null);

					String originalToTag =
						getOriginalRequest().getTo().getTag();

					String thisToTag = messageToTest.getTo().getTag();

					boolean skipTo =
						(originalToTag == null || thisToTag == null);

					if (getOriginalRequest()
						.getRequestURI()
						.equals(((SIPRequest) messageToTest).getRequestURI())
						&& (skipFrom || originalFromTag.equals(thisFromTag))
						&& (skipTo || originalToTag.equals(thisToTag))
						&& getOriginalRequest().getCallId().getCallId().equals(
							messageToTest.getCallId().getCallId())
						&& getOriginalRequest().getCSeq().getSequenceNumber()
							== messageToTest.getCSeq().getSequenceNumber()
						&& topViaHeader.equals(
							getOriginalRequest().getViaHeaders().getFirst())) {

						transactionMatches = true;
					}

				}

			}

		}
		return transactionMatches;

	}

	/**
	 * Send out a trying response (only happens when the transaction is
	 * mapped). Otherwise the transaction is not known to the stack.
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
				new Timer().schedule(new SendTrying(this), 200);
//else
/*
		 		new SimTimer().schedule( new SendTrying( this ), 200);
//endif
*/

			} else {
				isMapped = true;
			}
		}
	}

	/**
	 * Return true if the transaction is known to stack.
	 */
	public boolean isTransactionMapped() {
		return this.isMapped;
	}

	/**
	 * Process a new request message through this transaction.
	 * If necessary, this message will also be passed onto the TU.
	 *
	 * @param transactionRequest Request to process.
	 * @param sourceChannel Channel that received this message.
	 */
	public void processRequest(
		SIPRequest transactionRequest,
		MessageChannel sourceChannel)
		throws SIPServerException {
		boolean toTu = false;

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
					sendMessage(
						transactionRequest.createResponse(100, "Trying"));

				}
				// If an invite transaction is ACK'ed while in
				// the completed state,
			} else if (
				isInviteTransaction()
					&& TransactionState.COMPLETED == getRealState()
					&& transactionRequest.getMethod().equals(Request.ACK)) {

				this.setState(TransactionState.CONFIRMED);
				disableRetransmissionTimer();
				if (!isReliable()) {
					if (this.lastResponse != null
						&& this.lastResponse.getStatusCode()
							== Response.REQUEST_TERMINATED) {
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
			} else if (
				transactionRequest.getMethod().equals(
					getOriginalRequest().getMethod())) {

				if (TransactionState.PROCEEDING == getRealState()
					|| TransactionState.COMPLETED == getRealState()) {

					// Resend the last response to
					// the client
					if (lastResponse != null) {
						try {
							// Send the message to the client
							getMessageChannel().sendMessage(lastResponse);
						} catch (IOException e) {
							this.setState(TransactionState.TERMINATED);
							throw e;

						}
					}
				} else if (
					transactionRequest.getMethod().equals(Request.ACK)
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
				if (getOriginalRequest()
					.getMethod()
					.equals(transactionRequest.getMethod())) {
					// Only send original request to TU once!
					if (toTu)
						requestOf.processRequest(transactionRequest, this);
				} else {
					requestOf.processRequest(transactionRequest, this);
				}
			} else {
				// This seems like a common bug so I am allowing it through!
				if (((SIPTransactionStack) getSIPStack())
					.isDialogCreated(getOriginalRequest().getMethod())
					&& getRealState() == TransactionState.TERMINATED
					&& transactionRequest.getMethod().equals(Request.ACK)
					&& requestOf != null) {
					DialogImpl thisDialog = (DialogImpl) this.dialog;
					thisDialog.ackReceived(transactionRequest);
			
					if  ( ((SIPTransactionStack) getSIPStack())
						.retransmissionFilter ) { 
					  if ( ! thisDialog.ackProcessed ) {
					     // Filter out duplicate acks if retransmission filter
					     // is enabled.
					     thisDialog.ackProcessed = true;
					     requestOf.processRequest(transactionRequest, this);
					  }
					}  else {
					  // Duplicate ACKs are seen by the application.
					  requestOf.processRequest(transactionRequest, this);
					}
				} else if (
					transactionRequest.getMethod().equals(Request.CANCEL)) {
					if (LogWriter.needsLogging)
						parentStack.logWriter.logMessage(
							"Too late to cancel Transaction");
					// send OK and just ignore the CANCEL.
					try {
						this.sendMessage(
							transactionRequest.createResponse(Response.OK));
					} catch (IOException ex) {
						// Transaction is already terminated
						// just ignore the IOException.
					}
				}
				parentStack.logWriter.logMessage(
					"Dropping request " + getRealState());
			}

		} catch (IOException e) {
			raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
		}

	}

	/**
	 * Send a response message through this transactionand onto
	 * the client. The response drives the state machine.
	 *
	 * @param messageToSend Response to process and send.
	 */
	public void sendMessage(SIPMessage messageToSend) throws IOException {

		// Message typecast as a response
		SIPResponse transactionResponse;
		// Status code of the response being sent to the client
		int statusCode;

		// Get the status code from the response
		transactionResponse = (SIPResponse) messageToSend;
		statusCode = transactionResponse.getStatusCode();

		DialogImpl dialog = this.dialog;

		try {
			// Provided we have set the banch id for this we set the BID for the
			// outgoing via.
			if (this.getBranch() != null)
				transactionResponse.getTopmostVia().setBranch(this.getBranch());
			else
				transactionResponse.getTopmostVia().removeParameter(
					ParameterNames.BRANCH);
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		// Method of the response does not match the request used to
		// create the transaction - transaction state does not change.
		if (!transactionResponse
			.getCSeq()
			.getMethod()
			.equals(getOriginalRequest().getMethod())) {
			sendResponse(transactionResponse);
			return;
		}

		// Put a dialog record in the SIP Stack if necessary.
		if (this.dialog != null
			&& this.dialog.getRemoteTag() == null
			&& transactionResponse.getTo().getTag() != null
			&& ((SIPTransactionStack) this.getSIPStack()).isDialogCreated(
				transactionResponse.getCSeq().getMethod())) {
			this.dialog.setRemoteTag(transactionResponse.getTo().getTag());
			((SIPTransactionStack) this.getSIPStack()).putDialog(this.dialog);
			if (statusCode / 100 == 1)
				this.dialog.setState(DialogImpl.EARLY_STATE);
		} else if (
			((SIPTransactionStack) this.getSIPStack()).isDialogCreated(
				transactionResponse.getCSeq().getMethod())
				&& transactionResponse.getCSeq().getMethod().equals(
					getOriginalRequest().getMethod())) {
			if (statusCode / 100 == 2) {
				this.dialog.setState(DialogImpl.CONFIRMED_STATE);
			} else if (statusCode / 100 >= 3 && statusCode / 100 <= 6) {
				this.dialog.setState(DialogImpl.TERMINATED_STATE);
			}
		} else if ( this.dialog != null &&
			transactionResponse.getCSeq().getMethod().equals(Request.BYE)
				&& statusCode / 100 == 2 && dialog != null ) {
			// Dialog will be terminated when the transction is terminated.
			if (!isReliable())
				this.dialog.setState(DialogImpl.COMPLETED_STATE);
			else
				this.dialog.setState(DialogImpl.TERMINATED_STATE);
		}

		// If the TU sends a provisional response while in the
		// trying state,
		if (getRealState() == TransactionState.TRYING) {
			if (statusCode / 100 == 1) {
				this.setState(TransactionState.PROCEEDING);
			} else if (200 <= statusCode && statusCode <= 699) {
				// Check --  bug report from christophe
				if (!isInviteTransaction()) {
					this.setState(TransactionState.COMPLETED);
				} else {
					if (statusCode / 100 == 2)
						this.setState(TransactionState.TERMINATED);
					else
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
				if (statusCode / 100 == 2 ) {
					// Set up to catch returning ACKs

					// Antonis Karydas: Suggestion
					// Recall that the CANCEL's response will go 
					// through this transaction
					// and this may well be it. Do NOT change the 
					// transaction state if this
					// is a response for a CANCEL. 
					// Wait, instead for the 487 from TU.
					if (!transactionResponse
						.getCSeq()
						.getMethod()
						.equals(Request.CANCEL)) {

						this.setState(TransactionState.TERMINATED);
						if (!isReliable() ) {
							this.dialog
								.setRetransmissionTicks();
							enableRetransmissionTimer();

						}
						this.collectionTime = TIMER_J;
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
	 * Called by the transaction stack when a retransmission
	 * timer fires. This retransmits the last response when the
	 * retransmission filter is enabled.
	 */
	protected void fireRetransmissionTimer() {

		try {

			// Resend the last response sent by this transaction
			if (isInviteTransaction()) {
				if (((SIPTransactionStack) getSIPStack())
					.retransmissionFilter) {
					getMessageChannel().sendMessage(lastResponse);
				} else {
					// Inform the application to retransmit the last response.
					raiseErrorEvent(
						SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT);
				}
			}
		} catch (IOException e) {
			if (LogWriter.needsLogging)
				parentStack.logWriter.logException(e);
			raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);

		}

	}

	/**
	 * Called by the transaction stack when a timeout timer fires.
	 */
	protected void fireTimeoutTimer() {

		if (LogWriter.needsLogging)
			parentStack.logWriter.logMessage(
				"SIPServerTransaction.fireTimeoutTimer "
					+ this.getRealState()
					+ " method = "
					+ this.getOriginalRequest().getMethod());

		DialogImpl dialog = (DialogImpl) this.dialog;
		if (((SIPTransactionStack) getSIPStack())
			.isDialogCreated(this.getOriginalRequest().getMethod())
			&& (TransactionState.CALLING == this.getRealState()
				|| TransactionState.TRYING == this.getRealState())) {
			dialog.setState(DialogImpl.TERMINATED_STATE);
		} else if (getOriginalRequest().getMethod().equals(Request.BYE)) {
			if (dialog != null)
				dialog.setState(DialogImpl.TERMINATED_STATE);
		}

		if (TransactionState.COMPLETED == this.getRealState()
			&& isInviteTransaction()) {
			raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);

			this.setState(TransactionState.TERMINATED);

		} else if (
			TransactionState.CONFIRMED == this.getRealState()
				&& isInviteTransaction()) {
			// TIMER_I should not generate a timeout 
			// exception to the application when the 
			// Invite transaction is in Confirmed state.
			// Just transition to Terminated state.
			this.setState(TransactionState.TERMINATED);
		} else if (
			!isInviteTransaction()
				&& (TransactionState.COMPLETED == this.getRealState()
					|| TransactionState.CONFIRMED == this.getRealState())) {
			this.setState(TransactionState.TERMINATED);
		} else if (
			isInviteTransaction()
				&& TransactionState.TERMINATED == this.getRealState()) {
			// This state could be reached when retransmitting
			// Bug report sent in by Christophe
			raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
			if (dialog != null)
				dialog.setState(DialogImpl.TERMINATED_STATE);
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
	 * transaction is destroyed, by the underlying JAIN SIP implementation.
	 * This means that when the ACK sent by the corresponding UAC arrives
	 * at the UAS, there will be no matching server transaction for the ACK,
	 * and based on this rule, the ACK is passed to the UAS application core,
	 * where it is processed.
	 * This ensures that the three way handsake of an INVITE that is managed by
	 * the UAS application and not JAIN SIP.
	 *
	 * @param response - the Response to send to the Request
	 * @throws SipException if implementation cannot send response for any
	 * other reason
	 * @see Response
	 */
	public void sendResponse(Response response) throws SipException {

		DialogImpl dialog = this.dialog;
		// Fix up the response if the dialog has already been established.
		try {
			SIPResponse responseImpl = (SIPResponse) response;

			if (responseImpl.getStatusCode() == 200
				&& parentStack.isDialogCreated(responseImpl.getCSeq().getMethod())
				&& dialog.getLocalTag() == null
				&& responseImpl.getTo().getTag() == null)
				throw new SipException("To tag must be set for OK");

			if (responseImpl.getStatusCode() == 200
				&& responseImpl.getCSeq().getMethod().equals(Request.INVITE)
				&& responseImpl.getHeader(ContactHeader.NAME) == null)
				throw new SipException("Contact Header is mandatory for the OK");

			// If sending the response within an established dialog, then
			// set up the tags appropriately.

			if (dialog != null && dialog.getLocalTag() != null)
				responseImpl.getTo().setTag(dialog.getLocalTag());

			// Backward compatibility slippery slope....
			// Only set the from tag in the response when the
			// incoming request has a from tag.
			String fromTag =
				((SIPRequest) this.getRequest()).getFrom().getTag();
			if (fromTag != null)
				responseImpl.getFrom().setTag(fromTag);
			else {
				if (LogWriter.needsLogging)
					parentStack.logWriter.logMessage(
						"WARNING -- Null From tag  Dialog layer in jeopardy!!");
			}

			this.sendMessage((SIPResponse) response);
			// Transaction successfully cancelled but dialog has not yet
			// been established so delete the dialog.
			// Does not apply to re-invite (Bug report by Martin LeClerc )

			if (responseImpl.getCSeq().getMethod().equalsIgnoreCase("CANCEL")
				&& responseImpl.getStatusCode() == 200
				&& dialog != null 
				&& (!dialog.isReInvite())
				&& parentStack.isDialogCreated(getOriginalRequest().getMethod())
				&& (dialog.getState() == null
					|| dialog.getState().getValue() == DialogImpl.EARLY_STATE)) {
				dialog.setState(DialogImpl.TERMINATED_STATE);
			}
			// See if the dialog needs to be inserted into the dialog table
			// or if the state of the dialog needs to be changed.
			if (dialog != null) {
				dialog.printTags();
				if (responseImpl
					.getCSeq()
					.getMethod()
					.equalsIgnoreCase(Request.BYE)) {
					dialog.setState(DialogImpl.TERMINATED_STATE);
				} else if (
					responseImpl.getCSeq().getMethod().equalsIgnoreCase(
						Request.CANCEL)) {
					if (dialog.getState() == null
						|| dialog.getState().getValue()
							== DialogImpl.EARLY_STATE) {
						dialog.setState(DialogImpl.TERMINATED_STATE);
					}
				} else if (
					dialog.getLocalTag() == null
						&& responseImpl.getTo().getTag() != null) {
					if (responseImpl.getStatusCode() != 100)
						dialog.setLocalTag(responseImpl.getTo().getTag());
					if (parentStack
						.isDialogCreated(responseImpl.getCSeq().getMethod())) {
						if (response.getStatusCode() / 100 == 1) {
							dialog.setState(DialogImpl.EARLY_STATE);
						}
						// Enter into our dialog table provided this is a
						// dialog creating method.
						if (responseImpl.getStatusCode() != 100)
							parentStack.putDialog(dialog);
					}
				} else if (response.getStatusCode() / 100 == 2) {
					if (parentStack
						.isDialogCreated(responseImpl.getCSeq().getMethod())) {
						dialog.setState(DialogImpl.CONFIRMED_STATE);
						parentStack.putDialog(dialog);
					}
				}
			}
		} catch (IOException ex) {
			throw new SipException(ex.getMessage());
		} catch (java.text.ParseException ex1) {
			throw new SipException(ex1.getMessage());
		}
	}

	
	/** Return the book-keeping information that we actually use.
	*/
	private TransactionState getRealState() {
		return super.getState();
	}

	/** Return the current transaction state according to the RFC 3261 
	* transaction state machine. Invite transactions do not have a 
	* trying state. We just use this as a pseudo state for processing
	* requests.
	*@return the state of the transaction.
	*/
	public TransactionState getState() {
		// Trying is a pseudo state for INVITE transactions.
		if (this.isInviteTransaction() && TransactionState.TRYING == super.getState()) 
			return TransactionState.PROCEEDING;
		else return super.getState();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.64  2004/02/13 14:01:02  mranga
 * Assign dialog ptr for all transactions
 *
 * Revision 1.21  2004/02/13 13:55:32  mranga
 * Reviewed by:   mranga
 * per the spec, Transactions must always have a valid dialog pointer. Assigned a dummy dialog for transactions that are not assigned to any dialog (such as Message).
 *
 * Revision 1.20  2004/02/05 14:43:21  mranga
 * Reviewed by:   mranga
 * Fixed for correct reporting of transaction state.
 * Remove contact headers from ack
 *
 * Revision 1.19  2004/01/27 13:52:11  mranga
 * Reviewed by:   mranga
 * Fixed server/user-agent parser.
 * suppress sending ack to TU when retransFilter is enabled and ack is retransmitted.
 *
 * Revision 1.18  2004/01/25 16:06:24  mranga
 * Reviewed by:   M. Ranganathan
 *
 * Clean up setting state (Use TransactionState instead of integer). Convert to UNIX file format.
 * Remove extraneous methods.
 *
 * Revision 1.17  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.16  2004/01/22 14:23:45  mranga
 * Reviewed by:   mranga
 * Fixed some minor formatting issues.
 *
 * Revision 1.15  2004/01/22 13:26:33  sverker
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
