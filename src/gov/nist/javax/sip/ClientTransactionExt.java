package gov.nist.javax.sip;

import javax.sip.Timeout;

public interface ClientTransactionExt {

    /**
     * Notify on retransmission from the client transaction side. The listener will get a
     * notification on retransmission when this flag is set. When set the client transaction
     * listener will get a Timeout.RETRANSMIT event on each retransmission.
     * 
     * @param flag -- the flag that indicates whether or not notification is desired.
     * 
     * @since 2.0
     */
    public void setNotifyOnRetransmit(boolean flag);

    /**
     * Send a transaction timeout event to the application if Tx is still in Calling state in the
     * given time period ( in base timer interval count ) after sending request. The stack will
     * start a timer and alert the application if the client transaction does not transition out
     * of the Trying state by the given interval. This is a "one shot" alert.
     * 
     * @param count -- the number of base timer intervals after which an alert is issued.
     * 
     * 
     * @since 2.0
     */
    public void alertIfStillInCallingStateBy(int count);

}
