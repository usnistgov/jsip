package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.SIPConstants;
import javax.sip.message.*;
import java.util.*;
import gov.nist.javax.sip.address.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;

import java.io.IOException;

//ifdef SIMULATION
/*
import sim.java.*;
//endif
*/

/**
 *Represents a client transaction.
 * Implements the following state machines. (From RFC 3261)
 *<pre>
 *
 *
 *                               |INVITE from TU
 *             Timer A fires     |INVITE sent
 *             Reset A,          V                      Timer B fires
 *             INVITE sent +-----------+                or Transport Err.
 *               +---------|           |---------------+inform TU
 *               |         |  Calling  |               |
 *               +-------->|           |-------------->|
 *                         +-----------+ 2xx           |
 *                            |  |       2xx to TU     |
 *                            |  |1xx                  |
 *    300-699 +---------------+  |1xx to TU            |
 *   ACK sent |                  |                     |
 *resp. to TU |  1xx             V                     |
 *            |  1xx to TU  -----------+               |
 *            |  +---------|           |               |
 *            |  |         |Proceeding |-------------->|
 *            |  +-------->|           | 2xx           |
 *            |            +-----------+ 2xx to TU     |
 *            |       300-699    |                     |
 *            |       ACK sent,  |                     |
 *            |       resp. to TU|                     |
 *            |                  |                     |      NOTE:
 *            |  300-699         V                     |
 *            |  ACK sent  +-----------+Transport Err. |  transitions
 *            |  +---------|           |Inform TU      |  labeled with
 *            |  |         | Completed |-------------->|  the event
 *            |  +-------->|           |               |  over the action
 *            |            +-----------+               |  to take
 *            |              ^   |                     |
 *            |              |   | Timer D fires       |
 *            +--------------+   | -                   |
 *                               |                     |
 *                               V                     |
 *                         +-----------+               |
 *                         |           |               |
 *                         | Terminated|<--------------+
 *                         |           |
 *                         +-----------+
 *
 *                 Figure 5: INVITE client transaction
 *
 *
 *                                   |Request from TU
 *                                   |send request
 *               Timer E             V
 *               send request  +-----------+
 *                   +---------|           |-------------------+
 *                   |         |  Trying   |  Timer F          |
 *                   +-------->|           |  or Transport Err.|
 *                             +-----------+  inform TU        |
 *                200-699         |  |                         |
 *                resp. to TU     |  |1xx                      |
 *                +---------------+  |resp. to TU              |
 *                |                  |                         |
 *                |   Timer E        V       Timer F           |
 *                |   send req +-----------+ or Transport Err. |
 *                |  +---------|           | inform TU         |
 *                |  |         |Proceeding |------------------>|
 *                |  +-------->|           |-----+             |
 *                |            +-----------+     |1xx          |
 *                |              |      ^        |resp to TU   |
 *                | 200-699      |      +--------+             |
 *                | resp. to TU  |                             |
 *                |              |                             |
 *                |              V                             |
 *                |            +-----------+                   |
 *                |            |           |                   |
 *                |            | Completed |                   |
 *                |            |           |                   |
 *                |            +-----------+                   |
 *                |              ^   |                         |
 *                |              |   | Timer K                 |
 *                +--------------+   | -                       |
 *                                   |                         |
 *                                   V                         |
 *             NOTE:           +-----------+                   |
 *                             |           |                   |
 *         transitions         | Terminated|<------------------+
 *         labeled with        |           |
 *         the event           +-----------+
 *         over the action
 *         to take
 *
 *                 Figure 6: non-INVITE client transaction
 *
 *
 *</pre>
 *
 *@author Jeff Keyser
 *@author M. Ranganathan <mranga@nist.gov>
 *@author Bug fixes by Emil Ivov.
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *@version  JAIN-SIP-1.1 $Revision: 1.24 $ $Date: 2004-04-19 21:51:04 $
 */
