package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import javax.sip.message.*;
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
 * Adds a transaction layer to the {@link SIPMessageStack}class. This is done
 * by replacing the normal MessageChannels returned by the base class with
 * transaction-aware MessageChannels that encapsulate the original channels and
 * handle the transaction state machine, retransmissions, etc.
 * 
 * @author Jeff Keyser (original)
 * @author M. Ranganathan <mranga@nist.gov><br/>(Added Dialog table).
 * @author performance enhacements added by Pierre De Rop and Thomas Froment.
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.46 $ $Date: 2004-10-31 02:19:08 $ <a
 *          href=" {@docRoot}/uncopyright.html">This code is in the public
 *          domain. </a>
 */
public abstract class SIPTransactionStack extends SIPMessageStack implements
		SIPTransactionEventListener {

	/**
	 * Number of milliseconds between timer ticks (500).
	 */
	public static final int BASE_TIMER_INTERVAL = 500;

	/**
	 * Connection linger time (seconds)
	 */
	public static final int CONNECTION_LINGER_TIME = 32;

	// Collection of current client transactions
	private List clientTransactions;

	// Collection or current server transactions
	private List serverTransactions;

	// Table of dialogs.
	protected Hashtable dialogTable;

	// Max number of server transactions concurrent.
	protected int transactionTableSize;

	// Retransmissio{n filter - indicates the stack will retransmit 200 OK
	// for invite transactions.
	protected boolean retransmissionFilter;

	// A set of methods that result in dialog creations.
	protected HashSet dialogCreatingMethods;

	private int activeClientTransactionCount;

	private int activeServerTransactionCount;

	protected Timer timer;

	protected Thread pendingRecordScanner;

	/** List of pending dialog creating transactions. */
	private HashSet pendingTransactions;

	protected List pendingRecords;

	// hashtable for fast lookup
	private Hashtable clientTransactionTable;

	private Hashtable serverTransactionTable;

	/**
	 * Default constructor.
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
		// Create the transaction collections

		clientTransactions = Collections.synchronizedList(new ArrayList());
		serverTransactions = Collections.synchronizedList(new ArrayList());
		// Dialog dable.
		this.dialogTable = new Hashtable();

		clientTransactionTable = new Hashtable();
		serverTransactionTable = new Hashtable();

		// Start the timer event thread.
		//ifdef SIMULATION
		/*
		 * SimThread simThread = new SimThread( new TransactionScanner(this));
		 * simThread.setName("TransactionScanner"); simThread.start(); //else
		 */

		this.timer = new Timer();
		this.pendingRecordScanner = new Thread(new PendingRecordScanner(this));
		this.pendingRecordScanner.setDaemon(true);
		this.pendingTransactions = new HashSet();
		pendingRecords = Collections.synchronizedList(new ArrayList());
		pendingRecordScanner.setName("PendingRecordScanner");
		pendingRecordScanner.start();
		//endif
		//

	}

	/**
	 * Re Initialize the stack instance.
	 */
	protected void reInit() {
		super.reInit();
		clientTransactions = Collections.synchronizedList(new ArrayList());
		serverTransactions = Collections.synchronizedList(new ArrayList());
		pendingTransactions =  new HashSet();
		pendingRecords = Collections.synchronizedList(new ArrayList());
		clientTransactionTable = new Hashtable();
		serverTransactionTable = new Hashtable();
		// Dialog dable.
		this.dialogTable = new Hashtable();

		// Start the timer event thread.
		//ifdef SIMULATION
		/*
		 * SimThread simThread = new SimThread( new TransactionScanner(this));
		 * simThread.setName("TransactionScanner"); simThread.start(); //else
		 */
		this.timer = new Timer();
		pendingRecordScanner = new Thread(new PendingRecordScanner(this));
		pendingRecordScanner.setDaemon(true);
		pendingRecordScanner.setName("PendingRecordScanner");
		pendingRecordScanner.start();
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
	 * @param extensionMethod --
	 *            extension method to support for dialog creation
	 */
	public void addExtensionMethod(String extensionMethod) {
		if (extensionMethod.equals(Request.NOTIFY)) {
			if (LogWriter.needsLogging)
				logWriter.logMessage("NOTIFY Supported Natively");
		} else {
			this.dialogCreatingMethods
					.add(extensionMethod.trim().toUpperCase());
		}
	}

	/**
	 * Put a dialog into the dialog table.
	 * 
	 * @param dialog --
	 *            dialog to put into the dialog table.
	 *  
	 */
	public void putDialog(SIPDialog dialog) {
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

	public SIPDialog createDialog(SIPTransaction transaction) {
		SIPRequest sipRequest = transaction.getOriginalRequest();

		SIPDialog retval = new SIPDialog(transaction);

		return retval;

	}

	/**
	 * This is for debugging.
	 */
	public Iterator getDialogs() {
		return dialogTable.values().iterator();

	}

	/**
	 * Remove the dialog from the dialog table.
	 * 
	 * @param dialog --
	 *            dialog to remove.
	 */
	public void removeDialog(SIPDialog dialog) {
		synchronized (dialogTable) {
			Iterator it = this.dialogTable.values().iterator();
			while (it.hasNext()) {
				SIPDialog d = (SIPDialog) it.next();
				if (d == dialog) {
					if (LogWriter.needsLogging) {
						String dialogId = dialog.getDialogId();
						logWriter.logMessage("Removing Dialog " + dialogId);
					}
					it.remove();
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
		if (LogWriter.needsLogging)
			logWriter.logMessage("Getting dialog for " + dialogId);
		synchronized (dialogTable) {
			return (SIPDialog) dialogTable.get(dialogId);
		}
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
	 */
	public SIPClientTransaction findSubscribeTransaction(
			SIPRequest notifyMessage) {
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
						&& notifyMessage.getCallId().getCallId()
								.equalsIgnoreCase(ct.callId.getCallId()))
					return ct;
			}

		}
		return null;
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

		if (isServer) {
			Via via = sipMessage.getTopmostVia();
			if (via.getBranch() != null) {
				String key = sipMessage.getTransactionId();

				synchronized (this.serverTransactionTable) {
					retval = (SIPTransaction) serverTransactionTable.get(key);
					if (LogWriter.needsLogging)
						logMessage("looking for key " + key);
					if (retval != null
							&& retval.isMessagePartOfTransaction(sipMessage))
						return retval;
				}
			}
			// Need to scan the table for old style transactions (RFC 2543
			// style)
			synchronized (this.serverTransactions) {
				Iterator it = serverTransactions.iterator();
				while (it.hasNext()) {
					SIPServerTransaction sipServerTransaction = (SIPServerTransaction) it
							.next();
					if (sipServerTransaction
							.isMessagePartOfTransaction(sipMessage))
						return sipServerTransaction;
				}
			}
		} else {
			Via via = sipMessage.getTopmostVia();
			if (via.getBranch() != null) {
				String key = sipMessage.getTransactionId();
				synchronized (this.clientTransactionTable) {
					retval = (SIPTransaction) clientTransactionTable.get(key);
					if (retval != null
							&& retval.isMessagePartOfTransaction(sipMessage))
						return retval;
				}
			}
			// Need to scan the table for old style transactions (RFC 2543
			// style)
			synchronized (this.clientTransactions) {
				Iterator it = clientTransactions.iterator();
				while (it.hasNext()) {
					SIPClientTransaction clientTransaction = (SIPClientTransaction) it
							.next();
					if (clientTransaction
							.isMessagePartOfTransaction(sipMessage))
						return clientTransaction;
				}
			}

		}
		return null;

	}

	/**
	 * Get the transaction to cancel. Search the server transaction table for a
	 * transaction that matches the given transaction.
	 */
	public SIPTransaction findCancelTransaction(SIPRequest cancelRequest,
			boolean isServer) {

		if (LogWriter.needsLogging) {
			logWriter.logMessage("findCancelTransaction request= \n"
					+ cancelRequest + "\nfindCancelRequest isServer="
					+ isServer);
		}

		if (isServer) {
			synchronized (this.serverTransactions) {
				Iterator li = this.serverTransactions.iterator();
				while (li.hasNext()) {
					SIPTransaction transaction = (SIPTransaction) li.next();
					SIPRequest sipRequest = (SIPRequest) (transaction
							.getRequest());

					SIPServerTransaction sipServerTransaction = (SIPServerTransaction) transaction;
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
					SIPRequest sipRequest = (SIPRequest) (transaction
							.getRequest());

					SIPClientTransaction sipClientTransaction = (SIPClientTransaction) transaction;
					if (sipClientTransaction
							.doesCancelMatchTransaction(cancelRequest))
						return sipClientTransaction;

				}
			}
		}
		if (LogWriter.needsLogging) 
		    logWriter.logMessage("Could not find transaction for cancel request");
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
		super.sipMessageFactory = messageFactory;
	}

	public SIPServerTransaction findPendingTransaction(
			SIPRequest requestReceived) {
		SIPServerTransaction currentTransaction;
		Iterator transactionIterator;
		synchronized (pendingTransactions) {
			transactionIterator = pendingTransactions.iterator();
			currentTransaction = null;
			while (transactionIterator.hasNext() && currentTransaction == null) {

				SIPServerTransaction nextTransaction = (SIPServerTransaction) transactionIterator
						.next();

				// If this transaction should handle this request,
				if (nextTransaction.isMessagePartOfTransaction(requestReceived)) {
					// Mark this transaction as the one
					// to handle this message
					currentTransaction = nextTransaction;
				}
			}
		}
		return currentTransaction;
	}

	public void removePendingTransaction(SIPServerTransaction tr) {
		synchronized (pendingTransactions) {
			this.pendingTransactions.remove(tr);
		}
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
	protected ServerRequestInterface newSIPServerRequest(
			SIPRequest requestReceived, MessageChannel requestMessageChannel) {

		// Iterator through all server transactions
		Iterator transactionIterator;
		// Next transaction in the set
		SIPServerTransaction nextTransaction;
		// Transaction to handle this request
		SIPServerTransaction currentTransaction;

		String key = requestReceived.getTransactionId();

		currentTransaction = (SIPServerTransaction) serverTransactionTable
				.get(key);
		if (currentTransaction == null
				|| !currentTransaction
						.isMessagePartOfTransaction(requestReceived)) {

			// Loop through all server transactions
			synchronized (serverTransactions) {
				transactionIterator = serverTransactions.iterator();
				currentTransaction = null;
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

				// If no transaction exists to handle this message
				if (currentTransaction == null) {
					currentTransaction = findPendingTransaction(requestReceived);
					if (currentTransaction != null)
						return currentTransaction;
					currentTransaction = createServerTransaction(requestMessageChannel);
					currentTransaction.setOriginalRequest(requestReceived);
					if (!isDialogCreated(requestReceived.getMethod())) {
						// Dialog is not created - can we find the state?
						// If so, then create a transaction and add it.
						String dialogId = requestReceived.getDialogId(true);
						SIPDialog dialog = getDialog(dialogId);
						// Sequence numbers are supposed to increment.
						// avoid processing old sequence numbers and
						// delivering the same request up to the
						// application if the request has already been seen.
						// Special handling applies to ACK processing.
						if (dialog != null
								&& (requestReceived.getMethod().equals(
										Request.ACK) || requestReceived
										.getCSeq().getSequenceNumber() > dialog
										.getRemoteSequenceNumber())) {
							// Found a dialog.
							if (LogWriter.needsLogging)
								logWriter
										.logMessage("adding server transaction "
												+ currentTransaction);
							serverTransactions.add(0, currentTransaction);
							addTransactionHash(currentTransaction);
							currentTransaction.startTransactionTimer();
							currentTransaction.isMapped = true;
						}
					} else {
						// Create the transaction but dont map it.
						String dialogId = requestReceived.getDialogId(true);
						SIPDialog dialog = getDialog(dialogId);
						// This is a dialog creating request that is part of an
						// existing dialog (eg. re-Invite). Re-invites get a non
						// null server transaction Id (unlike the original
						// invite).
						if (dialog != null
								&& requestReceived.getCSeq()
										.getSequenceNumber() > dialog
										.getRemoteSequenceNumber()) {
							currentTransaction.map();
							if (LogWriter.needsLogging)
								logWriter
										.logMessage("adding server transaction "
												+ currentTransaction);
							serverTransactions.add(0, currentTransaction);
							addTransactionHash(currentTransaction);
							currentTransaction.startTransactionTimer();
							currentTransaction.toListener = true;
						}

					}
				}
			}
		}

		// Set ths transaction's encapsulated request
		// interface from the superclass
		currentTransaction.setRequestInterface(super.newSIPServerRequest(
				requestReceived, currentTransaction));
		return currentTransaction;
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
	protected ServerResponseInterface newSIPServerResponse(
			SIPResponse responseReceived, MessageChannel responseMessageChannel) {
		//	System.out.println("response = " + responseReceived.encode());

		// Iterator through all client transactions
		Iterator transactionIterator;
		// Next transaction in the set
		SIPClientTransaction nextTransaction;
		// Transaction to handle this request
		SIPClientTransaction currentTransaction;

		String key = responseReceived.getTransactionId();

		currentTransaction = (SIPClientTransaction) clientTransactionTable
				.get(key);

		if (currentTransaction == null
				|| !currentTransaction
						.isMessagePartOfTransaction(responseReceived)) {
			// Loop through all server transactions
			synchronized (clientTransactions) {
				transactionIterator = clientTransactions.iterator();
				currentTransaction = null;
				while (transactionIterator.hasNext()
						&& currentTransaction == null) {

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
			}

			// If no transaction exists to handle this message,
			if (currentTransaction == null) {

				// Pass the message directly to the TU
				return super.newSIPServerResponse(responseReceived,
						responseMessageChannel);

			}
		}

		// Set ths transaction's encapsulated response interface
		// from the superclass
		currentTransaction.setResponseInterface(super.newSIPServerResponse(
				responseReceived, currentTransaction));
		return currentTransaction;

	}

	/**
	 * Creates a client transaction to handle a new request. Gets the real
	 * message channel from the superclass, and then creates a new client
	 * transaction wrapped around this channel.
	 * 
	 * @param nextHop
	 *            Hop to create a channel to contact.
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
			clientTransactions.add(0, returnChannel);
			((SIPClientTransaction) returnChannel)
					.setViaPort(nextHop.getPort());
			((SIPClientTransaction) returnChannel)
					.setViaHost(nextHop.getHost());
			// Add the transaction timer for the state machine.
			returnChannel.startTransactionTimer();
			return returnChannel;
		}

	}

	/**
	 * Create a client transaction from a raw channel.
	 * 
	 * @param rawChannel
	 *            is the transport channel to encapsulate.
	 */

	public MessageChannel createMessageChannel(MessageChannel rawChannel) {
		synchronized (clientTransactions) {
			// New client transaction to return
			SIPTransaction returnChannel = createClientTransaction(rawChannel);
			clientTransactions.add(0, returnChannel);
			((SIPClientTransaction) returnChannel).setViaPort(rawChannel
					.getViaPort());
			((SIPClientTransaction) returnChannel).setViaHost(rawChannel
					.getHost());
			// Add the transaction timer for the state machine.
			returnChannel.startTransactionTimer();
			return returnChannel;
		}
	}

	/**
	 * Create a client transaction from a raw channel.
	 * 
	 * @param transaction
	 *            is the transport channel to encapsulate.
	 */
	public MessageChannel createMessageChannel(SIPTransaction transaction) {
		synchronized (clientTransactions) {
			// New client transaction to return
			SIPTransaction returnChannel = createClientTransaction(transaction
					.getMessageChannel());
			clientTransactions.add(0, returnChannel);
			((SIPClientTransaction) returnChannel).setViaPort(transaction
					.getViaPort());
			((SIPClientTransaction) returnChannel).setViaHost(transaction
					.getViaHost());
			// Add the transaction timer for the state machine.
			returnChannel.startTransactionTimer();
			return returnChannel;
		}
	}

	/**
	 * Creates a client transaction that encapsulates a MessageChannel. Useful
	 * for implementations that want to subclass the standard
	 * 
	 * @param encapsulatedMessageChannel
	 *            Message channel of the transport layer.
	 */
	public SIPClientTransaction createClientTransaction(
			MessageChannel encapsulatedMessageChannel) {
		return new SIPClientTransaction(this, encapsulatedMessageChannel);
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
		return new SIPServerTransaction(this, encapsulatedMessageChannel);
	}

	/**
	 * Creates a raw message channel. A raw message channel has no transaction
	 * wrapper.
	 * 
	 * @param hop --
	 *            hop for which to create the raw message channel.
	 */
	public MessageChannel createRawMessageChannel(Hop hop)
			throws UnknownHostException {
		return super.createMessageChannel(hop);
	}

	/**
	 * Add a new client transaction to the set of existing transactions. Add it
	 * to the top of the list so an incoming response has less work to do in
	 * order to find the transaction.
	 * 
	 * @param clientTransaction --
	 *            client transaction to add to the set.
	 */
	public void addTransaction(SIPClientTransaction clientTransaction) {
		if (LogWriter.needsLogging)
			logWriter.logMessage("added transaction " + clientTransaction);
		synchronized (clientTransactions) {
			clientTransactions.add(0, clientTransaction);
		}

		addTransactionHash(clientTransaction);
		clientTransaction.startTransactionTimer();
	}

	/**
	 * Remove transaction.
	 */
	protected void removeTransaction(SIPTransaction sipTransaction) {
		if (sipTransaction instanceof SIPServerTransaction) {
			synchronized (serverTransactions) {
				serverTransactions.remove(sipTransaction);
			}
			synchronized (serverTransactionTable) {
				String key = sipTransaction.getTransactionId();
				serverTransactionTable.remove(key);
			}
		} else {
			synchronized (clientTransactions) {
				clientTransactions.remove(sipTransaction);
			}
			synchronized (clientTransactionTable) {
				String key = sipTransaction.getTransactionId();
				clientTransactionTable.remove(key);
			}
		}
	}

	/**
	 * Add a new server transaction to the set of existing transactions. Add it
	 * to the top of the list so an incoming ack has less work to do in order to
	 * find the transaction.
	 * 
	 * @param serverTransaction --
	 *            server transaction to add to the set.
	 */
	public void addTransaction(SIPServerTransaction serverTransaction)
			throws IOException {
		if (LogWriter.needsLogging)
			logWriter.logMessage("added transaction " + serverTransaction);
		synchronized (serverTransactions) {
			this.serverTransactions.add(0, serverTransaction);
			serverTransaction.map();
		}
		addTransactionHash(serverTransaction);
		serverTransaction.startTransactionTimer();
	}

	/**
	 * Hash table for quick lookup of transactions.
	 */
	protected void addTransactionHash(SIPTransaction sipTransaction) {
		SIPRequest sipRequest = sipTransaction.getOriginalRequest();
		Via via = sipRequest.getTopmostVia();
		// Cannot cache old style requests.
		/**
		 * if (via.getBranch() == null || !
		 * via.getBranch().toUpperCase().startsWith
		 * (SIPConstants.BRANCH_MAGIC_COOKIE.toUpperCase())){ return; }
		 */
		if (sipTransaction instanceof SIPClientTransaction) {
			synchronized (clientTransactionTable) {
				String key = sipRequest.getTransactionId();
				clientTransactionTable.put(key, sipTransaction);
			}
		} else {
			synchronized (serverTransactionTable) {
				String key = sipRequest.getTransactionId();
				serverTransactionTable.put(key, sipTransaction);
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
		if (sipTransaction instanceof SIPClientTransaction) {
			synchronized (clientTransactionTable) {
				String key = sipTransaction.getTransactionId();
				clientTransactionTable.remove(key);
			}
		} else if (sipTransaction instanceof SIPServerTransaction) {
			synchronized (serverTransactionTable) {
				String key = sipTransaction.getTransactionId();
				serverTransactionTable.remove(key);
			}
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
	 * @param transactionErrorEvent
	 *            Error event.
	 */
	public synchronized void transactionErrorEvent(
			SIPTransactionErrorEvent transactionErrorEvent) {
		SIPTransaction transaction = (SIPTransaction) transactionErrorEvent
				.getSource();
		// TODO
		if (transactionErrorEvent.getErrorID() == SIPTransactionErrorEvent.TRANSPORT_ERROR) {
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
	
	
	/**
	 * Scan the list of pending records and redispatch the pending requests on these pending
	 * records. Pending records are queued on transactions when a listener is referencing the
	 * transaction. Pending records are queued on Dialogs when requests arrive out of sequence.
	 *
	 */
	
	class PendingRecordScanner implements Runnable {
	    SIPTransactionStack myStack;

	    protected PendingRecordScanner(SIPTransactionStack myStack) {
	        this.myStack = myStack;
	    }

	    public void run() {
	        try {
	            PendingRecord pr = null;

	            while (true) {
	                synchronized (pendingRecords) {
	                    try {
	                        if (pendingRecords.size() == 0)
	                            pendingRecords.wait();

	                        if (!isAlive()) {
	                            return;
	                        } else if (pendingRecords.size() != 0 ) {
	    	                        pr = (PendingRecord) pendingRecords.get(0);
	    	                        pendingRecords.remove(0);
	                        } else continue;
	                    }
	                    catch (InterruptedException ex) {
	                        if (!isAlive())
	                            return;
	                        else
	                            continue;
	                    }
	                    
	                }
	                if (!pr.isTerminated())
	                    pr.processPending();
	            }
	        }
	        finally {
	            if (LogWriter.needsLogging)
	                logWriter.logMessage("exitting pendingRecordScanner!!");
	        }
	       
	    }
	}
   
	   

	    public void putPending(PendingRecord pendingRecord) {
	        synchronized (pendingRecords) { 
	            pendingRecords.add(pendingRecord);
	        }
	    }
	    public void removePending(PendingRecord pendingRecord) {
			synchronized (pendingRecords) {
				pendingRecords.remove(pendingRecord);
			}
	    }
	    public void notifyPendingRecordScanner() {
			synchronized (this.pendingRecords) {
				this.pendingRecords.notify();
			}
		}
	
	    
	    /**
	
	class PendingRecordScanner implements Runnable {
		SIPTransactionStack myStack;

		protected PendingRecordScanner(SIPTransactionStack myStack) {
			this.myStack = myStack;
		}
		
		public void run() {
			try {
				while (true) {
					LinkedList ll = new LinkedList();
					synchronized (pendingRecords) {
						try {
						    if (pendingRecords.size() == 0) 
						        pendingRecords.wait();
							if (!isAlive())
								return;
						} catch (InterruptedException ex) {
							if (!isAlive())
								return;
							else
								continue;
						}
						Iterator ti = pendingRecords.iterator();
						while (ti.hasNext()) {
							PendingRecord next = (PendingRecord) ti.next();
							if (next.hasPending()) {
								ll.add(next);
								ti.remove();
							} else if (next.isTerminated()) {
								// Clean up if the the transaction is terminated.
								ti.remove();
							}
						}
					}
					Iterator it = ll.iterator();
					while (it.hasNext()) {
						PendingRecord next = (PendingRecord) it.next();
						next.processPending();
					}
				}
			} finally {
				if (LogWriter.needsLogging)
					logWriter.logMessage("exitting pendingRecordScanner!!");
			}
		}
	}
    **/
	
	
	    

	/**
	 * Stop stack. Clear all the timer stuff.
	 */
	public void stopStack() {
		this.notifyPendingRecordScanner();
		this.timer.cancel();
		this.pendingTransactions.clear();
		super.stopStack();
	}

	/**
	* Put a transaction in the pending transaction list.
	* This is to avoid a race condition when a duplicate may arrive
	* when the application is deciding whether to create a transaction or not.
	*/
	public void putPendingTransaction( SIPServerTransaction tr ) {
		synchronized (pendingTransactions) {
			this.pendingTransactions.add(tr);
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.45  2004/10/06 16:57:50  mranga
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
 * Revision 1.44  2004/10/05 16:22:38  mranga
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
 *
 * Revision 1.43  2004/10/04 16:03:53  mranga
 * Reviewed by:   mranga
 * attempted fix for memory leak
 *
 * Revision 1.42  2004/10/01 16:05:08  mranga
 * Submitted by:  mranga
 * Fixed memory leak
 *
 * Revision 1.41  2004/07/23 06:50:05  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Clean up - Get rid of annoying eclipse warnings.
 * Revision 1.40 2004/07/16 17:13:56 mranga
 * Submitted by: Damand Joost Reviewed by: mranga
 * 
 * Make threads into daemon threads, use address for received = parameter on via
 * 
 * Revision 1.39 2004/07/01 05:42:23 mranga Submitted by: Pierre De Rop and
 * Thomas Froment Reviewed by: M. Ranganathan
 * 
 * More performance hacks.
 * 
 * Revision 1.38 2004/06/27 01:00:21 mranga Reviewed by: mranga Forgot to add
 * record to hashtable structure.
 * 
 * Revision 1.37 2004/06/27 00:41:52 mranga Submitted by: Thomas Froment and
 * Pierre De Rop Reviewed by: mranga Performance improvements (auxiliary data
 * structure for fast lookup of transactions).
 * 
 * Revision 1.36 2004/06/21 05:42:32 mranga Reviewed by: mranga more code
 * smithing
 * 
 * Revision 1.35 2004/06/21 05:32:22 mranga Submitted by: Reviewed by:
 * 
 * Revision 1.34 2004/06/21 04:59:52 mranga Refactored code - no functional
 * changes.
 * 
 * Revision 1.33 2004/06/17 15:22:31 mranga Reviewed by: mranga
 * 
 * Added buffering of out-of-order in-dialog requests for more efficient
 * processing of such requests (this is a performance optimization ).
 * 
 * Revision 1.32 2004/06/15 09:54:45 mranga Reviewed by: mranga re-entrant
 * listener model added. (see configuration property
 * gov.nist.javax.sip.REENTRANT_LISTENER)
 * 
 * Revision 1.31 2004/06/07 16:12:33 mranga Reviewed by: mranga removed
 * commented out code
 * 
 * Revision 1.30 2004/06/01 11:42:59 mranga Reviewed by: mranga timer fix missed
 * starting the transaction timer in a couple of places.
 * 
 * Revision 1.29 2004/05/31 18:12:56 mranga Reviewed by: mranga arrange
 * transactions in a synchronized list and insert transactions into the front
 * end of the list to improve transaction search time and scalability.
 * 
 * Revision 1.28 2004/05/30 18:55:58 mranga Reviewed by: mranga Move to timers
 * and eliminate the Transaction scanner Thread to improve scalability and
 * reduce cpu usage.
 * 
 * Revision 1.27 2004/04/07 13:46:30 mranga Reviewed by: mranga move processing
 * of delayed responses outside the synchronized block.
 * 
 * Revision 1.26 2004/04/07 00:19:24 mranga Reviewed by: mranga Fixes a
 * potential race condition for client transactions. Handle re-invites
 * statefully within an established dialog.
 * 
 * Revision 1.25 2004/03/30 15:38:18 mranga Reviewed by: mranga Name the threads
 * so as to facilitate debugging.
 * 
 * Revision 1.24 2004/03/30 15:17:39 mranga Reviewed by: mranga Added
 * reInitialization for stack in support of applets.
 * 
 * Revision 1.23 2004/03/09 00:34:44 mranga Reviewed by: mranga Added TCP
 * connection management for client and server side Transactions. See
 * configuration parameter gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false Releases Client TCP
 * Connections after linger time
 * 
 * Revision 1.22 2004/03/07 22:25:25 mranga Reviewed by: mranga Added a new
 * configuration parameter that instructs the stack to drop a server connection
 * after server transaction termination set
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this Default behavior
 * is true.
 * 
 * Revision 1.21 2004/02/13 13:55:32 mranga Reviewed by: mranga per the spec,
 * Transactions must always have a valid dialog pointer. Assigned a dummy dialog
 * for transactions that are not assigned to any dialog (such as Message).
 * 
 * Revision 1.20 2004/02/11 20:22:30 mranga Reviewed by: mranga tighten up the
 * sequence number checks for BYE processing.
 * 
 * Revision 1.19 2004/02/05 15:40:31 mranga Reviewed by: mranga Add check for
 * type when casting to SIPServerTransaction in TransactionScanner
 * 
 * Revision 1.18 2004/02/04 18:44:18 mranga Reviewed by: mranga check sequence
 * number before delivering event to application.
 * 
 * Revision 1.17 2004/01/27 13:52:11 mranga Reviewed by: mranga Fixed
 * server/user-agent parser. suppress sending ack to TU when retransFilter is
 * enabled and ack is retransmitted.
 * 
 * Revision 1.16 2004/01/22 18:39:41 mranga Reviewed by: M. Ranganathan Moved
 * the ifdef SIMULATION and associated tags to the first column so Prep
 * preprocessor can deal with them.
 * 
 * Revision 1.15 2004/01/22 14:23:45 mranga Reviewed by: mranga Fixed some minor
 * formatting issues.
 * 
 * Revision 1.14 2004/01/22 13:26:33 sverker Issue number: Obtained from:
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
