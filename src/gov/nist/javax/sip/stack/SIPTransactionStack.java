package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import javax.sip.message.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;

import java.util.*;
import java.io.IOException;
import java.net.*;

//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/

/**
 * Adds a transaction layer to the {@link SIPStack} class.  This is done by
 * replacing the normal MessageChannels returned by the base class with
 * transaction-aware MessageChannels that encapsulate the original channels
 * and handle the transaction state machine, retransmissions, etc.
 *
 * @author Jeff Keyser (original) 
 * @author M. Ranganathan <mranga@nist.gov>  <br/> (Added Dialog table).
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.16 $ $Date: 2004-01-22 18:39:41 $
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public abstract class SIPTransactionStack
	extends SIPStack
	implements SIPTransactionEventListener {

	/**
	 * Number of milliseconds between timer ticks (500).
	 **/
	public static final int BASE_TIMER_INTERVAL = 500;

	// Collection of current client transactions
	private Set clientTransactions;
	// Collection or current server transactions
	private Set serverTransactions;
	// Table of dialogs.
	private Hashtable dialogTable;

	// Max number of server transactions concurrent.
	protected int transactionTableSize;

	// A table of assigned dialogs.

	// Retransmissio{n filter - indicates the stack will retransmit 200 OK
	// for invite transactions.
	protected boolean retransmissionFilter;

	// A set of methods that result in dialog creations.
	protected HashSet dialogCreatingMethods;

	private int activeClientTransactionCount;
	private int activeServerTransactionCount;

	/**
	 *	Default constructor.
	 */
	protected SIPTransactionStack() {
		super();
		this.transactionTableSize = -1;
		// a set of methods that result in dialog creation.
		this.dialogCreatingMethods = new HashSet();
		// Standard set of methods that create dialogs.
		this.dialogCreatingMethods.add(Request.REFER);
		this.dialogCreatingMethods.add(Request.INVITE);
		this.dialogCreatingMethods.add(Request.SUBSCRIBE);
		// Notify may or may not create a dialog. This is handled in 
		// the code.
		this.dialogCreatingMethods.add(Request.MESSAGE);
		// Create the transaction collections
		clientTransactions = Collections.synchronizedSet(new HashSet());
		serverTransactions = Collections.synchronizedSet(new HashSet());
		// Dialog dable.
		this.dialogTable = new Hashtable();
		// Start the timer event thread.
//ifdef SIMULATION
/*
		SimThread simThread =	new SimThread( new TransactionScanner( ));
		simThread.setName("TransactionScanner");
		simThread.start();
//else
*/
		new Thread(new TransactionScanner()).start();
//endif
//

	}

	/**
	 * Return true if extension is supported.
	 *
	 * @return true if extension is supported and false otherwise.
	 */
	public boolean isDialogCreated(String method) {
		return dialogCreatingMethods.contains(method.toUpperCase());
	}

	/**
	 * Add an extension method.
	 *
	 * @param extensionMethod -- extension method to support for dialog
	 * creation
	 */
	public void addExtensionMethod(String extensionMethod) {
		if (extensionMethod.equals(Request.NOTIFY)) {
			if (LogWriter.needsLogging)
				logWriter.logMessage("NOTIFY Supported Natively");
		} else {
			this.dialogCreatingMethods.add(
				extensionMethod.trim().toUpperCase());
		}
	}

	/**
	 * Put a dialog into the dialog table.
	 *
	 * @param dialog -- dialog to put into the dialog table.
	 *
	 */
	public void putDialog(DialogImpl dialog) {
		String dialogId = dialog.getDialogId();
		synchronized (dialogTable) {
			if (dialogTable.containsKey(dialogId))
				return;
		}
		if (LogWriter.needsLogging) {
			logWriter.logMessage("putDialog dialogId=" + dialogId);
		}
		// if (this.getDefaultRouteHeader() != null)
		//   dialog.addRoute(this.getDefaultRouteHeader(),false);
		dialog.setStack(this);
		if (LogWriter.needsLogging)
			logWriter.logStackTrace();
		synchronized (dialogTable) {
			dialogTable.put(dialogId, dialog);
		}

	}

	public synchronized DialogImpl createDialog(SIPTransaction transaction) {
		SIPRequest sipRequest = transaction.getOriginalRequest();

		DialogImpl retval = new DialogImpl(transaction);

		return retval;

	}

	/**
	 * Return the dialog for a given dialog ID. If compatibility is
	 * enabled then we do not assume the presence of tags and hence
	 * need to add a flag to indicate whether this is a server or
	 * client transaction.
	 *
	 * @param dialogId is the dialog id to check.
	 */

	public DialogImpl getDialog(String dialogId) {
		if (LogWriter.needsLogging)
			logWriter.logMessage("Getting dialog for " + dialogId);
		synchronized (dialogTable) {
			return (DialogImpl) dialogTable.get(dialogId);
		}
	}

	/**
	 * Find a matching client SUBSCRIBE to the incoming notify.
	 * NOTIFY requests are matched to such SUBSCRIBE requests if they
	 * contain the same "Call-ID", a "To" header "tag" parameter which
	 * matches the "From" header "tag" parameter of the SUBSCRIBE, and the
	 * same "Event" header field.  Rules for comparisons of the "Event"
	 * headers are described in section 7.2.1.  If a matching NOTIFY request
	 * contains a "Subscription-State" of "active" or "pending", it creates
	 * a new subscription and a new dialog (unless they have already been
	 * created by a matching response, as described above).
	 *
	 * @param notifyMessage
	 */
	public SIPClientTransaction findSubscribeTransaction(SIPRequest notifyMessage) {
		synchronized (clientTransactions) {
			Iterator it = clientTransactions.iterator();
			String thisToTag = notifyMessage.getTo().getTag();
			if (thisToTag == null)
				return null;
			Event eventHdr = (Event) notifyMessage.getHeader(EventHeader.NAME);
			if (eventHdr == null)
				return null;
			while (it.hasNext()) {
				SIPClientTransaction ct = (SIPClientTransaction) it.next();
				//SIPRequest sipRequest = ct.getOriginalRequest();
				String fromTag = ct.from.getTag();
				Event hisEvent = ct.event;
				// Event header is mandatory but some slopply clients
				// dont include it.
				if (hisEvent == null)
					continue;
				if (ct.method.equals(Request.SUBSCRIBE)
					&& fromTag.equalsIgnoreCase(thisToTag)
					&& hisEvent != null
					&& eventHdr.match(hisEvent)
					&& notifyMessage.getCallId().getCallId().equalsIgnoreCase(
						ct.callId.getCallId()))
					return ct;
			}

		}
		return null;
	}

	/**
	 * Find the transaction corresponding to a given request.
	 *
	 * @param sipMessage request for which to retrieve the transaction.
	 *
	 * @param isServer search the server transaction table if true.
	 *
	 * @return the transaction object corresponding to the request or null
	 * if no such mapping exists.
	 */
	public SIPTransaction findTransaction(
		SIPMessage sipMessage,
		boolean isServer) {

		if (isServer) {
			if (LogWriter.needsLogging)
				logWriter.logMessage(
					"searching server transaction for "
						+ sipMessage
						+ " size =  "
						+ this.serverTransactions.size());
			synchronized (this.serverTransactions) {
				Iterator it = serverTransactions.iterator();
				while (it.hasNext()) {
					SIPServerTransaction sipServerTransaction =
						(SIPServerTransaction) it.next();
					if (sipServerTransaction
						.isMessagePartOfTransaction(sipMessage))
						return sipServerTransaction;
				}
			}
		} else {
			synchronized (this.clientTransactions) {
				Iterator it = clientTransactions.iterator();
				while (it.hasNext()) {
					SIPClientTransaction clientTransaction =
						(SIPClientTransaction) it.next();
					if (clientTransaction
						.isMessagePartOfTransaction(sipMessage))
						return clientTransaction;
				}
			}

		}
		return null;

	}

	/**
	 * Get the transaction to cancel. Search the server transaction
	 * table for a transaction that matches the given transaction.
	 */
	public SIPTransaction findCancelTransaction(
		SIPRequest cancelRequest,
		boolean isServer) {

		if (LogWriter.needsLogging) {
			logWriter.logMessage(
				"findCancelTransaction request= \n"
					+ cancelRequest
					+ "\nfindCancelRequest isServer="
					+ isServer);
		}

		if (isServer) {
			synchronized (this.serverTransactions) {
				Iterator li = this.serverTransactions.iterator();
				while (li.hasNext()) {
					SIPTransaction transaction = (SIPTransaction) li.next();
					SIPRequest sipRequest =
						(SIPRequest) (transaction.getRequest());

					SIPServerTransaction sipServerTransaction =
						(SIPServerTransaction) transaction;
					if (sipServerTransaction
						.doesCancelMatchTransaction(cancelRequest))
						return sipServerTransaction;
				}
			}
		} else {
			synchronized (this.clientTransactions) {
				Iterator li = this.clientTransactions.iterator();
				while (li.hasNext()) {
					SIPTransaction transaction = (SIPTransaction) li.next();
					SIPRequest sipRequest =
						(SIPRequest) (transaction.getRequest());

					SIPClientTransaction sipClientTransaction =
						(SIPClientTransaction) transaction;
					if (sipClientTransaction
						.doesCancelMatchTransaction(cancelRequest))
						return sipClientTransaction;

				}
			}
		}
		return null;
	}

	/**
	 * Construcor for the stack. Registers the request and response
	 * factories for the stack.
	 *
	 * @param messageFactory User-implemented factory for processing
	 *	messages.
	 */
	protected SIPTransactionStack(SIPStackMessageFactory messageFactory) {
		this();
		super.sipMessageFactory = messageFactory;
	}

	/**
	 *	Thread used to throw timer events for all transactions.
	 */
	class TransactionScanner implements Runnable {
		private int prevSTCount;
		private int prevCTCount;
		private int scanCount;

		public void run() {

			// Iterator through all transactions
			Iterator transactionIterator;
			// One transaction in the set
			SIPTransaction nextTransaction;

			// Loop while this stack is running
			while (isAlive()) {

				try {

					// Sleep for one timer "tick"
//ifndef SIMULATION
//
					Thread.sleep(BASE_TIMER_INTERVAL);
//else
/*
					SimThread.sleep((double) BASE_TIMER_INTERVAL);
//endif
*/

					// Check all client transactions

					LinkedList fireList = new LinkedList();

					activeServerTransactionCount = 0;
					activeClientTransactionCount = 0;

					// Check all server transactions
					synchronized (serverTransactions) {
						transactionIterator = serverTransactions.iterator();
						while (transactionIterator.hasNext()) {

							nextTransaction =
								(SIPTransaction) transactionIterator.next();

							// If the transaction has terminated,
							if (nextTransaction.isTerminated()) {
								// Keep the transaction hanging around
								// to catch the incoming ACK.
								// BUG report from Antonis Karydas
								if (((SIPServerTransaction) nextTransaction)
									.collectionTime
									== 0) {
									// Remove it from the set
									if (LogWriter.needsLogging)
										logWriter.logMessage(
											"removing" + nextTransaction);
									transactionIterator.remove();
								} else {
									(
										(
											SIPServerTransaction) nextTransaction)
												.collectionTime--;
								}
								// If this transaction has not
								//terminated,
							} else {
								// Add to the fire list -- needs to be moved
								// outside the synchronized block to prevent
								// deadlock.
								fireList.add(nextTransaction);
								activeServerTransactionCount++;

							}

						}
					}

					synchronized (clientTransactions) {
						transactionIterator = clientTransactions.iterator();
						while (transactionIterator.hasNext()) {

							nextTransaction =
								(SIPTransaction) transactionIterator.next();

							// If the transaction has terminated,
							if (nextTransaction.isTerminated()) {

								transactionIterator.remove();
								// Remove it from the set
								if (LogWriter.needsLogging) {
									logWriter.logMessage(
										"Removing clientTransaction "
											+ nextTransaction
											+ " tableSize "
											+ clientTransactions.size());
								}

								// If this transaction has not
								// terminated,
							} else {
								// Add to the fire list -- needs to be moved
								// outside the synchronized block to prevent
								// deadlock. 
								fireList.add(nextTransaction);
								if (nextTransaction.getState() == null
									|| !nextTransaction.getState().equals(
										TransactionState.COMPLETED))
									activeClientTransactionCount++;

							}
						}
					}
					prevSTCount = prevSTCount + activeServerTransactionCount;
					prevCTCount = prevCTCount + activeClientTransactionCount;
					this.scanCount++;

//ifdef SIMULATION
/*
					 System.out.println
				   	( stackName + " tsize " + activeClientTransactionCount );
//endif
*/

					synchronized (dialogTable) {
						Collection values = dialogTable.values();
						Iterator iterator = values.iterator();
						while (iterator.hasNext()) {
							DialogImpl d = (DialogImpl) iterator.next();
							// System.out.println("dialogState = " +
							//	d.getState() + 
							//	" isServer = " +  d.isServer());
							if (d.getState() != null
								&& d.getState().getValue()
									== DialogImpl.TERMINATED_STATE) {
								if (LogWriter.needsLogging) {
									String dialogId = d.getDialogId();
									logWriter.logMessage(
										"Removing Dialog " + dialogId);
								}
								iterator.remove();
							}
							// If I ACK has not been seen on Dialog,
							// resend last response.
							if (d.isServer()
								&& (!d.ackSeen)
								&& d.isInviteDialog()) {
								SIPTransaction transaction =
									d.getLastTransaction();
								// If stack is managing the transaction
								// then retransmit the last response.
								if (TransactionState.TERMINATED
									== transaction.getState()
									&& (
										(
											SIPServerTransaction) transaction)
												.isMapped) {
									SIPResponse response =
										transaction.getLastResponse();
									// Retransmit to 200 until ack received.
									if (response.getStatusCode() == 200) {
										try {
											// If retransmission filter is
											// enabled, send the last response.
											if (retransmissionFilter
												&& d.toRetransmitFinalResponse())
												transaction.sendMessage(
													response);
										} catch (IOException ex) {
											/* Will eventully time out */
											d.setState(
												DialogImpl.TERMINATED_STATE);
										} finally {
											// Need to fire the timer so
											// transaction will eventually
											// time out whether or not
											// the IOException occurs 
											// (bug fix sent in
											// by Christophe).
											fireList.add(transaction);
										}
									}
								}
							}
						}
					}

					transactionIterator = fireList.iterator();
					while (transactionIterator.hasNext()) {
						nextTransaction =
							(SIPTransaction) transactionIterator.next();
						nextTransaction.fireTimer();
					}

				} catch (InterruptedException e) {

					// Ignore

				}

			}

		}

	}

	/**
	 * Handles a new SIP request.
	 * It finds a server transaction to handle
	 * this message.  If none exists, it creates a new transaction.
	 *
	 * @param requestReceived		Request to handle.
	 * @param requestMessageChannel	Channel that received message.
	 *
	 * @return A server transaction.
	 */
	protected SIPServerRequestInterface newSIPServerRequest(
		SIPRequest requestReceived,
		MessageChannel requestMessageChannel) {

		// Iterator through all server transactions
		Iterator transactionIterator;
		// Next transaction in the set
		SIPServerTransaction nextTransaction;
		// Transaction to handle this request
		SIPServerTransaction currentTransaction;

		// Loop through all server transactions
		synchronized (serverTransactions) {
			transactionIterator = serverTransactions.iterator();
			currentTransaction = null;
			while (transactionIterator.hasNext()
				&& currentTransaction == null) {

				nextTransaction =
					(SIPServerTransaction) transactionIterator.next();

				// If this transaction should handle this request,
				if (nextTransaction
					.isMessagePartOfTransaction(requestReceived)) {
					// Mark this transaction as the one
					// to handle this message
					currentTransaction = nextTransaction;
				}
			}

			// If no transaction exists to handle this message
			if (currentTransaction == null) {
				currentTransaction =
					createServerTransaction(requestMessageChannel);
				currentTransaction.setOriginalRequest(requestReceived);
				if (!isDialogCreated(requestReceived.getMethod())) {
					// Dialog is not created - can we find the state?
					// If so, then create a transaction and add it.
					String dialogId = requestReceived.getDialogId(true);
					DialogImpl dialog = getDialog(dialogId);
					if (dialog != null) {
						// Found a dialog.
						serverTransactions.add(currentTransaction);
						currentTransaction.isMapped = true;
					}
				} else {
					// Create the transaction but dont map it.
					currentTransaction.setOriginalRequest(requestReceived);
				}
			}

			// Set ths transaction's encapsulated request
			// interface from the superclass
			currentTransaction.setRequestInterface(
				super.newSIPServerRequest(requestReceived, currentTransaction));
			return currentTransaction;
		}

	}

	/**
	 * Handles a new SIP response.
	 * It finds a client transaction to handle
	 * this message.  If none exists, it sends the message directly to the
	 * superclass.
	 *
	 *	@param responseReceived			Response to handle.
	 *	@param responseMessageChannel	Channel that received message.
	 *
	 *	@return A client transaction.
	 */
	protected SIPServerResponseInterface newSIPServerResponse(
		SIPResponse responseReceived,
		MessageChannel responseMessageChannel) {
		//	System.out.println("response = " + responseReceived.encode());

		// Iterator through all client transactions
		Iterator transactionIterator;
		// Next transaction in the set
		SIPClientTransaction nextTransaction;
		// Transaction to handle this request
		SIPClientTransaction currentTransaction;

		// Loop through all server transactions
		synchronized (clientTransactions) {
			transactionIterator = clientTransactions.iterator();
			currentTransaction = null;
			while (transactionIterator.hasNext()
				&& currentTransaction == null) {

				nextTransaction =
					(SIPClientTransaction) transactionIterator.next();

				// If this transaction should handle this request,
				if (nextTransaction
					.isMessagePartOfTransaction(responseReceived)) {

					// Mark this transaction as the one to
					// handle this message
					currentTransaction = nextTransaction;

				}

			}
		}

		// If no transaction exists to handle this message,
		if (currentTransaction == null) {

			// Pass the message directly to the TU
			return super.newSIPServerResponse(
				responseReceived,
				responseMessageChannel);

		}

		// Set ths transaction's encapsulated response interface
		// from the superclass
		currentTransaction.setResponseInterface(
			super.newSIPServerResponse(responseReceived, currentTransaction));
		return currentTransaction;

	}

	/**
	 * Creates a client transaction to handle a new request.
	 * Gets the real
	 * message channel from the superclass, and then creates a new client
	 * transaction wrapped around this channel.
	 *
	 *	@param nextHop	Hop to create a channel to contact.
	 */
	public MessageChannel createMessageChannel(Hop nextHop)
		throws UnknownHostException {
		synchronized (clientTransactions) {
			// New client transaction to return
			SIPTransaction returnChannel;

			// Create a new client transaction around the
			// superclass' message channel
			MessageChannel mc = super.createMessageChannel(nextHop);

			// Superclass will return null if no message processor
			// available for the transport.
			if (mc == null)
				return null;

			returnChannel = createClientTransaction(mc);
			clientTransactions.add(returnChannel);
			((SIPClientTransaction) returnChannel).setViaPort(
				nextHop.getPort());
			((SIPClientTransaction) returnChannel).setViaHost(
				nextHop.getHost());
			return returnChannel;
		}

	}

	/** Create a client transaction from a raw channel.
	 *
	 *@param rawChannel is the transport channel to encapsulate.
	 */

	public MessageChannel createMessageChannel(MessageChannel rawChannel) {
		synchronized (clientTransactions) {
			// New client transaction to return
			SIPTransaction returnChannel = createClientTransaction(rawChannel);
			clientTransactions.add(returnChannel);
			((SIPClientTransaction) returnChannel).setViaPort(
				rawChannel.getViaPort());
			((SIPClientTransaction) returnChannel).setViaHost(
				rawChannel.getHost());
			return returnChannel;
		}
	}

	/**
	 * Create a client transaction from a raw channel.
	 *
	 * @param transaction is the transport channel to encapsulate.
	 */
	public MessageChannel createMessageChannel(SIPTransaction transaction) {
		synchronized (clientTransactions) {
			// New client transaction to return
			SIPTransaction returnChannel =
				createClientTransaction(transaction.getMessageChannel());
			clientTransactions.add(returnChannel);
			((SIPClientTransaction) returnChannel).setViaPort(
				transaction.getViaPort());
			((SIPClientTransaction) returnChannel).setViaHost(
				transaction.getViaHost());
			return returnChannel;
		}
	}

	/**
	 * Creates a client transaction that encapsulates a MessageChannel.
	 * Useful for implementations that want to subclass the standard
	 *
	 * @param encapsulatedMessageChannel Message channel of the transport layer.
	 */
	public SIPClientTransaction createClientTransaction(MessageChannel encapsulatedMessageChannel) {
		return new SIPClientTransaction(this, encapsulatedMessageChannel);
	}

	/**
	 * Creates a server transaction that encapsulates a MessageChannel.
	 * Useful for implementations that want to subclass the standard
	 *
	 * @param encapsulatedMessageChannel Message channel of the transport layer.
	 */
	public SIPServerTransaction createServerTransaction(MessageChannel encapsulatedMessageChannel) {
		return new SIPServerTransaction(this, encapsulatedMessageChannel);
	}

	/**
	 * Creates a raw message channel. A raw message channel has no
	 * transaction wrapper.
	 *
	 * @param hop -- hop for which to create the raw message channel.
	 */
	public MessageChannel createRawMessageChannel(Hop hop)
		throws UnknownHostException {
		return super.createMessageChannel(hop);
	}

	/**
	 * Add a new client transaction to the set of existing transactions.
	 *
	 * @param clientTransaction -- client transaction to add to the set.
	 */
	public void addTransaction(SIPClientTransaction clientTransaction) {
		synchronized (clientTransactions) {
			clientTransactions.add(clientTransaction);
		}
	}

	/**
	 * Add a new client transaction to the set of existing transactions.
	 *
	 * @param serverTransaction -- server transaction to add to the set.
	 */
	public void addTransaction(SIPServerTransaction serverTransaction)
		throws IOException {
		synchronized (serverTransactions) {
			this.serverTransactions.add(serverTransaction);
			serverTransaction.map();
		}
	}

	public boolean hasResources() {
		if (transactionTableSize == -1)
			return true;
		else {
			return serverTransactions.size() < transactionTableSize;
		}
	}

	/**
	 * Invoked when an error has ocurred with a transaction.
	 *
	 * @param transactionErrorEvent Error event.
	 */
	public synchronized void transactionErrorEvent(SIPTransactionErrorEvent transactionErrorEvent) {
		SIPTransaction transaction =
			(SIPTransaction) transactionErrorEvent.getSource();
		// TODO
		if (transactionErrorEvent.getErrorID()
			== SIPTransactionErrorEvent.TRANSPORT_ERROR) {
			// Kill scanning of this transaction.
			transaction.setState(SIPTransaction.TERMINATED_STATE);
			if (transaction instanceof SIPServerTransaction) {
				// let the reaper get him
				 ((SIPServerTransaction) transaction).collectionTime = 0;
			}
			transaction.disableTimeoutTimer();
			transaction.disableRetransmissionTimer();
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.15  2004/01/22 14:23:45  mranga
 * Reviewed by:   mranga
 * Fixed some minor formatting issues.
 *
 * Revision 1.14  2004/01/22 13:26:33  sverker
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
