package gov.nist.javax.sip;

import javax.sip.Timeout;

public interface ClientTransactionExt {
	
	/**
	 * Notify on retransmission from the client transaction side.
	 * The listener will get a notification on retransmission when this flag 
	 * is set. When set the client transaction listener will get a Timeout.RETRANSMIT
	 * event on each retransmission.
	 * 
	 * @param flag -- the flag that indicates whether or not notification is desired.
	 * 
	 * @since 2.0
	 */
	public void setNotifyOnRetransmit(boolean flag) ;
	

}