public class SIPClientTransaction
	extends SIPTransaction
	implements SIPServerResponseInterface, javax.sip.ClientTransaction {
	
	// max # of pending responses that can we can buffer (to avoid
	// response flooding DOS attack).
	private static final int MAX_PENDING_RESPONSES = 4;

	private LinkedList pendingResponses;

	private SIPRequest lastRequest;

	private int viaPort;

	private String viaHost;

	// Real ResponseInterface to pass messages to
	private SIPServerResponseInterface respondTo;


	class PendingResponse {
		protected SIPResponse sipResponse;
		protected MessageChannel messageChannel;
		public PendingResponse(SIPResponse sipResponse,
				MessageChannel messageChannel) {
			this.sipResponse = sipResponse;
			this.messageChannel = messageChannel;
		}
	}

	/**
	 *	Creates a new client transaction.
	 *
	 *	@param newSIPStack Transaction stack this transaction
	 *      belongs to.
	 *	@param newChannelToUse Channel to encapsulate.
	 */
	protected SIPClientTransaction(
		SIPTransactionStack newSIPStack,
		MessageChannel newChannelToUse) {
		super(newSIPStack, newChannelToUse);
		// Create a random branch parameter for this transaction
		// setBranch( SIPConstants.BRANCH_MAGIC_COOKIE +
		// Integer.toHexString( hashCode( ) ) );
		setBranch(Utils.generateBranchId());
		if (LogWriter.needsLogging) {
			parentStack.logWriter.logMessage(
				"Creating clientTransaction " + this);
			parentStack.logWriter.logStackTrace();
		}
		this.pendingResponses = new LinkedList();

	}

	/**
	 *	Sets the real ResponseInterface this transaction encapsulates.
	 *
	 *	@param newRespondTo ResponseInterface to send messages to.
	 */
	public void setResponseInterface(SIPServerResponseInterface newRespondTo) {

		respondTo = newRespondTo;

	}

	public String getProcessingInfo() {

		return respondTo.getProcessingInfo();

	}

	/**
	 *	Returns this transaction.
	 */
	public MessageChannel getRequestChannel() {

		return this;

	}

	/**
	 *	Deterines if the message is a part of this transaction.
	 *
	 *	@param messageToTest Message to check if it is part of this
	 *		transaction.
	 *
	 *	@return True if the message is part of this transaction,
	 * 		false if not.
	 */
	public boolean isMessagePartOfTransaction(SIPMessage messageToTest) {

		// List of Via headers in the message to test
		ViaList viaHeaders = messageToTest.getViaHeaders();
		// Flags whether the select message is part of this transaction
		boolean transactionMatches;
		String messageBranch = ((Via) viaHeaders.getFirst()).getBranch();
		boolean rfc3261Compliant =
				getBranch() != null
				&& messageBranch != null
				&& getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE)
				&& messageBranch.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE);

		/**
		if (parentStack.logWriter.needsLogging)  {
			parentStack.logWriter.logMessage("--------- TEST ------------");
			parentStack.logWriter.logMessage(" testing " + this.getOriginalRequest());
			parentStack.logWriter.logMessage("Against " + messageToTest);
			parentStack.logWriter.logMessage("isTerminated = " + isTerminated());
			parentStack.logWriter.logMessage("messageBranch = " + messageBranch);
			parentStack.logWriter.logMessage("viaList = " + messageToTest.getViaHeaders());
			parentStack.logWriter.logMessage("myBranch = " + getBranch());
		}
		**/

		transactionMatches = false;
		if (TransactionState.COMPLETED == this.getState()) {
			if (rfc3261Compliant) {
				transactionMatches =
					getBranch().equals(
						((Via) viaHeaders.getFirst()).getBranch());
			} else {
				transactionMatches =
					getBranch().equals(messageToTest.getTransactionId());
			}
		} else if (!isTerminated()) {
			if (rfc3261Compliant) {
				if (viaHeaders != null) {
					// If the branch parameter is the
					//same as this transaction and the method is the same,
					if (getBranch()
						.equals(((Via) viaHeaders.getFirst()).getBranch())) {
						transactionMatches =
							getOriginalRequest().getCSeq().getMethod().equals(
								messageToTest.getCSeq().getMethod());

					}
				}
			} else {
				// not RFC 3261 compliant.
				if (getBranch() != null) {
					transactionMatches =
						getBranch().equals(messageToTest.getTransactionId());
				} else {
					transactionMatches =
						getOriginalRequest().getTransactionId().equals(
							messageToTest.getTransactionId());
				}

			}

		}
		return transactionMatches;

	}

	/**
	 *  Send a request message through this transaction and
	 *  onto the client.
	 *
	 *	@param messageToSend Request to process and send.
	 */
	public void sendMessage(SIPMessage messageToSend) throws IOException {

		// Message typecast as a request
		SIPRequest transactionRequest;

//ifdef SIMULATION
/*
		SimSystem.hold(getSIPStack().stackProcessingTime);
//endif
*/

		transactionRequest = (SIPRequest) messageToSend;

		// Set the branch id for the top via header.
		Via topVia = (Via) transactionRequest.getViaHeaders().getFirst();
		// Tack on a branch identifier  to match responses.
		try {
			topVia.setBranch(getBranch());
		} catch (java.text.ParseException ex) {
		}

		// If this is the first request for this transaction,
		if (TransactionState.PROCEEDING == getState()
			|| TransactionState.CALLING == getState()) {

			// If this is a TU-generated ACK request,
			if (transactionRequest.getMethod().equals(Request.ACK)) {
				// Send directly to the underlying
				// transport and close this transaction
				// Bug fix by Emil Ivov
				if (isReliable()) {
					this.setState(TransactionState.TERMINATED);
				} else {
					this.setState(TransactionState.COMPLETED);
				}
				getMessageChannel().sendMessage(transactionRequest);
				return;

			}

		}
		try {

			// Send the message to the server
			lastRequest = transactionRequest;
			if (getState() == null) {
				// Save this request as the one this transaction
				// is handling
				setOriginalRequest(transactionRequest);
				// Change to trying/calling state
				if (transactionRequest.getMethod().equals(Request.INVITE)) {
					this.setState(TransactionState.CALLING);
				} else if (
					transactionRequest.getMethod().equals(Request.ACK)) {
					// Acks are never retransmitted.
					this.setState(TransactionState.TERMINATED);
				} else {
					this.setState(TransactionState.TRYING);
				}
				if (!isReliable()) {
					enableRetransmissionTimer();
				}
				if (isInviteTransaction()) {
					enableTimeoutTimer(TIMER_B);
				} else {
					enableTimeoutTimer(TIMER_F);
				}
			}
			// Set state first to avoid race condition..
			getMessageChannel().sendMessage(transactionRequest);

		} catch (IOException e) {

			this.setState(TransactionState.TERMINATED);
			throw e;

		}

	}

	/**
	 *	Process a new response message through this transaction.
	 * If necessary, this message will also be passed onto the TU.
	 *
	 *	@param transactionResponse Response to process.
	 *	@param sourceChannel Channel that received this message.
	 */
	public synchronized void processResponse(
		SIPResponse transactionResponse,
		MessageChannel sourceChannel)
		throws SIPServerException {
		// Log the incoming response in our log file.
		if (parentStack
			.serverLog
			.needsLogging(ServerLog.TRACE_MESSAGES))
			this.logResponse(transactionResponse,

//ifdef SIMULATION
/*
			SimSystem.currentTimeMillis(),
//else
*/
			System.currentTimeMillis(),
//endif
//

			"normal processing");

		// If the state has not yet been assigned then this is a
		// spurious response.
		if (getState() == null)
			return;

		// Ignore 1xx 
		if (TransactionState.COMPLETED == this.getState()
			&& transactionResponse.getStatusCode() / 100 == 1) {
			return;
		} else if (TransactionState.PROCEEDING == this.getState()
			 && transactionResponse.getStatusCode() == 100 ) { 
			// Ignore 100 if received after 180
			// bug report from Peter Parnes.
			return;
		}
		// Defer processing if a previous event has been placed in the processing queue.
		// bug shows up on fast dual processor machines where a subsequent response
		// arrives before a previous one completes processing.
		if (this.eventPending ) {
			if (this.pendingResponses.size() < MAX_PENDING_RESPONSES) 
				this.pendingResponses.add
				(new PendingResponse(transactionResponse,sourceChannel));
			return;
		}

		if (LogWriter.needsLogging)
			parentStack.logWriter.logMessage(
				"processing "
					+ transactionResponse.getFirstLine()
					+ "current state = "
					+ getState());

		this.lastResponse = transactionResponse;

		if (dialog != null) {
			// add the route before you process the response.
			// Bug noticed by Brad Templeton.
			dialog.addRoute(transactionResponse);
		}
		String method = transactionResponse.getCSeq().getMethod();
		if (dialog != null) {
			boolean added = false;
			SIPTransactionStack sipStackImpl =
				(SIPTransactionStack) getSIPStack();

			// A tag just got assigned  or changed.
			if (dialog.getRemoteTag() == null
				&& transactionResponse.getTo().getTag() != null) {

				// Dont assign tag on provisional response
				if (transactionResponse.getStatusCode() != 100) {
					dialog.setRemoteTag(transactionResponse.getToTag());
				}
				String dialogId = transactionResponse.getDialogId(false);
				dialog.setDialogId(dialogId);
				if (sipStackImpl.isDialogCreated(method)
					&& transactionResponse.getStatusCode() != 100) {
					sipStackImpl.putDialog(dialog);
					if (transactionResponse.getStatusCode() / 100 == 1)
						dialog.setState(DialogImpl.EARLY_STATE);
					else if (transactionResponse.getStatusCode() / 100 == 2)
						dialog.setState(DialogImpl.CONFIRMED_STATE);
					added = true;
				}

			} else if (
				dialog.getRemoteTag() != null
					&& transactionResponse.getToTag() != null
					&& !dialog.getRemoteTag().equals(
						transactionResponse.getToTag())) {
				String dialogId = transactionResponse.getDialogId(false);
				dialog.setRemoteTag(transactionResponse.getToTag());
				dialog.setDialogId(dialogId);
				if (sipStackImpl.isDialogCreated(method)) {
					sipStackImpl.putDialog(dialog);
					added = true;
				}
			}

			if (sipStackImpl.isDialogCreated(method)) {
				// Make  a final tag assignment.
				if (transactionResponse.getToTag() != null
					&& transactionResponse.getStatusCode() / 100 == 2) {
					// This is a dialog creating method (such as INVITE).
					// 2xx response -- set the state to the confirmed
					// state.
					dialog.setRemoteTag(transactionResponse.getToTag());
					dialog.setState(DialogImpl.CONFIRMED_STATE);
				} else if (
					(transactionResponse.getStatusCode() == 487
						|| transactionResponse.getStatusCode() / 100 == 5
						|| transactionResponse.getStatusCode() / 100 == 6)
						&& (dialog.getState() == null
							|| dialog.getState().getValue()
								== DialogImpl.EARLY_STATE)) {
					// Invite transaction generated an error.
					dialog.setState(DialogImpl.TERMINATED_STATE);
				}
			}

			// Only terminate the dialog on 200 OK response to BYE
			if ( this.getMethod().equals(Request.BYE) && 
				transactionResponse.getStatusCode() == 200 ) {
				dialog.setState(DialogImpl.TERMINATED_STATE);
			}
		}
		try {
			if (isInviteTransaction())
				inviteClientTransaction(transactionResponse, sourceChannel);
			else
				nonInviteClientTransaction(transactionResponse, sourceChannel);
		} catch (IOException ex) {
			this.setState(TransactionState.TERMINATED);
			raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
		}
	}

	/** Implements the state machine for invite client transactions.
	 *<pre>
	 *
	 *                                   |Request from TU
	 *                                   |send request
	 *               Timer E             V
	 *               send request  +-----------+
	 *                   +---------|           |-------------------+
	 *                   |         |  Trying   |  Timer F          |
	 *                   +-------->|           |  or Transport Err.|
	 *                             +-----------+  inform TU        |
	 *                200-699         |  |                         |
	 *                resp. to TU     |  |1xx                      |
	 *                +---------------+  |resp. to TU              |
	 *                |                  |                         |
	 *                |   Timer E        V       Timer F           |
	 *                |   send req +-----------+ or Transport Err. |
	 *                |  +---------|           | inform TU         |
	 *                |  |         |Proceeding |------------------>|
	 *                |  +-------->|           |-----+             |
	 *                |            +-----------+     |1xx          |
	 *                |              |      ^        |resp to TU   |
	 *                | 200-699      |      +--------+             |
	 *                | resp. to TU  |                             |
	 *                |              |                             |
	 *                |              V                             |
	 *                |            +-----------+                   |
	 *                |            |           |                   |
	 *                |            | Completed |                   |
	 *                |            |           |                   |
	 *                |            +-----------+                   |
	 *                |              ^   |                         |
	 *                |              |   | Timer K                 |
	 *                +--------------+   | -                       |
	 *                                   |                         |
	 *                                   V                         |
	 *             NOTE:           +-----------+                   |
	 *                             |           |                   |
	 *         transitions         | Terminated|<------------------+
	 *         labeled with        |           |
	 *         the event           +-----------+
	 *         over the action
	 *         to take
	 *
	 *                 Figure 6: non-INVITE client transaction
	 *</pre>
	 * @param transactionResponse -- transaction response received.
	 * @param sourceChannel - source channel on which the response was received.
	 */
	private void nonInviteClientTransaction(
		SIPResponse transactionResponse,
		MessageChannel sourceChannel)
		throws IOException, SIPServerException {
		int statusCode = transactionResponse.getStatusCode();
		if (TransactionState.TRYING == this.getState()) {
			if (statusCode / 100 == 1) {
				this.setState(TransactionState.PROCEEDING);
				enableRetransmissionTimer(MAXIMUM_RETRANSMISSION_TICK_COUNT);
				enableTimeoutTimer(TIMER_F);
				// According to RFC, the TU has to be informed on 
				// this transition.  Bug report by Emil Ivov
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
			} else if (200 <= statusCode && statusCode <= 699) {
				// Send the response up to the TU.
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
				if (!isReliable()) {
					this.setState(TransactionState.COMPLETED);
					enableTimeoutTimer(TIMER_K);
				} else {
					this.setState(TransactionState.TERMINATED);
				}
			}
		} else if (TransactionState.PROCEEDING == this.getState()) {
			// Bug fixes by Emil Ivov
			if (statusCode / 100 == 1) {
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
			} else if (200 <= statusCode && statusCode <= 699) {
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
				disableRetransmissionTimer();
				disableTimeoutTimer();
				if (!isReliable()) {
					this.setState(TransactionState.COMPLETED);
					enableTimeoutTimer(TIMER_K);
				} else {
					this.setState(TransactionState.TERMINATED);
				}
			}
		} else {
			if (LogWriter.needsLogging) {
				getSIPStack().logWriter.logMessage(
					" Not sending response to TU! " + getState());
			}
		}
	}

	/** Implements the state machine for invite client transactions.
	 *<pre>
	 *
	 *                               |INVITE from TU
	 *             Timer A fires     |INVITE sent
	 *             Reset A,          V                      Timer B fires
	 *             INVITE sent +-----------+                or Transport Err.
	 *               +---------|           |---------------+inform TU
	 *               |         |  Calling  |               |
	 *               +-------->|           |-------------->|
	 *                         +-----------+ 2xx           |
	 *                            |  |       2xx to TU     |
	 *                            |  |1xx                  |
	 *    300-699 +---------------+  |1xx to TU            |
	 *   ACK sent |                  |                     |
	 *resp. to TU |  1xx             V                     |
	 *            |  1xx to TU  -----------+               |
	 *            |  +---------|           |               |
	 *            |  |         |Proceeding |-------------->|
	 *            |  +-------->|           | 2xx           |
	 *            |            +-----------+ 2xx to TU     |
	 *            |       300-699    |                     |
	 *            |       ACK sent,  |                     |
	 *            |       resp. to TU|                     |
	 *            |                  |                     |      NOTE:
	 *            |  300-699         V                     |
	 *            |  ACK sent  +-----------+Transport Err. |  transitions
	 *            |  +---------|           |Inform TU      |  labeled with
	 *            |  |         | Completed |-------------->|  the event
	 *            |  +-------->|           |               |  over the action
	 *            |            +-----------+               |  to take
	 *            |              ^   |                     |
	 *            |              |   | Timer D fires       |
	 *            +--------------+   | -                   |
	 *                               |                     |
	 *                               V                     |
	 *                         +-----------+               |
	 *                         |           |               |
	 *                         | Terminated|<--------------+
	 *                         |           |
	 *                         +-----------+
	 *</pre>
	 * @param transactionResponse -- transaction response received.
	 * @param sourceChannel - source channel on which the response was received.
	 */

	private void inviteClientTransaction(
		SIPResponse transactionResponse,
		MessageChannel sourceChannel)
		throws IOException, SIPServerException {
		int statusCode = transactionResponse.getStatusCode();
		if (TransactionState.TERMINATED == this.getState()) {
			// Do nothing in the terminated state.
			return;
		} else if (TransactionState.CALLING == this.getState()) {
			if (statusCode / 100 == 2) {
				// 200 responses are always seen by TU.
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
				disableRetransmissionTimer();
				disableTimeoutTimer();
				this.setState(TransactionState.TERMINATED);
			} else if (statusCode / 100 == 1) {
				disableRetransmissionTimer();
				disableTimeoutTimer();
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
				this.setState(TransactionState.PROCEEDING);
			} else if (300 <= statusCode && statusCode <= 699) {
				// Send back an ACK request (do this before calling the
				// application (bug noticed by Andreas Bystrom).
				try {
					sendMessage((SIPRequest) createAck());
				} catch (SipException ex) {
					InternalErrorHandler.handleException(ex);
				}
				// When in either the "Calling" or "Proceeding" states,
				// reception of response with status code from 300-699
				// MUST cause the client transaction to 
				// transition to "Completed".
				// The client transaction MUST pass the received response up to
				// the TU, and the client transaction MUST generate an 
				// ACK request.

				if (respondTo != null) respondTo.processResponse(transactionResponse, this);

				if (!isReliable()) {
					this.setState(TransactionState.COMPLETED);
					enableTimeoutTimer(TIMER_D);
				} else {
					//Proceed immediately to the TERMINATED state.
					this.setState(TransactionState.TERMINATED);
				}
			}
		} else if (TransactionState.PROCEEDING == this.getState()) {
			if ( statusCode / 100 == 1) {
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
			} else if (statusCode / 100 == 2) {
				this.setState(TransactionState.TERMINATED);
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
			} else if (300 <= statusCode && statusCode <= 699) {
				// Send back an ACK request
				try {
					sendMessage((SIPRequest) createAck());
				} catch (SipException ex) {
					InternalErrorHandler.handleException(ex);
				}
				// Pass up to the TU for processing.
				if (respondTo != null) respondTo.processResponse(transactionResponse, this);
				if (!isReliable()) {
					this.setState(TransactionState.COMPLETED);
					enableTimeoutTimer(TIMER_D);
				} else {
					this.setState(TransactionState.TERMINATED);
				}
			}
		} else if (TransactionState.COMPLETED == this.getState()) {
			if (300 <= statusCode && statusCode <= 699) {
				// Send back an ACK request
				try {
					sendMessage((SIPRequest) createAck());
				} catch (SipException ex) {
					InternalErrorHandler.handleException(ex);
				}
			}

		}

	}

	/** Sends specified {@link javax.sip.message.Request} on a unique
	 * client transaction identifier. This method implies that the application
	 * is functioning as either a User Agent Client or a Stateful proxy, hence
	 * the underlying SipProvider acts statefully.
	 * <p>
	 * JAIN SIP defines a retransmission utility specific to user agent
	 * behaviour and the default retransmission behaviour for each method.
	 * <p>
	 * When an application wishes to send a message, it creates a Request
	 * message passes that Request to this method, this method returns the
	 * cleintTransactionId generated by the SipProvider. The Request message
	 * gets sent via the ListeningPoint that this SipProvider is attached to.
	 * <ul>
	 * <li>User Agent Client - must not send a BYE on a confirmed INVITE until
	 * it has received an ACK for its 2xx response or until the server
	 * transaction times out.
	 * </ul>
	 *
	 * @throws SipException if implementation cannot send request for any reason
	 */
	public void sendRequest() throws SipException {
		SIPRequest sipRequest = this.getOriginalRequest();
		try {
			// Only map this after the fist request is sent out.
			this.isMapped = true;
			this.sendMessage(sipRequest);
		} catch (IOException ex) {
			throw new SipException(ex.getMessage());
		}

	}

	/**
	 * Called by the transaction stack when a retransmission timer
	 * fires.
	 */
	protected void fireRetransmissionTimer() {

		try {
			// Resend the last request sent
			if (this.getState() == null || !this.isMapped)
				return;
			if (TransactionState.CALLING == this.getState()
				|| TransactionState.TRYING == this.getState()) {
				// If the retransmission filter is disabled then
				// retransmission of the INVITE is the application
				// responsibility.
				if ((!(((SIPTransactionStack) getSIPStack())
					.retransmissionFilter))
					&& this.isInviteTransaction()) {
					raiseErrorEvent(
						SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT);
				} else {
					// Could have allocated the transaction but not yet
					// sent out a request (Bug report by Dave Stuart).
					if (lastRequest != null)
						getMessageChannel().sendMessage(lastRequest);
				}
			}
		} catch (IOException e) {
			this.setState(TransactionState.TERMINATED);
			raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR);
		}

	}

	/**
	 *	Called by the transaction stack when a timeout timer fires.
	 */
	protected void fireTimeoutTimer() {

		if (LogWriter.needsLogging)
			parentStack.logWriter.logMessage("fireTimeoutTimer " + this);

		DialogImpl dialogImpl =  this.dialog;
		if (TransactionState.CALLING == this.getState()
			|| TransactionState.TRYING == this.getState()
			|| TransactionState.PROCEEDING == this.getState()) {
			// Timeout occured. If this is asociated with a transaction
			// creation then kill the dialog.
			if (dialogImpl != null) {
				if (((SIPTransactionStack) getSIPStack())
					.isDialogCreated(this.getOriginalRequest().getMethod())) {
					// terminate the enclosing dialog.
					dialogImpl.setState(DialogImpl.TERMINATED_STATE);
				} else if (
					getOriginalRequest().getMethod().equalsIgnoreCase(
						Request.BYE)) {
					// Terminate the associated dialog on BYE Timeout.
					dialogImpl.setState(DialogImpl.TERMINATED_STATE);
				}
			}
		}
		if (TransactionState.COMPLETED != this.getState()) {
			raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
		} else {
			this.setState(TransactionState.TERMINATED);
		}

	}

	/**
	 * Creates a new Cancel message from the Request associated with this client
	 * transaction. The CANCEL request, is used to cancel the previous request 
	 * sent by this client transaction. Specifically, it asks the UAS to cease 
	 * processing the request and to generate an error response to that request.
	 * 
	 *@return a cancel request generated from the original request.
	 */
	public Request createCancel() throws SipException {
		SIPRequest originalRequest = this.getOriginalRequest();
		if (originalRequest == null) 
			throw new SipException("Bad state " + getState());
		if (originalRequest.getMethod().equalsIgnoreCase(Request.ACK))
			throw new SipException("Cannot Cancel ACK!");
		else
			return originalRequest.createCancelRequest();
	}

	/**
	 * Creates an ACK request for this transaction
	 * 
	 *@return an ack request generated from the original request.
	 *
	 *@throws SipException if transaction is in the wrong state to be acked.
	 */
	public Request createAck() throws SipException {
		SIPRequest originalRequest = this.getOriginalRequest();
		if (originalRequest == null) throw new SipException("bad state " + getState());
		if (getMethod().equalsIgnoreCase(Request.ACK)) {
			throw new SipException("Cannot ACK an ACK!");
		} else if (lastResponse == null) {
			throw new SipException("bad Transaction state");
		} else if (lastResponse.getStatusCode() < 200) {
			if (LogWriter.needsLogging) {
				parentStack.logWriter.logMessage(
					"lastResponse = " + lastResponse);
			}
			throw new SipException("Cannot ACK a provisional response!");
		}
		SIPRequest ackRequest =
			originalRequest.createAckRequest((To) lastResponse.getTo());
		// Pull the record route headers from the last reesponse.
		RecordRouteList recordRouteList = lastResponse.getRecordRouteHeaders();
		if (recordRouteList == null)
			return ackRequest;
		ackRequest.removeHeader(RouteHeader.NAME);
		RouteList routeList = new RouteList();
		// start at the end of the list and walk backwards
		ListIterator li = recordRouteList.listIterator(recordRouteList.size());
		while (li.hasPrevious()) {
			RecordRoute rr = (RecordRoute) li.previous();
			AddressImpl addr = (AddressImpl) rr.getAddress();
			Route route = new Route();
			route.setAddress(
				(AddressImpl) ((AddressImpl) rr.getAddress()).clone());
			route.setParameters((NameValueList) rr.getParameters().clone());
			routeList.add(route);
		}

		Contact contact = null;
		if (lastResponse.getContactHeaders() != null) {
			contact = (Contact) lastResponse.getContactHeaders().getFirst();
		}

		if (!((SipURI) ((Route) routeList.getFirst()).getAddress().getURI())
			.hasLrParam()) {

			// Contact may not yet be there (bug reported by Andreas B).

			Route route = null;
			if (contact != null) {
				route = new Route();
				route.setAddress(
					(AddressImpl) ((AddressImpl) (contact.getAddress()))
						.clone());
			}

			Route firstRoute = (Route) routeList.getFirst();
			routeList.removeFirst();
			javax.sip.address.URI uri = firstRoute.getAddress().getURI();
			ackRequest.setRequestURI(uri);

			if (route != null)
				routeList.add(route);

			ackRequest.addHeader(routeList);
		} else {
			if (contact != null) {
				javax.sip.address.URI uri =
				(javax.sip.address.URI) contact.getAddress().getURI().clone();
				ackRequest.setRequestURI(uri);
				ackRequest.addHeader(routeList);
			}
		}
		return ackRequest;

	}

	/** Set the port of the recipient.
	 */
	protected void setViaPort(int port) {
		this.viaPort = port;
	}

	/** Set the port of the recipient.
	 */
	protected void setViaHost(String host) {
		this.viaHost = host;
	}

	/** Get the port of the recipient.
	 */
	public int getViaPort() {
		return this.viaPort;
	}

	/** Get the host of the recipient.
	 */
	public String getViaHost() {
		return this.viaHost;
	}

	/** get the via header for an outgoing request.
	 */
	public Via getOutgoingViaHeader() {
		return this.getMessageProcessor().getViaHeader();
	}

	public boolean isSecure() {
		return encapsulatedChannel.isSecure();
	}

	/** This is called by the stack after a non-invite client
	* transaction goes to completed state.
	*/
	public void clearState() {
			// reduce the state to minimum
			// This assumes that the application will not need
			// to access the request once the transaction is 
			// completed. 
			this.lastRequest = null;
			this.originalRequest = null;
			this.lastResponse = null;
	}

	/** Sets a timeout after which the connection is closed (provided the server does not
	* use the connection for outgoing requests in this time period) 
	* and  calls the superclass to set state.
	*/
	public void setState (TransactionState newState) {
			// Set this timer for connection caching
			// of incoming connections. 
			if ( newState == TransactionState.TERMINATED && 
				this.isReliable() && 
			    ( ! getSIPStack().cacheClientConnections )  ){
			    // Set a time after which the connection
			    // is closed.
			    this.collectionTime = TIMER_J;
			}
			super.setState(newState);
	}

	/** Run any pending responses - gets called at the end of the event loop.
	*/
	public synchronized void processPendingResponses( ) {
		if (pendingResponses.isEmpty() ) return;
		try {
		    PendingResponse pr = 
				(PendingResponse) this.pendingResponses.removeFirst();
		    this.processResponse(pr.sipResponse,pr.messageChannel);
		} catch (SIPServerException ex) { 
			// Should never happen.
			ex.printStackTrace();
		}
	}

	public synchronized boolean hasResponsesPending() {
		return !pendingResponses.isEmpty();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.23  2004/04/09 11:51:26  mranga
 * Reviewed by:   mranga
 * Limit size of pending buffer to thwart response flooding attack.
 *
 * Revision 1.22  2004/04/07 00:19:23  mranga
 * Reviewed by:   mranga
 * Fixes a potential race condition for client transactions.
 * Handle re-invites statefully within an established dialog.
 *
 * Revision 1.21  2004/04/06 01:19:00  mranga
 * Reviewed by:   mranga
 * suppress 100 if invite client transaction is in the Proceeding state
 *
 * Revision 1.20  2004/03/09 00:34:44  mranga
 * Reviewed by:   mranga
 * Added TCP connection management for client and server side
 * Transactions. See configuration parameter
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false
 * Releases Client TCP Connections after linger time
 *
 * Revision 1.19  2004/03/07 22:25:24  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.18  2004/02/24 22:39:34  mranga
 * Reviewed by:   mranga
 * Only terminate the client side dialog when the bye Terminates or times out
 * and not when the bye is initially sent out.
 *
 * Revision 1.17  2004/02/13 13:55:32  mranga
 * Reviewed by:   mranga
 * per the spec, Transactions must always have a valid dialog pointer. Assigned a dummy dialog for transactions that are not assigned to any dialog (such as Message).
 *
 * Revision 1.16  2004/01/25 16:06:24  mranga
 * Reviewed by:   M. Ranganathan
 *
 * Clean up setting state (Use TransactionState instead of integer). Convert to UNIX file format.
 * Remove extraneous methods.
 *
 * Revision 1.15  2004/01/23 18:26:10  mranga
 * Reviewed by:   M. Ranganathan
 * Check for presence of contact header when creating ACK for a transaction.
 *
 * Revision 1.14  2004/01/22 20:15:32  mranga
 * Reviewed by:  mranga
 * Fixed a possible race condition in  nulling out the transaction Request (earlier added for scalability).
 *
 * Revision 1.13  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.12  2004/01/22 14:23:45  mranga
 * Reviewed by:   mranga
 * Fixed some minor formatting issues.
 *
 * Revision 1.11  2004/01/22 13:26:33  sverker
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
