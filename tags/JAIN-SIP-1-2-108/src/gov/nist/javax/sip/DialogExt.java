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

}
