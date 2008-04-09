package gov.nist.javax.sip;

import gov.nist.core.net.AddressResolver;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;

import java.util.Collection;

import javax.sip.Dialog;
import javax.sip.header.HeaderFactory;

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

  
	
	/**
	 * Get the authentication helper.
	 * 
	 * 
	 * @param accountManager -- account manager (for fetching credentials).
	 * @param headerFactory -- header factory.
	 * 
	 * @return - the authentication helper which can be used for generating the appropriate headers for
	 * 		handling authentication challenges for user agents.
	 * 
	 * @since 2.0
	 */
	public AuthenticationHelper getAuthenticationHelper(AccountManager accountManager,
			HeaderFactory headerFactory);
	
	/**
	 * Set the address resolution interface. The address resolver allows you to register
	 * custom lookup schemes ( for example DNS SRV lookup ) that are not directly supported
	 * by the JDK.
	 * 
	 * @param addressResolver --
	 *            the address resolver to set.
	 *            
	 * @since 2.0 
	 */
	public void setAddressResolver(AddressResolver addressResolver);
	
	
	  /**
	   * @todo discuss
	   * 
	   *
	   * Finds a Dialog to join based on the given Join header
	   *
	   * JvB: proposal
	   *
	   * public Dialog getJoinDialog( JoinHeader joinHeader);
	   */

}
