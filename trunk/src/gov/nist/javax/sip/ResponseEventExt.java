package gov.nist.javax.sip;

import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.message.Response;

/**
 * Extension for ResponseEvent. 
 * 
 * @since v2.0
 */
public class ResponseEventExt extends ResponseEvent {
    private ClientTransactionExt  m_originalTransaction;
    private boolean isForked;
    private boolean isRetransmission;
    
    public ResponseEventExt(Object source, ClientTransactionExt clientTransaction, 
            Dialog dialog,  Response response) {
        super(source,clientTransaction,dialog,response);
        m_originalTransaction = clientTransaction;
    }
    
    /**
     * Return true if this is a forked response.
     * 
     * @return true if the response event is for a forked response.
     */
    public boolean isForkedResponse() {
        return isForked;
    }
    
    /**
     * Set true if this is a forked response.
     * 
     * @return true if the response event is for a forked response.
     */
    public void setForkedResponse(boolean forked) {
        this.isForked= forked;
    }
    
    /**
     * Set the original transaction for a forked response.
     * 
     * @param originalTransaction - the original transaction for which this response event is a fork.
     */
    public void setOriginalTransaction(ClientTransactionExt originalTransaction ) {
        m_originalTransaction = originalTransaction;
    }
    
    /**
     * Get the original transaction for which this is a forked response.
     * Note that this transaction can be in a TERMINATED state.
     * 
     * @return the original clientTx for which this is a forked response.
     */
    public ClientTransactionExt getOriginalTransaction() {
        return this.m_originalTransaction;
    }
    
    /**
     * Return true if this is a forked response.
     * 
     * @return true if the response event is for a forked response.
     */
    public boolean isRetransmission() {
        return isRetransmission;
    }

    /**
     * @param isRetransmission the isRetransmission to set
     */
    public void setRetransmission(boolean isRetransmission) {
        this.isRetransmission = isRetransmission;
    }
}
