package gov.nist.javax.sip;

import gov.nist.javax.sip.message.SIPResponse;

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
        if(getClientTransaction() == null && getOriginalTransaction() == null) {            
            return true;
        } else {
            return ((SIPResponse)getResponse()).isRetransmission();
        }
    }
}
