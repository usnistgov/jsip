package gov.nist.javax.sip;

import gov.nist.core.net.AddressResolver;
import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;

import java.util.Collection;

import javax.sip.Dialog;
import javax.sip.header.HeaderFactory;

/**
 * SIP Stack extensions to be added to the next spec revision. Only these may be safely used in
 * the interim between now and the next release. SipStackImpl implements this interface.
 * 
 * @author M. Ranganathan
 * 
 */
public interface SipStackExt {

    /**
     * Get the collection of dialogs currently in the Dialog table. This is useful for debugging
     * purposes.
     * 
     */
    public Collection<Dialog> getDialogs();

    /**
     * Get the ReferedTo dialog in the Replaces header.
     * 
     * @return Dialog object matching the Replaces header, provided it is in an appropriate state
     *         to be replaced, <code>null</code> otherwise
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
     * @return - the authentication helper which can be used for generating the appropriate
     *         headers for handling authentication challenges for user agents.
     * 
     * @since 2.0
     */
    public AuthenticationHelper getAuthenticationHelper(AccountManager accountManager,
            HeaderFactory headerFactory);

    /**
     * Set the address resolution interface. The address resolver allows you to register custom
     * lookup schemes ( for example DNS SRV lookup ) that are not directly supported by the JDK.
     * 
     * @param addressResolver -- the address resolver to set.
     * 
     * @since 2.0
     */
    public void setAddressResolver(AddressResolver addressResolver);

    /**
     * Get the dialog in the Join header.
     * 
     * @return Dialog object matching the Join header, provided it is in an appropriate state to
     *         be replaced, <code>null</code> otherwise
     * 
     * @since 2.0
     */
    public Dialog getJoinDialog(JoinHeader joinHeader);

    /**
     * Set the list of cipher suites supported by the stack. A stack can have only one set of
     * suites. These are not validated against the supported cipher suites of the java runtime, so
     * specifying a cipher here does not guarantee that it will work.<br>
     * The stack has a default cipher suite of:
     * <ul>
     * <li> TLS_RSA_WITH_AES_128_CBC_SHA </li>
     * <li> SSL_RSA_WITH_3DES_EDE_CBC_SHA </li>
     * <li> TLS_DH_anon_WITH_AES_128_CBC_SHA </li>
     * <li> SSL_DH_anon_WITH_3DES_EDE_CBC_SHA </li>
     * </ul>
     * 
     * <b>NOTE: This function must be called before adding a TLS listener</b>
     * 
     * @since 2.0
     * @param String[] The new set of ciphers to support.
     * @return
     * 
     */
    public void setEnabledCipherSuites(String[] newCipherSuites);

}
