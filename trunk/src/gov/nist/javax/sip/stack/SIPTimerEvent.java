package gov.nist.javax.sip.stack;
import java.util.*;

/**
 * An event that indicates that a transaction has encountered an error.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:33 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class SIPTimerEvent extends EventObject {

	public static int RETRANSMISSION = 1;

	private int eventId;

	/**
	 *	Creates a transaction error event.
	 *
	 *	@param sourceTransaction Transaction which is raising the error.
	 *	@param transactionErrorID ID of the error that has ocurred.
	 */
	SIPTimerEvent(SIPTransaction sourceTransaction, int eventId) {
		super(sourceTransaction);
		this.eventId = eventId;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
