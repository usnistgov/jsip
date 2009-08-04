package gov.nist.javax.sip;

import javax.sip.SipProvider;

/**
 * Extensions for Next specification revision. These interfaces will remain unchanged
 * and be merged with the next revision of the spec.
 *
 *
 * @author mranga
 *
 */
public interface DialogExt {

    /**
     * Returns the SipProvider that was used for the first transaction in this Dialog
     * @return SipProvider
     *
     * @since 2.0
     */
    public SipProvider getSipProvider() ;
    
    /**
     * Set a flag that instructs the implementation to take care of sequencing of re-INVITES.
     * If this flag is set then all the re-INVITEs that use the dialog will 
     * wait till an ACK has been sent on the previous re-INVITE before sending the
     * new re-INVITE. This prevents the UA recieving the re-INVITE from receiving an
     * out of order INVITE - which would result in it sending a 493 with a timeout
     * to indicate that the request should be re-transmitted. If this flag is set
     * the dialog.sendRequest works asynchronously. An error is reported on timeout
     * if the ACK is not sent within 8 seconds.
     * 
     * @since 2.0
     */
    public void setAllowReInviteInterleaving(boolean flag);
    

}
