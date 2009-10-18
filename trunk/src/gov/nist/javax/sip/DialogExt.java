package gov.nist.javax.sip;

import javax.sip.Dialog;
import javax.sip.SipProvider;

/**
 * Extensions for Next specification revision. These interfaces will remain unchanged
 * and be merged with the next revision of the spec.
 *
 *
 * @author mranga
 *
 */
public interface DialogExt extends Dialog {

    /**
     * Returns the SipProvider that was used for the first transaction in this Dialog
     * @return SipProvider
     *
     * @since 2.0
     */
    public SipProvider getSipProvider() ;
    
    /**
     * Sets a flag that indicates that this Dialog is part of a BackToBackUserAgent.
     * If this flag is set, INVITEs are not allowed to interleave and timed out ACK
     * transmission results in a BYE being sent to the other side.
     * 
     * @since 2.0
     */
    public void setBackToBackUserAgent(boolean flag);
    
    

}
