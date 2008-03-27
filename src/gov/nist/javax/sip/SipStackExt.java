package gov.nist.javax.sip;

import gov.nist.javax.sip.header.extensions.ReplacesHeader;

import java.util.Collection;

import javax.sip.Dialog;

/**
 * SIP Stack extensions to be added to the next spec revision. Only these may be 
 * safely used in the interim between now and the next release. SipStackImpl implements
 * this interface.
 * 
 * @author M. Ranganathan
 *
 */
public interface SipStackExt {
	
	/**
	 * Get the collection of dialogs currently in the Dialog table. This is useful for debugging purposes.
	 * 
	 */
	public Collection<Dialog> getDialogs() ;
	
	/**
	 * Get the ReferedTo dialog in the Replaces header.
	 *
	 * @return Dialog object matching the Replaces header, provided it is in an 
	 *         appropriate state to be replaced, <code>null</code> otherwise
	 *
	 * @since 2.0
	 */
	public Dialog getReplacesDialog(ReplacesHeader replacesHeader);

}
