package gov.nist.javax.sip;

import javax.sip.ClientTransaction;
import javax.sip.Timeout;
import javax.sip.TimeoutEvent;

/**
 * An extended TimeoutEvent that is delivered to the application when a 1xx is not within 
 * 500ms.
 */
public class TimeoutEventExt extends TimeoutEvent {
    private boolean is1XXTimeout;
    
    
    
    public TimeoutEventExt(Object source, 
            ClientTransaction clientTransaction, Timeout timeout, boolean is1XXTimeout ) {
        super(source, clientTransaction, timeout);
        this.is1XXTimeout = is1XXTimeout;
    }
    
    public boolean is1XXTimeout() {
        return is1XXTimeout;
    }
    

}
