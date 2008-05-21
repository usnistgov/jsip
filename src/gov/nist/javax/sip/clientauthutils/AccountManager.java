package gov.nist.javax.sip.clientauthutils;

import javax.sip.address.SipURI;

public interface AccountManager {
	
	/**
	 * Returns the user credentials for a given SIP Domain.
	 * You can implement any desired method (such as popping up a dialog for example ) 
	 * to retrieve the credentials.
	 * 
	 * @param requestUri -- the request uri of the request being challenged.
	 * @param domain -- the authentication domain.
	 * 
	 * @return -- the user credentials associated with the domain. 
	 */

	UserCredentials getCredentials(SipURI requestUri, String domain);

}
