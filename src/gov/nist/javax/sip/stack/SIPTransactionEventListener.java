package gov.nist.javax.sip.stack;


import java.util.EventListener;


/**
 *	Interface implemented by classes that want to be notified of asynchronous
 * transacion events.
 *
 *	@author	Jeff Keyser
 */
public interface SIPTransactionEventListener
	extends EventListener {

	/**
	 *	Invoked when an error has ocurred with a transaction.
	 *
	 *	@param transactionErrorEvent Error event.
	 */
	public void transactionErrorEvent(
		SIPTransactionErrorEvent	transactionErrorEvent
	);

}
