package gov.nist.javax.sip;

import java.util.Collection;

import javax.sip.Dialog;

/**
 * SIP Stack extensions to be added to the next spec revision.
 * 
 * @author mranga
 *
 */
public interface SipStackExtensions {
	
	/**
	 * Get the collection of dialogs currently in the Dialog table. This is useful for debugging purposes.
	 * 
	 */
	public Collection<Dialog> getDialogs() ;

}
