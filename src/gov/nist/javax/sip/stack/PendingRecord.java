package gov.nist.javax.sip.stack;
public interface PendingRecord {
	/** Return true if this has a pending request
	*/
	public boolean hasPending();
	/** Clear any pending request flag.
	*/
	public void clearPending();
	/** Process the pending request.
	*/
	public void processPending();

	/** Signal that we are done with this record.
	*/
	public boolean isTerminated();
}
