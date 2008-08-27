
package gov.nist.javax.sip;

import javax.sip.SipProvider;

public interface TransactionExt {
    
    /**
     * Get the Sip Provider associated with this transaction
     */
    public SipProvider getSipProvider();

}
