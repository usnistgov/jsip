package gov.nist.javax.sip.stack;

import java.util.EventObject;

/**
 * An event that indicates that a transaction has encountered an error.
 *
 * @author Jeff Keyser 
 * @author M. Ranganathan
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:33 $
 */
public class SIPTransactionErrorEvent extends EventObject {

	/**
	 *	This event ID indicates that the transaction has timed out.
	 */
	public static final int TIMEOUT_ERROR = 1;

	/**
	 * This event ID indicates that there was an error sending a message using
	 * the underlying transport.
	 */
	public static final int TRANSPORT_ERROR = 2;

	/**
	 * Retransmit signal to application layer.
	 */
	public static final int TIMEOUT_RETRANSMIT = 3;

	// ID of this error event
	private int errorID;

	/**
	 * Creates a transaction error event.
	 *
	 * @param sourceTransaction Transaction which is raising the error.
	 * @param transactionErrorID ID of the error that has ocurred.
	 */
	SIPTransactionErrorEvent(
		SIPTransaction sourceTransaction,
		int transactionErrorID) {

		super(sourceTransaction);
		errorID = transactionErrorID;

	}

	/**
	 * Returns the ID of the error.
	 *
	 * @return Error ID.
	 */
	public int getErrorID() {
		return errorID;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
