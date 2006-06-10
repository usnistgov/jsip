package gov.nist.javax.sip.stack;

import java.util.EventListener;

/**
 * Interface implemented by classes that want to be notified of asynchronous
 * transacion events.
 *
 * @author Jeff Keyser
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:33 $
 */
public interface SIPTransactionEventListener extends EventListener {

	/**
	 * Invoked when an error has ocurred with a transaction.
	 *
	 * @param transactionErrorEvent Error event.
	 */
	public void transactionErrorEvent(SIPTransactionErrorEvent transactionErrorEvent);
}
/*
 * $Log: not supported by cvs2svn $
 */
